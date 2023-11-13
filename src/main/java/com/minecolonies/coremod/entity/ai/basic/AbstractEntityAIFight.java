package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.api.entity.ai.citizen.guards.GuardGear;
import com.minecolonies.api.entity.ai.citizen.guards.GuardGearBuilder;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryFunctions;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.research.util.ResearchConstants.SHIELD_USAGE;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.api.util.constant.ToolLevelConstants.*;

/**
 * Class taking of the abstract guard methods for both archer and knights.
 *
 * @param <J> the generic job.
 */
public abstract class AbstractEntityAIFight<J extends AbstractJobGuard<J>, B extends AbstractBuildingGuards> extends AbstractEntityAIInteract<J, B>
{

    /**
     * Tools and Items needed by the worker.
     */
    public final List<ToolType> toolsNeeded = new ArrayList<>();

    /**
     * List of items that are required by the guard based on building level and guard level.  This array holds a pointer to the building level and then pointer to GuardGear
     */
    public final List<List<GuardGear>> itemsNeeded = new ArrayList<>();

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
    public static final double SPEED_LEVEL_BONUS = 0.01;

    /**
     * Creates the abstract part of the AI. Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public AbstractEntityAIFight(@NotNull final J job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 1),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding, 100),
          new AITarget(PREPARING, this::prepare, TICKS_SECOND)
        );
        worker.setCanPickUpLoot(true);

        itemsNeeded.add(GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_IRON, ARMOR_LEVEL_MAX, LEATHER_BUILDING_LEVEL_RANGE, DIA_BUILDING_LEVEL_RANGE));
        itemsNeeded.add(GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_CHAIN, ARMOR_LEVEL_DIAMOND, LEATHER_BUILDING_LEVEL_RANGE, DIA_BUILDING_LEVEL_RANGE));
        itemsNeeded.add(GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_LEATHER, ARMOR_LEVEL_IRON, LEATHER_BUILDING_LEVEL_RANGE, IRON_BUILDING_LEVEL_RANGE));
        itemsNeeded.add(GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_LEATHER, ARMOR_LEVEL_CHAIN, LEATHER_BUILDING_LEVEL_RANGE, CHAIN_BUILDING_LEVEL_RANGE));
        itemsNeeded.add(GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_LEATHER, ARMOR_LEVEL_GOLD, LEATHER_BUILDING_LEVEL_RANGE, GOLD_BUILDING_LEVEL_RANGE));
    }

    /**
     * Redirects the guard to their building.
     *
     * @return The next {@link IAIState}.
     */
    protected IAIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return PREPARING;
    }

    /**
     * Prepares the guard. Fills his required armor and tool lists and transfer from building chest if required.
     *
     * @return The next {@link IAIState}.
     */
    private IAIState prepare()
    {
        for (final ToolType tool : toolsNeeded)
        {
            if (checkForToolOrWeapon(tool))
            {
                return getState();
            }
            InventoryFunctions.matchFirstInProviderWithSimpleAction(worker,
              stack -> !ItemStackUtils.isEmpty(stack)
                         && ItemStackUtils.doesItemServeAsWeapon(stack)
                         && ItemStackUtils.hasToolLevel(stack, tool, 0, building.getMaxToolLevel()),
              itemStack -> worker.getCitizenItemHandler().setMainHeldItem(itemStack));
        }

        equipInventoryArmor();

        // Can only "see" the inventory and check for items if at the building
        if (worker.blockPosition().distSqr(building.getID()) > 50)
        {
            return DECIDE;
        }

        atBuildingActions();
        return DECIDE;
    }

    /**
     * Task to do when at the own building, as guards only go there on requests and on dump
     */
    protected void atBuildingActions()
    {
        for (final List<GuardGear> itemList : itemsNeeded)
        {
            for (final GuardGear item : itemList)
            {
                if (!(building.getBuildingLevel() >= item.getMinBuildingLevelRequired() && building.getBuildingLevel() <= item.getMaxBuildingLevelRequired()))
                {
                    continue;
                }
                if (item.getItemNeeded() == ToolType.SHIELD && worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(SHIELD_USAGE) <= 0)
                {
                    continue;
                }

                int bestSlot = -1;
                int bestLevel = -1;
                IItemHandler bestHandler = null;

                if (item.getType().getType() == EquipmentSlot.Type.ARMOR)
                {
                    if (!ItemStackUtils.isEmpty(worker.getInventoryCitizen().getArmorInSlot(item.getType())))
                    {
                        bestLevel = ItemStackUtils.getMiningLevel(worker.getInventoryCitizen().getArmorInSlot(item.getType()), item.getItemNeeded());
                    }
                }
                else
                {
                    if (!ItemStackUtils.isEmpty(worker.getItemBySlot(item.getType())))
                    {
                        bestLevel = ItemStackUtils.getMiningLevel(worker.getItemBySlot(item.getType()), item.getItemNeeded());
                    }
                }


                final Map<IItemHandler, List<Integer>> items = InventoryUtils.findAllSlotsInProviderWith(building, item);
                if (items.isEmpty())
                {
                        // None found, check for equipped
                    if ((item.getType().getType() == EquipmentSlot.Type.ARMOR && ItemStackUtils.isEmpty(worker.getInventoryCitizen().getArmorInSlot(item.getType()))) || ItemStackUtils.isEmpty(worker.getItemBySlot(item.getType())))
                    {
                        // create request
                        checkForToolorWeaponASync(item.getItemNeeded(), item.getMinArmorLevel(), item.getMaxArmorLevel());
                    }
                }
                else
                {
                    // Compare levels
                    for (Map.Entry<IItemHandler, List<Integer>> entry : items.entrySet())
                    {
                        for (final Integer slot : entry.getValue())
                        {
                            final ItemStack stack = entry.getKey().getStackInSlot(slot);
                            if (ItemStackUtils.isEmpty(stack))
                            {
                                continue;
                            }

                            int currentLevel = ItemStackUtils.getMiningLevel(stack, item.getItemNeeded());

                            if (currentLevel > bestLevel)
                            {
                                bestLevel = currentLevel;
                                bestSlot = slot;
                                bestHandler = entry.getKey();
                            }
                        }
                    }
                }

                // Transfer if needed
                if (bestHandler != null)
                {
                    if (item.getType().getType() == EquipmentSlot.Type.ARMOR)
                    {
                        if (!ItemStackUtils.isEmpty(worker.getInventoryCitizen().getArmorInSlot(item.getType())))
                        {
                            final ItemStack armorStack = worker.getInventoryCitizen().getArmorInSlot(item.getType());
                            worker.getInventoryCitizen().moveArmorToInventory(item.getType());
                            final int slot =
                              InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getInventoryCitizen(), stack -> stack == armorStack);
                            if (slot > -1)
                            {
                                InventoryUtils.transferItemStackIntoNextFreeSlotInProvider(worker.getInventoryCitizen(), slot, building);
                            }
                        }

                        final ItemStack newStack = bestHandler.getStackInSlot(bestSlot);
                        InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandler(bestHandler, bestSlot, worker.getInventoryCitizen());
                        final int slot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getInventoryCitizen(), stack -> stack == newStack);
                        if (slot > -1)
                        {
                            worker.getInventoryCitizen().transferArmorToSlot(item.getType(), slot);
                        }
                    }
                    else
                    {
                        if (!ItemStackUtils.isEmpty(worker.getItemBySlot(item.getType())))
                        {
                            final int slot =
                              InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getInventoryCitizen(), stack -> stack == worker.getItemBySlot(item.getType()));
                            if (slot > -1)
                            {
                                InventoryUtils.transferItemStackIntoNextFreeSlotInProvider(worker.getInventoryCitizen(), slot, building);
                            }
                        }

                        // Used for further comparisons, set to the right inventory slot afterwards
                        worker.setItemSlot(item.getType(), bestHandler.getStackInSlot(bestSlot));
                        InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandler(bestHandler, bestSlot, worker.getInventoryCitizen());
                    }
                }
            }
        }

        equipInventoryArmor();
    }

    @Override
    public IAIState afterDump()
    {
        return PREPARING;
    }

    /**
     * Equips armor existing in inventory
     */
    public void equipInventoryArmor()
    {
        cleanVisibleSlots();
        final Set<EquipmentSlot> equipment = new HashSet<>();
        for (final List<GuardGear> itemList : itemsNeeded)
        {
            for (final GuardGear item : itemList)
            {
                if (equipment.contains(item.getType()))
                {
                    continue;
                }
                if (item.getType().getType() == EquipmentSlot.Type.ARMOR)
                {
                    if (building.getBuildingLevel() >= item.getMinBuildingLevelRequired() && building.getBuildingLevel() <= item.getMaxBuildingLevelRequired())
                    {
                        int slot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getInventoryCitizen(), item);
                        if (slot <= -1)
                        {
                            continue;
                        }

                        equipment.add(item.getType());
                        final ItemStack current = worker.getInventoryCitizen().getArmorInSlot(item.getType());
                        if (!current.isEmpty() && current.getItem() instanceof ArmorItem)
                        {
                            final int currentLevel = ItemStackUtils.getMiningLevel(current, item.getItemNeeded());
                            final int newLevel =  ItemStackUtils.getMiningLevel(worker.getInventoryCitizen().getStackInSlot(slot), item.getItemNeeded());
                            if (currentLevel > newLevel)
                            {
                                continue;
                            }
                        }
                        worker.getInventoryCitizen().transferArmorToSlot(item.getType(), slot);
                    }
                }
                else
                {
                    if (ItemStackUtils.isEmpty(worker.getItemBySlot(item.getType())) && building.getBuildingLevel() >= item.getMinBuildingLevelRequired() && building.getBuildingLevel() <= item.getMaxBuildingLevelRequired())
                    {
                        equipment.add(item.getType());
                        int slot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getInventoryCitizen(), item);
                        if (slot > -1)
                        {
                            worker.setItemSlot(item.getType(), worker.getInventoryCitizen().getStackInSlot(slot));
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes currently equipped shield
     */
    public void cleanVisibleSlots()
    {
        final ItemStack stack = worker.getItemBySlot(EquipmentSlot.OFFHAND);
        if (stack.isEmpty() || InventoryUtils.findFirstSlotInItemHandlerWith(getInventory(), itemStack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, itemStack, false, true)) == -1)
        {
            worker.setItemSlot(EquipmentSlot.OFFHAND, ItemStackUtils.EMPTY);
        }
        worker.setItemSlot(EquipmentSlot.HEAD, ItemStackUtils.EMPTY);
        worker.setItemSlot(EquipmentSlot.CHEST, ItemStackUtils.EMPTY);
        worker.setItemSlot(EquipmentSlot.LEGS, ItemStackUtils.EMPTY);
        worker.setItemSlot(EquipmentSlot.FEET, ItemStackUtils.EMPTY);
    }
}
