package com.minecolonies.items;

import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.PlayerProperties;
import com.minecolonies.util.BlockUtils;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Log;
import com.minecolonies.util.Schematic;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
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
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if(worldIn == null || playerIn == null || worldIn.isRemote || stack.stackSize == 0 || !isFirstPlacing(playerIn))
            return false;

        EnumFacing facing = canShipBePlaced(worldIn, pos);
        if(facing != EnumFacing.DOWN)
        {
            spawnShip(worldIn, pos, playerIn, facing);
            stack.stackSize--;
            return true;
        }
        LanguageHandler.sendPlayerLocalizedMessage(playerIn, "item.supplyChestDeployer.invalid");
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
     * @param pos    coordinate clicked
     * @return      facings it can be placed at
     */
    public EnumFacing canShipBePlaced(World world, BlockPos pos)
    {
        if(check(world, pos.west(), true, false))
        {
            return EnumFacing.WEST;
        }
        else if(check(world, pos.east(), true, true))
        {
            return EnumFacing.EAST;
        }
        else if(check(world, pos.south(), false, true))
        {
            return EnumFacing.SOUTH;
        }
        else if(check(world, pos.north(), false, false))
        {
            return EnumFacing.NORTH;
        }
        return EnumFacing.DOWN;
    }

    /**
     * Checks the area for the ship to be placed.
     *
     * @param world                  world obj
     * @param pos                    coordinate clicked
     * @param shouldCheckX           boolean whether the x-sides should be checks
     * @param isCoordPositivelyAdded boolean whether the x or z side should be check on the positive side (true) or negative  side (false)
     * @return                       whether the space in the I shape is free or not
     */
    private boolean check(World world, BlockPos pos, boolean shouldCheckX, boolean isCoordPositivelyAdded)
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
                if(!BlockUtils.isWater(world.getBlockState(pos.add(j, 0, 0))) ||
                   !BlockUtils.isWater(world.getBlockState(pos.add(j, 0, spaceRightK))) ||
                   !BlockUtils.isWater(world.getBlockState(pos.add(j, 0, -spaceLeftK)))) return false;
            }
            for(int i = 0; i < LENGTH; i++)
            {
                if(!BlockUtils.isWater(world.getBlockState(pos.add(0, 0, -horizontalX + i))) ||
                   !BlockUtils.isWater(world.getBlockState(pos.add(widthKHalf, 0, -horizontalX + i))) ||
                   !BlockUtils.isWater(world.getBlockState(pos.add(widthK, 0, horizontalX + i)))) return false;
            }
        }
        else
        {
            for(int i = 0; i < WIDTH; i++)
            {
                int j = k * i;
                if(!BlockUtils.isWater(world.getBlockState(pos.add(0, 0, j))) ||
                   !BlockUtils.isWater(world.getBlockState(pos.add(-spaceRightK,0, j))) ||
                   !BlockUtils.isWater(world.getBlockState(pos.add(spaceLeftK, 0, j)))) return false;
            }

            for(int i = 0; i < LENGTH; i++)
            {
                if(!BlockUtils.isWater(world.getBlockState(pos.add(-horizontalZ + i, 0, 0))) ||
                   !BlockUtils.isWater(world.getBlockState(pos.add(-horizontalZ + i, 0, widthKHalf))) ||
                   !BlockUtils.isWater(world.getBlockState(pos.add(-horizontalZ + i, 0, widthK)))) return false;
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
     * @param pos          coordinate clicked
     * @param entityPlayer the player
     */
    private void spawnShip(World world, BlockPos pos, EntityPlayer entityPlayer, EnumFacing chestFacing)
    {
    	world.setBlockState(pos.up(), Blocks.chest.getDefaultState().withProperty(BlockChest.FACING, chestFacing));

        placeSupplyShip(world, pos, chestFacing);

        fillChest((TileEntityChest) world.getTileEntity(pos.up()));
        PlayerProperties.get(entityPlayer).placeSupplyChest();
    }

    private void placeSupplyShip(World world, BlockPos pos, EnumFacing direction)
    {
        switch(direction)
        {
            case SOUTH://North 2
                Schematic.loadAndPlaceSchematicWithRotation(world, "supplyShip", pos.add(-11, -2, 5), 3);
                break;
            case NORTH://South 3
                Schematic.loadAndPlaceSchematicWithRotation(world, "supplyShip", pos.add(-20, -2, -21), 1);
                break;
            case EAST://West 4
                Schematic.loadAndPlaceSchematicWithRotation(world, "supplyShip", pos.add(5, -2, -20), 2);
                break;
            case WEST://East 5
                Schematic.loadAndPlaceSchematicWithRotation(world, "supplyShip", pos.add(-21, -2, -11), 0);
                break;
            default:
            	break;
        }
    }

    private void fillChest(TileEntityChest chest)
    {
        if(chest == null)
        {
            Log.logger.error("Supply chest tile entity was null.");
            return;
        }
        chest.setInventorySlotContents(0, new ItemStack(ModBlocks.blockHutTownHall));
        chest.setInventorySlotContents(1, new ItemStack(ModItems.buildTool));
    }
}
