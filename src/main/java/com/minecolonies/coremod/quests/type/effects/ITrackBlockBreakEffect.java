package com.minecolonies.coremod.quests.type.effects;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

public interface ITrackBlockBreakEffect extends IQuestObjective
{
    /**
     * Callback for blockbreak event
     *
     * @param state  state
     * @param pos    position
     * @param player player entity
     */
    void onBlockBreak(final BlockState state, final BlockPos pos, final PlayerEntity player);

    /**
     * Save data
     */
    @Override
    default CompoundNBT serializeNBT()
    {
        final CompoundNBT nbt = new CompoundNBT();
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
    default void deserializeNBT(final CompoundNBT nbt)
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
