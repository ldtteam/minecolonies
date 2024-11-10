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
 * Alter a menu item message.
 */
public class AlterRestaurantMenuItemMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The menu item.
     */
    private ItemStack itemStack;

    /**
     * Type of the owning module.
     */
    private int id;

    /**
     * If add = true, or remove = false.
     */
    private boolean add;

    /**
     * Empty constructor used when registering the
     */
    public AlterRestaurantMenuItemMessage()
    {
        super();
    }

    /**
     * Add a menu item to the building.
     * @param building the building to add it to.
     * @param itemStack the stack to add.
     * @param runtimeID the id of the module.
     * @return the message,
     */
    public static AlterRestaurantMenuItemMessage addMenuItem(final IBuildingView building, final ItemStack itemStack, final int runtimeID)
    {
        return new AlterRestaurantMenuItemMessage(building, itemStack, runtimeID, true);
    }

    /**
     * Remove a menu item to the building.
     * @param building the building to remove it from.
     * @param itemStack the stack to remove.
     * @param runtimeID the id of the module.
     * @return the message,
     */
    public static AlterRestaurantMenuItemMessage removeMenuItem(final IBuildingView building, final ItemStack itemStack, final int runtimeID)
    {
        return new AlterRestaurantMenuItemMessage(building, itemStack, runtimeID, false);
    }

    /**
     * Creates a menu item alteration.
     *
     * @param itemStack to be altered.
     * @param building  the building we're executing on.
     * @param add if add = true if remove = false
     */
    private AlterRestaurantMenuItemMessage(final IBuildingView building, final ItemStack itemStack, final int runtimeID, final boolean add)
    {
        super(building);
        this.itemStack = itemStack;
        this.id = runtimeID;
        this.add = add;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        itemStack = buf.readItem();
        id = buf.readInt();
        add = buf.readBoolean();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeItem(itemStack);
        buf.writeInt(id);
        buf.writeBoolean(add);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        if (building.getModule(id) instanceof RestaurantMenuModule restaurantMenuModule)
        {
            if (add)
            {
                restaurantMenuModule.addMenuItem(itemStack);
            }
            else
            {
                restaurantMenuModule.removeMenuItem(itemStack);
            }
        }
    }
}
