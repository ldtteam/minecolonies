package com.minecolonies.coremod.entity.ai.citizen.undertaker;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.tileentities.TileEntityGrave;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.colony.GraveData;
import com.minecolonies.coremod.colony.buildings.BuildingMysticalSite;
import com.minecolonies.coremod.colony.buildings.modules.LivingBuildingModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingEnchanter;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingGraveyard;
import com.minecolonies.coremod.colony.jobs.JobUndertaker;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.util.AdvancementUtils;
import net.minecraft.block.*;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.research.util.ResearchConstants.*;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.UndertakerConstants.*;

/**
 * Undertaker AI class.
 */
public class EntityAIWorkUndertaker extends AbstractEntityAIInteract<JobUndertaker, BuildingGraveyard>
{
    /**
     * The random variable.
     */
    private Random random = new Random();

    /**
     * A counter to delay some task.
     */
    private int effortCounter = 0;

    /**
     * Changed after finished digging in order to dump the inventory.
     */
    private boolean shouldDumpInventory = false;

    /**
     * The current pos to wander at.
     */
    private BlockPos wanderPos = null;

    /**
     * The current pos to grave to build.
     */
    private Tuple<BlockPos, Direction> burialPos = null;

    /**
     * Constructor for the Undertaker. Defines the tasks the Undertaker executes.
     *
     * @param job a undertaker job to use.
     */
    public EntityAIWorkUndertaker(@NotNull final JobUndertaker job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, REQUEST_DELAY),
          new AITarget(START_WORKING, this::startWorking, STANDARD_DELAY),
          new AITarget(WANDER, this::wander, STANDARD_DELAY),
          new AITarget(EMPTY_GRAVE, this::emptyGrave, STANDARD_DELAY),
          new AITarget(TRY_RESURRECT, this::tryResurrect, STANDARD_DELAY),
          new AITarget(DIG_GRAVE, this::digGrave, STANDARD_DELAY),
          new AITarget(BURY_CITIZEN, this::buryCitizen, STANDARD_DELAY)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingGraveyard> getExpectedBuildingClass()
    {
        return BuildingGraveyard.class;
    }

    /**
     * Prepares the undertaker for digging.
     * Also requests the tools and checks if the undertaker has queued graves.
     *
     * @return the next IAIState
     */
    @NotNull
    private IAIState startWorking()
    {
        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);
        worker.getCitizenData().setIdleAtJob(false);

        @Nullable final BlockPos currentGrave = getOwnBuilding().getGraveToWorkOn();
        if (currentGrave != null)
        {
            if (walkToBuilding())
            {
                return getState();
            }

            final TileEntity entity = world.getBlockEntity(currentGrave);
            if (entity instanceof TileEntityGrave)
            {
                getOwnBuilding().setLastGraveData((GraveData) ((TileEntityGrave) entity).getGraveData());
                return EMPTY_GRAVE;
            }
            getOwnBuilding().ClearCurrentGrave();
        }

