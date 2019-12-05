package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.interactionhandling.InteractionValidatorPredicates;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteractionResponseHandler;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.network.messages.ItemParticleEffectMessage;
import com.minecolonies.coremod.util.AdvancementUtils;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.Food;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.EnumSet;

import static com.minecolonies.api.util.ItemStackUtils.*;
import static com.minecolonies.api.util.constant.CitizenConstants.HIGH_SATURATION;
import static com.minecolonies.api.util.constant.CitizenConstants.LOW_SATURATION;
import static com.minecolonies.api.util.constant.Constants.SECONDS_A_MINUTE;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.GuardConstants.BASIC_VOLUME;
import static com.minecolonies.api.util.constant.TranslationConstants.NO_RESTAURANT;
import static com.minecolonies.api.util.constant.TranslationConstants.RAW_FOOD;
import static com.minecolonies.coremod.entity.ai.citizen.cook.EntityAIWorkCook.AMOUNT_OF_FOOD_TO_SERVE;
import static com.minecolonies.coremod.entity.ai.minimal.EntityAIEatTask.EatingState.*;

/**
 * The AI task for citizens to execute when they are supposed to eat.
 */
public class EntityAIEatTask extends Goal
{
    /**
     * Minutes between consecutive food checks.
     */
    private static final int MINUTES_BETWEEN_FOOD_CHECKS = 2;

    /**
     * Max waiting time for food in minutes..
     */
    private static final int MINUTES_WAITING_TIME = 2;

    /**
     * Min distance in blocks to placeToPath block.
     */
    private static final int MIN_DISTANCE_TO_RESTAURANT = 5;

    /**
     * Max distance from the restaurant block the citizen should eat in x/z.
     */
    private static final int PLACE_TO_EAT_DISTANCE = 5;

    /**
     * Time required to eat in seconds.
     */
    private static final int REQUIRED_TIME_TO_EAT = 5;

    /**
     * The different types of AIStates related to eating.
     */
    public enum EatingState
    {
        IDLE,
        CHECK_FOR_FOOD,
        GO_TO_HUT,
        SEARCH_RESTAURANT,
        GO_TO_RESTAURANT,
        WAIT_FOR_FOOD,
        GET_FOOD_YOURSELF,
        FIND_PLACE_TO_EAT,
        EAT
    }

    /**
     * The citizen assigned to this task.
     */
    private final EntityCitizen citizen;

    /**
     * The state the task is in currently.
     */
    private EatingState currentState = IDLE;

    /**
     * Ticks since we're waiting for something.
     */
    private int waitingTicks = 0;

    /**
     * Inventory slot with food in it.
     */
    private int foodSlot = -1;

    /**
     * Restaurant to which the citizen should path.
     */
    private BlockPos placeToPath;


    static
    {
        InteractionValidatorPredicates.map.put(new TranslationTextComponent(RAW_FOOD),
          citizen -> InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(citizen.getInventory(), ISCOOKABLE) > 0 && InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(citizen.getInventory(), ISFOOD) == 0);
        InteractionValidatorPredicates.map.put(new TranslationTextComponent(NO_RESTAURANT),
          citizen -> citizen.getColony() != null && citizen.getSaturation() <= LOW_SATURATION && citizen.getCitizenEntity().isPresent() && citizen.getColony().getBuildingManager().getBestRestaurant(citizen.getCitizenEntity().get()) == null && InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(citizen.getInventory(), ISFOOD) == 0);
    }


