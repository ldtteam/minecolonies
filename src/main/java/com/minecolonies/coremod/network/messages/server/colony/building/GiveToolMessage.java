package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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
    protected void toBytesOverride(final PacketBuffer buf)
    {
        buf.writeItemStack(new ItemStack(item, 1));
    }

    @Override
    protected void fromBytesOverride(final PacketBuffer buf)
    {
        item = buf.readItemStack().getItem();
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final AbstractBuilding building)
    {
        final PlayerEntity player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        final ItemStack scepter = InventoryUtils.getOrCreateItemAndPutToHotbarAndSelectOrDrop(item, player, item::getDefaultInstance, true);
        final CompoundNBT compound = scepter.getOrCreateTag();
        BlockPosUtil.write(compound, TAG_POS, building.getID());
        compound.putInt(TAG_ID, colony.getID());

        player.inventory.markDirty();
    }
}
