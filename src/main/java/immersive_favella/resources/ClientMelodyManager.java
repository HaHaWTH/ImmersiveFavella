package immersive_favella.resources;

import immersive_favella.network.Network;
import immersive_favella.network.c2s.MelodyRequestMessage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.Set;

public final class ClientMelodyManager {
    private static final Map<ResourceLocation, Melody> melodies = new Object2ObjectOpenHashMap<>();
    private static final Map<ResourceLocation, MelodyDescriptor> melodiesList = new Object2ObjectOpenHashMap<>();
    private static final Set<ResourceLocation> requested = new ObjectOpenHashSet<>();

    private ClientMelodyManager() {
    }

    public static Map<ResourceLocation, MelodyDescriptor> getMelodiesList() {
        return melodiesList;
    }

    public static void replaceMelodiesList(Map<ResourceLocation, MelodyDescriptor> map) {
        melodiesList.clear();
        melodiesList.putAll(map);
    }

    public static Melody getMelody(ResourceLocation identifier) {
        if (!melodies.containsKey(identifier) && !requested.contains(identifier)) {
            Network.sendToServer(new MelodyRequestMessage(identifier));
            requested.add(identifier);
        }
        Melody melody = melodies.get(identifier);
        return melody == null ? Melody.DEFAULT : melody;
    }

    public static void setMelody(ResourceLocation identifier, Melody melody) {
        melodies.put(identifier, melody);
    }
}
