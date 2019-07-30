package com.minecolonies.coremod.client.render;

import com.minecolonies.coremod.tileentities.ITileEntityColonyBuilding;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

/**
 * Renderer for a normal tile entity (Nothing special with rendering).
 */
public class EmptyTileEntitySpecialRenderer extends TileEntitySpecialRenderer<TileEntityColonyBuilding>
{
    @Override
    public void render(final TileEntityColonyBuilding te, final double x, final double y, final double z, final float partialTicks, final int destroyStage, final float alpha)
    {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);
    }
}
