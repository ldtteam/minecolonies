package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.wrapper.InvWrapper;

import static com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBarracks.SPIES_GOLD_COST;

/**
 * Message for hiring spies at the cost of gold.
 */
public class HireSpiesMessage extends AbstractColonyServerMessage
{
    public HireSpiesMessage()
    {
    }

    public HireSpiesMessage(final IColony colony)
    {
        super(colony);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        final PlayerEntity player = ctxIn.getSender();
        if (player == null) return;

        if (InventoryUtils.getItemCountInItemHandler(new InvWrapper(player.inventory), stack -> stack.getItem() == Items.GOLD_INGOT) > SPIES_GOLD_COST)
        {
            InventoryUtils.reduceStackInItemHandler(new InvWrapper(player.inventory), new ItemStack(Items.GOLD_INGOT), SPIES_GOLD_COST);
            colony.getRaiderManager().setSpiesEnabled(true);
            colony.markDirty();
        }
    }

    @Override
    protected void toBytesOverride(final PacketBuffer buf)
    {


    }

    @Override
    protected void fromBytesOverride(final PacketBuffer buf)
    {

    }
}
