package com.minecolonies.client.render;

import com.minecolonies.client.model.ModelScarecrowBoth;
import com.minecolonies.lib.Constants;
import com.minecolonies.tileentities.ScarecrowTileEntity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Class to render the scarecrow.
 */
@SideOnly(Side.CLIENT)
public class TileEntityScarecrowRenderer extends TileEntitySpecialRenderer<ScarecrowTileEntity>
{
    /**
     * The model of the scarecrow.
     */
    private final ModelScarecrowBoth model;

    /**
     * Offset to the block middle.
     */
    private static final double BLOCK_MIDDLE = 0.5;

    /**
     * Y-Offset in order to have the scarecrow over ground.
     */
    private static final double YOFFSET = 1.5;

    /**
     * Which size the scarecrow should have ingame.
     */
    private static final double SIZERATIO = .0625;

    /**
     * Rotate the model some degrees.
     */
    private static final int ROTATION = 180;

    /**
     * Rotate it on the following x offset.
     */
    private static final float XROTATIONOFFSET = 0.311F;

    /**
     * Rotate it on the following y offset.
     */
    private static final float YROTATIONOFFSET = 0.0F;

    /**
     * Rotate it on the following z offset.
     */
    private static final float ZROTATIONOFFSET = 2.845F;


    /**
     * The public constructor for the renderer.
     */
    public TileEntityScarecrowRenderer()
    {
        super();
        this.model = new ModelScarecrowBoth();
    }
    
    @Override
    public void renderTileEntityAt(ScarecrowTileEntity te, double posX, double posY, double posZ, float partialTicks, int destroyStage)
    {
        //Store the transformation
        GlStateManager.pushMatrix();
        //Set viewport to tile entity position to render it
        GlStateManager.translate(posX+BLOCK_MIDDLE, posY+YOFFSET, posZ+BLOCK_MIDDLE);

        this.bindTexture(getResourceLocation(te));

        GlStateManager.rotate(ROTATION, XROTATIONOFFSET, YROTATIONOFFSET, ZROTATIONOFFSET);
        this.model.render((float) SIZERATIO);
        
        /* ============ Rendering Code stops here =========== */
        //Restore the transformation, so other renderer's are not messed up.
        GlStateManager.popMatrix();
    }

    /**
     * Returns the ResourceLocation of the scarecrow texture.
     * @param tileEntity the tileEntity of the scarecrow.
     * @return the location.
     */
    private static ResourceLocation getResourceLocation(ScarecrowTileEntity tileEntity)
    {
        String loc;
        
        if(tileEntity.getType() == ScarecrowTileEntity.ScareCrowType.PUMPKINHEAD)
        {
            loc = "textures/blocks/blockScarecrowPumpkin.png";
        }
        else
        {
            loc = "textures/blocks/blockScarecrowNormal.png";
        }
        
        return new ResourceLocation(Constants.MOD_ID + ":" + loc);
    }
}
