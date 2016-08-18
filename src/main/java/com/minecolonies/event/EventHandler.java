package com.minecolonies.event;

import com.minecolonies.blocks.AbstractBlockHut;
import com.minecolonies.blocks.BlockHutTownHall;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.IColony;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.entity.PlayerProperties;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Handles all forge events.
 */
public class EventHandler
{
    /**
     * Event when a block is broken
     * Event gets cancelled when there no permission to break a hut
     *
     * @param event     {@link net.minecraftforge.event.world.BlockEvent.BreakEvent}
     */
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event)
    {
        World world = event.world;

        if(!world.isRemote && event.state.getBlock() instanceof AbstractBlockHut)
        {
            AbstractBuilding building = ColonyManager.getBuilding(world, event.pos);
            if (building == null)
            {
                return;
            }

            if (!building.getColony().getPermissions().hasPermission(event.getPlayer(), Permissions.Action.BREAK_HUTS))
            {
                event.setCanceled(true);
                return;
            }

            building.destroy();
        }
    }

    /**
     * Event when a player right clicks a block, or right clicks with an item
     * Event gets cancelled when player has no permission
     * Event gets cancelled when the player has no permission to place a hut, and tried it
     *
     * @param event {@link PlayerInteractEvent}
     */
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if(event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
        {
            EntityPlayer player = event.entityPlayer;
            World world = event.world;

            if (playerRightClickInteract(player, world, event.pos) &&
                    // this was the simple way of doing it, minecraft calls onBlockActivated
                    // and uses that return value, but I didn't want to call it twice
                    world.getBlockState(event.pos).getBlock() instanceof AbstractBlockHut)
            {
                IColony colony = ColonyManager.getIColony(world, event.pos);
                if (colony != null &&
                        !colony.getPermissions().hasPermission(player, Permissions.Action.ACCESS_HUTS))
                {
                    event.setCanceled(true);
                }

                return;
            }

            if(player.getHeldItem() == null || player.getHeldItem().getItem() == null)
            {
                return;
            }

            handleEventCancellation(event, player);
        }
    }

    private void handleEventCancellation(PlayerInteractEvent event, EntityPlayer player)
    {
        Block heldBlock = Block.getBlockFromItem(player.getHeldItem().getItem());
        if(heldBlock instanceof AbstractBlockHut)
        {
            event.setCanceled(!onBlockHutPlaced(event.world, player, heldBlock, event.pos.offset(event.face)));
        }
    }

    private static boolean playerRightClickInteract(EntityPlayer player, World world, BlockPos pos)
    {
        return !player.isSneaking() || player.getHeldItem() == null || player.getHeldItem().getItem() == null ||
                player.getHeldItem().getItem().doesSneakBypassUse(world, pos, player);
    }

    /**
     * Called when a player tries to place a AbstractBlockHut. Returns true if successful and false to cancel the block placement.
     *
     * @param world  The world the player is in
     * @param player The player
     * @param block  The block type the player is placing
     * @param pos    The location of the block
     * @return       false to cancel the event
     */
    public static boolean onBlockHutPlaced(World world, EntityPlayer player, Block block, BlockPos pos)
    {
        if (block instanceof BlockHutTownHall)
        {
            return onTownHallPlaced(world, player, pos);
        }
        else
        {
            return onBlockHutPlaced(world, player, pos);
        }
    }

    static boolean onTownHallPlaced(World world, EntityPlayer player, BlockPos pos)
    {
        IColony colony = ColonyManager.getIColonyByOwner(world, player);
        if (colony != null)
        {
            return canOwnerPlaceTownHallHere(world, player, colony, pos);
        }

        colony = ColonyManager.getClosestIColony(world, pos);
        if(colony == null)
        {
            createColony(world, player, pos);
            return true;
        }

        //  Town Halls must be far enough apart
        return canPlayerPlaceTownHallHere(world, player, pos, colony);
    }

    private static boolean canOwnerPlaceTownHallHere(World world, EntityPlayer player, IColony colony, BlockPos pos)
    {
        if(!colony.isCoordInColony(world, pos) || colony.hasTownHall())
        {
            //  Players are currently only allowed a single colony
            LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHutTownHall.messagePlacedAlready");
            return false;
        }

        IColony currentColony = ColonyManager.getIColony(world, pos);
        if(currentColony != colony)
        {
            LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHutTownhall.messageTooFar");
            return false;
        }

        return true;
    }

    private static boolean canPlayerPlaceTownHallHere(World world, EntityPlayer player, BlockPos pos, IColony closestColony)
    {
        // Is the player trying to place a town hall in a colony
        if (closestColony.isCoordInColony(world, pos))
        {
            if (closestColony.hasTownHall() || !closestColony.getPermissions().isColonyMember(player))
            {
                //  Placing in a colony which already has a town hall
                LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHutTownHall.messageTooClose");
                return false;
            }

            if (!closestColony.getPermissions().hasPermission(player, Permissions.Action.PLACE_HUTS))
            {
                //  No permission to place hut in colony
                LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHut.messageNoPermissionPlace", closestColony.getName());
                return false;
            }

            return true;
        }

        if (closestColony.getDistanceSquared(pos) <= MathUtils.square(ColonyManager.getMinimumDistanceBetweenTownHalls()))
        {
            //  Placing too close to an existing colony
            LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHutTownHall.messageTooClose");
            return false;
        }

        createColony(world, player, pos);
        return true;
    }

    private static void createColony(World world, EntityPlayer player, BlockPos pos)
    {
        if (!world.isRemote)
        {
            ColonyManager.createColony(world, pos, player);
        }
    }

    private static boolean onBlockHutPlaced(World world, EntityPlayer player, BlockPos pos)
    {
        IColony colony = ColonyManager.getIColony(world, pos);

        if (colony == null)
        {
            //  Not in a colony
            LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHut.messageNoTownHall");
            return false;
        }
        else if (!colony.getPermissions().hasPermission(player, Permissions.Action.PLACE_HUTS))
        {
            //  No permission to place hut in colony
            LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHut.messageNoPermission", colony.getName());
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * Called when an entity is being constructed
     * Used to register player properties
     *
     * @param event     {@link net.minecraftforge.event.entity.EntityEvent.EntityConstructing}
     */
    @SubscribeEvent
    public void onEntityConstructing(EntityEvent.EntityConstructing event)
    {
        if(event.entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) event.entity;
            if(PlayerProperties.get(player) == null)
            {
                PlayerProperties.register(player);
            }
        }
    }

    /**
     * Called when an entity dies
     * Player property data is saved when a player dies
     *
     * @param event     {@link LivingDeathEvent}
     */
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        if(!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer)
        {
            PlayerProperties.saveProxyData((EntityPlayer) event.entity);
        }
    }

    /**
     * Called when an entity joins the world
     * Loads player property data when player enters
     *
     * @param event     {@link EntityJoinWorldEvent}
     */
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event)
    {
        if(!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer)
        {
            PlayerProperties.loadProxyData((EntityPlayer) event.entity);
        }
    }

    /**
     * Gets called when world loads.
     * Calls {@link ColonyManager#onWorldLoad(World)}
     *
     * @param event     {@link net.minecraftforge.event.world.WorldEvent.Load}
     */
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        ColonyManager.onWorldLoad(event.world);
    }

    /**
     * Gets called when world unloads.
     * Calls {@link ColonyManager#onWorldUnload(World)}
     *
     * @param event     {@link net.minecraftforge.event.world.WorldEvent.Unload}
     */
    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
        ColonyManager.onWorldUnload(event.world);
    }

    /**
     * Gets called when world saves.
     * Calls {@link ColonyManager#onWorldSave(World)}
     *
     * @param event     {@link net.minecraftforge.event.world.WorldEvent.Save}
     */
    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event)
    {
        ColonyManager.onWorldSave(event.world);
    }
}
