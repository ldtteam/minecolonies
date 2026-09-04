package com.minecolonies.core.client.render;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.core.client.render.state.ColonySignRenderState;
import com.minecolonies.core.client.render.worldevent.WorldEventContext;
import com.minecolonies.core.tileentities.TileEntityColonySign;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.NEXT;
import static com.minecolonies.api.util.constant.TranslationConstants.PREVIOUS;
import static com.minecolonies.core.blocks.BlockColonySign.CONNECTED;

@OnlyIn(Dist.CLIENT)
public class TileEntityColonySignRenderer
    implements BlockEntityRenderer<TileEntityColonySign, ColonySignRenderState>
{
    private final BlockModelRenderState normalModel = new BlockModelRenderState();
    private final BlockModelRenderState connectedModel = new BlockModelRenderState();
    private final Font font;

    public TileEntityColonySignRenderer(final BlockEntityRendererProvider.Context context)
    {
        this.font = context.font();
        final var resolver = Minecraft.getInstance().getBlockModelResolver();
        resolver.update(normalModel, ModBlocks.blockColonySign.defaultBlockState(), BlockDisplayContext.create());
        resolver.update(connectedModel,
            ModBlocks.blockColonySign.defaultBlockState().setValue(CONNECTED, true),
            BlockDisplayContext.create());
    }

    @Override
    public ColonySignRenderState createRenderState()
    {
        return new ColonySignRenderState();
    }

    @Override
    public void extractRenderState(@NotNull final TileEntityColonySign tileEntity,
                                   @NotNull final ColonySignRenderState state,
                                   final float partialTicks,
                                   @NotNull final Vec3 cameraPosition,
                                   final ModelFeatureRenderer.CrumblingOverlay breakProgress)
    {
        BlockEntityRenderer.super.extractRenderState(tileEntity, state, partialTicks, cameraPosition, breakProgress);
        state.relativeRotation = tileEntity.getRelativeRotation();
        state.connected = tileEntity.getTargetColonyId() != tileEntity.getCachedSignAboveColony();
        state.colonyName = tileEntity.getColonyName();
        state.colonyDistance = tileEntity.getColonyDistance();
        state.targetColonyName = tileEntity.getTargetColonyName();
        state.targetColonyDistance = tileEntity.getTargetColonyDistance();
    }

    @Override
    public void submit(@NotNull final ColonySignRenderState state,
                       @NotNull final PoseStack poseStack,
                       @NotNull final SubmitNodeCollector collector,
                       @NotNull final CameraRenderState camera)
    {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.relativeRotation));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        (state.connected ? connectedModel : normalModel).submitMultiLayer(
            poseStack,
            collector,
            state.lightCoords,
            0,
            0);
        poseStack.popPose();

        renderTextOnSide(state, poseStack, collector, state.lightCoords, true);
        renderTextOnSide(state, poseStack, collector, state.lightCoords, false);
    }

    private void renderTextOnSide(final ColonySignRenderState state,
                                  final PoseStack poseStack,
                                  final SubmitNodeCollector collector,
                                  final int light,
                                  final boolean mirrored)
    {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.relativeRotation));
        if (mirrored)
        {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        }
        poseStack.translate(0.0F, -0.1F, 0.2F);
        poseStack.scale(0.007F, -0.007F, 0.007F);

        if (state.colonyName.isEmpty())
        {
            renderText(poseStack, collector, light, "Unknown Colony", 0, 0);
            renderText(poseStack, collector, light,
                Component.translatable("com.minecolonies.coremod.dist.blocks", state.colonyDistance).getString(), 3, 0);
        }
        else if (!state.targetColonyName.isEmpty() && state.connected)
        {
            renderColonyNameOnSign(state.colonyName, poseStack, collector, light, state.colonyDistance, -10);
            renderColonyNameOnSign(state.targetColonyName, poseStack, collector, light, state.targetColonyDistance, -60);
        }
        else
        {
            renderColonyNameOnSign(state.colonyName, poseStack, collector, light, state.colonyDistance, -35);
        }

        poseStack.popPose();
    }

    private void renderColonyNameOnSign(final String colonyName,
                                        final PoseStack poseStack,
                                        final SubmitNodeCollector collector,
                                        final int light,
                                        final int distance,
                                        final int offset)
    {
        if (font.width(colonyName) > 90)
        {
            final List<net.minecraft.network.chat.FormattedText> lines = font.getSplitter()
                .splitLines(colonyName, 90, net.minecraft.network.chat.Style.EMPTY);
            for (int i = 0; i < Math.min(2, lines.size()); i++)
            {
                renderText(poseStack, collector, light, lines.get(i).getString(), i, offset);
            }
            renderText(poseStack, collector, light,
                Component.translatable("com.minecolonies.coremod.dist.blocks", distance).getString(), 3, offset);
        }
        else
        {
            renderText(poseStack, collector, light, colonyName, 0, offset);
            renderText(poseStack, collector, light,
                Component.translatable("com.minecolonies.coremod.dist.blocks", distance).getString(), 3, offset);
        }
    }

    private void renderText(final PoseStack poseStack,
                            final SubmitNodeCollector collector,
                            final int light,
                            String text,
                            final int line,
                            final float offset)
    {
        final int maxSize = 20;
        if (text.length() > maxSize)
        {
            text = text.substring(0, maxSize);
        }

        final FormattedCharSequence sequence = FormattedCharSequence.forward(text, net.minecraft.network.chat.Style.EMPTY);
        collector.submitText(
            poseStack,
            -font.width(sequence) / 2.0F,
            line * 8.0F + offset,
            sequence,
            false,
            Font.DisplayMode.NORMAL,
            light,
            0xdcdcdc00,
            0,
            0);
    }

    public static void renderSignHover(final WorldEventContext context)
    {
        final HitResult rayTraceResult = Minecraft.getInstance().hitResult;
        if (!(rayTraceResult instanceof final BlockHitResult blockRayTraceResult) || blockRayTraceResult.getType() == HitResult.Type.MISS)
        {
            return;
        }

        final BlockPos posAtCamera = blockRayTraceResult.getBlockPos();
        if (context.clientLevel.getBlockState(posAtCamera).getBlock() != ModBlocks.blockColonySign)
        {
            return;
        }

        if (context.clientLevel.getBlockEntity(posAtCamera) instanceof TileEntityColonySign tileEntityColonySign)
        {
            if (!BlockPos.ZERO.equals(tileEntityColonySign.getPreviousPos()))
            {
                context.renderDebugText(tileEntityColonySign.getPreviousPos(),
                    List.of(Component.translatable(PREVIOUS).getString()), true, 1);
            }
            if (!BlockPos.ZERO.equals(tileEntityColonySign.getNextPosition()))
            {
                context.renderDebugText(tileEntityColonySign.getNextPosition(),
                    List.of(Component.translatable(NEXT).getString()), true, 1);
            }
        }
    }
}
