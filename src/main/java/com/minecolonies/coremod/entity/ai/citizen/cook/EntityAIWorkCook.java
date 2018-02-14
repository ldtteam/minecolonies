package com.minecolonies.coremod.entity.ai.citizen.cook;

import com.minecolonies.api.colony.requestsystem.requestable.Food;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.jobs.JobCook;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIUsesFurnace;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.RESULT_SLOT;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * Cook AI class.
 */
public class EntityAIWorkCook extends AbstractEntityAIUsesFurnace<JobCook>
{
    /**
     * How often should charisma factor into the cook's skill modifier.
     */
    private static final int CHARISMA_MULTIPLIER = 2;

    /**
     * How often should intelligence factor into the cook's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 1;

    /**
     * The amount of food which should be served to the woker.
     */
    private static final int AMOUNT_OF_FOOD_TO_SERVE = 3;

    /**
     * Delay between each serving.
     */
    private static final int SERVE_DELAY = 30;

    /**
     * The citizen the worker is currently trying to serve.
     */
    private final List<EntityCitizen> citizenToServe = new ArrayList<>();

    /**
     * The building range the cook should search for clients.
     */
    private AxisAlignedBB range = null;

    /**
     * Constructor for the Cook.
     * Defines the tasks the cook executes.
     *
     * @param job a cook job to use.
     */
    public EntityAIWorkCook(@NotNull final JobCook job)
    {
        super(job);
        super.registerTargets(
                new AITarget(COOK_SERVE_FOOD_TO_CITIZEN, this::serveFoodToCitizen)
        );
        worker.setSkillModifier(CHARISMA_MULTIPLIER * worker.getCitizenData().getCharisma()
                + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());
        worker.setCanPickUpLoot(true);
    }

    /**
     * Very simple action, cook straightly extract it from the furnace.
     * @param furnace the furnace to retrieve from.
     */
    @Override
    protected void extractFromFurnace(final TileEntityFurnace furnace)
    {
        InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandlers(
                new InvWrapper(furnace), RESULT_SLOT,
                new InvWrapper(worker.getInventoryCitizen()));
    }

    @Override
    protected boolean isSmeltable(final ItemStack stack)
    {
        return ItemStackUtils.ISCOOKABLE.test(stack);
    }

    /**
     * Serve food to customer
     *
     * If no customer, transition to START_WORKING.
     * If we need to walk to the customer, repeat this state with tiny delay.
     * If the customer has a full inventory, report and remove customer, delay and repeat this state.
     * If we have food, then COOK_SERVE.
     * If no food in the building, transition to START_WORKING.
     * If we were able to get the stored food, then COOK_SERVE.
     * If food is no longer available, delay and transition to START_WORKING.
     * Otherwise, give the customer some food, then delay and repeat this state.
     *
     * @return next AIState
     */
    private AIState serveFoodToCitizen()
    {
        worker.setLatestStatus(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_SERVING));

        if (citizenToServe.isEmpty())
        {
            return START_WORKING;
        }

        if (walkToBlock(citizenToServe.get(0).getPosition()))
        {
            setDelay(2);
            return getState();
        }

        if (InventoryUtils.isItemHandlerFull(new InvWrapper(citizenToServe.get(0).getInventoryCitizen())))
        {
            chatSpamFilter.talkWithoutSpam(HUNGRY_INV_FULL);
            citizenToServe.remove(0);
            setDelay(SERVE_DELAY);
            return getState();
        }
        InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoNextFreeSlotInItemHandler(
                new InvWrapper(worker.getInventoryCitizen()),
                ItemStackUtils.ISFOOD,
                AMOUNT_OF_FOOD_TO_SERVE, new InvWrapper(citizenToServe.get(0).getInventoryCitizen())
                );

        citizenToServe.remove(0);
        setDelay(SERVE_DELAY);
        return getState();
    }

    /**
     * Checks if the cook has anything important to do before going to the default furnace user jobs.
     * First calculate the building range if not cached yet.
     * Then check for citizens around the building.
     * If no citizen around switch to default jobs.
     * If citizens around check if food in inventory, if not, switch to gather job.
     * If food in inventory switch to serve job.
     * @return the next AIState to transfer to.
     */
    @Override
    protected AIState checkForImportantJobs()
    {
        if (range == null)
        {
            range = getOwnBuilding().getTargetableArea(world);
        }

        citizenToServe.clear();
        final List<EntityCitizen> citizenList = world.getEntitiesWithinAABB(EntityCitizen.class,
                range, cit -> !(cit.getColonyJob() instanceof JobCook) && cit.getCitizenData() != null && cit.getCitizenData().getSaturation() <= 0);
        if (!citizenList.isEmpty())
        {
            citizenToServe.addAll(citizenList);
            if (InventoryUtils.hasItemInItemHandler(
                    new InvWrapper(worker.getInventoryCitizen()), ItemStackUtils.ISFOOD))
            {
                return COOK_SERVE_FOOD_TO_CITIZEN;
            }

            needsCurrently = ItemStackUtils.ISFOOD;
            return GATHERING_REQUIRED_MATERIALS;
        }
        return START_WORKING;
    }

    @Override
    protected IRequestable getSmeltAbleClass()
    {
        return new Food(STACKSIZE);
    }
}
