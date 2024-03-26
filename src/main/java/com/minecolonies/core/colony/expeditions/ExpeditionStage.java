package com.minecolonies.core.colony.expeditions;

import com.minecolonies.api.colony.expeditions.MobKill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

/**
 * Implementation for an expedition stage, an expedition can contain multiple stages, each with its own rewards/unlocks.
 */
public final class ExpeditionStage
{
    /**
     * NBT Tags.
     */
    private static final String TAG_HEADER           = "header";
    private static final String TAG_REWARDS          = "rewards";
    private static final String TAG_REWARD_ITEM      = "item";
    private static final String TAG_REWARD_COUNT     = "count";
    private static final String TAG_KILLS            = "kills";
    private static final String TAG_KILL_ENTITY_TYPE = "entityType";
    private static final String TAG_KILL_COUNT       = "count";
    private static final String TAG_MEMBERS_LOST     = "membersLost";

    /**
     * The header for this stage.
     */
    private final Component header;

    /**
     * The map of rewards.
     */
    private final Map<ItemStack, Integer> rewards;

    /**
     * The map of kills.
     */
    private final Map<EntityType<?>, Integer> kills;

    /**
     * The list of members that died.
     */
    private final List<Integer> membersLost;

    /**
     * A cache for the rewards so the list doesn't have to be recalculated each time.
     */
    private List<ItemStack> cachedRewards;

    /**
     * A cache for the kills so the list doesn't have to be recalculated each time.
     */
    private List<MobKill> cachedKills;

    /**
     * Default constructor.
     *
     * @param header the header for this stage.
     */
    public ExpeditionStage(final Component header)
    {
        this(header, new HashMap<>(), new HashMap<>(), new ArrayList<>());
    }

    /**
     * Deserialization constructor.
     *
     * @param header      the header for this stage.
     * @param rewards     the map of rewards.
     * @param kills       the map of kills.
     * @param membersLost the list of members that died.
     */
    public ExpeditionStage(final Component header, final Map<ItemStack, Integer> rewards, final Map<EntityType<?>, Integer> kills, final List<Integer> membersLost)
    {
        this.header = header;
        this.rewards = new HashMap<>(rewards);
        this.kills = new HashMap<>(kills);
        this.membersLost = new ArrayList<>(membersLost);
    }

    /**
     * Create an expedition stage instance from compound data.
     *
     * @param compound the compound data.
     * @return the expedition instance.
     */
    @NotNull
    public static ExpeditionStage loadFromNBT(final CompoundTag compound)
    {
        final Component header = Component.Serializer.fromJson(compound.getString(TAG_HEADER));

        final Map<ItemStack, Integer> rewards = new HashMap<>();
        final ListTag rewardsList = compound.getList(TAG_REWARDS, Tag.TAG_COMPOUND);
        for (int i = 0; i < rewardsList.size(); ++i)
        {
            final CompoundTag rewardCompound = rewardsList.getCompound(i);
            final ItemStack itemStack = ItemStack.of(rewardCompound.getCompound(TAG_REWARD_ITEM));
            if (itemStack.equals(ItemStack.EMPTY))
            {
                continue;
            }

            rewards.put(itemStack, rewardCompound.getInt(TAG_REWARD_COUNT));
        }

        final Map<EntityType<?>, Integer> kills = new HashMap<>();
        final ListTag killsList = compound.getList(TAG_KILLS, Tag.TAG_COMPOUND);
        for (int i = 0; i < killsList.size(); ++i)
        {
            final CompoundTag killCompound = killsList.getCompound(i);
            final ResourceLocation entityTypeId = new ResourceLocation(killCompound.getString(TAG_KILL_ENTITY_TYPE));
            final EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityTypeId);
            if (entityType == null)
            {
                continue;
            }

            kills.put(entityType, killCompound.getInt(TAG_KILL_COUNT));
        }

        final List<Integer> membersLost = IntStream.of(compound.getIntArray(TAG_MEMBERS_LOST)).boxed().toList();

        return new ExpeditionStage(header, rewards, kills, membersLost);
    }

    /**
     * A list of items obtained during this expedition stage.
     * Note: Adventure tokens are mixed raw into this list. Parsing them is up to implementation to handle.
     *
     * @return the list of items.
     */
    @NotNull
    public List<ItemStack> getRewards()
    {
        if (cachedRewards == null)
        {
            cachedRewards = rewards.entrySet().stream().map(entry -> entry.getKey().copyWithCount(entry.getValue())).toList();
        }
        return cachedRewards;
    }

    /**
     * Adds a reward to this stage.
     *
     * @param itemStack the item to add to the stage.
     */
    public void addReward(final ItemStack itemStack)
    {
        final ItemStack identifier = itemStack.copyWithCount(1);
        rewards.putIfAbsent(identifier, 0);
        rewards.put(identifier, rewards.get(identifier) + itemStack.getCount());
        cachedRewards.clear();
    }

    /**
     * Get a map of mobs killed during this expedition stage, entries contain the mob type and their amount killed.
     *
     * @return the list of kills.
     */
    @NotNull
    public List<MobKill> getKills()
    {
        if (cachedKills == null)
        {
            cachedKills = kills.entrySet().stream().map(entry -> new MobKill(entry.getKey(), entry.getValue())).toList();
        }
        return cachedKills;
    }

    /**
     * Adds a kill to this stage.
     *
     * @param type the entity type that got killed.
     */
    public void addKill(final EntityType<?> type)
    {
        kills.putIfAbsent(type, 0);
        kills.put(type, kills.get(type) + 1);
        cachedKills.clear();
    }

    /**
     * Get a members instance of what members were lost during this stage.
     *
     * @return which members were lost during this part of the expedition.
     */
    @NotNull
    public List<Integer> getMembersLost()
    {
        return membersLost;
    }

    /**
     * Adds a member that got lost during this stage.
     *
     * @param memberId the id of the member that was lost.
     */
    public void memberLost(final int memberId)
    {
        membersLost.add(memberId);
    }

    /**
     * Write this stage to compound data.
     *
     * @param compound the compound tag.
     */
    public void write(final CompoundTag compound)
    {
        compound.putString(TAG_HEADER, Component.Serializer.toJson(header));

        final ListTag rewardsList = new ListTag();
        for (final Entry<ItemStack, Integer> rewardEntry : rewards.entrySet())
        {
            final CompoundTag rewardCompound = new CompoundTag();
            rewardCompound.put(TAG_REWARD_ITEM, rewardEntry.getKey().serializeNBT());
            rewardCompound.putInt(TAG_REWARD_COUNT, rewardEntry.getValue());
            rewardsList.add(rewardCompound);
        }
        compound.put(TAG_REWARDS, rewardsList);

        final ListTag killsList = new ListTag();
        for (final Entry<EntityType<?>, Integer> killEntry : kills.entrySet())
        {
            final CompoundTag killCompound = new CompoundTag();
            killCompound.putString(TAG_KILL_ENTITY_TYPE, ForgeRegistries.ENTITY_TYPES.getKey(killEntry.getKey()).toString());
            killCompound.putInt(TAG_KILL_COUNT, killEntry.getValue());
            killsList.add(killCompound);
        }
        compound.put(TAG_KILLS, killsList);

        compound.putIntArray(TAG_MEMBERS_LOST, membersLost);
    }
}