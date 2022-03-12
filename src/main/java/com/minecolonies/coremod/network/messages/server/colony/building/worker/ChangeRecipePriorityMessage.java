package com.minecolonies.coremod.network.messages.server.colony.building.worker;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to change priorities of recipes.
 */
public class ChangeRecipePriorityMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The workOrder to remove or change priority.
     */
    private int recipeLocation;

    /**
     * If up true, if down false.
     */
    private boolean up;

    /**
     * Type of the owning module.
     */
    private String id;

    /**
     * If moving fully up/down.
     */
    private boolean fullMove;

    /**
     * Empty public constructor.
     */
    public ChangeRecipePriorityMessage()
    {
        super();
    }

    /**
     * Creates message for player to change the priority of the recipes.
     *  @param building view of the building to read data from
     * @param location the recipeLocation.
     * @param up       up or down?
     * @param id the unique id of the crafting module.
     * @param fullMove if it should be moved to the beginning/end.
     */
    public ChangeRecipePriorityMessage(@NotNull final IBuildingView building, final int location, final boolean up, final String id, final boolean fullMove)
    {
        super(building);
        this.recipeLocation = location;
        this.up = up;
        this.id = id;
        this.fullMove = fullMove;
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        this.recipeLocation = buf.readInt();
        this.up = buf.readBoolean();
        this.id = buf.readUtf(32767);
        this.fullMove = buf.readBoolean();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(this.recipeLocation);
        buf.writeBoolean(this.up);
        buf.writeUtf(this.id);
        buf.writeBoolean(this.fullMove);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        final AbstractCraftingBuildingModule module = building.getModuleMatching(AbstractCraftingBuildingModule.class, m -> m.getId().equals(id));
        if (up)
        {
            module.switchOrder(recipeLocation, recipeLocation - 1, fullMove);
        }
        else
        {
            module.switchOrder(recipeLocation, recipeLocation + 1, fullMove);
        }
    }
}
