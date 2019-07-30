package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Message for the hire citizen action
 */
public class BuyCitizenMessage extends AbstractMessage<BuyCitizenMessage, IMessage>
{
    /**
     * ID of the colony
     */
    private int colonyId;

    /**
     * Index of the chosen item, sent to the server
     */
    private int buyItemIndex;

    /**
     * The dimension of the message.
     */
    private int dimension;

    /**
     * Indexable information about the chosen item
     */
    public enum BuyCitizenType
    {
        HAY_BALE(1, Item.getItemFromBlock(Blocks.HAY_BLOCK)),
        BOOK(2, Items.BOOK),
        EMERALD(3, Items.EMERALD),
        DIAMOND(4, Items.DIAMOND);

        private final int  index;
        private final Item item;

        private BuyCitizenType(final int index, final Item item)
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

    public BuyCitizenMessage(@NotNull final BuyCitizenType buyCitizenType, @NotNull final int colonyId, @NotNull final int dimension)
    {
        super();
        this.buyItemIndex = buyCitizenType.getIndex();
        this.colonyId = colonyId;
        this.dimension = dimension;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        dimension = buf.readInt();
        buyItemIndex = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(dimension);
        buf.writeInt(buyItemIndex);
    }

    @Override
    public void messageOnServerThread(final BuyCitizenMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColonyByDimension(message.colonyId, message.dimension);

        if (colony == null)
        {
            return;
        }

        // Check if we spawn a new citizen
        if (colony.getCitizenManager().getCurrentCitizenCount() < colony.getCitizenManager().getMaxCitizens())
        {
            // Get item chosen by player
            final BuyCitizenType buyCitizenType = BuyCitizenType.getFromIndex(message.buyItemIndex);

            final ItemStack toRemove = new ItemStack(buyCitizenType.item, 1);
            toRemove.setCount(colony.getBoughtCitizenCost() + 1);
            final IItemHandler playerInv = new InvWrapper(player.inventory);

            // Remove items from player
            if (InventoryUtils.removeStackFromItemHandler(playerInv, toRemove))
            {
                // Create new citizen
                colony.increaseBoughtCitizenCost();

                final CitizenData data = colony.getCitizenManager().createAndRegisterNewCitizenData();

                // Never roll max happiness for buying citizens, so library has to be used.
                final double maxStat = colony.getOverallHappiness() - 1;

                final double high = maxStat * buyCitizenType.index / 4;
                final double low = maxStat * (buyCitizenType.index - 1) / 4;
                final Random rand = new Random();

                data.setIntelligence((int) Math.round(rand.nextDouble() * (high - low) + low));
                data.setEndurance((int) Math.round(rand.nextDouble() * (high - low) + low));
                data.setDexterity((int) Math.round(rand.nextDouble() * (high - low) + low));
                data.setCharisma((int) Math.round(rand.nextDouble() * (high - low) + low));
                data.setStrength((int) Math.round(rand.nextDouble() * (high - low) + low));

                LanguageHandler.sendPlayersMessage(colony.getMessageEntityPlayers(), "com.minecolonies.coremod.progress.hireCitizen");
                colony.getCitizenManager().spawnOrCreateCitizen(data, colony.getWorld(), null, true);
            }
        }
    }
}
