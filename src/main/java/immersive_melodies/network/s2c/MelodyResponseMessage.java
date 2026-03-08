package immersive_melodies.network.s2c;

import immersive_melodies.resources.ClientMelodyManager;
import immersive_melodies.resources.Melody;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class MelodyResponseMessage implements IMessage {
    private static final Map<String, Queue<byte[]>> BUFFER = new ConcurrentHashMap<String, Queue<byte[]>>();

    private String name;
    private byte[] fragment;
    private int totalLength;

    public MelodyResponseMessage() {
    }

    public MelodyResponseMessage(String name, byte[] fragment, int totalLength) {
        this.name = name;
        this.fragment = fragment;
        this.totalLength = totalLength;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        name = pb.readString(32767);
        int len = pb.readInt();
        fragment = new byte[len];
        pb.readBytes(fragment);
        totalLength = pb.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pb.writeString(name);
        pb.writeInt(fragment.length);
        pb.writeBytes(fragment);
        pb.writeInt(totalLength);
    }

    public static class Handler implements IMessageHandler<MelodyResponseMessage, IMessage> {
        @Override
        public IMessage onMessage(final MelodyResponseMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    Queue<byte[]> q = BUFFER.computeIfAbsent(message.name, k -> new LinkedList<>());
                    q.add(message.fragment);

                    int current = 0;
                    for (byte[] b : q) {
                        current += b.length;
                    }
                    if (current >= message.totalLength) {
                        ByteBuf assembled = Unpooled.buffer(message.totalLength);
                        for (byte[] b : q) {
                            assembled.writeBytes(b);
                        }
                        Melody melody = new Melody(new PacketBuffer(assembled));
                        ClientMelodyManager.setMelody(new ResourceLocation(message.name), melody);
                        BUFFER.remove(message.name);
                    }
                }
            });
            return null;
        }
    }
}
