package com.minecolonies.coremod.client.render;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.tileentities.TileEntityInfoPoster;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
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
    private static final ResourceLocation SIGN_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/blocks/info/info_poster.png");

    /**
     * Offset to the block middle.
     */
    private static final double BLOCK_MIDDLE = 0.5;

    /**
     * Y-Offset in order to have the scarecrow over ground.
     */
    private static final double YOFFSET = 1.0;

    /**
     * The ModelSign instance for use in this renderer
     */
    private IBakedModel model = null;

    @Override
    public void renderTileEntityAt(final TileEntityInfoPoster te, final double x, final double y, final double z, final float partialTicks, final int destroyStage)
    {
        if (model == null)
        {
            model = loadModel();
        }

        final World world = te.getWorld();
        final IBlockState state = world.getBlockState(te.getPos());
        final BlockPos pos = te.getPos();
        final IBlockState actualState = state.getBlock().getActualState(state, world, pos);
        float facing = actualState.getValue(BlockWallSign.FACING).getHorizontalAngle();


        int plusX = 0;
        int plusZ = 0;

        if (facing == 90 || facing == 180)
        {
            plusZ += 1;
            plusX += 1;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + plusX, y + YOFFSET, z + plusZ);
        GlStateManager.rotate(facing, 0.0F, 1.0F, 0.0F);
        GlStateManager.disableCull();

        RenderHelper.disableStandardItemLighting();

        GlStateManager.color(1F, 1F, 1F, 1F);

        final int alpha = ((int) (1.0D * 0xFF)) << 24;

        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.colorMask(false, false, false, false);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.depthFunc(GL11.GL_LEQUAL);

        this.renderModel(world, model, pos, alpha);


        GlStateManager.disableBlend();
        GlStateManager.popMatrix();


        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();
        final FontRenderer fontrenderer = this.getFontRenderer();
        //GlStateManager.translate(0.0F, 0.33333334F, 0.046666667F);
        GlStateManager.scale(0.010416667F, -0.010416667F, 0.010416667F);
        GlStateManager.glNormal3f(0.0F, 0.0F, -0.010416667F);
        GlStateManager.depthMask(false);

        if (destroyStage < 0)
        {
            for (int j = 0; j < te.signText.length; ++j)
            {
                if (te.signText[j] != null)
                {
                    ITextComponent itextcomponent = te.signText[j];
                    List<ITextComponent> list = GuiUtilRenderComponents.splitText(itextcomponent, 90, fontrenderer, false, true);
                    String s = list != null && !list.isEmpty() ? list.get(0).getFormattedText() : "";

                    if (j == te.lineBeingEdited)
                    {
                        s = "> " + s + " <";
                        fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, j * 10 - te.signText.length * 5, 0);
                    }
                    else
                    {
                        fontrenderer.drawString("blah" + s, -fontrenderer.getStringWidth(s) / 2, j * 10 - te.signText.length * 5, 0);
                    }
                }
            }
        }

        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
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

    private void renderModel(final World world, final IBakedModel model, final BlockPos pos, final int alpha)
    {
        final IBlockState state = world.getBlockState(pos);
        final IBlockState actualState = state.getBlock().getActualState(state, world, pos);
        final IBlockState iBlockExtendedState = state.getBlock().getExtendedState(state, world, pos);

        for (final EnumFacing facing : EnumFacing.values())
        {
            this.renderQuads(world, actualState, pos, model.getQuads(actualState, facing, 0), alpha);
        }

        this.renderQuads(world, actualState, pos, model.getQuads(iBlockExtendedState, null, 0), alpha);
    }

    private void renderQuads(final World world, final IBlockState actualState, final BlockPos pos, final List<BakedQuad> quads, final int alpha)
    {
        final Tessellator tessellator = Tessellator.getInstance();
        final VertexBuffer buffer = tessellator.getBuffer();

        for (final BakedQuad quad : quads)
        {
            buffer.begin(GL11.GL_QUADS, quad.getFormat());

            final int color = quad.hasTintIndex() ? this.getTint(world, actualState, pos, alpha, quad.getTintIndex()) : (alpha | 0xffffff);

            LightUtil.renderQuadColor(buffer, quad, color);

            tessellator.draw();
        }
    }

    private int getTint(final World world, final IBlockState actualState, final BlockPos pos, final int alpha, final int tintIndex)
    {
        return alpha | Minecraft.getMinecraft().getBlockColors().colorMultiplier(actualState, world, pos, tintIndex);
    }
}
