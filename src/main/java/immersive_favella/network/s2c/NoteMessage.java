package immersive_favella.network.s2c;

import immersive_favella.client.ClientPlayback;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class NoteMessage implements IMessage {
    private int entityId;
    private int tone;
    private int velocity;

    public NoteMessage() {
    }

    public NoteMessage(int entityId, int tone, int velocity) {
        this.entityId = entityId;
        this.tone = tone;
        this.velocity = velocity;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
        tone = buf.readInt();
        velocity = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(tone);
        buf.writeInt(velocity);
    }

    public static class Handler implements IMessageHandler<NoteMessage, IMessage> {
        @Override
        public IMessage onMessage(final NoteMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    if (Minecraft.getMinecraft().world == null) {
                        return;
                    }
                    Entity e = Minecraft.getMinecraft().world.getEntityByID(message.entityId);
                    if (e != null) {
                        ClientPlayback.playNote(e, message.tone, message.velocity);
                    }
                }
            });
            return null;
        }
    }
}
