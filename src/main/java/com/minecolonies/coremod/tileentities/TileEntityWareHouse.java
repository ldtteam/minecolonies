package com.minecolonies.coremod.tileentities;

import com.google.common.collect.Lists;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.api.tileentities.AbstractTileEntityWareHouse;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_WAREHOUSE_FULL;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

/**
 * Class which handles the tileEntity of our colonyBuildings.
 */
public class TileEntityWareHouse extends AbstractTileEntityWareHouse
{
    public TileEntityWareHouse()
    {
        super(MinecoloniesTileEntities.WAREHOUSE);
    }

    /**
     * Method used to check if this warehouse holds any of the requested itemstacks.
     *
     * @param itemStackSelectionPredicate The predicate to check with.
     * @return True when the warehouse holds a stack, false when not.
     */
    @Override
    public boolean hasMatchingItemStackInWarehouse(@NotNull final Predicate<ItemStack> itemStackSelectionPredicate, int count)
    {
        final List<ItemStack> targetStacks = getMatchingItemStacksInWarehouse(itemStackSelectionPredicate);
        return targetStacks.stream().mapToInt(ItemStackUtils::getSize).sum() >= count;
    }

    /**
     * Method to get the first matching ItemStack in the Warehouse.
     *
     * @param itemStackSelectionPredicate The predicate to select the ItemStack with.
     * @return The first matching ItemStack.
     */
    @Override
    @NotNull
    public List<ItemStack> getMatchingItemStacksInWarehouse(@NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        if (getBuilding() != null)
        {
            final Set<TileEntity> tileEntities = new HashSet<>();
            tileEntities.add(this);

            return tileEntities.stream()
                     .flatMap(tileEntity -> InventoryUtils.filterProvider(tileEntity, itemStackSelectionPredicate).stream())
                     .filter(itemStacks -> !itemStacks.isEmpty())
              .collect(Collectors.toList());

        }

        return Lists.newArrayList();
    }

    /**
     * Check for a certain item and return the position of the chest containing it.
     *
     * @param itemStackSelectionPredicate the stack to search for.
     * @return the position or null.
     */
    @Nullable
    public BlockPos getPositionOfChestWithItemStack(@NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        if (getBuilding() != null)
        {
            final Set<TileEntity> tileEntities = getBuilding().getAdditionalCountainers().stream().map(pos -> getWorld().getTileEntity(pos)).collect(Collectors.toSet());
            tileEntities.removeIf(Objects::isNull);
            tileEntities.add(this);

            return tileEntities.stream()
                     .filter(tileEntity -> InventoryUtils.hasItemInProvider(tileEntity, itemStackSelectionPredicate))
                     .map(TileEntity::getPos)
                     .findFirst().orElse(null);
        }

        return null;
    }

    /**
     * Dump the inventory of a citizen into the warehouse.
     * Go through all items and search the right chest to dump it in.
     *
     * @param inventoryCitizen the inventory of the citizen
     */
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
                LanguageHandler.sendPlayersMessage(getColony().getMessagePlayerEntitys(), COM_MINECOLONIES_COREMOD_WAREHOUSE_FULL);
                return;
            }
            final IItemHandler handler = chest.getCapability(ITEM_HANDLER_CAPABILITY, null).orElseGet(null);
            if (handler != null)
            {
                InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(inventoryCitizen, i, handler);
            }
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
        if (InventoryUtils.findSlotInProviderNotFullWithItem(this, stack.getItem(), ItemStackUtils.getSize(stack)) != -1)
        {
            return this;
        }

        for (@NotNull final BlockPos pos : getBuilding().getAdditionalCountainers())
        {
            final TileEntity entity = getWorld().getTileEntity(pos);
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
     * @param ignoreDamageValue should the damage value be ignored.
     * @return true if so.
     */
    private static boolean isInRack(final ItemStack stack, final TileEntity entity, final boolean ignoreDamageValue)
    {
        return entity instanceof TileEntityRack && !((AbstractTileEntityRack) entity).isEmpty() && ((AbstractTileEntityRack) entity).hasItemStack(stack, ignoreDamageValue)
                 && InventoryUtils.findSlotInItemHandlerNotFullWithItem(entity.getCapability(ITEM_HANDLER_CAPABILITY, null).orElseGet(null), stack);
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
        for (@NotNull final BlockPos pos : getBuilding().getAdditionalCountainers())
        {
            final TileEntity entity = getWorld().getTileEntity(pos);
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
        for (@NotNull final BlockPos pos : getBuilding().getAdditionalCountainers())
        {
            final TileEntity entity = getWorld().getTileEntity(pos);
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
                tempFreeSlots = ((ChestTileEntity) entity).getSizeInventory() - InventoryUtils.getAmountOfStacksInProvider(entity);
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
