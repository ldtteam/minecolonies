package com.minecolonies.coremod.quests.objectives;

import com.minecolonies.api.quests.IColonyQuest;
import com.minecolonies.api.quests.IDialogueObjective;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class MineBlockObjective implements IMineBlockObjective
{
    /**
     * ID for this special blockbreak effect
     */
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "tracktreechop");

    /**
     * Min block distance between block breaks to count as another tree
     */
    private static final double MIN_TREE_SEPERATION = 8;

    /**
     * Amount of trees to chop
     */
    private int treesToChop = 5;

    /**
     * The block to break
     */
    private Block treeBlock = Blocks.OAK_LOG;

    /**
     * Quest reference
     */
    private final IColonyQuest quest;

    /**
     * Last chopped position
     */
    private BlockPos lastTreePos = BlockPos.ZERO;

    public MineBlockObjective(final IColonyQuest quest)
    {
        this.quest = quest;
    }

    public ResourceLocation getID()
    {
        return ID;
    }

    public void onStart()
    {

    }

    public void onFinish()
    {

    }

    public void onCancel()
    {

    }

    public boolean isFulfilled()
    {
        return treesToChop <= 0;
    }

    @Override
    public void onBlockBreak(final BlockState state, final BlockPos pos, final Player player)
    {
        if (isFulfilled() || state.getBlock() != treeBlock)
        {
            return;
        }

        if (BlockPosUtil.getDistance2D(lastTreePos, pos) > MIN_TREE_SEPERATION && hasLeavesAround(pos, player.level))
        {
            treesToChop--;
            if (isFulfilled())
            {
                //quest.onEffectComplete(this);
            }
        }
    }

    /**
     * Save data
     */
    public CompoundTag serializeNBT()
    {
        final CompoundTag nbt = IMineBlockObjective.super.serializeNBT();
        BlockPosUtil.write(nbt, "pos,", lastTreePos);
        return nbt;
    }

    /**
     * Load data
     *
     * @param nbt compound to read from
     */
    public void deserializeNBT(final CompoundTag nbt)
    {
        lastTreePos = BlockPosUtil.read(nbt, "pos");
        IMineBlockObjective.super.deserializeNBT(nbt);
    }

    /**
     * Check if the position has leaves around
     *
     * @param pos   position to check
     * @param world world to check
     * @return true if had leaves close
     */
    private boolean hasLeavesAround(final BlockPos pos, final Level world)
    {
        for (final Direction dir : Direction.values())
        {
            if (world.getBlockState(pos.relative(dir)).getBlock() instanceof LeavesBlock)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getBreakCounter()
    {
        return treesToChop;
    }

    @Override
    public void setBreakCounter(final int count)
    {
        treesToChop = count;
    }

    public int getMaxObjectiveCount()
    {
        return 5;
    }

    public int getCurrentObjectiveCount()
    {
        return treesToChop;
    }

    @Override
    public boolean isReady(final Player player, final IColonyQuest colonyQuest)
    {
        return false;
    }

    @Override
    public boolean tryResolve(final Player player, final IColonyQuest colonyQuest)
    {
        return false;
    }

    @Override
    public IDialogueObjective.DialogueElement getReadyDialogueTree()
    {
        return null;
    }
}
