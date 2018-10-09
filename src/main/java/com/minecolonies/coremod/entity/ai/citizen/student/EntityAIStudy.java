package com.minecolonies.coremod.entity.ai.citizen.student;

import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLibrary;
import com.minecolonies.coremod.colony.jobs.JobStudent;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

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
     * The paper item to use
     */
    private Item paper = GameRegistry.makeItemStack("Paper", 0, 1, null).getItem();

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
     * @return the next AIState.
     */
    private AIState study()
    {
        final CitizenData data = worker.getCitizenData();
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

        // Search for paper to use to study
        final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(worker,
          itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.getItem() == paper);

        if (slot == -1)
        {
            data.tryRandomLevelUp(world.rand);

            if (data.getJob().getAsyncRequests().isEmpty())
            {
                data.createRequestAsync(new Stack(GameRegistry.makeItemStack("Paper", 0, 10, null)));
            }
        }
        else
        {
            data.tryRandomLevelUp(world.rand, 30);
            data.getInventory().decrStackSize(slot, 1);
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
    private AIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return STUDY;
    }
}
