package immersive_favella.network.c2s;

import immersive_favella.network.Network;
import immersive_favella.network.s2c.NoteMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class NoteBroadcastMessage implements IMessage {
    private int tone;
    private int velocity;

    public NoteBroadcastMessage() {
    }

    public NoteBroadcastMessage(int tone, int velocity) {
        this.tone = tone;
        this.velocity = velocity;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        tone = buf.readInt();
        velocity = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(tone);
        buf.writeInt(velocity);
    }

    public static class Handler implements IMessageHandler<NoteBroadcastMessage, IMessage> {
        @Override
        public IMessage onMessage(final NoteBroadcastMessage message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                for (EntityPlayerMP other : player.getServer().getPlayerList().getPlayers()) {
                    if (other != player && other.getDistance(player) < 64.0f) {
                        Network.sendToPlayer(new NoteMessage(player.getEntityId(), message.tone, message.velocity), other);
                    }
                }
            });
            return null;
        }
    }
}
