package immersive_favella.client.animation.animators;

import immersive_favella.client.animation.Animator;
import immersive_favella.client.animation.BipedModelAccessor;
import net.minecraft.entity.EntityLivingBase;

public class BagpipeAnimator implements Animator {
    @Override
    public void setAngles(BipedModelAccessor accessor, EntityLivingBase entity, float time) {
        accessor.headYaw(0.0f);
        accessor.headPitch(0.25f);

        accessor.leftArmPitch(-0.4f);
        accessor.leftArmYaw(-0.5f);

        accessor.rightArmPitch(-0.75f);
        accessor.rightArmYaw(-0.15f);
        accessor.rightArmRoll(0.0f);
    }
}
