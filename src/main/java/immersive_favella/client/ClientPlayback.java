package immersive_favella.client;

import immersive_favella.Config;
import immersive_favella.item.InstrumentItem;
import immersive_favella.resources.Note;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientPlayback {
    private static final Map<Integer, Long> HELD_NOTES = new ConcurrentHashMap<Integer, Long>();

    private ClientPlayback() {
    }

    public static boolean playLocalNote(int tone, int velocity) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        if (player == null || mc.isGamePaused()) {
            return false;
        }
        return playNote(player, tone, velocity);
    }

    public static boolean playNote(Entity entity, int tone, int velocity) {
        if (entity == null) {
            return false;
        }
        for (ItemStack stack : entity.getEquipmentAndArmor()) {
            if (stack.getItem() instanceof InstrumentItem) {
                InstrumentItem instrument = (InstrumentItem) stack.getItem();
                if (velocity > 0) {
                    Note note = new Note(tone, velocity, 0, 120, Config.getInstance().getBufferDelay + 100);
                    instrument.playTransientNote(entity.world, entity, note);
                    HELD_NOTES.put(tone, System.currentTimeMillis());
                    return true;
                } else {
                    HELD_NOTES.remove(tone);
                    return true;
                }
            }
        }
        return false;
    }
}
