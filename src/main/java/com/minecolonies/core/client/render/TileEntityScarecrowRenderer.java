package com.minecolonies.core.client.render;

import com.minecolonies.api.tileentities.AbstractTileEntityScarecrow;
import com.minecolonies.api.tileentities.ScareCrowType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.model.ScarecrowModel;
import com.minecolonies.core.client.render.state.ScarecrowRenderState;
import com.minecolonies.core.event.ClientRegistryHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class TileEntityScarecrowRenderer
    implements BlockEntityRenderer<AbstractTileEntityScarecrow, ScarecrowRenderState>
{
    private static final double BLOCK_MIDDLE = 0.5D;
    private static final double Y_OFFSET = 1.5D;
    private static final int ROTATION = 180;
    private static final int BASIC_ROTATION = 90;

    private static final Identifier SCARECROW_A =
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block/blockscarecrowpumpkin");
    private static final Identifier SCARECROW_B =
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block/blockscarecrownormal");

    private final ScarecrowModel model;

    public TileEntityScarecrowRenderer(final BlockEntityRendererProvider.Context context)
    {
        this.model = new ScarecrowModel(context.bakeLayer(ClientRegistryHandler.SCARECROW));
    }

    @Override
    public ScarecrowRenderState createRenderState()
    {
        return new ScarecrowRenderState();
    }

    @Override
    public void extractRenderState(@NotNull final AbstractTileEntityScarecrow tileEntity,
                                   @NotNull final ScarecrowRenderState state,
                                   final float partialTicks,
                                   @NotNull final net.minecraft.world.phys.Vec3 cameraPosition,
                                   final net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumblingOverlay)
    {
        BlockEntityRenderer.super.extractRenderState(tileEntity, state, partialTicks, cameraPosition, crumblingOverlay);
        state.scarecrowType = tileEntity.getScarecrowType();
        if (tileEntity.getLevel() != null)
        {
            final var blockState = tileEntity.getLevel().getBlockState(tileEntity.getBlockPos());
            if (blockState.hasProperty(com.minecolonies.core.blocks.BlockScarecrow.FACING))
            {
                state.facing = blockState.getValue(com.minecolonies.core.blocks.BlockScarecrow.FACING);
            }
            if (blockState.hasProperty(com.minecolonies.core.blocks.BlockScarecrow.LANTERN))
            {
                state.lantern = blockState.getValue(com.minecolonies.core.blocks.BlockScarecrow.LANTERN);
            }
        }

        state.blockLight = tileEntity.getLevel() != null
            ? tileEntity.getLevel().getBrightness(net.minecraft.world.level.LightLayer.BLOCK, tileEntity.getBlockPos())
            : 0;
        state.skyLight = tileEntity.getLevel() != null
            ? tileEntity.getLevel().getBrightness(net.minecraft.world.level.LightLayer.SKY, tileEntity.getBlockPos())
            : 15;

        state.lanternModel.clear();
        if (state.lantern)
        {
            Minecraft.getInstance().getBlockModelResolver().update(
                state.lanternModel,
                Blocks.LANTERN.defaultBlockState(),
                BlockDisplayContext.create());
        }
    }

    @Override
    public void submit(@NotNull final ScarecrowRenderState state,
                       @NotNull final PoseStack poseStack,
                       @NotNull final SubmitNodeCollector collector,
                       @NotNull final CameraRenderState camera)
    {
        poseStack.pushPose();
        poseStack.translate(BLOCK_MIDDLE, Y_OFFSET, BLOCK_MIDDLE);
        poseStack.mulPose(Axis.ZP.rotationDegrees(ROTATION));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees(state.facing)));

        collector.order(0).submitModel(
            model,
            Unit.INSTANCE,
            poseStack,
            state.scarecrowType == ScareCrowType.PUMPKINHEAD ? SCARECROW_A : SCARECROW_B,
            LightCoordsUtil.pack(state.blockLight, state.skyLight),
            OverlayTexture.NO_OVERLAY,
            0,
            null);

        submitLantern(state, poseStack, collector);
        poseStack.popPose();
    }

    private static int rotationDegrees(final Direction facing)
    {
        return switch (facing)
        {
            case EAST -> BASIC_ROTATION;
            case SOUTH -> BASIC_ROTATION * 2;
            case WEST -> BASIC_ROTATION * 3;
            default -> 0;
        };
    }

    private static void submitLantern(
      final ScarecrowRenderState state,
      final PoseStack poseStack,
      final SubmitNodeCollector collector)
    {
        if (!state.lantern || state.lanternModel.isEmpty())
        {
            return;
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.translate(0.6F, -0.6F, -0.375F);
        poseStack.scale(0.75F, 0.65F, 0.75F);
        state.lanternModel.submitMultiLayer(
            poseStack,
            collector,
            LightCoordsUtil.pack(state.blockLight, state.skyLight),
            OverlayTexture.NO_OVERLAY,
            0);
        poseStack.popPose();
    }
}