        return WANDER;
    }

    /**
     * The undertaker wander in the city, learning more about magic
     *
     * @return the next IAIState
     */
    @NotNull
    private IAIState wander()
    {
        if(wanderPos == null)
        {
            wanderPos = worker.getCitizenColonyHandler().getColony().getBuildingManager().
                    getRandomBuilding(b -> b.hasModule(LivingBuildingModule.class) || b instanceof BuildingMysticalSite || b instanceof BuildingEnchanter);
            return getState();
        }

        if (walkToBlock(wanderPos, 3))
        {
            return getState();
        }

        if(wanderPos == getOwnBuilding().getPosition())
        {
            wanderPos = null;
            worker.decreaseSaturationForAction();
            worker.getCitizenData().getCitizenSkillHandler().addXpToSkill(getOwnBuilding().getSecondarySkill(), XP_PER_WANDER, worker.getCitizenData());
        }
        else
        {
            wanderPos = getOwnBuilding().getPosition();
        }

        return IDLE;
    }

    /**
     * The undertaker empty the inventory from a grave to the graveyard inventory
     * The undertake will make multiple trip if needed
     *
     * @return the next IAIState
     */
    private IAIState emptyGrave()
    {
        @Nullable final BuildingGraveyard buildingGraveyard = getOwnBuilding();

        if (buildingGraveyard == null || checkForToolOrWeapon(ToolType.SHOVEL) || buildingGraveyard.getGraveToWorkOn() == null)
        {
            return IDLE;
        }

        worker.getCitizenData().setVisibleStatus(EMPTYING_ICON);
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.emptying"));
        worker.setSprinting(worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(UNDERTAKER_RUN) > 0);
        unequip();

        @Nullable final BlockPos gravePos = buildingGraveyard.getGraveToWorkOn();

        // Still moving to the block
        if (walkToBlock(gravePos, 3))
        {
            return getState();
        }

        final TileEntity entity = world.getBlockEntity(gravePos);
        if (entity instanceof TileEntityGrave)
        {
            if (((TileEntityGrave) entity).isEmpty())
            {
                return TRY_RESURRECT;
            }

            if (worker.getInventoryCitizen().isFull())
            {
                return INVENTORY_FULL;
            }

            if(effortCounter < EFFORT_EMPTY_GRAVE)
            {
                worker.swing(Hand.MAIN_HAND);
                effortCounter += getPrimarySkillLevel();
                return getState();
            }
            effortCounter = 0;

            //at position - try to take all item
            if (InventoryUtils.transferAllItemHandler(((TileEntityGrave) entity).getInventory(), worker.getInventoryCitizen()))
            {
                return TRY_RESURRECT;
            }
        }

        return IDLE;
    }

    /**
     * The undertaker dig (remove) the grave tile entity of a fallen citizen
     *
     * @return the next IAIState
     */
    private IAIState digGrave()
    {
        @Nullable final BuildingGraveyard buildingGraveyard = getOwnBuilding();

        if (checkForToolOrWeapon(ToolType.SHOVEL) || buildingGraveyard.getGraveToWorkOn() == null)
        {
            return IDLE;
        }

        worker.getCitizenData().setVisibleStatus(DIGGING_ICON);
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.digging"));
        worker.setSprinting(worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(UNDERTAKER_RUN) > 0);

        @Nullable final BlockPos gravePos = buildingGraveyard.getGraveToWorkOn();

        if (gravePos == null)
        {
            return IDLE;
        }

        // Still moving to the block
        if (walkToBlock(gravePos, 3))
        {
            return getState();
        }

        worker.setSprinting(false);

        final TileEntity entity = world.getBlockEntity(gravePos);
        if (entity instanceof TileEntityGrave)
        {
            //at position
            if (!digIfAble(gravePos))
            {
                return getState();
            }

            worker.decreaseSaturationForAction();
            worker.getCitizenData().getCitizenSkillHandler().addXpToSkill(getOwnBuilding().getPrimarySkill(), XP_PER_DIG, worker.getCitizenData());
            return BURY_CITIZEN;
        }

        return IDLE;
    }

    /**
     * Checks if we can dig a grave, and does so if we can.
     *
     * @param position the grave to harvest.
     * @return true if we harvested or not supposed to.
     */
    private boolean digIfAble(final BlockPos position)
    {
        if (!checkForToolOrWeapon(ToolType.SHOVEL))
        {
            if (mineBlock(position))
            {
                equipShovel();
                worker.swing(worker.getUsedItemHand());
                world.setBlockAndUpdate(position, Blocks.AIR.defaultBlockState());
                worker.getCitizenItemHandler().damageItemInHand(Hand.MAIN_HAND, 1);
                worker.decreaseSaturationForContinuousAction();
                getOwnBuilding().ClearCurrentGrave();
                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * Attempt to resurrect buried citizen from its citizen data
     * Randomize to see if resurrection successful and resurrect if need be
     *
     * @return the next IAIState
     */
    private IAIState tryResurrect()
    {
        @Nullable final BuildingGraveyard buildingGraveyard = getOwnBuilding();

        if (checkForToolOrWeapon(ToolType.SHOVEL) || buildingGraveyard.getLastGraveData() == null || buildingGraveyard.getGraveToWorkOn() == null)
        {
            return IDLE;
        }

        unequip();

        @Nullable final BlockPos gravePos = buildingGraveyard.getGraveToWorkOn();

        if (gravePos == null)
        {
            return IDLE;
        }

        // Still moving to the block
        if (walkToBlock(gravePos, 3))
        {
            return getState();
        }

        final TileEntity entity = world.getBlockEntity(gravePos);
        if (entity instanceof TileEntityGrave)
        {
            if (effortCounter < EFFORT_RESURRECT)
            {
                worker.swing(Hand.MAIN_HAND);
                effortCounter += getSecondarySkillLevel();
                return getState();
            }
            effortCounter = 0;

            shouldDumpInventory = true;
            final double chance = getResurrectChance(buildingGraveyard);

            if (getTotemResurrectChance() > 0 && random.nextDouble() <= TOTEM_BREAK_CHANCE)
            {
                worker.getInventoryCitizen().extractItem(InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), Items.TOTEM_OF_UNDYING), 1, false);
                worker.playSound(SoundEvents.TOTEM_USE, 1.0f, 1.0f);
            }

            if (chance >= random.nextDouble())
            {
                final ICitizenData citizenData = buildingGraveyard.getColony().getCitizenManager().resurrectCivilianData(buildingGraveyard.getLastGraveData().getCitizenDataNBT(), true, world, gravePos);
                LanguageHandler.sendPlayersMessage(buildingGraveyard.getColony().getImportantMessageEntityPlayers(), "com.minecolonies.coremod.resurrect", citizenData.getName());
                worker.getCitizenColonyHandler().getColony().getCitizenManager().updateCitizenMourn(citizenData, false);
                AdvancementUtils.TriggerAdvancementPlayersForColony(worker.getCitizenColonyHandler().getColony(), playerMP -> AdvancementTriggers.CITIZEN_RESURRECT.trigger(playerMP));
                buildingGraveyard.setLastGraveData(null);
                world.setBlockAndUpdate(gravePos, Blocks.AIR.defaultBlockState());
                return INVENTORY_FULL;
            }
        }

        return DIG_GRAVE;
    }

    /**
     * Calculate chance of resurrection from multiple factor: Undertaker Skill, Building Level, Research, Mystical Sites in the city
     *
     * @param buildingGraveyard the building.
     * @return the chance of resurrection
     */
    private double getResurrectChance(@NotNull final BuildingGraveyard buildingGraveyard)
    {
        double totemChance = getTotemResurrectChance();
        double chance = buildingGraveyard.getBuildingLevel() * RESURRECT_BUILDING_LVL_WEIGHT +
                worker.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Mana) * RESURRECT_WORKER_MANA_LVL_WEIGHT +
                worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(RESURRECT_CHANCE) +
                totemChance;

        final double cap = MAX_RESURRECTION_CHANCE + worker.getCitizenColonyHandler().getColony().getBuildingManager().getMysticalSiteMaxBuildingLevel() * MAX_RESURRECTION_CHANCE_MYSTICAL_LVL_BONUS + totemChance;
        if (chance > cap) { chance = cap; }
        return chance;
    }

    /**
     * Check for a totem of undying, the required research, and get the increased resurrection chance
     *
     * @return the chance increase that the totem provides
     */
    private double getTotemResurrectChance()
    {
        final int totems = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), Items.TOTEM_OF_UNDYING);

        if (totems > 0)
        {
            AdvancementUtils.TriggerAdvancementPlayersForColony(worker.getCitizenColonyHandler().getColony(), playerMP -> AdvancementTriggers.UNDERTAKER_TOTEM.trigger(playerMP));
        }

        if (worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(USE_TOTEM) > 0)
        {
            if ( totems == 1 )
            {
                return SINGLE_TOTEM_RESURRECTION_CHANCE_BONUS;
            }
            else if ( totems > 1 )
            {
                return MULTIPLE_TOTEMS_RESURRECTION_CHANCE_BONUS;
            }
        }

        return 0;
    }

    /**
     * The Undertaker search for an empty grave site in the graveyard and build a named graved with
     * the name of the citizen and its job as text
     *
     * @return the next IAIState
     */
    private IAIState buryCitizen()
    {
        @Nullable final BuildingGraveyard buildingGraveyard = getOwnBuilding();

        if (checkForToolOrWeapon(ToolType.SHOVEL) || buildingGraveyard.getLastGraveData() == null)
        {
            return IDLE;
        }
        worker.getCitizenData().setVisibleStatus(BURYING_ICON);
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.burying"));

        if(burialPos == null || !world.getBlockState(burialPos.getA()).isAir())
        {
            burialPos = getOwnBuilding().getRandomFreeVisualGravePos();
        }

        if(burialPos == null || burialPos.getA() == null)
        {
            //coudn't find a place to dig a grave
            LanguageHandler.sendPlayersMessage(buildingGraveyard.getColony().getImportantMessageEntityPlayers(),
                    "com.minecolonies.coremod.nospaceforgrave", buildingGraveyard.getLastGraveData().getCitizenName());
            return IDLE;
        }

        if(walkToBlock(burialPos.getA(), 3))
        {
            return getState();
        }

        if(effortCounter < EFFORT_BURY)
        {
            equipShovel();
            worker.swing(Hand.MAIN_HAND);
            effortCounter += getPrimarySkillLevel();
            return getState();
        }
        effortCounter = 0;
        unequip();

        buildingGraveyard.buryCitizenHere(burialPos);
        //Disabled until Mourning AI update: worker.getCitizenColonyHandler().getColony().setNeedToMourn(false, buildingGraveyard.getLastGraveData().getCitizenName());
        AdvancementUtils.TriggerAdvancementPlayersForColony(worker.getCitizenColonyHandler().getColony(), playerMP -> AdvancementTriggers.CITIZEN_BURY.trigger(playerMP));

        buildingGraveyard.setLastGraveData(null);
        burialPos = null;
        shouldDumpInventory = true;

        return INVENTORY_FULL;
    }

    /**
     * Called to check when the InventoryShouldBeDumped.
     *
     * @return true if the conditions are met
     */
    @Override
    protected boolean wantInventoryDumped()
    {
        if (shouldDumpInventory)
        {
            shouldDumpInventory = false;
            return true;
        }
        return false;
    }

    /**
     * Sets the shovel as held item.
     */
    private void equipShovel()
    {
        worker.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, getShovelSlot());
    }

    /**
     * Sets the nothing as held item.
     */
    private void unequip()
    {
        worker.getCitizenItemHandler().removeHeldItem();
    }

    /**
     * Get's the slot in which the shovel is in.
     *
     * @return slot number
     */
    private int getShovelSlot()
    {
        return InventoryUtils.getFirstSlotOfItemHandlerContainingTool(getInventory(), ToolType.SHOVEL, TOOL_LEVEL_WOOD_OR_GOLD, getOwnBuilding().getMaxToolLevel());
    }

    /**
     * Returns the undertaker's worker instance. Called from outside this class.
     *
     * @return citizen object
     */
    @Nullable
    public AbstractEntityCitizen getCitizen()
    {
        return worker;
    }
}
