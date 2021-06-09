package com.minecolonies.coremod.tileentities;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.api.tileentities.AbstractTileEntityWareHouse;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.TICKS_FIVE_MIN;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_WAREHOUSE_FULL;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_WAREHOUSE_FULL_MAX_UPGRADE;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

/**
 * Class which handles the tileEntity of our colony warehouse.
 */
public class TileEntityWareHouse extends AbstractTileEntityWareHouse
{
    /**
     * Time of last sent notifications.
     */
    private long lastNotification                   = 0;

    public TileEntityWareHouse()
    {
        super(MinecoloniesTileEntities.WAREHOUSE);
    }

    @Override
    public boolean hasMatchingItemStackInWarehouse(@NotNull final Predicate<ItemStack> itemStackSelectionPredicate, int count)
    {
        final List<Tuple<ItemStack, BlockPos>> targetStacks = getMatchingItemStacksInWarehouse(itemStackSelectionPredicate);
        return targetStacks.stream().mapToInt(tuple -> ItemStackUtils.getSize(tuple.getA())).sum() >= count;
    }

    @Override
    public boolean hasMatchingItemStackInWarehouse(@NotNull final ItemStack itemStack, final int count, final boolean ignoreNBT)
    {
        return hasMatchingItemStackInWarehouse(itemStack, count, ignoreNBT, 0);
    }

