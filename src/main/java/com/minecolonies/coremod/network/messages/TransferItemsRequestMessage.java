package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * Transfer some items from the player inventory to the Builder's chest or additional chests.
 * Created: January 20, 2017
 *
 * @author xavierh
 */
public class TransferItemsRequestMessage implements IMessage
{
    /**
     * The id of the building.
     */
    private BlockPos  buildingId;
    /**
     * The id of the colony.
     */
    private int       colonyId;
    /**
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private ItemStack itemStack;
    /**
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private int       quantity;
    /**
     * Attempt a resolve or not.
     */
    private boolean   attemptResolve;

    /**
     * The dimension of the message.
     */
    private int dimension;

    /**
     * Empty constructor used when registering the message.
     */
    public TransferItemsRequestMessage()
    {
        super();
    }

    /**
     * Creates a Transfer Items request message.
     *
     * @param building  AbstractBuilding of the request.
     * @param itemStack to be take from the player for the building
     * @param quantity  of item needed to be transfered
     */
    public TransferItemsRequestMessage(@NotNull final AbstractBuildingView building, final ItemStack itemStack, final int quantity, final boolean attemptResolve)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.itemStack = itemStack;
        this.quantity = quantity;
        this.attemptResolve = attemptResolve;
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        itemStack = buf.readItemStack();
        quantity = buf.readInt();
        attemptResolve = buf.readBoolean();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeItemStack(itemStack);
        buf.writeInt(quantity);
        buf.writeBoolean(attemptResolve);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final TransferItemsRequestMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony == null)
        {
            Log.getLogger().warn("TransferItemsRequestMessage colony is null");
            return;
        }

        final IBuilding building = colony.getBuildingManager().getBuilding(message.buildingId);
        if (building == null)
        {
            Log.getLogger().warn("TransferItemsRequestMessage building is null");
            return;
        }

        if (message.quantity <= 0)
        {
            Log.getLogger().warn("TransferItemsRequestMessage quantity below 0");
            return;
        }

        final boolean isCreative = player.capabilities.isCreativeMode;
        final Item item = message.itemStack.getItem();
        final int amountToTake;
        if (isCreative)
        {
            amountToTake = message.quantity;
        }
        else
        {
            amountToTake = Math.min(message.quantity, InventoryUtils.getItemCountInItemHandler(new InvWrapper(player.inventory),
                    stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, message.itemStack, true, true)));
        }

        final ItemStack itemStackToTake = new ItemStack(item, amountToTake, message.itemStack.getItemDamage());
        itemStackToTake.put(message.itemStack.getTag());

        ItemStack remainingItemStack = InventoryUtils.addItemStackToProviderWithResult(building.getTileEntity(), itemStackToTake);

        if (!ItemStackUtils.isEmpty(remainingItemStack))
        {
            //If we still have some to drop, let's try the additional chests now
            final World world = colony.getWorld();
            for (final BlockPos pos : building.getAdditionalCountainers())
            {
                final TileEntity entity = world.getTileEntity(pos);
                remainingItemStack = InventoryUtils.addItemStackToProviderWithResult(entity, remainingItemStack);

                if (ItemStackUtils.isEmpty(remainingItemStack))
                {
                    break;
                }
            }
        }

        if (ItemStackUtils.isEmpty(remainingItemStack) || ItemStackUtils.getSize(remainingItemStack) != ItemStackUtils.getSize(itemStackToTake))
        {
            //Only doing this at the moment as the additional chest do not detect new content
            building.getTileEntity().markDirty();
        }

        if (!isCreative)
        {
            int amountToRemoveFromPlayer = amountToTake - ItemStackUtils.getSize(remainingItemStack);
            while (amountToRemoveFromPlayer > 0)
            {
                final int slot =
                        InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(player.inventory),
                                stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, message.itemStack, true, true));
                final ItemStack itemsTaken = player.inventory.decrStackSize(slot, amountToRemoveFromPlayer);
                amountToRemoveFromPlayer -= ItemStackUtils.getSize(itemsTaken);
            }
        }

        if (attemptResolve)
        {
            building.overruleNextOpenRequestWithStack(itemStack);
        }
    }
}
