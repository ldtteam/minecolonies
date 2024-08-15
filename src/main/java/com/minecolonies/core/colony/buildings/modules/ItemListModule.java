package com.minecolonies.core.colony.buildings.modules;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IItemListModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.Utils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
     * List of default allowed items.
     */
    private ImmutableList<ItemStorage> defaultValues = ImmutableList.of();

    /**
     * Unique id of this module.
     */
    private final String id;

    /**
     * Construct a new grouped itemlist module with the unique list identifier.
     * @param id the list id.
     */
    public ItemListModule(final String id)
    {
        super();
        this.id = id;
    }

    /**
     * Construct a new grouped itemlist module with the unique list identifier and default values.
     * @param id the list id.
     * @param defaultStacks the default values.
     */
    public ItemListModule(final String id, final ItemStorage...defaultStacks)
    {
        this(id);
        defaultValues = ImmutableList.copyOf(defaultStacks);
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, CompoundTag compound)
    {
        if (compound.contains(id))
        {
            compound = compound.getCompound(id);
        }

        final List<ItemStorage> allowedItems = new ArrayList<>();
        final ListTag filterableList = compound.getList(TAG_ITEMLIST, Tag.TAG_COMPOUND);
        for (int i = 0; i < filterableList.size(); ++i)
        {
            allowedItems.add(new ItemStorage(ItemStack.parseOptional(provider, filterableList.getCompound(i))));
        }

        this.itemsAllowed = ImmutableList.copyOf(allowedItems);
    }

    @Override
    public void serializeNBT(@NotNull final HolderLookup.Provider provider, CompoundTag compound)
    {
        @NotNull final ListTag filteredItems = new ListTag();
        for (@NotNull final ItemStorage item : itemsAllowed)
        {
            filteredItems.add(item.getItemStack().saveOptional(provider));
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
        this.itemsAllowed = ImmutableList.copyOf(defaultValues);
    }

    @Override
    public void serializeToView(@NotNull final RegistryFriendlyByteBuf buf)
    {
        buf.writeInt(itemsAllowed.size());
        for (final ItemStorage item : itemsAllowed)
        {
            Utils.serializeCodecMess(buf, item.getItemStack());
        }
    }

    @Override
    public String getId()
    {
        return this.id;
    }
}
