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
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import java.util.HashMap;

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
        if(world == null || entityPlayer == null || world.isRemote || itemStack.stackSize == 0 || !isFirstPlacing(entityPlayer)) return itemStack;
        MovingObjectPosition blockPos = getMovingObjectPositionFromPlayer(world, entityPlayer, false);
        if(blockPos == null) return itemStack;
        int x = blockPos.blockX;
        int y = blockPos.blockY;

        int z = blockPos.blockZ;
        HashMap<Integer, Boolean> hashmap = canShipBePlaced(world, x, y, z);
     // System.out.println("hashmap 1 : " + hashmap.get(1));
     // System.out.println("hashmap 2 : " + hashmap.get(2));
     // System.out.println("hashmap 3 : " + hashmap.get(3));
     // System.out.println("hashmap 4 : " + hashmap.get(4));
     // System.out.println("hashmap 5 : " + hashmap.get(5));

        if(hashmap.get(1))
            for(int i = 2; i <= 5; i++)
                if(hashmap.get(i) != null)
                    if(hashmap.get(i))
                    {
                        spawnShip(world, x, y, z, entityPlayer, i);
                        return itemStack;
                    }
        FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("You must be near a big pool of water"));
        return itemStack;
    }

    /**
     * Checks if the ship can be placed, and stores the facings it can be placed in, in a hashmap
     * Keys: 1: value: can be placed at all
     *       2: value: can be placed at north
     *       3: value: can be placed at south
     *       4: value: can be placed at west
     *       5: value: can be placed at east
     *
     * @param world world obj
     * @param x xCoord clicked
     * @param y yCoord clicked
     * @param z zCoord clicked
     * @return hashMap whether it can be placed (1) and facings it can be placed at (2-5)
     */
    public HashMap<Integer, Boolean> canShipBePlaced(World world, int x, int y, int z)
    {
        HashMap<Integer, Boolean> hashMap = new HashMap<Integer, Boolean>();
        if(Utils.isWater(world.getBlock(x + 1, y, z)) && check(world, x, y, z, true, true))
        {
            hashMap.put(4, true);
            hashMap.put(1, true);
            return hashMap;
        }
        else if(Utils.isWater(world.getBlock(x - 1, y, z)) && check(world, x, y, z, true, false))
        {
            hashMap.put(5, true);
            if(!hashMap.containsKey(1)) hashMap.put(1, true);
            return hashMap;
        }
        else if(Utils.isWater(world.getBlock(x, y, z - 1)) && check(world, x, y, z, false, false))
        {
            hashMap.put(3, true);
            if(!hashMap.containsKey(1)) hashMap.put(1, true);
            return hashMap;
        }
        else if((Utils.isWater(world.getBlock(x, y, z + 1))) && check(world, x, y, z, false, true))
        {
            hashMap.put(2, true);
            if(!hashMap.containsKey(1)) hashMap.put(1, true);
            return hashMap;
        }
        if(hashMap.get(1) == null) hashMap.put(1, false);
        return hashMap;
    }

    /**
     * Checks if the area is free, checks in a 'I' shape, so 20 forward, 10 left at origin + 1, 10 right at origin + 1, 10 left at origin + 20, 10 right at origin + 20
     * @param world world obj
     * @param x xCoord clicked
     * @param y yCoord clicked
     * @param z zCoord clicked
     * @param shouldCheckX boolean whether the x-sides should be checks
     * @param isCoordPositivelyAdded boolean whether the x or z side should be check on the positive side (true) or negative  side (false)
     * @return whether the space in the I shape is free or not
     */
    private boolean check(World world, int x, int y, int z, boolean shouldCheckX, boolean isCoordPositivelyAdded)
    {
        int spaceNeededForShip = Constants.SIZENEEDEDFORSHIP;
        int spaceNeededForShipHalf = spaceNeededForShip >> 1;//Constants.SIZENEEDEDFORSHIP / 2;
        int k = isCoordPositivelyAdded ? 1 : -1;

        if(shouldCheckX)
        {
            for(int i = 0; i < spaceNeededForShip; i++)
            {
                int j = k * i;
                if(!Utils.isWater(world.getBlock(x + j + k, y, z)) ||
                        !Utils.isWater(world.getBlock(x + k * spaceNeededForShipHalf, y, z - spaceNeededForShipHalf + i)) ||
                        !Utils.isWater(world.getBlock(x + j + k, y, z - spaceNeededForShipHalf)) ||
                        !Utils.isWater(world.getBlock(x + j + k, y, z + spaceNeededForShipHalf)) ||
                        !Utils.isWater(world.getBlock(x + k * spaceNeededForShip, y, z + i - spaceNeededForShipHalf)) ||
                        !Utils.isWater(world.getBlock(x + k, y, z + i - spaceNeededForShipHalf)))
                    return false;
            }
            return true;
        }
        else
        {
            for(int i = 0; i < spaceNeededForShip; i++)
            {
                int j = k * i;
                if(!Utils.isWater(world.getBlock(x, y, z + j + k)) ||
                        !Utils.isWater(world.getBlock(x - spaceNeededForShipHalf + i, y, z + k * spaceNeededForShipHalf)) ||
                        !Utils.isWater(world.getBlock(x - spaceNeededForShipHalf, y, z + j + k)) ||
                        !Utils.isWater(world.getBlock(x + spaceNeededForShipHalf, y, z + j + k)) ||
                        !Utils.isWater(world.getBlock(x + i - spaceNeededForShipHalf, y, z + k * spaceNeededForShip)) ||
                        !Utils.isWater(world.getBlock(x + i - spaceNeededForShipHalf, y, z + k)))
                    return false;
            }
            return true;
        }
    }

    /**
     * Checks if the player already placed a supply chest
     *
     * @param player The player
     * @return boolean, returns true when player hasn't placed before, or when infinite placing is on.
     */
    boolean isFirstPlacing(EntityPlayer player)
    {
        if(Configurations.allowInfiniteSupplyChests) return true;
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
        PlayerProperties.get(entityPlayer).setHasPlacedSupplyChest(true);

        world.setBlock(x, y + 1, z, Blocks.chest);
        world.setBlockMetadataWithNotify(x, y + 1, z, chestFacing, 2);

        Schematic.loadAndPlaceSchematic(world, "test", x, y + 5, z);//TODO use correct schematic
        fillChest((TileEntityChest) world.getTileEntity(x, y + 1, z));
    }

    private void fillChest(TileEntityChest chest) {
        //TODO chest.setInventorySlotContents(slotID, ItemStack);

        switch(chest.getWorldObj().difficultySetting)
        {
            //The easier the difficulty, the more loot recieved
            case PEACEFUL:
                //TODO peacefull loot
            case EASY:
                //TODO easy loot
            case NORMAL:
                //TODO normal loot
            case HARD:
                //TODO hard loot
                break;
        }
    }
}
