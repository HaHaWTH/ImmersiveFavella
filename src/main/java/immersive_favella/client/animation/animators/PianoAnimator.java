package immersive_favella.client.animation.animators;

import immersive_favella.client.animation.Animator;
import immersive_favella.client.animation.BipedModelAccessor;
import net.minecraft.entity.EntityLivingBase;

public class PianoAnimator implements Animator {
    @Override
    public void setAngles(BipedModelAccessor accessor, EntityLivingBase entity, float time) {
        accessor.leftArmPitch(-1.4f);
        accessor.leftArmYaw(0.2f);

        accessor.rightArmPitch(-0.9f);
        accessor.rightArmYaw(-0.15f);
        accessor.rightArmRoll(0.0f);
    }
}
