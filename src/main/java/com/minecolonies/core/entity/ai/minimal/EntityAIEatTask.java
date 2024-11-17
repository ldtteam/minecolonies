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
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenFoodHandler;
import com.minecolonies.api.util.*;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.colony.jobs.JobCook;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.other.SittingEntity;
import com.minecolonies.core.network.messages.client.ItemParticleEffectMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.minecolonies.api.util.ItemStackUtils.ISCOOKABLE;
import static com.minecolonies.api.util.constant.CitizenConstants.FULL_SATURATION;
import static com.minecolonies.api.util.constant.Constants.SECONDS_A_MINUTE;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.GuardConstants.BASIC_VOLUME;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.RESTAURANT_MENU;
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
    private static final int MIN_DISTANCE_TO_RESTAURANT = 2;

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
     * The actual restaurant.
     */
    private IBuilding restaurant;

    /**
     * Timeout for walking
     */
    private int timeOutWalking = 0;

    /**
     * The food we've eaten in a meal.
     */
    private Set<Item> eatenFood = new LinkedHashSet<>();

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
        if (!FoodUtils.canEat(foodStack, citizenData.getHomeBuilding(), citizenData.getWorkBuilding()))
        {
            return CHECK_FOR_FOOD;
        }

        citizen.setItemInHand(InteractionHand.MAIN_HAND, foodStack);

        citizen.swing(InteractionHand.MAIN_HAND);
        citizen.playSound(SoundEvents.GENERIC_EAT, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(citizen.getRandom()));
        Network.getNetwork()
          .sendToTrackingEntity(new ItemParticleEffectMessage(citizen.getMainHandItem(),
            citizen.getX(),
            citizen.getY(),
            citizen.getZ(),
            citizen.getXRot(),
            citizen.getYRot(),
            citizen.getEyeHeight()), citizen);

        waitingTicks++;
        if (waitingTicks < REQUIRED_TIME_TO_EAT)
        {
            return EAT;
        }

        final ICitizenFoodHandler foodHandler = citizenData.getCitizenFoodHandler();
        if (eatenFood.isEmpty())
        {
            foodHandler.addLastEaten(foodStack.getItem());
        }
        eatenFood.add(foodStack.getItem());

        ItemStackUtils.consumeFood(foodStack, citizen, null);
        citizen.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        if (citizenData.getSaturation() < FULL_SATURATION && !citizenData.getInventory().getStackInSlot(foodSlot).isEmpty())
        {
            waitingTicks = 0;
            return EAT;
        }

        for (final Item foodItem : eatenFood)
        {
            if (foodHandler.getLastEaten() != foodItem)
            {
                foodHandler.addLastEaten(foodItem);
            }
        }
        eatenFood.clear();
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

        final IColony colony = citizen.getCitizenColonyHandler().getColonyOrRegister();
        final IBuilding cookBuilding = colony.getBuildingManager().getBuilding(restaurantPos);
        if (cookBuilding instanceof BuildingCook)
        {
            if (!citizen.isWorkerAtSiteWithMove(cookBuilding.getPosition(), MIN_DISTANCE_TO_RESTAURANT))
            {
                return GET_FOOD_YOURSELF;
            }

            final ItemStorage storageToGet = FoodUtils.checkForFoodInBuilding(citizen.getCitizenData(), cookBuilding.getModule(RESTAURANT_MENU).getMenu(), cookBuilding);
            if (storageToGet != null)
            {
                int homeBuildingLevel = citizen.getCitizenData().getHomeBuilding() == null ? 1 : citizen.getCitizenData().getHomeBuilding().getBuildingLevel();
                int qty = (int) (Math.max(1.0,
                    (FULL_SATURATION - citizen.getCitizenData().getSaturation()) / FoodUtils.getFoodValue(storageToGet.getItemStack(), citizen) * homeBuildingLevel / 2.0));
                InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(cookBuilding, storageToGet, qty, citizen.getInventoryCitizen());
                return EAT;
            }

            if (citizen.getCitizenData().getJob() instanceof JobCook jobCook && jobCook.getBuildingPos().equals(restaurantPos) && MathUtils.RANDOM.nextInt(TICKS_SECOND) <= 0)
            {
                reset();
                return DONE;
            }
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
                                                                                 && !WorldUtil.isDayTime(citizen.level)))
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

        restaurant = colony.getBuildingManager().getBuilding(restaurantPos);
        if (!restaurant.isInBuilding(citizen.blockPosition()))
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
            final int slot = FoodUtils.getBestFoodForCitizen(citizen.getInventoryCitizen(), citizen.getCitizenData(), null);
            if (slot != -1)
            {
                final ItemStorage storageToGet = FoodUtils.checkForFoodInBuilding(citizen.getCitizenData(), null, buildingWorker);
                if (storageToGet != null && InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(buildingWorker, storageToGet, citizen.getInventoryCitizen()))
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
            final IBuilding building = citizen.getCitizenColonyHandler().getColonyOrRegister().getBuildingManager().getBuilding(restaurantPos);
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
            citizenData.triggerInteraction(new StandardInteraction(Component.translatable(NO_RESTAURANT), ChatPriority.BLOCKING));
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
        final int slot = FoodUtils.getBestFoodForCitizen(citizen.getInventoryCitizen(), citizen.getCitizenData(), restaurant == null ? null : restaurant.getModule(RESTAURANT_MENU).getMenu());
        if (slot != -1)
        {
            foodSlot = slot;
            return true;
        }

        final ICitizenData citizenData = citizen.getCitizenData();

        if (InventoryUtils.hasItemInItemHandler(citizen.getInventoryCitizen(), ISCOOKABLE))
        {
            citizenData.triggerInteraction(new StandardInteraction(Component.translatable(RAW_FOOD), ChatPriority.PENDING));
        }
        else if (InventoryUtils.hasItemInItemHandler(citizen.getInventoryCitizen(), stack -> FoodUtils.canEat(stack, citizenData.getHomeBuilding(), citizenData.getWorkBuilding())))
        {
            if (citizenData.isChild())
            {
                citizenData.triggerInteraction(new StandardInteraction(Component.translatable(BETTER_FOOD_CHILDREN), ChatPriority.BLOCKING));
            }
            else
            {
                citizenData.triggerInteraction(new StandardInteraction(Component.translatable(BETTER_FOOD), ChatPriority.BLOCKING));
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
        eatenFood.clear();
        restaurant = null;
    }
}
