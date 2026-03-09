package immersive_favella.mixin;

import immersive_favella.client.animation.EntityModelAnimator;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBiped.class)
public abstract class ModelBipedMixin {
    @Inject(method = "setRotationAngles", at = @At("TAIL"))
    private void immersiveMelodies$setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn, CallbackInfo ci) {
        if (entityIn instanceof EntityLivingBase) {
            EntityModelAnimator.setAngles((ModelBiped) (Object) this, (EntityLivingBase) entityIn, ageInTicks);
        }
    }
}
