package com.minecolonies.coremod.entity.ai.citizen.student;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.ICitizenData;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLibrary;
import com.minecolonies.coremod.colony.jobs.JobStudent;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.coremod.entity.ai.statemachine.AITarget;
import com.minecolonies.coremod.entity.ai.statemachine.states.IAIState;
import com.minecolonies.coremod.entity.ai.util.StudyItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.coremod.entity.ai.statemachine.states.AIWorkerState.*;

/**
 * The Entity AI study class.
 */
public class EntityAIStudy extends AbstractEntityAISkill<JobStudent>
{
    /**
     * Delay for each subject study.
     */
    private static final int STUDY_DELAY = 20 * 60;

    /**
     * The current pos to study at.
     */
    private BlockPos studyPos = null;

    /**
     * Constructor for the student.
     * Defines the tasks the student executes.
     *
     * @param job a student job to use.
     */
    public EntityAIStudy(@NotNull final JobStudent job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
          new AITarget(STUDY, this::study)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingLibrary.class;
    }

    /**
     * The AI task for the student to study.
     * For this he should walk between the different bookcase hit them once and then stand around for a while.
     *
     * @return the next IAIState.
     */
    private IAIState study()
    {
        final ICitizenData data = worker.getCitizenData();
        if (data == null)
        {
            setDelay(STUDY_DELAY);
            return getState();
        }

        if (studyPos == null)
        {
            studyPos = getOwnBuilding(BuildingLibrary.class).getRandomBookShelf();
        }

        if (walkToBlock(studyPos))
        {
            setDelay(WALK_DELAY);
            return getState();
        }

        // Search for Items to use to study
        final List<StudyItem> currentItems = new ArrayList<>();
        worker.decreaseSaturationForAction();

        for (final StudyItem curItem : getOwnBuilding(BuildingLibrary.class).getStudyItems())
        {
            final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(worker,
              itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.getItem() == curItem.getItem());

            if (slot != -1)
            {
                curItem.setSlot(slot);
                currentItems.add(curItem);
            }
        }

        // Create a new Request for items
        if (currentItems.isEmpty())
        {
            // Default levelup
            data.tryRandomLevelUp(world.rand);
            worker.setHeldItem(EnumHand.MAIN_HAND, ItemStackUtils.EMPTY);

            for (final StudyItem studyItem : getOwnBuilding(BuildingLibrary.class).getStudyItems())
            {
                final int bSlot = InventoryUtils.findFirstSlotInProviderWith(getOwnBuilding(), studyItem.getItem(), 0);
                if (bSlot > -1)
                {
                    if (walkToBuilding())
                    {
                        setDelay(WALK_DELAY);
                        return getState();
                    }
                    takeItemStackFromProvider(getOwnBuilding(), bSlot);
                }
                else
                {
                    checkIfRequestForItemExistOrCreateAsynch(new ItemStack(studyItem.getItem(), studyItem.getBreakPct() / 10 > 0 ? studyItem.getBreakPct() / 10 : 1));
                }
            }
        }
        // Use random item
        else
        {
            final StudyItem chosenItem = currentItems.get(world.rand.nextInt(currentItems.size()));

            worker.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(chosenItem.getItem(), 1));
            data.tryRandomLevelUp(world.rand, data.getChanceToLevel() * 100 / chosenItem.getSkillIncreasePct());

            // Break item rand
            if (world.rand.nextInt(100) <= chosenItem.getBreakPct())
            {
                data.getInventory().decrStackSize(chosenItem.getSlot(), 1);
            }
        }

        studyPos = null;
        setDelay(STUDY_DELAY);
        return getState();
    }

    /**
     * Redirects the student to his library.
     *
     * @return the next state.
     */
    private IAIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return STUDY;
    }
}
