package com.minecolonies.core.client.render.worldevent;

import com.minecolonies.api.items.ModDataComponents;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.core.items.ItemBannerRallyGuards;
import net.minecraft.core.BlockPos;

public class GuardTowerRallyBannerRenderer
{
    /**
     * Renders the rallying banner guard tower indicators into the world.
     * 
     * @param ctx rendering context
     */
    static void render(final WorldEventContext ctx)
    {
        if (ctx.mainHandItem.getItem() != ModItems.bannerRallyGuards)
        {
            return;
        }

        final ModDataComponents.ColonyId component = ctx.mainHandItem.getOrDefault(ModDataComponents.COLONY_ID_COMPONENT, ModDataComponents.ColonyId.EMPTY);
        if (component.id() == -1 || component.dimension() != ctx.clientLevel.dimension())
        {
            return;
        }

        for (final BlockPos guardTower : ItemBannerRallyGuards.getGuardTowerLocations(ctx.mainHandItem))
        {
            ctx.pushPoseCameraToPos(guardTower);
            ctx.renderBlackLineBox(BlockPos.ZERO, BlockPos.ZERO, WorldEventContext.DEFAULT_LINE_WIDTH);
            ctx.popPose();
        }
    }
}
