package immersive_favella.network.c2s;

import immersive_favella.network.PacketSplitter;
import immersive_favella.resources.Melody;
import immersive_favella.resources.ServerMelodyManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MelodyRequestMessage implements IMessage {
    private ResourceLocation identifier;

    public MelodyRequestMessage() {
    }

    public MelodyRequestMessage(ResourceLocation identifier) {
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

    public static class Handler implements IMessageHandler<MelodyRequestMessage, IMessage> {
        @Override
        public IMessage onMessage(final MelodyRequestMessage message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    Melody melody = ServerMelodyManager.getMelody(player.world, message.identifier);
                    PacketSplitter.sendToPlayer(message.identifier, melody, player);
                }
            });
            return null;
        }
    }
}
