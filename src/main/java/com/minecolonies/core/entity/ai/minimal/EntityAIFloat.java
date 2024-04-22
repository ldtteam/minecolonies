package com.minecolonies.core.entity.ai.minimal;

import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.entity.pathfinding.navigation.MinecoloniesAdvancedPathNavigate;
import com.minecolonies.core.entity.pathfinding.pathjobs.PathJobEscapeWater;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;

public class EntityAIFloat extends FloatGoal
{
    private final Mob owner;

    private PathResult waterPathing = null;

    public EntityAIFloat(final Mob mob)
    {
        super(mob);
        owner = mob;

        if (!(mob.getNavigation() instanceof MinecoloniesAdvancedPathNavigate))
        {
            Log.getLogger().error("Unsupported entity for EntityAIFloat goal:" + mob);
        }
    }

    @Override
    public void tick()
    {
        if (!owner.getEyeInFluidType().isAir() && owner.getEyeInFluidType().canSwim(owner))
        {
            if (waterPathing == null || !waterPathing.isInProgress())
            {
                if (owner.getNavigation() instanceof MinecoloniesAdvancedPathNavigate nav)
                {
                    nav.setPauseTicks(0);
                    nav.stop();

                    waterPathing = nav.setPathJob(
                      new PathJobEscapeWater(CompatibilityUtils.getWorldFromEntity(owner),
                        owner.blockPosition(),
                        (int) owner.getAttribute(Attributes.FOLLOW_RANGE).getValue() * 5,
                        owner),
                      null, 1.0, false);
                    nav.setPauseTicks(20 * 15);
                }
            }
        }
        else
        {
            if (waterPathing != null)
            {
                waterPathing = null;
                if (owner.getNavigation() instanceof MinecoloniesAdvancedPathNavigate nav)
                {
                    nav.setPauseTicks(0);
                }
            }

            if (owner.tickCount % 3 == 0)
            {
                owner.getJumpControl().jump();
            }
        }
    }
}
