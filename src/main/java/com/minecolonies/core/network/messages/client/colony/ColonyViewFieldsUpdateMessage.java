package com.minecolonies.core.network.messages.client.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.colony.fields.registry.FieldDataManager;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    private Map<IField, IField> fields;

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
        this.fields = new HashMap<>();
        fields.forEach(field -> this.fields.put(field, field));
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeUtf(dimension.location().toString());
        buf.writeInt(fields.size());
        for (IField field : fields.keySet())
        {
            FriendlyByteBuf fieldBuffer = FieldDataManager.fieldToBuffer(field);
            buf.writeInt(fieldBuffer.readableBytes());
            buf.writeBytes(fieldBuffer);
        }
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        colonyId = buf.readInt();
        dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
        fields = new HashMap<>();
        int fieldCount = buf.readInt();
        for (int i = 0; i < fieldCount; i++)
        {
            int readableBytes = buf.readInt();
            FriendlyByteBuf fieldData = new FriendlyByteBuf(Unpooled.buffer(readableBytes));
            buf.readBytes(fieldData, readableBytes);
            IField parsedField = FieldDataManager.bufferToField(fieldData);
            fields.put(parsedField, parsedField);
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
            Set<IField> updatedFields = new HashSet<>();
            view.getFields(field -> true).forEach(existingField -> {
                if (this.fields.containsKey(existingField))
                {
                    final FriendlyByteBuf copyBuffer = new FriendlyByteBuf(Unpooled.buffer());
                    this.fields.get(existingField).serialize(copyBuffer);
                    existingField.deserialize(copyBuffer);
                    updatedFields.add(existingField);
                }
            });
            updatedFields.addAll(this.fields.keySet());

            view.handleColonyFieldViewUpdateMessage(updatedFields);
        }
        else
        {
            Log.getLogger().error("Colony view does not exist for ID #{}", colonyId);
        }
    }
}