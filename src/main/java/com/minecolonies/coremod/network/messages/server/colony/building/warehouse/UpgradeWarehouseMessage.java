package com.minecolonies.coremod.network.messages.server.colony.building.warehouse;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.wrapper.InvWrapper;

/**
 * Issues the upgrade of the warehouse pos level 5.
 */
public class UpgradeWarehouseMessage extends AbstractBuildingServerMessage<BuildingWareHouse>
{
    /**
     * Empty constructor used when registering the 
     */
    public UpgradeWarehouseMessage()
    {
        super();
    }

    @Override
    protected void toBytesOverride(final PacketBuffer buf)
    {

    }

    @Override
    protected void fromBytesOverride(final PacketBuffer buf)
    {

    }

    public UpgradeWarehouseMessage(final IBuildingView building)
    {
        super(building);
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingWareHouse building)
    {
        final PlayerEntity player = ctxIn.getSender();
        if (player == null) return;

        building.upgradeContainers(player.world);

        final boolean isCreative = player.isCreative();
        if (!isCreative)
        {
            final int slot = InventoryUtils.
                                             findFirstSlotInItemHandlerWith(new InvWrapper(player.inventory),
                                               itemStack -> itemStack.isItemEqual(new ItemStack(Blocks.EMERALD_BLOCK)));
            player.inventory.decrStackSize(slot, 1);
        }
    }
}
