package immersive_melodies.network.c2s;

import immersive_melodies.Config;
import immersive_melodies.network.Network;
import immersive_melodies.network.PacketSplitter;
import immersive_melodies.network.s2c.MelodyListMessage;
import immersive_melodies.resources.Melody;
import immersive_melodies.resources.ServerMelodyManager;
import immersive_melodies.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class UploadMelodyMessage implements IMessage {
    private static final Map<String, Queue<byte[]>> BUFFER = new ConcurrentHashMap<String, Queue<byte[]>>();

    private String name;
    private byte[] fragment;
    private int totalLength;

    public UploadMelodyMessage() {
    }

    public UploadMelodyMessage(String name, byte[] fragment, int totalLength) {
        this.name = name;
        this.fragment = fragment;
        this.totalLength = totalLength;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        name = ByteBufUtils.readUTF8String(buf);
        int len = buf.readInt();
        fragment = new byte[len];
        buf.readBytes(fragment);
        totalLength = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeInt(fragment.length);
        buf.writeBytes(fragment);
        buf.writeInt(totalLength);
    }

    public static class Handler implements IMessageHandler<UploadMelodyMessage, IMessage> {
        @Override
        public IMessage onMessage(final UploadMelodyMessage message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    if (!player.canUseCommand(Config.getInstance().uploadPermissionLevel, "")) {
                        return;
                    }

                    String key = player.getUniqueID().toString() + ":" + message.name;
                    Queue<byte[]> q = BUFFER.get(key);
                    if (q == null) {
                        q = new LinkedList<byte[]>();
                        BUFFER.put(key, q);
                    }
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
                        String id = Utils.getPlayerName(player) + "/" + Utils.escapeString(message.name);
                        ResourceLocation identifier = new ResourceLocation("player", id);
                        ServerMelodyManager.registerMelody(player.world, identifier, melody);
                        BUFFER.remove(key);

                        for (EntityPlayerMP online : player.getServer().getPlayerList().getPlayers()) {
                            Network.sendToPlayer(new MelodyListMessage(ServerMelodyManager.listMelodies(online)), online);
                            PacketSplitter.sendToPlayer(identifier, melody, online);
                        }
                    }
                }
            });
            return null;
        }
    }
}
