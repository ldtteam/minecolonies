package com.minecolonies.coremod.entity.ai.citizen.smelter;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.requestable.Burnable;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.BuildingSmeltery;
import com.minecolonies.coremod.colony.jobs.JobSmelter;
import com.minecolonies.coremod.colony.requestable.SmeltableOre;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_BAKER_NO_FURNACES;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * Smelter AI class.
 */
public class EntityAIWorkSmelter extends AbstractEntityAISkill<JobSmelter>
{
    //TODO: add status for sign over their head
    /**
     * How often should strength factor into the smelter's skill modifier.
     */
    private static final int STRENGTH_MULTIPLIER = 2;

    /**
     * How often should intelligence factor into the smelter's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 1;

    /**
     * The standard delay after each terminated action.
     */
    private static final int STANDARD_DELAY = 5;

    /**
     * Wait this amount of ticks after requesting a burnable material.
     */
    private static final int WAIT_AFTER_REQUEST = 50;

    /**
     * Time the worker delays until the next hit.
     */
    private static final int HIT_DELAY = 20;

    /**
     * Slot with the result of the furnace.
     */
    private static final int RESULT_SLOT = 2;

    /**
     * Slot where ores should be put in the furnace.
     */
    private static final int ORE_SLOT = 0;

    /**
     * Slot where the fuel should be put in the furnace.
     */
    private static final int FUEL_SLOT = 1;

    /**
     * Increase this value to make the product creation progress way slower.
     */
    private static final int PROGRESS_MULTIPLIER = 50;

    /**
     * Max level which should have an effect on the speed of the worker.
     */
    private static final int MAX_LEVEL = 50;

    /**
     * Times the dough needs to be kneaded.
     */
    private static final int HITTING_TIME     = 5;

    /**
     * The materials for certain armor body parts.
     */
    private static final int CHEST_MAT_AMOUNT = 8;
    private static final int LEGS_MAT_AMOUNT  = 7;
    private static final int HEAD_MAT_AMOUNT  = 5;
    private static final int FEET_MAT_AMOUNT  = 4;

    /**
     * Base xp gain for the smelter.
     */
    private static final double BASE_XP_GAIN     = 5;

    /**
     * The current position the worker should walk to.
     */
    private BlockPos walkTo = null;

    /**
     * Progress in hitting the product.
     */
    private int progress = 0;

    /**
     * What he currently might be needing.
     */
    private Predicate<ItemStack> needsCurrently = null;

    /**
     * Constructor for the Smelter.
     * Defines the tasks the cook executes.
     *
     * @param job a cook job to use.
     */
    public EntityAIWorkSmelter(@NotNull final JobSmelter job)
    {
        super(job);
        super.registerTargets(
                new AITarget(IDLE, START_WORKING),
                new AITarget(START_WORKING, this::startWorking),
                new AITarget(SMELTER_GATHERING, this::getNeededItem),
                new AITarget(SMELTER_SMELT_ORE, this::smeltOre),
                new AITarget(SMELTER_RETRIEVE_ORE, this::retrieve),
                new AITarget(SMELTER_SMELT_STUFF, this::smeltStuff)
        );
        worker.setSkillModifier(STRENGTH_MULTIPLIER * worker.getCitizenData().getStrength()
                + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());
        worker.setCanPickUpLoot(true);
    }

