package com.minecolonies.coremod.client.render;

import com.minecolonies.coremod.blocks.BlockDecorationController;
import com.minecolonies.coremod.client.render.worldevent.RenderTypes;
import com.minecolonies.coremod.tileentities.TileEntityDecorationController;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class TileEntityDecoControllerRenderer implements BlockEntityRenderer<BlockEntity>
{
    private BlockRenderDispatcher blockRenderer;

    public TileEntityDecoControllerRenderer(BlockEntityRendererProvider.Context p_173623_)
    {
        this.blockRenderer = p_173623_.getBlockRenderDispatcher();
    }

    @Override
    public void render(@NotNull BlockEntity blockEntity, float partialTick, @NotNull PoseStack matrixStack, @NotNull MultiBufferSource bufferSource, int lightA, int lightB)
    {
        if (blockEntity instanceof TileEntityDecorationController decorationController)
        {
            Level level = blockEntity.getLevel();
            if (level != null)
            {
                final BlockState decoController = decorationController.getBlockState();
                final Direction direction = decoController.getValue(BlockDecorationController.FACING);
                final BlockPos offsetPos = blockEntity.getBlockPos().relative(direction);
                final BlockState state = level.getBlockState(offsetPos);
                final VoxelShape shape = state.getShape(level, offsetPos);
                if (shape.isEmpty() || Block.isShapeFullBlock(shape))
                {
                    ModelBlockRenderer.enableCaching();
                    matrixStack.pushPose();

                    this.renderBlock(offsetPos, decoController, matrixStack, bufferSource, level, lightA);

                    matrixStack.popPose();
                    ModelBlockRenderer.clearCache();
                    return;
                }

                final Vec3 translateVec;
                switch (direction)
                {
                    case UP ->
                    {
                        translateVec = new Vec3(0, shape.min(Direction.Axis.Y), 0);
                    }
                    case DOWN ->
                    {
                        translateVec = new Vec3(0, shape.max(Direction.Axis.Y)-1, 0);
                    }
                    case NORTH ->
                    {
                        translateVec = new Vec3(0, 0, shape.max(Direction.Axis.Z)-1);
                    }
                    case SOUTH ->
                    {
                        translateVec = new Vec3(0, 0, shape.min(Direction.Axis.Z));
                    }
                    case EAST ->
                    {
                        translateVec = new Vec3( shape.min(Direction.Axis.X), 0, 0);
                    }
                    case WEST ->
                    {
                        translateVec = new Vec3(shape.max(Direction.Axis.X)-1, 0, 0);
                    }
                    default -> translateVec = new Vec3(0,0,0);
                }

                if (!decoController.isAir())
                {
                    ModelBlockRenderer.enableCaching();
                    matrixStack.pushPose();
                    matrixStack.translate(translateVec.x, translateVec.y, translateVec.z);

                    this.renderBlock(offsetPos, decoController, matrixStack, bufferSource, level, lightB);

                    matrixStack.popPose();
                    ModelBlockRenderer.clearCache();
                }
            }
        }
    }

    private void renderBlock(BlockPos pos, BlockState state, PoseStack poseStack, MultiBufferSource buffer, Level level, int light)
    {
        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.cutout());
        this.blockRenderer.getModelRenderer()
          .tesselateBlock(level, this.blockRenderer.getBlockModel(state), state, pos, poseStack, vertexconsumer, false, RandomSource.create(), state.getSeed(pos), light);
    }
}
