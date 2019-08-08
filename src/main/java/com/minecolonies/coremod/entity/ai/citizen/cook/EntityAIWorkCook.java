package com.minecolonies.coremod.entity.ai.citizen.cook;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.requestable.Food;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.coremod.colony.jobs.JobCook;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIUsesFurnace;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.HUNGRY_INV_FULL;

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
     * The amount of food which should be served to the worker.
     */
    public static final int AMOUNT_OF_FOOD_TO_SERVE = 2;

    /**
     * Delay between each serving.
     */
    private static final int SERVE_DELAY          = 30;

    /**
     * Level at which the cook should give some food to the player.
     */
    private static final int LEVEL_TO_FEED_PLAYER = 10;

    /**
     * The citizen the worker is currently trying to serve.
     */
    private final List<AbstractEntityCitizen> citizenToServe = new ArrayList<>();

    /**
     * The citizen the worker is currently trying to serve.
     */
    private final List<PlayerEntity> playerToServe = new ArrayList<>();

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
        worker.getCitizenExperienceHandler().setSkillModifier(CHARISMA_MULTIPLIER * worker.getCitizenData().getCharisma()
                + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingCook.class;
    }

    /**
     * Very simple action, cook straightly extract it from the furnace.
     * @param furnace the furnace to retrieve from.
     */
    @Override
    protected void extractFromFurnace(final FurnaceTileEntity furnace)
    {
        InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandler(
                new InvWrapper(furnace), RESULT_SLOT,
                new InvWrapper(worker.getInventoryCitizen()));
        worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        this.incrementActionsDoneAndDecSaturation();
    }

    @Override
    protected boolean isSmeltable(final ItemStack stack)
    {
        return ItemStackUtils.ISCOOKABLE.test(stack);
    }

    @Override
    protected boolean reachedMaxToKeep()
    {
        return InventoryUtils.getItemCountInProvider(getOwnBuilding(), ItemStackUtils.ISFOOD) > Math.max(1, getOwnBuilding().getBuildingLevel() * getOwnBuilding().getBuildingLevel()) * SLOT_PER_LINE;
    }

    @Override
    public void requestSmeltable()
    {
        if (!getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(getSmeltAbleClass().getClass())))
        {
            worker.getCitizenData().createRequestAsync(getSmeltAbleClass());
        }
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
     * @return next IAIState
     */
    private IAIState serveFoodToCitizen()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_SERVING));

        if (citizenToServe.isEmpty() && playerToServe.isEmpty())
        {
            return START_WORKING;
        }

        final Entity living = citizenToServe.isEmpty() ? playerToServe.get(0) : citizenToServe.get(0);

        if (range == null)
        {
            range = getOwnBuilding().getTargetableArea(world);
        }

        if (!range.contains(new Vec3d(living.getPosition())))
        {
            worker.getNavigator().clearPath();
            removeFromQueue();
            return START_WORKING;
        }

        if (walkToBlock(living.getPosition()))
        {
            if (worker.getCitizenStuckHandler().isStuck())
            {
                worker.getNavigator().clearPath();
                removeFromQueue();
            }
            setDelay(2);
            return getState();
        }

        final IItemHandler handler = citizenToServe.isEmpty() ? new InvWrapper(playerToServe.get(0).inventory) : new InvWrapper(citizenToServe.get(0).getInventoryCitizen());

        if (InventoryUtils.isItemHandlerFull(handler))
        {
            chatSpamFilter.talkWithoutSpam(HUNGRY_INV_FULL);
            removeFromQueue();
            setDelay(SERVE_DELAY);
            return getState();
        }
        InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoNextFreeSlotInItemHandler(
                new InvWrapper(worker.getInventoryCitizen()),
                ItemStackUtils.CAN_EAT,
                getOwnBuilding().getBuildingLevel() * AMOUNT_OF_FOOD_TO_SERVE, handler
                );

        if (!citizenToServe.isEmpty() && citizenToServe.get(0).getCitizenData() != null)
        {
            citizenToServe.get(0).getCitizenData().setJustAte(true);
        }

        if (citizenToServe.isEmpty() && living instanceof PlayerEntity)
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) living, "com.minecolonies.coremod.cook.serve.player", worker.getName());
        }
        removeFromQueue();

        setDelay(SERVE_DELAY);
        worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        this.incrementActionsDoneAndDecSaturation();
        return START_WORKING;
    }

    /**
     * Remove the last citizen or player from the queue.
     */
    private void removeFromQueue()
    {
        if (citizenToServe.isEmpty())
        {
            playerToServe.remove(0);
        }
        else
        {
            citizenToServe.remove(0);
        }
    }

    /**
     * Checks if the cook has anything important to do before going to the default furnace user jobs.
     * First calculate the building range if not cached yet.
     * Then check for citizens around the building.
     * If no citizen around switch to default jobs.
     * If citizens around check if food in inventory, if not, switch to gather job.
     * If food in inventory switch to serve job.
     * @return the next IAIState to transfer to.
     */
    @Override
    protected IAIState checkForImportantJobs()
    {
        if (range == null)
        {
            range = getOwnBuilding().getTargetableArea(world);
        }

        citizenToServe.clear();
        final List<AbstractEntityCitizen> citizenList = world.getEntitiesWithinAABB(Entity.class, range)
          .stream()
                                                          .filter(e -> e instanceof AbstractEntityCitizen)
                                                          .map(e -> (AbstractEntityCitizen) e)
          .filter(cit -> !(cit.getCitizenJobHandler().getColonyJob() instanceof JobCook) && cit.shouldBeFed())
          .collect(Collectors.toList());

        final List<PlayerEntity> playerList = world.getEntitiesWithinAABB(PlayerEntity.class,
          range, player -> player != null && player.getFoodStats().getFoodLevel() < LEVEL_TO_FEED_PLAYER);

        if (!citizenList.isEmpty() || !playerList.isEmpty())
        {
            citizenToServe.addAll(citizenList);
            playerToServe.addAll(playerList);

            if (InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), ItemStackUtils.CAN_EAT))
            {
                return COOK_SERVE_FOOD_TO_CITIZEN;
            }
            else if (!InventoryUtils.hasItemInProvider(getOwnBuilding(), ItemStackUtils.CAN_EAT))
            {
                return START_WORKING;
            }

            needsCurrently = ItemStackUtils.CAN_EAT;
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
