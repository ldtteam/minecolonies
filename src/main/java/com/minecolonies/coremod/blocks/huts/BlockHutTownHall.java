package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.client.gui.WindowTownHallColonyManage;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    /**
     * Choose a different gui when no colony view, for colony overview and creation/deletion
     *
     * @param state
     * @param worldIn
     * @param pos
     * @param player
     * @param hand
     * @param ray
     * @return
     */
    @Override
    public ActionResultType onBlockActivated(
      final BlockState state,
      final World worldIn,
      final BlockPos pos,
      final PlayerEntity player,
      final Hand hand,
      final BlockRayTraceResult ray)
    {
       /*
        If the world is client, open the gui of the building
         */
        if (worldIn.isRemote)
        {
            @Nullable final IBuildingView building = IColonyManager.getInstance().getBuildingView(worldIn.getDimension().getType().getId(), pos);

            if (building != null
                  && building.getColony() != null
                  && building.getColony().getPermissions().hasPermission(player, Action.ACCESS_HUTS))
            {
                building.openGui(player.isShiftKeyDown());
            }
            else
            {
                new WindowTownHallColonyManage(player, pos, worldIn).open();
            }
        }
        return ActionResultType.SUCCESS;
    }

    public static boolean canCreateColonyHere(World world, BlockPos pos, PlayerEntity placer)
    {


        return true;
    }



}
