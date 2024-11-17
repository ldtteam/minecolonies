package com.minecolonies.core.entity.ai.workers.education;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingLibrary;
import com.minecolonies.core.colony.jobs.JobStudent;
import com.minecolonies.core.datalistener.StudyItemListener;
import com.minecolonies.core.datalistener.StudyItemListener.StudyItem;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAISkill;
import com.minecolonies.core.entity.pathfinding.navigation.PathfindingAIHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.StatisticsConstants.INT_LEVELED;
import static com.minecolonies.api.util.constant.StatisticsConstants.ITEM_USED;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.STATS_MODULE;
import static com.minecolonies.core.entity.ai.workers.AbstractEntityAIInteract.RENDER_META_WORKING;

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
    public static final  String   RENDER_META_STUDYING = "study";
    /**
     * One in X chance to gain experience
     */
    public static final  int      ONE_IN_X_CHANCE      = 8;
    /**
     * Delay for each subject study.
     */
    private static final int      STUDY_DELAY          = 20 * 60;
    /**
     * The current pos to study at.
     */
    private              BlockPos studyPos             = null;

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
    public Class<BuildingLibrary> getExpectedBuildingClass()
    {
        return BuildingLibrary.class;
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

        if (PathfindingAIHelper.walkCloseToXNearY(worker, studyPos, building.getPosition(), 7))
        {
            setDelay(WALK_DELAY);
            return getState();
        }

        final Collection<StudyItem> studyItems = StudyItemListener.getAllStudyItems().values();

        // Search for Items to use to study
        final List<StudyItem> availableItemKeys = new ArrayList<>();
        final Map<StudyItem, Integer> availableItems = new HashMap<>();
        for (final StudyItem curItem : studyItems)
        {
            final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(worker, (item) -> item.is(curItem.item()));
            if (slot != -1)
            {
                availableItemKeys.add(curItem);
                availableItems.put(curItem, slot);
            }
        }

        // Create a new Request for items
        if (availableItems.isEmpty())
        {
            final List<ItemStack> itemsToRequest = new ArrayList<>();
            for (final StudyItem studyItem : studyItems)
            {
                final int bSlot = InventoryUtils.findFirstSlotInProviderWith(building, studyItem.item());
                if (bSlot > -1)
                {
                    needsCurrently = new Tuple<>(itemStack -> studyItem.item() == itemStack.getItem(), 10);
                    return GATHERING_REQUIRED_MATERIALS;
                }

                final ItemStack itemStack = new ItemStack(studyItem.item(), studyItem.item().getDefaultInstance().getMaxStackSize());
                itemsToRequest.add(itemStack);
            }

            checkIfRequestForItemExistOrCreate(new StackList(itemsToRequest, "Study Items", 64));

            // Default levelup
            data.getCitizenSkillHandler().tryLevelUpIntelligence(data.getRandom(), ONE_IN_X_CHANCE, data);
            worker.setItemInHand(InteractionHand.MAIN_HAND, ItemStackUtils.EMPTY);
        }
        // Use random item
        else
        {
            final StudyItem chosenItem = availableItemKeys.get(world.random.nextInt(availableItems.size()));
            final int chosenSlot = availableItems.get(chosenItem);

            worker.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(chosenItem.item(), 1));
            if (data.getCitizenSkillHandler().tryLevelUpIntelligence(data.getRandom(), ONE_IN_X_CHANCE * (10D / chosenItem.skillIncreaseChance()), data))
            {
                building.getModule(STATS_MODULE).increment(INT_LEVELED);
            }
            // Break item rand
            if (world.random.nextInt(100) <= chosenItem.breakChance())
            {
                data.getInventory().extractItem(chosenSlot, 1, false);
                building.getModule(STATS_MODULE).increment(ITEM_USED + ";" + chosenItem.item().getDescriptionId());
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
