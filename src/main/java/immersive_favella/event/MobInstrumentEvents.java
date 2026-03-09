package immersive_favella.event;

import immersive_favella.Common;
import immersive_favella.Config;
import immersive_favella.Items;
import immersive_favella.item.InstrumentItem;
import immersive_favella.resources.ServerMelodyManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = Common.MOD_ID)
public class MobInstrumentEvents {
    private static final List<Item> INSTRUMENTS = new ArrayList<Item>();
    private static final Random RANDOM = new Random();

    static {
        INSTRUMENTS.add(Items.BAGPIPE);
        INSTRUMENTS.add(Items.DIDGERIDOO);
        INSTRUMENTS.add(Items.FLUTE);
        INSTRUMENTS.add(Items.LUTE);
        INSTRUMENTS.add(Items.PIANO);
        INSTRUMENTS.add(Items.TRIANGLE);
        INSTRUMENTS.add(Items.TRUMPET);
        INSTRUMENTS.add(Items.TINY_DRUM);
        INSTRUMENTS.add(Items.VIELLE);
        INSTRUMENTS.add(Items.ENDER_BASS);
        INSTRUMENTS.add(Items.HANDPAN);
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof EntityLiving) || entity instanceof EntityPlayer) {
            return;
        }
        EntityLiving mob = (EntityLiving) entity;
        String id = EntityRegistry.getEntry(mob.getClass()) != null ? EntityRegistry.getEntry(mob.getClass()).getRegistryName().toString() : "";
        Float chance = Config.getInstance().mobInstrumentFactors.get(id);
        if (chance != null && event.getWorld().rand.nextFloat() < chance) {
            Item item = INSTRUMENTS.get(RANDOM.nextInt(INSTRUMENTS.size()));
            mob.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, new ItemStack(item));
            mob.setDropChance(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, Config.getInstance().mobInstrumentDropFactor);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase living = event.getEntityLiving();
        if (living.world.isRemote || !(living instanceof EntityLiving) || living instanceof EntityPlayer) {
            return;
        }
        ItemStack stack = living.getHeldItemMainhand();
        if (stack.getItem() instanceof InstrumentItem) {
            InstrumentItem instrument = (InstrumentItem) stack.getItem();
            if (!stack.hasTagCompound() || stack.getSubCompound(Common.MOD_ID) == null || !stack.getSubCompound(Common.MOD_ID).getBoolean(InstrumentItem.TAG_PLAYING)) {
                ResourceLocation melody = ServerMelodyManager.getRandomMelody(living.world);
                instrument.play(stack, melody, living.world, null);
            }
            instrument.serverTickPlayback(stack, living.world, living);
        }
    }
}
