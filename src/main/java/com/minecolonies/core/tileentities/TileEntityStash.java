package com.minecolonies.core.tileentities;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.MessageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

import static com.minecolonies.api.colony.requestsystem.requestable.deliveryman.AbstractDeliverymanRequestable.getPlayerActionPriority;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_DELIVERYMAN_FORCEPICKUP;

/**
 * Class which handles the tileEntity for the Stash block.
 */
public class TileEntityStash extends TileEntityColonyBuilding
{

    /**
     * Constructor of the stash based on a tile entity type
     *
     * @param type tile entity type
     */
    public TileEntityStash(final BlockEntityType<? extends TileEntityStash> type, final BlockPos pos, final BlockState state)
    {
        super(type, pos, state);
    }

    /**
     * Default constructor of the stash
     */
    public TileEntityStash(final BlockPos pos, final BlockState state)
    {
        super(MinecoloniesTileEntities.STASH.get(), pos, state);
    }

    @Override
    public ItemStackHandler createInventory(final int slots)
    {
        return new NotifyingRackInventory(slots);
    }

    /**
     * An {@link ItemStackHandler} that notifies the container TileEntity when it's inventory has changed.
     */
    public class NotifyingRackInventory extends RackInventory
    {
        public NotifyingRackInventory(final int defaultSize)
        {
            super(defaultSize);
        }

        @Override
        protected void onContentsChanged(final int slot)
        {
            super.onContentsChanged(slot);

            if (level != null && !level.isClientSide && IColonyManager.getInstance().isCoordinateInAnyColony(level, worldPosition))
            {
                final IColony colony = IColonyManager.getInstance().getClosestColony(level, worldPosition);
                if (colony != null)
                {
                    final IBuilding building = colony.getBuildingManager().getBuilding(worldPosition);
                    if (!isEmpty() && building != null)
                    {
                        MessageUtils.format(COM_MINECOLONIES_COREMOD_ENTITY_DELIVERYMAN_FORCEPICKUP).sendToClose(getTilePos(), 6, colony.getMessagePlayerEntities());
                        // Note that createPickupRequest will make sure to only create on request per building.
                        building.createPickupRequest(getPlayerActionPriority(true));
                    }
                }
            }
        }
    }
}
