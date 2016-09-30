package com.minecolonies.permissions;

import com.minecolonies.blocks.AbstractBlockHut;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.util.Log;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 *
 */
public class ColonyPermissionEventHandler
{

    private final Colony colony;

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
        if (checkBlockEventDenied(event.getWorld(), event.getPos(), event.getPlayer(), event.getPlacedBlock())) {
            event.setResult(Event.Result.DENY);
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
        if (checkBlockEventDenied(event.getWorld(), event.getPos(), event.getPlayer(), event.getWorld().getBlockState(event.getPos()))) {
            event.setResult(Event.Result.DENY);
        }
    }

    /**
     * ExplosionEvent.Detonate handler.
     *
     * @param event ExplosionEvent.Detonate
     */
    @SubscribeEvent
    public void on(ExplosionEvent.Detonate event)
    {
        Log.getLogger().debug("ExplosionEvent.Detonate");

        // if block is in colony -> remove from list
        for (BlockPos pos: event.getAffectedBlocks())
        {
            if (colony.isCoordInColony(event.getWorld(), pos))
            {
                Log.getLogger().info("Found affected block in colony, removing from affected list");
                event.getAffectedBlocks().remove(pos);
            }
        }

        // if entity is in colony -> remove from list
        for (Entity entity: event.getAffectedEntities())
        {
            if (colony.isCoordInColony(entity.getEntityWorld(), entity.getPosition())) {
                Log.getLogger().info("Found affected entity in colony, removing from affected list");
                event.getAffectedEntities().remove(entity);
            }
        }
    }

    /**
     * PlayerInteractEvent handler.
     *
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
        Log.getLogger().debug("Check if coordinate is in colony `" + this.colony.getName() + "`");
        if (colony.isCoordInColony(event.getWorld(), event.getPos()))
        {
            Log.getLogger().info("Coordinate is in colony");
            //todo: check to what hand rightclick maps to
            //if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)

            final Block block = event.getWorld().getBlockState(event.getPos()).getBlock();

            // Huts
            if (block instanceof AbstractBlockHut &&
                  !colony.getPermissions().hasPermission(event.getEntityPlayer(), Permissions.Action.ACCESS_HUTS))
            {
                //todo: hopefully that works
                event.setResult(Event.Result.DENY);
            }

        }
    }

    /**
     * TODO Check this behavior
     *
     * @param event
     */
    @SubscribeEvent
    public void on(final PlayerContainerEvent.Open event)
    {
        if (this.colony.isCoordInColony(event.getEntity().getEntityWorld(), event.getEntity().getPosition()))
        {
            if (this.colony.getPermissions().isColonyMember(event.getEntityPlayer()))
            {
                Log.getLogger().info("Colony member `" + event.getEntityPlayer().getName() + "` opens container `" + event.getEntity().getName() + "`");
            }
            else
            {
                Log.getLogger().info("Player `" + event.getEntity().getName() + "` is not a member of `" + this.colony.getName() + "`");
            }
        }
        else
        {
            Log.getLogger().info("The opened entity is not inside `" + this.colony.getName() + "`");
        }
    }

    /**
     * ItemTossEvent handler.
     *
     * @param event ItemTossEvent
     */
    @SubscribeEvent
    public void on(ItemTossEvent event)
    {
        final EntityPlayer playerIn = event.getPlayer();

        Log.getLogger().debug(String.format("Check if playerIn `%s` is inside colony `%s`", playerIn.getName(), colony.getName()));
        if (colony.isCoordInColony(playerIn.getEntityWorld(), playerIn.getPosition()))
        {
            Log.getLogger().debug(String.format("Check if playerIn `%s` has at least Rank `%s`", playerIn.getName(), colony.getName()));

            Permissions.Rank rank = colony.getPermissions().getRank(playerIn);

            if (rank.ordinal() < Permissions.Rank.FRIEND.ordinal()) {
                event.setResult(Event.Result.DENY);
            }
        } else
        {
            Log.getLogger().info(String.format("Player `%s` is not inside colony `%s`", playerIn.getName(), colony.getName()));
        }
    }

    /**
     * Template
     */
    public void on()
    {
        //
    }

    /**
     * This method returns TRUE if this event should be denied.
     *
     * @param worldIn
     * @param posIn
     * @param playerIn
     * @param blockState
     * @return
     */
    private boolean checkBlockEventDenied(final World worldIn, final BlockPos posIn, final EntityPlayer playerIn, final IBlockState blockState)
    {
        Log.getLogger().debug("Check if coordinate is in colony `" + this.colony.getName() + "`");
        if (colony.isCoordInColony(worldIn, posIn))
        {
            Log.getLogger().info("Coordinate is inside colony");

            Log.getLogger().debug("Check if player `" + playerIn.getName() + "` is member of `" + this.colony.getName() + "`.");
            if (colony.getPermissions().isColonyMember(playerIn)) {
                Log.getLogger().info("Player `" + playerIn.getName() + "` is member");
                final Permissions.Rank rank = colony.getPermissions().getRank(playerIn);

                Log.getLogger().debug("Check if player `" + playerIn.getName() + "` has at least rank `" + Permissions.Rank.NEUTRAL + "`");
                if (rank.ordinal() >= Permissions.Rank.NEUTRAL.ordinal())
                {
                    Log.getLogger().info("Player `" + playerIn.getName() + "` has no permission to place/break a block in `" + this.colony.getName() + "`");

                    return true;
                }

                Log.getLogger().debug("Check of block `" + blockState.getBlock().getRegistryName() + "` is instanceof `" + AbstractBlockHut.class.getSimpleName() + "`");
                if (blockState.getBlock() instanceof AbstractBlockHut) {
                    Log.getLogger().info("The block is instance of `" + AbstractBlockHut.class.getSimpleName() + "`");

                    Log.getLogger().debug("Check if player `" + playerIn.getName() + "` has at least rank `" + Permissions.Rank.OFFICER + "`");
                    if (rank.ordinal() >= Permissions.Rank.OFFICER.ordinal())
                    {
                        return true;
                    }
                }
            }
            else
            {
                Log.getLogger().info("Player `" + playerIn.getName() + "` is not a member of `" + this.colony.getName() + "`");
            }
        }
        else
        {
            Log.getLogger().info("The coordinate is not inside `" + this.colony.getName() + "`");
        }
        /*
         * - We are not inside the colony
         * - We are not a member
         * - We are an officer or owner
         */
        return false;
    }

}
