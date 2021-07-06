package com.minecolonies.coremod.entity.mobs.aitasks;

import com.minecolonies.api.blocks.decorative.AbstractBlockGate;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.BreakDoorGoal;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

import static com.minecolonies.api.research.util.ResearchConstants.MECHANIC_ENHANCED_GATES;

import net.minecraft.entity.ai.goal.Goal.Flag;

/**
 * Break door entity AI with mutex.
 */
public class EntityAIBreakDoor extends BreakDoorGoal
{
    /**
     * Previous break pos
     */
    private BlockPos prevDoorPos = BlockPos.ZERO;

    /**
     * The door's hardness we're breaking
     */
    private int hardness = 0;

    /**
     * Amount of nearby raiders
     */
    private int breakChance = 1;

    public EntityAIBreakDoor(final MobEntity entityIn)
    {
        super(entityIn, difficulty -> difficulty.getId() > 0);
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canContinueToUse()
    {
        return super.canContinueToUse() && !mob.level.isEmptyBlock(doorPos);
    }

    @Override
    public void start()
    {
        super.start();
        if (!prevDoorPos.equals(doorPos))
        {
            this.breakTime = 0;
        }
        prevDoorPos = doorPos;
        hardness = (int) (1 + mob.level.getBlockState(doorPos).getDestroySpeed(mob.level, doorPos));

        // No stuck during door break
        if (mob instanceof AbstractEntityMinecoloniesMob)
        {
            ((AbstractEntityMinecoloniesMob) mob).setCanBeStuck(false);
        }
    }

    public void stop()
    {
        super.stop();
        this.mob.level.destroyBlockProgress(this.mob.getId(), this.doorPos, -1);
        if (mob instanceof AbstractEntityMinecoloniesMob)
        {
            ((AbstractEntityMinecoloniesMob) mob).setCanBeStuck(true);
        }
    }

    @Override
    public void tick()
    {
        if (mob.getCommandSenderWorld().getDifficulty().getId() < 2 || !MineColonies.getConfig().getServer().shouldRaidersBreakDoors.get())
        {
            breakTime = 0;
        }

        // Only advances breaking time in relation to hardness
        if (this.mob.getRandom().nextInt(breakChance) != 0)
        {
            this.breakTime--;
        }
        else
        {
            int fasterBreakPerXNearby = 5;

            if (mob instanceof AbstractEntityMinecoloniesMob && !mob.level.isClientSide())
            {
                final IColony colony = ((AbstractEntityMinecoloniesMob) mob).getColony();

                fasterBreakPerXNearby += colony.getResearchManager().getResearchEffects().getEffectStrength(MECHANIC_ENHANCED_GATES);
            }
            breakChance = Math.max(1,
              hardness / (1 + (mob.level.getLoadedEntitiesOfClass(AbstractEntityMinecoloniesMob.class, mob.getBoundingBox().inflate(5)).size() / fasterBreakPerXNearby)));
        }

        if (this.breakTime == this.getDoorBreakTime() - 1)
        {
            final BlockState toBreak = mob.level.getBlockState(doorPos);
            if (toBreak.getBlock() instanceof AbstractBlockGate)
            {
                ((AbstractBlockGate) toBreak.getBlock()).removeGate(mob.level, doorPos, toBreak.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise());
            }
        }

        super.tick();
    }
}
