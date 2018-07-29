package com.minecolonies.coremod.entity.ai.citizen.composter;

import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingComposter;
import com.minecolonies.coremod.colony.jobs.JobComposter;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.tileentities.TileEntityBarrel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Random;

import static com.minecolonies.api.util.constant.Constants.DOUBLE;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
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
    private static final double BASE_XP_GAIN = 1;

    private  BlockPos currentTarget;

    /**
     * The number of times the AI will check if the player has set any items on the list until messaging him
     */
    private static final int TICKS_UNTIL_COMPLAIN = 12000;

    /**
     * The ticks elapsed since the last complain
     */
    private int ticksToComplain = 0;

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
        if(getOwnBuilding(BuildingComposter.class).getCopyOfAllowedItems().isEmpty())
        {
            complain();
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
            for (ItemStorage item : getOwnBuilding(BuildingComposter.class).getCopyOfAllowedItems())
            {
                ItemStack itemStack = item.getItemStack();
                itemStack.setCount(STACKSIZE);
                itemList.add(itemStack);
            }
            if (!itemList.isEmpty())
            {
                worker.getCitizenData().createRequestAsync(new StackList(itemList));
            }
        }

        setDelay(2);
        return START_WORKING;
    }

    private AIState decideWhatToDo()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation(COM_MINECOLONIES_COREMOD_STATUS_IDLING));

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
        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation(COM_MINECOLONIES_COREMOD_STATUS_COMPOSTER_FILLING));

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
        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation(COM_MINECOLONIES_COREMOD_STATUS_COMPOSTER_HARVESTING));

        if (walkToBlock(currentTarget))
        {
            setDelay(2);
            return getState();
        }

        if(world.getTileEntity(currentTarget) instanceof TileEntityBarrel)
        {
            worker.getCitizenItemHandler().hitBlockWithToolInHand(currentTarget);

            TileEntityBarrel te = (TileEntityBarrel) world.getTileEntity(currentTarget);

            ItemStack compost = te.retrieveCompost(getLoopMultiplier(new Random()));

            InventoryUtils.addItemStackToItemHandler(new InvWrapper(worker.getInventoryCitizen()), compost);

            worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        }

        return START_WORKING;
    }

    //level2 % chance to double it, level4% chance to make 50% more, level*8% chance to make 25% more
    private double getLoopMultiplier( final Random random)
    {
        int citizenLevel = worker.getCitizenData().getLevel();

        int diceResult = random.nextInt(100);

        if(diceResult <= citizenLevel*2)
        {
            return DOUBLE;
        }
        if(diceResult <= citizenLevel*4)
        {
            return 1.5;
        }
        if(diceResult <= citizenLevel*8)
        {
            return 1.25;
        }

        return 1;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1;
    }

    private void complain()
    {
        if(ticksToComplain <= 0)
        {
            ticksToComplain = TICKS_UNTIL_COMPLAIN;
            for(EntityPlayer player : getOwnBuilding().getColony().getMessageEntityPlayers())
            {
                player.sendMessage(new TextComponentTranslation(COM_MINECOLONIES_COREMOD_ENTITY_COMPOSTER_EMPTYLIST));
            }
        }
        else
        {
            ticksToComplain--;
        }
    }
}
