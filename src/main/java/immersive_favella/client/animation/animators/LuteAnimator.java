package immersive_favella.client.animation.animators;

import immersive_favella.client.animation.Animator;
import immersive_favella.client.animation.BipedModelAccessor;
import net.minecraft.entity.EntityLivingBase;

public class LuteAnimator implements Animator {
    @Override
    public void setAngles(BipedModelAccessor accessor, EntityLivingBase entity, float time) {
        accessor.leftArmPitch(-0.5f);
        accessor.leftArmRoll(-0.2f);
        accessor.leftArmYaw(-0.4f);

        accessor.rightArmPitch(-0.75f);
        accessor.rightArmYaw(0.0f);
        accessor.rightArmRoll(accessor.rightArmRoll() * 0.25f - 0.2f);
    }
}
