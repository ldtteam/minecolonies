package com.minecolonies.core.network.messages.client.colony;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.colony.workorders.view.AbstractWorkOrderView;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewWorkOrderMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "colony_view_workorder", ColonyViewWorkOrderMessage::new);

    private final int                colonyId;
    private final ResourceKey<Level> dimension;
    private final RegistryFriendlyByteBuf       workOrderBuffer;

    /**
     * Updates a {@link AbstractWorkOrderView} of the workOrders.
     *
     * @param colony        colony of the workOrder.
     * @param workOrderList list of workorders to send to the client
     */
    public ColonyViewWorkOrderMessage(@NotNull final Colony colony, @NotNull final List<IWorkOrder> workOrderList)
    {
        super(TYPE);
        this.colonyId = colony.getID();
        this.workOrderBuffer = new RegistryFriendlyByteBuf(new FriendlyByteBuf(Unpooled.buffer()), colony.getWorld().registryAccess());
        this.dimension = colony.getDimension();

        workOrderBuffer.writeInt(workOrderList.size());
        for (final IWorkOrder workOrder : workOrderList)
        {
            workOrder.serializeViewNetworkData(workOrderBuffer);
        }
    }

    public ColonyViewWorkOrderMessage(@NotNull final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        colonyId = buf.readInt();
        dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(buf.readUtf(32767)));
        workOrderBuffer = new RegistryFriendlyByteBuf(new FriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray())), buf.registryAccess());
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        workOrderBuffer.resetReaderIndex();
        buf.writeInt(colonyId);
        buf.writeUtf(dimension.location().toString());
        buf.writeByteArray(workOrderBuffer.array());
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final Player player)
    {
        IColonyManager.getInstance().handleColonyViewWorkOrderMessage(colonyId, workOrderBuffer, dimension);
    }
}


