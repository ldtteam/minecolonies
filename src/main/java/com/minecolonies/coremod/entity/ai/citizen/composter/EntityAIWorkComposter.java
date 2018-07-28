package com.minecolonies.coremod.entity.ai.citizen.composter;

import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingComposter;
import com.minecolonies.coremod.colony.jobs.JobComposter;
import com.minecolonies.coremod.colony.requestable.Compostable;
import com.minecolonies.coremod.colony.requestsystem.init.RequestSystemInitializer;
import com.minecolonies.coremod.colony.requestsystem.requests.StandardRequests;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.tileentities.TileEntityBarrel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

public class EntityAIWorkComposter extends AbstractEntityAIInteract<JobComposter>
{

    /**
     * How often should intelligence factor into the composter's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 2;

    /**
     * How often should intelligence factor into the composter's skill modifier.
     */
    private static final int DESTERITY_MULTIPLIER = 1;

    /**
     * Base xp gain for the composter.
     */
    private static final double BASE_XP_GAIN = 5;

    private  BlockPos currentTarget;

    /**
     * Constructor for the AI
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkComposter(@NotNull final JobComposter job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING),
          new AITarget(GATHERING_REQUIRED_MATERIALS, this::getMaterials),
          new AITarget(START_WORKING, this::decideWhatToDo),
          new AITarget(COMPOSTER_FILL, this::fillBarrels),
          new AITarget(COMPOSTER_HARVEST, this::harvestBarrels)
        );
        worker.getCitizenExperienceHandler().setSkillModifier(DESTERITY_MULTIPLIER * worker.getCitizenData().getDexterity()
                                                                + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());

        worker.setCanPickUpLoot(true);

    }

    private AIState getMaterials()
    {
        if (walkToBuilding())
        {
            setDelay(2);
            return getState();
        }
        if(InventoryUtils.hasItemInProvider(getOwnBuilding(), stack -> getOwnBuilding(BuildingComposter.class).isAllowedItem(new ItemStorage(stack))))
        {
            InventoryUtils.transferItemStackIntoNextFreeSlotFromProvider(
              getOwnBuilding(),
              InventoryUtils.findFirstSlotInProviderWith(getOwnBuilding(), stack -> getOwnBuilding(BuildingComposter.class).isAllowedItem(new ItemStorage(stack))),
              new InvWrapper(worker.getInventoryCitizen()));

        }

        final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(
          new InvWrapper(worker.getInventoryCitizen()),
          stack -> getOwnBuilding(BuildingComposter.class).isAllowedItem(new ItemStorage(stack))
        );
        if(slot >= 0)
        {
            worker.setHeldItem(EnumHand.MAIN_HAND, worker.getInventoryCitizen().getStackInSlot(slot));
            return START_WORKING;
        }

        worker.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);

        if(!getOwnBuilding().hasWorkerOpenRequests(worker.getCitizenData()))
        {
            ArrayList<ItemStack> itemList = new ArrayList<>();
            for(ItemStorage item : getOwnBuilding(BuildingComposter.class).getCopyOfAllowedItems())
            {
                itemList.add(item.getItemStack());
            }
            worker.getCitizenData().createRequestAsync(new StackList(itemList));
        }

        setDelay(2);
        return START_WORKING;
    }

    private AIState decideWhatToDo()
    {
        final BuildingComposter building = this.getOwnBuilding();

        for(final BlockPos barrel : building.getBarrels())
        {
            if(world.getTileEntity(barrel) instanceof TileEntityBarrel)
            {
                final TileEntityBarrel te = ((TileEntityBarrel) world.getTileEntity(barrel));
                this.currentTarget = barrel;
                if (te.isDone())
                {
                    return COMPOSTER_HARVEST;
                }
            }
        }

        for(final BlockPos barrel : building.getBarrels())
        {
            if(world.getTileEntity(barrel) instanceof TileEntityBarrel)
            {
                final TileEntityBarrel te = ((TileEntityBarrel) world.getTileEntity(barrel));
                if (!te.checkIfWorking())
                {
                    this.currentTarget = barrel;
                    return COMPOSTER_FILL;
                }
            }
        }

        setDelay(2);
        return START_WORKING;
    }

    private AIState fillBarrels()
    {
        if(worker.getHeldItem(EnumHand.MAIN_HAND) == ItemStack.EMPTY)
        {
            final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(
                            new InvWrapper(worker.getInventoryCitizen()), stack -> getOwnBuilding(BuildingComposter.class).isAllowedItem(new ItemStorage(stack)));

            if(slot >= 0)
            {
                worker.setHeldItem(EnumHand.MAIN_HAND, worker.getInventoryCitizen().getStackInSlot(slot));
            }
            else
            {
                return GATHERING_REQUIRED_MATERIALS;
            }
        }
        if (walkToBlock(currentTarget))
        {
            setDelay(2);
            return getState();
        }

        if(world.getTileEntity(currentTarget) instanceof TileEntityBarrel)
        {

            TileEntityBarrel barrel = (TileEntityBarrel) world.getTileEntity(currentTarget);

            worker.getCitizenItemHandler().hitBlockWithToolInHand(currentTarget);
            barrel.addItem(worker.getHeldItem(EnumHand.MAIN_HAND));
            worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
            worker.setHeldItem(EnumHand.MAIN_HAND, ItemStackUtils.EMPTY);

            incrementActionsDone();

        }
        setDelay(2);
        return START_WORKING;
    }

    private AIState harvestBarrels()
    {
        if (walkToBlock(currentTarget))
        {
            setDelay(2);
            return getState();
        }

        if(world.getTileEntity(currentTarget) instanceof TileEntityBarrel)
        {
            worker.getCitizenItemHandler().hitBlockWithToolInHand(currentTarget);

            TileEntityBarrel te = (TileEntityBarrel) world.getTileEntity(currentTarget);

            ItemStack compost = te.retrieveCompost(1);

            InventoryUtils.addItemStackToItemHandler(new InvWrapper(worker.getInventoryCitizen()), compost);

            worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        }

        return START_WORKING;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1;
    }
}
