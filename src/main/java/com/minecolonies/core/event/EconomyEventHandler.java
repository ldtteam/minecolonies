package com.minecolonies.core.event;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHall;
import com.minecolonies.core.economy.EconomyManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Hooks the economy into the server lifecycle. There are no client classes or
 * custom payloads here; the only UI action is opening vanilla's merchant menu.
 */
public final class EconomyEventHandler
{
    private EconomyEventHandler()
    {
    }

    @SubscribeEvent
    public static void onServerTick(final ServerTickEvent.Pre event)
    {
        EconomyManager.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onTownHallMarket(final PlayerInteractEvent.RightClickBlock event)
    {
        if (event.isCanceled() || event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND || !(event.getEntity() instanceof ServerPlayer player) || !player.isShiftKeyDown())
        {
            return;
        }

        final IBuilding building = IColonyManager.getInstance().getBuilding(event.getLevel(), event.getPos());
        if (!(building instanceof ITownHall))
        {
            return;
        }

        final IColony colony = building.getColony();
        if (!(player.level() instanceof ServerLevel serverLevel))
        {
            return;
        }
        final var server = serverLevel.getServer();
        if (!EconomyManager.ledger(server, colony).enabled())
        {
            return;
        }

        if (player.distanceToSqr(colony.getCenter().getX() + 0.5D, colony.getCenter().getY() + 0.5D, colony.getCenter().getZ() + 0.5D) > 64.0D * 64.0D)
        {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        new com.minecolonies.core.economy.MarketMerchant(
            server, colony, EconomyManager.ledger(server, colony)).openFor(player);
    }
}
