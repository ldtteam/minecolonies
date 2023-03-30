package com.minecolonies.coremod.quests.objectives;

import com.minecolonies.api.quests.IQuestActionObjective;
import com.minecolonies.api.quests.IQuestObjective;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public interface IMineBlockObjective extends IQuestActionObjective
{
    /**
     * Callback for blockbreak event
     *
     * @param state  state
     * @param pos    position
     * @param player player entity
     */
    void onBlockBreak(final BlockState state, final BlockPos pos, final Player player);
}
