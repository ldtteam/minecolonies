package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.entity.ai.citizen.guards.GuardGear;
import com.minecolonies.api.entity.ai.citizen.guards.GuardGearBuilder;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryFunctions;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.api.util.constant.ToolLevelConstants.*;

/**
 * Class taking of the abstract guard methods for both archer and knights.
 *
 * @param <J> the generic job.
 */
public abstract class AbstractEntityAIFight<J extends AbstractJobGuard<J>, B extends AbstractBuildingGuards>
    extends AbstractEntityAIInteract<J, B>
{
    /**
     * Tools and Items needed by the worker.
     */
    public final List<ToolType> toolsNeeded = new ArrayList<>();

    /**
     * List of items that are required by the guard based on building level
     * and guard level. This array holds a pointer to the building level
     * and then pointer to GuardGear
     */
    public final List<List<GuardGear>> itemsNeeded = new ArrayList<>();

    /**
     * Holds a list of required armor for this guard
     */
    private final Map<IToolType, List<GuardGear>> requiredArmor = new LinkedHashMap<IToolType, List<GuardGear>>();

    /**
     * Holds a list of required armor for this guard
     */
    private final Map<IToolType, ItemStack> armorToWear = new HashMap<>();

    /**
     * The current target for our guard.
     */
    protected LivingEntity target = null;

    /**
     * The value of the speed which the guard will move.
     */
    private static final double COMBAT_SPEED = 1.0;

    /**
     * The bonus speed per worker level.
     */
    private static final double SPEED_LEVEL_BONUS = 0.01;

    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public AbstractEntityAIFight(@NotNull final J job)
    {
        super(job);
        super.registerTargets(new AITarget(IDLE, START_WORKING, 1),
            new AITarget(START_WORKING, this::startWorkingAtOwnBuilding, 100),
            new AITarget(PREPARING, this::prepare, 1));
        worker.setCanPickUpLoot(true);

        itemsNeeded.add(GuardGearBuilder
            .buildGearForLevel(ARMOR_LEVEL_LEATHER, ARMOR_LEVEL_LEATHER, LEATHER_LEVEL_RANGE, LEATHER_BUILDING_LEVEL_RANGE));
        itemsNeeded
            .add(GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_GOLD, ARMOR_LEVEL_GOLD, GOLD_LEVEL_RANGE, GOLD_BUILDING_LEVEL_RANGE));
        itemsNeeded
            .add(GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_CHAIN, ARMOR_LEVEL_CHAIN, CHAIN_LEVEL_RANGE, CHAIN_BUILDING_LEVEL_RANGE));
        itemsNeeded
            .add(GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_IRON, ARMOR_LEVEL_IRON, IRON_LEVEL_RANGE, IRON_BUILDING_LEVEL_RANGE));
        itemsNeeded
            .add(GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_DIAMOND, ARMOR_LEVEL_DIAMOND, DIA_LEVEL_RANGE, DIA_BUILDING_LEVEL_RANGE));
        itemsNeeded
            .add(GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_DIAMOND, ARMOR_LEVEL_MAX, DIA_LEVEL_RANGE, DIA_BUILDING_LEVEL_RANGE));
    }

    /**
     * Can be overridden in implementations.
     * <p>
     * Here the AI can check if the armour have to be re rendered and do it.
     */
    @Override
    protected void updateRenderMetaData()
    {
        if (getState() != NEEDS_ITEM)
        {
            updateArmor();
        }
    }

    protected abstract int getAttackRange();

    /**
     * Redirects the herder to their building.
     *
     * @return The next {@link IAIState}.
     */
    private IAIState startWorkingAtOwnBuilding()
    {
        worker.getCitizenStatusHandler()
            .setLatestStatus(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_WORKER_GOINGTOHUT));
        if (walkToBuilding())
        {
            return getState();
        }
        return PREPARING;
    }

    /**
     * Prepares the guard.
     * Fills his required armor and tool lists and transfer from building chest if required.
     *
     * @return The next {@link IAIState}.
     */
    private IAIState prepare()
    {
        setDelay(Constants.TICKS_SECOND * PREPARE_DELAY_SECONDS);

        @Nullable
        final IGuardBuilding building = getOwnBuilding();
        if (building == null || worker.getCitizenData() == null)
        {
            return PREPARING;
        }

        for (final ToolType tool : toolsNeeded)
        {
            if (checkForToolOrWeapon(tool))
            {
                return getState();
            }
            else if (getOwnBuilding() != null)
            {
                InventoryFunctions.matchFirstInProviderWithSimpleAction(worker,
                    stack -> !ItemStackUtils.isEmpty(stack) && ItemStackUtils.doesItemServeAsWeapon(stack)
                        && ItemStackUtils.hasToolLevel(stack, tool, 0, getOwnBuilding().getMaxToolLevel()),
                    itemStack -> worker.getCitizenItemHandler().setMainHeldItem(itemStack));
            }
        }

        requiredArmor.clear();
        armorToWear.clear();
        final Map<IToolType, List<GuardGear>> correctArmor = new LinkedHashMap<>();
        for (final List<GuardGear> itemList : itemsNeeded)
        {
            final int level = worker.getCitizenData().getJobModifier();
            for (final GuardGear item : itemList)
            {
                /*
                 * Make sure that we only take the item for the right building and guard level.
                 */
                if (level >= item.getMinLevelRequired() && level <= item.getMaxLevelRequired()
                    && building.getBuildingLevel() >= item.getMinBuildingLevelRequired()
                    && building.getBuildingLevel() <= item.getMaxBuildingLevelRequired())
                {
                    final List<GuardGear> listOfItems = new ArrayList<>();
                    listOfItems.add(item);

                    if (correctArmor.containsKey(item.getItemNeeded()))
                    {
                        listOfItems.addAll(correctArmor.get(item.getItemNeeded()));
                    }
                    correctArmor.put(item.getItemNeeded(), listOfItems);
                }
            }
        }

        for (final Map.Entry<IToolType, List<GuardGear>> entry : correctArmor.entrySet())
        {
            final List<Integer> slotsWorker = InventoryUtils.findAllSlotsInItemHandlerWith(worker.getInventoryCitizen(),
                itemStack -> entry.getValue().stream().anyMatch(guardGear -> guardGear.test(itemStack)));
            int bestLevel = -1;
            final List<Integer> nonOptimalSlots = new ArrayList<>();
            int bestSlot = -1;
            for (final int slot : slotsWorker)
            {
                final ItemStack stack = worker.getInventoryCitizen().getStackInSlot(slot);
                if (!ItemStackUtils.isEmpty(stack))
                {
                    final int level = ItemStackUtils.getMiningLevel(stack, entry.getKey());
                    if (level > bestLevel)
                    {
                        if (bestSlot != -1)
                        {
                            nonOptimalSlots.add(bestLevel);
                        }
                        bestLevel = level;
                        bestSlot = slot;
                    }
                }
            }

            int bestLevelChest = -1;
            int bestSlotChest = -1;
            IItemHandler bestHandler = null;
            final Map<IItemHandler, List<Integer>> slotsChest = InventoryUtils.findAllSlotsInProviderWith(building,
                itemStack -> entry.getValue().stream().anyMatch(guardGear -> guardGear.test(itemStack)));
            for (final Map.Entry<IItemHandler, List<Integer>> handlers : slotsChest.entrySet())
            {
                for (final int slot : handlers.getValue())
                {
                    final ItemStack stack = handlers.getKey().getStackInSlot(slot);
                    if (!ItemStackUtils.isEmpty(stack))
                    {
                        final int level = ItemStackUtils.getMiningLevel(stack, entry.getKey());
                        if (level > bestLevel && level > bestLevelChest)
                        {
                            bestLevelChest = level;
                            bestSlotChest = slot;
                            bestHandler = handlers.getKey();
                        }
                    }
                }
            }

            if (!entry.getValue().isEmpty())
            {
                if (bestLevelChest > bestLevel)
                {
                    final ItemStack armorStack = bestHandler.getStackInSlot(bestSlotChest).copy();
                    if (armorStack.getItem() instanceof ArmorItem)
                    {
                        armorToWear.put(entry.getKey(), armorStack);
                    }

                    InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandler(bestHandler, bestSlotChest, worker.getInventoryCitizen());
                    if (bestSlot != -1)
                    {
                        nonOptimalSlots.add(bestSlot);
                    }
                }
                else if (bestSlot == -1)
                {
                    requiredArmor.put(entry.getKey(), entry.getValue());
                }
                else
                {
                    final ItemStack armorStack = worker.getInventoryCitizen().getStackInSlot(bestSlot).copy();
                    if (armorStack.getItem() instanceof ArmorItem)
                    {
                        armorToWear.put(entry.getKey(), armorStack);
                    }
                }

                for (final int slot : nonOptimalSlots)
                {
                    InventoryUtils.transferItemStackIntoNextFreeSlotInProvider(worker.getInventoryCitizen(), slot, building);
                }
            }
        }

        return DECIDE;
    }

    /**
     * This gets the attack speed for the guard
     * with adjustment for guards level.
     * Capped at 2
     *
     * @return movement speed for guard
     */
    public double getCombatMovementSpeed()
    {
        if (worker.getCitizenData() == null)
        {
            return COMBAT_SPEED;
        }
        double levelAdjustment = worker.getCitizenData().getJobModifier() * SPEED_LEVEL_BONUS;

        if (getOwnBuilding() != null)
        {
            levelAdjustment += (getOwnBuilding().getBuildingLevel() - 1) * SPEED_LEVEL_BONUS;
        }

        levelAdjustment = levelAdjustment > 0.3 ? 0.3 : levelAdjustment;
        return COMBAT_SPEED + levelAdjustment;
    }

    /**
     * Gets the base reload time for an attack.
     *
     * @return the reload time
     */
    protected int getAttackDelay()
    {
        if (worker.getCitizenData() != null)
        {
            final int delay = PHYSICAL_ATTACK_DELAY_BASE - (worker.getCitizenData().getJobModifier());
            return delay > PHYSICAL_ATTACK_DELAY_MIN ? PHYSICAL_ATTACK_DELAY_MIN : delay;
        }
        return PHYSICAL_ATTACK_DELAY_BASE;
    }

    /**
     * Updates the equipment. Take the first item of the required type only.
     * Skip over the items not requires. Ex. Required Iron, skip leather and
     * everything else.
     */
    private void updateArmor()
    {
        if (worker.getRandom().nextInt(60) <= 0)
        {
            worker.setItemStackToSlot(EquipmentSlotType.CHEST, ItemStackUtils.EMPTY);
            worker.setItemStackToSlot(EquipmentSlotType.FEET, ItemStackUtils.EMPTY);
            worker.setItemStackToSlot(EquipmentSlotType.HEAD, ItemStackUtils.EMPTY);
            worker.setItemStackToSlot(EquipmentSlotType.LEGS, ItemStackUtils.EMPTY);

            for (final Map.Entry<IToolType, ItemStack> armorStack : armorToWear.entrySet())
            {
                if (ItemStackUtils.isEmpty(armorStack.getValue()))
                {
                    continue;
                }
                final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(),
                    itemStack -> itemStack.isItemEqualIgnoreDurability(armorStack.getValue()));
                if (slot == -1)
                {
                    continue;
                }
                final ItemStack stack = worker.getInventoryCitizen().getStackInSlot(slot);
                if (ItemStackUtils.isEmpty(stack))
                {
                    worker.setItemStackToSlot(((ArmorItem) stack.getItem()).getEquipmentSlot(), ItemStackUtils.EMPTY);
                    continue;
                }

                if (stack.getItem() instanceof ArmorItem)
                {
                    worker.setItemStackToSlot(((ArmorItem) stack.getItem()).getEquipmentSlot(), stack);
                    requiredArmor.remove(armorStack.getKey());
                    cancelAsynchRequestForArmor(armorStack.getKey());
                }
            }

            for (final Map.Entry<IToolType, List<GuardGear>> entry : requiredArmor.entrySet())
            {
                int minLevel = Integer.MAX_VALUE;
                int maxLevel = -1;
                for (final GuardGear item : entry.getValue())
                {
                    if (item.getMinArmorLevel() < minLevel)
                    {
                        minLevel = item.getMinArmorLevel();
                    }
                    if (item.getMaxArmorLevel() > maxLevel)
                    {
                        maxLevel = item.getMaxArmorLevel();
                    }
                }
                checkForToolorWeaponASync(entry.getKey(), minLevel, maxLevel);
            }
        }
    }
}
