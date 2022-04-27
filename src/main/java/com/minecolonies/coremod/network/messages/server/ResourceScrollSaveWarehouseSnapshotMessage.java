package com.minecolonies.coremod.network.messages.server;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.items.ItemResourceScroll;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Message sent to the server when the client saves a new snapshot by clicking on a warehouse.
 */
public class ResourceScrollSaveWarehouseSnapshotMessage implements IMessage
{
    /**
     * The position of the builder.
     */
    @NotNull
    private BlockPos builderPos = BlockPos.ZERO;

    /**
     * The warehouse snapshot mapping.
     */
    @NotNull
    private Map<String, Integer> snapshot = new HashMap<>();

    /**
     * The hash of the current work order (if any).
     */
    @NotNull
    private String workOrderHash = "";

    /**
     * Empty constructor used when registering the message.
     */
    public ResourceScrollSaveWarehouseSnapshotMessage()
    {
        super();
    }

    /**
     * Empty constructor used when registering the message.
     */
    public ResourceScrollSaveWarehouseSnapshotMessage(@NotNull BlockPos builderPos, @NotNull Map<String, Integer> snapshot, @NotNull String workOrderHash)
    {
        super();
        this.builderPos = builderPos;
        this.snapshot = snapshot;
        this.workOrderHash = workOrderHash;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        builderPos = buf.readBlockPos();
        int numItems = buf.readInt();
        snapshot = new HashMap<>();
        for (int i = 0; i < numItems; i++)
        {
            String itemName = buf.readUtf(32767);
            int itemAmount = buf.readInt();
            snapshot.put(itemName, itemAmount);
        }
        workOrderHash = buf.readUtf(32767);
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeBlockPos(builderPos);
        buf.writeInt(snapshot.size());
        snapshot.forEach((key, value) -> {
            buf.writeUtf(key);
            buf.writeInt(value);
        });
        buf.writeUtf(workOrderHash);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        Objects.requireNonNull(ctxIn.getSender()).inventory.items.stream()
          .filter(stack -> stack.getItem() instanceof ItemResourceScroll)
          .filter(stack -> stack.getTag() != null)
          .filter(stack -> builderPos.equals(BlockPosUtil.read(stack.getTag(), TAG_BUILDER)))
          .forEach(stack -> {
              CompoundNBT data = stack.getTag();
              CompoundNBT newData = new CompoundNBT();
              snapshot.keySet().forEach(f -> newData.putInt(f, snapshot.getOrDefault(f, 0)));
              data.put(TAG_WAREHOUSE_SNAPSHOT, newData);
              data.putString(TAG_WAREHOUSE_SNAPSHOT_WO_HASH, workOrderHash);
          });
    }
}
