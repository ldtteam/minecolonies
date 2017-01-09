package com.minecolonies.coremod.permissions;

import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.entity.EntityCitizen;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class handles all permission checks on events and cancels them if needed.
 */
public class ColonyPermissionEventHandler
{

    private final Colony colony;

    /**
     * Create this EventHandler.
     *
     * @param colony the colony to check on.
     */
    public ColonyPermissionEventHandler(final Colony colony)
    {
        this.colony = colony;
    }

    /**
     * BlockEvent.PlaceEvent handler.
     *
     * @param event BlockEvent.PlaceEvent
     */
    @SubscribeEvent
    public void on(final BlockEvent.PlaceEvent event)
    {
        if (Configurations.enableColonyProtection && checkBlockEventDenied(event.getWorld(), event.getPos(), event.getPlayer(), event.getPlacedBlock()))
        {
            cancelEvent(event);
        }
    }

    /**
     * This method returns TRUE if this event should be denied.
     *
     * @param worldIn    the world to check in
     * @param posIn      the block to check
     * @param playerIn   the player who tries
     * @param blockState the state that block is in
     * @return true if canceled
     */
    private boolean checkBlockEventDenied(final World worldIn, final BlockPos posIn, final EntityPlayer playerIn, final IBlockState blockState)
    {
        if (colony.isCoordInColony(worldIn, posIn))
        {
            if (!colony.getPermissions().isColonyMember(playerIn))
            {
                return true;
            }
            final Permissions.Rank rank = colony.getPermissions().getRank(playerIn);
            if (rank.ordinal() >= Permissions.Rank.FRIEND.ordinal())
            {
                return true;
            }
            if (blockState.getBlock() instanceof AbstractBlockHut
                  && rank.ordinal() >= Permissions.Rank.OFFICER.ordinal())
            {
                return true;
            }
        }
        /*
         * - We are not inside the colony
         * - We are in but not denied
         */
        return false;
    }

    private static void cancelEvent(final Event event)
    {
        event.setResult(Event.Result.DENY);
        if (event.isCancelable())
        {
            event.setCanceled(true);
        }
    }

    /**
     * BlockEvent.BreakEvent handler.
     *
     * @param event BlockEvent.BreakEvent
     */
    @SubscribeEvent
    public void on(final BlockEvent.BreakEvent event)
    {
        if (Configurations.enableColonyProtection && checkBlockEventDenied(event.getWorld(), event.getPos(), event.getPlayer(), event.getWorld().getBlockState(event.getPos())))
        {
            cancelEvent(event);
        }
    }

    /**
     * ExplosionEvent.Detonate handler.
     *
     * @param event ExplosionEvent.Detonate
     */
    @SubscribeEvent
    public void on(final ExplosionEvent.Detonate event)
    {
        if(!Configurations.enableColonyProtection || !Configurations.turnOffExplosionsInColonies)
        {
            return;
        }

        final World eventWorld = event.getWorld();
        final Predicate<BlockPos> getBlocksInColony = pos -> colony.isCoordInColony(eventWorld, pos);
        final Predicate<Entity> getEntitiesInColony = entity -> colony.isCoordInColony(entity.getEntityWorld(), entity.getPosition());
        // if block is in colony -> remove from list
        final List<BlockPos> blocksToRemove = event.getAffectedBlocks().stream()
                                                .filter(getBlocksInColony)
                                                .collect(Collectors.toList());

        // if entity is in colony -> remove from list
        final List<Entity> entitiesToRemove = event.getAffectedEntities().stream()
                                                .filter(getEntitiesInColony)
                                                .collect(Collectors.toList());
        event.getAffectedBlocks().removeAll(blocksToRemove);
        event.getAffectedEntities().removeAll(entitiesToRemove);
    }

    /**
     * ExplosionEvent.Start handler.
     *
     * @param event ExplosionEvent.Detonate
     */
    @SubscribeEvent
    public void on(final ExplosionEvent.Start event)
    {
        if (Configurations.enableColonyProtection
                && Configurations.turnOffExplosionsInColonies
                && colony.isCoordInColony(event.getWorld(), new BlockPos(event.getExplosion().getPosition())))
        {
            cancelEvent(event);
        }
    }

