package com.minecolonies.coremod.quests.objectives;

import com.minecolonies.api.quests.IColonyQuest;
import com.minecolonies.api.quests.IDialogueObjective;
import com.minecolonies.api.quests.IObjectiveData;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.quests.ColonyQuest;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MineBlockObjective extends DialogueObjective implements IMineBlockObjective
{
    /**
     * Amount of blocks to mine.
     */
    private int blocksToMine;

    /**
     * The block to mine.
     */
    private Block blockToMine;

    public MineBlockObjective(final int target, final int blocksToMine, final Block blockToMine)
    {
        super(target, null);
        this.blocksToMine = blocksToMine;
        this.blockToMine = blockToMine;
    }

    @Override
    public IObjectiveData init(final IColonyQuest colonyQuest)
    {
        super.init(colonyQuest);
        //todo add to an event listener that tracks a <Player-Quest, Block, Map> This should also now the colony quest
        return new ObjectiveData();
    }

    @Override
    public void onAbort()
    {
        //todo cleanup the event listener we added.
    }

    @Override
    public void onBlockBreak(final ColonyQuest colonyQuest)
    {
        //todo gets colony quest as input and thats all we need, we increment quest objective then until fulfilled. (No need to go on forever).
        if (isFulfilled() || state.getBlock() != treeBlock)
        {
            return;
        }

        if (BlockPosUtil.getDistance2D(lastTreePos, pos) > MIN_TREE_SEPERATION && hasLeavesAround(pos, player.level))
        {
            if (isFulfilled())
            {
                //quest.onEffectComplete(this);
            }
        }
    }

    @Override
    public boolean isReady(final Player player, final IColonyQuest colonyQuest)
    {
        return colonyQuest.getObjectiveData() instanceof ObjectiveData && colonyQuest.getObjectiveData().isFulfilled();
    }

    @Override
    public boolean tryResolve(final Player player, final IColonyQuest colonyQuest)
    {
        //todo also cleanup
        return colonyQuest.getObjectiveData() instanceof ObjectiveData && colonyQuest.getObjectiveData().isFulfilled();
    }

    @Override
    public IDialogueObjective.DialogueElement getReadyDialogueTree()
    {
        //todo almost same dialogue setup
        //todo we need to give the dialogue thing also the objective data so that it can do checkups. It should query this on client and serverside.
        return null;
    }

    public class ObjectiveData implements IObjectiveData
    {
        private int currentProgress = 0;

        @Override
        public boolean isFulfilled()
        {
            return currentProgress >= blocksToMine;
        }

        @Override
        public CompoundTag serializeNBT()
        {
            return null;
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {

        }
    }
}
