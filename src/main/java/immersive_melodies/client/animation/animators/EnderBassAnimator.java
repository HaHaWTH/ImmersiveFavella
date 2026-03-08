package immersive_melodies.client.animation.animators;

import immersive_melodies.client.animation.Animator;
import immersive_melodies.client.animation.BipedModelAccessor;
import net.minecraft.entity.EntityLivingBase;

public class EnderBassAnimator implements Animator {
    @Override
    public void setAngles(BipedModelAccessor accessor, EntityLivingBase entity, float time) {
        accessor.leftArmPitch(-0.3f);
        accessor.leftArmRoll(0.0f);
        accessor.leftArmYaw(0.0f);

        accessor.rightArmPitch(-0.3f);
        accessor.rightArmRoll(0.0f);
        accessor.rightArmYaw(0.0f);
    }
}
