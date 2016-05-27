package com.minecolonies.client.render;

import com.minecolonies.client.model.ModelScarecrowBottom;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

/**
 * Class to render the scarecrow.
 */
public class TileEntityScarecrowRenderer extends TileEntitySpecialRenderer<TileEntityFieldHut>
{
    private final ModelScarecrowBottom model;

    public TileEntityScarecrowRenderer() { this.model = new ModelScarecrowBottom(); }

    private void adjustRotatePivotViaMeta(World world, BlockPos pos)
    {
        IBlockState meta = world.getBlockState(pos);
        GL11.glPushMatrix();
        //GL11.glRotatef(meta.getValue(Rot), 0.0F, 0.0F, 1.0F);
        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
    {
    }
        //The PushMatrix tells the renderer to "start" doing something. GL11.glPushMatrix(); //This is setting the initial location. GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F); //This is the texture of your block. It's pathed to be the same place as your other blocks here. //Outdated bindTextureByName("/mods/roads/textures/blocks/TrafficLightPoleRed.png");

            //Use in 1.6.2  this
            ResourceLocation textures = (new ResourceLocation("[yourmodidhere]:textures/blocks/TrafficLightPoleRed.png"));
            //the ':' is very important
            //binding the textures
            Minecraft.getMinecraft().renderEngine.bindTexture(textures);
//This rotation part is very important! Without it, your model will render upside-down! And for some reason you DO need PushMatrix again!	GL11.glPushMatrix(); GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F); //A reference to your Model file. Again, very important. this.model.render((Entity)null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F); //Tell it to stop rendering for both the PushMatrix's GL11.glPopMatrix(); GL11.glPopMatrix(); }

//Set the lighting stuff, so it changes it's brightness properly.	private void adjustLightFixture(World world, int i, int j, int k, Block block) { Tessellator tess = Tessellator.instance; //float brightness = block.getBlockBrightness(world, i, j, k); //As of MC 1.7+ block.getBlockBrightness() has become block.getLightValue(): float brightness = block.getLightValue(world, i, j, k); int skyLight = world.getLightBrightnessForSkyBlocks(i, j, k, 0); int modulousModifier = skyLight % 65536; int divModifier = skyLight / 65536; tess.setColorOpaque_F(brightness, brightness, brightness); OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) modulousModifier, divModifier); } } </syntaxhighlight>

}
