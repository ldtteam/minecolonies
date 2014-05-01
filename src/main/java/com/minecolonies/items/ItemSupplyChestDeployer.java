package com.minecolonies.items;

import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.PlayerProperties;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.CreativeTab;
import com.minecolonies.util.IColony;
import com.minecolonies.util.Utils;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class ItemSupplyChestDeployer extends net.minecraft.item.Item implements IColony
{
    private String name = "supplyChestDeployer";
    public ItemSupplyChestDeployer()
    {
        setUnlocalizedName(getName());
        setCreativeTab(CreativeTab.mineColoniesTab);
        setMaxStackSize(1);
        GameRegistry.registerItem(this, getName());
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer)
    {
        if(world == null || entityPlayer == null || world.isRemote || itemStack.stackSize == 0)
            return itemStack;
        MovingObjectPosition blockPos = getMovingObjectPositionFromPlayer(world, entityPlayer, false);
        if(blockPos == null)
            return itemStack;
        int x = blockPos.blockX;
        int y = blockPos.blockY;
        int z = blockPos.blockZ;
        if(!canShipBePlaced(world, x, y, z, entityPlayer))
        {
            return itemStack;
        }
        spawnShip(world, x, y, z, entityPlayer);
        return itemStack;
    }

    /**
     * Checks if the ship can be placed, by checking an area for water
     * @param world world
     * @param x xCoord clicked
     * @param y yCoord clicked
     * @param z zCoord clicked
     * @param entityPlayer Player
     * @return true if ship can be placed, false otherwise
     */
    public boolean canShipBePlaced(World world, int x, int y, int z, EntityPlayer entityPlayer)
    {
        if (!isFirstPlacing(world, entityPlayer)) {
            FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("Supply Chest Already Placed"));

            return false;
        }
        if (Utils.isWater(world.getBlock(x - 1, y, z)) && Utils.isWater(world.getBlock(x - 15, y, z)) && Utils.isWater(world.getBlock(x - 15, y, z + 23)))
            return true;
        if (Utils.isWater(world.getBlock(x + 1, y, z)) && Utils.isWater(world.getBlock(x + 15, y, z)) && Utils.isWater(world.getBlock(x + 15, y, z - 23)))
            return true;
        if (Utils.isWater(world.getBlock(x, y, z - 1)) && Utils.isWater(world.getBlock(x, y, z - 15)) && Utils.isWater(world.getBlock(x - 23, y, z - 15)))
            return true;
        if (Utils.isWater(world.getBlock(x, y, z + 1)) && Utils.isWater(world.getBlock(x, y, z + 15)) && Utils.isWater(world.getBlock(x + 23, y, z + 15)))
            return true;

        FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("Supply Chest Must Be next to a large body of water"));

        return false;
    }

    /**
     * Checks if the player already placed a supply chest
     * @param world World obj
     * @param player The player
     * @return boolean, returns true when player hasn't placed before, or when infinite placing is on.
     */
    boolean isFirstPlacing(World world, EntityPlayer player)
    {
        if(Configurations.allowInfinitePlacing)
            return true;
        return !PlayerProperties.get(player).hasPlacedSupplyChest();
    }

    /**
     * Spawns the ship and supply chest
     * @param world world obj
     * @param x xCoord clicked
     * @param y yCoord clicked
     * @param z zCoord clicked
     * @param player the player
     */
    private void spawnShip(World world, int x, int y, int z, EntityPlayer player)
    {
      //TODO Spawn ship, spawn chest, fill chest, save new ship.
        PlayerProperties.get(player).setHasPlacedSupplyChest(true);
    }
}
