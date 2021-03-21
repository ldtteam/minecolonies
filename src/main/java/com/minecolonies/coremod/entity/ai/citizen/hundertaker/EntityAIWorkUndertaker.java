package com.minecolonies.coremod.entity.ai.citizen.hundertaker;

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
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.colony.GraveData;
import com.minecolonies.coremod.colony.buildings.BuildingMysticalSite;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingEnchanter;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingGraveyard;
import com.minecolonies.coremod.colony.jobs.JobUndertaker;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.util.AdvancementUtils;
import net.minecraft.block.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.research.util.ResearchConstants.RESURRECT_CHANCE;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Undertaker AI class.
 */
public class EntityAIWorkUndertaker extends AbstractEntityAIInteract<JobUndertaker, BuildingGraveyard>
{
    /**
     * The EXP Earned per dig.
     */
    private static final double XP_PER_DIG = 0.5;

    /**
     * The EXP Earned per wander.
     */
    private static final double XP_PER_WANDER = 0.01;

    /**
     * The weigth of each building level on the resurrection chances.
     */
    private static final double RESURRECT_BUILDING_LVL_WEIGHT = 0.005;

    /**
     * The weigth of each worker level on the resurrection chances.
     */
    private static final double RESURRECT_WORKER_MANA_LVL_WEIGHT = 0.0025;

    /**
     * The max resurrection chance cap [0.0 min -> 1.0 max]
     */
    private static final double MAX_RESURRECTION_CHANCE = 0.05;

    /**
     * The bonus to max resurrection chance cap per max lvl of Mystical Site in the city
     */
    private static final double MAX_RESURRECTION_CHANCE_MYSTICAL_LVL_BONUS = 0.01;

    /**
     * The random variable.
     */
    private Random random = new Random();

    /**
     * A counter to delay some task.
     */
    private int effortCounter = 0;

    private static final int EFFORT_EMPTY_GRAVE = 100;
    private static final int EFFORT_BURY = 400;
    private static final int EFFORT_RESURRECT = 400;

    /**
     * Undertaker emptying icon
     */
    private final static VisibleCitizenStatus EMPTYING_ICON =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/undertaker.png"), "com.minecolonies.gui.visiblestatus.emptying");

    /**
     * Undertaker digging icon
     */
    private final static VisibleCitizenStatus DIGGING_ICON =
            new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/undertaker.png"), "com.minecolonies.gui.visiblestatus.digging");

    /**
     * Undertaker bury icon
     */
    private final static VisibleCitizenStatus BURYING_ICON =
            new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/undertaker.png"), "com.minecolonies.gui.visiblestatus.burying");

    /**
     * Undertaker resurrect icon
     */
    private final static VisibleCitizenStatus RESURRECT_ICON =
            new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/undertaker.png"), "com.minecolonies.gui.visiblestatus.resurrect");

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
     * Prepares the undertaker for digging. Also requests the tools and checks if the undertaker has queued graves.
     *
     * @return the next IAIState
     */
    @NotNull
    private IAIState startWorking()
    {
        @Nullable final BuildingGraveyard building = getOwnBuilding();
        if (building == null || building.getBuildingLevel() < 1)
        {
            worker.getCitizenData().setVisibleStatus(null);
            return IDLE;
        }

        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);
        worker.getCitizenData().setIdleAtJob(false);

        @Nullable final BlockPos currentGrave = building.getGraveToWorkOn();
        if (currentGrave != null)
        {
            if (walkToBuilding())
            {
                return getState();
            }

            final TileEntity entity = world.getTileEntity(currentGrave);
            if (entity != null && entity instanceof TileEntityGrave)
            {
                building.setLastGraveData((GraveData) ((TileEntityGrave) entity).getGraveData());
                return EMPTY_GRAVE;
            }
            building.ClearCurrentGrave();
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
        @Nullable final BuildingGraveyard building = getOwnBuilding();
        if (building == null || building.getBuildingLevel() < 1)
        {
            worker.getCitizenData().setVisibleStatus(null);
            return IDLE;
        }

        if(wanderPos == null)
        {
            final BlockPos newWanderPos = worker.getCitizenColonyHandler().getColony().getBuildingManager().
                    getRandomBuilding(b -> b.getSchematicName() == "citizen" || b instanceof BuildingMysticalSite || b instanceof BuildingEnchanter);
            if (newWanderPos != null)
            {
                wanderPos = newWanderPos;
            }
            return getState();
        }

        if (walkToBlock(wanderPos, 1))
        {
            return getState();
        }

        if(wanderPos == building.getPosition())
        {
            wanderPos = null;
            worker.decreaseSaturationForContinuousAction();
            worker.getCitizenData().getCitizenSkillHandler().addXpToSkill(getOwnBuilding().getSecondarySkill(), XP_PER_WANDER, worker.getCitizenData());
        }
        else
        {
            wanderPos = building.getPosition();
        }

        return IDLE;
    }