    /**
     * Instantiates this task.
     *
     * @param citizen the citizen.
     */
    public EntityAIEatTask(final EntityCitizen citizen)
    {
        super();
        this.citizen = citizen;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean shouldExecute()
    {
        if (citizen.getDesiredActivity() == DesiredActivity.SLEEP)
        {
            return false;
        }

        if (currentState != IDLE)
        {
            return true;
        }

        final ICitizenData citizenData = citizen.getCitizenData();
        if (citizenData == null || citizen.getCitizenData().getSaturation() >= HIGH_SATURATION || (!citizen.isOkayToEat()
                                                                                                                      && citizen.getCitizenData().getSaturation() > 0))
        {
            return false;
        }

        if (citizenData.getSaturation() <= CitizenConstants.AVERAGE_SATURATION)
        {
            waitingTicks++;
            return (waitingTicks >= TICKS_SECOND * SECONDS_A_MINUTE * MINUTES_BETWEEN_FOOD_CHECKS && citizenData.getSaturation() < CitizenConstants.LOW_SATURATION)
                     || citizenData.getJob() == null || citizenData.getSaturation() == 0;
        }

        return (citizenData.getSaturation() <= HIGH_SATURATION && checkForFood(citizenData) == EAT && citizen.isOkayToEat());
    }

    @Override
    public void tick()
    {
        final ICitizenData citizenData = citizen.getCitizenData();
        if (citizenData == null)
        {
            return;
        }

        switch (currentState)
        {
            case CHECK_FOR_FOOD:
                currentState = checkForFood(citizenData);
                return;
            case GO_TO_HUT:
                currentState = goToHut(citizenData);
                return;
            case SEARCH_RESTAURANT:
                currentState = searchRestaurant(citizenData);
                return;
            case GO_TO_RESTAURANT:
                currentState = goToRestaurant();
                return;
            case WAIT_FOR_FOOD:
                currentState = waitForFood(citizenData);
                return;
            case FIND_PLACE_TO_EAT:
                currentState = findPlaceToEat();
                return;
            case GET_FOOD_YOURSELF:
                currentState = getFoodYourself();
                return;
            case EAT:
                currentState = eat(citizenData);
                return;
            default:
                reset();
                break;
        }
    }

    /**
     * Actual action of eating.
     *
     * @param citizenData the citizen.
     * @return the next state to go to, if successful idle.
     */
    private EatingState eat(final ICitizenData citizenData)
    {
        if (foodSlot == -1)
        {
            return CHECK_FOR_FOOD;
        }

        final ItemStack stack = citizenData.getInventory().getStackInSlot(foodSlot);
        if (!CAN_EAT.test(stack))
        {
            return CHECK_FOR_FOOD;
        }

        citizen.setHeldItem(Hand.MAIN_HAND, stack);

        if (waitingTicks % 10 == 0)
        {
            citizen.swingArm(Hand.MAIN_HAND);
            citizen.playSound(SoundEvents.ENTITY_GENERIC_EAT, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(citizen.getRandom()));
            Network.getNetwork()
              .sendToTrackingEntity(new ItemParticleEffectMessage(citizen.getHeldItemMainhand(),
                citizen.posX,
                citizen.posY,
                citizen.posZ,
                citizen.rotationPitch,
                citizen.rotationYaw,
                citizen.getEyeHeight()), citizen);
        }

        waitingTicks++;
        if (waitingTicks < TICKS_SECOND * REQUIRED_TIME_TO_EAT)
        {
            return EAT;
        }


        final Food itemFood = stack.getItem().getFood();
        citizenData.increaseSaturation(itemFood.getHealing() / 2.0);
        citizenData.getInventory().extractItem(foodSlot, 1, false);

        IColony citizenColony = citizen.getCitizenColonyHandler().getColony();
        if (citizenColony != null )
        {
            AdvancementUtils.TriggerAdvancementPlayersForColony(citizenColony, playerMP -> AdvancementTriggers.CITIZEN_EAT_FOOD.trigger(playerMP, stack));
        }

        citizenData.markDirty();
        citizen.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);

        if (citizenData.getSaturation() < CitizenConstants.FULL_SATURATION && !stack.isEmpty())
        {
            waitingTicks = 0;
            return EAT;
        }
        return IDLE;
    }

    /**
     * Try to gather some food from the restaurant block.
     *
     * @return the next state to go to.
     */
    private EatingState getFoodYourself()
    {
        if (placeToPath == null)
        {
            return SEARCH_RESTAURANT;
        }
        final IColony colony = citizen.getCitizenColonyHandler().getColony();
        if (colony == null)
        {
            return IDLE;
        }

        final BlockPos restaurant = colony.getBuildingManager().getBestRestaurant(citizen);
        if (restaurant == null)
        {
            return SEARCH_RESTAURANT;
        }

        final IBuilding cookBuilding = colony.getBuildingManager().getBuilding(restaurant);
        if (cookBuilding instanceof BuildingCook)
        {
            InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoNextFreeSlotInItemHandler(
              cookBuilding.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElseGet(null),
              CAN_EAT,
              AMOUNT_OF_FOOD_TO_SERVE,
              citizen.getInventoryCitizen());
            return WAIT_FOR_FOOD;
        }

        return IDLE;
    }

    /**
     * Find a good place within the restaurant to eat.
     *
     * @return the next state to go to.
     */
    private EatingState findPlaceToEat()
    {
        if (placeToPath == null)
        {
            final Vec3d placeToEat = RandomPositionGenerator.getLandPos(citizen, PLACE_TO_EAT_DISTANCE, 0);
            if (placeToEat == null)
            {
                waitingTicks = 0;
                return EAT;
            }
            placeToPath = new BlockPos(placeToEat);
        }

        if (citizen.isWorkerAtSiteWithMove(placeToPath, MIN_DISTANCE_TO_RESTAURANT))
        {
            waitingTicks = 0;
            return EAT;
        }
        return FIND_PLACE_TO_EAT;
    }

