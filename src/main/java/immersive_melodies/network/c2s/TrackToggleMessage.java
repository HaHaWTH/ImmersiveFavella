package immersive_melodies.network.c2s;

import immersive_melodies.item.InstrumentItem;
import immersive_melodies.resources.ServerMelodyManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TrackToggleMessage implements IMessage {
    private ResourceLocation melody;
    private int track;
    private boolean enabled;

    public TrackToggleMessage() {
    }

    public TrackToggleMessage(ResourceLocation melody, int track, boolean enabled) {
        this.melody = melody;
        this.track = track;
        this.enabled = enabled;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        melody = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
        track = buf.readInt();
        enabled = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, melody.toString());
        buf.writeInt(track);
        buf.writeBoolean(enabled);
    }

    public static class Handler implements IMessageHandler<TrackToggleMessage, IMessage> {
        @Override
        public IMessage onMessage(final TrackToggleMessage message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                ItemStack stack = player.getHeldItemMainhand();
                if (!(stack.getItem() instanceof InstrumentItem)) {
                    stack = player.getHeldItemOffhand();
                }
                if (stack.getItem() instanceof InstrumentItem) {
                    InstrumentItem instrument = (InstrumentItem) stack.getItem();
                    String identifier = ServerMelodyManager.getIdentifier(instrument.getInstrumentName());
                    if (message.enabled) {
                        ServerMelodyManager.enableTrack(player.world, message.melody, identifier, message.track);
                    } else {
                        ServerMelodyManager.disableTrack(player.world, message.melody, identifier, message.track);
                    }
                    instrument.refreshTracks(stack, player.world, player);
                }
            });
            return null;
        }
    }
}
