package com.minecolonies.coremod.client.render;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.blocks.huts.AbstractBlockMinecoloniesDefault;
import com.minecolonies.api.tileentities.TileEntityNamedGrave;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class TileEntityNamedGraveRenderer extends TileEntityRenderer<TileEntityNamedGrave> {

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

    private final int textColor = NativeImage.combine(0, 220, 220, 220);

    public TileEntityNamedGraveRenderer(TileEntityRendererDispatcher rendererDispatcher)
    {
        super(rendererDispatcher);
    }


    @Override
    public void render(@NotNull final TileEntityNamedGrave tileEntity, final float partialTicks, final MatrixStack matrixStack, @NotNull final IRenderTypeBuffer buffer, final int combinedLight, final int combinedOverlay)
    {
        matrixStack.pushPose();

        if(tileEntity != null)
        {
            final BlockState state = tileEntity.getLevel().getBlockState(tileEntity.getBlockPos());
            if (state.getBlock() == ModBlocks.blockNamedGrave)
            {
                final Direction facing = state.getValue(AbstractBlockMinecoloniesDefault.FACING);
                switch (facing)
                {
                    case NORTH:
                        matrixStack.translate(0.5f, 1.18F, 0.48F); //in front of the center point of the name plate
                        matrixStack.scale(0.006F, -0.006F, 0.006F); //size of the text font
                        matrixStack.mulPose(Vector3f.YP.rotationDegrees(BASIC_ROTATION * ROTATE_NORTH));
                        break;
                    case SOUTH:
                        matrixStack.translate(0.5f, 1.18F, 0.54F); //in front of the center point of the name plate
                        matrixStack.scale(0.006F, -0.006F, 0.006F); //size of the text font
                        //don't rotate at all.
                        break;

                    case EAST:
                        matrixStack.translate(0.54f, 1.18F, 0.5F); //in front of the center point of the name plate
                        matrixStack.scale(0.006F, -0.006F, 0.006F); //size of the text font
                        matrixStack.mulPose(Vector3f.YP.rotationDegrees(BASIC_ROTATION * ROTATE_EAST));
                        break;
                    case WEST:
                        matrixStack.translate(0.48f, 1.18F, 0.5F); //in front of the center point of the name plate
                        matrixStack.scale(0.006F, -0.006F, 0.006F); //size of the text font
                        matrixStack.mulPose(Vector3f.YP.rotationDegrees(BASIC_ROTATION * ROTATE_WEST));
                        break;
                }

                if (tileEntity.getTextLines().isEmpty())
                {
                    renderText(matrixStack, buffer, combinedLight, "Unknown Citizen", 0);
                }
                else
                {
                    for (int i = 0; i < tileEntity.getTextLines().size(); i++)
                    {
                        renderText(matrixStack, buffer, combinedLight, tileEntity.getTextLines().get(i), i);
                    }
                }
            }
        }

        // restore the original transformation matrix + normals matrix
        matrixStack.popPose();
    }

    private void renderText(final MatrixStack matrixStack, final IRenderTypeBuffer buffer, final int combinedLight, String text, final int line)
    {
        final int maxSize = 20;
        if (text.length() > maxSize)
        {
            text = text.substring(0, maxSize);
        }

        final IReorderingProcessor iReorderingProcessor = IReorderingProcessor.forward(text, Style.EMPTY);
        if (iReorderingProcessor != null)
        {
            final FontRenderer fontRenderer = this.renderer.getFont();

            float x = (float) (-fontRenderer.width(iReorderingProcessor) / 2); //render width of text divided by 2
            fontRenderer.drawInBatch(iReorderingProcessor, x, line * 10f,
                    textColor, false, matrixStack.last().pose(), buffer, false, 0, combinedLight);
        }
    }

    // this should be true for tileentities which render globally (no render bounding box), such as beacons.
    @Override
    public boolean shouldRenderOffScreen(TileEntityNamedGrave tileEntityMBE21)
    {
        return false;
    }
}
