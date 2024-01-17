package com.minecolonies.core.colony.buildings.modules;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IItemListModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.crafting.ItemStorage;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Abstract class for all buildings which require a filterable list of allowed/blocked items.
 */
public class ItemListModule extends AbstractBuildingModule implements IItemListModule, IPersistentModule
{
    /**
     * Tag to store the item list.
     */
    private static final String TAG_ITEMLIST = "itemList";

    /**
     * List of allowed items.
     */
    private ImmutableList<ItemStorage> itemsAllowed = ImmutableList.of();

    /**
     * Unique id of this module.
     */
    private final String id;

    /**
     * Action to take when resetting to defaults.
     */
    private Consumer<ItemListModule> resetToDefaultsAction;

    /**
     * Construct a new grouped itemlist module with the unique list identifier.
     * @param id the list id.
     */
    public ItemListModule(final String id)
    {
        super();
        this.id = id;
        this.resetToDefaultsAction = ItemListModule::clearItems;
    }

    /**
     * Change the action taken when resetting the list to defaults.
     * (Intended to be called as a fluent builder.)
     *
     * @param action The action.
     */
    public ItemListModule onResetToDefaults(final Consumer<ItemListModule> action)
    {
        this.resetToDefaultsAction = action;
        resetToDefaults();      // also actually call it for first-build
        return this;
    }

    @Override
    public void deserializeNBT(CompoundTag compound)
    {
        if (compound.contains(id))
        {
            compound = compound.getCompound(id);
        }

        final List<ItemStorage> allowedItems = new ArrayList<>();
            final ListTag filterableList = compound.getList(TAG_ITEMLIST, Tag.TAG_COMPOUND);
            for (int i = 0; i < filterableList.size(); ++i)
            {
                allowedItems.add(new ItemStorage(ItemStack.of(filterableList.getCompound(i))));
            }

        this.itemsAllowed = ImmutableList.copyOf(allowedItems);
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        @NotNull final ListTag filteredItems = new ListTag();
        for (@NotNull final ItemStorage item : itemsAllowed)
        {
            @NotNull final CompoundTag itemCompound = new CompoundTag();
            item.getItemStack().save(itemCompound);
            filteredItems.add(itemCompound);
        }
        compound.put(TAG_ITEMLIST, filteredItems);
    }

    @Override
    public void addItem(final ItemStorage item)
    {
        if (!itemsAllowed.contains(item))
        {
            this.itemsAllowed = ImmutableList.<ItemStorage>builder().addAll(itemsAllowed).add(item).build();
            markDirty();
        }
    }

    @Override
    public boolean isItemInList(final ItemStorage item)
    {
        return itemsAllowed.contains(item);
    }

    @Override
    public void removeItem(final ItemStorage item)
    {
        final List<ItemStorage> allowedItems = new ArrayList<>(itemsAllowed);
        allowedItems.remove(item);
        this.itemsAllowed = ImmutableList.copyOf(allowedItems);
        markDirty();
    }

    @Override
    public ImmutableList<ItemStorage> getList()
    {
        return itemsAllowed;
    }

    @Override
    public String getListIdentifier()
    {
        return this.id;
    }

    @Override
    public void clearItems()
    {
        itemsAllowed = ImmutableList.of();
        markDirty();
    }

    @Override
    public void resetToDefaults()
    {
        this.resetToDefaultsAction.accept(this);
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(itemsAllowed.size());
        for (final ItemStorage item : itemsAllowed)
        {
            buf.writeItem(item.getItemStack());
        }
    }

    @Override
    public String getId()
    {
        return this.id;
    }
}
