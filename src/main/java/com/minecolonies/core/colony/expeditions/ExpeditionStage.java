package com.minecolonies.core.colony.expeditions;

import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.api.colony.expeditions.IExpeditionStage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class ExpeditionStage implements IExpeditionStage
{
    @Override
    public List<ItemStack> getRewards()
    {
        return null;
    }

    @Override
    public void addReward(final ItemStack itemStack)
    {

    }

    @Override
    public Map<EntityType<?>, Integer> getKills()
    {
        return null;
    }

    @Override
    public void rewardFound(final EntityType<?> type)
    {

    }

    @Override
    public @Nullable List<IExpeditionMember> getMembersLost()
    {
        return null;
    }

    @Override
    public void memberLost(final IExpeditionMember member)
    {

    }

    @Override
    public void write(final CompoundTag compound)
    {

    }
}