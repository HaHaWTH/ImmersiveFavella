package immersive_favella.item;

import immersive_favella.resources.*;
import immersive_favella.Common;
import immersive_favella.Config;
import immersive_favella.Sounds;
import immersive_favella.network.Network;
import immersive_favella.network.s2c.MelodyListMessage;
import immersive_favella.network.s2c.OpenGuiMessage;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.*;

public class InstrumentItem extends Item {
    public static final String TAG_PLAYING = "playing";
    public static final String TAG_MELODY = "melody";
    public static final String TAG_START_TIME = "start_time";
    public static final String TAG_TRACKS = "tracks";
    public static final String TAG_WAS_HELD = "was_held";

    public static class ClientPlaybackState {
        public long realTimeBase = 0;
        public long lastElapsed = -1;
        public String signature = "";
        public long lastStartTime = -1;
        public String lastMelody = "";
    }

    public static class ServerPlaybackState {
        public long lastElapsed = -1;
        public String signature = "";
    }

    private static final WeakHashMap<Entity, ClientPlaybackState> CLIENT_STATES = new WeakHashMap<>();
    private static final WeakHashMap<Entity, ServerPlaybackState> SERVER_STATES = new WeakHashMap<>();

    private ClientPlaybackState getClientState(Entity player) {
        ClientPlaybackState state = CLIENT_STATES.get(player);
        if (state == null) {
            state = new ClientPlaybackState();
            CLIENT_STATES.put(player, state);
        }
        return state;
    }

    private ServerPlaybackState getServerState(Entity player) {
        ServerPlaybackState state = SERVER_STATES.get(player);
        if (state == null) {
            state = new ServerPlaybackState();
            SERVER_STATES.put(player, state);
        }
        return state;
    }

    private static final long MAX_CATCHUP_MS = 200L;

    private static final Map<String, ParticleOffset> PARTICLE_OFFSETS = buildParticleOffsets();

    private final String instrumentName;
    private final long defaultSustain;

    private static class ParticleOffset {
        final double x;
        final double y;
        final double z;

