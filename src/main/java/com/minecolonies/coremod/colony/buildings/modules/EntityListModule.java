package com.minecolonies.coremod.colony.buildings.modules;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IEntityListModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;
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
    public void deserializeNBT(final CompoundNBT compound)
    {
        final ListNBT filterableList = compound.getCompound(id).getList(TAG_MOBLIST, Constants.NBT.TAG_STRING);
        for (int i = 0; i < filterableList.size(); ++i)
        {
            final ResourceLocation res = new ResourceLocation(filterableList.getString(i));
            if (ForgeRegistries.ENTITIES.containsKey(res))
            {
                mobsAllowed.add(res);
            }
        }
    }

    @Override
    public void serializeNBT(final CompoundNBT compound)
    {
        final CompoundNBT moduleCompound = new CompoundNBT();
        @NotNull final ListNBT filteredMobs = new ListNBT();
        for (@NotNull final ResourceLocation mob : mobsAllowed)
        {
            filteredMobs.add(StringNBT.valueOf(mob.toString()));
        }
        moduleCompound.put(TAG_MOBLIST, filteredMobs);
        compound.put(id, moduleCompound);
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
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(mobsAllowed.size());
        for (final ResourceLocation entity : mobsAllowed)
        {
            buf.writeRegistryIdUnsafe(ForgeRegistries.ENTITIES, entity);
        }
    }

    @Override
    public String getId()
    {
        return this.id;
    }
}
