package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.jobs.JobKnight;
import com.minecolonies.coremod.entity.ai.util.AIState;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityAIKnight extends AbstractEntityAIGuard<JobKnight>
{

    /**
     * This guard's minimum distance for attack.
     */
    private static final double MAX_DISTANCE_FOR_ATTACK = 3;

    /**
     * Basic delay for the next shot.
     */
    private static final int BASE_RELOAD_TIME = 30;

    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIKnight(@NotNull final JobKnight job)
    {
        super(job);
        toolsNeeded.add(ToolType.SWORD);
        final GuardItemsNeeded itemlvl1Needed = new GuardItemsNeeded(ToolType.SHIELD, EntityEquipmentSlot.MAINHAND, 0, 0, 1, 4, 99);
        itemsNeeded.get(1).add(itemlvl1Needed);

        final GuardItemsNeeded itemlvl2Needed = new GuardItemsNeeded(ToolType.SHIELD, EntityEquipmentSlot.MAINHAND, 0,0 , 1, 1, 99);
        itemsNeeded.get(2).add(itemlvl2Needed);
        itemsNeeded.get(3).add(itemlvl2Needed);
        itemsNeeded.get(4).add(itemlvl2Needed);
        itemsNeeded.get(5).add(itemlvl2Needed);
    }

    @Override
    protected int getAttackRange()
    {
        return (int) MAX_DISTANCE_FOR_ATTACK;
    }

    @Override
    protected int getAttackDelay()
    {
        if (worker.getCitizenData() != null)
        {
            return BASE_RELOAD_TIME / (worker.getCitizenData().getLevel() + 1);
        }
        return BASE_RELOAD_TIME;
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

        if (superState != DECIDE || target == null)
        {
            return superState;
        }

        return GUARD_ATTACK_PHYSICAL;
    }
}
