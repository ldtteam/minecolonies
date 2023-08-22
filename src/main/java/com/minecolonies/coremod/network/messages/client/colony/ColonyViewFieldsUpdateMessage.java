package com.minecolonies.coremod.network.messages.client.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.fields.registry.FieldDataManager;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Update message for auto syncing the entire field list.
 */
public class ColonyViewFieldsUpdateMessage implements IMessage
{
    /**
     * The colony this field belongs to.
     */
    private int colonyId;

    /**
     * Dimension of the colony.
     */
    private ResourceKey<Level> dimension;

    /**
     * The list of field items.
     */
    private Set<IField> fields;

    /**
     * Empty constructor used when registering the
     */
    public ColonyViewFieldsUpdateMessage()
    {
        super();
    }

    /**
     * Creates a message to handle colony all field views.
     *
     * @param colony the colony this field is in.
     * @param fields the complete list of fields of this colony.
     */
    public ColonyViewFieldsUpdateMessage(@NotNull final IColony colony, @NotNull final Set<IField> fields)
    {
        super();
        this.colonyId = colony.getID();
        this.dimension = colony.getDimension();
        this.fields = fields;
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeUtf(dimension.location().toString());
        buf.writeInt(fields.size());
        for (IField field : fields)
        {
            final FriendlyByteBuf fieldBuffer = FieldDataManager.fieldToBuffer(field);
            buf.writeInt(fieldBuffer.readableBytes());
            buf.writeBytes(fieldBuffer);
        }
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        colonyId = buf.readInt();
        dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
        fields = new HashSet<>();
        int fieldCount = buf.readInt();
        for (int i = 0; i < fieldCount; i++)
        {
            int readableBytes = buf.readInt();
            FriendlyByteBuf fieldData = new FriendlyByteBuf(Unpooled.buffer(readableBytes));
            buf.readBytes(fieldData, readableBytes);
            fields.add(FieldDataManager.bufferToField(fieldData));
        }
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final IColonyView view = IColonyManager.getInstance().getColonyView(colonyId, dimension);
        if (view != null)
        {
            view.handleColonyFieldViewUpdateMessage(fields);
        }
        else
        {
            Log.getLogger().error("Colony view does not exist for ID #{}", colonyId);
        }
    }
}