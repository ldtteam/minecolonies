package com.minecolonies.core.network.messages.server.colony.building;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.IMinimumStockModule;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Set a new block to the minimum stock list.
 */
public class RemoveMinimumStockFromBuildingModuleMessage extends AbstractBuildingServerMessage<IBuilding>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "remove_minimum_stock_from_building_module", RemoveMinimumStockFromBuildingModuleMessage::new);

    /**
     * The module's id
     */
    private int moduleId;

    /**
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private ItemStack itemStack;

    /**
     * Creates a Transfer Items request
     *
     * @param building  the building we're executing on.
     * @param itemStack to be take from the player for the building
     */
    public RemoveMinimumStockFromBuildingModuleMessage(final IBuildingView building, final ItemStack itemStack, final int moduleId)
    {
        super(TYPE, building);
        this.itemStack = itemStack;
        this.moduleId = moduleId;
    }

    protected RemoveMinimumStockFromBuildingModuleMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        itemStack = buf.readItem();
        moduleId = buf.readInt();
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeItem(itemStack);
        buf.writeInt(moduleId);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final IBuilding building)
    {
        if (building.getModule(moduleId) instanceof IMinimumStockModule module)
        {
            module.removeMinimumStock(itemStack);
        }
    }
}
