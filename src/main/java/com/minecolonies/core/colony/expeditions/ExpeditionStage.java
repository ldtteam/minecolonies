package com.minecolonies.core.colony.expeditions;

import com.minecolonies.api.colony.expeditions.MobKill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
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
    private static final String TAG_HEADER            = "header";
    private static final String TAG_REWARDS           = "rewards";
    private static final String TAG_REWARD_ITEM       = "item";
    private static final String TAG_REWARD_COUNT      = "count";
    private static final String TAG_KILLS             = "kills";
    private static final String TAG_KILL_ENCOUNTER_ID = "encounterId";
    private static final String TAG_KILL_COUNT        = "count";
    private static final String TAG_MEMBERS_LOST      = "membersLost";

    /**
     * The header for this stage.
     */
    private final Component header;

    /**
     * The map of rewards.
     */
    private final Map<Item, Integer> rewards;

    /**
     * The map of kills.
     */
    private final Map<ResourceLocation, Integer> kills;

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
    public ExpeditionStage(final Component header, final Map<Item, Integer> rewards, final Map<ResourceLocation, Integer> kills, final List<Integer> membersLost)
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

        final Map<Item, Integer> rewards = new HashMap<>();
        final ListTag rewardsList = compound.getList(TAG_REWARDS, Tag.TAG_COMPOUND);
        for (int i = 0; i < rewardsList.size(); ++i)
        {
            final CompoundTag rewardCompound = rewardsList.getCompound(i);
            final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(rewardCompound.getString(TAG_REWARD_ITEM)));
            if (item == null)
            {
                continue;
            }

            rewards.put(item, rewardCompound.getInt(TAG_REWARD_COUNT));
        }

        final Map<ResourceLocation, Integer> kills = new HashMap<>();
        final ListTag killsList = compound.getList(TAG_KILLS, Tag.TAG_COMPOUND);
        for (int i = 0; i < killsList.size(); ++i)
        {
            final CompoundTag killCompound = killsList.getCompound(i);
            final ResourceLocation encounterId = new ResourceLocation(killCompound.getString(TAG_KILL_ENCOUNTER_ID));
            kills.put(encounterId, killCompound.getInt(TAG_KILL_COUNT));
        }

        final List<Integer> membersLost = IntStream.of(compound.getIntArray(TAG_MEMBERS_LOST)).boxed().toList();

        return new ExpeditionStage(header, rewards, kills, membersLost);
    }

    /**
     * Get the header for this stage.
     *
     * @return the component.
     */
    public Component getHeader()
    {
        return header;
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
            cachedRewards = rewards.entrySet().stream().map(entry -> new ItemStack(entry.getKey(), entry.getValue())).toList();
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
        if (itemStack.isEmpty())
        {
            return;
        }

        rewards.putIfAbsent(itemStack.getItem(), 0);
        rewards.put(itemStack.getItem(), rewards.get(itemStack.getItem()) + itemStack.getCount());
        cachedRewards = null;
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
     * @param encounterId the encounter type that got killed.
     */
    public void addKill(final ResourceLocation encounterId)
    {
        kills.putIfAbsent(encounterId, 0);
        kills.put(encounterId, kills.get(encounterId) + 1);
        cachedKills = null;
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
        for (final Entry<Item, Integer> rewardEntry : rewards.entrySet())
        {
            final CompoundTag rewardCompound = new CompoundTag();
            rewardCompound.putString(TAG_REWARD_ITEM, ForgeRegistries.ITEMS.getKey(rewardEntry.getKey()).toString());
            rewardCompound.putInt(TAG_REWARD_COUNT, rewardEntry.getValue());
            rewardsList.add(rewardCompound);
        }
        compound.put(TAG_REWARDS, rewardsList);

        final ListTag killsList = new ListTag();
        for (final Entry<ResourceLocation, Integer> killEntry : kills.entrySet())
        {
            final CompoundTag killCompound = new CompoundTag();
            killCompound.putString(TAG_KILL_ENCOUNTER_ID, killEntry.getKey().toString());
            killCompound.putInt(TAG_KILL_COUNT, killEntry.getValue());
            killsList.add(killCompound);
        }
        compound.put(TAG_KILLS, killsList);

        compound.putIntArray(TAG_MEMBERS_LOST, membersLost);
    }
}