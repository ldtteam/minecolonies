package com.minecolonies.core.client.render;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.tileentities.TileEntityColonySign;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
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

@OnlyIn(Dist.CLIENT)
public class TileEntityColonySignRenderer implements BlockEntityRenderer<TileEntityColonySign>
{

    /**
     * The model of the scarecrow.
     */
    private final BakedModel model;

    public static final Material SIGN_MATERIAL;
    static
    {
        SIGN_MATERIAL = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(Constants.MOD_ID, "block/colonysign"));
    }
    private final BlockRenderDispatcher renderDispatcher;

    public TileEntityColonySignRenderer(final BlockEntityRendererProvider.Context context)
    {
        super();
        model = context.getBlockRenderDispatcher().getBlockModel(ModBlocks.blockColonySign.defaultBlockState());
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
            final float relativeRotationToColony = tileEntity.getRelativeRotation(tileEntity.getLevel().dimension());
            final BlockState state = tileEntity.getLevel().getBlockState(tileEntity.getBlockPos());
            if (state.getBlock() == ModBlocks.blockColonySign)
            {
                matrixStack.pushPose();
                matrixStack.translate(0.5, 0.5, 0.5);
                matrixStack.mulPose(Axis.YP.rotationDegrees(relativeRotationToColony));
                matrixStack.translate(-0.5, -0.5, -0.5);
                renderSingleBlock(state, matrixStack, buffer, combinedLight, combinedOverlay);
                matrixStack.popPose();

                matrixStack.pushPose();
                matrixStack.translate(0.5f, 0.5F, 0.5f);
                matrixStack.mulPose(Axis.YP.rotationDegrees(relativeRotationToColony));
                matrixStack.mulPose(Axis.YP.rotationDegrees(180));
                matrixStack.translate(-0.0f, -0.2F, 0.2f);

                matrixStack.scale(0.007F, -0.007F, 0.007F);

                final String colonyName = tileEntity.getColonyName(tileEntity.getLevel().dimension());
                final int distance = tileEntity.getColonyDistance(tileEntity.getLevel().dimension());
                if (colonyName.isEmpty())
                {
                    renderText(matrixStack, buffer, combinedLight, "Unknown Colony");
                }
                else
                {
                    renderText(matrixStack, buffer, combinedLight, colonyName);
                }
                matrixStack.popPose();
            }
        }
    }

    private void renderSingleBlock(BlockState state, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay)
    {
        for (net.minecraft.client.renderer.RenderType rt : this.model.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY))
        {
            this.renderDispatcher.getModelRenderer().renderModel(pose.last(),
                buffer.getBuffer(net.minecraftforge.client.RenderTypeHelper.getEntityRenderType(rt, false)),
                state,
                this.model,
                0,
                0,
                0,
                combinedLight,
                combinedOverlay,
                ModelData.EMPTY,
                rt);
        }
    }

    private void renderText(final PoseStack matrixStack, final MultiBufferSource buffer, final int combinedLight, String text)
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
            fontRenderer.drawInBatch(iReorderingProcessor, x, 0f,
                0xdcdcdc00, false, matrixStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, combinedLight);
        }
    }

    // this should be true for tileentities which render globally (no render bounding box), such as beacons.
    @Override
    public boolean shouldRenderOffScreen(TileEntityColonySign tileEntityMBE21)
    {
        return false;
    }
}
