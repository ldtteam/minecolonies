package com.minecolonies.permissions.events;

import com.minecolonies.blocks.AbstractBlockHut;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.permissions.Permissions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
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

    @SubscribeEvent
    public void on(final BlockEvent.PlaceEvent event)
    {
        if (checkBlockEventDenied(event.world, event.pos, event.player, event.placedBlock)) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void on(final BlockEvent.BreakEvent event)
    {
        if (checkBlockEventDenied(event.world, event.pos, event.getPlayer(), event.world.getBlockState(event.pos))) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void on(ExplosionEvent.Start event)
    {
        final BlockPos blockPos = new BlockPos(event.explosion.getPosition());

        if (colony.isCoordInColony(event.world, blockPos))
        {
            event.setResult(Event.Result.DENY);
        }
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
        if (colony.isCoordInColony(worldIn, posIn))
        {
            // The coordinate is inside the colony
            if (colony.getPermissions().isColonyMember(playerIn)) {
                // The player is a member of the colony
                final Permissions.Rank rank = colony.getPermissions().getRank(playerIn);

                if (rank.ordinal() >= Permissions.Rank.NEUTRAL.ordinal())
                {
                    // We have no permissions

                    return true;
                }

                if (blockState.getBlock() instanceof AbstractBlockHut) {
                    // We break/place a hut

                    if (rank.ordinal() >= Permissions.Rank.OFFICER.ordinal())
                    {
                        return true;
                    }
                }
            }
        }

        /*
         * - We are not inside the colony
         * - We are not a member
         * - We are an officer or owner
         */
        return false;
    }

}
