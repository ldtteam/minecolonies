package com.minecolonies.coremod.tileentities;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.inventory.InventoryCitizen;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_WAREHOUSE_FULL;

/**
 * Class which handles the tileEntity of our colonyBuildings.
 */
public class TileEntityWareHouse extends TileEntityColonyBuilding
{

    /**
     * Method used to check if this warehouse holds any of the requested itemstacks.
     *
     * @param itemStackSelectionPredicate The predicate to check with.
     * @return True when the warehouse holds a stack, false when not.
     */
    public boolean hasMatchinItemStackInWarehouse(@NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        return !ItemStackUtils.isEmpty(getFirstMatchingItemStackInWarehouse(itemStackSelectionPredicate));
    }

    /**
     * Method to get the first matching ItemStack in the Warehouse.
     *
     * @param itemStackSelectionPredicate The predicate to select the ItemStack with.
     * @return The first matching ItemStack.
     */
    @Nullable
    public ItemStack getFirstMatchingItemStackInWarehouse(@NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        if (getBuilding() != null)
        {
            Set<TileEntity> tileEntities = getBuilding().getAdditionalCountainers().stream().map(pos -> getWorld().getTileEntity(pos)).collect(Collectors.toSet());
            tileEntities.removeIf(Objects::isNull);
            tileEntities.add(this);

            return tileEntities.stream()
                     .map(tileEntity -> InventoryUtils.filterProvider(tileEntity, itemStackSelectionPredicate))
                     .filter(itemStacks -> !itemStacks.isEmpty())
                     .map(itemStacks -> itemStacks.get(0))
                     .findFirst().orElse(ItemStackUtils.EMPTY);
        }

        return ItemStackUtils.EMPTY;
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
            Set<TileEntity> tileEntities = getBuilding().getAdditionalCountainers().stream().map(pos -> getWorld().getTileEntity(pos)).collect(Collectors.toSet());
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
    public void dumpInventoryIntoWareHouse(@NotNull final InventoryCitizen inventoryCitizen)
    {
        for (int i = 0; i < new InvWrapper(inventoryCitizen).getSlots(); i++)
        {
            final ItemStack stack = inventoryCitizen.getStackInSlot(i);
            if (ItemStackUtils.isEmpty(stack))
            {
                continue;
            }
            @Nullable final TileEntity chest = searchRightChestForStack(stack);
            if (chest == null)
            {
                LanguageHandler.sendPlayersMessage(getColony().getMessageEntityPlayers(), COM_MINECOLONIES_COREMOD_WAREHOUSE_FULL);
                return;
            }
            InventoryUtils.transferItemStackIntoNextFreeSlotInProvider(new InvWrapper(inventoryCitizen), i, chest);
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
        if (InventoryUtils.findSlotInProviderNotFullWithItem(this, stack.getItem(), stack.getItemDamage(), ItemStackUtils.getSize(stack)) != -1)
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
        return entity instanceof TileEntityRack && !((TileEntityRack) entity).isEmpty() && ((TileEntityRack) entity).hasItemStack(stack, ignoreDamageValue)
                 && InventoryUtils.findSlotInProviderNotFullWithItem(entity, stack.getItem(), ignoreDamageValue ? -1 : stack.getItemDamage(), ItemStackUtils.getSize(stack)) != -1;
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
        return entity instanceof TileEntityChest
                 && InventoryUtils.findSlotInProviderNotFullWithItem(entity, stack.getItem(), ignoreDamageValue ? -1 : stack.getItemDamage(), ItemStackUtils.getSize(stack)) != -1;
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
                if (((TileEntityRack) entity).isEmpty())
                {
                    return entity;
                }

                tempFreeSlots = ((TileEntityRack) entity).getFreeSlots();
                if (freeSlots < tempFreeSlots)
                {
                    freeSlots = tempFreeSlots;
                    emptiestChest = entity;
                }
            }
            else if (entity instanceof TileEntityChest && InventoryUtils.getFirstOpenSlotFromProvider(entity) != -1)
            {
                tempFreeSlots = ((TileEntityChest) entity).getSizeInventory() - InventoryUtils.getAmountOfStacksInProvider(entity);
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
