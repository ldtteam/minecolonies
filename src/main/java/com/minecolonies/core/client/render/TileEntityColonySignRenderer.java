package com.minecolonies.core.client.render;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.core.tileentities.TileEntityColonySign;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.core.blocks.BlockColonySign.CONNECTED;

@OnlyIn(Dist.CLIENT)
public class TileEntityColonySignRenderer implements BlockEntityRenderer<TileEntityColonySign>
{
    /**
     * The model of the scarecrow.
     */
    private final BakedModel model;
    private final BakedModel model2;

    /**
     * Cached render dispatcher.
     */
    private final BlockRenderDispatcher renderDispatcher;

    public TileEntityColonySignRenderer(final BlockEntityRendererProvider.Context context)
    {
        super();
        model = context.getBlockRenderDispatcher().getBlockModel(ModBlocks.blockColonySign.defaultBlockState());
        model2 = context.getBlockRenderDispatcher().getBlockModel(ModBlocks.blockColonySign.defaultBlockState().setValue(CONNECTED, true));
        renderDispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(
        @NotNull final TileEntityColonySign tileEntity,
        final float partialTicks,
        final PoseStack matrixStack,
        @NotNull final MultiBufferSource buffer,
        final int combinedLight,
        final int combinedOverlay)
    {
        if (tileEntity != null)
        {
            final float relativeRotationToColony = tileEntity.getRelativeRotation();
            final BlockState state = tileEntity.getLevel().getBlockState(tileEntity.getBlockPos());
            if (state.getBlock() == ModBlocks.blockColonySign)
            {
                matrixStack.pushPose();
                matrixStack.translate(0.5, 0.5, 0.5);
                matrixStack.mulPose(Axis.YP.rotationDegrees(relativeRotationToColony));
                matrixStack.translate(-0.5, -0.5, -0.5);
                renderSingleBlock(state, matrixStack, buffer, combinedLight, combinedOverlay, tileEntity.getTargetColonyId() != -1);
                matrixStack.popPose();

                matrixStack.pushPose();
                matrixStack.translate(0.5f, 0.5F, 0.5f);
                matrixStack.mulPose(Axis.YP.rotationDegrees(relativeRotationToColony));
                matrixStack.mulPose(Axis.YP.rotationDegrees(180));
                matrixStack.translate(-0.0f, -0.1F, 0.2f);

                matrixStack.scale(0.007F, -0.007F, 0.007F);

                final String colonyName = tileEntity.getColonyName();
                final int distance = tileEntity.getColonyDistance();
                if (colonyName.isEmpty())
                {
                    renderText(matrixStack, buffer, combinedLight, "Unknown Colony", 0, 0);
                    renderText(matrixStack, buffer, combinedLight, Component.translatable("com.minecolonies.coremod.dist.blocks",distance).getString(), 3, 0);
                }
                else
                {
                    final String targetColonyName = tileEntity.getTargetColonyName();
                    if (!targetColonyName.isEmpty())
                    {
                        final int targetColonyDistance = tileEntity.getTargetColonyDistance();
                        renderColonyNameOnSign(colonyName, matrixStack, buffer, combinedLight, distance, -10);
                        renderColonyNameOnSign(targetColonyName, matrixStack, buffer, combinedLight, targetColonyDistance, -60);
                    }
                    else
                    {
                        renderColonyNameOnSign(colonyName, matrixStack, buffer, combinedLight, distance, 0);
                    }
                }
                matrixStack.popPose();
            }
        }
    }

    private void renderColonyNameOnSign(final String colonyName, final PoseStack matrixStack, final @NotNull MultiBufferSource buffer, final int combinedLight, final int distance, final int offset)
    {
        final int textWidth = Minecraft.getInstance().font.width(colonyName);
        if (textWidth > 90)
        {
            final List<FormattedText> splitName = Minecraft.getInstance().font.getSplitter().splitLines(colonyName, 90, Style.EMPTY);;
            for (int i = 0; i < Math.min(2, splitName.size()); i++)
            {
                renderText(matrixStack, buffer, combinedLight, splitName.get(i).getString(), i, offset);
            }
            renderText(matrixStack, buffer, combinedLight, Component.translatable("com.minecolonies.coremod.dist.blocks",distance).getString(), 3, offset);
        }
        else
        {
            renderText(matrixStack, buffer, combinedLight, colonyName, 0, offset);
            renderText(matrixStack, buffer, combinedLight, Component.translatable("com.minecolonies.coremod.dist.blocks",distance).getString(), 3, offset);
        }
    }

    private void renderSingleBlock(final BlockState state, final PoseStack pose, final MultiBufferSource buffer, final int combinedLight, final int combinedOverlay, final boolean connected)
    {
        final BakedModel usedModel = connected ? model2 : model;
        for (net.minecraft.client.renderer.RenderType rt : usedModel.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY))
        {
            this.renderDispatcher.getModelRenderer().renderModel(pose.last(),
                buffer.getBuffer(net.minecraftforge.client.RenderTypeHelper.getEntityRenderType(rt, false)),
                state,
                usedModel,
                0,
                0,
                0,
                combinedLight,
                combinedOverlay,
                ModelData.EMPTY,
                rt);
        }
    }

    private void renderText(final PoseStack matrixStack, final MultiBufferSource buffer, final int combinedLight, String text, final int line, final float offset)
    {
        final int maxSize = 20;
        if (text.length() > maxSize)
        {
            text = text.substring(0, maxSize);
        }

        final FormattedCharSequence iReorderingProcessor = FormattedCharSequence.forward(text, Style.EMPTY);
        if (iReorderingProcessor != null)
        {
            final Font fontRenderer = Minecraft.getInstance().font;

            float x = (float) (-fontRenderer.width(iReorderingProcessor) / 2); //render width of text divided by 2
            fontRenderer.drawInBatch(iReorderingProcessor, x, line * 8f + offset,
                0xdcdcdc00, false, matrixStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, combinedLight);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityColonySign tileEntityMBE21)
    {
        return false;
    }
}