    /**
     * PlayerInteractEvent handler.
     * <p>
     * Check, if a player right clicked a block.
     * Deny if:
     * - If the block is in colony
     * - block is AbstractBlockHut
     * - player has not permission
     *
     * @param event PlayerInteractEvent
     */
    @SubscribeEvent
    public void on(final PlayerInteractEvent event)
    {
        if (colony.isCoordInColony(event.getWorld(), event.getPos()))
        {
            final Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
            // Huts
            if (block instanceof AbstractBlockHut
                    && !colony.getPermissions().hasPermission(event.getEntityPlayer(), Permissions.Action.ACCESS_HUTS))
            {
                cancelEvent(event);
            }

            if(Configurations.enableColonyProtection && event.getWorld().getBlockState(event.getPos()).getBlock() instanceof BlockContainer)
            {
                final Permissions.Rank rank = colony.getPermissions().getRank(event.getEntityPlayer());

                if (rank.ordinal() >= Permissions.Rank.FRIEND.ordinal())
                {
                    cancelEvent(event);
                }
            }

            if(event.getItemStack() != null
                    && event.getItemStack().getItem() instanceof ItemMonsterPlacer
                    && !colony.getPermissions().hasPermission(event.getEntityPlayer(), Permissions.Action.PLACE_HUTS))
            {
                cancelEvent(event);
            }
        }

    }

    /**
     * ItemTossEvent handler.
     * <p>
     * Check, if a player tossed a block.
     * Deny if:
     * - If the tossing happens in the colony
     * - player is hostile to colony
     *
     * @param event ItemTossEvent
     */
    @SubscribeEvent
    public void on(final ItemTossEvent event)
    {
        final EntityPlayer playerIn = event.getPlayer();
        if (Configurations.enableColonyProtection && colony.isCoordInColony(playerIn.getEntityWorld(), playerIn.getPosition()))
        {
            final Permissions.Rank rank = colony.getPermissions().getRank(playerIn);

            if (rank.ordinal() > Permissions.Rank.NEUTRAL.ordinal())
            {
                /*
                    this will delete the item entirely:
                    Canceling the event will stop the items from entering the world,
                    but will not prevent them being removed from the inventory
                    - and thus removed from the system.
                 */
                cancelEvent(event);
            }
        }
    }

    /**
     * EntityItemPickupEvent handler.
     * <p>
     * Check, if a player tries to pickup a block.
     * Deny if:
     * - If the pickUp happens in the colony
     * - player is neutral or hostile to colony
     *
     * @param event EntityItemPickupEvent
     */
    @SubscribeEvent
    public void on(final EntityItemPickupEvent event)
    {
        final EntityPlayer playerIn = event.getEntityPlayer();
        if (Configurations.enableColonyProtection && colony.isCoordInColony(playerIn.getEntityWorld(), playerIn.getPosition()))
        {
            final Permissions.Rank rank = colony.getPermissions().getRank(playerIn);

            if (rank.ordinal() > Permissions.Rank.FRIEND.ordinal())
            {
                cancelEvent(event);
            }
        }
    }

    /**
     * AttackEntityEvent handler.
     * <p>
     * Check, if a player tries to attack an entity..
     * Deny if:
     * - If the attacking happens in the colony
     * - Player is less than officer to the colony.
     * - Entity is a citizen.
     *
     * @param event EntityItemPickupEvent
     */
    @SubscribeEvent
    public void on(final AttackEntityEvent event)
    {
        final EntityPlayer playerIn = event.getEntityPlayer();
        if (Configurations.enableColonyProtection && colony.isCoordInColony(playerIn.getEntityWorld(), playerIn.getPosition()) && event.getTarget() instanceof EntityCitizen )
        {
            final Permissions.Rank rank = colony.getPermissions().getRank(playerIn);

            if (rank.ordinal() > Permissions.Rank.FRIEND.ordinal())
            {
                cancelEvent(event);
            }
        }
    }
}
