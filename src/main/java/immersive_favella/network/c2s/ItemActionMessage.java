package immersive_favella.network.c2s;

import immersive_favella.item.InstrumentItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ItemActionMessage implements IMessage {
    public enum State {
        PLAY,
        CONTINUE,
        PAUSE
    }

    private int slot;
    private State state;
    private ResourceLocation melody;

    public ItemActionMessage() {
    }

    public ItemActionMessage(int slot, State state, ResourceLocation melody) {
        this.slot = slot;
        this.state = state;
        this.melody = melody;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.slot = buf.readInt();
        this.state = State.values()[buf.readInt()];
        this.melody = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(slot);
        buf.writeInt(state.ordinal());
        ByteBufUtils.writeUTF8String(buf, melody.toString());
    }

    public static class Handler implements IMessageHandler<ItemActionMessage, IMessage> {
        @Override
        public IMessage onMessage(final ItemActionMessage message, final MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (message.slot < 0 || message.slot >= player.inventory.mainInventory.size()) {
                    return;
                }
                ItemStack stack = player.inventory.mainInventory.get(message.slot);
                if (stack.getItem() instanceof InstrumentItem) {
                    InstrumentItem instrument = (InstrumentItem) stack.getItem();
                    if (message.state == State.PLAY) {
                        instrument.play(stack, message.melody, player.world, player);
                    } else if (message.state == State.CONTINUE) {
                        instrument.play(stack, player.world);
                    } else if (message.state == State.PAUSE) {
                        instrument.pause(stack, player.world);
                    }
                }
            });
            return null;
        }
    }
}
