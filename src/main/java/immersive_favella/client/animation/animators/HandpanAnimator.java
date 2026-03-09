package immersive_favella.client.animation.animators;

import immersive_favella.client.animation.Animator;
import immersive_favella.client.animation.BipedModelAccessor;
import net.minecraft.entity.EntityLivingBase;

public class HandpanAnimator implements Animator {
    @Override
    public void setAngles(BipedModelAccessor accessor, EntityLivingBase entity, float time) {
        float hit = (float) ((Math.sin(time * 0.6f) * 0.5f + 0.5f) * 0.4f);
        accessor.leftArmPitch(-1.2f - hit);
        accessor.leftArmYaw(0.2f - hit);
        accessor.leftArmRoll(0.2f);

        accessor.rightArmPitch(-0.6f);
        accessor.rightArmYaw(-0.6f);
        accessor.rightArmRoll(-0.2f);
    }
}
