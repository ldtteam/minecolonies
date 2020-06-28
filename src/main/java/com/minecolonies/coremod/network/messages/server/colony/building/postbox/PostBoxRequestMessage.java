package com.minecolonies.coremod.network.messages.server.colony.building.postbox;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.PostBox;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Creates a request from the postbox.
 */
public class PostBoxRequestMessage extends AbstractBuildingServerMessage<PostBox>
{
    /**
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private ItemStack itemStack;

    /**
     * Empty constructor used when registering the
     */
    public PostBoxRequestMessage()
    {
        super();
    }

    /**
     * Creates a Transfer Items request
     *
     * @param building  AbstractBuilding of the request.
     * @param itemStack to be take from the player for the building
     * @param quantity  of item needed to be transfered
     */
    public PostBoxRequestMessage(@NotNull final AbstractBuildingView building, final ItemStack itemStack, final int quantity)
    {
        super(building);
        this.itemStack = itemStack;
        this.itemStack.setCount(quantity);
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
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final PostBox building)
    {
        building.createRequest(new Stack(itemStack), false);
    }
}
