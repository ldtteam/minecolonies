package com.minecolonies.core.colony.buildings.modules.expedition;

import com.google.common.base.Enums;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This keeps track of an "expedition", which can be any worker that wanders off somewhere and gathers and fights.
 * It keeps track of the worker's health (and potentially other interesting stats), whatever equipment they're using
 * on the job, what mobs they're interacting with (fighting or otherwise), and any resources they've gathered along
 * the way.  It is intended to be updated "live" while the expedition is in progress and then persist the final state
 * until the next expedition begins.
 */
public class ExpeditionLog
{
    private static final String TAG_STATUS = "status";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_STATS = "stats";
    private static final String TAG_EQUIPMENT = "equip";
    private static final String TAG_MOBS = "mobs";
    private static final String TAG_TYPE = "type";
    private static final String TAG_COUNT = "count";
    private static final String TAG_LOOT = "loot";

    public enum Status
    {
        NONE,
        STARTING,
        IN_PROGRESS,
        RETURNING_HOME,
        COMPLETED,
        KILLED
    }

    // it would be nice to have a more generic way to store these, but they're all split between Data and Attributes
    // and CitizenData and not all of those have names for consistent persistence, or will reliably persist the
    // same values as captured and not more recent values :(
    public enum StatType
    {
        HEALTH,
        SATURATION,
    }

    private Status status = Status.NONE;
    private int id;
    private String name;
    private Map<StatType, Double> stats = new HashMap<>();
    private List<ItemStack> equipment = new ArrayList<>();
    private Map<EntityType<?>, Integer> mobs = new HashMap<>();
    private Map<ItemStorage, ItemStorage> loot = new HashMap<>();

    /**
     * Resets the expedition log to prepare to start a new expedition.
     * Only call this when the next expedition is actually about to begin, so that the player
     * can see the results of the last expedition for as long as possible.
     */
    public void reset()
    {
        this.status = Status.NONE;
        this.id = 0;
        this.name = null;
        this.stats.clear();
        this.equipment = Collections.emptyList();
        this.mobs.clear();
        this.loot.clear();
    }

    /**
     * Reports the current status of the expedition
     * @return the current expedition status
     */
    public Status getStatus()
    {
        return this.status;
    }

    /**
     * Sets the current status of the expedition
     * @param status the new expedition status
     */
    public void setStatus(final Status status)
    {
        this.status = status;
    }

    /**
     * Reports the id of the citizen who is on this expedition, if any.
     * @return the id, or 0 if there is no citizen or the citizen was killed.
     */
    public int getId()
    {
        return this.id;
    }

    /**
     * Reports the name of the citizen who is (or was) on this expedition, if any.
     * @return the name, or null if there is nobody.  (does not reset when killed)
     */
    @Nullable
    public String getName()
    {
        return this.name;
    }

    /**
     * Reports the stats of the citizen.
     * @param stat the stat to retrieve
     * @return the value of that stat (as of the latest update, not necessarily "live")
     */
    public double getStat(final StatType stat)
    {
        return this.stats.getOrDefault(stat, 0.0);
    }

    /**
     * Captures and updates the stats and other info for the citizen currently on the expedition.
     * @param citizen the citizen who is currently on the expedition
     */
    public void setCitizen(@Nullable final AbstractEntityCitizen citizen)
    {
        if (citizen == null)
        {
            this.id = 0;
            this.name = null;
            this.stats.clear();
        }
        else
        {
            this.id = citizen.getId();
            this.name = citizen.getCitizenData().getName();
            this.stats.put(StatType.HEALTH, (double) citizen.getHealth());
            this.stats.put(StatType.SATURATION, citizen.getCitizenData().getSaturation());
        }
    }

    /**
     * Indicates that the citizen was killed while on the expedition (which ends it).
     */
    public void setKilled()
    {
        this.id = 0;
        this.status = Status.KILLED;
    }

