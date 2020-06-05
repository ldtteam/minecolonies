package com.minecolonies.coremod.entity.ai.citizen.smelter;

import com.google.common.reflect.TypeToken;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSmeltery;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteractionResponseHandler;
import com.minecolonies.coremod.colony.jobs.JobSmelter;
import com.minecolonies.coremod.colony.requestable.SmeltableOre;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIUsesFurnace;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.SMELTER_SMELTING_ITEMS;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.START_WORKING;
import static com.minecolonies.api.util.constant.Constants.RESULT_SLOT;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Smelter AI class.
 */
public class EntityAIWorkSmelter extends AbstractEntityAIUsesFurnace<JobSmelter, BuildingSmeltery>
{
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
     * Value to identify the list of filterable ores.
     */
    public static final String ORE_LIST = "ores";

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
          new AITarget(SMELTER_SMELTING_ITEMS, this::smeltStuff, HIT_DELAY)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingSmeltery> getExpectedBuildingClass()
    {
        return BuildingSmeltery.class;
    }

    /**
     * He will smelt down armor, weapons and tools to smaller pieces here.
     *
     * @return the next state to go to.
     */
    private IAIState smeltStuff()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(SMELTING_DOWN));
        if (walkToBuilding())
        {
            return getState();
        }

        if (ItemStackUtils.isEmpty(worker.getHeldItem(Hand.MAIN_HAND)))
        {
            progress = 0;
            if (InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), EntityAIWorkSmelter::isSmeltableToolOrWeapon) <= 0)
            {
                if (!InventoryUtils.hasItemInProvider(getOwnBuilding(), EntityAIWorkSmelter::isSmeltableToolOrWeapon))
                {
                    return START_WORKING;
                }
                InventoryUtils.transferItemStackIntoNextFreeSlotFromProvider(
                  getOwnBuilding(),
                  InventoryUtils.findFirstSlotInProviderNotEmptyWith(getOwnBuilding(), EntityAIWorkSmelter::isSmeltableToolOrWeapon),
                  worker.getInventoryCitizen());
            }

            final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), EntityAIWorkSmelter::isSmeltableToolOrWeapon);

            if (slot == -1)
            {
                return START_WORKING;
            }

            worker.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, slot);
        }

        worker.getCitizenItemHandler().hitBlockWithToolInHand(getOwnBuilding().getPosition());

        if (progress >= getRequiredProgressForMakingRawMaterial())
        {
            progress = 0;

            final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), EntityAIWorkSmelter::isSmeltableToolOrWeapon);

            if (slot == -1)
            {
                worker.setHeldItem(Hand.MAIN_HAND, ItemStackUtils.EMPTY);
                return START_WORKING;
            }

            final ItemStack stack = worker.getInventoryCitizen().extractItem(slot, 1, false);
            final Tuple<ItemStack, Integer> materialTuple = getMaterialAndAmount(stack);
            final ItemStack material = materialTuple.getA();
            if (!ItemStackUtils.isEmpty(material))
            {
                material.setCount(materialTuple.getB());
                worker.getInventoryCitizen().insertItem(slot, material, false);
                if (getOwnBuilding().getBuildingLevel() > 0 && stack.isEnchanted() &&
                      ENCHANTED_BOOK_CHANCE[getOwnBuilding().getBuildingLevel() - 1] < new Random().nextInt(MAX_ENCHANTED_BOOK_CHANCE))
                {
                    final ItemStack book = extractEnchantFromItem(stack);
                    worker.getInventoryCitizen().insertItem(InventoryUtils.findFirstSlotInItemHandlerWith(
                      worker.getInventoryCitizen(),
                      ItemStack::isEmpty), book, false);
                }
                incrementActionsDoneAndDecSaturation();
            }
            else
            {
                worker.getInventoryCitizen().insertItem(slot, stack, false);
            }

            worker.decreaseSaturationForAction();
            worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
            worker.setHeldItem(Hand.MAIN_HAND, ItemStackUtils.EMPTY);
            return START_WORKING;
        }

        progress++;
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
        Ingredient material = Ingredient.EMPTY;
        if (stack.getItem() instanceof SwordItem)
        {
            material = ((SwordItem) stack.getItem()).getTier().getRepairMaterial();
        }
        else if (stack.getItem() instanceof ToolItem)
        {
            material = ((ToolItem) stack.getItem()).getTier().getRepairMaterial();
        }
        else if (stack.getItem() instanceof ArmorItem)
        {
            material = ((ArmorItem) stack.getItem()).getArmorMaterial().getRepairMaterial();
            final EquipmentSlotType eq = ((ArmorItem) stack.getItem()).getEquipmentSlot();
            if (eq == EquipmentSlotType.CHEST)
            {
                amount = CHEST_MAT_AMOUNT;
            }
            else if (eq == EquipmentSlotType.LEGS)
            {
                amount = LEGS_MAT_AMOUNT;
            }
            else if (eq == EquipmentSlotType.HEAD)
            {
                amount = HEAD_MAT_AMOUNT;
            }
            else if (eq == EquipmentSlotType.FEET)
            {
                amount = FEET_MAT_AMOUNT;
            }
        }

        if (material.hasNoMatchingItems())
        {
            return new Tuple<>(ItemStack.EMPTY, amount);
        }

        return new Tuple<>(material.getMatchingStacks()[0], amount);
    }

    /**
     * Gather bars from the furnace and double or triple them by chance.
     *
     * @param furnace the furnace to retrieve from.
     */
    protected void extractFromFurnace(final FurnaceTileEntity furnace)
    {
        final ItemStack ingots = new InvWrapper(furnace).extractItem(RESULT_SLOT, STACKSIZE, false);
        final int multiplier = getOwnBuilding().ingotMultiplier(worker.getCitizenData().getJobModifier(), worker.getRandom());
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

            final ItemStack resultStack = InventoryUtils.addItemStackToItemHandlerWithResult(worker.getInventoryCitizen(), copyStack);
            if (!ItemStackUtils.isEmpty(resultStack))
            {
                resultStack.setCount(resultStack.getCount() + amount / multiplier);
                new InvWrapper(furnace).setStackInSlot(RESULT_SLOT, resultStack);
                return;
            }
            worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
            worker.decreaseSaturationForAction();
        }
    }

    /**
     * If no clear tasks are given, check if something else is to do.
     *
     * @return the next IAIState to traverse to.
     */
    @Override
    protected IAIState checkForAdditionalJobs()
    {
        final int amountOfTools = InventoryUtils.getItemCountInProvider(getOwnBuilding(), EntityAIWorkSmelter::isSmeltableToolOrWeapon)
                                    + InventoryUtils.getItemCountInItemHandler(
          worker.getInventoryCitizen(), EntityAIWorkSmelter::isSmeltableToolOrWeapon);

        if (amountOfTools > 0)
        {
            return SMELTER_SMELTING_ITEMS;
        }
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_STATUS_IDLING));
        setDelay(WAIT_AFTER_REQUEST);
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
          itemStack -> IColonyManager.getInstance().getCompatibilityManager().isOre(stack)).test(stack);
    }

    @Override
    public void requestSmeltable()
    {
        if (!getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(getSmeltAbleClass().getClass())) &&
              !getOwnBuilding().hasWorkerOpenRequestsFiltered(worker.getCitizenData(),
                req -> req.getShortDisplayString().getString().equals(LanguageHandler.format(COM_MINECOLONIES_REQUESTS_SMELTABLE_ORE))))
        {
            final Map<String, List<ItemStorage>> allowedItems = getOwnBuilding().getCopyOfAllowedItems();
            if (allowedItems.containsKey(ORE_LIST))
            {
                final List<ItemStack> requests = IColonyManager.getInstance().getCompatibilityManager().getSmeltableOres().stream()
                                                   .filter(storage -> !allowedItems.get(ORE_LIST).contains(storage))
                                                   .map(ItemStorage::getItemStack)
                                                   .collect(Collectors.toList());

                if ( requests.isEmpty() )
                {
                    if (worker.getCitizenData() != null)
                    {
                        worker.getCitizenData().triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(FURNACE_USER_NO_ORE), ChatPriority.BLOCKING));
                    }
                }
                else
                {
                    worker.getCitizenData().createRequestAsync(new StackList(requests, COM_MINECOLONIES_REQUESTS_SMELTABLE_ORE, STACKSIZE, 1));
                }
            }
            else
            {
                worker.getCitizenData().createRequestAsync(getSmeltAbleClass());
            }
        }
    }

    /**
     * Check if a stack is a smeltable tool or weapon.
     *
     * @param stack the stack to test.
     * @return true if so.
     */
    private static boolean isSmeltableToolOrWeapon(final ItemStack stack)
    {
        return !ItemStackUtils.isEmpty(stack) && (stack.getItem() instanceof SwordItem
                                                    || stack.getItem() instanceof ToolItem
                                                    || stack.getItem() instanceof ArmorItem)
                 && !stack.getItem().isDamaged(stack);
    }

    /**
     * Get the required progress to make an ingot out of a tool or weapon or armor.
     *
     * @return the amount of hits required.
     */
    private int getRequiredProgressForMakingRawMaterial()
    {
        return PROGRESS_MULTIPLIER / Math.min(worker.getCitizenData().getJobModifier() + 1, MAX_LEVEL) * HITTING_TIME;
    }

    private ItemStack extractEnchantFromItem(final ItemStack item)
    {
        final Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(item);
        final ItemStack books = new ItemStack(Items.ENCHANTED_BOOK);
        for (final Map.Entry<Enchantment, Integer> entry : enchants.entrySet())
        {
            EnchantedBookItem.addEnchantment(books, new EnchantmentData(entry.getKey(), entry.getValue()));
        }
        worker.decreaseSaturationForAction();
        return books;
    }
}
