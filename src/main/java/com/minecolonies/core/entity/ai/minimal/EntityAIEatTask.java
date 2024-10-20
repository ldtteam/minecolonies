package com.minecolonies.core.entity.ai.minimal;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.IStateAI;
import com.minecolonies.api.entity.ai.statemachine.states.CitizenAIState;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.happiness.ExpirationBasedHappinessModifier;
import com.minecolonies.api.entity.citizen.happiness.StaticHappinessSupplier;
import com.minecolonies.api.items.IMinecoloniesFoodItem;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.entity.other.SittingEntity;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.network.messages.client.ItemParticleEffectMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;

import static com.minecolonies.api.util.ItemStackUtils.CAN_EAT;
import static com.minecolonies.api.util.ItemStackUtils.ISCOOKABLE;
import static com.minecolonies.api.util.constant.Constants.SECONDS_A_MINUTE;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.GuardConstants.BASIC_VOLUME;
import static com.minecolonies.api.util.constant.HappinessConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.core.entity.ai.minimal.EntityAIEatTask.EatingState.*;

/**
 * The AI task for citizens to execute when they are supposed to eat.
 */
public class EntityAIEatTask implements IStateAI
{
    /**
     * Max waiting time for food in minutes..
     */
    private static final int MINUTES_WAITING_TIME = 2;

    /**
     * Min distance in blocks to placeToPath block.
     */
    private static final int MIN_DISTANCE_TO_RESTAURANT = 3;

    /**
     * Time required to eat in seconds.
     */
    private static final int REQUIRED_TIME_TO_EAT = 5;

    /**
     * Amount of food to get by yourself
     */
    private static final int GET_YOURSELF_SATURATION = 30;

    /**
     * Limit to go to the restaurant.
     */
    public static final double RESTAURANT_LIMIT = 2.5;

    /**
     * The different types of AIStates related to eating.
     */
    public enum EatingState implements IState
    {
        CHECK_FOR_FOOD,
        GO_TO_HUT,
        SEARCH_RESTAURANT,
        GO_TO_RESTAURANT,
        WAIT_FOR_FOOD,
        GET_FOOD_YOURSELF,
        GO_TO_EAT_POS,
        EAT,
        DONE
    }

    /**
     * Minutes between consecutive food checks if saturation is low but not 0.
     */
    private static final int MINUTES_BETWEEN_FOOD_CHECKS = 5;

    /**
     * The citizen assigned to this task.
     */
    private final EntityCitizen citizen;

    /**
     * Ticks since we're waiting for something.
     */
    private int waitingTicks = 0;

    /**
     * Inventory slot with food in it.
     */
    private int foodSlot = -1;

    /**
     * The eating position to go to
     */
    private BlockPos eatPos = null;

    /**
     * Restaurant to which the citizen should path.
     */
    private BlockPos restaurantPos;

    /**
     * Timeout for walking
     */
    private int timeOutWalking = 0;

