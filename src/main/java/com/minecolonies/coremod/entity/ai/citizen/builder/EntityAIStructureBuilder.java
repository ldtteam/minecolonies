package com.minecolonies.coremod.entity.ai.citizen.builder;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.coremod.entity.ai.util.StructureIterator;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildRemoval;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructureWithWorkOrder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.CitizenConstants.MIN_OPEN_SLOTS;

/**
 * AI class for the builder.
 * Manages building and repairing buildings.
 */
public class EntityAIStructureBuilder extends AbstractEntityAIStructureWithWorkOrder<JobBuilder>
{
    /**
     * over this y level the builder will be faster.
     */
    private static final int DEPTH_LEVEL_0 = 60;

    /**
     * At this y level the builder will be slower.
     */
    private static final int DEPTH_LEVEL_1 = 30;

    /**
     * Max depth difference.
     */
    private static final int MAX_DEPTH_DIFFERENCE = 5;

    /**
     * At this y level the builder will be way slower..
     */
    private static final int DEPTH_LEVEL_2 = 15;

    /**
     * Speed buff at 0 depth level.
     */
    private static final double SPEED_BUFF_0 = 0.5;

    /**
     * Speed buff at first depth level.
     */
    private static final int SPEED_BUFF_1 = 2;

    /**
     * Speed buff at second depth level.
     */
    private static final int SPEED_BUFF_2 = 4;

    /**
     * After how many actions should the builder dump his inventory.
     */
    private static final int ACTIONS_UNTIL_DUMP = 1024;

    /**
     * Min distance from placing block.
     */
    private static final int MIN_DISTANCE = 3;

    /**
     * Max distance to placing block.
     */
    private static final int MAX_DISTANCE = 10;

    /**
     * After which distance the builder has to recalculate his position.
     */
    private static final double ACCEPTANCE_DISTANCE = 20;

    /**
     * Building level to purge mobs at the build site.
     */
    private static final int LEVEL_TO_PURGE_MOBS    = 4;

    /**
     * The id in the list of the last picked up item.
     */
    private int pickUpCount = 0;

    /**
     * Initialize the builder and add all his tasks.
     *
     * @param job the job he has.
     */
    public EntityAIStructureBuilder(@NotNull final JobBuilder job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 100),
          new AITarget(START_WORKING, this::checkForWorkOrder, this::startWorkingAtOwnBuilding, 100),
          new AITarget(PICK_UP, this::pickUpMaterial, 5)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public IAIState getStateAfterPickUp()
    {
        return PICK_UP;
    }

