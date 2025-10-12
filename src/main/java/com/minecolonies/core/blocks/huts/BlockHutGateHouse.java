package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.modules.ConnectionModuleWindow;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
    public ItemInteractionResult useItemOn(
        final ItemStack stack,
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
                return ItemInteractionResult.FAIL;
            }

            @Nullable final IBuildingView building = IColonyManager.getInstance().getBuildingView(worldIn.dimension(), pos);
            if (building != null && !building.getColony().getPermissions().hasPermission(player, Action.ACCESS_HUTS))
            {
                new ConnectionModuleWindow(Constants.MOD_ID + ":gui/layouthuts/layoutcolonyconnection.xml", building, true).open();
                return ItemInteractionResult.FAIL;
            }

            return super.useItemOn(stack, state, worldIn, pos, player, hand, ray);
        }
        else
        {
            final IColony colony = IColonyManager.getInstance().getIColony(worldIn, pos);
            if (colony != null)
            {
                if (!colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
                {
                    return ItemInteractionResult.FAIL;
                }
            }
        }
        return ItemInteractionResult.FAIL;
    }

}
