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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
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

    /**
     * The block pos to which the AI is going.
     */
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
     * Number of ticks that the AI should wait before deciding again
     */
    private static final int DECIDE_DELAY = 40;

    /**
     * Number of ticks that the AI should wait after completing a task
     */
    private static final int AFTER_TASK_DELAY = 5;

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

    /**
     * Method for the AI to try to get the materials needed for the task he's doing. Will request if there are no materials
     * @return the new AIState after doing this
     */
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
              InventoryUtils.findFirstSlotInProviderNotEmptyWith(getOwnBuilding(), stack -> getOwnBuilding(BuildingComposter.class).isAllowedItem(new ItemStorage(stack))),
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
            final ArrayList<ItemStack> itemList = new ArrayList<>();
            for (final ItemStorage item : getOwnBuilding(BuildingComposter.class).getCopyOfAllowedItems())
            {
                final ItemStack itemStack = item.getItemStack();
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

    /**
     * Method for the AI to decide what to do. Possible actions: harvest barrels, fill barrels or idle
     * @return the decision it made
     */
    private AIState decideWhatToDo()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation(COM_MINECOLONIES_COREMOD_STATUS_IDLING));

        if(walkToBuilding())
        {
            setDelay(2);
            return getState();
        }

        final BuildingComposter building = this.getOwnBuilding();

        for(final BlockPos barrel : building.getBarrels())
        {
            final TileEntity te =world.getTileEntity(barrel);
            if(te instanceof TileEntityBarrel)
            {

                this.currentTarget = barrel;
                if (((TileEntityBarrel) te).isDone())
                {
                    setDelay(DECIDE_DELAY);
                    return COMPOSTER_HARVEST;
                }
            }
        }

        for(final BlockPos barrel : building.getBarrels())
        {
            final TileEntity te =world.getTileEntity(barrel);
            if(te instanceof TileEntityBarrel && !((TileEntityBarrel) te).checkIfWorking())
            {
                this.currentTarget = barrel;
                setDelay(DECIDE_DELAY);
                return COMPOSTER_FILL;
            }
        }

        setDelay(DECIDE_DELAY);
        return START_WORKING;
    }

    /**
     * The AI will now fill the barrel that he found empty on his building
     * @return the nex AIState after doing this
     */
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

            final TileEntityBarrel barrel = (TileEntityBarrel) world.getTileEntity(currentTarget);

            worker.getCitizenItemHandler().hitBlockWithToolInHand(currentTarget);
            barrel.addItem(worker.getHeldItem(EnumHand.MAIN_HAND));
            worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
            worker.setHeldItem(EnumHand.MAIN_HAND, ItemStackUtils.EMPTY);

            incrementActionsDone();

        }
        setDelay(AFTER_TASK_DELAY);
        return START_WORKING;
    }

    /**
     * The AI will harvest the barrels he found finished on his building.
     * @return the next AIState after doing this
     */
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

            final TileEntityBarrel te = (TileEntityBarrel) world.getTileEntity(currentTarget);

            final ItemStack compost = te.retrieveCompost(getLootMultiplier(new Random()));

            InventoryUtils.addItemStackToItemHandler(new InvWrapper(worker.getInventoryCitizen()), compost);

            worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        }
        setDelay(AFTER_TASK_DELAY);
        return START_WORKING;
    }

    /**
     * Gives the loot multiplier based on the citizen level and a random number.
     * @param random the random number to get the percentages
     * @return the multiplier for the amount of compost (base amount: 6)
     */
    private double getLootMultiplier( final Random random)
    {
        final int citizenLevel = worker.getCitizenData().getLevel();

        final int diceResult = random.nextInt(100);

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

    /**
     * If the list of allowed items is empty, the AI will message all the officers of the colony asking for them to set the list.
     * Happens more or less once a day if the list is not filled
     */
    private void complain()
    {
        if(ticksToComplain <= 0)
        {
            ticksToComplain = TICKS_UNTIL_COMPLAIN;
            for(final EntityPlayer player : getOwnBuilding().getColony().getMessageEntityPlayers())
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
