package com.minecolonies.coremod.entity.ai.citizen.smelter;

import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSmeltery;
import com.minecolonies.coremod.colony.jobs.JobSmelter;
import com.minecolonies.coremod.colony.requestable.SmeltableOre;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIUsesFurnace;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Random;

import static com.minecolonies.api.util.constant.Constants.RESULT_SLOT;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_IDLING;
import static com.minecolonies.api.util.constant.TranslationConstants.SMELTING_DOWN;
import static com.minecolonies.coremod.entity.ai.util.AIState.SMELTER_SMELTING_ITEMS;
import static com.minecolonies.coremod.entity.ai.util.AIState.START_WORKING;

/**
 * Smelter AI class.
 */
public class EntityAIWorkSmelter extends AbstractEntityAIUsesFurnace<JobSmelter>
{
    /**
     * How often should strength factor into the smelter's skill modifier.
     */
    private static final int STRENGTH_MULTIPLIER = 2;

    /**
     * How often should intelligence factor into the smelter's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 1;

    /**
     * Time the worker delays until the next hit.
     */
    private static final int HIT_DELAY = 20;

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
    private static final int HITTING_TIME = 5;

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
    private static final double BASE_XP_GAIN = 5;

    /**
     * Progress in hitting the product.
     */
    private int progress = 0;

    /**
     * Percentage to loot an enchanted book from stuff item
     */
    private static final int[] ENCHANTED_BOOK_CHANCE = new int[] {0, 10, 25, 40, 60};

