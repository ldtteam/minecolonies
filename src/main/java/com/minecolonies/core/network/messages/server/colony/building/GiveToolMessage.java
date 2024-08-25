package com.minecolonies.core.network.messages.server.colony.building;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Message to set the beekeeper scepter in the player inventory.
 */
public class GiveToolMessage extends AbstractBuildingServerMessage<AbstractBuilding>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "give_tool", GiveToolMessage::new);

    /**
     * The item to give.
     */
    private final Item item;

    /**
     * Create a new tool message.
     * @param building the building it's created from.
     * @param item the item to give.
     */
    public GiveToolMessage(final IBuildingView building, final Item item)
    {
        super(TYPE, building);
        this.item = item;
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);
        Utils.serializeCodecMess(buf, new ItemStack(item, 1));
    }

    protected GiveToolMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        item = Utils.deserializeCodecMess(buf).getItem();
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final AbstractBuilding building)
    {
        final ItemStack scepter = InventoryUtils.getOrCreateItemAndPutToHotbarAndSelectOrDrop(item, player, item::getDefaultInstance, true);
        building.writeToItemStack(scepter);

        player.getInventory().setChanged();
    }
}
