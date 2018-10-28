package com.minecolonies.coremod.entity.ai.minimal;


import org.jetbrains.annotations.Nullable;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.util.ChatSpamFilter;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class EntityAIEatTask extends EntityAIBase
{

    private final EntityCitizen citizen;
    private int eatingTicks;
    private ItemStack stack;
    private int slot;
    protected final ChatSpamFilter      chatSpamFilter;
    
    private boolean checkingForFood;
    private boolean checkingRestaurant;
    private boolean citizenAtRestaurant;
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
        this.chatSpamFilter = new ChatSpamFilter(citizen.getCitizenData());
    }
    
    @Override
    public boolean shouldExecute()
    {
        if (citizen.getCitizenSleepHandler().isAsleep() || citizen.getCitizenData().getSaturation() > CitizenConstants.HIGH_SATURATION)
        {
            return false;
        }

        if (eatingTicks > 0 || (checkingForFood && !citizenAtRestaurant && (citizen.getCitizenData().getSaturation() < CitizenConstants.HIGH_SATURATION))
                || (checkingForFood && citizenAtRestaurant && (citizen.getCitizenData().getSaturation() < CitizenConstants.LOW_SATURATION)))
        {
            return true;
        }

        if (!citizen.isOkayToEat() ||
             (citizen.getCitizenData().getSaturation() > CitizenConstants.HIGH_SATURATION))
        {
            cleanTask();
            return false;
        }

        if (shouldGetFood())
        {
            checkingForFood = true;
            checkingRestaurant = false;
            return true;
        }

        if (citizen.getCitizenData().getSaturation() < CitizenConstants.HIGH_SATURATION)
        {
            if (eatingTicks == 0 && tryToEat() && !citizenAtRestaurant)
            {
                return true;
            }
        } 

        cleanTask();
        return false;
        
    }

    @Override
    public void updateTask()
    {
        super.updateTask();
        if (checkingForFood)
        {
            if (citizenAtRestaurant)
            {
                tryToEat();
            }
            else if (checkingRestaurant || !citizenAtRestaurant)
            {
                searchForFood();
            }
        }
        else
        {
            if(eatingTicks > 0 && slot != -1 && stack != null) {
                eatingTicks--;
                
                if(eatingTicks == 0) 
                {
                    citizen.dismountRidingEntity();
                    citizen.resetActiveHand();
                    if (stack != null)
                    {
                        if (stack.getItem() instanceof ItemFood)
                        {
                            final int heal = ((ItemFood) stack.getItem()).getHealAmount(stack);
                            citizen.getCitizenData().increaseSaturation(heal);
                        }
                    }
                    citizen.getCitizenData().getInventory().decrStackSize(slot, 1);
                    citizen.getCitizenData().markDirty();
                    cleanTask();
                }
            }
        }
    }

    protected void cleanTask()
    {
        checkingForFood = false;
        checkingRestaurant = false;
        citizenAtRestaurant = false;
        slot = -1;
        stack = null;
        eatingTicks = 0;
    }
    /**
     * Lets the citizen tryToEat to replenish saturation. 
     *  
     * @return return true or not if citizen eat food. 
     */
    private boolean tryToEat()
    {
        slot = InventoryUtils.findFirstSlotInProviderWith(citizen,
          itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.getItem() instanceof ItemFood);

        if (slot == -1)
        {
            checkingForFood = true;
            return true;
        }

        stack = citizen.getCitizenData().getInventory().getStackInSlot(slot);
        if (!ItemStackUtils.isEmpty(stack) && stack.getItem() instanceof ItemFood && citizen.getCitizenData() != null)
        {
            eatFood();
            return true;
        }
        else
        {
            stack = null;
        }

        return false;
    }

    /**
     * Check if the citizen should get food, meaning, check if he checked for food today already and check if his saturation is decent.
     *
     * @return true if he should go search for food.
     */
    private boolean shouldGetFood()
    {
        return (citizen.getCitizenData().getSaturation() <= CitizenConstants.HIGH_SATURATION
                  && (citizen.getCitizenJobHandler().getColonyJob() != null && !citizen.getCitizenJobHandler().getColonyJob().hasCheckedForFoodToday()))
                 || citizen.getCitizenData().getSaturation() <= CitizenConstants.LOW_SATURATION;
    }

    private void searchForFood()
    {
        if (citizen.getCitizenJobHandler().getColonyJob() != null && !citizen.getCitizenJobHandler().getColonyJob().hasCheckedForFoodToday())
        {
            @Nullable final AbstractBuilding homeBuilding = citizen.getCitizenColonyHandler().getHomeBuilding();
            if (!citizen.isWorkerAtSiteWithMove(homeBuilding.getLocation(),2))
            {
                return;
            }

            citizen.getCitizenJobHandler().getColonyJob().setCheckedForFood();
            if (citizen.getCitizenJobHandler().isInHut(itemStack -> (!ItemStackUtils.isEmpty(itemStack) && itemStack.getItem() instanceof ItemFood) || citizen.getCitizenData().getSaturation() > 0))
            {
                checkingForFood = false;
                return;
            }
        }

        if (restaurant == null)
        {
            final BlockPos goodCook = citizen.getCitizenColonyHandler().getColony().getBuildingManager().getBestRestaurant(citizen);

            if (goodCook == null)
            {
                chatSpamFilter.talkWithoutSpam("com.minecolonies.coremod.ai.noRestaurant");
                checkingRestaurant = false;
                checkingForFood = false;
                citizenAtRestaurant = false;
                return;
            }
            restaurant = goodCook;
            checkingRestaurant = true;
        }

        if (restaurant != null)
        {
             citizenAtRestaurant = citizen.isWorkerAtSiteWithMove(restaurant, 4);
        }
    }

    protected void eatFood()
    {
        eatingTicks = stack.getMaxItemUseDuration();
        citizen.setHeldItem(EnumHand.MAIN_HAND, stack);
        citizen.setActiveHand(EnumHand.MAIN_HAND);
        checkingForFood = false;
    }
}
