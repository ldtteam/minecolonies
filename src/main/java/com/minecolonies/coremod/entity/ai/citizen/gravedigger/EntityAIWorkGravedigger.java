package com.minecolonies.coremod.entity.ai.citizen.gravedigger;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.tileentities.TileEntityGrave;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingGraveyard;
import com.minecolonies.coremod.colony.jobs.JobGravedigger;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import net.minecraft.block.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Gravedigger AI class.
 */
public class EntityAIWorkGravedigger extends AbstractEntityAICrafting<JobGravedigger, BuildingGraveyard>
{
    /**
     * Return to chest after this amount of stacks.
     */
    private static final int MAX_BLOCKS_MINED = 64;

    /**
     * The standard delay the gravedigger should have.
     */
    private static final int STANDARD_DELAY = 40;

    /**
     * The smallest delay the gravedigger should have.
     */
    private static final int SMALLEST_DELAY = 1;

    /**
     * The bonus the gravedigger gains each update is level/divider.
     */
    private static final double DELAY_DIVIDER = 1;

    /**
     * The EXP Earned per dig.
     */
    private static final double XP_PER_DIG = 0.5;

    /**
     * Gravedigger icon
     */
    private final static VisibleCitizenStatus DIGGING_ICON =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/gravedigger.png"), "com.minecolonies.gui.visiblestatus.farmer");

    /**
     * Changed after finished digging in order to dump the inventory.
     */
    private boolean shouldDumpInventory = false;

    /**
     * The previous position which has been worked at.
     */
    @Nullable
    private BlockPos prevPos;

    /**
     * Constructor for the Gravedigger. Defines the tasks the Gravedigger executes.
     *
     * @param job a gravedigger job to use.
     */
    public EntityAIWorkGravedigger(@NotNull final JobGravedigger job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, () -> START_WORKING, 10),
          new AITarget(PREPARING, this::prepareForDigging, TICKS_SECOND),
          new AITarget(DIG_GRAVE, this::digGrave, 5)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingGraveyard> getExpectedBuildingClass()
    {
        return BuildingGraveyard.class;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return MAX_BLOCKS_MINED;
    }

    @Override
    protected int getActionRewardForCraftingSuccess()
    {
        return MAX_BLOCKS_MINED;
    }

    @Override
    protected IAIState decide()
    {
        final IAIState nextState = super.decide();
        if (nextState != START_WORKING)
        {
            return nextState;
        }

        if (job.getTaskQueue().isEmpty())
        {
            return PREPARING;
        }

        if (job.getCurrentTask() == null)
        {
            return PREPARING;
        }

        return GET_RECIPE;
    }

    /**
     * Prepares the gravedigger for digging. Also requests the tools and checks if the gravedigger has queued graves.
     *
     * @return the next IAIState
     */
    @NotNull
    private IAIState prepareForDigging()
    {
        @Nullable final BuildingGraveyard building = getOwnBuilding();
        if (building == null || building.getBuildingLevel() < 1)
        {
            return PREPARING;
        }

        if (!job.getTaskQueue().isEmpty())
        {
            return START_WORKING;
        }
        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);

        if (building.getPendingGraves().isEmpty())
        {
            worker.getCitizenData().setIdleAtJob(true);
            return IDLE;
        }

        worker.getCitizenData().setIdleAtJob(false);

        @Nullable final BlockPos currentGrave = building.getGraveToWorkOn();
        if (currentGrave == null)
        {
            return IDLE;
        }
        else
        {
            final TileEntity entity = world.getTileEntity(currentGrave);
            if (entity != null && entity instanceof TileEntityGrave)
            {
                return DIG_GRAVE;
            }
            building.ClearCurrentGrave();
        }

        return PREPARING;
    }

    private IAIState digGrave()
    {
        @Nullable final BuildingGraveyard buildingGraveyard = getOwnBuilding();

        if (buildingGraveyard == null || checkForToolOrWeapon(ToolType.SHOVEL) || buildingGraveyard.getGraveToWorkOn() == null)
        {
            return PREPARING;
        }
        worker.getCitizenData().setVisibleStatus(DIGGING_ICON);

        @Nullable final BlockPos gravePos = buildingGraveyard.getGraveToWorkOn();
        final TileEntity entity = world.getTileEntity(gravePos);
        if (entity instanceof TileEntityGrave)
        {
            // Still moving to the block
            worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.walking"));

            if (walkToBlock(gravePos.up().south(1).east(1)))
            {
                return getState();
            }

            //at position
            worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.digging"));
            if (!digIfAble(gravePos))
            {
                return getState();
            }

            worker.getCitizenColonyHandler().getColony().removeNeedToMourn(((TileEntityGrave) entity).getSavedCitizenName());
            shouldDumpInventory = true;
            buildingGraveyard.ClearCurrentGrave();
            return IDLE;
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
                return true;
            }
            return false;
        }
        return true;
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
