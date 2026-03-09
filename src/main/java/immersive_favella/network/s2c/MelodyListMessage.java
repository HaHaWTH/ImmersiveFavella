package immersive_favella.network.s2c;

import immersive_favella.resources.MelodyDescriptor;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.HashMap;
import java.util.Map;

public class MelodyListMessage implements IMessage {
    private final Map<ResourceLocation, MelodyDescriptor> melodies = new HashMap<ResourceLocation, MelodyDescriptor>();

    public MelodyListMessage() {
    }

    public MelodyListMessage(Map<ResourceLocation, MelodyDescriptor> melodies) {
        this.melodies.putAll(melodies);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        int size = pb.readInt();
        melodies.clear();
        for (int i = 0; i < size; i++) {
            ResourceLocation id = new ResourceLocation(pb.readString(32767));
            melodies.put(id, new MelodyDescriptor(pb));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pb.writeInt(melodies.size());
        for (Map.Entry<ResourceLocation, MelodyDescriptor> entry : melodies.entrySet()) {
            pb.writeString(entry.getKey().toString());
            entry.getValue().encodeLite(pb);
        }
    }

    public Map<ResourceLocation, MelodyDescriptor> getMelodies() {
        return melodies;
    }
}
