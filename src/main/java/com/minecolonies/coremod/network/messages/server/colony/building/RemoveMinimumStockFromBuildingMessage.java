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
public class RemoveMinimumStockFromBuildingMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private ItemStack itemStack;

    /**
     * Empty constructor used when registering the
     */
    public RemoveMinimumStockFromBuildingMessage()
    {
        super();
    }

    /**
     * Creates a Transfer Items request
     *
     * @param building  the building we're executing on.
     * @param itemStack to be take from the player for the building
     */
    public RemoveMinimumStockFromBuildingMessage(final IBuildingView building, final ItemStack itemStack)
    {
        super(building);
        this.itemStack = itemStack;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        itemStack = buf.readItemStack();
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        buf.writeItemStack(itemStack);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        building.removeMinimumStock(itemStack);
    }
}
