package com.minecolonies.core.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.core.colony.buildings.modules.RestaurantMenuModule;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Add a menu item message.
 */
public class AddRestaurantMenuItemMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The menu item.
     */
    private ItemStack itemStack;

    /**
     * Empty constructor used when registering the
     */
    public AddRestaurantMenuItemMessage()
    {
        super();
    }

    /**
     * Creates a Transfer Items request
     *
     * @param itemStack to be take from the player for the building
     * @param building  the building we're executing on.
     */
    public AddRestaurantMenuItemMessage(final IBuildingView building, final ItemStack itemStack)
    {
        super(building);
        this.itemStack = itemStack;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        itemStack = buf.readItem();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeItem(itemStack);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        if (building.hasModule(RestaurantMenuModule.class))
        {
            building.getFirstModuleOccurance(RestaurantMenuModule.class).addMenuItem(itemStack);
        }
    }
}
