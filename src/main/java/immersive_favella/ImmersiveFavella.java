package immersive_favella;

import immersive_favella.client.gui.GuiHandler;
import immersive_favella.network.Network;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = Common.MOD_ID, name = Common.MOD_NAME, version = Common.VERSION, acceptedMinecraftVersions = "[1.12.2]", dependencies = "required-after:mixinbooter@[10.1,);")
public class ImmersiveFavella {
    @Mod.Instance(Common.MOD_ID)
    public static ImmersiveFavella INSTANCE;

    public static final int GUI_SELECTOR = 0;

    public static final CreativeTabs TAB = new CreativeTabs(Common.MOD_ID + "_tab") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Items.LUTE);
        }
    };

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Network.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide().isClient()) {
            MidiListener.launch();
        }
    }
}
