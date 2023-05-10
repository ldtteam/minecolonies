package com.minecolonies.coremod.network.messages.server.colony.building.fields;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.fields.IFieldMatcher;
import com.minecolonies.api.colony.fields.registry.IFieldDataManager;
import com.minecolonies.coremod.colony.buildings.modules.FieldsModule;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message which handles the assignment of fields to farmers.
 */
public class AssignFieldMessage extends AbstractBuildingServerMessage<IBuilding>
{
    private boolean assign;

    private IFieldMatcher matcher;

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
     * @param matcher  the field matcher to lookup fields by.
     * @param building the building we're executing on.
     */
    public AssignFieldMessage(final IBuildingView building, final boolean assign, final IFieldMatcher matcher)
    {
        super(building);
        this.assign = assign;
        this.matcher = matcher;
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBoolean(assign);
        IFieldDataManager.getInstance().matcherToBytes(matcher, buf);
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        assign = buf.readBoolean();
        matcher = IFieldDataManager.getInstance().matcherFromBytes(buf);
    }

    @Override
    public void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        if (assign)
        {
            building.getFirstOptionalModuleOccurance(FieldsModule.class).ifPresent(m -> m.assignField(matcher));
        }
        else
        {
            building.getFirstOptionalModuleOccurance(FieldsModule.class).ifPresent(m -> m.freeField(matcher));
        }
    }
}

