package immersive_favella.client.animation;

import immersive_favella.client.animation.animators.*;
import immersive_favella.item.InstrumentItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class EntityModelAnimator {
    private static final Map<String, Animator> ANIMATORS = new Object2ObjectArrayMap<>();

    static {
        ANIMATORS.put("lute", new LuteAnimator());
        ANIMATORS.put("flute", new FluteAnimator());
        ANIMATORS.put("didgeridoo", new DidgeridooAnimator());
        ANIMATORS.put("trumpet", new TrumpetAnimator());
        ANIMATORS.put("piano", new PianoAnimator());
        ANIMATORS.put("triangle", new TriangleAnimator());
        ANIMATORS.put("tiny_drum", new TinyDrumAnimator());
        ANIMATORS.put("handpan", new HandpanAnimator());
        ANIMATORS.put("vielle", new VielleAnimator());
        ANIMATORS.put("bagpipe", new BagpipeAnimator());
        ANIMATORS.put("ender_bass", new EnderBassAnimator());
    }

    private EntityModelAnimator() {
    }

    public static void setAngles(ModelBiped model, EntityLivingBase entity, float ageInTicks) {
        ItemStack stack = entity.getHeldItemMainhand();
        if (!(stack.getItem() instanceof InstrumentItem) || !((InstrumentItem) stack.getItem()).isPlaying(stack)) {
            stack = entity.getHeldItemOffhand();
        }
        if (!(stack.getItem() instanceof InstrumentItem)) {
            return;
        }

        InstrumentItem item = (InstrumentItem) stack.getItem();
        Animator animator = ANIMATORS.get(item.getInstrumentName());
        if (animator == null) {
            return;
        }

        float time = entity.ticksExisted + ageInTicks;
        animator.setAngles(new BipedModelAccessor(model), entity, time);
    }
}
