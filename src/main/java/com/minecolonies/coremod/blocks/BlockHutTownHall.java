package com.minecolonies.coremod.blocks;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
        //Sets the working range to whatever the config is set to
        this.workingRange = Configurations.workingRangeTownHall;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutTownHall";
    }

    @Override
    public void onBlockPlacedBy(
            @NotNull final World worldIn, @NotNull final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack stack)
    {
        if(worldIn.isRemote)
        {
            return;
        }

        if(placer.getActiveHand().equals(EnumHand.MAIN_HAND))
        {
            final Colony colony = ColonyManager.getClosestColony(worldIn, pos);

            if ((colony == null
                    || BlockPosUtil.getDistance2D(colony.getCenter(), pos) >= Configurations.workingRangeTownHall * 2 + Configurations.townHallPadding)
                    && placer instanceof EntityPlayer)
            {

                ColonyManager.createColony(worldIn, pos, (EntityPlayer) placer);
            }
        }
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }
}
