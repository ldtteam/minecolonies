package com.minecolonies.core.network.messages.server.colony.building.postbox;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.core.colony.buildings.workerbuildings.PostBox;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Creates a request from the postbox.
 */
public class PostBoxRequestMessage extends AbstractBuildingServerMessage<PostBox>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "post_box_request", PostBoxRequestMessage::new);

    /**
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private final ItemStack itemStack;

    /*
     * Whether to deliver what's currently available or entire request
     */
    private final boolean deliverAvailable;

    private final int reqQuantity;

    /**
     * Creates a Transfer Items request
     *
     * @param building  AbstractBuilding of the request.
     * @param itemStack to be take from the player for the building
     * @param quantity  of item needed to be transfered
     */
    public PostBoxRequestMessage(@NotNull final AbstractBuildingView building, final ItemStack itemStack, final int quantity, final boolean deliverAvailable)
    {
        super(TYPE, building);
        this.itemStack = itemStack;
        reqQuantity = quantity;
        this.deliverAvailable = deliverAvailable;
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);

        buf.writeItem(itemStack);
        buf.writeBoolean(deliverAvailable);
        buf.writeInt(reqQuantity);
    }

    protected PostBoxRequestMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);

        itemStack = buf.readItem();
        deliverAvailable = buf.readBoolean();
        reqQuantity = buf.readInt();
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final PostBox building)
    {

        final int minCount = (deliverAvailable) ? 1 : reqQuantity;
        final Stack requestStack = new Stack(itemStack, reqQuantity, minCount);

        building.createRequest(requestStack, false);
    }
}
