package com.minecolonies.items;

import com.minecolonies.MineColonies;
import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.PlayerProperties;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Schematic;
import com.minecolonies.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;

public class ItemSupplyChestDeployer extends ItemMinecolonies
{
    public ItemSupplyChestDeployer()
    {
        super();
        setMaxStackSize(1);
    }

    @Override
    public String getName()
    {
        return "supplyChestDeployer";
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int face, float px, float py, float pz)
    {
        if(world == null || player == null || world.isRemote || stack.stackSize == 0 || !isFirstPlacing(player))
            return false;

        int facing = canShipBePlaced(world, x, y, z);
        if(facing != 0)
        {
            spawnShip(world, x, y, z, player, facing);
            stack.stackSize--;
            return true;
        }
        LanguageHandler.sendPlayerLocalizedMessage(player, "item.supplyChestDeployer.invalid");
        return false;
    }

    /**
     * Checks if the ship can be placed and returns the direction it can face.
     * <p/>
     * 0: cannot be placed
     * 2: can be placed at north
     * 3: can be placed at south
     * 4: can be placed at west
     * 5: can be placed at east
     *
     * @param world world obj
     * @param x     x coordinate clicked
     * @param y     y coordinate clicked
     * @param z     z coordinate clicked
     * @return      facings it can be placed at (2-5)
     */
    public int canShipBePlaced(World world, int x, int y, int z)
    {
        if(check(world, x + 1, y, z, true, true))
        {
            return 4;
        }
        else if(check(world, x - 1, y, z, true, false))
        {
            return 5;
        }
        else if(check(world, x, y, z - 1, false, false))
        {
            return 3;
        }
        else if(check(world, x, y, z + 1, false, true))
        {
            return 2;
        }
        return 0;
    }

    /**
     * Checks the area for the ship to be placed.
     *
     * @param world                  world obj
     * @param x                      x coordinate clicked
     * @param y                      y coordinate clicked
     * @param z                      z coordinate clicked
     * @param shouldCheckX           boolean whether the x-sides should be checks
     * @param isCoordPositivelyAdded boolean whether the x or z side should be check on the positive side (true) or negative  side (false)
     * @return                       whether the space in the I shape is free or not
     */
    private boolean check(World world, int x, int y, int z, boolean shouldCheckX, boolean isCoordPositivelyAdded)
    {
        int k = isCoordPositivelyAdded ? 1 : -1;

        final int SPACE_RIGHT = 11;
        final int SPACE_LEFT = 20;
        final int LENGTH = 32;
        final int WIDTH = 20;

        int horizontalX = isCoordPositivelyAdded ? SPACE_LEFT : SPACE_RIGHT;
        int horizontalZ = isCoordPositivelyAdded ? SPACE_RIGHT : SPACE_LEFT;

        int spaceRightK = SPACE_RIGHT * k;
        int spaceLeftK = SPACE_LEFT * k;

        int widthK = WIDTH * k;
        int widthKHalf = widthK / 2;

        if(shouldCheckX)
        {
            for(int i = 0; i < WIDTH; i++)
            {
                int j = k * i;
                if(!Utils.isWater(world.getBlock(x + j, y, z)) ||
                        !Utils.isWater(world.getBlock(x + j, y, z + spaceRightK)) ||
                        !Utils.isWater(world.getBlock(x + j, y, z - spaceLeftK))) return false;
            }
            for(int i = 0; i < LENGTH; i++)
            {
                if(!Utils.isWater(world.getBlock(x, y, z - horizontalX + i)) ||
                        !Utils.isWater(world.getBlock(x + widthKHalf, y, z - horizontalX + i)) ||
                        !Utils.isWater(world.getBlock(x + widthK, y, z - horizontalX + i))) return false;
            }
        }
        else
        {
            for(int i = 0; i < WIDTH; i++)
            {
                int j = k * i;
                if(!Utils.isWater(world.getBlock(x, y, z + j)) ||
                        !Utils.isWater(world.getBlock(x - spaceRightK, y, z + j)) ||
                        !Utils.isWater(world.getBlock(x + spaceLeftK, y, z + j))) return false;
            }

            for(int i = 0; i < LENGTH; i++)
            {
                if(!Utils.isWater(world.getBlock(x - horizontalZ + i, y, z)) ||
                        !Utils.isWater(world.getBlock(x - horizontalZ + i, y, z + widthKHalf)) ||
                        !Utils.isWater(world.getBlock(x - horizontalZ + i, y, z + widthK))) return false;
            }
        }
        return true;
    }

    /**
     * Checks if the player already placed a supply chest
     *
     * @param player    The player
     * @return          boolean, returns true when player hasn't placed before, or when infinite placing is on.
     */
    boolean isFirstPlacing(EntityPlayer player)
    {
        if(Configurations.allowInfiniteSupplyChests || !PlayerProperties.get(player).getHasPlacedSupplyChest())
            return true;
        LanguageHandler.sendPlayerLocalizedMessage(player, "com.minecolonies.error.supplyChestAlreadyPlaced");
        return false;
    }

    /**
     * Spawns the ship and supply chest
     *
     * @param world        world obj
     * @param x            x coordinate clicked
     * @param y            y coordinate clicked
     * @param z            z coordinate clicked
     * @param entityPlayer the player
     */
    private void spawnShip(World world, int x, int y, int z, EntityPlayer entityPlayer, int chestFacing)
    {
        world.setBlock(x, y + 1, z, Blocks.chest);
        world.setBlockMetadataWithNotify(x, y + 1, z, chestFacing, 2);

        placeSupplyShip(world, x, y, z, chestFacing);

        fillChest((TileEntityChest) world.getTileEntity(x, y + 1, z));
        PlayerProperties.get(entityPlayer).placeSupplyChest();
    }

    private void placeSupplyShip(World world, int x, int y, int z, int direction)
    {
        switch(direction)
        {
            case 2://North
                Schematic.loadAndPlaceSchematicWithRotation(world, "supplyShip", x - 11, y - 2, z + 5, 3);
                break;
            case 3://South
                Schematic.loadAndPlaceSchematicWithRotation(world, "supplyShip", x - 20, y - 2, z - 21, 1);
                break;
            case 4://West
                Schematic.loadAndPlaceSchematicWithRotation(world, "supplyShip", x + 5, y - 2, z - 20, 2);
                break;
            case 5://East
                Schematic.loadAndPlaceSchematicWithRotation(world, "supplyShip", x - 21, y - 2, z - 11, 0);
                break;
        }
    }

    private void fillChest(TileEntityChest chest)
    {
        if(chest == null)
        {
            MineColonies.logger.error("Supply chest tile entity was null.");
            return;
        }
        chest.setInventorySlotContents(0, new ItemStack(ModBlocks.blockHutTownHall));
        chest.setInventorySlotContents(1, new ItemStack(ModItems.buildTool));
    }
}
