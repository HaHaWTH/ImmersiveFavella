package immersive_favella;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Config extends JsonConfig {
    private static final Config INSTANCE = loadOrCreate();

    public static Config getInstance() {
        return INSTANCE;
    }

    public int getBufferDelay = 75;
    public int maxAudibleDistance = 48;
    public float instrumentVolumeFactor = 1.0f;
    public float perceivedLoudnessAdjustmentFactor = 0.5f;

    public Map<String, Float> mobInstrumentFactors = ImmutableMap.<String, Float>builder()
            .put("minecraft:zombie", 0.01f)
            .put("minecraft:skeleton", 0.01f)
            .build();

    public float mobInstrumentDropFactor = 0.085f;

    public boolean showOtherPlayersMelodies = true;
    public boolean forceMobsToPickUp = true;
    public boolean clickedHelp = false;
    public boolean loadInbuiltMidis = true;
    public boolean stopGameMusicForPlayers = true;
    public boolean stopGameMusicForMobs = false;
    public boolean enableUnderwaterSoundEffect = true;
    public int uploadPermissionLevel = 0;

    public Map<Integer, Integer> scancodeToMidi = defaultScanCodes();

    private static Map<Integer, Integer> defaultScanCodes() {
        Map<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
        map.put(30, 60);
        map.put(31, 62);
        map.put(32, 64);
        map.put(33, 65);
        map.put(34, 67);
        map.put(35, 69);
        map.put(36, 71);
        map.put(37, 72);
        map.put(16, 61);
        map.put(17, 63);
        map.put(18, 66);
        map.put(19, 68);
        map.put(20, 70);
        map.put(21, 73);
        return map;
    }

    public static Config loadOrCreate() {
        File file = getConfigFile();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Config config = new Gson().fromJson(reader, Config.class);
                if (config.version != config.getVersion()) {
                    config = new Config();
                }
                config.save();
                return config;
            } catch (Exception e) {
                LOGGER.error("Failed to load config, resetting.", e);
                Config config = new Config();
                config.save();
                return config;
            }
        } else {
            Config config = new Config();
            config.save();
            return config;
        }
    }
}
