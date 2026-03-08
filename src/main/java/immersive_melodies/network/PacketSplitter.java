package immersive_melodies.network;

import immersive_melodies.network.c2s.UploadMelodyMessage;
import immersive_melodies.network.s2c.MelodyResponseMessage;
import immersive_melodies.resources.Melody;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.LinkedList;
import java.util.List;

public final class PacketSplitter {
    private static final int FRAGMENT_SIZE = 8192;

    private PacketSplitter() {
    }

    private static List<byte[]> fragment(Melody melody) {
        PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
        melody.encode(pb);
        byte[] all = new byte[pb.writerIndex()];
        pb.getBytes(0, all);
        List<byte[]> parts = new LinkedList<>();
        for (int i = 0; i < all.length; i += FRAGMENT_SIZE) {
            int len = Math.min(FRAGMENT_SIZE, all.length - i);
            byte[] p = new byte[len];
            System.arraycopy(all, i, p, 0, len);
            parts.add(p);
        }
        return parts;
    }

    public static void sendToPlayer(ResourceLocation id, Melody melody, EntityPlayerMP player) {
        List<byte[]> parts = fragment(melody);
        int total = 0;
        for (byte[] p : parts) {
            total += p.length;
        }
        for (byte[] p : parts) {
            Network.sendToPlayer(new MelodyResponseMessage(id.toString(), p, total), player);
        }
    }

    public static void sendToServer(String name, Melody melody) {
        List<byte[]> parts = fragment(melody);
        int total = 0;
        for (byte[] p : parts) {
            total += p.length;
        }
        for (byte[] p : parts) {
            Network.sendToServer(new UploadMelodyMessage(name, p, total));
        }
    }
}
