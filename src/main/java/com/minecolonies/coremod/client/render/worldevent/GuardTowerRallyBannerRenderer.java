package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.structurize.util.WorldRenderMacros;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.coremod.items.ItemBannerRallyGuards;

public class GuardTowerRallyBannerRenderer
{
    /**
     * Renders the rallying banner guard tower indicators into the world.
     * 
     * @param ctx rendering context
     */
    static void render(final WorldEventContext ctx)
    {
        if (ctx.getMainHandItem().getItem() != ModItems.bannerRallyGuards)
        {
            return;
        }

        for (final ILocation guardTower : ItemBannerRallyGuards.getGuardTowerLocations(ctx.getMainHandItem()))
        {
            if (ctx.getClientLevel().dimension() != guardTower.getDimension())
            {
                WorldRenderMacros.renderBlackLineBox(ctx.getBufferSource(),
                    ctx.getPoseStack(),
                    guardTower.getInDimensionLocation(),
                    guardTower.getInDimensionLocation(),
                    0.02f);
            }
        }
    }
}
