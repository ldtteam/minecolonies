package com.minecolonies.core.network.messages.server.colony.building.fields;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.fields.FarmField;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * Message to change the farmer field plot size.
 */
public class FarmFieldPlotResizeMessage extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "farm_field_plot_resize", FarmFieldPlotResizeMessage::new);

    /**
     * The new radius of the field plot.
     */
    private final int size;

    /**
     * The specified direction for the new radius.
     */
    private final Direction direction;

    /**
     * The field position.
     */
    private final BlockPos position;

    /**
     * @param size      the new radius of the field plot
     * @param direction the specified direction for the new radius
     * @param position  the field position.
     */
    public FarmFieldPlotResizeMessage(final IColony colony, final int size, final Direction direction, final BlockPos position)
    {
        super(TYPE, colony);
        this.size = size;
        this.direction = direction;
        this.position = position;
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
        colony.getBuildingManager()
          .getField(f -> f.getFieldType().equals(FieldRegistries.farmField.get()) && f.getPosition().equals(position))
          .map(m -> (FarmField) m)
          .ifPresent(field -> field.setRadius(direction, size));
    }

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(size);
        buf.writeInt(direction.get2DDataValue());
        buf.writeBlockPos(position);
    }

    protected FarmFieldPlotResizeMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        size = buf.readInt();
        direction = Direction.from2DDataValue(buf.readInt());
        position = buf.readBlockPos();
    }
}