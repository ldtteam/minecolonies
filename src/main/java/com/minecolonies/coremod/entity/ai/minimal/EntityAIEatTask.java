package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.util.ChatSpamFilter;
import com.minecolonies.coremod.network.messages.ItemParticleEffectMessage;
import com.minecolonies.coremod.util.SoundUtils;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import static com.minecolonies.api.util.ItemStackUtils.ISFOOD;
import static com.minecolonies.api.util.constant.Constants.SECONDS_A_MINUTE;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.GuardConstants.BASIC_VOLUME;
import static com.minecolonies.coremod.entity.ai.citizen.cook.EntityAIWorkCook.AMOUNT_OF_FOOD_TO_SERVE;
import static com.minecolonies.coremod.entity.ai.minimal.EntityAIEatTask.STATE.*;

/**
 * The AI task for citizens to execute when they are supposed to eat.
 */
public class EntityAIEatTask extends EntityAIBase
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
    private static final int REQUIRED_TIME_TO_EAT  = 5;

    /**
     * Filter for message propagation.
     */
    protected ChatSpamFilter chatSpamFilter;

    /**
     * The different types of AIStates related to eating.
     */
    public enum STATE
    {
        IDLE,
        CHECK_FOR_FOOD,
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
    private STATE currentState = IDLE;

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

    /**
     * Instantiates this task.
     *
     * @param citizen the citizen.
     */
    public EntityAIEatTask(final EntityCitizen citizen)
    {
        super();
        this.citizen = citizen;
        this.setMutexBits(1);
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

        final CitizenData citizenData = citizen.getCitizenData();
        if (citizenData == null || citizen.getCitizenData().getSaturation() >= CitizenConstants.HIGH_SATURATION || !citizen.isOkayToEat())
        {
            return false;
        }

        if (citizenData.getSaturation() <= CitizenConstants.AVERAGE_SATURATION)
        {
            waitingTicks++;
            return waitingTicks >= TICKS_SECOND * SECONDS_A_MINUTE * MINUTES_BETWEEN_FOOD_CHECKS || citizen.getCitizenData().getSaturation() < CitizenConstants.LOW_SATURATION || citizenData.getJob() == null;
        }

        return false;
    }

    @Override
    public void updateTask()
    {
        if (chatSpamFilter == null)
        {
            chatSpamFilter = new ChatSpamFilter(citizen.getCitizenData());
        }

        final CitizenData citizenData = citizen.getCitizenData();
        if (citizenData == null)
        {
            return;
        }

        switch(currentState)
        {
            case CHECK_FOR_FOOD:
                currentState = checkForFood(citizenData);
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
     * @return the next state to go to, if successful idle.
     * @param citizenData the citizen.
     */
    private STATE eat(final CitizenData citizenData)
    {
        if (foodSlot == -1)
        {
            return WAIT_FOR_FOOD;
        }

        final ItemStack stack = citizenData.getInventory().getStackInSlot(foodSlot);
        if (!ISFOOD.test(stack))
        {
            return WAIT_FOR_FOOD;
        }

        citizen.setHeldItem(EnumHand.MAIN_HAND, stack);

        if (waitingTicks % 10 == 0)
        {
            citizen.swingArm(EnumHand.MAIN_HAND);
            citizen.playSound(SoundEvents.ENTITY_GENERIC_EAT, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(citizen.getRandom()));
            MineColonies.getNetwork().sendToAllTracking(new ItemParticleEffectMessage(citizen.getHeldItemMainhand(), citizen.posX, citizen.posY, citizen.posZ, citizen.rotationPitch, citizen.rotationYaw, citizen.getEyeHeight()), citizen);
        }

        waitingTicks ++;
        if (waitingTicks < TICKS_SECOND * REQUIRED_TIME_TO_EAT)
        {
            return EAT;
        }

        final ItemFood itemFood = (ItemFood) stack.getItem();
        citizenData.increaseSaturation(itemFood.getHealAmount(stack) / 2.0);
        citizenData.getInventory().decrStackSize(foodSlot, 1);
        citizenData.markDirty();
        citizen.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);

        if (citizenData.getSaturation() < CitizenConstants.FULL_SATURATION && !stack.isEmpty())
        {
            waitingTicks = 0;
            return EAT;
        }
        return IDLE;
    }

    /**
     * Try to gather some food from the restaurant block.
     * @return the next state to go to.
     */
    private STATE getFoodYourself()
    {
        if (placeToPath == null)
        {
            return SEARCH_RESTAURANT;
        }
        final Colony colony = citizen.getCitizenColonyHandler().getColony();
        if (colony == null)
        {
            return IDLE;
        }

        final BlockPos restaurant = colony.getBuildingManager().getBestRestaurant(citizen);
        if (restaurant == null)
        {
            return SEARCH_RESTAURANT;
        }

        final AbstractBuilding cookBuilding = colony.getBuildingManager().getBuilding(restaurant);
        if (cookBuilding instanceof BuildingCook)
        {
            InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoNextFreeSlotInItemHandler(
              cookBuilding.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null),
              ISFOOD,
              AMOUNT_OF_FOOD_TO_SERVE,
              new InvWrapper(citizen.getInventoryCitizen()));
            return WAIT_FOR_FOOD;
        }

        return IDLE;
    }

    /**
     * Find a good place within the restaurant to eat.
     * @return the next state to go to.
     */
    private STATE findPlaceToEat()
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
     * @return the next state to go to.
     * @param citizenData the citizen to check.
     */
    private STATE waitForFood(final CitizenData citizenData)
    {
        final Colony colony = citizenData.getColony();
        placeToPath = colony.getBuildingManager().getBestRestaurant(citizen);

        if (placeToPath == null)
        {
            return SEARCH_RESTAURANT;
        }

        if (BlockPosUtil.getDistance2D(placeToPath, citizen.getPosition()) > MIN_DISTANCE_TO_RESTAURANT)
        {
            return GO_TO_RESTAURANT;
        }

        final STATE state = checkForFood(citizenData);
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
     * Go to the previously found placeToPath to get some food.
     * @return the next state to go to.
     */
    private STATE goToRestaurant()
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
     * @return the next state to go to.
     * @param citizenData the citizen.
     */
    private STATE searchRestaurant(final CitizenData citizenData)
    {
        final Colony colony = citizenData.getColony();
        placeToPath = colony.getBuildingManager().getBestRestaurant(citizen);

        if (placeToPath == null)
        {
            chatSpamFilter.talkWithoutSpam("com.minecolonies.coremod.ai.noRestaurant");
            return SEARCH_RESTAURANT;
        }

        return GO_TO_RESTAURANT;
    }

    /**
     * Checks if the citizen has food in the inventory and makes a decision based on that.
     * @param citizenData the citizen to check.
     * @return the next state to go to.
     */
    private STATE checkForFood(final CitizenData citizenData)
    {
        final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(citizen, ISFOOD);

        if (slot == -1)
        {
            citizenData.getCitizenHappinessHandler().setFoodModifier(false);
            if ((citizenData.getSaturation() < CitizenConstants.LOW_SATURATION || citizen.isIdlingAtJob()) && citizenData.getSaturation() < CitizenConstants.HIGH_SATURATION)
            {
                return SEARCH_RESTAURANT;
            }

            reset();
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
        citizen.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
        placeToPath = null;
        currentState = CHECK_FOR_FOOD;
    }
}
