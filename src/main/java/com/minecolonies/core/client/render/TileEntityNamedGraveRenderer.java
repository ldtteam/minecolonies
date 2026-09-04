package com.minecolonies.core.client.render;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.blocks.huts.AbstractBlockMinecoloniesDefault;
import com.minecolonies.core.tileentities.TileEntityNamedGrave;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.minecolonies.core.client.render.state.NamedGraveRenderState;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class TileEntityNamedGraveRenderer implements BlockEntityRenderer<TileEntityNamedGrave, NamedGraveRenderState> {

    /**
     * Basic rotation to achieve a certain direction.
     */
    private static final int BASIC_ROTATION = 90;

    /**
     * Rotate by amount to go east.
     */
    private static final int ROTATE_EAST = 1;

    /**
     * Rotate by amount to go north.
     */
    private static final int ROTATE_NORTH = 2;

    /**
     * Rotate by amount to go west.
     */
    private static final int ROTATE_WEST = 3;

    public TileEntityNamedGraveRenderer(final BlockEntityRendererProvider.Context context)
    {
        super();
    }


    @Override
    public NamedGraveRenderState createRenderState()
    {
        return new NamedGraveRenderState();
    }

    @Override
    public void extractRenderState(@NotNull final TileEntityNamedGrave tileEntity,
                                   @NotNull final NamedGraveRenderState state,
                                   final float partialTicks,
                                   @NotNull final Vec3 cameraPosition,
                                   final ModelFeatureRenderer.CrumblingOverlay breakProgress)
    {
        BlockEntityRenderer.super.extractRenderState(tileEntity, state, partialTicks, cameraPosition, breakProgress);
        state.isNamedGrave = tileEntity.getBlockState().getBlock() == ModBlocks.blockNamedGrave;
        state.facing = tileEntity.getBlockState().getValue(AbstractBlockMinecoloniesDefault.FACING);
        state.textLines = List.copyOf(tileEntity.getTextLines());
    }

    @Override
    public void submit(@NotNull final NamedGraveRenderState state,
                       @NotNull final PoseStack matrixStack,
                       @NotNull final SubmitNodeCollector collector,
                       @NotNull final CameraRenderState camera)
    {
        if (!state.isNamedGrave)
        {
            return;
        }

        matrixStack.pushPose();
        switch (state.facing)
        {
            case NORTH -> {
                matrixStack.translate(0.5f, 1.18F, 0.48F);
                matrixStack.scale(0.006F, -0.006F, 0.006F);
                matrixStack.mulPose(Axis.YP.rotationDegrees(BASIC_ROTATION * ROTATE_NORTH));
            }
            case EAST -> {
                matrixStack.translate(0.54f, 1.18F, 0.5F);
                matrixStack.scale(0.006F, -0.006F, 0.006F);
                matrixStack.mulPose(Axis.YP.rotationDegrees(BASIC_ROTATION * ROTATE_EAST));
            }
            case WEST -> {
                matrixStack.translate(0.48f, 1.18F, 0.5F);
                matrixStack.scale(0.006F, -0.006F, 0.006F);
                matrixStack.mulPose(Axis.YP.rotationDegrees(BASIC_ROTATION * ROTATE_WEST));
            }
            default -> {
                matrixStack.translate(0.5f, 1.18F, 0.54F);
                matrixStack.scale(0.006F, -0.006F, 0.006F);
            }
        }

        if (state.textLines.isEmpty())
        {
            renderText(matrixStack, collector, state.lightCoords, "Unknown Citizen", 0);
        }
        else
        {
            for (int i = 0; i < state.textLines.size(); i++)
            {
                renderText(matrixStack, collector, state.lightCoords, state.textLines.get(i), i);
            }
        }

        matrixStack.popPose();
    }

    private void renderText(final PoseStack matrixStack,
                            final SubmitNodeCollector collector,
                            final int combinedLight,
                            String text,
                            final int line)
    {
        final int maxSize = 20;
        if (text.length() > maxSize)
        {
            text = text.substring(0, maxSize);
        }

        final FormattedCharSequence sequence = FormattedCharSequence.forward(text, Style.EMPTY);
        collector.submitText(
            matrixStack,
            (-Minecraft.getInstance().font.width(sequence)) / 2.0F,
            line * 10.0F,
            sequence,
            false,
            Font.DisplayMode.NORMAL,
            combinedLight,
            0xdcdcdc00,
            0,
            0);
    }

}
