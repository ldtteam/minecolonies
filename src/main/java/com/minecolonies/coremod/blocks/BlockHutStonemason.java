package com.minecolonies.coremod.blocks;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.coremod.client.gui.WindowGuiCrafting;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Hut for the stone mason.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutStonemason extends AbstractBlockHut<BlockHutStonemason>
{
    protected BlockHutStonemason()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutStonemason";
    }

    @Override
    public boolean onBlockActivated(
            final World worldIn,
            final BlockPos pos,
            final IBlockState state,
            final EntityPlayer playerIn,
            final EnumHand hand,
            final EnumFacing facing,
            final float hitX,
            final float hitY,
            final float hitZ)
    {
        /*
        If the world is client, open the gui of the building
         */
        if (worldIn.isRemote)
        {
            @Nullable final AbstractBuildingView building = ColonyManager.getBuildingView(pos);
            if (building != null
                    && building.getColony() != null
                    && building.getColony().getPermissions().hasPermission(playerIn, Action.ACCESS_HUTS))
            {
                Minecraft.getMinecraft().displayGuiScreen(new WindowGuiCrafting(playerIn.inventory, worldIn, building));
            }
        }
        return true;
    }
}
