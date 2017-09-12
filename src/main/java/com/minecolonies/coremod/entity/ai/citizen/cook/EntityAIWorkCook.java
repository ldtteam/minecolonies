package com.minecolonies.coremod.entity.ai.citizen.cook;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.BuildingCook;
import com.minecolonies.coremod.colony.buildings.BuildingWareHouse;
import com.minecolonies.coremod.colony.jobs.JobCook;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;

import com.minecolonies.coremod.tileentities.TileEntityRack;
import net.minecraft.item.ItemFood;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
     * Constructor for the Fisherman.
     * Defines the tasks the fisherman executes.
     *
     * @param job a fisherman job to use.
     */
    public EntityAIWorkCook(@NotNull final JobCook job)
    {
        super(job);
        super.registerTargets(
                new AITarget(IDLE, START_WORKING),
                new AITarget(START_WORKING, startWorking()),
                new AITarget(COOK_GET_FOOD, gatherFoodFromWarehouse()),
                new AITarget(COOK_GATHERING, gatherFoodFromBuilding()),
                new AITarget(COOK_COOK_FOOD, cookFood()),
                new AITarget(COOK_SERVE, serveFood()),
                new AITarget(COOK_RETRIEVE_FOOD, retrieve()),
                new AITarget(COOK_GET_FIREWOOD, getBurnableMaterial())
        );
        worker.setSkillModifier(CHARISMA_MULTIPLIER * worker.getCitizenData().getCharisma()
                + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());
        worker.setCanPickUpLoot(true);
    }

    private AIState getBurnableMaterial()
    {
        //todo check for burnable material in his chest, if not available, request asynch

        return START_WORKING;
    }

    private AIState retrieve()
    {
        if(walkTo == null)
        {
            return START_WORKING;
        }

        if (walkToBlock(walkTo))
        {
            return getState();
        }

        walkTo = null;
        final TileEntity entity = world.getTileEntity(walkTo);
        if(!(entity instanceof TileEntityFurnace)
                || ((TileEntityFurnace) entity).isBurning()
                || !ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(1)))
        {
            return START_WORKING;
        }

        InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandlers(
                new InvWrapper((TileEntityFurnace) entity),
                Constants.STACKSIZE,
                new InvWrapper(worker.getInventoryCitizen()));

        if(!ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(0)))
        {
            if(!InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), itemStack ->  TileEntityFurnace.isItemFuel(itemStack)))
            {
                return COOK_GET_FIREWOOD;
            }

            InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandlers(
                    new InvWrapper(worker.getInventoryCitizen()),
                    Constants.STACKSIZE,
                    new InvWrapper((TileEntityFurnace) entity));
        }

        incrementActionsDone();
        return START_WORKING;
    }

    private AIState gatherFoodFromBuilding()
    {
        final BlockPos pos = getOwnBuilding().getTileEntity().getPositionOfChestWithItemStack(itemStack -> itemStack.getItem() instanceof ItemFood);

        if (walkToBlock(pos))
        {
            return getState();
        }

        final TileEntity entity = world.getTileEntity(pos);
        boolean transfered = false;
        if (entity instanceof TileEntityChest)
        {
            transfered = InventoryUtils.transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(
                    new InvWrapper((TileEntityChest) entity),
                    itemStack -> itemStack.getItem() instanceof ItemFood,
                    AMOUNT_OF_FOOD_TO_SERVE,
                    new InvWrapper(citizenToServe.get(0).getInventoryCitizen()));
        }
        else if(entity instanceof TileEntityRack)
        {
            transfered = InventoryUtils.transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(
                    ((TileEntityRack) entity).getInventory(),
                    itemStack -> itemStack.getItem() instanceof ItemFood,
                    AMOUNT_OF_FOOD_TO_SERVE,
                    new InvWrapper(citizenToServe.get(0).getInventoryCitizen()));
        }

        if(transfered)
        {
            return COOK_SERVE;
        }
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
            setDelay(1);
            return getState();
        }

        InventoryUtils.transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(
                new InvWrapper(worker.getInventoryCitizen()),
                itemStack -> itemStack.getItem() instanceof ItemFood,
                AMOUNT_OF_FOOD_TO_SERVE,
                new InvWrapper(citizenToServe.get(0).getInventoryCitizen()));

        setDelay(SERVE_DELAY);
        return getState();
    }

    private AIState cookFood()
    {
        if(((BuildingCook) getOwnBuilding()).getFurnaces().isEmpty())
        {
            chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_ENTITY_BAKER_NO_FURNACES);
            return START_WORKING;
        }

        //todo get a stack of cookable food from the chest

        //todo then check for burnable material in the furnace, if yes -> put in, else check for burnable material in the inventory

        //todo if not in the inv -> state change.

        //todo then put things in the oven

        return COOK_COOK_FOOD;
    }

    private AIState gatherFoodFromWarehouse()
    {
        ((BuildingCook) getOwnBuilding()).setGatheredToday();
        if(!worker.getColony().hasWarehouse())
        {
            chatSpamFilter.requestTextComponentWithoutSpam(new TextComponentTranslation("com.minecolonies.coremod.ai.noWarehouse"));
            return START_WORKING;
        }

        if(wareHouse == null)
        {
            double distance = Double.MAX_VALUE;
            for(final AbstractBuilding building : worker.getColony().getBuildings().values())
            {
                if(building instanceof BuildingWareHouse && getOwnBuilding().getLocation().distanceSq(building.getLocation()) < distance)
                {
                    wareHouse = (BuildingWareHouse) building;
                    distance = getOwnBuilding().getLocation().distanceSq(building.getLocation());
                }
            }
        }

        if(wareHouse == null)
        {
            Log.getLogger().warn("Colony should have warehouse but its not in the list of buildings!");
            return START_WORKING;
        }

        if(walkToBlock(wareHouse.getLocation()))
        {
            return getState();
        }

        if(walkTo == null)
        {
            final BlockPos pos = wareHouse.getTileEntity().getPositionOfChestWithItemStack(itemStack -> itemStack.getItem() instanceof ItemFood);
            if (pos == null)
            {
                chatSpamFilter.requestTextComponentWithoutSpam(new TextComponentTranslation("com.minecolonies.coremod.ai.noFood"));
                return START_WORKING;
            }
            walkTo = pos;
        }

        if(walkToBlock(walkTo))
        {
            return getState();
        }

        int transfersDone = 0;
        boolean transfered = true;
        final TileEntity entity = world.getTileEntity(walkTo);
        while(transfered && transfersDone < getOwnBuilding().getBuildingLevel())
        {
            if (entity instanceof TileEntityChest)
            {
                transfered = InventoryUtils.transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(
                        new InvWrapper((TileEntityChest) entity),
                        itemStack -> itemStack.getItem() instanceof ItemFood,
                        Constants.STACKSIZE,
                        new InvWrapper(worker.getInventoryCitizen()));
            }
            else if (entity instanceof TileEntityRack)
            {
                transfered = InventoryUtils.transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(
                        ((TileEntityRack) entity).getInventory(),
                        itemStack -> itemStack.getItem() instanceof ItemFood,
                        Constants.STACKSIZE,
                        new InvWrapper(worker.getInventoryCitizen()));
            }
        }

        walkTo = null;
        incrementActionsDone();
        return START_WORKING;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1;
    }

    private AIState startWorking()
    {
        if(((BuildingCook)getOwnBuilding()).isSomethingInOven())
        {
            for(final BlockPos pos: ((BuildingCook) getOwnBuilding()).getFurnaces())
            {
                final TileEntity entity = world.getTileEntity(pos);
                if(entity instanceof TileEntityFurnace && !((TileEntityFurnace) entity).isBurning() && ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(1)))
                {
                    walkTo = pos;
                    return COOK_RETRIEVE_FOOD;
                }
            }
            ((BuildingCook)getOwnBuilding()).setIsSomethingInOven(false);
        }

        final int amountOfFood = getOwnBuilding().getCountOfPredicateInHut(
                itemStack -> itemStack.getItem() instanceof ItemFood, getOwnBuilding().getBuildingLevel() * LEAST_KEEP_FOOD_MULTIPLIER, world);

        if (amountOfFood <= 0)
        {
            return COOK_GET_FOOD;
        }

        final List<EntityCitizen> citizenList = world.getEntitiesWithinAABB(EntityCitizen.class, new AxisAlignedBB(getOwnBuilding().getLocation()));
        if (citizenList.size() > LEAST_KEEP_FOOD_MULTIPLIER)
        {
            citizenToServe.addAll(citizenList);
            return COOK_GATHERING;
        }

        if (amountOfFood < getOwnBuilding().getBuildingLevel() * LEAST_KEEP_FOOD_MULTIPLIER && !((BuildingCook) getOwnBuilding()).hasGatheredToday())
        {
            return COOK_GET_FOOD;
        }

        if (getOwnBuilding().getCountOfPredicateInHut(
                itemStack -> itemStack.getItem() instanceof ItemFood && ItemStackUtils.isEmpty(FurnaceRecipes.instance().getSmeltingResult(itemStack)), 1, world) >= 1)
        {
            return COOK_COOK_FOOD;
        }

        setDelay(10);
        return COOK_GET_FOOD;
    }
}
