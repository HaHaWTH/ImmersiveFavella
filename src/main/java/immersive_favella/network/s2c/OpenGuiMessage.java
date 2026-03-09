package immersive_favella.network.s2c;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class OpenGuiMessage implements IMessage {
    public static final int SELECTOR = 0;

    int gui;

    public OpenGuiMessage() {
    }

    public OpenGuiMessage(int gui) {
        this.gui = gui;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        gui = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(gui);
    }
}
