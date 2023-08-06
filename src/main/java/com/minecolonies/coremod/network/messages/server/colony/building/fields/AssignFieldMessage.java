package com.minecolonies.coremod.network.messages.server.colony.building.fields;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.coremod.colony.buildings.modules.FieldsModule;
import com.minecolonies.coremod.colony.fields.registry.FieldDataManager;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message which handles the assignment of fields to farmers.
 */
public class AssignFieldMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The field to (un)assign.
     */
    private FriendlyByteBuf fieldData;

    /**
     * Whether to assign or un-assign this field.
     */
    private boolean assign;

    /**
     * Empty standard constructor.
     */
    public AssignFieldMessage()
    {
        super();
    }

    /**
     * Creates the message to assign a field.
     *
     * @param assign   assign if true, free if false.
     * @param field    the field.
     * @param building the building we're executing on.
     */
    public AssignFieldMessage(final IBuildingView building, final IField field, final boolean assign)
    {
        super(building);
        this.assign = assign;
        this.fieldData = FieldDataManager.fieldToBuffer(field);
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBoolean(assign);
        buf.writeBytes(fieldData);
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        assign = buf.readBoolean();
        fieldData = new FriendlyByteBuf(Unpooled.buffer(buf.readableBytes()));
        buf.readBytes(fieldData, buf.readableBytes());
    }

    @Override
    public void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        final IField parsedField = FieldDataManager.bufferToField(fieldData);
        colony.getBuildingManager().getField(otherField -> otherField.equals(parsedField)).ifPresent(field -> {
            if (assign)
            {
                building.getFirstOptionalModuleOccurance(FieldsModule.class).ifPresent(m -> m.assignField(field));
            }
            else
            {
                building.getFirstOptionalModuleOccurance(FieldsModule.class).ifPresent(m -> m.freeField(field));
            }
        });
    }
}

