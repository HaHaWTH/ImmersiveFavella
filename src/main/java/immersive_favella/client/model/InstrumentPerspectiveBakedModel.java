package immersive_favella.client.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.List;

public class InstrumentPerspectiveBakedModel implements IBakedModel {
    private final IBakedModel inventoryModel;
    private final IBakedModel handModel;

    public InstrumentPerspectiveBakedModel(IBakedModel inventoryModel, IBakedModel handModel) {
        this.inventoryModel = inventoryModel;
        this.handModel = handModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return inventoryModel.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return inventoryModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return inventoryModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return inventoryModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return inventoryModel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return inventoryModel.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return inventoryModel.getOverrides();
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        switch (cameraTransformType) {
            case FIRST_PERSON_LEFT_HAND:
            case FIRST_PERSON_RIGHT_HAND:
            case THIRD_PERSON_LEFT_HAND:
            case THIRD_PERSON_RIGHT_HAND:
                return ForgeHooksClient.handlePerspective(handModel, cameraTransformType);
            default:
                return ForgeHooksClient.handlePerspective(inventoryModel, cameraTransformType);
        }
    }
}
