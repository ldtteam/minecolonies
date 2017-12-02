package com.minecolonies.coremod.entity.ai.citizen.cook;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.BuildingCook;
import com.minecolonies.coremod.colony.buildings.BuildingWareHouse;
import com.minecolonies.coremod.colony.jobs.JobCook;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;

import com.minecolonies.coremod.tileentities.TileEntityRack;
import com.minecolonies.coremod.util.StructureWrapper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.Mirror;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_BAKER_NO_FURNACES;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * Cook AI class.
 */
public class EntityAIWorkCook extends AbstractEntityAISkill<JobCook>
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
     * This times the building level the worker has to keep.
     */
    private static final int LEAST_KEEP_FOOD_MULTIPLIER = 10;

    /**
     * The amount of food which should be served to the woker.
     */
    private static final int AMOUNT_OF_FOOD_TO_SERVE = 3;

    /**
     * Delay between each serving.
     */
    private static final int SERVE_DELAY = 30;

    /**
     * The standard delay after each terminated action.
     */
    private static final int STANDARD_DELAY = 5;

    /**
     * Predicate describing food.
     */
    private static final Predicate<ItemStack> isFood = itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.getItem() instanceof ItemFood;

    /**
     * Predicate describing a cookable itemStack.
     */
    private static final Predicate<ItemStack> isCookable = isFood.and(itemStack -> !ItemStackUtils.isEmpty(FurnaceRecipes.instance().getSmeltingResult(itemStack)));

    /**
     * Wait this amount of ticks after requesting a burnable material.
     */
    private static final int WAIT_AFTER_REQUEST = 400;

    /**
     * Base height considered for the restaurant.
     */
    private static final int BASE_HEIGHT        = 10;

    /**
     * The citizen the worker is currently trying to serve.
     */
    private final List<EntityCitizen> citizenToServe = new ArrayList<>();

    /**
     * The warehouse he found.
     */
    private BuildingWareHouse wareHouse = null;

    /**
     * The current position the worker should walk to.
     */
    private BlockPos walkTo = null;

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
                new AITarget(IDLE, START_WORKING),
                new AITarget(START_WORKING, this::startWorking),
                new AITarget(COOK_GET_FOOD, this::gatherFoodFromWarehouse),
                new AITarget(COOK_GATHERING, this::gatherFoodFromBuilding),
                new AITarget(COOK_COOK_FOOD, this::cookFood),
                new AITarget(COOK_SERVE, this::serveFood),
                new AITarget(COOK_RETRIEVE_FOOD, this::retrieve),
                new AITarget(COOK_GET_FIREWOOD, this::getBurnableMaterial)
        );
        worker.setSkillModifier(CHARISMA_MULTIPLIER * worker.getCitizenData().getCharisma()
                + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());
        worker.setCanPickUpLoot(true);
    }

    private AIState getBurnableMaterial()
    {
        if(walkTo == null && walkToBuilding())
        {
            return getState();
        }

        if (getOwnBuilding().getCountOfPredicateInHut(TileEntityFurnace::isItemFuel, 1, world) < 1)
        {
            checkOrRequestItemsAsynch(false, new ItemStack(Blocks.LOG), new ItemStack(Blocks.LOG2), new ItemStack(Items.COAL));
            setDelay(WAIT_AFTER_REQUEST);
        }
        else
        {
            if(walkTo == null)
            {
                final BlockPos pos = getOwnBuilding().getTileEntity().getPositionOfChestWithItemStack(TileEntityFurnace::isItemFuel);
                if (pos == null)
                {
                    return START_WORKING;
                }
                walkTo = pos;
            }

            if (walkToBlock(walkTo))
            {
                return getState();
            }

            final boolean transfered = tryTransferFromPosToCook(walkTo, TileEntityFurnace::isItemFuel);
            if (!transfered)
            {
                walkTo = null;
                return START_WORKING;
            }
            walkTo = null;
        }

        return COOK_COOK_FOOD;
    }

    private AIState retrieve()
    {
        if (walkTo == null)
        {
            return START_WORKING;
        }

        if (walkToBlock(walkTo))
        {
            return getState();
        }

        final TileEntity entity = world.getTileEntity(walkTo);
        if (!(entity instanceof TileEntityFurnace)
                || ((TileEntityFurnace) entity).isBurning()
                || (ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(2))
                && ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(0))))
        {
            walkTo = null;
            return START_WORKING;
        }
        walkTo = null;

        InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandlers(
                new InvWrapper((TileEntityFurnace) entity), 2,
                new InvWrapper(worker.getInventoryCitizen()));

        if (!ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(0)))
        {
            if (!InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), TileEntityFurnace::isItemFuel))
            {
                walkTo = null;
                return COOK_GET_FIREWOOD;
            }

            InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandlers(
                    new InvWrapper(worker.getInventoryCitizen()),
                    InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(worker.getInventoryCitizen()), TileEntityFurnace::isItemFuel),
                    new InvWrapper((TileEntityFurnace) entity));
        }

        incrementActionsDone();
        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }

    private AIState gatherFoodFromBuilding()
    {
        if(InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), isFood))
        {
            return COOK_SERVE;
        }

        final BlockPos pos = getOwnBuilding().getTileEntity().getPositionOfChestWithItemStack(isFood);
        if (pos == null)
        {
            return START_WORKING;
        }

        if (walkToBlock(pos))
        {
            return getState();
        }

        final boolean transfered = tryTransferFromPosToCook(pos, isFood);
        if (transfered)
        {
            return COOK_SERVE;
        }
        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }

    private AIState serveFood()
    {
        if (citizenToServe.isEmpty())
        {
            return START_WORKING;
        }

        if (walkToBlock(citizenToServe.get(0).getPosition()))
        {
            setDelay(2);
            return getState();
        }

        InventoryUtils.transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(
                new InvWrapper(worker.getInventoryCitizen()),
                isFood,
                AMOUNT_OF_FOOD_TO_SERVE,
                new InvWrapper(citizenToServe.get(0).getInventoryCitizen()));

        citizenToServe.remove(0);
        setDelay(SERVE_DELAY);
        return getState();
    }

    private AIState cookFood()
    {
        if (((BuildingCook) getOwnBuilding()).getFurnaces().isEmpty())
        {
            chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_ENTITY_BAKER_NO_FURNACES);
            return START_WORKING;
        }

        if (!InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), isCookable)
                && (walkTo == null || world.getBlockState(walkTo).getBlock() != Blocks.FURNACE))
        {
            walkTo = null;
            return COOK_GATHERING;
        }

        if (walkTo == null)
        {
            for (final BlockPos pos : ((BuildingCook) getOwnBuilding()).getFurnaces())
            {
                final TileEntity entity = world.getTileEntity(pos);
                if (ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(0)))
                {
                    walkTo = pos;
                }
            }
        }

        if (walkToBlock(walkTo))
        {
            setDelay(2);
            return getState();
        }

        if (!InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), TileEntityFurnace::isItemFuel))
        {
            walkTo = null;
            return COOK_GET_FIREWOOD;
        }

        final TileEntity entity = world.getTileEntity(walkTo);
        if (entity instanceof TileEntityFurnace)
        {
            InventoryUtils.transferXOfFirstSlotInProviderWithIntoInItemHandler(
                    new InvWrapper(worker.getInventoryCitizen()), isCookable, Constants.STACKSIZE,
                    new InvWrapper((TileEntityFurnace) entity), 0);

            if (ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(1)))
            {
                InventoryUtils.transferXOfFirstSlotInProviderWithIntoInItemHandler(
                        new InvWrapper(worker.getInventoryCitizen()), TileEntityFurnace::isItemFuel, Constants.STACKSIZE,
                        new InvWrapper((TileEntityFurnace) entity), 1);
            }

            ((BuildingCook) getOwnBuilding()).setIsSomethingInOven(true);
            return START_WORKING;
        }
        setDelay(STANDARD_DELAY);
        return COOK_COOK_FOOD;
    }

    /**
     * Try to transfer a item matching a predicate from a position to the cook.
     *
     * @param pos       the position to transfer it from.
     * @param predicate the predicate to evaluate.
     * @return true if succesful.
     */
    private boolean tryTransferFromPosToCook(final BlockPos pos, @NotNull final Predicate<ItemStack> predicate)
    {
        final TileEntity entity = world.getTileEntity(pos);
        if (entity instanceof TileEntityChest)
        {
            return InventoryUtils.transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(
                    new InvWrapper((TileEntityChest) entity),
                    predicate,
                    Constants.STACKSIZE,
                    new InvWrapper(worker.getInventoryCitizen()));
        }
        else if (entity instanceof TileEntityRack)
        {
            return InventoryUtils.transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(
                    ((TileEntityRack) entity).getInventory(),
                    predicate,
                    Constants.STACKSIZE,
                    new InvWrapper(worker.getInventoryCitizen()));
        }
        return false;
    }

    private AIState gatherFoodFromWarehouse()
    {
        if (!worker.getColony().hasWarehouse())
        {
            chatSpamFilter.requestTextComponentWithoutSpam(new TextComponentTranslation("com.minecolonies.coremod.ai.noWarehouse"));
            return START_WORKING;
        }

        if (wareHouse == null)
        {
            double distance = Double.MAX_VALUE;
            for (final AbstractBuilding building : worker.getColony().getBuildings().values())
            {
                if (building instanceof BuildingWareHouse && getOwnBuilding().getLocation().distanceSq(building.getLocation()) < distance)
                {
                    wareHouse = (BuildingWareHouse) building;
                    distance = getOwnBuilding().getLocation().distanceSq(building.getLocation());
                }
            }
        }

        if (wareHouse == null)
        {
            Log.getLogger().warn("Colony should have warehouse but its not in the list of buildings!");
            return START_WORKING;
        }

        if (walkTo == null)
        {
            if (walkToBlock(wareHouse.getLocation()))
            {
                return getState();
            }

            ((BuildingCook) getOwnBuilding()).setGatheredToday();
            final BlockPos pos = wareHouse.getTileEntity().getPositionOfChestWithItemStack(isFood);
            if (pos == null)
            {
                if(!((BuildingCook)getOwnBuilding()).hasGatheredToday())
                {
                    chatSpamFilter.talkWithoutSpam("com.minecolonies.coremod.ai.noFood");
                }
                return START_WORKING;
            }
            walkTo = pos;
        }

        if (walkToBlock(walkTo))
        {
            return getState();
        }

        int transfersDone = 0;
        boolean transfered = true;
        while (transfered && transfersDone < getOwnBuilding().getBuildingLevel())
        {
            transfered = tryTransferFromPosToCook(walkTo, isFood);
            transfersDone++;
        }

        walkTo = null;
        incrementActionsDone();
        return START_WORKING;
    }

    private BlockPos getPositionOfOvenToRetrieveFrom()
    {
        for (final BlockPos pos : ((BuildingCook) getOwnBuilding()).getFurnaces())
        {
            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileEntityFurnace && !((TileEntityFurnace) entity).isBurning()
                    && (!ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(2)) || !ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(0))))
            {
                worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.retrieving"));
                return pos;
            }
        }
        ((BuildingCook) getOwnBuilding()).setIsSomethingInOven(false);
        return null;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1;
    }

    private AIState startWorking()
    {
        if (((BuildingCook) getOwnBuilding()).isSomethingInOven())
        {
            final BlockPos posOfOven = getPositionOfOvenToRetrieveFrom();
            if(posOfOven != null)
            {
                walkTo = posOfOven;
                return COOK_RETRIEVE_FOOD;
            }
        }

        final int amountOfFood = getOwnBuilding().getCountOfPredicateInHut(isFood, getOwnBuilding().getBuildingLevel() * LEAST_KEEP_FOOD_MULTIPLIER, world)
                + InventoryUtils.getItemCountInItemHandler(new InvWrapper(worker.getInventoryCitizen()), isFood);

        if (amountOfFood <= 0 && !((BuildingCook) getOwnBuilding()).hasGatheredToday())
        {
            worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.gathering"));
            return COOK_GET_FOOD;
        }

        if (range == null)
        {
            range = getTargetableArea();
        }

        citizenToServe.clear();
        final List<EntityCitizen> citizenList = world.getEntitiesWithinAABB(EntityCitizen.class,
                range, cit -> !(cit.getColonyJob() instanceof JobCook) && cit.getCitizenData().getSaturation() <= 0);
        if (!citizenList.isEmpty())
        {
            citizenToServe.addAll(citizenList);
            worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.serving"));
            return COOK_GATHERING;
        }

        if (amountOfFood < getOwnBuilding().getBuildingLevel() * LEAST_KEEP_FOOD_MULTIPLIER && !((BuildingCook) getOwnBuilding()).hasGatheredToday())
        {
            worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.gathering"));
            return COOK_GET_FOOD;
        }

        if (getOwnBuilding().getCountOfPredicateInHut(isCookable, 1, world) >= 1
                || InventoryUtils.getItemCountInItemHandler(new InvWrapper(worker.getInventoryCitizen()), isCookable) >= 1)
        {
            worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.cooking"));
            return COOK_COOK_FOOD;
        }
        return checkForAdditionalJobs();
    }

    /**
     * If no clear tasks are given, check if something else is to do.
     * @return the next AIState to traverse to.
     */
    private AIState checkForAdditionalJobs()
    {
        for (final BlockPos pos : ((BuildingCook) getOwnBuilding()).getFurnaces())
        {
            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileEntityFurnace && !((TileEntityFurnace) entity).isBurning())
            {
                walkTo = pos;
                if(!ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(2)))
                {
                    worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.retrieving"));
                    return COOK_RETRIEVE_FOOD;
                }
                else if(!ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(0)))
                {
                    worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.cooking"));
                    return COOK_COOK_FOOD;
                }
            }
        }

        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.idling"));
        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }

    /**
     * Creates a simple area around the Shepherd Hut used for AABB calculations for finding sheep
     *
     * @return The AABB
     */
    private AxisAlignedBB getTargetableArea()
    {
        final int x1;
        final int z1;
        final int x3;
        final int z3;
        final int y1 = getOwnBuilding().getLocation().getY() - 1;
        final int y3;

        if(getOwnBuilding().getHeight() == 0)
        {
            final Structures.StructureName sn =
                    new Structures.StructureName(Structures.SCHEMATICS_PREFIX,
                            getOwnBuilding().getStyle(),
                            getOwnBuilding().getSchematicName() + getOwnBuilding().getBuildingLevel());

            final String structureName = sn.toString();

            final StructureWrapper wrapper = new StructureWrapper(world, structureName);
            wrapper.rotate(getOwnBuilding().getRotation(), world, getOwnBuilding().getLocation(), getOwnBuilding().isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE);

            final BlockPos pos = getOwnBuilding().getLocation();
            wrapper.setPosition(pos);

            x1 = wrapper.getPosition().getX() - wrapper.getOffset().getX() - 1;
            z1 = wrapper.getPosition().getZ() - wrapper.getOffset().getZ() - 1;
            x3 = wrapper.getPosition().getX() + (wrapper.getWidth() - wrapper.getOffset().getX());
            z3 = wrapper.getPosition().getZ() + (wrapper.getLength() - wrapper.getOffset().getZ());
            y3 = getOwnBuilding().getLocation().getY() + BASE_HEIGHT;
        }
        else
        {
            final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners = getOwnBuilding().getCorners();
            x1 = corners.getFirst().getFirst();
            x3 = corners.getFirst().getSecond();
            z1 = corners.getSecond().getFirst();
            z3 = corners.getSecond().getSecond();
            y3 = getOwnBuilding().getHeight();
        }

        return new AxisAlignedBB(x1, y1, z1, x3, y3, z3);
    }
}
