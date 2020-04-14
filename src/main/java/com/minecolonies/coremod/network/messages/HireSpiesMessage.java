package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.InventoryUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBarracks.SPIES_GOLD_COST;

/**
 * Message for hiring spies at the cost of gold.
 */
public class HireSpiesMessage extends AbstractMessage<HireSpiesMessage, IMessage>
{
    /**
     * Dimension of the colony
     */
    private int dimension;

    /**
     * ID of the colony
     */
    private int colonyId;

    /**
     * Default constructor for forge
     */
    public HireSpiesMessage() {super();}

    public HireSpiesMessage(@NotNull final int colonyId, @NotNull final int dimension)
    {
        super();
        this.colonyId = colonyId;
        this.dimension = dimension;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final HireSpiesMessage message, final EntityPlayerMP player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);

        if (colony == null || !colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
        {
            return;
        }

        if (InventoryUtils.getItemCountInItemHandler(new InvWrapper(player.inventory), stack -> stack.getItem() == Items.GOLD_INGOT) > SPIES_GOLD_COST)
        {
            InventoryUtils.reduceStackInItemHandler(new InvWrapper(player.inventory), new ItemStack(Items.GOLD_INGOT), SPIES_GOLD_COST);
            colony.getRaiderManager().setSpiesEnabled(true);
            colony.markDirty();
        }
    }
}
