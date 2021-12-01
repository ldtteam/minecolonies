package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.EntityListModule;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message which handles the assignment of entities to filterable entity lists.
 */
public class AssignFilterableEntityMessage extends AbstractBuildingServerMessage<AbstractBuilding>
{
    /**
     * True if assign, false if remove.
     */
    private boolean assign;

    /**
     * The entity in question.
     */
    private ResourceLocation entity;

    /**
     * The id of the list.
     */
    private String id;

    /**
     * Empty standard constructor.
     */
    public AssignFilterableEntityMessage()
    {
        super();
    }

    /**
     * Creates the message to add an entity.
     *
     * @param id       the id of the list of filterables.
     * @param assign   compost if true, dont if false.
     * @param entity     the entity to assign
     * @param building the building we're executing on.
     */
    public AssignFilterableEntityMessage(final IBuildingView building, final String id, final ResourceLocation entity, final boolean assign)
    {
        super(building);
        this.assign = assign;
        this.entity = entity;
        this.id = id;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

        this.assign = buf.readBoolean();
        this.entity =buf.readResourceLocation();
        this.id = buf.readUtf(32767);
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

        buf.writeBoolean(this.assign);
        buf.writeResourceLocation(this.entity);
        buf.writeUtf(this.id);
    }

    @Override
    public void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final AbstractBuilding building)
    {
        if (building.hasModule(EntityListModule.class))
        {
            if (assign)
            {
                building.getModuleMatching(EntityListModule.class, m -> m.getId().equals(id)).addEntity(entity);
            }
            else
            {
                building.getModuleMatching(EntityListModule.class, m -> m.getId().equals(id)).removeEntity(entity);
            }
        }
    }
}
