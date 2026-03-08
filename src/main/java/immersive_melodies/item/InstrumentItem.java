package immersive_melodies.item;

import immersive_melodies.Common;
import immersive_melodies.ImmersiveMelodies;
import immersive_melodies.network.Network;
import immersive_melodies.network.s2c.MelodyListMessage;
import immersive_melodies.network.s2c.OpenGuiMessage;
import immersive_melodies.resources.ClientMelodyManager;
import immersive_melodies.resources.MelodyDescriptor;
import immersive_melodies.resources.Note;
import immersive_melodies.resources.ServerMelodyManager;
import immersive_melodies.Sounds;
import immersive_melodies.Config;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InstrumentItem extends Item {
    public static final String TAG_PLAYING = "playing";
    public static final String TAG_MELODY = "melody";
    public static final String TAG_START_TIME = "start_time";
    public static final String TAG_TRACKS = "tracks";
    public static final String TAG_LAST_ELAPSED = "last_elapsed";
    public static final String TAG_PAUSED_ELAPSED = "paused_elapsed";
    public static final String TAG_HIDDEN_PAUSE = "hidden_pause";
    private static final long MAX_CATCHUP_MS = 200L;

    private static final Map<String, Long> CLIENT_LAST_ELAPSED = new ConcurrentHashMap<String, Long>();
    private static final Map<String, Long> CLIENT_BASE_MS = new ConcurrentHashMap<String, Long>();
    private static final Map<String, String> CLIENT_SIGNATURE = new ConcurrentHashMap<String, String>();
    private static final Map<String, Long> PARTICLE_LAST_MS = new ConcurrentHashMap<String, Long>();

    private static final Map<String, Long> SERVER_LAST_ELAPSED = new ConcurrentHashMap<String, Long>();
    private static final Map<String, Long> SERVER_BASE_MS = new ConcurrentHashMap<String, Long>();
    private static final Map<String, String> SERVER_SIGNATURE = new ConcurrentHashMap<String, String>();

    private static final long PARTICLE_THROTTLE_MS = 35L;

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
        Map<String, ParticleOffset> map = new HashMap<String, ParticleOffset>();
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
                Network.sendToPlayer(new OpenGuiMessage(OpenGuiMessage.SELECTOR), mp);
                Map<ResourceLocation, MelodyDescriptor> list = ServerMelodyManager.listMelodies(player);
                Network.sendToPlayer(new MelodyListMessage(list), mp);
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    public void play(ItemStack stack, ResourceLocation melody, World world, EntityPlayer player) {
        stack.getOrCreateSubCompound(Common.MOD_ID).setString(TAG_MELODY, melody.toString());
        stack.getOrCreateSubCompound(Common.MOD_ID).setBoolean(TAG_PLAYING, true);
        stack.getOrCreateSubCompound(Common.MOD_ID).setLong(TAG_START_TIME, world.getTotalWorldTime());
        stack.getOrCreateSubCompound(Common.MOD_ID).setLong(TAG_LAST_ELAPSED, -1L);
        stack.getOrCreateSubCompound(Common.MOD_ID).setLong(TAG_PAUSED_ELAPSED, 0L);
        stack.getOrCreateSubCompound(Common.MOD_ID).setBoolean(TAG_HIDDEN_PAUSE, false);
        if (player != null) {
            refreshTracks(stack, world, player);
        }
    }

    public void play(ItemStack stack, World world) {
        stack.getOrCreateSubCompound(Common.MOD_ID).setBoolean(TAG_PLAYING, true);
        long paused = stack.getOrCreateSubCompound(Common.MOD_ID).getLong(TAG_PAUSED_ELAPSED);
        if (paused > 0L) {
            stack.getOrCreateSubCompound(Common.MOD_ID).setLong(TAG_START_TIME, world.getTotalWorldTime() - paused / 50L);
            stack.getOrCreateSubCompound(Common.MOD_ID).setLong(TAG_LAST_ELAPSED, paused);
            stack.getOrCreateSubCompound(Common.MOD_ID).setLong(TAG_PAUSED_ELAPSED, 0L);
        }
        stack.getOrCreateSubCompound(Common.MOD_ID).setBoolean(TAG_HIDDEN_PAUSE, false);
        if (!stack.getOrCreateSubCompound(Common.MOD_ID).hasKey(TAG_LAST_ELAPSED)) {
            stack.getOrCreateSubCompound(Common.MOD_ID).setLong(TAG_LAST_ELAPSED, -1L);
        }
    }

    public void play(ItemStack stack) {
        stack.getOrCreateSubCompound(Common.MOD_ID).setBoolean(TAG_PLAYING, true);
        stack.getOrCreateSubCompound(Common.MOD_ID).setBoolean(TAG_HIDDEN_PAUSE, false);
    }

    public void pause(ItemStack stack, World world) {
        long elapsed = (world.getTotalWorldTime() - stack.getOrCreateSubCompound(Common.MOD_ID).getLong(TAG_START_TIME)) * 50L;
        long last = stack.getOrCreateSubCompound(Common.MOD_ID).getLong(TAG_LAST_ELAPSED);
        stack.getOrCreateSubCompound(Common.MOD_ID).setLong(TAG_PAUSED_ELAPSED, Math.max(last, elapsed));
        stack.getOrCreateSubCompound(Common.MOD_ID).setBoolean(TAG_PLAYING, false);
        stack.getOrCreateSubCompound(Common.MOD_ID).setBoolean(TAG_HIDDEN_PAUSE, false);
    }

    public void pause(ItemStack stack) {
        stack.getOrCreateSubCompound(Common.MOD_ID).setBoolean(TAG_PLAYING, false);
        stack.getOrCreateSubCompound(Common.MOD_ID).setBoolean(TAG_HIDDEN_PAUSE, false);
    }

    public void refreshTracks(ItemStack stack, World world, EntityPlayer player) {
        if (stack.getSubCompound(Common.MOD_ID) == null) {
            return;
        }
        String melodyStr = stack.getSubCompound(Common.MOD_ID).getString(TAG_MELODY);
        if (melodyStr == null || melodyStr.isEmpty()) {
            return;
        }
        ResourceLocation melody = new ResourceLocation(melodyStr);
        String identifier = ServerMelodyManager.getIdentifier(instrumentName);
        Set<Integer> enabled = ServerMelodyManager.getEnabledTracks(world, melody, identifier);
        int[] array = new int[enabled.size()];
        int i = 0;
        for (Integer track : enabled) {
            array[i++] = track;
        }
        stack.getSubCompound(Common.MOD_ID).setIntArray(TAG_TRACKS, array);
    }

    public Set<Integer> getEnabledTracks(ItemStack stack) {
        Set<Integer> set = new HashSet<Integer>();
        if (!stack.hasTagCompound() || stack.getSubCompound(Common.MOD_ID) == null) {
            return set;
        }
        int[] values = stack.getSubCompound(Common.MOD_ID).getIntArray(TAG_TRACKS);
        for (int v : values) {
            set.add(v);
        }
        return set;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public boolean isPlaying(ItemStack stack) {
        return stack != null && stack.hasTagCompound() && stack.getSubCompound(Common.MOD_ID) != null && stack.getSubCompound(Common.MOD_ID).getBoolean(TAG_PLAYING);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
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

        if (!stack.hasTagCompound() || stack.getSubCompound(Common.MOD_ID) == null) {
            return;
        }
        NBTTagCompound tag = stack.getSubCompound(Common.MOD_ID);

        if (!isHeld) {
            if (tag.getBoolean(TAG_PLAYING)) {
                tag.setLong(TAG_START_TIME, world.getTotalWorldTime());
            }
            return;
        }

        if (world.isRemote) {
            clientTickPlayback(stack, world, entity, slot, true);
        } else {
            if (tag.getBoolean(TAG_PLAYING)) {
                serverTickPlayback(stack, world, entity);
            }
        }
    }

    public void clientTickPlayback(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
        if (!stack.hasTagCompound() || stack.getSubCompound(Common.MOD_ID) == null) {
            return;
        }

        String key = buildClientKey(entity);

        if (!stack.getSubCompound(Common.MOD_ID).getBoolean(TAG_PLAYING)) {
            CLIENT_LAST_ELAPSED.remove(key);
            CLIENT_BASE_MS.remove(key);
            CLIENT_SIGNATURE.remove(key);
            return;
        }

        String melodyName = stack.getSubCompound(Common.MOD_ID).getString(TAG_MELODY);
        if (melodyName == null || melodyName.isEmpty()) return;

        long startTime = stack.getSubCompound(Common.MOD_ID).getLong(TAG_START_TIME);
        String signature = melodyName + "@" + startTime;
        String oldSignature = CLIENT_SIGNATURE.get(key);

        if (oldSignature == null || !oldSignature.equals(signature)) {
            CLIENT_SIGNATURE.put(key, signature);

            long worldTime = world.getTotalWorldTime();
            long elapsedMs = (worldTime - startTime) * 50L;
            if (elapsedMs < 0) elapsedMs = 0;

            CLIENT_BASE_MS.put(key, System.currentTimeMillis() - elapsedMs);

            CLIENT_LAST_ELAPSED.put(key, Math.max(-1L, elapsedMs - 50L));
        }

        immersive_melodies.resources.Melody melody = ClientMelodyManager.getMelody(new ResourceLocation(melodyName));
        if (melody == null || melody == immersive_melodies.resources.Melody.DEFAULT) return;

        Long baseObj = CLIENT_BASE_MS.get(key);
        long base = baseObj == null ? System.currentTimeMillis() : baseObj;
        long elapsed = System.currentTimeMillis() - base;
        if (elapsed < 0) elapsed = 0;

        Long previousObj = CLIENT_LAST_ELAPSED.get(key);
        long previousElapsed = previousObj == null ? -1L : previousObj;
        if (elapsed < previousElapsed) elapsed = previousElapsed;

        long windowStart = previousElapsed;
        if (windowStart >= 0L && elapsed - windowStart > MAX_CATCHUP_MS) {
            windowStart = elapsed - MAX_CATCHUP_MS;
        }

        Set<Integer> enabledTracks = getEnabledTracks(stack);
        List<immersive_melodies.resources.Track> tracks = melody.getTracks();
        for (int trackIndex = 0; trackIndex < tracks.size(); trackIndex++) {
            immersive_melodies.resources.Track track = tracks.get(trackIndex);
            if (!enabledTracks.isEmpty() && !enabledTracks.contains(trackIndex)) continue;

            for (Note note : track.getNotes()) {
                int t = note.getTime();
                if (t > elapsed) break;
                if (t > windowStart && t <= elapsed) {
                    playNote(world, entity, note);
                }
            }
        }

        CLIENT_LAST_ELAPSED.put(key, elapsed);

        int length = melody.getLength();
        if (length > 0 && elapsed > length) {
            long wrapped = elapsed % length;
            CLIENT_BASE_MS.put(key, System.currentTimeMillis() - wrapped);
            CLIENT_LAST_ELAPSED.put(key, wrapped);
        }
    }

    public void serverTickPlayback(ItemStack stack, World world, Entity entity) {
        if (world.isRemote || !stack.hasTagCompound() || stack.getSubCompound(Common.MOD_ID) == null) {
            return;
        }

        NBTTagCompound tag = stack.getSubCompound(Common.MOD_ID);

        if (!tag.getBoolean(TAG_PLAYING)) {
            return;
        }

        String melodyName = tag.getString(TAG_MELODY);
        if (melodyName == null || melodyName.isEmpty()) {
            return;
        }

        immersive_melodies.resources.Melody melody = ServerMelodyManager.getMelody(world, new ResourceLocation(melodyName));
        if (melody == null) {
            return;
        }

        String key = buildClientKey(entity);
        long startTime = tag.getLong(TAG_START_TIME);
        String signature = melodyName + "@" + startTime;
        String oldSignature = SERVER_SIGNATURE.get(key);

        if (oldSignature == null || !oldSignature.equals(signature)) {
            SERVER_SIGNATURE.put(key, signature);

            long worldTime = world.getTotalWorldTime();
            long elapsedMs = (worldTime - startTime) * 50L;
            if (elapsedMs < 0) elapsedMs = 0;

            SERVER_BASE_MS.put(key, System.currentTimeMillis() - elapsedMs);
            SERVER_LAST_ELAPSED.put(key, Math.max(-1L, elapsedMs - 50L));
        }

        Long baseObj = SERVER_BASE_MS.get(key);
        long base = baseObj == null ? System.currentTimeMillis() : baseObj;
        long elapsed = System.currentTimeMillis() - base;
        if (elapsed < 0) elapsed = 0;

        Long previousObj = SERVER_LAST_ELAPSED.get(key);
        long previousElapsed = previousObj == null ? -1L : previousObj;
        if (elapsed < previousElapsed) elapsed = previousElapsed;

        long windowStart = previousElapsed;
        if (windowStart >= 0L && elapsed - windowStart > MAX_CATCHUP_MS) {
            windowStart = elapsed - MAX_CATCHUP_MS;
        }

        Set<Integer> enabledTracks = getEnabledTracks(stack);
        List<immersive_melodies.resources.Track> tracks = melody.getTracks();

        for (int trackIndex = 0; trackIndex < tracks.size(); trackIndex++) {
            if (!enabledTracks.isEmpty() && !enabledTracks.contains(trackIndex)) {
                continue;
            }

            immersive_melodies.resources.Track track = tracks.get(trackIndex);
            for (Note note : track.getNotes()) {
                int t = note.getTime();

                if (t > elapsed) {
                    break;
                }

                if (t > windowStart && t <= elapsed) {
                    playNote(world, entity, note);
                }
            }
        }

        SERVER_LAST_ELAPSED.put(key, elapsed);

        if (elapsed > melody.getLength()) {
            tag.setLong(TAG_START_TIME, world.getTotalWorldTime());
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (slotChanged) {
            return true;
        }

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

        net.minecraft.util.SoundEvent event = Sounds.get(instrumentName, octave);
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
        if (!world.isRemote) {
            return;
        }

        if (!shouldRenderParticle(entity)) {
            return;
        }

        String key = entity.getEntityId() + ":" + instrumentName;
        long now = System.currentTimeMillis();
        Long last = PARTICLE_LAST_MS.get(key);
        if (last != null && now - last < PARTICLE_THROTTLE_MS) {
            return;
        }
        PARTICLE_LAST_MS.put(key, now);

        float yaw = entity.rotationYaw;
        if (entity instanceof EntityLivingBase) {
            yaw = ((EntityLivingBase) entity).renderYawOffset;
        }
        double x = Math.sin(-yaw / 180.0 * Math.PI);
        double z = Math.cos(-yaw / 180.0 * Math.PI);

        ParticleOffset offset = PARTICLE_OFFSETS.get(instrumentName);
        if (offset == null) {
            offset = new ParticleOffset(0.0, 0.25, 0.5);
        }

        double px = entity.posX + x * offset.z + z * offset.x;
        double py = entity.posY + entity.height / 2.0 + offset.y;
        double pz = entity.posZ + z * offset.z - x * offset.x;

        world.spawnParticle(EnumParticleTypes.NOTE, px, py, pz, x * 5.0, 0.0, z * 5.0);
    }

    private boolean shouldRenderParticle(Entity sourceEntity) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) {
            return false;
        }
        Entity camera = mc.getRenderViewEntity();
        if (camera == null) {
            return false;
        }
        if (camera != sourceEntity) {
            return true;
        }
        return mc.gameSettings.thirdPersonView != 0;
    }

    public void playTransientNote(World world, Entity entity, Note note) {
        playNote(world, entity, note);
    }

    private String buildClientKey(Entity entity) {
        return entity.getEntityId() + ":" + instrumentName;
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, java.util.List<String> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
        if (stack.hasTagCompound() && stack.getSubCompound(Common.MOD_ID) != null && stack.getSubCompound(Common.MOD_ID).getBoolean(TAG_PLAYING)) {
            tooltip.add(net.minecraft.client.resources.I18n.format("immersive_melodies.playing"));
        }
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
