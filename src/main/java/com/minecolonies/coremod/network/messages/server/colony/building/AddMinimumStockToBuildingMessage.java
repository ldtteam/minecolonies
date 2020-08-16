package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Set a new block to the minimum stock list.
 */
public class AddMinimumStockToBuildingMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private final ItemStack itemStack;

    /**
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private final int quantity;

    /**
     * Empty constructor used when registering the
     */
    public AddMinimumStockToBuildingMessage(final PacketBuffer buf)
    {
        super(buf);
        this.itemStack = buf.readItemStack();
        this.quantity = buf.readInt();
    }

    /**
     * Creates a Transfer Items request
     *
     * @param itemStack to be take from the player for the building
     * @param quantity  of item needed to be transfered
     * @param building  the building we're executing on.
     */
    public AddMinimumStockToBuildingMessage(final IBuildingView building, final ItemStack itemStack, final int quantity)
    {
        super(building);
        this.itemStack = itemStack;
        this.quantity = quantity;
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeItemStack(itemStack);
        buf.writeInt(quantity);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        building.addMinimumStock(itemStack, quantity);
    }
}
