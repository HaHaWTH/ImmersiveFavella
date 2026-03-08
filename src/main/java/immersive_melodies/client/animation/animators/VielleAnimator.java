package immersive_melodies.client.animation.animators;

import immersive_melodies.client.animation.Animator;
import immersive_melodies.client.animation.BipedModelAccessor;
import net.minecraft.entity.EntityLivingBase;

public class VielleAnimator implements Animator {
    @Override
    public void setAngles(BipedModelAccessor accessor, EntityLivingBase entity, float time) {
        accessor.leftArmPitch(-1.25f);
        accessor.leftArmYaw((float) (Math.cos(time * 0.2f) * 0.25f + 0.2f));
        accessor.leftArmRoll(-0.05f);

        accessor.rightArmPitch((float) (-0.9f + Math.cos(time * 0.15f) * 0.05f));
        accessor.rightArmYaw(-0.2f);
        accessor.rightArmRoll(0.2f);
    }
}