    /**
     * He will smelt down armor, weapons and tools to smaller pieces here.
     * @return the next state to go to.
     */
    private AIState smeltStuff()
    {
        if (walkToBuilding())
        {
            return getState();
        }

        if(ItemStackUtils.isEmpty(worker.getHeldItem(EnumHand.MAIN_HAND)))
        {
            progress = 0;
            if(InventoryUtils.getItemCountInItemHandler(new InvWrapper(worker.getInventoryCitizen()), EntityAIWorkSmelter::isSmeltableToolOrWeapon) <= 0)
            {
                if(InventoryUtils.hasItemInProvider(getOwnBuilding(), EntityAIWorkSmelter::isSmeltableToolOrWeapon))
                {
                    return START_WORKING;
                }
                InventoryUtils.transferItemStackIntoNextFreeSlotFromProvider(
                        getOwnBuilding(),
                        InventoryUtils.findFirstSlotInProviderWith(getOwnBuilding(), EntityAIWorkSmelter::isSmeltableToolOrWeapon),
                        new InvWrapper(worker.getInventoryCitizen()));
            }

            final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(worker.getInventoryCitizen()), EntityAIWorkSmelter::isSmeltableToolOrWeapon);

            if(slot == -1)
            {
                return START_WORKING;
            }

            worker.setHeldItem(slot);
        }

        worker.hitBlockWithToolInHand(getOwnBuilding().getLocation());

        if (progress >= getRequiredProgressForMakingRawMaterial())
        {
            progress = 0;

            final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(worker.getInventoryCitizen()), EntityAIWorkSmelter::isSmeltableToolOrWeapon);

            if(slot == -1)
            {
                worker.setHeldItem(EnumHand.MAIN_HAND, ItemStackUtils.EMPTY);
                return START_WORKING;
            }

            final ItemStack stack = new InvWrapper(worker.getInventoryCitizen()).extractItem(slot, 1, false);
            final Tuple<ItemStack, Integer> materialTuple = getMaterialAndAmount(stack);
            final ItemStack material = materialTuple.getFirst();
            if(!ItemStackUtils.isEmpty(material))
            {
                material.setCount(materialTuple.getSecond());
                material.setItemDamage(0);
                new InvWrapper(worker.getInventoryCitizen()).setStackInSlot(slot, material);
                incrementActionsDone();
            }
            else
            {
                new InvWrapper(worker.getInventoryCitizen()).setStackInSlot(slot, stack);
            }

