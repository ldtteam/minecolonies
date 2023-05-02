package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.modules.BuildingResourcesModule;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;

public class TransferAllItemsRequestMessage extends AbstractBuildingServerMessage<IBuilding> {

    private boolean attemptResolve;

    public TransferAllItemsRequestMessage()
    {
        super();
    }

    public TransferAllItemsRequestMessage(final IBuildingView building, boolean attemptResolve)
    {
        super(building);
        this.attemptResolve = attemptResolve;
    }

    @Override
    protected void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer, IColony colony, IBuilding building)
    {
        final Player player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        final boolean isCreative = player.isCreative();

        if (building instanceof BuildingBuilder buildingBuilder)
        {
            final Map<String, BuildingBuilderResource> neededResources = buildingBuilder.getNeededResources();

            boolean changed = false;

            for (Map.Entry<String, BuildingBuilderResource> entry : neededResources.entrySet())
            {
                final BuildingBuilderResource res = entry.getValue();
                final ItemStack itemStack = res.getItemStack();
                final int amountToTake;

                int needed = res.getAmount() - res.getAvailable();

                if (isCreative)
                {
                    amountToTake = needed;
                }
                else
                {
                    amountToTake = Math.min(needed, InventoryUtils.getItemCountInItemHandler(new InvWrapper(player.getInventory()),
                            stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, res.getItemStack(), true, true)));
                }

                if (amountToTake == 0)
                {
                    continue;
                }

                ItemStack remainingItemStack = ItemStack.EMPTY;
                int tempAmount = amountToTake;
                for (int i = 0; i < Math.max(1, Math.ceil((double) amountToTake / itemStack.getMaxStackSize())); i++)
                {
                    final ItemStack itemStackToTake = itemStack.copy();
                    int insertAmount = Math.min(itemStack.getMaxStackSize(), tempAmount);
                    itemStackToTake.setCount(insertAmount);
                    tempAmount -= insertAmount;

                    remainingItemStack = InventoryUtils.addItemStackToProviderWithResult(building.getTileEntity(), itemStackToTake);
                    if (!remainingItemStack.isEmpty())
                    {
                        tempAmount += remainingItemStack.getCount();
                        break;
                    }
                }

                if (ItemStackUtils.isEmpty(remainingItemStack) || ItemStackUtils.getSize(remainingItemStack) != amountToTake)
                {
                    //Only doing this at the moment as the additional chest do not detect new content
                    changed = true;
                }

                if (InventoryUtils.openSlotCount(building.getTileEntity().getInventory()) <= 0)
                {
                    break;
                }

                if (ItemStackUtils.isEmpty(remainingItemStack) || ItemStackUtils.getSize(remainingItemStack) != amountToTake)
                {
                    if (!isCreative)
                    {
                        int amountToRemoveFromPlayer = amountToTake - tempAmount;
                        while (amountToRemoveFromPlayer > 0)
                        {
                            final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(player.getInventory()),
                                    stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, itemStack, true, true));
                            final ItemStack itemsTaken = player.getInventory().removeItem(slot, amountToRemoveFromPlayer);
                            amountToRemoveFromPlayer -= ItemStackUtils.getSize(itemsTaken);
                        }
                    }

                    building.overruleNextOpenRequestWithStack(itemStack);
                }
            }

            if (changed)
            {
                building.getTileEntity().setChanged();
            }
        }
    }

    @Override
    protected void toBytesOverride(FriendlyByteBuf buf)
    {
        buf.writeBoolean(attemptResolve);
    }

    @Override
    protected void fromBytesOverride(FriendlyByteBuf buf)
    {
        attemptResolve = buf.readBoolean();
    }
}
