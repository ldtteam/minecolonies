package com.minecolonies.core.colony.expeditions;

import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.api.colony.expeditions.IExpeditionStage;
import com.minecolonies.api.colony.expeditions.MobKill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ExpeditionStage implements IExpeditionStage
{
    /**
     * NBT Tags.
     */
    private static final String TAG_HEADER       = "header";
    private static final String TAG_REWARDS      = "rewards";
    private static final String TAG_KILLS        = "kills";
    private static final String TAG_MEMBERS_LOST = "membersLost";

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
    private final Map<EntityType<?>, Integer> kills;

    /**
     * The list of members that died.
     */
    private final List<IExpeditionMember<?>> membersLost;

    /**
     * A cache for the rewards so the list doesn't have to be recalculated each time.
     */
    private List<ItemStack> cachedRewards;

    /**
     * A cache for the kills so the list doesn't have to be recalculated each time.
     */
    private List<MobKill> cachedKills;

    public ExpeditionStage(final Component header)
    {
        this.header = header;
        this.rewards = new HashMap<>();
        this.kills = new HashMap<>();
        this.membersLost = new ArrayList<>();
    }

    /**
     * Create an expedition stage instance from compound data.
     *
     * @param compound the compound data.
     * @return the expedition instance.
     */
    @NotNull
    public static Expedition loadFromNBT(final CompoundTag compound)
    {


        return new ExpeditionStage(equipment, members, status);
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
        rewards.putIfAbsent(itemStack.getItem(), 0);
        rewards.put(itemStack.getItem(), rewards.get(itemStack.getItem()) + itemStack.getCount());
        cachedRewards.clear();
    }

    /**
     * Get a map of mobs killed during this expedition stage, entries contain the mob type and their amount killed.
     *
     * @return the list of kills.
     */
    @Override
    @NotNull
    public List<MobKill> getKills()
    {
        if (cachedKills == null)
        {
            cachedKills = kills.entrySet().stream().map(entry -> new MobKill(entry.getKey(), entry.getValue())).toList();
        }
        return cachedKills;
    }

    @Override
    public void addKill(final EntityType<?> type)
    {
        kills.putIfAbsent(type, 0);
        kills.put(type, kills.get(type) + 1);
        cachedKills.clear();
    }

    @Override
    @NotNull
    public List<IExpeditionMember<?>> getMembersLost()
    {
        return membersLost;
    }

    @Override
    public void memberLost(final IExpeditionMember<?> member)
    {
        membersLost.add(member);
    }

    @Override
    public void write(final CompoundTag compound)
    {
        compound.putString(TAG_HEADER, Component.Serializer.toJson(header));
    }

    @Override
    public void deserializeNBT(final CompoundTag compoundTag)
    {
        header = Component.Serializer.fromJson(compoundTag.getString(TAG_HEADER));
    }
}