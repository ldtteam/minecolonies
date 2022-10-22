package com.minecolonies.coremod.quests.type.effects;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.quests.IQuest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class TrackTreeChoppingEffect implements ITrackBlockBreakEffect
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
    private final IQuest quest;

    /**
     * Last chopped position
     */
    private BlockPos lastTreePos = BlockPos.ZERO;

    public TrackTreeChoppingEffect(final IQuest quest)
    {
        this.quest = quest;
    }

    @Override
    public ResourceLocation getID()
    {
        return ID;
    }

    @Override
    public void onStart()
    {

    }

    @Override
    public void onFinish()
    {

    }

    @Override
    public void onCancel()
    {

    }

    @Override
    public boolean isFulfilled()
    {
        return treesToChop <= 0;
    }

    @Override
    public void onBlockBreak(final BlockState state, final BlockPos pos, final PlayerEntity player)
    {
        if (isFulfilled() || state.getBlock() != treeBlock)
        {
            return;
        }

        if (BlockPosUtil.getDistance2D(lastTreePos, pos) > MIN_TREE_SEPERATION && hasLeavesAround(pos, player.world))
        {
            treesToChop--;
            if (isFulfilled())
            {
                quest.onEffectComplete(this);
            }
        }
    }

    /**
     * Save data
     */
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT nbt = ITrackBlockBreakEffect.super.serializeNBT();
        BlockPosUtil.write(nbt, "pos,", lastTreePos);
        return nbt;
    }

    /**
     * Load data
     *
     * @param nbt compound to read from
     */
    public void deserializeNBT(final CompoundNBT nbt)
    {
        lastTreePos = BlockPosUtil.read(nbt, "pos");
        ITrackBlockBreakEffect.super.deserializeNBT(nbt);
    }

    /**
     * Check if the position has leaves around
     *
     * @param pos   position to check
     * @param world world to check
     * @return true if had leaves close
     */
    private boolean hasLeavesAround(final BlockPos pos, final IBlockReader world)
    {
        for (final Direction dir : Direction.values())
        {
            if (world.getBlockState(pos.offset(dir)).getBlock() instanceof LeavesBlock)
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

    @Override
    public int getMaxObjectiveCount()
    {
        return 5;
    }

    @Override
    public int getCurrentObjectiveCount()
    {
        return treesToChop;
    }
}
