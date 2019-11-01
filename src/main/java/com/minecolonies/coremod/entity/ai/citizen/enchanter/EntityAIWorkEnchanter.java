package com.minecolonies.coremod.entity.ai.citizen.enchanter;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingEnchanter;
import com.minecolonies.coremod.colony.jobs.JobEnchanter;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.TranslationConstants.NO_WORKERS_TO_DRAIN_SET;

/**
 * Enchanter AI class.
 */
public class EntityAIWorkEnchanter extends AbstractEntityAIInteract<JobEnchanter>
{
    /**
     * How often should intelligence factor into the enchanter's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 2;

    /**
     * How often should intelligence factor into the enchanter's skill modifier.
     */
    private static final int CHARISMA_MULTIPLIER = 1;

    /**
     * Predicate to define an ancient tome which can be enchanted.
     */
    private static final Predicate<ItemStack> IS_ANCIENT_TOME = item -> !item.isEmpty() && item.getItem() == ModItems.ancientTome;

    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkEnchanter(@NotNull final JobEnchanter job)
    {
        super(job);
        super.registerTargets(
            new AITarget(IDLE, START_WORKING, TICKS_SECOND),
            new AITarget(DECIDE, this::decide, TICKS_SECOND),
            new AITarget(ENCHANTER_DRAIN, this::gatherAndDrain, TICKS_SECOND),
            new AITarget(ENCHANT, this::enchant, TICKS_SECOND)
        );
        worker.getCitizenExperienceHandler().setSkillModifier(CHARISMA_MULTIPLIER * worker.getCitizenData().getCharisma()
                                                                + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());
        worker.setCanPickUpLoot(true);
    }

    /**
     * Decide method of the enchanter.
     * Check if everything is alright to work and then decide between gathering and draining and actually enchanting.
     *
     * @return the next state to go to.
     */
    private IAIState decide()
    {
        worker.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
        if (walkToBuilding())
        {
            return DECIDE;
        }

        if (worker.getCitizenExperienceHandler().getLevel() < getOwnBuilding().getBuildingLevel())
        {
            if (getOwnBuilding(BuildingEnchanter.class).getBuildingsToGatherFrom().isEmpty())
            {
                chatSpamFilter.talkWithoutSpam(NO_WORKERS_TO_DRAIN_SET);
                return IDLE;
            }

            //todo random draw (check if worker was drained today already).
            return ENCHANTER_DRAIN;
        }

        final int amountOfCompostInInv = InventoryUtils.getItemCountInItemHandler(new InvWrapper(worker.getInventoryCitizen()), IS_ANCIENT_TOME);
        if (amountOfCompostInInv <= 0)
        {
            final int amountOfCompostInBuilding = InventoryUtils.getItemCountInProvider(getOwnBuilding(), IS_ANCIENT_TOME);
            if (amountOfCompostInBuilding > 0)
            {
                needsCurrently = IS_ANCIENT_TOME;
                return GATHERING_REQUIRED_MATERIALS;
            }
            checkIfRequestForItemExistOrCreateAsynch(new ItemStack(ModItems.ancientTome, 1));
            return IDLE;
        }

        return ENCHANT;
    }

    private IAIState enchant()
    {
        //todo enchant
        return null;
    }

    private IAIState gatherAndDrain()
    {
        //todo go to worker place
        //todo wait there for worker
        //todo if not appear go back
        //todo if appear start
        //todo start drainage with effects travelling between both
        //todo stop workers AI for a moment there (entitycitizen)=
        //todo add random enchant to worker drained from
        //todo remove 50% of xp only and give us full (make this configurable)
        //todo set worker as gathered today (entityCitizen)
        return null;
    }

}
