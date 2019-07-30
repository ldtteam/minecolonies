package com.minecolonies.coremod.client.render;

import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.tileentities.TileEntityInfoPoster;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.List;

@SideOnly(Side.CLIENT)
public class TileEntityInfoPosterRenderer extends TileEntitySpecialRenderer<TileEntityInfoPoster>
{
    /**
     * Offset to the block middle.
     */
    private static final double BLOCK_MIDDLE = 0.5;

    /**
     * Used to move the text slightly in the front.
     */
    private static final double SLIGHTLY_IN_FRONT = 0.01;

    /**
     * Scaling factor.
     */
    private static final double SCALING_FACTOR = 0.010416667F;

    /**
     * Y-Offset in order to have the scarecrow over ground.
     */
    private static final double YOFFSET = 0.2;

    /**
     * 90° offset.
     */
    private static final int NINETY_DEGREE = 90;

    /**
     * 180° offset.
     */
    private static final int HALF_ROTATION = 180;

    /**
     * 270° offset.
     */
    private static final int THREE_QUARTERS = 270;

    /**
     * Max text length.
     */
    private static final int MAX_TEXT_LENGTH = 90;

    /**
     * Text offset at x.
     */
    private static final int TEXT_OFFSET_X = 10;

    /**
     * Text offset at y.
     */
    private static final int TEXT_OFFSET_Y = 5;

    /**
     * The ModelSign instance for use in this renderer
     */
    private IBakedModel model = null;

    @Override
    public void render(final TileEntityInfoPoster te, final double x, final double y, final double z, final float partialTicks, final int destroyStage, final float alpha)
    {
        if (model == null)
        {
            model = loadModel();
        }

        if (model == null)
        {
            return;
        }

        final World world = te.getWorld();
        final BlockState state = world.getBlockState(te.getPos());
        final BlockPos pos = te.getPos();
        final BlockState actualState = state.getBlock().getExtendedState(state, world, pos);
        int facing = (int) actualState.getValue(BlockWallSign.FACING).getHorizontalAngle();

        double plusX = 0;
        double plusZ = 0;

        switch (facing)
        {
            case NINETY_DEGREE:
                plusX += 1;
                plusZ += 1;
                break;
            case HALF_ROTATION:
                plusZ += 1;
                facing = 0;
                break;
            case 0:
                facing = HALF_ROTATION;
                plusX += 1;
                break;
            default:
                //do nothing

        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + plusX, y + YOFFSET, z + plusZ);
        GlStateManager.rotate(facing, 0.0F, 1.0F, 0.0F);

        GlStateManager.disableCull();

        RenderHelper.disableStandardItemLighting();

        GlStateManager.color(1F, 1F, 1F, 1F);

        final int alpha2 = ((int) (1.0D * 0xFF)) << 24;

        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.colorMask(false, false, false, false);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        renderModel(world, model, pos, alpha2);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        GlStateManager.enableRescaleNormal();
        renderText(actualState, plusZ, plusX, te, x, y, z);
    }

    private static IBakedModel loadModel()
    {
        try
        {
            final IModel mod = ModelLoaderRegistry.getModel(new ResourceLocation(Constants.MOD_ID, "block/blockInfoPoster"));
            final IModelState state = mod.getDefaultState();
            return mod.bake(state, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());
        }
        catch (final Exception e)
        {
            Log.getLogger().error("Error loading infoPoster texture", e);
        }
        return null;
    }

    private static void renderModel(final World world, final IBakedModel model, final BlockPos pos, final int alpha)
    {
        final BlockState state = world.getBlockState(pos);
        final BlockState actualState = state.getBlock().getExtendedState(state, world, pos);
        final BlockState iBlockExtendedState = state.getBlock().getExtendedState(state, world, pos);

        for (final Direction facing : Direction.values())
        {
            renderQuads(world, actualState, pos, model.getQuads(actualState, facing, 0), alpha);
        }

        renderQuads(world, actualState, pos, model.getQuads(iBlockExtendedState, null, 0), alpha);
    }

    private void renderText(
                             final BlockState actualState, final double addZ, final double addX, final TileEntityInfoPoster te,
                             final double x, final double y, final double z)
    {
        GlStateManager.pushMatrix();
        final FontRenderer fontrenderer = this.getFontRenderer();
        double plusX = addX;
        double plusZ = addZ;

        int facing = (int) actualState.getValue(BlockWallSign.FACING).getHorizontalAngle();
        switch (facing)
        {
            case NINETY_DEGREE:
                facing = THREE_QUARTERS;
                plusZ -= BLOCK_MIDDLE;
                plusX -= SLIGHTLY_IN_FRONT;
                break;
            case HALF_ROTATION:
                plusX += BLOCK_MIDDLE;
                plusZ -= SLIGHTLY_IN_FRONT;
                break;
            case THREE_QUARTERS:
                facing = NINETY_DEGREE;
                plusZ += BLOCK_MIDDLE;
                plusX += SLIGHTLY_IN_FRONT;
                break;
            default:
                plusX -= BLOCK_MIDDLE;
                plusZ += SLIGHTLY_IN_FRONT;
                break;
        }

        GlStateManager.translate(x + plusX, y + YOFFSET * 2, z + plusZ);
        GlStateManager.rotate(facing, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(SCALING_FACTOR, -SCALING_FACTOR, SCALING_FACTOR);
        GlStateManager.depthMask(false);


        for (int j = 0; j < te.signText.length; ++j)
        {
            if (te.signText[j] != null)
            {
                final List<ITextComponent> list = GuiUtilRenderComponents.splitText(te.signText[j], MAX_TEXT_LENGTH, fontrenderer, false, true);
                final String text = list != null && !list.isEmpty() ? list.get(0).getFormattedText() : "";
                fontrenderer.drawString(text, -fontrenderer.getStringWidth(text) / 2, j * TEXT_OFFSET_X - te.signText.length * TEXT_OFFSET_Y, 0);
            }
        }


        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private static void renderQuads(final World world, final BlockState actualState, final BlockPos pos, final List<BakedQuad> quads, final int alpha)
    {
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();

        for (final BakedQuad quad : quads)
        {
            buffer.begin(GL11.GL_QUADS, quad.getFormat());

            final int color = quad.hasTintIndex() ? getTint(world, actualState, pos, alpha, quad.getTintIndex()) : (alpha | 0xffffff);

            LightUtil.renderQuadColor(buffer, quad, color);

            tessellator.draw();
        }
    }

    private static int getTint(final World world, final BlockState actualState, final BlockPos pos, final int alpha, final int tintIndex)
    {
        return alpha | Minecraft.getInstance().getBlockColors().colorMultiplier(actualState, world, pos, tintIndex);
    }
}
