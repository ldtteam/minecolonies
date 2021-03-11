package com.minecolonies.coremod.entity.ai.citizen.gravedigger;

import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.tileentities.TileEntityGrave;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.blocks.BlockMinecoloniesGrave;
import com.minecolonies.coremod.blocks.huts.BlockHutCitizen;
import com.minecolonies.coremod.colony.buildings.BuildingMysticalSite;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingEnchanter;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingGraveyard;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.colony.jobs.JobGravedigger;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildMiner;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructureWithWorkOrder;
import com.minecolonies.coremod.entity.ai.citizen.miner.Level;
import com.minecolonies.coremod.entity.ai.util.BuildingStructureHandler;
import com.minecolonies.coremod.util.AdvancementUtils;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.block.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.research.util.ResearchConstants.RESURECT_CHANCE;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Gravedigger AI class.
 */
public class EntityAIWorkGravedigger extends AbstractEntityAIInteract<JobGravedigger, BuildingGraveyard>
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
     * The weigth of each building level on the resurection chances.
     */
    private static final double RESURECT_BUILDING_LVL_WEIGHT = 0.005;

    /**
     * The weigth of each worker level on the resurection chances.
     */
    private static final double RESURECT_WORKER_MANA_LVL_WEIGHT = 0.0025;

    /**
     * The max resurection chance cap [0.0 min -> 1.0 max]
     */
    private static final double MAX_RESURECTION_CHANCE = 0.10;

    /**
     * The bonus to max resurection chance cap per max lvl of Mystical Site in the city
     */
    private static final double MAX_RESURECTION_CHANCE_MYSTICAL_LVL_BONUS = 0.01;

    /**
     * The random variable.
     */
    private Random random = new Random();

    /**
     * Gravedigger emptying icon
     */
    private final static VisibleCitizenStatus EMPTYING_ICON =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/gravedigger.png"), "com.minecolonies.gui.visiblestatus.emptying");

    /**
     * Gravedigger digging icon
     */
    private final static VisibleCitizenStatus DIGGING_ICON =
            new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/gravedigger.png"), "com.minecolonies.gui.visiblestatus.digging");

    /**
     * Gravedigger burry icon
     */
    private final static VisibleCitizenStatus BURYING_ICON =
            new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/gravedigger.png"), "com.minecolonies.gui.visiblestatus.burying");

    /**
     * Gravedigger burry icon
     */
    private final static VisibleCitizenStatus RESURRECT_ICON =
            new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/gravedigger.png"), "com.minecolonies.gui.visiblestatus.resurrect");

    /**
     * Changed after finished digging in order to dump the inventory.
     */
    private boolean shouldDumpInventory = false;

    /**
     * The current pos to wander at.
     */
    private BlockPos wanderPos = null;

    /**
     * Constructor for the Gravedigger. Defines the tasks the Gravedigger executes.
     *
     * @param job a gravedigger job to use.
     */
    public EntityAIWorkGravedigger(@NotNull final JobGravedigger job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, REQUEST_DELAY),
          new AITarget(START_WORKING, this::startWorking, STANDARD_DELAY),
          new AITarget(EMPTY_GRAVE, this::emptyGrave, STANDARD_DELAY),
          new AITarget(DIG_GRAVE, this::digGrave, STANDARD_DELAY),
          new AITarget(BURRY_CITIZEN, this::buryCitizen, STANDARD_DELAY),
          new AITarget(TRY_RESURRECT, this::tryResurrect, STANDARD_DELAY)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingGraveyard> getExpectedBuildingClass()
    {
        return BuildingGraveyard.class;
    }

    /**
     * Prepares the gravedigger for digging. Also requests the tools and checks if the gravedigger has queued graves.
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
                return EMPTY_GRAVE;
            }
            building.ClearCurrentGrave();
        }

        //TODO enable/diable for testing visual grave building
        if(false)
            buildVisualGrave("Fallen Citizen Name");

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
        return getState();
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

        @Nullable final BlockPos gravePos = buildingGraveyard.getGraveToWorkOn();
        final TileEntity entity = world.getTileEntity(gravePos);
        if (entity instanceof TileEntityGrave)
        {
            if (((TileEntityGrave) entity).isEmpty())
            {
                return DIG_GRAVE;
            }

            if (worker.getInventoryCitizen().isFull())
            {
                // coudn't take all item --> empty inventory to building
                shouldDumpInventory = true;
                return IDLE;
            }

            // Still moving to the block
            if (walkToBlock(gravePos, 1))
            {
                return getState();
            }

            //at position - try to take all item
            if (InventoryUtils.transferAllItemHandler(((TileEntityGrave) entity).getInventory(), worker.getInventoryCitizen()))
            {
                return DIG_GRAVE;
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

        @Nullable final BlockPos gravePos = buildingGraveyard.getGraveToWorkOn();
        final TileEntity entity = world.getTileEntity(gravePos);
        if (entity instanceof TileEntityGrave)
        {
            // Still moving to the block
            //if (walkToBlock(gravePos.up().south(1).east(1)))
            if (walkToBlock(gravePos, 1))
            {
                return getState();
            }

            buildingGraveyard.setLastGraveOwner(((TileEntityGrave) entity).getSavedCitizenDataNBT(), ((TileEntityGrave) entity).getSavedCitizenName());

            //at position
            if (!digIfAble(gravePos))
            {
                return getState();
            }

            worker.decreaseSaturationForContinuousAction();
            worker.getCitizenData().getCitizenSkillHandler().addXpToSkill(getOwnBuilding().getPrimarySkill(), XP_PER_DIG, worker.getCitizenData());
            return BURRY_CITIZEN;
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

    private IAIState buryCitizen()
    {
        @Nullable final BuildingGraveyard buildingGraveyard = getOwnBuilding();

        if (buildingGraveyard == null || checkForToolOrWeapon(ToolType.SHOVEL) || buildingGraveyard.getLastGraveOwnerNBT() == null)
        {
            return IDLE;
        }
        worker.getCitizenData().setVisibleStatus(BURYING_ICON);
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.burying"));

        //Go back to graveyard
        if (walkToBuilding())
        {
            return getState();
        }

        worker.getCitizenColonyHandler().getColony().removeNeedToMourn(buildingGraveyard.getLastGraveName(),false);
        AdvancementUtils.TriggerAdvancementPlayersForColony(worker.getCitizenColonyHandler().getColony(), playerMP -> AdvancementTriggers.CITIZEN_BURY.trigger(playerMP));

        return TRY_RESURRECT;
    }

    /**
     * Attempt to resurect buried citizen from its citizen data
     *
     * Calculate chance of resurectionfrom, rool to see if resurection successfull and resurect if need be
     */
    private IAIState tryResurrect()
    {
        @Nullable final BuildingGraveyard buildingGraveyard = getOwnBuilding();

        if (buildingGraveyard == null || checkForToolOrWeapon(ToolType.SHOVEL) || buildingGraveyard.getLastGraveOwnerNBT() == null)
        {
            return IDLE;
        }
        worker.getCitizenData().setVisibleStatus(RESURRECT_ICON);
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.resurrecting"));

        //Go back to graveyard
        if (walkToBuilding())
        {
            return getState();
        }

        shouldDumpInventory = true;

        double chance = buildingGraveyard.getBuildingLevel() * RESURECT_BUILDING_LVL_WEIGHT +
                worker.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Mana) * RESURECT_WORKER_MANA_LVL_WEIGHT +
                worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(RESURECT_CHANCE);

        double cap = MAX_RESURECTION_CHANCE + worker.getCitizenColonyHandler().getColony().getBuildingManager().getMysticalSiteMaxBuildingLevel() * MAX_RESURECTION_CHANCE_MYSTICAL_LVL_BONUS;
        if (chance > cap) { chance = cap; }

        if (chance >= random.nextDouble())
        {
            final ICitizenData citizenData = buildingGraveyard.getColony().getCitizenManager().resurrectCivilianData(buildingGraveyard.getLastGraveOwnerNBT(), true, world, buildingGraveyard.getPosition());
            LanguageHandler.sendPlayersMessage(buildingGraveyard.getColony().getImportantMessageEntityPlayers(), "com.minecolonies.coremod.resurrect", citizenData.getName());
            worker.getCitizenColonyHandler().getColony().removeNeedToMourn(buildingGraveyard.getLastGraveName(), true);
            AdvancementUtils.TriggerAdvancementPlayersForColony(worker.getCitizenColonyHandler().getColony(), playerMP -> AdvancementTriggers.CITIZEN_RESURRECT.trigger(playerMP));
        }
        else
        {
            buildingGraveyard.BuryCitizenHere(buildingGraveyard.getLastGraveName());
            buildVisualGrave(buildingGraveyard.getLastGraveName());
        }

        return IDLE;
    }

    public IAIState buildVisualGrave(final String citizenName)
    {
        final BlockPos position = getOwnBuilding().getRandomFreeVisualGravePos();

        if(position != null)
        {
            if(walkToBlock(position, 1))
            {
                //TODO TG return STATE BUILD GRAVE
            }

            world.setBlockState(position, BlockMinecoloniesGrave.getPlacementState(ModBlocks.blockRack.getDefaultState(), new TileEntityRack(), position));
            //TODO TG Set name in custom renderer final TileEntityRack graveEntity = (TileEntityRack) world.getTileEntity(position);
        }

        return IDLE;
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
     * Get's the slot in which the shovel is in.
     *
     * @return slot number
     */
    private int getShovelSlot()
    {
        return InventoryUtils.getFirstSlotOfItemHandlerContainingTool(getInventory(), ToolType.SHOVEL, TOOL_LEVEL_WOOD_OR_GOLD, getOwnBuilding().getMaxToolLevel());
    }

    /**
     * Returns the gravedigger's worker instance. Called from outside this class.
     *
     * @return citizen object
     */
    @Nullable
    public AbstractEntityCitizen getCitizen()
    {
        return worker;
    }
}
