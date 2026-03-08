package immersive_melodies.network.s2c;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
