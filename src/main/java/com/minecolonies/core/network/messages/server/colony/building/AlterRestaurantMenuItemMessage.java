package com.minecolonies.core.network.messages.server.colony.building;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.modules.RestaurantMenuModule;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Alter a menu item message.
 */
public class AlterRestaurantMenuItemMessage extends AbstractBuildingServerMessage<IBuilding>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "alter_restaurant_menu_module", AlterRestaurantMenuItemMessage::new);

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
        super(TYPE, building);
        this.itemStack = itemStack;
        this.id = runtimeID;
        this.add = add;
    }

    protected AlterRestaurantMenuItemMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        itemStack = Utils.deserializeCodecMess(buf);
        id = buf.readInt();
        add = buf.readBoolean();
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);
        Utils.serializeCodecMess(buf, itemStack);
        buf.writeInt(id);
        buf.writeBoolean(add);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final IBuilding building)
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
