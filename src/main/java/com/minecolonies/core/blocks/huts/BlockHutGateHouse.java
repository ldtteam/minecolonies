package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.core.client.gui.modules.building.ConnectionModuleWindow;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Block of the gate house hut.
 */
public class BlockHutGateHouse extends AbstractBlockHut<BlockHutGateHouse>
{
    /**
     * Default constructor.
     */
    public BlockHutGateHouse()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutgatehouse";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.gateHouse.get();
    }

    @Override
    public boolean canRightClickWithoutPermissions()
    {
        return true;
    }

    @NotNull
    @Override
    public InteractionResult use(
        final BlockState state,
        final Level worldIn,
        final BlockPos pos,
        final Player player,
        final InteractionHand hand,
        final BlockHitResult ray)
    {
       /*
        If the world is client, open the gui of the building
         */
        if (worldIn.isClientSide)
        {
            if (hand == InteractionHand.OFF_HAND)
            {
                return InteractionResult.FAIL;
            }

            @Nullable final IBuildingView building = IColonyManager.getInstance().getBuildingView(worldIn.dimension(), pos);
            if (building != null && !building.getColony().getPermissions().hasPermission(player, Action.ACCESS_HUTS))
            {
                new ConnectionModuleWindow(building, true).open();
                return InteractionResult.FAIL;
            }

            return super.use(state, worldIn, pos, player, hand, ray);
        }
        else
        {
            final IColony colony = IColonyManager.getInstance().getIColony(worldIn, pos);
            if (colony != null)
            {
                if (!colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
                {
                    return InteractionResult.FAIL;
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

}
