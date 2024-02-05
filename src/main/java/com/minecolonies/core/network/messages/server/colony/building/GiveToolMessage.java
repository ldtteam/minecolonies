package com.minecolonies.core.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * Message to set the beekeeper scepter in the player inventory.
 */
public class GiveToolMessage extends AbstractBuildingServerMessage<AbstractBuilding>
{
    /**
     * The item to give.
     */
    private Item item;

    /**
     * Empty standard constructor.
     */
    public GiveToolMessage()
    {
        super();
    }

    /**
     * Create a new tool message.
     * @param building the building it's created from.
     * @param item the item to give.
     */
    public GiveToolMessage(final IBuildingView building, final Item item)
    {
        super(building);
        this.item = item;
    }

    @Override
    protected void toBytesOverride(final FriendlyByteBuf buf)
    {
        buf.writeItem(new ItemStack(item, 1));
    }

    @Override
    protected void fromBytesOverride(final FriendlyByteBuf buf)
    {
        item = buf.readItem().getItem();
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final AbstractBuilding building)
    {
        final Player player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        final ItemStack scepter = InventoryUtils.getOrCreateItemAndPutToHotbarAndSelectOrDrop(item, player, item::getDefaultInstance, true);
        final CompoundTag compound = scepter.getOrCreateTag();
        BlockPosUtil.write(compound, TAG_POS, building.getID());
        compound.putInt(TAG_ID, colony.getID());

        player.getInventory().setChanged();
    }
}
