package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.coremod.colony.CitizenData;
import org.jetbrains.annotations.Nullable;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.util.ChatSpamFilter;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import static com.minecolonies.api.util.ItemStackUtils.ISFOOD;

/**
 * The AI task for citizens to execute when they are supposed to eat.
 */
public class EntityAIEatTask extends EntityAIBase
{
    /**
     * Chair searching range.
     */
    private final static int CHAIR_SEARCH_RANGE = 20;

    /**
     * The different types of AIStates related to eating.
     */
    public enum STATE
    {
        IDLE,
        CHECKING_FOR_FOOD,
        CHECKING_FOR_RESTAURANT,
        AT_RESTAURANT,
        EATING,
    }

    /**
     * The citizen, owner of the AI task.
     */
    private final EntityCitizen citizen;

    /**
     * The currently selected stack.
     */
    private ItemStack stack;

    /**
     * Ticks since the citizen started to eat.
     */
    private int eatingTicks;

    /**
     * The slot the citizen has selected (slot of the stack).
     */
    private int slot;

    /**
     * The chat spam filter to communicate through.
     */
    protected ChatSpamFilter chatSpamFilter;

    /**
     * The current state the entity is in.
     */
    private STATE currentState;

    /**
     * The restaurant this AI usually goes to.
     */
    private BlockPos restaurant;

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
        currentState = STATE.IDLE;
    }
    
    @Override
    public boolean shouldExecute()
    {
        if (!citizen.isOkayToEat())
        {
            return false;
        }

        if (currentState != STATE.IDLE && citizen.getDesiredActivity() != DesiredActivity.SLEEP)
        {
            return true;
        }

        if ((citizen.getDesiredActivity() == DesiredActivity.SLEEP || citizen.getCitizenData() != null && citizen.getCitizenData().getSaturation() >= CitizenConstants.HIGH_SATURATION))
        {
            return false;
        }

        if (shouldGetFood())
        {
            currentState = STATE.CHECKING_FOR_RESTAURANT;
            return true;
        }

        if (citizen.getCitizenData().getSaturation() < CitizenConstants.HIGH_SATURATION && tryToEat())
        {
            return true;
        } 

        cleanTask();
        return false;
    }

    @Override
    public void updateTask()
    {
        super.updateTask();
        switch (currentState)
        {
            case AT_RESTAURANT:
                searchForPlaceToEat();
                break;
            case CHECKING_FOR_RESTAURANT:
                searchForFood();
                break;
            case CHECKING_FOR_FOOD:
                tryToEat();
                break;
            case EATING:
                if(eatingTicks > 0 && slot != -1 && stack != null) {
                    eatingTicks--;
                    
                    if(eatingTicks == 0) 
                    {
                        citizen.stopActiveHand();
                        citizen.resetActiveHand();
                        citizen.setHeldItem(EnumHand.MAIN_HAND,ItemStack.EMPTY);
                        if (stack != null)
                        {
                            if (stack.getItem() instanceof ItemFood)
                            {
                                final int heal = ((ItemFood) stack.getItem()).getHealAmount(stack);
                                citizen.getCitizenData().increaseSaturation(heal);
                            }
                        }
                        citizen.getCitizenData().getCitizenHappinessHandler().setFoodModifier(true);
                        citizen.getCitizenData().markDirty();
                        cleanTask();
                    }
                }
                else
                {
                    citizen.dismountRidingEntity();
                    cleanTask();                
                }
                break;
            default:
                break;
                
        }
    }

    /**
     * Search for a place to eat in the restaurant.
     */
    private void searchForPlaceToEat()
    {
        if (citizen.getCitizenJobHandler().getColonyJob() == null
              && citizen.getCitizenData().getSaturation() >= CitizenConstants.LOW_SATURATION
              && checkForRandom())
        {
            final double distance1 = restaurant.distanceSq(citizen.posX, citizen.posY, citizen.posZ);

            if (distance1 < CHAIR_SEARCH_RANGE)
            {
                Vec3d vec3d = null;
                if(vec3d == null)
                {
                    vec3d = RandomPositionGenerator.getLandPos(citizen, 3, 3);
                    if (vec3d != null)
                    {
                        citizen.getNavigator().tryMoveToXYZ(vec3d.x, vec3d.y, vec3d.z, 0.2);
                    }
                }
            }
            else
            {
                citizen.getNavigator().tryMoveToXYZ(restaurant.getX(), restaurant.getY(), restaurant.getZ(), 3);
            }
        }
        else
        {
            tryToEat();
        }
    }

    /**
     * Cleanup the task and reset all the tasks.
     */
    private void cleanTask()
    {
        citizen.stopActiveHand();
        citizen.resetActiveHand();
        citizen.setHeldItem(EnumHand.MAIN_HAND,ItemStack.EMPTY);

        slot = -1;
        stack = null;
        eatingTicks = 0;
        currentState = STATE.IDLE;
        restaurant = null;
    }

    /**
     * Lets the citizen tryToEat to replenish saturation. 
     *  
     * @return return true or not if citizen eat food. 
     */
    private boolean tryToEat()
    {
        final CitizenData citizenData = citizen.getCitizenData();

        if (citizenData == null)
        {
            return false;
        }

        slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(citizen, ISFOOD);

        if (slot == -1)
        {
            citizenData.getCitizenHappinessHandler().setFoodModifier(false);
            return false;
        }

        final ItemStack stack = citizenData.getInventory().getStackInSlot(slot);
        if (!ItemStackUtils.isEmpty(stack) && stack.getItem() instanceof ItemFood && citizenData != null)
        {
            citizenData.getInventory().decrStackSize(slot, 1);
            citizenData.markDirty();
            eatFood();
            return true;
        }

        citizenData.getCitizenHappinessHandler().setFoodModifier(false);
        return false;
    }

    /**
     * Check if the citizen should get food, meaning, check if he checked for food today 
     * already and check if his saturation is decent if they have a job.
     * If no job then will check if they need food below high saturation.
     *
     * @return true if he should go search for food.
     */
    private boolean shouldGetFood()
    {
        //Do not go to building or restaurant at night.
        if (!CompatibilityUtils.getWorld(citizen).isDaytime())
        {
            return false;
        }
        
        return ((citizen.getCitizenData().getSaturation() < CitizenConstants.LOW_SATURATION
                && (citizen.getCitizenJobHandler().getColonyJob() != null && !citizen.getCitizenJobHandler().getColonyJob().hasCheckedForFoodToday()))
                || (citizen.getCitizenData().getSaturation() <= 0)
                || (citizen.getCitizenData().getSaturation() < CitizenConstants.LOW_SATURATION
                        && citizen.getCitizenJobHandler().getColonyJob() == null));
    }

    private void searchForFood()
    {
        if (citizen.getCitizenJobHandler().getColonyJob() != null && !citizen.getCitizenJobHandler().getColonyJob().hasCheckedForFoodToday())
        {
            @Nullable final AbstractBuilding homeBuilding = citizen.getCitizenColonyHandler().getWorkBuilding();
            if (homeBuilding != null && !citizen.isWorkerAtSiteWithMove(homeBuilding.getLocation(),2))
            {
                return;
            }

           citizen.getCitizenJobHandler().getColonyJob().setCheckedForFood();
            if (citizen.getCitizenJobHandler().isInHut(itemStack -> (!ItemStackUtils.isEmpty(itemStack) && itemStack.getItem() instanceof ItemFood)))
            {
                currentState = STATE.CHECKING_FOR_FOOD;
                return;
            }
        }

        if (restaurant == null)
        {
            final BlockPos goodCook = citizen.getCitizenColonyHandler().getColony().getBuildingManager().getBestRestaurant(citizen);

            if (goodCook == null)
            {
                sendChatMessage("com.minecolonies.coremod.ai.noRestaurant");
                currentState = STATE.IDLE;
                return;
            }
            restaurant = goodCook;
        }

        if (restaurant != null)
        {
             final boolean citizenAtRestaurant = citizen.isWorkerAtSiteWithMove(restaurant, 4);
             if (citizenAtRestaurant)
             {
                 currentState = STATE.AT_RESTAURANT;
             }
        }
    }

    /**
     * Consume a certain type of food.
     */
    private void eatFood()
    {
        eatingTicks = stack.getMaxItemUseDuration() * 2;
        currentState = STATE.EATING;
        citizen.setHeldItem(EnumHand.MAIN_HAND, stack);
        citizen.setActiveHand(EnumHand.MAIN_HAND);
    }

    private boolean checkForRandom()
    {
        return citizen.getRNG().nextInt(120) == 0;
    }

    private void sendChatMessage(final String text)
    {
        if (chatSpamFilter == null)
        {
          this.chatSpamFilter = new ChatSpamFilter(citizen.getCitizenData());
        }
        chatSpamFilter.talkWithoutSpam(text);
    }
}
