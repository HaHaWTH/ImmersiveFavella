package immersive_favella.client.animation;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;

public class BipedModelAccessor {
    private final ModelBiped model;

    public BipedModelAccessor(ModelBiped model) {
        this.model = model;
    }

    public float headPitch() {
        return model.bipedHead.rotateAngleX;
    }

    public void headPitch(float v) {
        model.bipedHead.rotateAngleX = v;
    }

    public float headYaw() {
        return model.bipedHead.rotateAngleY;
    }

    public void headYaw(float v) {
        model.bipedHead.rotateAngleY = v;
    }

    public float leftArmPitch() {
        return model.bipedLeftArm.rotateAngleX;
    }

    public void leftArmPitch(float v) {
        model.bipedLeftArm.rotateAngleX = v;
    }

    public float leftArmYaw() {
        return model.bipedLeftArm.rotateAngleY;
    }

    public void leftArmYaw(float v) {
        model.bipedLeftArm.rotateAngleY = v;
    }

    public float leftArmRoll() {
        return model.bipedLeftArm.rotateAngleZ;
    }

    public void leftArmRoll(float v) {
        model.bipedLeftArm.rotateAngleZ = v;
    }

    public float rightArmPitch() {
        return model.bipedRightArm.rotateAngleX;
    }

    public void rightArmPitch(float v) {
        model.bipedRightArm.rotateAngleX = v;
    }

    public float rightArmYaw() {
        return model.bipedRightArm.rotateAngleY;
    }

    public void rightArmYaw(float v) {
        model.bipedRightArm.rotateAngleY = v;
    }

    public float rightArmRoll() {
        return model.bipedRightArm.rotateAngleZ;
    }

    public void rightArmRoll(float v) {
        model.bipedRightArm.rotateAngleZ = v;
    }

    public void syncOuterLayers() {
        ModelBase.copyModelAngles(model.bipedHead, model.bipedHeadwear);

        if (model instanceof ModelPlayer) {
            ModelPlayer playerModel = (ModelPlayer) model;

            ModelBase.copyModelAngles(model.bipedBody, playerModel.bipedBodyWear);
            ModelBase.copyModelAngles(model.bipedLeftArm, playerModel.bipedLeftArmwear);
            ModelBase.copyModelAngles(model.bipedRightArm, playerModel.bipedRightArmwear);
            ModelBase.copyModelAngles(model.bipedLeftLeg, playerModel.bipedLeftLegwear);
            ModelBase.copyModelAngles(model.bipedRightLeg, playerModel.bipedRightLegwear);
        }
    }
}