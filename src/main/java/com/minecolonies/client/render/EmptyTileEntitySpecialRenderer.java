package com.minecolonies.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

/**
 * Renderer for a normal tile entity (Nothing special with rendering)
 */
public class EmptyTileEntitySpecialRenderer extends TileEntitySpecialRenderer
{
	/**
	 * {@inheritDoc}
	 * Method is empty because there are no special ways required to render
	 *
	 * @param tileEntity        Tile entity to render
	 * @param x                 X-coordinate
	 * @param y                 Y-coordinate
	 * @param z                 Z-coordinate
	 * @param partialTicks
	 */
	@Override
	public void renderTileEntityAt(final TileEntity tileEntity, final double x, final double y, final double z, final float partialTicks)
	{

	}
}
