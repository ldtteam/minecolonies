package com.minecolonies.core.network.messages.client.colony;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.fields.registry.FieldDataManager;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Update message for auto syncing the entire field list.
 */
public class ColonyViewFieldsUpdateMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "colony_view_fields_update", ColonyViewFieldsUpdateMessage::new);

    /**
     * The colony this field belongs to.
     */
    private final int colonyId;

    /**
     * Dimension of the colony.
     */
    private final ResourceKey<Level> dimension;

    /**
     * The list of field items.
     */
    private final Map<IField, IField> fields;

    /**
     * Creates a message to handle colony all field views.
     *
     * @param colony the colony this field is in.
     * @param fields the complete list of fields of this colony.
     */
    public ColonyViewFieldsUpdateMessage(@NotNull final IColony colony, @NotNull final Set<IField> fields)
    {
        super(TYPE);
        this.colonyId = colony.getID();
        this.dimension = colony.getDimension();
        this.fields = new HashMap<>();
        fields.forEach(field -> this.fields.put(field, field));
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeUtf(dimension.location().toString());
        buf.writeInt(fields.size());
        for (final IField field : fields.keySet())
        {
            final FriendlyByteBuf fieldBuffer = FieldDataManager.fieldToBuffer(field);
            buf.writeInt(fieldBuffer.readableBytes());
            buf.writeByteArray(fieldBuffer.array());
        }
    }

    protected ColonyViewFieldsUpdateMessage(@NotNull final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        colonyId = buf.readInt();
        dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
        fields = new HashMap<>();
        final int fieldCount = buf.readInt();
        for (int i = 0; i < fieldCount; i++)
        {
            final int readableBytes = buf.readInt();
            final FriendlyByteBuf fieldData = new FriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray(readableBytes)));
            final IField parsedField = FieldDataManager.bufferToField(fieldData);
            fieldData.release();
            fields.put(parsedField, parsedField);
        }
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final Player player)
    {
        final IColonyView view = IColonyManager.getInstance().getColonyView(colonyId, dimension);
        if (view != null)
        {
            final Set<IField> updatedFields = new HashSet<>();
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