package immersive_favella.network.s2c;

import immersive_favella.client.gui.ImmersiveMelodiesScreen;
import immersive_favella.resources.ClientMelodyManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MelodyListMessageHandler implements IMessageHandler<MelodyListMessage, IMessage> {
    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onMessage(final MelodyListMessage message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            ClientMelodyManager.replaceMelodiesList(message.getMelodies());
            if (Minecraft.getMinecraft().currentScreen instanceof ImmersiveMelodiesScreen) {
                ((ImmersiveMelodiesScreen) Minecraft.getMinecraft().currentScreen).refreshPage();
            }
        });
        return null;
    }
}