    private IAIState emptyGrave()
    {
        @Nullable final BuildingGraveyard buildingGraveyard = getOwnBuilding();

        if (buildingGraveyard == null || checkForToolOrWeapon(ToolType.SHOVEL) || buildingGraveyard.getGraveToWorkOn() == null)
        {
            return IDLE;
        }

        worker.getCitizenData().setVisibleStatus(EMPTYING_ICON);
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.emptying"));
        worker.setSprinting(true);
        unequip();

        @Nullable final BlockPos gravePos = buildingGraveyard.getGraveToWorkOn();
        final TileEntity entity = world.getTileEntity(gravePos);
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

            // Still moving to the block
            if (walkToBlock(gravePos, 1))
            {
                return getState();
            }

            if(effortCounter < EFFORT_EMPTY_GRAVE)
            {
                worker.swingArm(Hand.MAIN_HAND);
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

    private IAIState digGrave()
    {
        @Nullable final BuildingGraveyard buildingGraveyard = getOwnBuilding();

        if (buildingGraveyard == null || checkForToolOrWeapon(ToolType.SHOVEL) || buildingGraveyard.getGraveToWorkOn() == null)
        {
            return IDLE;
        }

        worker.getCitizenData().setVisibleStatus(DIGGING_ICON);
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.digging"));
        worker.setSprinting(false);

        @Nullable final BlockPos gravePos = buildingGraveyard.getGraveToWorkOn();
        final TileEntity entity = world.getTileEntity(gravePos);
        if (entity instanceof TileEntityGrave)
        {
            // Still moving to the block
            if (walkToBlock(gravePos, 1))
            {
                return getState();
            }

            //at position
            if (!digIfAble(gravePos))
            {
                return getState();
            }

            worker.decreaseSaturationForContinuousAction();
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
                worker.swingArm(worker.getActiveHand());
                world.setBlockState(position, Blocks.AIR.getDefaultState());
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
     *
     * Calculate chance of resurrectionfrom, rool to see if resurrection successfull and resurrect if need be
     */
    private IAIState tryResurrect()
    {
        @Nullable final BuildingGraveyard buildingGraveyard = getOwnBuilding();

        if (buildingGraveyard == null || checkForToolOrWeapon(ToolType.SHOVEL) || buildingGraveyard.getLastGraveData() == null)
        {
            return IDLE;
        }

        worker.getCitizenData().setVisibleStatus(RESURRECT_ICON);
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.resurrecting"));
        unequip();

        @Nullable final BlockPos gravePos = buildingGraveyard.getGraveToWorkOn();
        final TileEntity entity = world.getTileEntity(gravePos);
        if (entity instanceof TileEntityGrave)
        {
            // Still moving to the block
            if (walkToBlock(gravePos, 1))
            {
                return getState();
            }

            if (effortCounter < EFFORT_RESURRECT)
            {
                worker.swingArm(Hand.MAIN_HAND);
                effortCounter += getSecondarySkillLevel();
                return getState();
            }
            effortCounter = 0;

            shouldDumpInventory = true;
            final double chance = getResurrectChance(buildingGraveyard);

            if (chance >= random.nextDouble())
            {
                final ICitizenData citizenData = buildingGraveyard.getColony().getCitizenManager().resurrectCivilianData(buildingGraveyard.getLastGraveData().getCitizenDataNBT(), true, world, gravePos);
                LanguageHandler.sendPlayersMessage(buildingGraveyard.getColony().getImportantMessageEntityPlayers(), "com.minecolonies.coremod.resurrect", citizenData.getName());
                worker.getCitizenColonyHandler().getColony().setNeedToMourn(false, buildingGraveyard.getLastGraveData().getCitizenName());
                AdvancementUtils.TriggerAdvancementPlayersForColony(worker.getCitizenColonyHandler().getColony(), playerMP -> AdvancementTriggers.CITIZEN_RESURRECT.trigger(playerMP));
                buildingGraveyard.setLastGraveData(null);
                world.setBlockState(gravePos, Blocks.AIR.getDefaultState());
                return INVENTORY_FULL;
            }
        }

        return DIG_GRAVE;
    }

    private double getResurrectChance(@NotNull final BuildingGraveyard buildingGraveyard)
    {
        double chance = buildingGraveyard.getBuildingLevel() * RESURRECT_BUILDING_LVL_WEIGHT +
                worker.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Mana) * RESURRECT_WORKER_MANA_LVL_WEIGHT +
                worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(RESURRECT_CHANCE);

        final double cap = MAX_RESURRECTION_CHANCE + worker.getCitizenColonyHandler().getColony().getBuildingManager().getMysticalSiteMaxBuildingLevel() * MAX_RESURRECTION_CHANCE_MYSTICAL_LVL_BONUS;
        if (chance > cap) { chance = cap; }
        return chance;
    }

    private IAIState buryCitizen()
    {
        @Nullable final BuildingGraveyard buildingGraveyard = getOwnBuilding();

        if (buildingGraveyard == null || checkForToolOrWeapon(ToolType.SHOVEL) || buildingGraveyard.getLastGraveData() == null)
        {
            return IDLE;
        }
        worker.getCitizenData().setVisibleStatus(BURYING_ICON);
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.burying"));

        if(burialPos == null || !world.getBlockState(burialPos.getA()).isAir())
        {
            burialPos = getOwnBuilding().getRandomFreeVisualGravePos();
        }

        if(burialPos == null)
        {
            //coudn't find a place to dig a grave
            LanguageHandler.sendPlayersMessage(buildingGraveyard.getColony().getImportantMessageEntityPlayers(),
                    "com.minecolonies.coremod.nospaceforgrave", buildingGraveyard.getLastGraveData().getCitizenName());
            return IDLE;
        }

        if(walkToBlock(burialPos.getA(), 1))
        {
            return getState();
        }

        if(effortCounter < EFFORT_BURY)
        {
            equipShovel();
            worker.swingArm(Hand.MAIN_HAND);
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
