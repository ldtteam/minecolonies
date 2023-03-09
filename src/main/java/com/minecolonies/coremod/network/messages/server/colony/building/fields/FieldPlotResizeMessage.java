package com.minecolonies.coremod.network.messages.server.colony.building.fields;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.FieldRecord;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.FieldType;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.IField;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.FarmField;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Message to change the farmer field plot size.
 */
public class FieldPlotResizeMessage implements IMessage
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
     * The field matcher.
     */
    private FieldRecord matcher;

    /**
     * Forge default constructor
     */
    public FieldPlotResizeMessage() {super();}

    /**
     * @param size      the new radius of the field plot
     * @param direction the specified direction for the new radius
     * @param matcher   the field matcher.
     */
    public FieldPlotResizeMessage(int size, Direction direction, FieldRecord matcher)
    {
        this.size = size;
        this.direction = direction;
        this.matcher = matcher;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.size);
        buf.writeInt(this.direction.get2DDataValue());
        this.matcher.toBytes(buf);
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        this.size = buf.readInt();
        this.direction = Direction.from2DDataValue(buf.readInt());
        this.matcher = FieldRecord.fromBytes(buf);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        if (isLogicalServer && ctxIn.getSender() != null)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(ctxIn.getSender().level, matcher.position());
            if (colony != null)
            {
                final IField existingField = colony.getBuildingManager().getField(FieldType.FARMER_FIELDS, matcher);
                if (existingField instanceof FarmField farmField)
                {
                    farmField.setRadius(this.direction, this.size);
                    colony.getBuildingManager().addOrUpdateField(farmField);
                }
            }
        }
    }
}