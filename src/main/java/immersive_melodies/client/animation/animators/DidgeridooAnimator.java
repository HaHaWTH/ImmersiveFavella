package immersive_melodies.client.animation.animators;

public class DidgeridooAnimator extends FluteAnimator {
    @Override
    protected float getVerticalOffset(float time) {
        return (float) (Math.cos(time * 0.05f) * 0.05f) + 0.5f;
    }
}
