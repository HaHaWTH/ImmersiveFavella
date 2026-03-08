package immersive_melodies.network;

import immersive_melodies.Common;
import immersive_melodies.network.c2s.*;
import immersive_melodies.network.s2c.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class Network {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Common.MOD_ID);
    private static int id;

    private Network() {
    }

    public static void init() {
        id = 0;
        INSTANCE.registerMessage(ItemActionMessage.Handler.class, ItemActionMessage.class, id++, Side.SERVER);
        INSTANCE.registerMessage(MelodyRequestMessage.Handler.class, MelodyRequestMessage.class, id++, Side.SERVER);
        INSTANCE.registerMessage(MelodyDeleteMessage.Handler.class, MelodyDeleteMessage.class, id++, Side.SERVER);
        INSTANCE.registerMessage(UploadMelodyMessage.Handler.class, UploadMelodyMessage.class, id++, Side.SERVER);
        INSTANCE.registerMessage(NoteBroadcastMessage.Handler.class, NoteBroadcastMessage.class, id++, Side.SERVER);
        INSTANCE.registerMessage(TrackToggleMessage.Handler.class, TrackToggleMessage.class, id++, Side.SERVER);
        INSTANCE.registerMessage(OpenGuiMessageHandler.class, OpenGuiMessage.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(MelodyListMessageHandler.class, MelodyListMessage.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(MelodyResponseMessage.Handler.class, MelodyResponseMessage.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(NoteMessage.Handler.class, NoteMessage.class, id++, Side.CLIENT);
    }

    public static void sendToPlayer(IMessage message, EntityPlayerMP player) {
        INSTANCE.sendTo(message, player);
    }

    public static void sendToServer(IMessage message) {
        INSTANCE.sendToServer(message);
    }
}
