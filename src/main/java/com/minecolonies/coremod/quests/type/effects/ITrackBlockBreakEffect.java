package com.minecolonies.coremod.quests.type.effects;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public interface ITrackBlockBreakEffect extends IQuestObjective
{
    /**
     * Callback for blockbreak event
     *
     * @param state  state
     * @param pos    position
     * @param player player entity
     */
    void onBlockBreak(final BlockState state, final BlockPos pos, final Player player);

    /**
     * Save data
     */
    @Override
    default CompoundTag serializeNBT()
    {
        final CompoundTag nbt = new CompoundTag();
        nbt.putInt("count", getBreakCounter());
        return nbt;
    }

    /**
     * Gets the amount of blocks to break
     *
     * @return amount
     */
    int getBreakCounter();

    /**
     * Load data
     *
     * @param nbt compound to read from
     */
    @Override
    default void deserializeNBT(final CompoundTag nbt)
    {
        setBreakCounter(nbt.getInt("count"));
    }

    /**
     * Sets the amount of blocks to break
     *
     * @param count
     */
    void setBreakCounter(int count);
}
