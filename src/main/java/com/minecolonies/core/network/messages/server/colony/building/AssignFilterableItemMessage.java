package com.minecolonies.core.network.messages.server.colony.building;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.ItemListModule;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message which handles the assignment of items to filterable item lists.
 */
public class AssignFilterableItemMessage extends AbstractBuildingServerMessage<AbstractBuilding>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "assign_filterable_item_message", AssignFilterableItemMessage::new);

    /**
     * True if assign, false if remove.
     */
    private final boolean assign;

    /**
     * The item in question.
     */
    private final ItemStorage item;

    /**
     * The id of the list.
     */
    private final int id;

    /**
     * Creates the message to add an item.
     *
     * @param id       the id of the list of filterables.
     * @param assign   compost if true, dont if false.
     * @param item     the item to assign
     * @param building the building we're executing on.
     */
    public AssignFilterableItemMessage(final IBuildingView building, final int id, final ItemStorage item, final boolean assign)
    {
        super(TYPE, building);
        this.assign = assign;
        this.item = item;
        this.id = id;
    }

    protected AssignFilterableItemMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);

        this.assign = buf.readBoolean();
        this.item = new ItemStorage(Utils.deserializeCodecMess(buf));
        this.id = buf.readInt();
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);

        buf.writeBoolean(this.assign);
        Utils.serializeCodecMess(buf, this.item.getItemStack());
        buf.writeInt(this.id);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final AbstractBuilding building)
    {
        if (building.getModule(id) instanceof final ItemListModule module)
        {
            if (assign)
            {
                module.addItem(item);
            }
            else
            {
                module.removeItem(item);
            }
        }
    }
}

