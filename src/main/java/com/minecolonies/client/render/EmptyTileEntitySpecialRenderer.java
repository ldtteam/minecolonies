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
     * @param rotation          Rotation of the entity
     */
    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float rotation)
    {

    }
}
