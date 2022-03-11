package com.minecolonies.coremod.network.messages.server.colony.building.worker;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to toggle recipes (enable/disable).
 */
public class ToggleRecipeMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The recipe to toggle.
     */
    private int recipeLocation;

    /**
     * Type of the owning module.
     */
    private String id;

    /**
     * Empty public constructor.
     */
    public ToggleRecipeMessage()
    {
        super();
    }

    /**
     * Creates message for player to enable/disable a recipe.
     *  @param building view of the building to read data from
     * @param location the recipeLocation.
     * @param id the unique id of the crafting module.
     */
    public ToggleRecipeMessage(@NotNull final IBuildingView building, final int location, final String id)
    {
        super(building);
        this.recipeLocation = location;
        this.id = id;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {
        this.recipeLocation = buf.readInt();
        this.id = buf.readUtf(32767);
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(this.recipeLocation);
        buf.writeUtf(this.id);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        final AbstractCraftingBuildingModule module = building.getModuleMatching(AbstractCraftingBuildingModule.class, m -> m.getId().equals(id));
        module.toggle(recipeLocation);
    }
}
