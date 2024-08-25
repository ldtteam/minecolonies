package com.minecolonies.core.network.messages.server;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.items.component.Pos;
import com.minecolonies.api.items.component.WarehouseSnapshot;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.items.ItemResourceScroll;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

/**
 * Message sent to the server when the client saves a new snapshot by clicking on a warehouse.
 */
public class ResourceScrollSaveWarehouseSnapshotMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "resource_scroll_save_warehouse_snapshot", ResourceScrollSaveWarehouseSnapshotMessage::new);

    /**
     * The position of the builder.
     */
    private final BlockPos builderPos;

    /**
     * The warehouse snapshot mapping.
     */
    @NotNull
    private final Map<String, Integer> snapshot;

    /**
     * The hash of the current work order (if any).
     */
    @NotNull
    private final String workOrderHash;

    /**
     * Empty constructor used when registering the message.
     */
    public ResourceScrollSaveWarehouseSnapshotMessage(final BlockPos builderPos)
    {
        this(builderPos, Map.of(), "");
    }

    /**
     * Empty constructor used when registering the message.
     */
    public ResourceScrollSaveWarehouseSnapshotMessage(final BlockPos builderPos, @NotNull final Map<String, Integer> snapshot, @NotNull final String workOrderHash)
    {
        super(TYPE);
        this.builderPos = builderPos;
        this.snapshot = snapshot;
        this.workOrderHash = workOrderHash;
    }

    protected ResourceScrollSaveWarehouseSnapshotMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        builderPos = buf.readBoolean() ? buf.readBlockPos() : null;
        snapshot = buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readInt);
        workOrderHash = buf.readUtf(32767);
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        buf.writeBoolean(builderPos != null);
        if (builderPos != null)
        {
            buf.writeBlockPos(builderPos);
        }
        buf.writeMap(snapshot, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeInt);
        buf.writeUtf(workOrderHash);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player)
    {
        player.getInventory().items.stream()
          .filter(stack -> stack.getItem() instanceof ItemResourceScroll)
          .filter(stack -> Objects.equals(builderPos, Pos.readFromItemStack(stack).pos()))
          .forEach(stack -> new WarehouseSnapshot(snapshot, workOrderHash).writeToItemStack(stack));
    }
}
