package immersive_melodies.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class Utils {
    public static String escapeString(String string) {
        return string.toLowerCase(Locale.ROOT).replaceAll("[^a-z\\d_.-]", "");
    }

    public static String toTitle(String string) {
        return StringUtils.capitalize(string.replace("_", " "));
    }

    public static String getPlayerName(EntityPlayer player) {
        return escapeString(player.getGameProfile().getName());
    }

    public static boolean isPlayerMelody(ResourceLocation identifier) {
        return "player".equals(identifier.getNamespace());
    }

    public static boolean ownsMelody(ResourceLocation identifier, EntityPlayer player) {
        return isPlayerMelody(identifier) && identifier.getPath().startsWith(getPlayerName(player) + "/");
    }

    public static boolean canDelete(ResourceLocation identifier, EntityPlayer player) {
        return ownsMelody(identifier, player) || (isPlayerMelody(identifier) && player.canUseCommand(2, ""));
    }

    public static String removeLastPart(String input, String delimiter) {
        int index = input.lastIndexOf(delimiter);
        return index == -1 ? input : input.substring(0, index);
    }

    public static String getLastPart(String input, String delimiter) {
        int index = input.lastIndexOf(delimiter);
        return index == -1 ? input : input.substring(index + delimiter.length());
    }
}
