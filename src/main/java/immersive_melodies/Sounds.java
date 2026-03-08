package immersive_melodies;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.LinkedList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Common.MOD_ID)
public class Sounds {
    public static final List<SoundEvent> sounds = new LinkedList<>();

    public static SoundEvent register(String path) {
        ResourceLocation id = new ResourceLocation("immersive_melodies", path);
        SoundEvent event = new SoundEvent(id).setRegistryName(id);
        sounds.add(event);
        return event;
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        registerInstrument("bagpipe");
        registerInstrument("didgeridoo");
        registerInstrument("flute");
        registerInstrument("lute");
        registerInstrument("piano");
        registerInstrument("triangle");
        registerInstrument("trumpet");
        registerInstrument("tiny_drum");
        registerInstrument("vielle");
        registerInstrument("ender_bass");
        registerInstrument("handpan");
        event.getRegistry().registerAll(sounds.toArray(new SoundEvent[0]));
    }

    private static void registerInstrument(String instrument) {
        for (int octave = 1; octave <= 8; octave++) {
            register(instrument + ".c" + octave);
        }
    }

    public static SoundEvent get(String instrument, int octave) {
        int clamped = Math.max(1, Math.min(8, octave));
        ResourceLocation id = new ResourceLocation(Common.MOD_ID, instrument + ".c" + clamped);
        return ForgeRegistries.SOUND_EVENTS.getValue(id);
    }
}
