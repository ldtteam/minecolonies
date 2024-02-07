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
 * Message to toggle recipes (enable/disable).
 */
public class ToggleRecipeMessage extends AbstractBuildingServerMessage<IBuilding>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "toggle_recipe", ToggleRecipeMessage::new);

    /**
     * The recipe to toggle.
     */
    private final int recipeLocation;

    /**
     * Type of the owning module.
     */
    private final int id;

    /**
     * Creates message for player to enable/disable a recipe.
     *  @param building view of the building to read data from
     * @param location the recipeLocation.
     * @param id the unique id of the crafting module.
     */
    public ToggleRecipeMessage(@NotNull final IBuildingView building, final int location, final int id)
    {
        super(TYPE, building);
        this.recipeLocation = location;
        this.id = id;
    }

    protected ToggleRecipeMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.recipeLocation = buf.readInt();
        this.id = buf.readInt();
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(this.recipeLocation);
        buf.writeInt(this.id);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final IBuilding building)
    {
        if (building.getModule(id)!= null)
        {
            final AbstractCraftingBuildingModule module = (AbstractCraftingBuildingModule) building.getModule(id);
            module.toggle(recipeLocation);
        }
    }
}
