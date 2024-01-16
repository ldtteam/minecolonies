package com.minecolonies.core.entity.ai.citizen.student;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.util.StudyItem;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingLibrary;
import com.minecolonies.core.colony.jobs.JobStudent;
import com.minecolonies.core.entity.ai.basic.AbstractEntityAISkill;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.StatisticsConstants.INT_LEVELED;
import static com.minecolonies.api.util.constant.StatisticsConstants.ITEM_USED;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.STATS_MODULE;
import static com.minecolonies.core.entity.ai.basic.AbstractEntityAIInteract.RENDER_META_WORKING;

/**
 * The Entity AI study class.
 */
public class EntityAIStudy extends AbstractEntityAISkill<JobStudent, BuildingLibrary>
{
    /**
     * Render the book.
     */
    public static final String RENDER_META_BOOK = "book";

    /**
     * Render the book.
     */
    public static final String RENDER_META_STUDYING = "study";

    /**
     * Delay for each subject study.
     */
    private static final int STUDY_DELAY = 20 * 60;

    /**
     * One in X chance to gain experience
     */
    public static final int ONE_IN_X_CHANCE = 8;

    /**
     * The current pos to study at.
     */
    private BlockPos studyPos = null;

    /**
     * Constructor for the student. Defines the tasks the student executes.
     *
     * @param job a student job to use.
     */
    public EntityAIStudy(@NotNull final JobStudent job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 1),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding, TICKS_SECOND),
          new AITarget(STUDY, this::study, STANDARD_DELAY)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    protected void updateRenderMetaData()
    {
        String renderMeta = getState() == IDLE ? "" : RENDER_META_WORKING;
        if (InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), itemStack -> itemStack.getItem() == Items.BOOK || itemStack.getItem() == Items.PAPER))
        {
            renderMeta += RENDER_META_BOOK;
        }
        if (worker.getNavigation().isDone())
        {
            renderMeta += RENDER_META_STUDYING;
        }
        worker.setRenderMetadata(renderMeta);
    }

    @Override
    public Class<BuildingLibrary> getExpectedBuildingClass()
    {
        return BuildingLibrary.class;
    }

    /**
     * The AI task for the student to study. For this he should walk between the different bookcase hit them once and then stand around for a while.
     *
     * @return the next IAIState.
     */
    private IAIState study()
    {
        final ICitizenData data = worker.getCitizenData();

        if (studyPos == null)
        {
            studyPos = building.getRandomBookShelf();
        }

        if (walkToBlock(studyPos))
        {
            setDelay(WALK_DELAY);
            return getState();
        }

        // Search for Items to use to study
        final List<StudyItem> currentItems = new ArrayList<>();
        for (final StudyItem curItem : building.getStudyItems())
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
            for (final StudyItem studyItem : building.getStudyItems())
            {
                final int bSlot = InventoryUtils.findFirstSlotInProviderWith(building, studyItem.getItem());
                if (bSlot > -1)
                {
                    needsCurrently = new Tuple<>(itemStack -> studyItem.getItem() == itemStack.getItem(), 10);
                    return GATHERING_REQUIRED_MATERIALS;
                }
                else
                {
                    checkIfRequestForItemExistOrCreateAsync(new ItemStack(studyItem.getItem(), studyItem.getBreakPct() / 10 > 0 ? studyItem.getBreakPct() / 10 : 1));
                }
            }

            // Default levelup
            data.getCitizenSkillHandler().tryLevelUpIntelligence(data.getRandom(), ONE_IN_X_CHANCE, data);
            worker.setItemInHand(InteractionHand.MAIN_HAND, ItemStackUtils.EMPTY);
        }
        // Use random item
        else
        {
            final StudyItem chosenItem = currentItems.get(world.random.nextInt(currentItems.size()));

            worker.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(chosenItem.getItem(), 1));
            if (data.getCitizenSkillHandler().tryLevelUpIntelligence(data.getRandom(), ONE_IN_X_CHANCE * (100D / chosenItem.getSkillIncreasePct()), data))
            {
                building.getModule(STATS_MODULE).increment(INT_LEVELED);
            }
            // Break item rand
            if (world.random.nextInt(100) <= chosenItem.getBreakPct())
            {
                data.getInventory().extractItem(chosenItem.getSlot(), 1, false);
                building.getModule(STATS_MODULE).increment(ITEM_USED + ";" + chosenItem.getItem().getDescriptionId());
            }
        }

        worker.decreaseSaturationForAction();
        studyPos = null;
        worker.queueSound(SoundEvents.BOOK_PAGE_TURN, worker.blockPosition().above(), 80, 15, 0.25f, 1.5f);

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