    /**
     * Wander around the placeToPath a bit while waiting for the cook to deliver food.
     * After waiting for a certain time, get the food yourself.
     *
     * @param citizenData the citizen to check.
     * @return the next state to go to.
     */
    private EatingState waitForFood(final ICitizenData citizenData)
    {
        final IColony colony = citizenData.getColony();
        placeToPath = colony.getBuildingManager().getBestRestaurant(citizen);

        if (placeToPath == null)
        {
            return SEARCH_RESTAURANT;
        }

        if (BlockPosUtil.getDistance2D(placeToPath, citizen.getPosition()) > MIN_DISTANCE_TO_RESTAURANT)
        {
            return GO_TO_RESTAURANT;
        }

        final EatingState state = checkForFood(citizenData);
        if (state == EAT)
        {
            return FIND_PLACE_TO_EAT;
        }
        else if (state == IDLE)
        {
            reset();
            return IDLE;
        }

        waitingTicks++;

        if (waitingTicks > TICKS_SECOND * SECONDS_A_MINUTE * MINUTES_WAITING_TIME)
        {
            waitingTicks = 0;
            return GET_FOOD_YOURSELF;
        }
        return WAIT_FOR_FOOD;
    }

    /**
     * Go to the hut to try to get food there first.
     *
     * @return the next state to go to.
     */
    private EatingState goToHut(final ICitizenData data)
    {
        final IBuildingWorker buildingWorker = data.getWorkBuilding();
        if (buildingWorker == null)
        {
            return SEARCH_RESTAURANT;
        }

        if (citizen.isWorkerAtSiteWithMove(buildingWorker.getPosition(), MIN_DISTANCE_TO_RESTAURANT))
        {
            final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(buildingWorker, CAN_EAT);
            if (slot != -1)
            {
                InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoNextFreeSlotInItemHandler(
                  buildingWorker.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElseGet(null),
                  CAN_EAT,
                  buildingWorker.getBuildingLevel() * AMOUNT_OF_FOOD_TO_SERVE,
                  citizen.getInventoryCitizen());
                return CHECK_FOR_FOOD;
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
        if (placeToPath == null)
        {
            return SEARCH_RESTAURANT;
        }

        if (citizen.isWorkerAtSiteWithMove(placeToPath, MIN_DISTANCE_TO_RESTAURANT))
        {
            return WAIT_FOR_FOOD;
        }
        return SEARCH_RESTAURANT;
    }

    /**
     * Search for a placeToPath within the colony of the citizen.
     *
     * @param citizenData the citizen.
     * @return the next state to go to.
     */
    private EatingState searchRestaurant(final ICitizenData citizenData)
    {
        final IColony colony = citizenData.getColony();
        placeToPath = colony.getBuildingManager().getBestRestaurant(citizen);

        final int uncookedFood = InventoryUtils.findFirstSlotInProviderNotEmptyWith(citizen, ISCOOKABLE);
        boolean complained = false;
        if (uncookedFood != -1)
        {
            complained = true;
            citizenData.triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(RAW_FOOD), ChatPriority.PENDING));
        }

        if (placeToPath == null)
        {
            if (!complained)
            {
                citizenData.triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(NO_RESTAURANT), ChatPriority.BLOCKING));
            }
            return IDLE;
        }
        // Reset AI when going to the restaurant to eat
        if (citizen.getCitizenJobHandler().getColonyJob() != null)
        {
            citizen.getCitizenJobHandler().getColonyJob().resetAIAfterEating();
        }
        return GO_TO_RESTAURANT;
    }

    /**
     * Checks if the citizen has food in the inventory and makes a decision based on that.
     *
     * @param citizenData the citizen to check.
     * @return the next state to go to.
     */
    private EatingState checkForFood(final ICitizenData citizenData)
    {
        final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(citizen, CAN_EAT);

        if (slot == -1)
        {
            citizenData.getCitizenHappinessHandler().setFoodModifier(false);
            if ((citizenData.getSaturation() < CitizenConstants.LOW_SATURATION || citizen.isIdlingAtJob()) && citizenData.getSaturation() < HIGH_SATURATION)
            {
                return GO_TO_HUT;
            }

            reset();
            currentState = IDLE;
            return IDLE;
        }
        foodSlot = slot;
        return EAT;
    }

    /**
     * Resets the state of the AI.
     */
    private void reset()
    {
        waitingTicks = 0;
        foodSlot = -1;
        citizen.stopActiveHand();
        citizen.resetActiveHand();
        citizen.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
        placeToPath = null;
        currentState = CHECK_FOR_FOOD;
    }
}
