package immersive_melodies.client.animation;

import net.minecraft.client.model.ModelBiped;

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
}
