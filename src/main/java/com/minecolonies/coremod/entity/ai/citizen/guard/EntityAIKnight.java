package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.jobs.JobKnight;
import com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard.GuardItemsNeeded;
import com.minecolonies.coremod.entity.ai.util.AIState;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
        final List<GuardItemsNeeded> itemlvl1Needed = new ArrayList<>();
        itemlvl1Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.MAINHAND,  Items.SHIELD, 1, 4, 99));
        itemsNeeded.put(Integer.valueOf(1), itemlvl1Needed);

        final List<GuardItemsNeeded> itemlvl2Needed = new ArrayList<>();
        itemlvl2Needed.add(new GuardItemsNeeded(EntityEquipmentSlot.MAINHAND,  Items.SHIELD, 1, 1, 99));
        itemsNeeded.put(Integer.valueOf(2), itemlvl2Needed);
        itemsNeeded.put(Integer.valueOf(3), itemlvl2Needed);
        itemsNeeded.put(Integer.valueOf(4), itemlvl2Needed);
        itemsNeeded.put(Integer.valueOf(5), itemlvl2Needed);
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