        ParticleOffset(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static Map<String, ParticleOffset> buildParticleOffsets() {
        Object2ObjectArrayMap<String, ParticleOffset> map = new Object2ObjectArrayMap<>();
        map.put("bagpipe", new ParticleOffset(0.5, 0.6, 0.05));
        map.put("didgeridoo", new ParticleOffset(0.0, -0.45, 1.0));
        map.put("flute", new ParticleOffset(0.0, 0.15, 0.9));
        map.put("lute", new ParticleOffset(0.0, 0.0, 0.5));
        map.put("piano", new ParticleOffset(0.0, 0.25, 0.5));
        map.put("triangle", new ParticleOffset(0.0, 0.0, 0.6));
        map.put("trumpet", new ParticleOffset(0.0, 0.25, 1.4));
        map.put("tiny_drum", new ParticleOffset(0.0, 0.25, 0.5));
        map.put("vielle", new ParticleOffset(-0.25, 0.4, 0.35));
        map.put("ender_bass", new ParticleOffset(0.0, 0.0, 0.65));
        map.put("handpan", new ParticleOffset(0.0, 0.25, 0.5));
        map.defaultReturnValue(new ParticleOffset(0.0, 0.25, 0.5));
        return map;
    }

    public InstrumentItem(String instrumentName, long defaultSustain) {
        this.instrumentName = instrumentName;
        this.defaultSustain = defaultSustain;
    }

    public InstrumentItem() {
        this("lute", 300L);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            if (player instanceof EntityPlayerMP) {
                EntityPlayerMP mp = (EntityPlayerMP) player;
                Map<ResourceLocation, MelodyDescriptor> list = ServerMelodyManager.listMelodies(player);
                Network.sendToPlayer(new MelodyListMessage(list), mp);
                Network.sendToPlayer(new OpenGuiMessage(OpenGuiMessage.SELECTOR), mp);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    public void play(ItemStack stack, ResourceLocation melody, World world, EntityPlayer player) {
        NBTTagCompound tag = stack.getOrCreateSubCompound(Common.MOD_ID);
        tag.setString(TAG_MELODY, melody.toString());
        tag.setBoolean(TAG_PLAYING, true);
        tag.setLong(TAG_START_TIME, world.getTotalWorldTime());
        if (player != null) {
            refreshTracks(stack, world, player);
        }
    }

    public void play(ItemStack stack, World world) {
        NBTTagCompound tag = stack.getOrCreateSubCompound(Common.MOD_ID);
        tag.setBoolean(TAG_PLAYING, true);
    }

    private void resetToBeginning(ItemStack stack, World world) {
        NBTTagCompound tag = stack.getOrCreateSubCompound(Common.MOD_ID);
        tag.setLong(TAG_START_TIME, world.getTotalWorldTime());
    }

    public void play(ItemStack stack) {
        NBTTagCompound tag = stack.getOrCreateSubCompound(Common.MOD_ID);
        tag.setBoolean(TAG_PLAYING, true);
    }

    public void pause(ItemStack stack, World world) {
        NBTTagCompound tag = stack.getOrCreateSubCompound(Common.MOD_ID);
        tag.setBoolean(TAG_PLAYING, false);
    }

    public void pause(ItemStack stack) {
        NBTTagCompound tag = stack.getOrCreateSubCompound(Common.MOD_ID);
        tag.setBoolean(TAG_PLAYING, false);
    }

    public void refreshTracks(ItemStack stack, World world, EntityPlayer player) {
        NBTTagCompound tag = stack.getSubCompound(Common.MOD_ID);
        if (tag == null) return;
        String melodyStr = tag.getString(TAG_MELODY);
        if (melodyStr == null || melodyStr.isEmpty()) return;
        ResourceLocation melody = new ResourceLocation(melodyStr);
        String identifier = ServerMelodyManager.getIdentifier(instrumentName);
        Set<Integer> enabled = ServerMelodyManager.getEnabledTracks(world, melody, identifier);
        int[] array = new int[enabled.size()];
        int i = 0;
        for (Integer track : enabled) {
            array[i++] = track;
        }
        tag.setIntArray(TAG_TRACKS, array);
    }

    public Set<Integer> getEnabledTracks(ItemStack stack) {
        Set<Integer> set = new IntOpenHashSet();
        if (!stack.hasTagCompound() || stack.getSubCompound(Common.MOD_ID) == null) return set;
        int[] values = stack.getSubCompound(Common.MOD_ID).getIntArray(TAG_TRACKS);
        for (int v : values) set.add(v);
        return set;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public boolean isPlaying(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) return false;
        NBTTagCompound tag = stack.getSubCompound(Common.MOD_ID);
        return tag != null && tag.getBoolean(TAG_PLAYING);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
        if (!(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entity;

        boolean isMainHand = isSelected;
        boolean isOffhand = player.getHeldItemOffhand() == stack;
        boolean isHeld = isMainHand || isOffhand;

        if (isOffhand) {
            ItemStack mainStack = player.getHeldItemMainhand();
            if (!mainStack.isEmpty() && mainStack.getItem() instanceof InstrumentItem) {
                isHeld = false;
            }
        }

        NBTTagCompound tag = stack.getOrCreateSubCompound(Common.MOD_ID);

        if (!tag.getBoolean(TAG_PLAYING)) {
            tag.setBoolean(TAG_WAS_HELD, isHeld);
            return;
        }

        boolean wasHeld = tag.getBoolean(TAG_WAS_HELD);
        tag.setBoolean(TAG_WAS_HELD, isHeld);

        if (!isHeld) {
            if (wasHeld) {
                resetToBeginning(stack, world);
            }
            return;
        }

        if (!wasHeld) {
            resetToBeginning(stack, world);
        }

        if (world.isRemote) {
            clientTickPlayback(stack, world, entity);
        } else {
            serverTickPlayback(stack, world, entity);
        }
    }

    private void clientTickPlayback(ItemStack stack, World world, Entity entity) {
        NBTTagCompound tag = stack.getSubCompound(Common.MOD_ID);
        if (tag == null || !tag.getBoolean(TAG_PLAYING)) return;

        String melodyName = tag.getString(TAG_MELODY);
        if (melodyName == null || melodyName.isEmpty()) return;

        long startTime = tag.getLong(TAG_START_TIME);
        String signature = melodyName + "@" + startTime;

        ClientPlaybackState state = getClientState(entity);

        if (!signature.equals(state.signature)) {
            long worldTime = world.getTotalWorldTime();
            long elapsedMs = (worldTime - startTime) * 50L;
            if (elapsedMs < 0) elapsedMs = 0;

            boolean isSync = melodyName.equals(state.lastMelody) &&
                    state.lastStartTime != -1 &&
                    Math.abs(startTime - state.lastStartTime) < 40;

            state.realTimeBase = System.currentTimeMillis() - elapsedMs;

            if (!isSync) {
                state.lastElapsed = Math.max(-1L, elapsedMs - 50L);
            } else {
                if (state.lastElapsed > elapsedMs) {
                    state.lastElapsed = elapsedMs;
                }
            }

            state.signature = signature;
            state.lastStartTime = startTime;
            state.lastMelody = melodyName;
        }

        Melody melody = ClientMelodyManager.getMelody(new ResourceLocation(melodyName));
        if (melody == null || melody == Melody.DEFAULT) return;

        long elapsed = System.currentTimeMillis() - state.realTimeBase;
        if (elapsed < 0) elapsed = 0;

        if (elapsed < state.lastElapsed) elapsed = state.lastElapsed;

        long windowStart = state.lastElapsed;
        if (windowStart >= 0L && elapsed - windowStart > MAX_CATCHUP_MS) {
            windowStart = elapsed - MAX_CATCHUP_MS;
        }

        Set<Integer> enabledTracks = getEnabledTracks(stack);
        List<Track> tracks = melody.getTracks();
        for (int trackIndex = 0; trackIndex < tracks.size(); trackIndex++) {
            if (!enabledTracks.isEmpty() && !enabledTracks.contains(trackIndex)) continue;
            Track track = tracks.get(trackIndex);
            for (Note note : track.getNotes()) {
                int t = note.getTime();
                if (t > elapsed) break;
                if (t > windowStart && t <= elapsed) {
                    playNote(world, entity, note);
                }
            }
        }

        state.lastElapsed = elapsed;

        int length = melody.getLength();
        if (length > 0 && elapsed > length) {
            long wrapped = elapsed % length;
            state.realTimeBase = System.currentTimeMillis() - wrapped;
            state.lastElapsed = wrapped;
            state.signature = "";
        }
    }

    public void serverTickPlayback(ItemStack stack, World world, Entity entity) {
        NBTTagCompound tag = stack.getSubCompound(Common.MOD_ID);
        if (tag == null || !tag.getBoolean(TAG_PLAYING)) return;

        String melodyName = tag.getString(TAG_MELODY);
        if (melodyName == null || melodyName.isEmpty()) return;

        Melody melody = ServerMelodyManager.getMelody(world, new ResourceLocation(melodyName));
        if (melody == null) return;

        long startTime = tag.getLong(TAG_START_TIME);
        String signature = melodyName + "@" + startTime;

        ServerPlaybackState state = getServerState(entity);

        if (!signature.equals(state.signature)) {
            state.signature = signature;
            state.lastElapsed = -1L;
        }

        long elapsed = (world.getTotalWorldTime() - startTime) * 50L;
        if (elapsed < 0) elapsed = 0;

        long windowStart = state.lastElapsed;

        Set<Integer> enabledTracks = getEnabledTracks(stack);
        List<Track> tracks = melody.getTracks();
        for (int trackIndex = 0; trackIndex < tracks.size(); trackIndex++) {
            if (!enabledTracks.isEmpty() && !enabledTracks.contains(trackIndex)) continue;
            Track track = tracks.get(trackIndex);
            for (Note note : track.getNotes()) {
                int t = note.getTime();
                if (t > elapsed) break;
                if (t > windowStart && t <= elapsed) {
                    playNote(world, entity, note);
                }
            }
        }

        state.lastElapsed = elapsed;

        int length = melody.getLength();
        if (length > 0 && elapsed > length) {
            tag.setLong(TAG_START_TIME, world.getTotalWorldTime());
            state.lastElapsed = -1L;
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (slotChanged) return true;
        return oldStack.getItem() != newStack.getItem();
    }

    private void playNote(World world, Entity entity, Note note) {
        float volume = note.getVelocity() / 127.0f * Config.getInstance().instrumentVolumeFactor;
        float pitch = (float) Math.pow(2.0, (note.getNote() - 24) / 12.0);
        int octave = 1;
        while (octave < 8 && pitch > (4.0f / 3.0f)) {
            pitch *= 0.5f;
            octave++;
        }

        float factor = Config.getInstance().perceivedLoudnessAdjustmentFactor;
        float adjusted = (float) (volume / Math.sqrt(pitch * Math.pow(2, octave - 4)));
        volume = volume * (1.0f - factor) + adjusted * factor;

        if (world.isRemote && Config.getInstance().enableUnderwaterSoundEffect) {
            Entity listener = Minecraft.getMinecraft().getRenderViewEntity();
            if (listener != null && listener.isInsideOfMaterial(Material.WATER)) {
                volume *= 0.45f;
                pitch *= 0.86f;
            }
        }

        SoundEvent event = Sounds.get(instrumentName, octave);
        if (event != null) {
            if (world.isRemote) {
                world.playSound(entity.posX, entity.posY, entity.posZ, event, SoundCategory.NEUTRAL, Math.max(0.01f, volume), pitch, false);
                spawnNoteParticle(world, entity, note);
            } else {
                if (entity instanceof EntityPlayer) {
                    world.playSound((EntityPlayer) entity, entity.posX, entity.posY, entity.posZ, event, SoundCategory.NEUTRAL, Math.max(0.01f, volume), pitch);
                } else {
                    world.playSound(null, entity.posX, entity.posY, entity.posZ, event, SoundCategory.NEUTRAL, Math.max(0.01f, volume), pitch);
                }
            }
        }
    }

    private void spawnNoteParticle(World world, Entity entity, Note note) {
        if (!world.isRemote) return;
        if (!shouldRenderParticle(entity)) return;

        float yaw = entity.rotationYaw;
        if (entity instanceof EntityLivingBase) {
            yaw = ((EntityLivingBase) entity).renderYawOffset;
        }
        double x = Math.sin(-yaw / 180.0 * Math.PI);
        double z = Math.cos(-yaw / 180.0 * Math.PI);

        ParticleOffset offset = PARTICLE_OFFSETS.get(instrumentName);

        double px = entity.posX + x * offset.z + z * offset.x;
        double py = entity.posY + entity.height / 2.0 + offset.y;
        double pz = entity.posZ + z * offset.z - x * offset.x;

        world.spawnParticle(EnumParticleTypes.NOTE, px, py, pz, x * 5.0, 0.0, z * 5.0);
    }

    private boolean shouldRenderParticle(Entity sourceEntity) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return false;
        Entity camera = mc.getRenderViewEntity();
        if (camera == null) return false;
        if (camera != sourceEntity) return true;
        return mc.gameSettings.thirdPersonView != 0;
    }

    public void playTransientNote(World world, Entity entity, Note note) {
        playNote(world, entity, note);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.hasTagCompound() && stack.getSubCompound(Common.MOD_ID) != null && stack.getSubCompound(Common.MOD_ID).getBoolean(TAG_PLAYING)) {
            tooltip.add(I18n.format("immersive_favella.playing"));
        }
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}