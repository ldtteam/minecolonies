package com.minecolonies.core.network.messages.server.colony.building.fields;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.core.colony.fields.FarmField;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Message to change the farmer field plot size.
 */
public class FarmFieldPlotResizeMessage extends AbstractColonyServerMessage
{
    /**
     * The new radius of the field plot.
     */
    private int size;

    /**
     * The specified direction for the new radius.
     */
    private Direction direction;

    /**
     * The field position.
     */
    private BlockPos position;

    /**
     * Forge default constructor
     */
    public FarmFieldPlotResizeMessage()
    {
        super();
    }

    /**
     * @param size      the new radius of the field plot
     * @param direction the specified direction for the new radius
     * @param position  the field position.
     */
    public FarmFieldPlotResizeMessage(IColony colony, int size, Direction direction, BlockPos position)
    {
        super(colony);
        this.size = size;
        this.direction = direction;
        this.position = position;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        if (!isLogicalServer || ctxIn.getSender() == null)
        {
            return;
        }

        colony.getBuildingManager()
          .getField(f -> f.getFieldType().equals(FieldRegistries.farmField.get()) && f.getPosition().equals(position))
          .map(m -> (FarmField) m)
          .ifPresent(field -> field.setRadius(direction, size));
    }

    @Override
    public void toBytesOverride(final FriendlyByteBuf buf)
    {
        buf.writeInt(size);
        buf.writeInt(direction.get2DDataValue());
        buf.writeBlockPos(position);
    }

    @Override
    public void fromBytesOverride(final FriendlyByteBuf buf)
    {
        size = buf.readInt();
        direction = Direction.from2DDataValue(buf.readInt());
        position = buf.readBlockPos();
    }
}