package com.minecolonies.core.colony.buildings.modules;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IEntityListModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Class for all buildings that need a list of mobs to toggle for various reasons.
 */
public class EntityListModule extends AbstractBuildingModule implements IEntityListModule, IPersistentModule
{
    /**
     * Tag to store the mob list.
     */
    private static final String TAG_MOBLIST = "newmoblist";

    /**
     * List of allowed items.
     */
    private final Set<ResourceLocation> mobsAllowed = new HashSet<>();

    /**
     * Unique id of this module.
     */
    private final String id;

    /**
     * Construct a new grouped itemlist module with the unique list identifier.
     * @param id the list id.
     */
    public EntityListModule(final String id)
    {
        super();
        this.id = id;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, CompoundTag compound)
    {
        if (compound.contains(id))
        {
            compound = compound.getCompound(id);
        }

        final ListTag filterableList = compound.getList(TAG_MOBLIST, Tag.TAG_STRING);
        for (int i = 0; i < filterableList.size(); ++i)
        {
            final ResourceLocation res = ResourceLocation.parse(filterableList.getString(i));
            if (BuiltInRegistries.ENTITY_TYPE.containsKey(res))
            {
                mobsAllowed.add(res);
            }
        }
    }

    @Override
    public void serializeNBT(@NotNull final HolderLookup.Provider provider, CompoundTag compound)
    {
        @NotNull final ListTag filteredMobs = new ListTag();
        for (@NotNull final ResourceLocation mob : mobsAllowed)
        {
            filteredMobs.add(StringTag.valueOf(mob.toString()));
        }
        compound.put(TAG_MOBLIST, filteredMobs);
    }

    @Override
    public void addEntity(final ResourceLocation item)
    {
        mobsAllowed.add(item);
        markDirty();
    }

    @Override
    public boolean isEntityInList(final ResourceLocation entity)
    {
        return mobsAllowed.contains(entity);
    }

    @Override
    public void removeEntity(final ResourceLocation item)
    {
        mobsAllowed.remove(item);
        markDirty();
    }

    @Override
    public ImmutableList<ResourceLocation> getList()
    {
        return ImmutableList.copyOf(mobsAllowed);
    }

    @Override
    public String getListIdentifier()
    {
        return this.id;
    }

    @Override
    public void serializeToView(@NotNull final RegistryFriendlyByteBuf buf)
    {
        buf.writeInt(mobsAllowed.size());
        for (final ResourceLocation entity : mobsAllowed)
        {
            buf.writeResourceLocation(entity);
        }
    }

    @Override
    public String getId()
    {
        return this.id;
    }
}