    /**
     * Instantiates this task.
     *
     * @param citizen the citizen.
     */
    public EntityAIEatTask(final EntityCitizen citizen)
    {
        super();
        this.citizen = citizen;

        citizen.getCitizenAI().addTransition(new TickingTransition<>(CitizenAIState.EATING, () -> true, () -> {
            reset();
            return CHECK_FOR_FOOD;
        }, 20));

        citizen.getCitizenAI().addTransition(new TickingTransition<>(DONE, () -> true, () -> CitizenAIState.IDLE, 1));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(CHECK_FOR_FOOD, () -> true, this::getFood, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(GO_TO_HUT, () -> true, this::goToHut, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(EAT, () -> true, this::eat, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(SEARCH_RESTAURANT, () -> true, this::searchRestaurant, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(GO_TO_RESTAURANT, () -> true, this::goToRestaurant, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(WAIT_FOR_FOOD, () -> true, this::waitForFood, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(GO_TO_EAT_POS, () -> true, this::goToEatingPlace, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(GET_FOOD_YOURSELF, () -> true, this::getFoodYourself, 20));
    }

    /**
     * Eats when it has food, or goes to check his building for food.
     *
     * @return
     */
    private EatingState getFood()
    {
        if (hasFood())
        {
            return EAT;
        }

        return GO_TO_HUT;
    }

    /**
     * Check if a citizen can eat something.
     *
     * @param citizenData the citizen to check.
     * @param stack       the stack to check.
     * @return true if so.
     */
    private boolean canEat(final ICitizenData citizenData, final ItemStack stack)
    {
        return citizenData.getHomeBuilding() == null || citizenData.getHomeBuilding().canEat(stack);
    }

    /**
     * Actual action of eating.
     *
     * @return the next state to go to, if successful idle.
     */
    private IState eat()
    {
        if (!hasFood())
        {
            return CHECK_FOR_FOOD;
        }

        final ICitizenData citizenData = citizen.getCitizenData();
        final ItemStack foodStack = citizenData.getInventory().getStackInSlot(foodSlot);
        if (!CAN_EAT.test(foodStack) || !canEat(citizenData, foodStack))
        {
            return CHECK_FOR_FOOD;
        }

        citizen.setItemInHand(InteractionHand.MAIN_HAND, foodStack);

        citizen.swing(InteractionHand.MAIN_HAND);
        citizen.playSound(SoundEvents.GENERIC_EAT, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(citizen.getRandom()));
        new ItemParticleEffectMessage(foodStack.copy(), citizen.getX(), citizen.getY(), citizen.getZ(), citizen.getXRot(), citizen.getYRot(), citizen.getEyeHeight()).sendToTrackingEntity(citizen);

        waitingTicks++;
        if (waitingTicks < REQUIRED_TIME_TO_EAT)
        {
            return EAT;
        }
        if (foodStack.getItem() instanceof IMinecoloniesFoodItem foodItem)
        {
            if (foodItem.getTier() == 3)
            {
                citizen.getCitizenData().getCitizenHappinessHandler().addModifier(new ExpirationBasedHappinessModifier(HADGREATFOOD, 2.0, new StaticHappinessSupplier(2.0), 5));
            }
            citizen.getCitizenData().getCitizenHappinessHandler().resetModifier(HADDECENTFOOD);
        }
        ItemStackUtils.consumeFood(foodStack, citizen, null);
        citizen.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        if (citizenData.getSaturation() < CitizenConstants.FULL_SATURATION && !citizenData.getInventory().getStackInSlot(foodSlot).isEmpty())
        {
            waitingTicks = 0;
            return EAT;
        }

        citizenData.setJustAte(true);
        return CitizenAIState.IDLE;
    }

    /**
     * Try to gather some food from the restaurant block.
     *
     * @return the next state to go to.
     */
    private EatingState getFoodYourself()
    {
        if (restaurantPos == null)
        {
            return SEARCH_RESTAURANT;
        }

        final IColony colony = citizen.getCitizenColonyHandler().getColony();
        final IBuilding cookBuilding = colony.getBuildingManager().getBuilding(restaurantPos);
        if (cookBuilding instanceof BuildingCook)
        {
            if (!citizen.isWorkerAtSiteWithMove(cookBuilding.getPosition(), MIN_DISTANCE_TO_RESTAURANT))
            {
                return GET_FOOD_YOURSELF;
            }
            InventoryUtils.transferFoodUpToSaturation(cookBuilding,
              citizen.getInventoryCitizen(),
              GET_YOURSELF_SATURATION,
              stack -> CAN_EAT.test(stack) && canEat(citizen.getCitizenData(), stack)
                         && !(cookBuilding.getModule(BuildingModules.ITEMLIST_FOODEXCLUSION)
                .isItemInList(new ItemStorage(stack))));
        }

        return WAIT_FOR_FOOD;
    }

    /**
     * Walks to the eating position
     *
     * @return
     */
    private EatingState goToEatingPlace()
    {
        if (eatPos == null || timeOutWalking++ > 400)
        {
            if (hasFood())
            {
                timeOutWalking = 0;
                return EAT;
            }
            else
            {
                waitingTicks++;
                if (waitingTicks > SECONDS_A_MINUTE * MINUTES_WAITING_TIME || (citizen.getCitizenData().getJob() instanceof AbstractJobGuard<?>
                                                                                 && !WorldUtil.isDayTime(citizen.level())))
                {
                    waitingTicks = 0;
                    return GET_FOOD_YOURSELF;
                }
            }
        }

        if (citizen.isWorkerAtSiteWithMove(eatPos, 2))
        {
            SittingEntity.sitDown(eatPos, citizen, TICKS_SECOND * 60);
            // Delay till they start eating
            timeOutWalking += 10;

            if (!hasFood())
            {
                waitingTicks++;
                if (waitingTicks > SECONDS_A_MINUTE * MINUTES_WAITING_TIME)
                {
                    waitingTicks = 0;
                    return GET_FOOD_YOURSELF;
                }
            }
        }

        return GO_TO_EAT_POS;
    }

    /**
     * Find a good place within the restaurant to eat.
     *
     * @return the next state to go to.
     */
    private BlockPos findPlaceToEat()
    {
        if (restaurantPos != null)
        {
            final IBuilding restaurant = citizen.getCitizenData().getColony().getBuildingManager().getBuilding(restaurantPos);
            if (restaurant instanceof BuildingCook)
            {
                return ((BuildingCook) restaurant).getNextSittingPosition();
            }
        }

        return null;
    }

    /**
     * Wander around the placeToPath a bit while waiting for the cook to deliver food. After waiting for a certain time, get the food yourself.
     *
     * @return the next state to go to.
     */
    private EatingState waitForFood()
    {
        final ICitizenData citizenData = citizen.getCitizenData();
        final IColony colony = citizenData.getColony();
        restaurantPos = colony.getBuildingManager().getBestBuilding(citizen, BuildingCook.class);

        if (restaurantPos == null)
        {
            return SEARCH_RESTAURANT;
        }

        if (!colony.getBuildingManager().getBuilding(restaurantPos).isInBuilding(citizen.blockPosition()))
        {
            return GO_TO_RESTAURANT;
        }


        eatPos = findPlaceToEat();
        if (eatPos != null)
        {
            return GO_TO_EAT_POS;
        }

        if (hasFood())
        {
            return EAT;
        }

        return WAIT_FOR_FOOD;
    }

    /**
     * Go to the hut to try to get food there first.
     *
     * @return the next state to go to.
     */
    private EatingState goToHut()
    {
        final IBuilding buildingWorker = citizen.getCitizenData().getWorkBuilding();
        if (buildingWorker == null)
        {
            return SEARCH_RESTAURANT;
        }

        if (citizen.isWorkerAtSiteWithMove(buildingWorker.getPosition(), MIN_DISTANCE_TO_RESTAURANT))
        {
            final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(buildingWorker, stack -> CAN_EAT.test(stack) && canEat(citizen.getCitizenData(), stack));
            if (slot != -1)
            {
                if (InventoryUtils.transferFoodUpToSaturation(buildingWorker,
                  citizen.getInventoryCitizen(),
                  GET_YOURSELF_SATURATION,
                  stack -> CAN_EAT.test(stack) && canEat(citizen.getCitizenData(), stack)) > 0)
                {
                    return EAT;
                }
            }
            return SEARCH_RESTAURANT;
        }

        return GO_TO_HUT;
    }

    /**
     * Go to the previously found placeToPath to get some food.
     *
     * @return the next state to go to.
     */
    private EatingState goToRestaurant()
    {
        if (restaurantPos != null)
        {
            final IBuilding building = citizen.getCitizenColonyHandler().getColony().getBuildingManager().getBuilding(restaurantPos);
            if (building != null)
            {
                if (building.isInBuilding(citizen.blockPosition()))
                {
                    return WAIT_FOR_FOOD;
                }
                else if (!citizen.isWorkerAtSiteWithMove(restaurantPos, MIN_DISTANCE_TO_RESTAURANT))
                {
                    return GO_TO_RESTAURANT;
                }
            }
        }
        return SEARCH_RESTAURANT;
    }

    /**
     * Search for a placeToPath within the colony of the citizen.
     *
     * @return the next state to go to.
     */
    private EatingState searchRestaurant()
    {
        final ICitizenData citizenData = citizen.getCitizenData();
        final IColony colony = citizenData.getColony();
        if (citizenData.getWorkBuilding() != null)
        {
            restaurantPos = colony.getBuildingManager().getBestBuilding(citizenData.getWorkBuilding().getPosition(), BuildingCook.class);
        }
        else if (citizenData.getHomeBuilding() != null)
        {
            restaurantPos = colony.getBuildingManager().getBestBuilding(citizenData.getHomeBuilding().getPosition(), BuildingCook.class);
        }
        else
        {
            restaurantPos = colony.getBuildingManager().getBestBuilding(citizen, BuildingCook.class);
        }

        final IJob<?> job = citizen.getCitizenJobHandler().getColonyJob();
        if (job != null && citizenData.isWorking())
        {
            citizenData.setWorking(false);
        }

        if (restaurantPos == null)
        {
            citizenData.triggerInteraction(new StandardInteraction(Component.translatableEscape(NO_RESTAURANT), ChatPriority.BLOCKING));
            return CHECK_FOR_FOOD;
        }
        return GO_TO_RESTAURANT;
    }

    /**
     * Checks if the citizen has food in the inventory and makes a decision based on that.
     *
     * @return the next state to go to.
     */
    private boolean hasFood()
    {
        final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(citizen, stack -> CAN_EAT.test(stack) && canEat(citizen.getCitizenData(), stack));
        if (slot != -1)
        {
            foodSlot = slot;
            return true;
        }

        final ICitizenData citizenData = citizen.getCitizenData();

        if (InventoryUtils.hasItemInItemHandler(citizen.getInventoryCitizen(), ISCOOKABLE))
        {
            citizenData.triggerInteraction(new StandardInteraction(Component.translatableEscape(RAW_FOOD), ChatPriority.PENDING));
        }
        else if (InventoryUtils.hasItemInItemHandler(citizen.getInventoryCitizen(), stack -> CAN_EAT.test(stack) && !canEat(citizenData, stack)))
        {
            if (citizenData.isChild())
            {
                citizenData.triggerInteraction(new StandardInteraction(Component.translatableEscape(BETTER_FOOD_CHILDREN), ChatPriority.BLOCKING));
            }
            else
            {
                citizenData.triggerInteraction(new StandardInteraction(Component.translatableEscape(BETTER_FOOD), ChatPriority.BLOCKING));
            }
        }

        return false;
    }

    /**
     * Resets the state of the AI.
     */
    private void reset()
    {
        waitingTicks = 0;
        foodSlot = -1;
        citizen.releaseUsingItem();
        citizen.stopUsingItem();
        citizen.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        restaurantPos = null;
        eatPos = null;
    }
}
