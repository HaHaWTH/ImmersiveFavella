package immersive_favella.resources;

import immersive_favella.util.MidiParser;
import immersive_favella.Common;
import immersive_favella.Config;
import immersive_favella.util.Utils;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public final class ServerMelodyManager {
    private static final String INDEX_DATA_NAME = "immersive_favella_index";
    private static final String TRACK_DATA_NAME = "immersive_favella_tracks";
    private static final String SAVE_ROOT = "immersive_favella";
    private static final List<ResourceLocation> BUILTIN_MELODIES = new ArrayList<>();
    private static final Random RANDOM = new Random();
    private static final Map<ResourceLocation, Melody> MELODY_CACHE = new Object2ObjectOpenHashMap<>();

    private ServerMelodyManager() {
    }

    public static Map<ResourceLocation, MelodyDescriptor> listMelodies(EntityPlayer player) {
        World world = player.getEntityWorld();
        ensureBuiltinsLoaded(world);
        IndexData index = getIndex(world);
        if (Config.getInstance().showOtherPlayersMelodies) {
            return new HashMap<>(index.melodies);
        }
        Map<ResourceLocation, MelodyDescriptor> own = new HashMap<>();
        for (Map.Entry<ResourceLocation, MelodyDescriptor> entry : index.melodies.entrySet()) {
            if (Utils.ownsMelody(entry.getKey(), player)) {
                own.put(entry.getKey(), entry.getValue());
            }
        }
        return own;
    }

    public static void registerMelody(World world, ResourceLocation id, Melody melody) {
        ensureBuiltinsLoaded(world);
        IndexData index = getIndex(world);
        index.melodies.put(id, new MelodyDescriptor(melody.getName()));
        index.markDirty();
        MELODY_CACHE.put(id, melody);

        File file = getFile(world, id);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
        melody.encode(pb);
        NBTTagCompound root = new NBTTagCompound();
        byte[] data = new byte[pb.writerIndex()];
        pb.getBytes(0, data);
        root.setByteArray("data", data);

        File temp = new File(file.getParentFile(), file.getName() + ".tmp");

        try (FileOutputStream out = new FileOutputStream(temp)) {
            CompressedStreamTools.writeCompressed(root, out);
            out.flush();
        } catch (Exception e) {
            if (temp.exists()) {
                temp.delete();
            }
            return;
        }

        try {
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception moveEx) {
            try {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                Common.LOGGER.error("Failed to move melody file: {} -> {}", temp.getAbsolutePath(), file.getAbsolutePath(), e);
            }
        }
    }

    public static void deleteMelody(World world, ResourceLocation id) {
        ensureBuiltinsLoaded(world);
        IndexData index = getIndex(world);
        index.melodies.remove(id);
        index.markDirty();
        MELODY_CACHE.remove(id);
        File file = getFile(world, id);
        if (file.exists()) {
            file.delete();
        }
    }

    public static Melody getMelody(World world, ResourceLocation id) {
        ensureBuiltinsLoaded(world);
        if (MELODY_CACHE.containsKey(id)) {
            return MELODY_CACHE.get(id);
        }
        if (id.getNamespace().equals("immersive_favella")) {
            String path = "data/immersive_favella/" + id.getPath();
            try (InputStream in = ServerMelodyManager.class.getClassLoader().getResourceAsStream(path)) {
                if (in != null) {
                    Melody parsed = MidiParser.parseMidi(in, id.getPath());
                    MELODY_CACHE.put(id, parsed);
                    return parsed;
                }
            } catch (Exception ignored) {
            }
        }
        File file = getFile(world, id);
        if (!file.exists()) {
            return Melody.DEFAULT;
        }
        try (FileInputStream in = new FileInputStream(file)) {
            NBTTagCompound root = CompressedStreamTools.readCompressed(in);
            byte[] bytes = root.getByteArray("data");
            if (bytes.length == 0) {
                return Melody.DEFAULT;
            }
            PacketBuffer pb = new PacketBuffer(Unpooled.wrappedBuffer(bytes));
            Melody parsed = new Melody(pb);
            MELODY_CACHE.put(id, parsed);
            return parsed;
        } catch (Exception e) {
            return Melody.DEFAULT;
        }
    }

    private static IndexData getIndex(World world) {
        MapStorage storage = world.getPerWorldStorage();
        IndexData data = (IndexData) storage.getOrLoadData(IndexData.class, INDEX_DATA_NAME);
        if (data == null) {
            data = new IndexData();
            storage.setData(INDEX_DATA_NAME, data);
        }
        return data;
    }

    private static TrackData getTrackData(World world) {
        MapStorage storage = world.getPerWorldStorage();
        TrackData data = (TrackData) storage.getOrLoadData(TrackData.class, TRACK_DATA_NAME);
        if (data == null) {
            data = new TrackData();
            storage.setData(TRACK_DATA_NAME, data);
        }
        return data;
    }

    public static void enableTrack(World world, ResourceLocation melody, String identifier, int track) {
        TrackData data = getTrackData(world);
        data.enableTrack(melody, identifier, track);
        data.markDirty();
    }

    public static void disableTrack(World world, ResourceLocation melody, String identifier, int track) {
        TrackData data = getTrackData(world);
        data.disableTrack(melody, identifier, track);
        data.markDirty();
    }

    public static Set<Integer> getEnabledTracks(World world, ResourceLocation melody, String identifier) {
        return getTrackData(world).getEnabledTracks(melody, identifier);
    }

    public static ResourceLocation getRandomMelody(World world) {
        ensureBuiltinsLoaded(world);
        IndexData index = getIndex(world);
        if (index.melodies.isEmpty()) {
            return new ResourceLocation("immersive_favella", "melodies/wet_hands.midi");
        }
        int target = RANDOM.nextInt(index.melodies.size());
        int i = 0;
        for (ResourceLocation id : index.melodies.keySet()) {
            if (i == target) {
                return id;
            }
            i++;
        }
        return index.melodies.keySet().iterator().next();
    }

    public static String getIdentifier(String instrumentName) {
        return instrumentName;
    }

    private static void ensureBuiltinsLoaded(World world) {
        if (!BUILTIN_MELODIES.isEmpty()) {
            return;
        }
        String[] names = new String[] {
                "wet_hands.midi",
                "minecraft.midi",
                "megalovania.midi",
                "never_gonna_give_you_up.midi",
                "haruhikage.midi"
        };
        IndexData index = getIndex(world);
        for (String name : names) {
            ResourceLocation id = new ResourceLocation("immersive_favella", "melodies/" + name);
            BUILTIN_MELODIES.add(id);
            if (!index.melodies.containsKey(id)) {
                String title = Utils.toTitle(Utils.removeLastPart(name, "."));
                index.melodies.put(id, new MelodyDescriptor(title));
            }
        }
        index.markDirty();
    }

    private static File getFile(World world, ResourceLocation id) {
        String relative = id.toString().replace(':', File.separatorChar) + ".bin";
        return new File(world.getSaveHandler().getWorldDirectory(), SAVE_ROOT + File.separator + relative);
    }

    public static class IndexData extends WorldSavedData {
        private final Map<ResourceLocation, MelodyDescriptor> melodies = new HashMap<ResourceLocation, MelodyDescriptor>();

        public IndexData(String name) {
            super(name);
        }

        public IndexData() {
            super(INDEX_DATA_NAME);
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            melodies.clear();
            int size = nbt.getInteger("size");
            for (int i = 0; i < size; i++) {
                String key = "id_" + i;
                String nameKey = "name_" + i;
                if (nbt.hasKey(key) && nbt.hasKey(nameKey)) {
                    melodies.put(new ResourceLocation(nbt.getString(key)), new MelodyDescriptor(nbt.getString(nameKey)));
                }
            }
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound compound) {
            compound.setInteger("size", melodies.size());
            int i = 0;
            for (Map.Entry<ResourceLocation, MelodyDescriptor> entry : melodies.entrySet()) {
                compound.setString("id_" + i, entry.getKey().toString());
                compound.setString("name_" + i, entry.getValue().getName());
                i++;
            }
            return compound;
        }

        public Map<ResourceLocation, MelodyDescriptor> getMelodies() {
            return Collections.unmodifiableMap(melodies);
        }
    }

    public static class TrackData extends WorldSavedData {
        private final Map<String, Set<Integer>> tracks = new HashMap<String, Set<Integer>>();

        public TrackData(String name) {
            super(name);
        }

        public TrackData() {
            super(TRACK_DATA_NAME);
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            tracks.clear();
            int size = nbt.getInteger("size");
            for (int i = 0; i < size; i++) {
                String key = nbt.getString("k_" + i);
                int[] values = nbt.getIntArray("v_" + i);
                Set<Integer> set = new HashSet<>();
                for (int v : values) {
                    set.add(v);
                }
                tracks.put(key, set);
            }
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound compound) {
            compound.setInteger("size", tracks.size());
            int i = 0;
            for (Map.Entry<String, Set<Integer>> entry : tracks.entrySet()) {
                compound.setString("k_" + i, entry.getKey());
                int[] arr = new int[entry.getValue().size()];
                int j = 0;
                for (Integer v : entry.getValue()) {
                    arr[j++] = v;
                }
                compound.setIntArray("v_" + i, arr);
                i++;
            }
            return compound;
        }

        private String key(ResourceLocation melody, String identifier) {
            return melody.toString() + "|" + identifier;
        }

        public void enableTrack(ResourceLocation melody, String identifier, int track) {
            String key = key(melody, identifier);
            Set<Integer> set = tracks.computeIfAbsent(key, k -> new HashSet<>());
            set.add(track);
        }

        public void disableTrack(ResourceLocation melody, String identifier, int track) {
            String key = key(melody, identifier);
            Set<Integer> set = tracks.get(key);
            if (set != null) {
                set.remove(track);
            }
        }

        public Set<Integer> getEnabledTracks(ResourceLocation melody, String identifier) {
            String key = key(melody, identifier);
            Set<Integer> set = tracks.get(key);
            return set == null ? Collections.emptySet() : new HashSet<Integer>(set);
        }
    }
}
