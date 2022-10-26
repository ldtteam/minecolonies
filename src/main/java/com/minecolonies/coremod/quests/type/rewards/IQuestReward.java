package com.minecolonies.coremod.quests.type.rewards;

import com.minecolonies.coremod.quests.IQuest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Reward for a certain quest, saves changes
 */
public interface IQuestReward extends INBTSerializable<CompoundTag>
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
    void applyReward(final IQuest quest, final Player playerEntity);

    /**
     * @param nbt
     */
    default void deserializeNBT(final CompoundTag nbt)
    {

    }

    /**
     * @return
     */
    default CompoundTag serializeNBT()
    {
        return new CompoundTag();
    }
}
