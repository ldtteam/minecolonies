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
     * The id of the module.
     */
    private int id;

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
    public AssignFilterableEntityMessage(final IBuildingView building, final int id, final ResourceLocation entity, final boolean assign)
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
        this.id = buf.readInt();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBoolean(this.assign);
        buf.writeResourceLocation(this.entity);
        buf.writeInt(id);
    }

    @Override
    public void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final AbstractBuilding building)
    {
        if (building.getModule(id) instanceof EntityListModule module)
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
