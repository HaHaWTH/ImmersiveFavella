package immersive_melodies.client;

import immersive_melodies.Common;
import immersive_melodies.Items;
import immersive_melodies.client.model.InstrumentPerspectiveBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Common.MOD_ID, value = Side.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        register(Items.BAGPIPE);
        register(Items.DIDGERIDOO);
        register(Items.FLUTE);
        register(Items.LUTE);
        register(Items.PIANO);
        register(Items.TRIANGLE);
        register(Items.TRUMPET);
        register(Items.TINY_DRUM);
        register(Items.VIELLE);
        register(Items.ENDER_BASS);
        register(Items.HANDPAN);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        wrap(event, Items.BAGPIPE);
        wrap(event, Items.DIDGERIDOO);
        wrap(event, Items.FLUTE);
        wrap(event, Items.LUTE);
        wrap(event, Items.PIANO);
        wrap(event, Items.TRIANGLE);
        wrap(event, Items.TRUMPET);
        wrap(event, Items.TINY_DRUM);
        wrap(event, Items.VIELLE);
        wrap(event, Items.ENDER_BASS);
        wrap(event, Items.HANDPAN);
    }

    private static void register(Item item) {
        if (item.getRegistryName() != null) {
            ResourceLocation id = item.getRegistryName();
            ModelBakery.registerItemVariants(item, id, new ResourceLocation(id.getNamespace(), id.getPath() + "_hand"));
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(id, "inventory"));
        }
    }

    private static void wrap(ModelBakeEvent event, Item item) {
        ResourceLocation id = item.getRegistryName();
        if (id == null) {
            return;
        }

        ModelResourceLocation invLoc = new ModelResourceLocation(id, "inventory");
        ModelResourceLocation handLoc = new ModelResourceLocation(new ResourceLocation(id.getNamespace(), id.getPath() + "_hand"), "inventory");

        IBakedModel invModel = event.getModelRegistry().getObject(invLoc);
        IBakedModel handModel = event.getModelRegistry().getObject(handLoc);
        if (invModel == null || handModel == null) {
            return;
        }

        event.getModelRegistry().putObject(invLoc, new InstrumentPerspectiveBakedModel(invModel, handModel));
    }
}
