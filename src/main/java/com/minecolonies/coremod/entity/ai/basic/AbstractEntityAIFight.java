package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.entity.ai.citizen.guards.GuardGear;
import com.minecolonies.api.entity.ai.citizen.guards.GuardGearBuilder;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryFunctions;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_WORKER_GOINGTOHUT));
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
                         && ItemStackUtils.hasToolLevel(stack, tool, 0, getOwnBuilding().getMaxToolLevel()),
              itemStack -> worker.getCitizenItemHandler().setMainHeldItem(itemStack));
        }

        equipInventoryArmor();

        // Can only "see" the inventory and check for items if at the building
        if (worker.blockPosition().distSqr(getOwnBuilding().getID()) > 50)
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
        final IGuardBuilding building = getOwnBuilding();
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

                if (!ItemStackUtils.isEmpty(worker.getItemBySlot(item.getType())))
                {
                    bestLevel = ItemStackUtils.getMiningLevel(worker.getItemBySlot(item.getType()), item.getItemNeeded());
                }

                final Map<IItemHandler, List<Integer>> items = InventoryUtils.findAllSlotsInProviderWith(building, item::test);
                if (items.isEmpty())
                {
                    // None found, check for equipped
                    if (ItemStackUtils.isEmpty(worker.getItemBySlot(item.getType())))
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
        cleanArmor();
        final IGuardBuilding building = getOwnBuilding();

        for (final List<GuardGear> itemList : itemsNeeded)
        {
            for (final GuardGear item : itemList)
            {
                if (ItemStackUtils.isEmpty(worker.getItemBySlot(item.getType())) && building.getBuildingLevel() >= item.getMinBuildingLevelRequired() && building.getBuildingLevel() <= item.getMaxBuildingLevelRequired())
                {
                    int slot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getInventoryCitizen(), item::test);

                    if (slot > -1)
                    {
                        worker.setItemSlot(item.getType(), worker.getInventoryCitizen().getStackInSlot(slot));
                    }
                }
            }
        }
    }

    /**
     * Removes currently equipped armor and shields
     */
    public void cleanArmor()
    {
        updateArmorInSlot(EquipmentSlotType.CHEST);
        updateArmorInSlot(EquipmentSlotType.FEET);
        updateArmorInSlot(EquipmentSlotType.HEAD);
        updateArmorInSlot(EquipmentSlotType.LEGS);
        updateArmorInSlot(EquipmentSlotType.OFFHAND);
    }

    /**
     * Check if the armor is still in the inventory, if not empty the equipment slot.
     * @param type the type of armor.
     */
    private void updateArmorInSlot(final EquipmentSlotType type)
    {
        final ItemStack stack = worker.getItemBySlot(type);
        if (stack.isEmpty() || InventoryUtils.findFirstSlotInItemHandlerWith(getInventory(), itemStack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, itemStack, false, true)) == -1)
        {
            worker.setItemSlot(type, ItemStackUtils.EMPTY);
        }
    }
}
