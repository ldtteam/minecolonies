package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.ai.util.AIState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

public class EntityAIKnight extends AbstractEntityAIGuardNew
{

    /**
     * This guard's minimum distance for attack.
     */
    private static final double MAX_DISTANCE_FOR_ATTACK = 5;

    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIKnight(@NotNull final AbstractJobGuard job)
    {
        super(job);
        toolsNeeded.add(ToolType.SWORD);
    }

    @NotNull
    @Override
    protected List<ItemStack> itemsNiceToHave()
    {
        final List<ItemStack> list = super.itemsNiceToHave();
        list.add(new ItemStack(Items.SHIELD, 1));
        return list;
    }

    @Override
    protected AIState decide()
    {
        final AIState superState = super.decide();

        if (superState != DECIDE)
        {
            return superState;
        }

        if (worker.getDistance(target) > MAX_DISTANCE_FOR_ATTACK)
        {
            walkToBlock(target.getPosition());
        }
        else if (worker.getDistance(target) < MAX_DISTANCE_FOR_ATTACK)
        {
            if (currentAttackDelay == 0)
            {
                currentAttackDelay = PHYSICAL_ATTACK_DELAY;
                return GUARD_ATTACK_PHYSICAL;
            }
            else
            {
                return GUARD_ATTACK_PROTECT;
            }
        }

        return DECIDE;
    }
}
