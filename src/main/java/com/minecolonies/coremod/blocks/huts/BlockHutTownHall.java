package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.constant.Constants;
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
    /**
     * Hardness for townhall block in pvp mode.
     */
    public static final float PVP_MODE_HARDNESS = 200F;

    public BlockHutTownHall()
    {
        super();
        if (Configurations.gameplay.pvp_mode)
        {
            setHardness(PVP_MODE_HARDNESS);
        }
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutTownHall";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.townHall;
    }

    @Override
    public void onBlockPlacedBy(@NotNull final World worldIn, @NotNull final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        if (worldIn.isRemote)
        {
            return;
        }

        if (placer.getActiveHand().equals(EnumHand.MAIN_HAND) && placer instanceof EntityPlayer)
        {
            final IColony colony = IColonyManager.getInstance().getClosestColony(worldIn, pos);
            String style = Constants.DEFAULT_STYLE;
            final TileEntity tileEntity = worldIn.getTileEntity(pos);
            if(tileEntity instanceof TileEntityColonyBuilding
                 && !((AbstractTileEntityColonyBuilding) tileEntity).getStyle().isEmpty())
            {
                style = ((AbstractTileEntityColonyBuilding) tileEntity).getStyle();
            }

            if (colony == null || !IColonyManager.getInstance().isTooCloseToColony(worldIn, pos))
            {
                if (Configurations.gameplay.enableDynamicColonySizes)
                {
                    IColony ownedColony = IColonyManager.getInstance().getIColonyByOwner(worldIn, (EntityPlayer) placer);

                    if (ownedColony == null)
                    {
                        IColonyManager.getInstance().createColony(worldIn, pos, (EntityPlayer) placer, style);
                    }
                    else
                    {
                        colony.getBuildingManager().addNewBuilding((TileEntityColonyBuilding) tileEntity, worldIn);
                    }
                }
                else
                {
                    IColonyManager.getInstance().createColony(worldIn, pos, (EntityPlayer) placer, style);
                }
            }
            else
            {
                colony.setStyle(style);
                colony.getBuildingManager().addNewBuilding((TileEntityColonyBuilding) tileEntity, worldIn);
            }
        }
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }
}
