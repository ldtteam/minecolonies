package com.minecolonies.coremod.network.messages.client.colony;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.FieldStructureType;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.IField;
import com.minecolonies.api.network.IMessage;
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
public class ColonyViewRemoveFieldViewMessage implements IMessage
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
     * The position of the field.
     */
    private BlockPos position;

    /**
     * The type of the field.
     */
    private FieldStructureType type;

    /**
     * The buffer containing the serialized field class.
     */
    private FriendlyByteBuf fieldData;

    /**
     * Empty constructor used when registering the
     */
    public ColonyViewRemoveFieldViewMessage()
    {
        super();
    }

    /**
     * Creates a message to handle colony views.
     *
     * @param field field to add or update a view.
     */
    public ColonyViewRemoveFieldViewMessage(@NotNull final IField field)
    {
        super();
        this.colonyId = field.getColony().getID();
        this.position = field.getPosition();
        this.type = field.getType();
        this.dimension = field.getColony().getDimension();
        this.fieldData = new FriendlyByteBuf(Unpooled.buffer());
        field.serializeToView(fieldData);
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(position);
        buf.writeEnum(type);
        buf.writeUtf(dimension.location().toString());
        buf.writeBytes(fieldData);
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        colonyId = buf.readInt();
        position = buf.readBlockPos();
        type = buf.readEnum(FieldStructureType.class);
        dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(buf.readUtf(32767)));
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
        IColonyManager.getInstance().handleColonyRemoveFieldViewMessage(colonyId, position, type, fieldData, dimension);
    }
}
