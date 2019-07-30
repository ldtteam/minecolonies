package com.minecolonies.coremod.tileentities;

import com.minecolonies.coremod.inventory.InventoryCitizen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.ILootContainer;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public interface ITileEntityWareHouse extends ICapabilitySerializable<NBTTagCompound>, ILockableContainer, ILootContainer, ITickable, ITileEntityColonyBuilding
{
    boolean hasMatchingItemStackInWarehouse(@NotNull Predicate<ItemStack> itemStackSelectionPredicate, int count);

    @NotNull
    List<ItemStack> getMatchingItemStacksInWarehouse(@NotNull Predicate<ItemStack> itemStackSelectionPredicate);

    /**
     * Dump the inventory of a citizen into the warehouse.
     * Go through all items and search the right chest to dump it in.
     *
     * @param inventoryCitizen the inventory of the citizen
     */
    void dumpInventoryIntoWareHouse(@NotNull InventoryCitizen inventoryCitizen);

    World getWorld();
}
