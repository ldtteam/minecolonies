package com.minecolonies.permissions;

import com.minecolonies.blocks.AbstractBlockHut;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.util.Log;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.item.ItemEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
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
        if (checkBlockEventDenied(event.world, event.pos, event.player, event.placedBlock)) {
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
        if (checkBlockEventDenied(event.world, event.pos, event.getPlayer(), event.world.getBlockState(event.pos))) {
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
        Log.logger.debug("ExplosionEvent.Detonate");

        // if block is in colony -> remove from list
        for (BlockPos pos: event.getAffectedBlocks())
        {
            if (colony.isCoordInColony(event.world, pos))
            {
                Log.logger.info("Found affected block in colony, removing from affected list");
                event.getAffectedBlocks().remove(pos);
            }
        }

        // if entity is in colony -> remove from list
        for (Entity entity: event.getAffectedEntities())
        {
            if (colony.isCoordInColony(entity.getEntityWorld(), entity.getPosition())) {
                Log.logger.info("Found affected entity in colony, removing from affected list");
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
        Log.logger.debug("Check if coordinate is in colony `" + this.colony.getName() + "`");
        if (colony.isCoordInColony(event.world, event.pos))
        {
            Log.logger.info("Coordinate is in colony");
            if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
            {
                final Block block = event.world.getBlockState(event.pos).getBlock();

                // Huts
                if (block instanceof AbstractBlockHut &&
                    !colony.getPermissions().hasPermission(event.entityPlayer, Permissions.Action.ACCESS_HUTS))
                {
                    event.useBlock = Event.Result.DENY;
                }
            }
        }
    }

    /**
     * TODO Check this behavior
     *
     * @param event
     */
    @SubscribeEvent
    public void on(final PlayerOpenContainerEvent event)
    {
        if (this.colony.isCoordInColony(event.entity.getEntityWorld(), event.entity.getPosition()))
        {
            if (this.colony.getPermissions().isColonyMember(event.entityPlayer))
            {
                Log.logger.info("Colony member `" + event.entityPlayer.getName() + "` opens container `" + event.entity.getName() + "`");
            }
            else
            {
                Log.logger.info("Player `" + event.entityPlayer.getName() + "` is not a member of `" + this.colony.getName() + "`");
            }
        }
        else
        {
            Log.logger.info("The opened entity is not inside `" + this.colony.getName() + "`");
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
        final EntityPlayer playerIn = event.player;

        Log.logger.debug(String.format("Check if playerIn `%s` is inside colony `%s`", playerIn.getName(), colony.getName()));
        if (colony.isCoordInColony(playerIn.getEntityWorld(), playerIn.getPosition()))
        {
            Log.logger.debug(String.format("Check if playerIn `%s` has at least Rank `%s`", playerIn.getName(), colony.getName()));

            Permissions.Rank rank = colony.getPermissions().getRank(playerIn);

            if (rank.ordinal() < Permissions.Rank.FRIEND.ordinal()) {
                event.setResult(Event.Result.DENY);
            }
        } else
        {
            Log.logger.info(String.format("Player `%s` is not inside colony `%s`", playerIn.getName(), colony.getName()));
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
        Log.logger.debug("Check if coordinate is in colony `" + this.colony.getName() + "`");
        if (colony.isCoordInColony(worldIn, posIn))
        {
            Log.logger.info("Coordinate is inside colony");

            Log.logger.debug("Check if player `" + playerIn.getName() + "` is member of `" + this.colony.getName() + "`.");
            if (colony.getPermissions().isColonyMember(playerIn)) {
                Log.logger.info("Player `" + playerIn.getName() + "` is member");
                final Permissions.Rank rank = colony.getPermissions().getRank(playerIn);

                Log.logger.debug("Check if player `" + playerIn.getName() + "` has at least rank `" + Permissions.Rank.NEUTRAL + "`");
                if (rank.ordinal() >= Permissions.Rank.NEUTRAL.ordinal())
                {
                    Log.logger.info("Player `" + playerIn.getName() + "` has no permission to place/break a block in `" + this.colony.getName() + "`");

                    return true;
                }

                Log.logger.debug("Check of block `" + blockState.getBlock().getRegistryName() + "` is instanceof `" + AbstractBlockHut.class.getSimpleName() + "`");
                if (blockState.getBlock() instanceof AbstractBlockHut) {
                    Log.logger.info("The block is instance of `" + AbstractBlockHut.class.getSimpleName() + "`");

                    Log.logger.debug("Check if player `" + playerIn.getName() + "` has at least rank `" + Permissions.Rank.OFFICER + "`");
                    if (rank.ordinal() >= Permissions.Rank.OFFICER.ordinal())
                    {
                        return true;
                    }
                }
            }
            else
            {
                Log.logger.info("Player `" + playerIn.getName() + "` is not a member of `" + this.colony.getName() + "`");
            }
        }
        else
        {
            Log.logger.info("The coordinate is not inside `" + this.colony.getName() + "`");
        }
        /*
         * - We are not inside the colony
         * - We are not a member
         * - We are an officer or owner
         */
        return false;
    }

}
