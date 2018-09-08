package com.minecolonies.coremod.entity.ai.minimal;


import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.coremod.blocks.interfaces.IBlockMinecoloniesSeat;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.EntityCushion;
import com.minecolonies.coremod.entity.ai.util.ChatSpamFilter;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityAIEatTask extends EntityAIBase
{
    public enum STATE
    {
        IDLE,
        CHECKING_FOR_FOOD,
        CHECKING_FOR_RESTAURANT,
        AT_RESTAURANT,
        CHECKING_FOR_CHAIR,
        SITTING,
        EATING,
        RELAXING
    }

    
    private final static int RELAXING_TICKS = 500;
    private final EntityCitizen citizen;
    private int eatingTicks;
    private int relaxingTicks;
    private ItemStack stack;
    private int slot;
    protected ChatSpamFilter      chatSpamFilter;
    
    private STATE currentState;
    private boolean isSitting;
    private BlockPos chairPosition;
    private Optional<IBlockMinecoloniesSeat> seatBlock;
    /**
     * The restaurant this AI usually goes to.
     */
    private BlockPos restaurant;

    private final static int CHAIR_SEARCH_RANGE = 20;
    
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
        seatBlock = Optional.ofNullable(null);
        
    }
    
    @Override
    public boolean shouldExecute()
    {
        if ((citizen.isRiding() && (currentState == STATE.IDLE )) || (currentState == STATE.SITTING && !citizen.isRiding()))
        {
            citizen.dismountRidingEntity();
            currentState = STATE.IDLE;
        }

        if (currentState != STATE.IDLE && citizen.getDesiredActivity() != DesiredActivity.SLEEP )
        {
            return true;
        }

        
        if ( (citizen.getDesiredActivity() == DesiredActivity.SLEEP || citizen.getCitizenData().getSaturation() >= CitizenConstants.HIGH_SATURATION))
        {
            if (citizen.isRiding())
            {
                citizen.dismountRidingEntity();
            }
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

//        cleanTask();
        return false;
        
    }

    @Override
    public void updateTask()
    {
        super.updateTask();
        switch (currentState)
        {
            case AT_RESTAURANT:
                if (!citizen.isRiding() && Configurations.gameplay.restaurantSittingRequired)
                {
                    findChair();
                }
                else
                {
                    if (citizen.getCitizenJobHandler().getColonyJob() == null && citizen.getCitizenData().getSaturation() >= CitizenConstants.LOW_SATURATION
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
                break;
            case CHECKING_FOR_CHAIR:
                findChair();
            case CHECKING_FOR_RESTAURANT:
                searchForFood();
                break;
            case CHECKING_FOR_FOOD:
            case SITTING:
                tryToEat();
                break;
            case RELAXING:
                if (relaxingTicks > 0)
                {
                    relaxingTicks--;
                    if (eatingTicks > 0)
                    {
                        eatingTicks--;
                    }
                    else
                    {
                        citizen.stopActiveHand();
                        citizen.resetActiveHand();
                    }

                    if(relaxingTicks == 0) 
                    {
                        citizen.dismountRidingEntity();
                        cleanTask();
                    }
                    else if (eatingTicks == 0 && checkForRandom())
                    {
                        citizen.stopActiveHand();
                        citizen.resetActiveHand();
                        citizen.setHeldItem(EnumHand.MAIN_HAND, stack);
                        citizen.setActiveHand(EnumHand.MAIN_HAND);
                        eatingTicks = 50;
                    }
                }
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
                        if (restaurant != null && Configurations.gameplay.restaurantSittingRequired)
                        {
                            currentState = STATE.RELAXING;
                            relaxingTicks = RELAXING_TICKS;
                        }
                        else
                        {
                            citizen.dismountRidingEntity();
                            cleanTask();
                        }
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

    protected void cleanTask()
    {
        if (isSitting)
        {
            citizen.dismountRidingEntity();
            seatBlock.ifPresent(block -> block.dismountSeat(CompatibilityUtils.getWorld(citizen)));
        }
        citizen.stopActiveHand();
        citizen.resetActiveHand();
        citizen.setHeldItem(EnumHand.MAIN_HAND,ItemStack.EMPTY);

        slot = -1;
        stack = null;
        eatingTicks = 0;
        relaxingTicks = 0;
        isSitting = false;
        currentState = STATE.IDLE;
        chairPosition = null;
        restaurant = null;
        seatBlock = Optional.ofNullable(null);
    }

    /**
     * Lets the citizen tryToEat to replenish saturation. 
     *  
     * @return return true or not if citizen eat food. 
     */
    private boolean tryToEat()
    {
        slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(citizen,
          itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.getItem() instanceof ItemFood);

        if (slot == -1)
        {
            citizen.getCitizenData().getCitizenHappinessHandler().setFoodModifier(false);
            return false;
        }

        stack = citizen.getCitizenData().getInventory().getStackInSlot(slot);
        if (!ItemStackUtils.isEmpty(stack) && stack.getItem() instanceof ItemFood && citizen.getCitizenData() != null)
        {
            citizen.getCitizenData().getInventory().decrStackSize(slot, 1);
            citizen.getCitizenData().markDirty();
            eatFood();
            return true;
        }
        else
        {
            stack = null;
        }

        citizen.getCitizenData().getCitizenHappinessHandler().setFoodModifier(false);
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

    protected void eatFood()
    {
        eatingTicks = stack.getMaxItemUseDuration() * 2;
        currentState = STATE.EATING;
        citizen.setHeldItem(EnumHand.MAIN_HAND, stack);
        citizen.setActiveHand(EnumHand.MAIN_HAND);
    }
    
    protected void findChair()
    {
        final World world = CompatibilityUtils.getWorld(citizen);
        if (chairPosition != null)
        {
            if (citizen.isWorkerAtSiteWithMove(chairPosition,2))
            {
                final AxisAlignedBB range = new AxisAlignedBB(chairPosition);
                List<EntityCushion> items = world.getEntitiesWithinAABB(EntityCushion.class, range);
                
                if (items.size() > 0)
                {
                    EntityCushion entity = items.get(0);
                    if (entity.isBeingRidden() || entity.isSeatTaken())
                    {
                        chairPosition = null;
                        return;
                    }
                }
                
                seatBlock.ifPresent(block -> block.startSeating(world, chairPosition, citizen));
                currentState = STATE.SITTING;
                isSitting = true;
            }
        }
        else
        {
            List<BlockPos> chairs = null;
            BuildingCook building = (BuildingCook) citizen.getCitizenColonyHandler().getColony().getBuildingManager().getBuilding(restaurant);
            if (building != null)
            {
                chairs = building.getChairs(citizen);
            }
            
            if (chairs != null && chairs.size() > 0)
            {
                for(int i = 0;i < chairs.size(); i ++)
                {
                    tryFindChairAt(chairs.get(i));
                    if (chairPosition != null)
                    {
                        return;
                    }
                }
            }
            if (chairPosition == null)
            {
                sendChatMessage("com.minecolonies.coremod.ai.noChairs");
                return;
            }
        }
    }

    private void tryFindChairAt(@NotNull final BlockPos pos)
    {
        final World world = CompatibilityUtils.getWorld(citizen);
        final Block block = world.getBlockState(pos).getBlock();

        final AxisAlignedBB range = new AxisAlignedBB(pos);
        List<EntityCushion> items = world.getEntitiesWithinAABB(EntityCushion.class, range);

        if (!(block instanceof IBlockMinecoloniesSeat))
        {
            //rerun finding chairs, this chair is not available anymore.
            BuildingCook building = (BuildingCook) citizen.getCitizenColonyHandler().getColony().getBuildingManager().getBuilding(restaurant);
            if (building != null)
            {
                building.recalculateChairs(citizen);
                return;
            }
        }
        
        if (items.size() > 0)
        {
            EntityCushion entity = items.get(0);
            if (entity.isBeingRidden() || entity.isSeatTaken())
            {
                return;
            }
            entity.setSeatTaken();
        }
        if (citizen.getRidingEntity() == null
            && world.getBlockState(pos.add(0, 1, 0)).getBlock() == Blocks.AIR) 
        {
            seatBlock = Optional.ofNullable((IBlockMinecoloniesSeat) block);
            chairPosition = pos;
        }

        
//        final World world = CompatibilityUtils.getWorld(citizen);
//        final Block block = world.getBlockState(pos).getBlock();
//        if (block instanceof IBlockMinecoloniesSeat)
//        {
//            final AxisAlignedBB range = new AxisAlignedBB(pos);
//            List<EntityCushion> items = world.getEntitiesWithinAABB(EntityCushion.class, range);
//            IBlockMinecoloniesSeat seat = (IBlockMinecoloniesSeat) block;
//            
//            if (items.size() > 0)
//            {
//                EntityCushion entity = items.get(0);
//                if (entity.isBeingRidden() || entity.isSeatTaken())
//                {
//                    return;
//                }
//                entity.setSeatTaken();
//            }
//            if (citizen.getRidingEntity() == null
//                && world.getBlockState(pos.add(0, 1, 0)).getBlock() == Blocks.AIR) 
//            {
//                seatBlock = Optional.ofNullable((IBlockMinecoloniesSeat) block);
//                chairPosition = pos;
//            }
//        }
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
