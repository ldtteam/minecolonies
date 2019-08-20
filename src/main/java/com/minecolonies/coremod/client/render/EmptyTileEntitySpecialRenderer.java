package com.minecolonies.coremod.client.render;

import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Renderer for a normal tile entity (Nothing special with rendering).
 */
@OnlyIn(Dist.CLIENT)
public class EmptyTileEntitySpecialRenderer extends TileEntityRenderer<TileEntityColonyBuilding>
{
    @Override
    public void render(final TileEntityColonyBuilding tileEntityIn, final double x, final double y, final double z, final float partialTicks, final int destroyStage)
    {
        super.render(tileEntityIn, x, y, z, partialTicks, destroyStage);
    }
}
