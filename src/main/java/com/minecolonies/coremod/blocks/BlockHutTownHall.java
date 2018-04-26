package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the town hall.
 * Sets the working range for the town hall in the constructor
 */
public class BlockHutTownHall extends AbstractBlockHut<BlockHutTownHall>
{
    protected BlockHutTownHall()
    {
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutTownHall";
    }

    @Override
    public void onBlockPlacedBy(@NotNull final World worldIn, @NotNull final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        if (worldIn.isRemote)
        {
            return;
        }

        if (placer.getActiveHand().equals(EnumHand.MAIN_HAND))
        {
            final Colony colony = ColonyManager.getClosestColony(worldIn, pos);
            String style = Constants.DEFAULT_STYLE;
            final TileEntity tileEntity = worldIn.getTileEntity(pos);
            if(tileEntity instanceof TileEntityColonyBuilding
                    && !((TileEntityColonyBuilding) tileEntity).getStyle().isEmpty())
            {
                style = ((TileEntityColonyBuilding) tileEntity).getStyle();
            }

            if (colony == null || !ColonyManager.isTooCloseToColony(worldIn, pos))
            {
                ColonyManager.createColony(worldIn, pos, (EntityPlayer) placer, style);
            }
            else
            {
                colony.setStyle(style);
            }
        }
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }
}
