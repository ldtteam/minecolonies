package com.minecolonies.core.entity.mobs.aitasks;

import com.minecolonies.api.blocks.decorative.AbstractBlockGate;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesRaider;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.entity.ai.workers.guard.AbstractEntityAIGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.MECHANIC_ENHANCED_GATES;

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

    public EntityAIBreakDoor(final Mob entityIn)
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
        if (mob instanceof AbstractEntityMinecoloniesRaider)
        {
            ((AbstractEntityMinecoloniesRaider) mob).setCanBeStuck(false);
        }
    }

    public void stop()
    {
        super.stop();
        this.mob.level.destroyBlockProgress(this.mob.getId(), this.doorPos, -1);
        if (mob instanceof AbstractEntityMinecoloniesRaider)
        {
            ((AbstractEntityMinecoloniesRaider) mob).setCanBeStuck(true);
        }
    }

    @Override
    public void tick()
    {
        if (mob.getCommandSenderWorld().getDifficulty().getId() < 2 || !MineColonies.getConfig().getServer().raidersbreakdoors.get())
        {
            breakTime = 0;
            return;
        }

        // Only advances breaking time in relation to hardness
        if (this.mob.getRandom().nextInt(breakChance) != 0)
        {
            this.breakTime--;
        }
        else
        {
            double fasterBreakPerXNearby = 5;

            if (mob instanceof AbstractEntityMinecoloniesRaider && !mob.level.isClientSide() && mob.level.getBlockState(doorPos).getBlock() instanceof AbstractBlockGate)
            {
                final IColony colony = ((AbstractEntityMinecoloniesRaider) mob).getColony();

                fasterBreakPerXNearby += colony.getResearchManager().getResearchEffects().getEffectStrength(MECHANIC_ENHANCED_GATES);
            }

            fasterBreakPerXNearby /= 2;
            breakChance = (int) Math.max(1,
              hardness / (1 + (mob.level.getEntitiesOfClass(AbstractEntityMinecoloniesRaider.class, mob.getBoundingBox().inflate(5)).size() / fasterBreakPerXNearby)));

            // Alert nearby guards
            if (this.mob.getRandom().nextInt(breakChance) == 0 && mob instanceof AbstractEntityMinecoloniesRaider raider && mob.level.getBlockState(doorPos)
                .getBlock() instanceof AbstractBlockGate)
            {
                // Alerts guards of raiders reaching a building
                final List<AbstractEntityCitizen> possibleGuards = new ArrayList<>();

                for (final ICitizenData entry : raider.getColony().getCitizenManager().getCitizens())
                {
                    if (entry.getEntity().isPresent()
                        && entry.getJob() instanceof AbstractJobGuard
                        && BlockPosUtil.getDistanceSquared(entry.getEntity().get().blockPosition(), doorPos) < 100 * 100 && entry.getJob().getWorkerAI() != null)
                    {
                        if (((AbstractEntityAIGuard<?, ?>) entry.getJob().getWorkerAI()).canHelp(doorPos) && !doorPos.equals(((AbstractEntityAIGuard<?, ?>) entry.getJob()
                            .getWorkerAI()).getCurrentPatrolPoint()))
                        {
                            possibleGuards.add(entry.getEntity().get());
                        }
                    }
                }

                possibleGuards.sort(Comparator.comparingInt(guard -> (int) doorPos.distSqr(guard.blockPosition())));
                BlockPos gotoPos = BlockPos.containing(Vec3.atCenterOf(doorPos)
                    .add(Vec3.atCenterOf(raider.getColony().getCenter()).subtract(Vec3.atCenterOf(doorPos)).normalize().multiply(3, 0, 3)));

                for (int i = 0; i < possibleGuards.size() && i <= 3; i++)
                {
                    ((AbstractEntityAIGuard<?, ?>) possibleGuards.get(i).getCitizenData().getJob().getWorkerAI()).setNextPatrolTargetAndMove(gotoPos);
                }
            }
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
