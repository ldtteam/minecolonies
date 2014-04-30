package com.minecolonies.items;

import com.minecolonies.configuration.Configurations;
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
        if(!canPlaceBlockAt(world, x, y, z))
        {
            return itemStack;
        }
        spawnShip(world, x, y, z, entityPlayer);
        return itemStack;
    }

    public boolean canPlaceBlockAt(World world, int x, int y, int z)
    {
        if (!isFirstPlacing(world)) {
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

    boolean isFirstPlacing(World world)
    {
        if(Configurations.allowInfinitePlacing)
            return true;
            //TODO Check for already placed (player properties);
            return false;
    }

    private void spawnShip(World world, int x, int y, int z, EntityPlayer entityPlayer)
    {
      //TODO Spawn ship, spawn chest, fill chest, save new ship.
    }
}