    /**
     * State to pick up material before going back to work.
     * @return the next state to go to.
     */
    public IAIState pickUpMaterial()
    {
        final BuildingBuilder building = getOwnBuilding();
        final List<Predicate<ItemStack>> neededItemsList = new ArrayList<>();
        final List<BuildingBuilderResource> neededItemStackList = new ArrayList<>();

        for (final BuildingBuilderResource stack : building.getNeededResources().values())
        {

            // This change creates a more detailed neededItemsList. basically if in the schematic MAterial is needed > as Stacksize it generates more entries as long as the needed amount is bigger than the stacksize
            // for instance a building needs 220 Cobblestone it will make not 1 entry like before. it will make ( 64 + &4 + 64 +  28 ) > 4 entries in the list.
            // need to implement a BuildingBuilderRessource copy system so the stack amount is changeable. it is tackled in the trytotransfer function but this is only a fallback if anything slips.
            // so it is WIP but works already. You can see it if You build the birch town hall... now it doesnt take AGES to build large structures.
            if(stack.getAmount() > stack.getItemStack().getMaxStackSize()) // we have an amount of more than one stack
            {
                int newStackValue = 1; // intermediate value for calculation
                int oldStackValue = stack.getAmount();  // for restore purpose
                do{ // runs as long as the stack value is bigger than stack size
                    newStackValue = stack.getAmount() - stack.getItemStack().getMaxStackSize(); // gets the diff  of needed amount

                    stack.setAmount(stack.getItemStack().getMaxStackSize()); // set the to be putted value to stacksize

                    neededItemsList.add(itemstack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack.getItemStack(), itemstack, true, true)); // add it
                    neededItemStackList.add(stack);
                    stack.setAmount(newStackValue); // set new value to rest value from calculation
                }
                while(stack.getAmount() > stack.getItemStack().getMaxStackSize());
                if(stack.getAmount() > 0) // check if the amount is still above 0
                {
                    neededItemsList.add(itemstack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack.getItemStack(), itemstack, true, true)); // add the rest which is smaller than stacksize
                    neededItemStackList.add(stack);
                    //Log.getLogger().info("Added " +stack.getAmount() + " of " + stack.getName()+" to the stack ");
                }
                stack.setAmount(oldStackValue); // restore the old value... due reruns it changes the list weirdly.

            }else{
                //Log.getLogger().info("Amount  of " + stack.getName() + "is "+ stack.getAmount());
                neededItemsList.add(itemstack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack.getItemStack(), itemstack, true, true));
                neededItemStackList.add(stack);
                //Log.getLogger().info("Added " +stack.getAmount() + " of " + stack.getName()+" to the stack ");
            }



            //Log.getLogger().info("size of neeedet item list : "+ neededItemsList.size());
            //Log.getLogger().info("Add stack :" + stack.getItemStack().toString());
        }

        if (neededItemsList.size() <= pickUpCount || InventoryUtils.openSlotCount(worker.getInventoryCitizen()) <= MIN_OPEN_SLOTS)
        {
            pickUpCount = 0;
            return START_WORKING;
        }

        needsCurrently = neededItemsList.get(pickUpCount);
        needsCurrentlyStack = neededItemStackList.get(pickUpCount); // adding the stack too

        pickUpCount++;

        if (currentStructure == null)
        {
            return IDLE;
        }

        if (currentStructure.getStage() != StructureIterator.Stage.DECORATE)
        {
            //Log.getLogger().info("Check if item is decoration and now is not decoration time");
            needsCurrently = needsCurrently.and(stack -> !ItemStackUtils.isDecoration(stack));
            //Log.getLogger().info( needsCurrently.toString());

        }

        if (InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), needsCurrently))
        {
            // here still needs to be work done. Here the implementation follows to calculate all items
            // which equals the needed item in the inventory of the worker follows.
            // at the moment we dump the inventory after each step anyway. so it is not needed.
            // the change enables that the builder can pickup more than only one stack.
            // This part is unfinished and may be removed entierly later...
            // get the amount in Inventory and compares it to the needed amount.
//            int amount = needsCurrently.and(stack -> ItemStack.getSize(stack));
//
//            int otheramount = 0;
//            int slot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getInventoryCitizen(), needsCurrently);
//            if (slot != -1) {
//                ItemStack myStack = worker.getInventoryCitizen().getStackInSlot(slot);
//                otheramount = myStack.getCount();
//            }
//            Log.getLogger().info("But has "+ otheramount +" already in local inventory ");
            //return getState();
            return GATHERING_REQUIRED_MATERIALS;


        }
        else if (InventoryUtils.hasItemInProvider(building.getTileEntity(), needsCurrently))
        {
            Log.getLogger().info("return pickup stuff, need to get it from Storage");
            return GATHERING_REQUIRED_MATERIALS;
        }

        return pickUpMaterial();
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingBuilder.class;
    }

    /**
     * Checks if we got a valid workorder.
     *
     * @return true if we got a workorder to work with
     */
    private boolean checkForWorkOrder()
    {
        if (!job.hasWorkOrder())
        {
            getOwnBuilding(AbstractBuildingStructureBuilder.class).searchWorkOrder();
            return false;
        }

        final WorkOrderBuildDecoration wo = job.getWorkOrder();

        if (wo == null)
        {
            job.setWorkOrder(null);
            return false;
        }

        final IBuilding building = job.getColony().getBuildingManager().getBuilding(wo.getBuildingLocation());
        if (building == null && wo instanceof WorkOrderBuild && !(wo instanceof WorkOrderBuildRemoval))
        {
            job.complete();
            return false;
        }

        if (!job.hasStructure())
        {
            super.initiate();
        }

        return true;
    }

    @Override
    public IAIState switchStage(final IAIState state)
    {
        if (job.getWorkOrder() instanceof WorkOrderBuildRemoval && state.equals(BUILDING_STEP))
        {
            return COMPLETE_BUILD;
        }
        return super.switchStage(state);
    }

    private IAIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return START_BUILDING;
    }

    /**
     * Kill all mobs at the building site.
     */
    private void killMobs()
    {
        if (getOwnBuilding().getBuildingLevel() >= LEVEL_TO_PURGE_MOBS && job.getWorkOrder() instanceof WorkOrderBuildBuilding)
        {
            final BlockPos buildingPos = job.getWorkOrder().getBuildingLocation();
            final IBuilding building = worker.getCitizenColonyHandler().getColony().getBuildingManager().getBuilding(buildingPos);
            if (building != null)
            {
                world.getEntitiesWithinAABB(MonsterEntity.class, building.getTargetableArea(world)).forEach(Entity::remove);
            }
        }
    }

    @Override
    public void checkForExtraBuildingActions()
    {
        if (!getOwnBuilding(BuildingBuilder.class).hasPurgedMobsToday())
        {
            killMobs();
            getOwnBuilding(BuildingBuilder.class).setPurgedMobsToday(true);
        }
    }

    @Override
    public IAIState afterRequestPickUp()
    {
        return INVENTORY_FULL;
    }

    @Override
    public IAIState afterDump()
    {
        return PICK_UP;
    }

    @Override
    public boolean walkToConstructionSite(final BlockPos targetPos)
    {
        if (workFrom == null || MathUtils.twoDimDistance(targetPos, workFrom) < MIN_DISTANCE || MathUtils.twoDimDistance(targetPos, workFrom) > ACCEPTANCE_DISTANCE)
        {
            workFrom = getWorkingPosition(targetPos);
        }

        return worker.isWorkerAtSiteWithMove(workFrom, MAX_DISTANCE) || MathUtils.twoDimDistance(worker.getPosition(), workFrom) <= ACCEPTANCE_DISTANCE;
    }

    /**
     * Calculates the working position.
     * <p>
     * Takes a min distance from width and length.
     * <p>
     * Then finds the floor level at that distance and then check if it does contain two air levels.
     *
     * @param targetPosition the position to work at.
     * @return BlockPos position to work from.
     */
    @Override
    public BlockPos getWorkingPosition(final BlockPos targetPosition)
    {
        if (job.getWorkOrder() != null)
        {
            final BlockPos schemPos = job.getWorkOrder().getBuildingLocation();
            final int yStart = targetPosition.getY() > schemPos.getY() ? targetPosition.getY() : schemPos.getY();
            final int yEnd = targetPosition.getY() < schemPos.getY() ? Math.max(targetPosition.getY(), schemPos.getY() - MAX_DEPTH_DIFFERENCE) : schemPos.getY();
            final Direction direction = BlockPosUtil.getXZFacing(worker.getPosition(), targetPosition).getOpposite();
            for (int i = MIN_DISTANCE + 1; i < MAX_DISTANCE; i++)
            {
                for (int y = yStart; y >= yEnd; y--)
                {
                    final BlockPos pos = targetPosition.offset(direction, i);
                    final BlockPos basePos = new BlockPos(pos.getX(), y, pos.getZ());
                    if (EntityUtils.checkForFreeSpace(world, basePos))
                    {
                        return basePos;
                    }
                }
            }
            return schemPos.up();
        }
        return targetPosition;
    }

    @Override
    public void connectBlockToBuildingIfNecessary(@NotNull final BlockState blockState, @NotNull final BlockPos pos)
    {
        final BlockPos buildingLocation = job.getWorkOrder().getBuildingLocation();
        final IBuilding building = this.getOwnBuilding().getColony().getBuildingManager().getBuilding(buildingLocation);

        if (building != null)
        {
            building.registerBlockPosition(blockState, pos, world);
        }

        if (blockState.getBlock() == ModBlocks.blockWayPoint)
        {
            worker.getCitizenColonyHandler().getColony().addWayPoint(pos, world.getBlockState(pos));
        }
    }

    @Override
    public boolean shallReplaceSolidSubstitutionBlock(final Block worldBlock, final BlockState worldMetadata)
    {
        return false;
    }

    @Override
    public BlockState getSolidSubstitution(@NotNull final BlockPos location)
    {
        return BlockUtils.getSubstitutionBlockAtWorld(world, location).getBlockState();
    }

    @Override
    public int getBlockMiningDelay(@NotNull final Block block, @NotNull final BlockPos pos)
    {
        final int initialDelay = super.getBlockMiningDelay(block, pos);

        if (pos.getY() > DEPTH_LEVEL_0 || !MineColonies.getConfig().getCommon().restrictBuilderUnderground.get())
        {
            return (int) (initialDelay * SPEED_BUFF_0);
        }

        if (pos.getY() > DEPTH_LEVEL_1)
        {
            return initialDelay;
        }

        if (pos.getY() < DEPTH_LEVEL_2)
        {
            return initialDelay * SPEED_BUFF_2;
        }
        return initialDelay * SPEED_BUFF_1;
    }

    /**
     * Calculates after how many actions the ai should dump it's inventory.
     *
     * @return the number of actions done before item dump.
     */
    @Override
    protected int getActionsDoneUntilDumping()
    {
        return ACTIONS_UNTIL_DUMP;
    }
}
