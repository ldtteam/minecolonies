package com.minecolonies.core.network.messages.server.colony.building;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.EntityListModule;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message which handles the assignment of entities to filterable entity lists.
 */
public class AssignFilterableEntityMessage extends AbstractBuildingServerMessage<AbstractBuilding>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "assign_filterable_entity", AssignFilterableEntityMessage::new);

    /**
     * True if assign, false if remove.
     */
    private final boolean assign;

    /**
     * The entity in question.
     */
    private final ResourceLocation entity;

    /**
     * The id of the module.
     */
    private final int id;

    /**
     * Creates the message to add an entity.
     *
     * @param id       the id of the list of filterables.
     * @param assign   compost if true, dont if false.
     * @param entity     the entity to assign
     * @param building the building we're executing on.
     */
    public AssignFilterableEntityMessage(final IBuildingView building, final int id, final ResourceLocation entity, final boolean assign)
    {
        super(TYPE, building);
        this.assign = assign;
        this.entity = entity;
        this.id = id;
    }

    protected AssignFilterableEntityMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.assign = buf.readBoolean();
        this.entity =buf.readResourceLocation();
        this.id = buf.readInt();
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeBoolean(this.assign);
        buf.writeResourceLocation(this.entity);
        buf.writeInt(id);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final AbstractBuilding building)
    {
        if (building.getModule(id) instanceof final EntityListModule module)
        {
            if (assign)
            {
                module.addEntity(entity);
            }
            else
            {
                module.removeEntity(entity);
            }
        }
    }
}
