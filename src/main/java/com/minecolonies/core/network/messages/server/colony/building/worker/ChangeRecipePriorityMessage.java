package com.minecolonies.core.network.messages.server.colony.building.worker;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message to change priorities of recipes.
 */
public class ChangeRecipePriorityMessage extends AbstractBuildingServerMessage<IBuilding>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "change_recipe_priority", ChangeRecipePriorityMessage::new);

    /**
     * The workOrder to remove or change priority.
     */
    private final int recipeLocation;

    /**
     * If up true, if down false.
     */
    private final boolean up;

    /**
     * Type of the owning module.
     */
    private final int id;

    /**
     * If moving fully up/down.
     */
    private final boolean fullMove;

    /**
     * Creates message for player to change the priority of the recipes.
     *  @param building view of the building to read data from
     * @param location the recipeLocation.
     * @param up       up or down?
     * @param id the unique id of the crafting module.
     * @param fullMove if it should be moved to the beginning/end.
     */
    public ChangeRecipePriorityMessage(@NotNull final IBuildingView building, final int location, final boolean up, final int id, final boolean fullMove)
    {
        super(TYPE, building);
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
    protected ChangeRecipePriorityMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.recipeLocation = buf.readInt();
        this.up = buf.readBoolean();
        this.id = buf.readInt();
        this.fullMove = buf.readBoolean();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(this.recipeLocation);
        buf.writeBoolean(this.up);
        buf.writeInt(this.id);
        buf.writeBoolean(this.fullMove);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final IBuilding building)
    {
        if (building.getModule(id) instanceof final AbstractCraftingBuildingModule module)
        {
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
}
