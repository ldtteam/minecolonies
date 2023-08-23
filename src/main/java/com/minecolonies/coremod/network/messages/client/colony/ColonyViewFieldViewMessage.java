package com.minecolonies.coremod.network.messages.client.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.Log;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Add or Update a IFieldView to a ColonyView on the client.
 */
public class ColonyViewFieldViewMessage implements IMessage
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
     * The type of the field.
     */
    private FieldRegistries.FieldEntry type;

    /**
     * The position of the field.
     */
    private BlockPos position;

    /**
     * The buffer containing the serialized field class.
     */
    private FriendlyByteBuf fieldData;

    /**
     * Empty constructor used when registering the
     */
    public ColonyViewFieldViewMessage()
    {
        super();
    }

    /**
     * Creates a message to handle colony views.
     *
     * @param colony the colony this field is in.
     * @param field  field to add or update a view.
     */
    public ColonyViewFieldViewMessage(@NotNull final IColony colony, @NotNull final IField field)
    {
        super();
        this.colonyId = colony.getID();
        this.dimension = colony.getDimension();
        this.type = field.getFieldType();
        this.position = field.getPosition();
        this.fieldData = new FriendlyByteBuf(Unpooled.buffer());
        field.serialize(fieldData);
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeUtf(dimension.location().toString());
        buf.writeRegistryId(FieldRegistries.getFieldRegistry(), type);
        buf.writeBlockPos(position);
        buf.writeBytes(fieldData);
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        colonyId = buf.readInt();
        dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(buf.readUtf(32767)));
        type = buf.readRegistryIdSafe(FieldRegistries.FieldEntry.class);
        position = buf.readBlockPos();
        fieldData = new FriendlyByteBuf(Unpooled.buffer(buf.readableBytes()));
        buf.readBytes(fieldData, buf.readableBytes());
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
            view.handleColonyFieldViewMessage(type, position, fieldData);
        }
        else
        {
            Log.getLogger().error("Colony view does not exist for ID #{}", colonyId);
        }
    }
}