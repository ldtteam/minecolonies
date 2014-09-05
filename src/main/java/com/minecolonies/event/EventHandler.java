package com.minecolonies.event;

import com.minecolonies.blocks.BlockHut;
import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.PlayerProperties;
import com.minecolonies.tileentities.TileEntityHut;
import com.minecolonies.tileentities.TileEntityTownHall;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;

public class EventHandler
{
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event)
    {
        World world = event.world;

        if(!world.isRemote && event.block instanceof BlockHut)
        {
            TileEntityHut hut = (TileEntityHut) world.getTileEntity(event.x, event.y, event.z);
            EntityPlayer player = event.getPlayer();

            if(isPlayerOwner(hut, player))
            {
                if(hut != null)
                {
                    TileEntityTownHall townhall = hut.getTownHall();

                    if(hut instanceof TileEntityTownHall)
                    {
                        PlayerProperties.get(player).removeTownhall();
                    }
                    else if(townhall != null)
                    {
                        townhall.removeHut(hut.getPosition());
                    }
                    hut.breakBlock();
                }
            }
            else
            {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if(event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
        {
            EntityPlayer player = event.entityPlayer;
            World world = event.world;
            int x = event.x, y = event.y, z = event.z;

            if(!player.isSneaking() || player.getHeldItem() == null || player.getHeldItem().getItem() == null || player.getHeldItem().getItem().doesSneakBypassUse(world, x, y, z, player))
            {
                if(world.getBlock(x, y, z) instanceof BlockHut)//this was the simple way of doing it, minecraft calls onBlockActivated
                {                                              // and uses that return value, but I didn't want to call it twice
                    return;
                }
            }

            if(player.getHeldItem() == null || player.getHeldItem().getItem() == null) return;

            Block heldBlock = Block.getBlockFromItem(player.getHeldItem().getItem());
            if(heldBlock instanceof BlockHut)
            {
                switch(event.face)
                {
                    case 0:
                        y--;
                        break;
                    case 1:
                        y++;
                        break;
                    case 2:
                        z--;
                        break;
                    case 3:
                        z++;
                        break;
                    case 4:
                        x--;
                        break;
                    case 5:
                        x++;
                        break;
                }
                event.setCanceled(!onBlockHutPlaced(event.world, player, heldBlock, x, y, z));
            }
        }
    }

    /**
     * Called when a player tries to place a BlockHut. Returns true if successful and false to cancel the block placement.
     *
     * @param world  The world the player is in
     * @param player The player
     * @param block  The block type the player is placing
     * @param x      The x coordinate of the block
     * @param y      The y coordinate of the block
     * @param z      The z coordinate of the block
     * @return false to cancel the event
     */
    private boolean onBlockHutPlaced(World world, EntityPlayer player, Block block, int x, int y, int z)
    {
        if(block == ModBlocks.blockHutTownhall)
        {
            if(!world.provider.isSurfaceWorld())
            {
                LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHutTownhall.messageInvalidWorld");
                return false;
            }

            TileEntityTownHall closestTownHall = Utils.getClosestTownHall(world, x, y, z);
            if(closestTownHall != null && closestTownHall.getDistanceFrom(x, y, z) < Utils.square(2 * Configurations.workingRangeTownhall + Configurations.townhallPadding))
            {
                LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHutTownhall.messageTooClose");
                return false;
            }

            if(PlayerProperties.get(player).hasPlacedTownHall())
            {
                LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHutTownhall.messagePlacedAlready");
                return false;
            }
        }
        else
        {
            if(world.isRemote)
                return true;//Player properties aren't stored client side, so we must do this or huts will never be placed
            //Only downfall is it causes huts to flicker, when they get cancelled.
            TileEntityTownHall townhall = Utils.getTownhallByOwner(world, player);
            if(townhall == null || Utils.getDistanceToTileEntity(x, y, z, townhall) > Configurations.workingRangeTownhall)
            {
                if(townhall == null)
                {
                    LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHut.messageNoTownhall");
                }
                else
                {
                    LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHut.messageTooFarFromTownhall");
                }
                return false;
            }
        }
        return true;
    }

    private boolean isPlayerOwner(TileEntityHut hut, EntityPlayer player)
    {
        return hut == null || hut.isPlayerOwner(player);
    }

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

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        if(!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer)
        {
            PlayerProperties.saveProxyData((EntityPlayer) event.entity);
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event)
    {
        if(!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer)
        {
            PlayerProperties.loadProxyData((EntityPlayer) event.entity);
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        ColonyManager.onWorldLoad(event.world);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
        ColonyManager.onWorldUnload(event.world);
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event)
    {
        ColonyManager.onWorldSave(event.world);
    }
}
