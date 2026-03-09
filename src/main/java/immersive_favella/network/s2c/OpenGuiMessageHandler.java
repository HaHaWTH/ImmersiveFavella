package immersive_favella.network.s2c;

import immersive_favella.client.gui.ImmersiveMelodiesScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static immersive_favella.network.s2c.OpenGuiMessage.SELECTOR;

public class OpenGuiMessageHandler implements IMessageHandler<OpenGuiMessage, IMessage> {
    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onMessage(final OpenGuiMessage message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (message.gui == SELECTOR) {
                Minecraft.getMinecraft().displayGuiScreen(new ImmersiveMelodiesScreen());
            }
        });
        return null;
    }
}