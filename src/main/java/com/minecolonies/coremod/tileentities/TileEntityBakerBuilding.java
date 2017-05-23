package com.minecolonies.coremod.tileentities;

import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.BuildingBaker;
import com.minecolonies.coremod.entity.ai.citizen.baker.Product;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Baker building special TileEntity.
 */
public class TileEntityBakerBuilding extends TileEntityColonyBuilding
{
    /**
     * Wait this amount of ticks before checking again.
     */
    private static final int WAIT_TICKS = 60;

    /**
     * Ticks past since the last check.
     */
    private int ticksPassed = 0;

    @Override
    public void update()
    {
        super.update();

        if(ticksPassed != WAIT_TICKS)
        {
            ticksPassed++;
            return;
        }
        ticksPassed = 0;

        final AbstractBuilding building = getBuilding();

        if(building instanceof BuildingBaker)
        {
            checkFurnaces((BuildingBaker) building);
        }
    }

    /**
     * Checks the furnaces of the baker if they're ready.
     * @param building the building they belong to.
     */
    private void checkFurnaces(@NotNull final BuildingBaker building)
    {
        for(final Map.Entry<BlockPos, Product> entry: building.getFurnacesWithProduct().entrySet())
        {
            final IBlockState furnace = worldObj.getBlockState(entry.getKey());
            if(!(furnace.getBlock() instanceof BlockFurnace))
            {
                building.removeFromFurnaces(entry.getKey());
                continue;
            }

            final Product product = entry.getValue();
            if(product != null && product.getState() == Product.ProductState.BAKING)
            {
                product.increaseBakingProgress();
                worldObj.setBlockState(entry.getKey(), Blocks.LIT_FURNACE.getDefaultState().withProperty(BlockFurnace.FACING, furnace.getValue(BlockFurnace.FACING)));
            }
            else
            {
                worldObj.setBlockState(entry.getKey(), Blocks.FURNACE.getDefaultState().withProperty(BlockFurnace.FACING, furnace.getValue(BlockFurnace.FACING)));
            }
        }
    }
}
