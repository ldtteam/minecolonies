package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
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
        super(Properties.create(Material.WOOD).hardnessAndResistance(HARDNESS, RESISTANCE));
    }

    @Override
    public float getBlockHardness(final BlockState blockState, final IBlockReader worldIn, final BlockPos pos)
    {
        return MineColonies.getConfig().getCommon().pvp_mode.get() ? PVP_MODE_HARDNESS : HARDNESS;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockhuttownhall";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.townHall;
    }

    @Override
    public void onBlockPlacedBy(
      @NotNull final World worldIn, @NotNull final BlockPos pos, final BlockState state, final LivingEntity placer, final ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        if (worldIn.isRemote)
        {
            return;
        }

        if (placer.getActiveHand().equals(Hand.MAIN_HAND) && placer instanceof PlayerEntity)
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
                if (MineColonies.getConfig().getCommon().enableDynamicColonySizes.get())
                {
                    final IColony ownedColony = IColonyManager.getInstance().getIColonyByOwner(worldIn, (PlayerEntity) placer);

                    if (ownedColony == null)
                    {
                        IColonyManager.getInstance().createColony(worldIn, pos, (PlayerEntity) placer, style);
                    }
                    else
                    {
                        colony.getBuildingManager().addNewBuilding((TileEntityColonyBuilding) tileEntity, worldIn);
                    }
                }
                else
                {
                    IColonyManager.getInstance().createColony(worldIn, pos, (PlayerEntity) placer, style);
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
