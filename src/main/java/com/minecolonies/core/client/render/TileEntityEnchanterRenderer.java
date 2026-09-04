package com.minecolonies.core.client.render;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.tileentities.TileEntityEnchanter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class TileEntityEnchanterRenderer
    implements BlockEntityRenderer<TileEntityEnchanter, EnchantTableRenderState>
{
    public static final SpriteId BOOK_TEXTURE = new SpriteId(
        net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS,
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block/enchanting_table_book"));

    private final SpriteGetter sprites;
    private final BookModel modelBook;

    public TileEntityEnchanterRenderer(final BlockEntityRendererProvider.Context context)
    {
        this.sprites = context.sprites();
        this.modelBook = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public EnchantTableRenderState createRenderState()
    {
        return new EnchantTableRenderState();
    }

    @Override
    public void extractRenderState(@NotNull final TileEntityEnchanter entity,
                                   @NotNull final EnchantTableRenderState state,
                                   final float partialTicks,
                                   @NotNull final Vec3 cameraPosition,
                                   final ModelFeatureRenderer.CrumblingOverlay breakProgress)
    {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.time = entity.tickCount + partialTicks;
        state.flip = Mth.lerp(partialTicks, entity.pageFlipPrev, entity.pageFlip);
        state.open = Mth.lerp(partialTicks, entity.bookSpreadPrev, entity.bookSpread);

        float rotation = entity.bookRotation - entity.bookRotationPrev;
        while (rotation >= (float) Math.PI)
        {
            rotation -= (float) (Math.PI * 2.0D);
        }
        while (rotation < -(float) Math.PI)
        {
            rotation += (float) (Math.PI * 2.0D);
        }
        state.yRot = entity.bookRotationPrev + rotation * partialTicks;
    }

    @Override
    public void submit(@NotNull final EnchantTableRenderState state,
                       @NotNull final PoseStack poseStack,
                       @NotNull final SubmitNodeCollector collector,
                       @NotNull final CameraRenderState camera)
    {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.75D, 0.5D);
        poseStack.translate(0.0D, 0.1F + Mth.sin(state.time * 0.1F) * 0.01F, 0.0D);
        poseStack.mulPose(Axis.YP.rotation(-state.yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));

        final float flipA = Mth.frac(state.flip + 0.25F) * 1.6F - 0.3F;
        final float flipB = Mth.frac(state.flip + 0.75F) * 1.6F - 0.3F;
        final BookModel.State bookState = BookModel.State.forAnimation(
            state.time,
            Mth.clamp(flipA, 0.0F, 1.0F),
            Mth.clamp(flipB, 0.0F, 1.0F),
            state.open);

        collector.submitModel(
            this.modelBook,
            bookState,
            poseStack,
            state.lightCoords,
            OverlayTexture.NO_OVERLAY,
            -1,
            BOOK_TEXTURE,
            this.sprites,
            0,
            state.breakProgress);
        poseStack.popPose();
    }
}
