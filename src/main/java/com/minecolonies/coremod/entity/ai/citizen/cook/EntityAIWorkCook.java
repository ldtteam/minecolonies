package com.minecolonies.coremod.entity.ai.citizen.cook;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.buildings.BuildingCook;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
                new AITarget(COOK_SERVE, serveFood())
        );
        worker.setSkillModifier(CHARISMA_MULTIPLIER * worker.getCitizenData().getCharisma()
                + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());
        worker.setCanPickUpLoot(true);
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
        //todo search for furnaces (registered to this building) -> only position
        //todo check for burnable material, if not available request coal?
        //todo then put things in the oven
        //todo make a boolean with "waiting for oven in building" store it to NBT!
        //todo in Start working check for that so we can do things asynch.
        return COOK_COOK_FOOD;
    }

    private AIState gatherFoodFromWarehouse()
    {
        ((BuildingCook) getOwnBuilding()).setGatheredToday();
        //todo if no food at all -> -> go to warehouse and get buildingLevel food

        return START_WORKING;
    }

    private AIState startWorking()
    {
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
