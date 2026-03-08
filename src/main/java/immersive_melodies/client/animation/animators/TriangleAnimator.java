package immersive_melodies.client.animation.animators;

import immersive_melodies.client.animation.Animator;
import immersive_melodies.client.animation.BipedModelAccessor;
import net.minecraft.entity.EntityLivingBase;

public class TriangleAnimator implements Animator {
    @Override
    public void setAngles(BipedModelAccessor accessor, EntityLivingBase entity, float time) {
        float delta = (float) Math.sin(time * 0.3f) * 0.6f;

        accessor.leftArmPitch(-1.1f);
        accessor.leftArmYaw(delta);

        accessor.rightArmPitch(-1.6f);
        accessor.rightArmRoll((float) Math.cos(time * 0.25f) * 0.05f);
        accessor.rightArmYaw(-0.5f);
    }
}
