package com.minecolonies.core.tileentities;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.api.tileentities.AbstractTileEntityWareHouse;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.*;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.TICKS_FIVE_MIN;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse.MAX_STORAGE_UPGRADE;

/**
 * Class which handles the tileEntity of our colony warehouse.
 */
public class TileEntityWareHouse extends AbstractTileEntityWareHouse
{
    /**
     * Time of last sent notifications.
     */
    private long lastNotification                   = 0;

    public TileEntityWareHouse(final BlockPos pos, final BlockState state)
    {
        super(MinecoloniesTileEntities.WAREHOUSE.get(), pos, state);
        inWarehouse = true;
    }

    @Override
    public boolean hasMatchingItemStackInWarehouse(@NotNull final Predicate<ItemStack> itemStackSelectionPredicate, int count)
    {
        int totalCount = 0;
        if (getBuilding() != null)
        {
            for (@NotNull final BlockPos pos : getBuilding().getContainers())
            {
                if (WorldUtil.isBlockLoaded(level, pos))
                {
                    final BlockEntity entity = getLevel().getBlockEntity(pos);
                    if (entity instanceof final TileEntityRack rack && !rack.isEmpty())
                    {
                        totalCount += rack.getItemCount(itemStackSelectionPredicate);
                        if (totalCount >= count)
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public int getCountInWarehouse(@NotNull final ItemStorage storage, int count)
    {
        int totalCount = 0;
        if (getBuilding() != null)
        {
            for (@NotNull final BlockPos pos : getBuilding().getContainers())
            {
                if (WorldUtil.isBlockLoaded(level, pos))
                {
                    final BlockEntity entity = getLevel().getBlockEntity(pos);
                    if (entity instanceof final TileEntityRack rack && !rack.isEmpty())
                    {
                        totalCount += rack.getCount(storage);
                        if (totalCount >= count)
                        {
                            return totalCount;
                        }
                    }
                }
            }
        }

        return totalCount;
    }

    @Override
    public boolean hasMatchingItemStackInWarehouse(@NotNull final ItemStack itemStack, final int count, final boolean ignoreNBT)
    {
        return hasMatchingItemStackInWarehouse(itemStack, count, ignoreNBT, 0);
    }

    @Override
    public boolean hasMatchingItemStackInWarehouse(@NotNull final ItemStack itemStack, final int count, final boolean ignoreNBT, final boolean ignoreDamage, final int leftOver)
    {
        int totalCountFound = 0 - leftOver;
        for (@NotNull final BlockPos pos : getBuilding().getContainers())
        {
            if (WorldUtil.isBlockLoaded(level, pos))
            {
                final BlockEntity entity = getLevel().getBlockEntity(pos);
                if (entity instanceof TileEntityRack && !((AbstractTileEntityRack) entity).isEmpty())
                {
                    totalCountFound += ((AbstractTileEntityRack) entity).getCount(itemStack, ignoreDamage, ignoreNBT);
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
    public boolean hasMatchingItemStackInWarehouse(@NotNull final ItemStack itemStack, final int count, final boolean ignoreNBT, final int leftOver)
    {
        return hasMatchingItemStackInWarehouse(itemStack, count, ignoreNBT, true, leftOver);
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
                if (WorldUtil.isBlockLoaded(level, pos))
                {
                    final BlockEntity entity = getLevel().getBlockEntity(pos);
                    if (entity instanceof final TileEntityRack rack && !rack.isEmpty() && rack.getItemCount(itemStackSelectionPredicate) > 0)
                    {
                        for (final ItemStack stack : (InventoryUtils.filterItemHandler(rack.getInventory(), itemStackSelectionPredicate)))
                        {
                            found.add(new Tuple<>(stack, pos));
                        }
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

            @Nullable final AbstractTileEntityRack chest = getRackForStack(stack);
            if (chest == null)
            {
                if(level.getGameTime() - lastNotification > TICKS_FIVE_MIN)
                {
                    lastNotification = level.getGameTime();
                    if (getBuilding().getBuildingLevel() == getBuilding().getMaxBuildingLevel())
                    {
                        if (getBuilding().getModule(BuildingModules.WAREHOUSE_OPTIONS).getStorageUpgrade() < MAX_STORAGE_UPGRADE)
                        {
                            MessageUtils.format(COM_MINECOLONIES_COREMOD_WAREHOUSE_FULL_LEVEL5_UPGRADE).sendTo(getColony()).forAllPlayers();
                        }
                        else
                        {
                            MessageUtils.format(COM_MINECOLONIES_COREMOD_WAREHOUSE_FULL_MAX_UPGRADE).sendTo(getColony()).forAllPlayers();
                        }
                    }
                    else
                    {
                        MessageUtils.format(COM_MINECOLONIES_COREMOD_WAREHOUSE_FULL).sendTo(getColony()).forAllPlayers();
                    }
                }
                return;
            }

            InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(inventoryCitizen, i, chest.getItemHandlerCap());
        }
    }

    /**
     * Get a rack for a stack.
     * @param stack the stack to insert.
     * @return the matching rack.
     */
    public AbstractTileEntityRack getRackForStack(final ItemStack stack)
    {
        AbstractTileEntityRack rack = getPositionOfChestWithItemStack(stack);
        if (rack == null)
        {
            rack = getPositionOfChestWithSimilarItemStack(stack);
            if (rack == null)
            {
                rack = searchMostEmptyRack();
            }
        }
        return rack;
    }

    /**
     * Search the right chest for an itemStack.
     *
     * @param stack the stack to dump.
     * @return the tile entity of the chest
     */
    @Nullable
    private AbstractTileEntityRack getPositionOfChestWithItemStack(@NotNull final ItemStack stack)
    {
        for (@NotNull final BlockPos pos : getBuilding().getContainers())
        {
            if (WorldUtil.isBlockLoaded(level, pos))
            {
                final BlockEntity entity = getLevel().getBlockEntity(pos);
                if (entity instanceof final AbstractTileEntityRack rack)
                {
                    if (rack.getFreeSlots() > 0 && rack.hasItemStack(stack, 1, true))
                    {
                        return rack;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Searches a chest with a similar item as the incoming stack.
     *
     * @param stack the stack.
     * @return the entity of the chest.
     */
    @Nullable
    private AbstractTileEntityRack getPositionOfChestWithSimilarItemStack(final ItemStack stack)
    {
        for (@NotNull final BlockPos pos : getBuilding().getContainers())
        {
            if (WorldUtil.isBlockLoaded(level, pos))
            {
                final BlockEntity entity = getLevel().getBlockEntity(pos);
                if (entity instanceof final AbstractTileEntityRack rack)
                {
                    if (rack.getFreeSlots() > 0 && rack.hasSimilarStack(stack))
                    {
                        return rack;
                    }
                }
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
    private AbstractTileEntityRack searchMostEmptyRack()
    {
        int freeSlots = 0;
        AbstractTileEntityRack emptiestChest = null;
        for (@NotNull final BlockPos pos : getBuilding().getContainers())
        {
            final BlockEntity entity = getLevel().getBlockEntity(pos);
            if (entity instanceof final TileEntityRack rack)
            {
                if (rack.isEmpty())
                {
                    return rack;
                }

                final int tempFreeSlots = rack.getFreeSlots();
                if (tempFreeSlots > freeSlots)
                {
                    freeSlots = tempFreeSlots;
                    emptiestChest = rack;
                }
            }
        }
        return emptiestChest;
    }
}
