package com.minecolonies.core.client.render;

import com.minecolonies.core.blocks.BlockDecorationController;
import com.minecolonies.core.client.render.state.DecoControllerRenderState;
import com.minecolonies.core.tileentities.TileEntityDecorationController;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class TileEntityDecoControllerRenderer
    implements BlockEntityRenderer<BlockEntity, DecoControllerRenderState>
{
    public TileEntityDecoControllerRenderer(final BlockEntityRendererProvider.Context context)
    {
    }

    @Override
    public DecoControllerRenderState createRenderState()
    {
        return new DecoControllerRenderState();
    }

    @Override
    public void extractRenderState(@NotNull final BlockEntity blockEntity,
                                   @NotNull final DecoControllerRenderState state,
                                   final float partialTicks,
                                   @NotNull final Vec3 cameraPosition,
                                   final ModelFeatureRenderer.CrumblingOverlay breakProgress)
    {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        if (!(blockEntity instanceof TileEntityDecorationController decorationController)
              || decorationController.getLevel() == null)
        {
            return;
        }

        final Level level = decorationController.getLevel();
        state.controllerState = decorationController.getBlockState();
        state.direction = state.controllerState.getValue(BlockDecorationController.FACING);
        final BlockPos offsetPos = blockEntity.getBlockPos().relative(state.direction);
        state.neighborShape = level.getBlockState(offsetPos).getShape(level, offsetPos);

        if (state.neighborShape.isEmpty() || Block.isShapeFullBlock(state.neighborShape))
        {
            state.renderAtNeighbor = true;
            state.translation = Vec3.ZERO;
        }
        else
        {
            state.renderAtNeighbor = false;
            state.translation = switch (state.direction)
            {
                case UP -> new Vec3(0, state.neighborShape.min(Direction.Axis.Y), 0);
                case DOWN -> new Vec3(0, state.neighborShape.max(Direction.Axis.Y) - 1, 0);
                case NORTH -> new Vec3(0, 0, state.neighborShape.max(Direction.Axis.Z) - 1);
                case SOUTH -> new Vec3(0, 0, state.neighborShape.min(Direction.Axis.Z));
                case EAST -> new Vec3(state.neighborShape.min(Direction.Axis.X), 0, 0);
                case WEST -> new Vec3(state.neighborShape.max(Direction.Axis.X) - 1, 0, 0);
            };
        }

        Minecraft.getInstance().getBlockModelResolver().update(
            state.controllerModel,
            state.controllerState,
            BlockDisplayContext.create());
    }

    @Override
    public void submit(@NotNull final DecoControllerRenderState state,
                       @NotNull final PoseStack poseStack,
                       @NotNull final SubmitNodeCollector collector,
                       @NotNull final CameraRenderState camera)
    {
        if (!state.renderAtNeighbor && (state.translation == Vec3.ZERO || state.controllerState.isAir()))
        {
            return;
        }

        poseStack.pushPose();
        if (!state.renderAtNeighbor)
        {
            poseStack.translate(state.translation.x, state.translation.y, state.translation.z);
        }
        state.controllerModel.submitMultiLayer(poseStack, collector, state.lightCoords, 0, 0);
        poseStack.popPose();
    }
}
