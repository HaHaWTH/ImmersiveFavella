package immersive_favella;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JsonConfig {
    public static final Logger LOGGER = LogManager.getLogger(Common.MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    public int version = 0;

    int getVersion() {
        return 1;
    }

    public static File getConfigFile() {
        return new File("./config/" + Common.MOD_ID + ".json");
    }

    public void save() {
        try (FileWriter writer = new FileWriter(getConfigFile())) {
            version = getVersion();
            writer.write(GSON.toJson(this));
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }
}