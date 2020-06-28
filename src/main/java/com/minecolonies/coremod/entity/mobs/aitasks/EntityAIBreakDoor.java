package com.minecolonies.coremod.entity.mobs.aitasks;

import com.minecolonies.coremod.MineColonies;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.BreakDoorGoal;

import java.util.EnumSet;

/**
 * Break door entity AI with mutex.
 */
public class EntityAIBreakDoor extends BreakDoorGoal
{
    public EntityAIBreakDoor(final MobEntity entityIn)
    {
        super(entityIn, difficulty -> difficulty.getId() > 0);
        setMutexFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public void tick()
    {
        if (entity.getEntityWorld().getDifficulty().getId() < 2 || !MineColonies.getConfig().getCommon().shouldRaidersBreakDoors.get())
        {
            breakingTime = 0;
        }
        super.tick();
    }
}
