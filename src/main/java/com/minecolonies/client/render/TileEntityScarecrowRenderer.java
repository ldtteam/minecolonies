package com.minecolonies.client.render;

import com.minecolonies.blocks.BlockHutField;
import com.minecolonies.client.model.ModelScarecrowBoth;
import com.minecolonies.lib.Constants;
import com.minecolonies.tileentities.ScarecrowTileEntity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Class to render the scarecrow.
 */
@SideOnly(Side.CLIENT)
public class TileEntityScarecrowRenderer extends TileEntitySpecialRenderer<ScarecrowTileEntity>
{
    private final ModelScarecrowBoth model;

    public TileEntityScarecrowRenderer() { 
        this.model = new ModelScarecrowBoth();
    }
    
    @Override
    public void renderTileEntityAt(ScarecrowTileEntity te, double posX, double posY, double posZ, float partialTicks, int destroyStage) {
        GlStateManager.pushMatrix(); // store the transformation 
        GlStateManager.translate(posX+0.5, posY+1.5, posZ+0.5); // set viewport to tile entity position to render it
        
        /* ============ Rendering Code goes here ============ */
        EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(BlockHutField.FACING);
        
        this.bindTexture(this.getResourceLocation(te));

        GlStateManager.rotate(180, 0.311F, 0, 2.845F);
        this.model.render(.0625f);
        
        /* ============ Rendering Code stops here =========== */
        
        GlStateManager.popMatrix(); // restore the transformation, so other renderer's are not messed up
    }
    
    private ResourceLocation getResourceLocation(ScarecrowTileEntity tileentity) { 
        String loc;
        
        if(tileentity.getType())
            loc = "/textures/blocks/blockScarecrowPumpkin.png";
        else
            loc = "textures/blocks/blockScarecrowNormal.png";
        
        return new ResourceLocation(Constants.MOD_ID + ":" + loc);
    }
}