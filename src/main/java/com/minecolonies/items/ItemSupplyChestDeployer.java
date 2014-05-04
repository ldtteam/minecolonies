package com.minecolonies.items;

import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.PlayerProperties;
import com.minecolonies.lib.Constants;
import com.minecolonies.lib.Schematic;
import com.minecolonies.util.CreativeTab;
import com.minecolonies.util.IColony;
import com.minecolonies.util.Utils;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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
        if(world == null || entityPlayer == null || world.isRemote || itemStack.stackSize == 0) return itemStack;
        MovingObjectPosition blockPos = getMovingObjectPositionFromPlayer(world, entityPlayer, false);
        if(blockPos == null) return itemStack;
        int x = blockPos.blockX;
        int y = blockPos.blockY;
        int z = blockPos.blockZ;
        if(!canShipBePlaced(world, x, y, z, entityPlayer))
        {
            return itemStack;
        }
        spawnShip(world, x, y, z, entityPlayer, getChestFacing(world, x, y, z));
        return itemStack;
    }

    /**
     * Checks if the ship can be placed, by checking an area for water and returns metadata/facing of chest
     *
     * @param world        world
     * @param x            xCoord clicked
     * @param y            yCoord clicked
     * @param z            zCoord clicked
     * @param entityPlayer Player
     * @return true if ship can be places, false else
     */
    public boolean canShipBePlaced(World world, int x, int y, int z, EntityPlayer entityPlayer)
    {
        if(!isFirstPlacing(entityPlayer))
        {
            FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("Supply Chest Already Placed"));

            return false;
        }
        if(Utils.isWater(world.getBlock(x - 1, y, z)) && Utils.isWater(world.getBlock(x - 15, y, z)) && Utils.isWater(world.getBlock(x - 15, y, z + 23)))
            return true; //Chest: East
        if(Utils.isWater(world.getBlock(x + 1, y, z)) && Utils.isWater(world.getBlock(x + 15, y, z)) && Utils.isWater(world.getBlock(x + 15, y, z - 23)))
            return true; //Chest: West
        if(Utils.isWater(world.getBlock(x, y, z - 1)) && Utils.isWater(world.getBlock(x, y, z - 15)) && Utils.isWater(world.getBlock(x - 23, y, z - 15)))
            return true; //Chest: South
        if(Utils.isWater(world.getBlock(x, y, z + 1)) && Utils.isWater(world.getBlock(x, y, z + 15)) && Utils.isWater(world.getBlock(x + 23, y, z + 15)))
            return true; //Chest: North

        FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("Supply Chest Must Be next to a large body of water"));

        return false;
    }

    /**
     * Checks if the ship can be placed, by checking an area for water and returns metadata/facing of chest
     *
     * @param world        world
     * @param x            xCoord clicked
     * @param y            yCoord clicked
     * @param z            zCoord clicked
     * @return 2: north, 3: south 4: west 5: east
     */
    public int getChestFacing(World world, int x, int y, int z)
    {
        if(Utils.isWater(world.getBlock(x - 1, y, z)) && Utils.isWater(world.getBlock(x - 15, y, z)) && Utils.isWater(world.getBlock(x - 15, y, z + 23)))
            return 5; //East
        if(Utils.isWater(world.getBlock(x + 1, y, z)) && Utils.isWater(world.getBlock(x + 15, y, z)) && Utils.isWater(world.getBlock(x + 15, y, z - 23)))
            return 4; //West
        if(Utils.isWater(world.getBlock(x, y, z - 1)) && Utils.isWater(world.getBlock(x, y, z - 15)) && Utils.isWater(world.getBlock(x - 23, y, z - 15)))
            return 3; //South
        else return 2; //North
    }

    /**
     * Checks if the player already placed a supply chest
     *
     * @param player The player
     * @return boolean, returns true when player hasn't placed before, or when infinite placing is on.
     */
    boolean isFirstPlacing(EntityPlayer player)
    {
        if(Configurations.allowInfinitePlacing)
            return true;
        return !PlayerProperties.get(player).hasPlacedSupplyChest();
    }

    /**
     * Spawns the ship and supply chest
     *
     * @param world        world obj
     * @param x            xCoord clicked
     * @param y            yCoord clicked
     * @param z            zCoord clicked
     * @param entityPlayer the player
     */
    private void spawnShip(World world, int x, int y, int z, EntityPlayer entityPlayer, int chestFacing)
    {
        //TODO Spawn ship
        PlayerProperties playerProperties = (PlayerProperties) entityPlayer.getExtendedProperties(Constants.PlayerPropertyName);
        playerProperties.setHasPlacedSupplyChest(true);

        world.setBlock(x, y + 1, z, Blocks.chest);
        world.setBlockMetadataWithNotify(x, y + 1, z, chestFacing, 2);

        Schematic.loadAndPlaceSchematic(world, "test", x, y + 5, z);
    }
}
