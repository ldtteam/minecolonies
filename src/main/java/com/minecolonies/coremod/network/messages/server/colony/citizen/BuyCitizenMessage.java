package com.minecolonies.coremod.network.messages.server.colony.citizen;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * Message for the hire citizen action
 */
public class BuyCitizenMessage extends AbstractColonyServerMessage
{
    /**
     * Index of the chosen item, sent to the server
     */
    private int buyItemIndex;

    /**
     * Indexable information about the chosen item
     */
    public enum BuyCitizenType
    {
        HAY_BALE(1, Items.HAY_BLOCK),
        BOOK(2, Items.BOOK),
        EMERALD(3, Items.EMERALD),
        DIAMOND(4, Items.DIAMOND);

        private final int  index;
        private final Item item;

        BuyCitizenType(final int index, final Item item)
        {
            this.index = index;
            this.item = item;
        }

        public int getIndex()
        {
            return index;
        }

        public Item getItem()
        {
            return item;
        }

        public static BuyCitizenType getFromIndex(final int index)
        {
            for (final BuyCitizenType type : BuyCitizenType.values())
            {
                if (type.index == index)
                {
                    return type;
                }
            }
            return DIAMOND;
        }
    }

    /**
     * Default constructor for forge
     */
    public BuyCitizenMessage() {super();}

    public BuyCitizenMessage(@NotNull final BuyCitizenType buyCitizenType, final int colonyId, final int dimension)
    {
        super(dimension, colonyId);
        this.buyItemIndex = buyCitizenType.getIndex();
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        buyItemIndex = buf.readInt();
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        buf.writeInt(buyItemIndex);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        final PlayerEntity player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        // Check if we spawn a new citizen
        if (colony.getCitizenManager().getCurrentCitizenCount() < colony.getCitizenManager().getPotentialMaxCitizens())
        {
            // Get item chosen by player
            final BuyCitizenType buyCitizenType = BuyCitizenType.getFromIndex(buyItemIndex);

            final ItemStack toRemove = new ItemStack(buyCitizenType.item, 1);
            toRemove.setCount(colony.getBoughtCitizenCost() + 1);
            final IItemHandler playerInv = new InvWrapper(player.inventory);

            // Remove items from player
            if (InventoryUtils.tryRemoveStackFromItemHandler(playerInv, toRemove))
            {
                // Create new citizen
                colony.increaseBoughtCitizenCost();

                final ICitizenData data = colony.getCitizenManager().createAndRegisterNewCitizenData();
                data.getCitizenSkillHandler().init(buyCitizenType.index * 10);
                LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(), "com.minecolonies.coremod.progress.hireCitizen");
                colony.getCitizenManager().spawnOrCreateCitizen(data, colony.getWorld(), null, true);
            }
        }
    }
}
