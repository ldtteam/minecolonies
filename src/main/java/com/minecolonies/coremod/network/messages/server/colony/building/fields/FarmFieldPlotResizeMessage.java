package com.minecolonies.coremod.network.messages.server.colony.building.fields;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.IFieldMatcher;
import com.minecolonies.api.colony.fields.registry.IFieldDataManager;
import com.minecolonies.coremod.colony.fields.FarmField;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
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
     * The field matcher.
     */
    private IFieldMatcher matcher;

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
     * @param matcher   the field matcher.
     */
    public FarmFieldPlotResizeMessage(IColony colony, int size, Direction direction, IFieldMatcher matcher)
    {
        super(colony);
        this.size = size;
        this.direction = direction;
        this.matcher = matcher;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        if (!isLogicalServer || ctxIn.getSender() == null)
        {
            return;
        }

        final IField existingField = colony.getBuildingManager().getField(matcher);
        if (existingField instanceof FarmField farmField)
        {
            farmField.setRadius(this.direction, this.size);
            colony.getBuildingManager().addOrUpdateField(farmField);
        }
    }

    @Override
    public void toBytesOverride(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.size);
        buf.writeInt(this.direction.get2DDataValue());
        this.matcher.toBytes(buf);
    }

    @Override
    public void fromBytesOverride(final FriendlyByteBuf buf)
    {
        this.size = buf.readInt();
        this.direction = Direction.from2DDataValue(buf.readInt());
        this.matcher = IFieldDataManager.getInstance().matcherFromBytes(buf);
    }
}