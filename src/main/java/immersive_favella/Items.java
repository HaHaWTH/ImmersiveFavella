package immersive_favella;

import immersive_favella.item.InstrumentItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Common.MOD_ID)
public class Items {
    public static final Item LUTE = instrument("lute");
    public static final Item BAGPIPE = instrument("bagpipe", 300L);
    public static final Item DIDGERIDOO = instrument("didgeridoo", 400L);
    public static final Item FLUTE = instrument("flute", 100L);
    public static final Item PIANO = instrument("piano", 500L);
    public static final Item TRIANGLE = instrument("triangle", 300L);
    public static final Item TRUMPET = instrument("trumpet", 100L);
    public static final Item TINY_DRUM = instrument("tiny_drum", 500L);
    public static final Item VIELLE = instrument("vielle", 200L);
    public static final Item ENDER_BASS = instrument("ender_bass", 100L);
    public static final Item HANDPAN = instrument("handpan", 300L);

    private static Item instrument(String name) {
        return instrument(name, 300L);
    }

    private static Item instrument(String name, long sustain) {
        InstrumentItem item = new InstrumentItem(name, sustain);
        item.setRegistryName(Common.MOD_ID, name);
        item.setTranslationKey(Common.MOD_ID + "." + name);
        item.setCreativeTab(ImmersiveMelodies.TAB);
        item.setMaxStackSize(1);
        return item;
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(LUTE, BAGPIPE, DIDGERIDOO, FLUTE, PIANO, TRIANGLE, TRUMPET, TINY_DRUM, VIELLE, ENDER_BASS, HANDPAN);
    }
}
