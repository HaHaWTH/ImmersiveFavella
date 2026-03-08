package immersive_melodies.client.animation.animators;

import immersive_melodies.client.animation.Animator;
import immersive_melodies.client.animation.BipedModelAccessor;
import net.minecraft.entity.EntityLivingBase;

public class FluteAnimator implements Animator {
    protected float getVerticalOffset(float time) {
        return (float) (Math.cos(time * 0.15f) * 0.05f) + 0.25f;
    }

    protected float getHorizontalOffset(float time) {
        return (float) (Math.sin(time * 0.1f) * 0.05f);
    }

    @Override
    public void setAngles(BipedModelAccessor accessor, EntityLivingBase entity, float time) {
        accessor.headPitch(getVerticalOffset(time));
        accessor.headYaw(0.0f);

        float horizontalOffset = getHorizontalOffset(time);

        accessor.leftArmPitch((float) (-Math.PI / 2.0f) + accessor.headPitch());
        accessor.leftArmYaw(0.4f - 0.1f * accessor.headPitch() + horizontalOffset);
        accessor.leftArmRoll(0.25f * accessor.headPitch());

        accessor.rightArmPitch((float) (-Math.PI / 2.0f) + accessor.headPitch());
        accessor.rightArmYaw(-0.45f + horizontalOffset);
    }
}