            worker.addExperience(BASE_XP_GAIN);
            worker.setHeldItem(EnumHand.MAIN_HAND, ItemStackUtils.EMPTY);
            return START_WORKING;
        }

        progress++;
        setDelay(HIT_DELAY);
        return getState();
    }

    /**
     * Get the material and amount of a certain stack.
     * @param stack the stack.
     * @return a tuple of the stack and the amount.
     */
    private static Tuple<ItemStack, Integer> getMaterialAndAmount(final ItemStack stack)
    {
        int amount = 1;
        ItemStack material = ItemStackUtils.EMPTY;
        if(stack.getItem() instanceof ItemSword)
        {
            material = Item.ToolMaterial.valueOf(((ItemSword) stack.getItem()).getToolMaterialName()).getRepairItemStack();
        }
        else if(stack.getItem() instanceof ItemTool)
        {
            material = Item.ToolMaterial.valueOf(((ItemTool) stack.getItem()).getToolMaterialName()).getRepairItemStack();
        }
        else if(stack.getItem() instanceof ItemArmor)
        {
            material = ((ItemArmor) stack.getItem()).getArmorMaterial().getRepairItemStack();
            final EntityEquipmentSlot eq = ((ItemArmor) stack.getItem()).getEquipmentSlot();
            if(eq == EntityEquipmentSlot.CHEST)
            {
                amount = CHEST_MAT_AMOUNT;
            }
            else if(eq == EntityEquipmentSlot.LEGS)
            {
                amount = LEGS_MAT_AMOUNT;
            }
            else if(eq == EntityEquipmentSlot.HEAD)
            {
                amount = HEAD_MAT_AMOUNT;
            }
            else if(eq == EntityEquipmentSlot.FEET)
            {
                amount = FEET_MAT_AMOUNT;
            }
        }
        return new Tuple<>(material, amount);
    }

    /**
     * Retrieve burnable material from the building to get to start smelting.
     * @return the next state to transfer to.
     */
    private AIState getNeededItem()
    {
        if (walkTo == null && walkToBuilding())
        {
            return getState();
        }

        if (needsCurrently == null || !InventoryUtils.hasItemInProvider(getOwnBuilding(), needsCurrently))
        {
            setDelay(STANDARD_DELAY);
            return START_WORKING;
        }
        else
        {
            if (walkTo == null)
            {
                final BlockPos pos = getOwnBuilding().getTileEntity().getPositionOfChestWithItemStack(needsCurrently);
                if (pos == null)
                {
                    setDelay(STANDARD_DELAY);
                    return START_WORKING;
                }
                walkTo = pos;
            }

            if (walkToBlock(walkTo))
            {
                setDelay(2);
                return getState();
            }

            final boolean transfered = tryTransferFromPosToWorker(walkTo, needsCurrently);
            if (!transfered)
            {
                walkTo = null;
                return START_WORKING;
            }
            walkTo = null;
        }

        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }

    /**
     * Retrieve ready bars from the furnaces.
     * @return the next state to go to.
     */
    private AIState retrieve()
    {
        if (walkTo == null)
        {
            return START_WORKING;
        }

        if (walkToBlock(walkTo))
        {
            setDelay(2);
            return getState();
        }

        final TileEntity entity = world.getTileEntity(walkTo);
        if (!(entity instanceof TileEntityFurnace)
                || ((TileEntityFurnace) entity).isBurning()
                || (ItemStackUtils.isEmpty(((TileEntityFurnace) entity).getStackInSlot(RESULT_SLOT))))
        {
            walkTo = null;
            return START_WORKING;
        }

        walkTo = null;

        gatherBarsFromFurnaceWithChance((TileEntityFurnace) entity);
        incrementActionsDone();
        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }

    /**
     * Gather bars from the furnace and double or triple them by chance.
     * @param furnace the furnace to retrieve from.
     */
    private void gatherBarsFromFurnaceWithChance(final TileEntityFurnace furnace)
    {
        final ItemStack ingots = new InvWrapper(furnace).extractItem(RESULT_SLOT, STACKSIZE, false);
        final int multiplier = ((BuildingSmeltery) getOwnBuilding()).ingotMultiplier(worker.getCitizenData().getLevel(), worker.getRandom());
        int amount = ingots.getCount() * multiplier;

        while(amount > 0)
        {
            final ItemStack copyStack = ingots.copy();
            if(amount < ingots.getMaxStackSize())
            {
                copyStack.setCount(amount);
            }
            else
            {
                copyStack.setCount(ingots.getMaxStackSize());
            }
            amount -= copyStack.getCount();

            final ItemStack resultStack = InventoryUtils.addItemStackToItemHandlerWithResult(new InvWrapper(worker.getInventoryCitizen()), copyStack);
            if(!ItemStackUtils.isEmpty(resultStack))
            {
                resultStack.setCount(resultStack.getCount() + amount / multiplier);
                new InvWrapper(furnace).setStackInSlot(RESULT_SLOT, resultStack);
                return;
            }
            worker.addExperience(BASE_XP_GAIN);
        }
    }

    /**
     * Smelt the ore after the required items are in the inv.
     * @return the next state to go to.
     */
    private AIState smeltOre()
    {
        if (((BuildingSmeltery) getOwnBuilding()).getFurnaces().isEmpty())
        {
            chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_ENTITY_BAKER_NO_FURNACES);
            setDelay(STANDARD_DELAY);
            return START_WORKING;
        }

        if (walkTo == null || world.getBlockState(walkTo).getBlock() != Blocks.FURNACE)
        {
            walkTo = null;
            setDelay(STANDARD_DELAY);
            return START_WORKING;
        }

        if (walkToBlock(walkTo))
        {
            setDelay(2);
            return getState();
        }

        final TileEntity entity = world.getTileEntity(walkTo);
        if (entity instanceof TileEntityFurnace)
        {
            final TileEntityFurnace furnace = (TileEntityFurnace) entity;

            if (InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), EntityAIWorkSmelter::isSmeltableOre)
                    && (hasFuelInFurnaceAndNoOre(furnace) || hasNeitherFuelNorOre(furnace)))
            {
                InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                        new InvWrapper(worker.getInventoryCitizen()), EntityAIWorkSmelter::isSmeltableOre, STACKSIZE,
                        new InvWrapper(furnace), ORE_SLOT);
            }

            if (InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), TileEntityFurnace::isItemFuel)
                    && (hasOreInFurnaceAndNoFuel(furnace) || hasNeitherFuelNorOre(furnace)))
            {
                InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoInItemHandler(
                        new InvWrapper(worker.getInventoryCitizen()), TileEntityFurnace::isItemFuel, STACKSIZE,
                        new InvWrapper(furnace), FUEL_SLOT);
            }
        }
        walkTo = null;
        setDelay(STANDARD_DELAY);
        return START_WORKING;
    }

    /**
     * Get the furnace which has finished ores.
     * @return the position of the furnace.
     */
    private BlockPos getPositionOfOvenToRetrieveFrom()
    {
        for (final BlockPos pos : ((BuildingSmeltery) getOwnBuilding()).getFurnaces())
        {
            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileEntityFurnace && !((TileEntityFurnace) entity).isBurning()
                    && !ItemStackUtils.isEmpty(((TileEntityFurnace) entity)
                    .getStackInSlot(RESULT_SLOT)))
            {
                worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.retrieving"));
                return pos;
            }
        }
        return null;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1;
    }

    /**
     * Central method of the smelter, he decides about what to do next from here.
     * @return the next state to go to.
     */
    private AIState startWorking()
    {
        final BlockPos posOfOven = getPositionOfOvenToRetrieveFrom();
        if (posOfOven != null)
        {
            walkTo = posOfOven;
            worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.retrieving"));
            return SMELTER_RETRIEVE_ORE;
        }

        final int amountOfOreInBuilding = InventoryUtils.getItemCountInProvider(getOwnBuilding(), EntityAIWorkSmelter::isSmeltableOre);
        final int amountOfOreInInv = InventoryUtils.getItemCountInItemHandler(new InvWrapper(worker.getInventoryCitizen()), EntityAIWorkSmelter::isSmeltableOre);

        final int amountOfFuelInBuilding = InventoryUtils.getItemCountInProvider(getOwnBuilding(), TileEntityFurnace::isItemFuel);
        final int amountOfFuelInInv = InventoryUtils.getItemCountInItemHandler(new InvWrapper(worker.getInventoryCitizen()), TileEntityFurnace::isItemFuel);

        if (amountOfOreInBuilding + amountOfOreInInv <= 0 && !getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(SmeltableOre.class)))
        {
            worker.getCitizenData().createRequestAsync(new SmeltableOre(STACKSIZE));
        }
        else if (amountOfFuelInBuilding + amountOfFuelInInv <= 0 && !getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(Burnable.class)))
        {
            worker.getCitizenData().createRequestAsync(new Burnable(STACKSIZE));
        }

        if(amountOfOreInBuilding > 0 && amountOfFuelInInv == 0)
        {
            needsCurrently = TileEntityFurnace::isItemFuel;
            return SMELTER_GATHERING;
        }
        else if(amountOfFuelInBuilding > 0 && amountOfFuelInInv == 0)
        {
            needsCurrently = EntityAIWorkSmelter::isSmeltableOre;
            return SMELTER_GATHERING;
        }

        return checkForAdditionalJobs(amountOfOreInInv, amountOfFuelInInv);
    }

    /**
     * If no clear tasks are given, check if something else is to do.
     * @param amountOfOre the amount of ore.
     * @param amountOfFuel the amoutn of fuel.
     * @return the next AIState to traverse to.
     */
    private AIState checkForAdditionalJobs(final int amountOfOre, final int amountOfFuel)
    {
        final int amountOfTools = InventoryUtils.getItemCountInProvider(getOwnBuilding(), EntityAIWorkSmelter::isSmeltableToolOrWeapon)
                + InventoryUtils.getItemCountInItemHandler(
                new InvWrapper(worker.getInventoryCitizen()), EntityAIWorkSmelter::isSmeltableToolOrWeapon);

        for (final BlockPos pos : ((BuildingSmeltery) getOwnBuilding()).getFurnaces())
        {
            final TileEntity entity = world.getTileEntity(pos);

            if(entity instanceof TileEntityFurnace && !((TileEntityFurnace) entity).isBurning())
            {
                final TileEntityFurnace furnace = (TileEntityFurnace) entity;
                if ((amountOfFuel > 0 && hasOreInFurnaceAndNoFuel(furnace))
                        || (amountOfOre > 0 && hasFuelInFurnaceAndNoOre(furnace))
                        || (amountOfFuel > 0 && amountOfOre > 0 && hasNeitherFuelNorOre(furnace)))
                {
                    walkTo = pos;
                    return SMELTER_SMELT_ORE;
                }
            }
        }

        if(amountOfTools > 0)
        {
            return SMELTER_SMELT_STUFF;
        }
        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.idling"));
        setDelay(WAIT_AFTER_REQUEST);
        walkToBuilding();
        return START_WORKING;
    }

    /**
     * Check if the furnace has ore in it and fuel empty.
     * @param entity the furnace.
     * @return true if so.
     */
    private static boolean hasOreInFurnaceAndNoFuel(final TileEntityFurnace entity)
    {
        return !ItemStackUtils.isEmpty(entity.getStackInSlot(ORE_SLOT))
                && ItemStackUtils.isEmpty(entity.getStackInSlot(FUEL_SLOT));
    }

    /**
     * Check if the furnace has ore in it and fuel empty.
     * @param entity the furnace.
     * @return true if so.
     */
    private static boolean hasNeitherFuelNorOre(final TileEntityFurnace entity)
    {
        return ItemStackUtils.isEmpty(entity.getStackInSlot(ORE_SLOT))
                && ItemStackUtils.isEmpty(entity.getStackInSlot(FUEL_SLOT));
    }

    /**
     * Check if the furnace has fuel in it and ore empty.
     * @param entity the furnace.
     * @return true if so.
     */
    private static boolean hasFuelInFurnaceAndNoOre(final TileEntityFurnace entity)
    {
        return ItemStackUtils.isEmpty(entity.getStackInSlot(ORE_SLOT))
                && !ItemStackUtils.isEmpty(entity.getStackInSlot(FUEL_SLOT));
    }

    /**
     * Check if a stack is a smeltable ore.
     *
     * @param stack the stack to test.
     * @return true if so.
     */
    private static boolean isSmeltableOre(final ItemStack stack)
    {
        return !ItemStackUtils.isEmpty(stack) && ItemStackUtils.IS_SMELTABLE.and(
                itemStack -> itemStack.getItem() instanceof ItemBlock
                        && ColonyManager.getCompatabilityManager().isOre(((ItemBlock) itemStack.getItem()).getBlock().getDefaultState())).test(stack);
    }

    /**
     * Check if a stack is a smeltable tool or weapon.
     *
     * @param stack the stack to test.
     * @return true if so.
     */
    private static boolean isSmeltableToolOrWeapon(final ItemStack stack)
    {
        return !ItemStackUtils.isEmpty(stack) && (stack.getItem() instanceof ItemSword
                || stack.getItem() instanceof ItemTool
                || stack.getItem() instanceof ItemArmor)
                && !stack.getItem().isDamaged(stack);
    }

    /**
     * Get the required progress to make an ingot out of a tool or weapon or armor.
     * @return the amount of hits required.
     */
    private int getRequiredProgressForMakingRawMaterial()
    {
        return PROGRESS_MULTIPLIER / Math.min(worker.getLevel() + 1, MAX_LEVEL) * HITTING_TIME;
    }

}
