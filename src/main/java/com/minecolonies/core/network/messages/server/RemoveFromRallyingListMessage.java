package com.minecolonies.core.network.messages.server;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.MessageUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_BANNER_RALLY_GUARDS_GUI_ERROR;
import static com.minecolonies.core.items.ItemBannerRallyGuards.removeGuardTowerAtLocation;

/**
 * Removes a guard tower from the rallying list
 */
public class RemoveFromRallyingListMessage implements IMessage
{
    /**
     * The banner to be modified.
     */
    private ItemStack banner;

    /**
     * The position of the guard tower that should be removed.
     */
    private ILocation location;

    /**
     * Empty constructor used when registering the message
     */
    public RemoveFromRallyingListMessage()
    {
        super();
    }

    /**
     * Remove the guard tower from the rallying list
     *
     * @param banner   The banner to be modified.
     * @param location The position of the guard tower
     */
    public RemoveFromRallyingListMessage(final ItemStack banner, final ILocation location)
    {
        super();
        this.banner = banner;
        this.location = location;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        banner = buf.readItem();
        location = StandardFactoryController.getInstance().deserialize(buf.readNbt());
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeItem(banner);
        buf.writeNbt(StandardFactoryController.getInstance().serialize(location));
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final ServerPlayer player = ctxIn.getSender();
        final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(player.getInventory()),
          (itemStack -> ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, banner)));

        if (slot == -1)
        {
            MessageUtils.format(COM_MINECOLONIES_BANNER_RALLY_GUARDS_GUI_ERROR).sendTo(player);
            return;
        }

        removeGuardTowerAtLocation(player.getInventory().getItem(slot), location);
    }
}
