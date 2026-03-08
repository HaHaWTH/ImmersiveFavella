package immersive_melodies;

import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Common {
    public static final String MOD_ID = "immersive_melodies";
    public static final String MOD_NAME = "Immersive Melodies";
    public static final String VERSION = "1.0.0-backport";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private Common() {
    }

    public static ResourceLocation locate(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