    /**
     * Max looting chance
     */
    private static final int MAX_ENCHANTED_BOOK_CHANCE = 100;

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
          new AITarget(SMELTER_SMELTING_ITEMS, this::smeltStuff)
        );
        worker.getCitizenExperienceHandler().setSkillModifier(STRENGTH_MULTIPLIER * worker.getCitizenData().getStrength()
                                  + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingSmeltery.class;
    }

    /**
     * He will smelt down armor, weapons and tools to smaller pieces here.
     *
     * @return the next state to go to.
     */
    private AIState smeltStuff()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation(SMELTING_DOWN));
        if (walkToBuilding())
        {
            return getState();
        }

        if (ItemStackUtils.isEmpty(worker.getHeldItem(EnumHand.MAIN_HAND)))
        {
            progress = 0;
            if (InventoryUtils.getItemCountInItemHandler(new InvWrapper(worker.getInventoryCitizen()), EntityAIWorkSmelter::isSmeltableToolOrWeapon) <= 0)
            {
                if (InventoryUtils.hasItemInProvider(getOwnBuilding(), EntityAIWorkSmelter::isSmeltableToolOrWeapon))
                {
                    return START_WORKING;
                }
                InventoryUtils.transferItemStackIntoNextFreeSlotFromProvider(
                  getOwnBuilding(),
                  InventoryUtils.findFirstSlotInProviderNotEmptyWith(getOwnBuilding(), EntityAIWorkSmelter::isSmeltableToolOrWeapon),
                  new InvWrapper(worker.getInventoryCitizen()));
            }

            final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(worker.getInventoryCitizen()), EntityAIWorkSmelter::isSmeltableToolOrWeapon);

            if (slot == -1)
            {
                return START_WORKING;
            }

            worker.getCitizenItemHandler().setHeldItem(EnumHand.MAIN_HAND, slot);
        }

        worker.getCitizenItemHandler().hitBlockWithToolInHand(getOwnBuilding().getLocation());

        if (progress >= getRequiredProgressForMakingRawMaterial())
        {
            progress = 0;

            final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(worker.getInventoryCitizen()), EntityAIWorkSmelter::isSmeltableToolOrWeapon);

            if (slot == -1)
            {
                worker.setHeldItem(EnumHand.MAIN_HAND, ItemStackUtils.EMPTY);
                return START_WORKING;
            }

            final ItemStack stack = new InvWrapper(worker.getInventoryCitizen()).extractItem(slot, 1, false);
            final Tuple<ItemStack, Integer> materialTuple = getMaterialAndAmount(stack);
            final ItemStack material = materialTuple.getFirst();
            if (!ItemStackUtils.isEmpty(material))
            {
                material.setCount(materialTuple.getSecond());
                material.setItemDamage(0);
                new InvWrapper(worker.getInventoryCitizen()).setStackInSlot(slot, material);
                if (getOwnBuilding().getBuildingLevel() > 0 && stack.isItemEnchanted() &&
                      ENCHANTED_BOOK_CHANCE[getOwnBuilding().getBuildingLevel() - 1] < new Random().nextInt(MAX_ENCHANTED_BOOK_CHANCE))
                {
                    final ItemStack book = extractEnchantFromItem(stack);
                    new InvWrapper(worker.getInventoryCitizen()).insertItem(InventoryUtils.findFirstSlotInItemHandlerWith(
                      new InvWrapper(worker.getInventoryCitizen()),
                      ItemStack::isEmpty), book, false);
                }
                incrementActionsDoneAndDecSaturation();
            }
            else
            {
                new InvWrapper(worker.getInventoryCitizen()).setStackInSlot(slot, stack);
            }

            worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
            worker.setHeldItem(EnumHand.MAIN_HAND, ItemStackUtils.EMPTY);
            return START_WORKING;
        }

        progress++;
        setDelay(HIT_DELAY);
        return getState();
    }

    /**
     * Get the material and amount of a certain stack.
     *
     * @param stack the stack.
     * @return a tuple of the stack and the amount.
     */
    private static Tuple<ItemStack, Integer> getMaterialAndAmount(final ItemStack stack)
    {
        int amount = 1;
        ItemStack material = ItemStackUtils.EMPTY;
        if (stack.getItem() instanceof ItemSword)
        {
            material = Item.ToolMaterial.valueOf(((ItemSword) stack.getItem()).getToolMaterialName()).getRepairItemStack();
        }
        else if (stack.getItem() instanceof ItemTool)
        {
            material = Item.ToolMaterial.valueOf(((ItemTool) stack.getItem()).getToolMaterialName()).getRepairItemStack();
        }
        else if (stack.getItem() instanceof ItemArmor)
        {
            material = ((ItemArmor) stack.getItem()).getArmorMaterial().getRepairItemStack();
            final EntityEquipmentSlot eq = ((ItemArmor) stack.getItem()).armorType;
            if (eq == EntityEquipmentSlot.CHEST)
            {
                amount = CHEST_MAT_AMOUNT;
            }
            else if (eq == EntityEquipmentSlot.LEGS)
            {
                amount = LEGS_MAT_AMOUNT;
            }
            else if (eq == EntityEquipmentSlot.HEAD)
            {
                amount = HEAD_MAT_AMOUNT;
            }
            else if (eq == EntityEquipmentSlot.FEET)
            {
                amount = FEET_MAT_AMOUNT;
            }
        }
        return new Tuple<>(material, amount);
    }

    /**
     * Gather bars from the furnace and double or triple them by chance.
     *
     * @param furnace the furnace to retrieve from.
     */
    protected void extractFromFurnace(final TileEntityFurnace furnace)
    {
        final ItemStack ingots = new InvWrapper(furnace).extractItem(RESULT_SLOT, STACKSIZE, false);
        final int multiplier = ((BuildingSmeltery) getOwnBuilding()).ingotMultiplier(worker.getCitizenData().getLevel(), worker.getRandom());
        int amount = ingots.getCount() * multiplier;

        while (amount > 0)
        {
            final ItemStack copyStack = ingots.copy();
            if (amount < ingots.getMaxStackSize())
            {
                copyStack.setCount(amount);
            }
            else
            {
                copyStack.setCount(ingots.getMaxStackSize());
            }
            amount -= copyStack.getCount();

            final ItemStack resultStack = InventoryUtils.addItemStackToItemHandlerWithResult(new InvWrapper(worker.getInventoryCitizen()), copyStack);
            if (!ItemStackUtils.isEmpty(resultStack))
            {
                resultStack.setCount(resultStack.getCount() + amount / multiplier);
                new InvWrapper(furnace).setStackInSlot(RESULT_SLOT, resultStack);
                return;
            }
            worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        }
    }

    /**
     * If no clear tasks are given, check if something else is to do.
     *
     * @return the next AIState to traverse to.
     */
    @Override
    protected AIState checkForAdditionalJobs()
    {
        final int amountOfTools = InventoryUtils.getItemCountInProvider(getOwnBuilding(), EntityAIWorkSmelter::isSmeltableToolOrWeapon)
                                    + InventoryUtils.getItemCountInItemHandler(
          new InvWrapper(worker.getInventoryCitizen()), EntityAIWorkSmelter::isSmeltableToolOrWeapon);

        if (amountOfTools > 0)
        {
            return SMELTER_SMELTING_ITEMS;
        }
        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation(COM_MINECOLONIES_COREMOD_STATUS_IDLING));
        setDelay(WAIT_AFTER_REQUEST);
        walkToBuilding();
        return START_WORKING;
    }

    @Override
    protected IRequestable getSmeltAbleClass()
    {
        return new SmeltableOre(STACKSIZE);
    }

    /**
     * Check if a stack is a smeltable ore.
     *
     * @param stack the stack to test.
     * @return true if so.
     */
    protected boolean isSmeltable(final ItemStack stack)
    {
        return !ItemStackUtils.isEmpty(stack) && ItemStackUtils.IS_SMELTABLE.and(
          itemStack -> itemStack.getItem() instanceof ItemBlock
                         && ColonyManager.getCompatibilityManager().isOre(((ItemBlock) itemStack.getItem()).getBlock().getDefaultState())).test(stack);
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
     *
     * @return the amount of hits required.
     */
    private int getRequiredProgressForMakingRawMaterial()
    {
        return PROGRESS_MULTIPLIER / Math.min(worker.getCitizenExperienceHandler().getLevel() + 1, MAX_LEVEL) * HITTING_TIME;
    }

    private ItemStack extractEnchantFromItem(final ItemStack item)
    {
        final Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(item);
        final ItemStack books = new ItemStack(Items.ENCHANTED_BOOK);
        for (final Map.Entry<Enchantment, Integer> entry : enchants.entrySet())
        {
            ItemEnchantedBook.addEnchantment(books, new EnchantmentData(entry.getKey(), entry.getValue()));
        }
        return books;
    }
}
