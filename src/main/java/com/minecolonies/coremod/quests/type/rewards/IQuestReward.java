package com.minecolonies.coremod.quests.type.rewards;

import com.minecolonies.coremod.quests.IQuest;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Reward for a certain quest, saves changes
 */
public interface IQuestReward extends INBTSerializable<CompoundNBT>
{
    /**
     * Gets the quest effects ID
     *
     * @return res location id
     */
    ResourceLocation getID();

    /**
     * Applies the reward for the quest/player
     *
     * @param quest
     * @param playerEntity
     */
    void applyReward(final IQuest quest, final PlayerEntity playerEntity);

    /**
     * @param nbt
     */
    default void deserializeNBT(final CompoundNBT nbt)
    {

    }

    /**
     * @return
     */
    default CompoundNBT serializeNBT()
    {
        return new CompoundNBT();
    }
}
