package immersive_melodies.network.s2c;

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
            immersive_melodies.resources.ClientMelodyManager.replaceMelodiesList(message.getMelodies());
            if (Minecraft.getMinecraft().currentScreen instanceof immersive_melodies.client.gui.ImmersiveMelodiesScreen) {
                ((immersive_melodies.client.gui.ImmersiveMelodiesScreen) Minecraft.getMinecraft().currentScreen).refreshPage();
            }
        });
        return null;
    }
}