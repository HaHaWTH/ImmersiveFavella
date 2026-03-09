package immersive_favella.client.animation;

import net.minecraft.entity.EntityLivingBase;

public interface Animator {
    void setAngles(BipedModelAccessor accessor, EntityLivingBase entity, float time);
}
