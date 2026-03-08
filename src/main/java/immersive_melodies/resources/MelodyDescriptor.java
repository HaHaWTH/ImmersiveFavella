package immersive_melodies.resources;

import net.minecraft.network.PacketBuffer;

public class MelodyDescriptor {
    private final String name;

    public MelodyDescriptor(String name) {
        this.name = name;
    }

    public MelodyDescriptor(PacketBuffer b) {
        this.name = b.readString(32767);
    }

    public String getName() {
        return name;
    }

    public void encodeLite(PacketBuffer b) {
        b.writeString(name);
    }
}