    /**
     * Reports the list of equipment currently in use on this expedition.
     * @return the equipment list.  Some stacks might be empty.
     */
    public List<ItemStack> getEquipment()
    {
        return this.equipment;
    }

    /**
     * Captures and sets the list of equipment currently in use on this expedition.
     * Some stacks may be empty to indicate that there is no equipment in that "slot".
     * It's up to the actual expedition to decide what equipment is interesting to show.
     * @param equipment the list of equipment
     */
    public void setEquipment(@NotNull final List<ItemStack> equipment)
    {
        this.equipment = equipment.stream()
                .map(ItemStack::copy)
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * Reports the list and count of mobs interacted with (usually fought, but doesn't have to be) during
     * this expedition.
     * @return the list of mobs and counts, sorted highest-count-first
     */
    public List<Tuple<EntityType<?>, Integer>> getMobs()
    {
        return this.mobs.entrySet().stream()
                .map(entry -> new Tuple<EntityType<?>, Integer>(entry.getKey(), entry.getValue()))
                .sorted(Comparator.<Tuple<EntityType<?>, Integer>>comparingInt(Tuple::getB).reversed())
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * Adds a mob to the interaction list of this expedition.
     * @param mobType the type of mob
     */
    public void addMob(@NotNull final EntityType<?> mobType)
    {
        this.mobs.merge(mobType, 1, Integer::sum);
    }

    /**
     * Reports the resources gathered so far during this expedition.
     * @return the list of resources, sorted highest-amount-first
     */
    public List<ItemStorage> getLoot()
    {
        return this.loot.keySet().stream()
                .sorted(Comparator.comparing(ItemStorage::getAmount).reversed())
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * Adds a list of resources to this expedition.
     * @param loot the additional resources gathered this round
     */
    public void addLoot(@NotNull final List<ItemStack> loot)
    {
        for (final ItemStack stack : loot)
        {
            ItemStorage storage = new ItemStorage(stack);
            this.loot.merge(storage, storage, (o, n) ->
            {
                o.setAmount(o.getAmount() + n.getAmount());
                return o;
            });
        }
    }

    /**
     * Save to NBT
     * @param compound target
     */
    public void serializeNBT(@NotNull final CompoundTag compound)
    {
        compound.putString(TAG_STATUS, this.status.name());
        compound.putInt(TAG_ID, this.id);
        compound.putString(TAG_NAME, this.name == null ? "" : this.name);

        final CompoundTag stats = new CompoundTag();
        for (final Map.Entry<StatType, Double> entry : this.stats.entrySet())
        {
            stats.putDouble(entry.getKey().name().toLowerCase(Locale.US), entry.getValue());
        }
        compound.put(TAG_STATS, stats);

        final ListTag equipment = new ListTag();
        for (final ItemStack stack : this.equipment)
        {
            equipment.add(stack.save(new CompoundTag()));
        }
        compound.put(TAG_EQUIPMENT, equipment);

        final ListTag mobs = new ListTag();
        for (final Map.Entry<EntityType<?>, Integer> entry : this.mobs.entrySet())
        {
            final CompoundTag mob = new CompoundTag();
            mob.putString(TAG_TYPE, BuiltInRegistries.ENTITY_TYPE.getKey(entry.getKey()).toString());
            mob.putInt(TAG_COUNT, entry.getValue());
            mobs.add(mob);
        }
        compound.put(TAG_MOBS, mobs);

        final ListTag loot = new ListTag();
        for (final ItemStorage storage : this.loot.values())
        {
            loot.add(StandardFactoryController.getInstance().serialize(storage));
        }
        compound.put(TAG_LOOT, loot);
    }

    /**
     * Reload from NBT
     * @param compound source
     */
    public void deserializeNBT(@NotNull final CompoundTag compound)
    {
        this.status = Enums.getIfPresent(Status.class, compound.getString(TAG_STATUS)).or(Status.NONE);
        this.id = compound.getInt(TAG_ID);
        this.name = compound.getString(TAG_NAME);
        if (this.name.isEmpty()) this.name = null;

        this.stats.clear();
        final CompoundTag stats = compound.getCompound(TAG_STATS);
        for (final StatType stat : StatType.values())
        {
            final String key = stat.name().toLowerCase(Locale.US);
            if (stats.contains(key))
            {
                this.stats.put(stat, stats.getDouble(key));
            }
        }

        this.equipment.clear();
        final ListTag equipment = compound.getList(TAG_EQUIPMENT, Tag.TAG_COMPOUND);
        for (int i = 0; i < equipment.size(); i++)
        {
            this.equipment.add(ItemStack.of(equipment.getCompound(i)));
        }

        this.mobs.clear();
        final ListTag mobs = compound.getList(TAG_MOBS, Tag.TAG_COMPOUND);
        for (int i = 0; i < mobs.size(); ++i)
        {
            final CompoundTag mob = mobs.getCompound(i);
            final ResourceLocation type = new ResourceLocation(mob.getString(TAG_TYPE));
            final EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(type);
            if (entityType != null)
            {
                this.mobs.put(entityType, mob.getInt(TAG_COUNT));
            }
        }

        this.loot.clear();
        final ListTag loot = compound.getList(TAG_LOOT, Tag.TAG_COMPOUND);
        for (int i = 0; i < loot.size(); i++)
        {
            final ItemStorage storage = StandardFactoryController.getInstance().deserializeTag(loot.getCompound(i));
            this.loot.put(storage, storage);
        }
    }

    /**
     * Save to network
     * @param buf target
     */
    public void serialize(@NotNull final RegistryFriendlyByteBuf buf)
    {
        buf.writeVarInt(this.status.ordinal());
        buf.writeVarInt(this.id);
        buf.writeUtf(this.name == null ? "" : this.name);

        for (final StatType stat : StatType.values())
        {
            buf.writeDouble(this.stats.getOrDefault(stat, 0.0));
        }

        buf.writeVarInt(this.equipment.size());
        for (final ItemStack stack : this.equipment)
        {
            Utils.serializeCodecMess(buf, stack);
        }

        buf.writeVarInt(this.mobs.size());
        for (final Map.Entry<EntityType<?>, Integer> entry : this.mobs.entrySet())
        {
            buf.writeById(BuiltInRegistries.ENTITY_TYPE::getIdOrThrow, entry.getKey());
            buf.writeVarInt(entry.getValue());
        }

        buf.writeVarInt(this.loot.size());
        for (final ItemStorage storage : this.loot.values())
        {
            StandardFactoryController.getInstance().serializeTag(buf, storage);
        }
    }

    /**
     * Reload from network
     * @param buf source
     */
    public void deserialize(@NotNull final RegistryFriendlyByteBuf buf)
    {
        this.status = Status.values()[buf.readVarInt()];
        this.id = buf.readVarInt();
        this.name = buf.readUtf();
        if (this.name.isEmpty()) this.name = null;

        this.stats.clear();
        for (final StatType stat : StatType.values())
        {
            this.stats.put(stat, buf.readDouble());
        }

        this.equipment.clear();
        for (int size = buf.readVarInt(); size > 0; --size)
        {
            this.equipment.add(Utils.deserializeCodecMess(buf));
        }

        this.mobs.clear();
        for (int size = buf.readVarInt(); size > 0; --size)
        {
            final EntityType<?> entityType = buf.readById(BuiltInRegistries.ENTITY_TYPE::byIdOrThrow);
            final int count = buf.readVarInt();
            if (entityType != null)
            {
                this.mobs.put(entityType, count);
            }
        }

        this.loot.clear();
        for (int size = buf.readVarInt(); size > 0; --size)
        {
            final ItemStorage storage = StandardFactoryController.getInstance().deserializeTag(buf);
            this.loot.put(storage, storage);
        }
    }
}
