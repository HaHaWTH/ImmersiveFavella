package immersive_melodies.network.c2s;

import immersive_melodies.network.Network;
import immersive_melodies.network.s2c.MelodyListMessage;
import immersive_melodies.resources.ServerMelodyManager;
import immersive_melodies.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MelodyDeleteMessage implements IMessage {
    private ResourceLocation identifier;

    public MelodyDeleteMessage() {
    }

    public MelodyDeleteMessage(ResourceLocation identifier) {
        this.identifier = identifier;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        identifier = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, identifier.toString());
    }

    public static class Handler implements IMessageHandler<MelodyDeleteMessage, IMessage> {
        @Override
        public IMessage onMessage(final MelodyDeleteMessage message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (Utils.canDelete(message.identifier, player)) {
                    ServerMelodyManager.deleteMelody(player.world, message.identifier);
                    for (EntityPlayerMP online : player.getServer().getPlayerList().getPlayers()) {
                        Network.sendToPlayer(new MelodyListMessage(ServerMelodyManager.listMelodies(online)), online);
                    }
                }
            });
            return null;
        }
    }
}
