package com.minecolonies.core.tileentities;

import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.tileentities.AbstractTileEntityWareHouse;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.tileentities.storageblocks.AbstractStorageBlockInterface;
import com.minecolonies.api.util.*;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import net.minecraft.world.item.ItemStack;
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
            for (@NotNull final AbstractStorageBlockInterface storageInterface : getBuilding().getContainers())
            {
                if (!storageInterface.isLoaded() || !storageInterface.isStillValid())
                {
                    continue;
                }

                if (storageInterface.isEmpty()) {
                    continue;
                }

                totalCount += storageInterface.getItemCount(itemStackSelectionPredicate);
                if (totalCount >= count)
                {
                    return true;
                }
            }
        }

        return false;
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
        for (@NotNull final AbstractStorageBlockInterface storageInterface : getBuilding().getContainers())
        {
            if (!storageInterface.isLoaded() || !storageInterface.isStillValid())
            {
                continue;
            }

            if (storageInterface.isEmpty())
            {
                continue;
            }

            totalCountFound += storageInterface.getCount(itemStack, ignoreDamage, ignoreNBT);
            if (totalCountFound >= count)
            {
                return true;
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
            for (@NotNull final AbstractStorageBlockInterface storageInterface : getBuilding().getContainers())
            {
                if (!storageInterface.isLoaded() || !storageInterface.isStillValid())
                {
                    continue;
                }

                if (storageInterface.isEmpty())
                {
                    continue;
                }

                if (storageInterface.getItemCount(itemStackSelectionPredicate) <= 0)
                {
                    continue;
                }

                for (final ItemStack stack : storageInterface.getMatching(itemStackSelectionPredicate))
                {
                    found.add(new Tuple<>(stack, storageInterface.getPosition()));
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

            @Nullable final AbstractStorageBlockInterface chest = getBlockStorageForStack(stack);
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

            final int index = i;
            InventoryUtils.transferIntoNextBestSlotInStorageBlock(inventoryCitizen, index, chest);
        }
    }

    /**
     * Get a blockstorage for a stack.
     * @param stack the stack to insert.
     * @return the matching blockstorage interface.
     */
    public AbstractStorageBlockInterface getBlockStorageForStack(final ItemStack stack)
    {
        AbstractStorageBlockInterface storageBlock = getPositionOfStorageBlockWithItemStack(stack);
        if (storageBlock != null)
        {
            return storageBlock;
        }

        storageBlock = getPositionOfStorageBlockWithSimilarItemStack(stack);
        if (storageBlock != null)
        {
            return storageBlock;
        }

        return searchMostEmptyStorageBlock();
    }

    /**
     * Search the right storageblock for an itemStack.
     *
     * @param stack the stack to dump.
     * @return the interface of the storageblock
     */
    @Nullable
    private AbstractStorageBlockInterface getPositionOfStorageBlockWithItemStack(@NotNull final ItemStack stack)
    {
        for (@NotNull final AbstractStorageBlockInterface storageInterface : getBuilding().getContainers())
        {
            if (!storageInterface.isLoaded() || !storageInterface.isStillValid())
            {
                continue;
            }

            if (storageInterface.getFreeSlots() > 0 && storageInterface.hasItemStack(stack, 1, true))
            {
                return storageInterface;
            }
        }

        return null;
    }

    /**
     * Searches a storageblock with a similar item as the incoming stack.
     *
     * @param stack the stack.
     * @return the interface of the storageblock.
     */
    @Nullable
    private AbstractStorageBlockInterface getPositionOfStorageBlockWithSimilarItemStack(final ItemStack stack)
    {
        for (@NotNull final AbstractStorageBlockInterface storageInterface : getBuilding().getContainers())
        {
            if (!storageInterface.isLoaded() || !storageInterface.isStillValid())
            {
                continue;
            }

            if (storageInterface.getFreeSlots() <= 0)
            {
                continue;
            }

            if (storageInterface.hasItemStack(stack, 1, true))
            {
                return storageInterface;
            }
        }
        return null;
    }

    /**
     * Search for the chest with the least items in it.
     *
     * @return the interface of this chest.
     */
    @Nullable
    private AbstractStorageBlockInterface searchMostEmptyStorageBlock()
    {
        int freeSlots = 0;
        AbstractStorageBlockInterface emptiestChest = null;
        for (@NotNull final AbstractStorageBlockInterface storageInterface : getBuilding().getContainers())
        {
            if (!storageInterface.isStillValid())
            {
                continue;
            }

            if (storageInterface.isEmpty()) {
                return storageInterface;
            }

            final int tempFreeSlots = storageInterface.getFreeSlots();
            if (tempFreeSlots > freeSlots)
            {
                freeSlots = tempFreeSlots;
                emptiestChest = storageInterface;
            }
        }
        return emptiestChest;
    }
}
