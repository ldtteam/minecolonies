package com.minecolonies.event;

import com.minecolonies.blocks.BlockHut;
import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.PlayerProperties;
import com.minecolonies.tileentities.TileEntityHut;
import com.minecolonies.tileentities.TileEntityTownHall;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

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

            if(hut != null && isPlayerOwner(hut, player))
            {
                TileEntityTownHall townhall = hut.getTownHall();

                if(hut instanceof TileEntityTownHall)
                {
                    PlayerProperties.get(player).removeTownhall();
                }
                else if(townhall != null)
                {
                    townhall.removeHut(hut.xCoord, hut.yCoord, hut.zCoord);
                }
                hut.breakBlock();
            }
            else
            {
                event.setCanceled(hut != null);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if(event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && event.entityPlayer.getHeldItem() != null)
        {
            Block block = Block.getBlockFromItem(event.entityPlayer.getHeldItem().getItem());
            if(block instanceof BlockHut)
            {
                int x = event.x, y = event.y, z = event.z;
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
                event.setCanceled(!onBlockPlaced(event.entityPlayer.worldObj, event.entityPlayer, block, x, y, z));
            }
        }
    }

    private boolean onBlockPlaced(World world, EntityPlayer player, Block block, int x, int y, int z)
    {
        if(block == ModBlocks.blockHutTownhall)
        {
            if(!world.provider.isSurfaceWorld())
            {
                LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHutTownhall.messageInvalidWorld");
                return false;
            }

            TileEntityTownHall closestTownHall = Utils.getClosestTownHall(world, x, y, z);
            if(closestTownHall != null && closestTownHall.getDistanceFrom(x, y, z) < Math.pow(2 * Configurations.workingRangeTownhall + Configurations.townhallPadding, 2))
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
}
