package com.minecolonies.client.render;

import com.minecolonies.tileentities.TileEntityColonyBuilding;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

/**
 * Renderer for a normal tile entity (Nothing special with rendering).
 */
public class EmptyTileEntitySpecialRenderer extends TileEntitySpecialRenderer<TileEntityColonyBuilding>
{
    
    /**
     * {@inheritDoc}
     * Method is empty because there are no special ways required to render.
     *
     * @param tileEntity        Tile entity to render
     * @param x                 X-coordinate
     * @param y                 Y-coordinate
     * @param z                 Z-coordinate
     * @param partialTicks      probably used for animations
     * @param destroyStage      don't know, doesn't really matter
     */
	@Override
	public void renderTileEntityAt(TileEntityColonyBuilding tileEntity, double x, double y, double z, float partialTicks, int destroyStage) {
		
	}
}