    @Override
    public boolean hasMatchingItemStackInWarehouse(@NotNull final ItemStack itemStack, final int count, final boolean ignoreNBT, final int leftOver)
    {
        int totalCountFound = 0 - leftOver;
        for (@NotNull final BlockPos pos : getBuilding().getContainers())
        {
            if (WorldUtil.isBlockLoaded(level, pos))
            {
                final TileEntity entity = getLevel().getBlockEntity(pos);
                if (entity instanceof TileEntityRack && !((AbstractTileEntityRack) entity).isEmpty())
                {
                    totalCountFound += ((AbstractTileEntityRack) entity).getCount(itemStack, true, ignoreNBT);
                    if (totalCountFound >= count)
                    {
                        return true;
                    }
                }

                if (entity instanceof ChestTileEntity)
                {
                    totalCountFound += InventoryUtils.getItemCountInItemHandler(entity.getCapability(ITEM_HANDLER_CAPABILITY, null).orElseGet(null),
                      item -> item.sameItemStackIgnoreDurability(itemStack) && item.getCount() >= itemStack.getCount());
                    if (totalCountFound >= count)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    @NotNull
    public List<Tuple<ItemStack, BlockPos>> getMatchingItemStacksInWarehouse(@NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        List<Tuple<ItemStack, BlockPos>> found = new ArrayList<>();
        
        if (getBuilding() != null)
        {
            for (@NotNull final BlockPos pos : getBuilding().getContainers())
            {
                final TileEntity entity = getLevel().getBlockEntity(pos);
                if (entity instanceof TileEntityRack && !((AbstractTileEntityRack) entity).isEmpty() && ((AbstractTileEntityRack) entity).getItemCount(itemStackSelectionPredicate) > 0)
                {
                    final TileEntityRack rack = (TileEntityRack) entity;
                    for (final ItemStack stack : (InventoryUtils.filterItemHandler(rack.getInventory(), itemStackSelectionPredicate)))
                    {
                        found.add(new Tuple<>(stack, pos));
                    }
                }

                if (entity instanceof ChestTileEntity && InventoryUtils.hasItemInItemHandler(entity.getCapability(ITEM_HANDLER_CAPABILITY, null).orElseGet(null), itemStackSelectionPredicate))
                {
                    for (final ItemStack stack : InventoryUtils.filterItemHandler(entity.getCapability(ITEM_HANDLER_CAPABILITY, null).orElseGet(null), itemStackSelectionPredicate))
                    {
                        found.add(new Tuple<>(stack, pos));
                    }
                }
            }
        }
        return found;
    }

    @Override
    public void dumpInventoryIntoWareHouse(@NotNull final InventoryCitizen inventoryCitizen)
    {
        for (int i = 0; i < inventoryCitizen.getSlots(); i++)
        {
            final ItemStack stack = inventoryCitizen.getStackInSlot(i);
            if (ItemStackUtils.isEmpty(stack))
            {
                continue;
            }

            @Nullable final TileEntity chest = searchRightChestForStack(stack);
            if (chest == null)
            {
                if(level.getGameTime() - lastNotification > TICKS_FIVE_MIN)
                {
                    lastNotification = level.getGameTime();
                    if(getBuilding().getBuildingLevel() == getBuilding().getMaxBuildingLevel())
                    {
                        LanguageHandler.sendPlayersMessage(getColony().getMessagePlayerEntities(), COM_MINECOLONIES_COREMOD_WAREHOUSE_FULL_MAX_UPGRADE);
                    }
                    else
                    {
                        LanguageHandler.sendPlayersMessage(getColony().getMessagePlayerEntities(), COM_MINECOLONIES_COREMOD_WAREHOUSE_FULL);
                    }
                }
                return;
            }

            final int index = i;
            chest.getCapability(ITEM_HANDLER_CAPABILITY, null).ifPresent(handler -> InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(inventoryCitizen, index, handler));
        }
    }

    /**
     * Search the right chest for an itemStack.
     *
     * @param stack the stack to dump.
     * @return the tile entity of the chest
     */
    @Nullable
    private TileEntity searchRightChestForStack(@NotNull final ItemStack stack)
    {
        for (@NotNull final BlockPos pos : getBuilding().getContainers())
        {
            final TileEntity entity = getLevel().getBlockEntity(pos);
            if (isInRack(stack, entity, false) || isInChest(stack, entity, false))
            {
                return entity;
            }
        }

        @Nullable final TileEntity chest = searchChestWithSimilarItem(stack);
        return chest == null ? searchMostEmptySlot() : chest;
    }

    /**
     * Check if a similar item is in the rack.
     *
     * @param stack             the stack to check.
     * @param entity            the entity.
     * @param includeSimilarMatches if similar matches should be included or only exact matches.
     * @return true if so.
     */
    private static boolean isInRack(final ItemStack stack, final TileEntity entity, final boolean includeSimilarMatches)
    {
        return entity instanceof TileEntityRack
                 && !((AbstractTileEntityRack) entity).isEmpty()
                 && (includeSimilarMatches ? ((TileEntityRack) entity).hasSimilarStack(stack) : ((AbstractTileEntityRack) entity).hasItemStack(stack, 1, false))
                 && ((TileEntityRack) entity).getFreeSlots() > 0;
    }

    /**
     * Check if a similar item is in the chest.
     *
     * @param stack             the stack to check.
     * @param entity            the entity.
     * @param ignoreDamageValue should the damage value be ignored.
     * @return true if so.
     */
    private static boolean isInChest(final ItemStack stack, final TileEntity entity, final boolean ignoreDamageValue)
    {
        return entity instanceof ChestTileEntity
                 && InventoryUtils.findSlotInItemHandlerNotFullWithItem(entity.getCapability(ITEM_HANDLER_CAPABILITY, null).orElseGet(null), stack);
    }

    /**
     * Searches a chest with a similar item as the incoming stack.
     *
     * @param stack the stack.
     * @return the entity of the chest.
     */
    @Nullable
    private TileEntity searchChestWithSimilarItem(final ItemStack stack)
    {
        for (@NotNull final BlockPos pos : getBuilding().getContainers())
        {
            final TileEntity entity = getLevel().getBlockEntity(pos);
            if (isInRack(stack, entity, true) || isInChest(stack, entity, true))
            {
                return entity;
            }
        }
        return null;
    }

    /**
     * Search for the chest with the least items in it.
     *
     * @return the tileEntity of this chest.
     */
    @Nullable
    private TileEntity searchMostEmptySlot()
    {
        int freeSlots = 0;
        TileEntity emptiestChest = null;
        for (@NotNull final BlockPos pos : getBuilding().getContainers())
        {
            final TileEntity entity = getLevel().getBlockEntity(pos);
            if (entity == null)
            {
                getBuilding().removeContainerPosition(pos);
                continue;
            }
            final int tempFreeSlots;
            if (entity instanceof TileEntityRack)
            {
                if (((AbstractTileEntityRack) entity).isEmpty())
                {
                    return entity;
                }

                tempFreeSlots = ((AbstractTileEntityRack) entity).getFreeSlots();
                if (freeSlots < tempFreeSlots)
                {
                    freeSlots = tempFreeSlots;
                    emptiestChest = entity;
                }
            }
            else if (entity instanceof ChestTileEntity && InventoryUtils.getFirstOpenSlotFromProvider(entity) != -1)
            {
                tempFreeSlots = ((ChestTileEntity) entity).getContainerSize() - InventoryUtils.getAmountOfStacksInProvider(entity);
                if (freeSlots < tempFreeSlots)
                {
                    freeSlots = tempFreeSlots;
                    emptiestChest = entity;
                }
            }
        }

        return emptiestChest;
    }
}
