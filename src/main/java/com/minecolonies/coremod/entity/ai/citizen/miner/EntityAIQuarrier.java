package com.minecolonies.coremod.entity.ai.citizen.miner;

import com.ldtteam.structurize.management.Structures;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.entity.pathfinding.SurfaceType;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.modules.QuarryModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.JobQuarrier;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildMiner;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructureWithWorkOrder;
import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.research.util.ResearchConstants.MORE_ORES;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner.FILL_BLOCK;

/**
 * Class which handles the miner behaviour.
 */
public class EntityAIQuarrier extends AbstractEntityAIStructureWithWorkOrder<JobQuarrier, BuildingMiner>
{
    private static final String RENDER_META_TORCH = "torch";
    private static final String RENDER_META_STONE = "stone";

    /**
     * Return to chest after 3 stacks.
     */
    private static final int MAX_BLOCKS_MINED = 64;

    /**
     * Mining icon
     */
    private final static VisibleCitizenStatus MINING =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/miner.png"), "com.minecolonies.gui.visiblestatus.miner");

    /**
     * Constructor for the Miner. Defines the tasks the miner executes.
     *
     * @param job a fisherman job to use.
     */
    public EntityAIQuarrier(@NotNull final JobQuarrier job)
    {
        super(job);
        super.registerTargets(
          /*
           * If IDLE - switch to start working.
           */
          new AITarget(IDLE, START_WORKING, 1),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding, TICKS_SECOND)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingMiner> getExpectedBuildingClass()
    {
        return BuildingMiner.class;
    }

    //Miner wants to work but is not at building
    @NotNull
    private IAIState startWorkingAtOwnBuilding()
    {
        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);

        final IBuilding quarry = job.findQuarry();
        if (quarry == null)
        {
            worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslationTextComponent(QUARRY_MINER_NO_QUARRY), ChatPriority.BLOCKING));
            return IDLE;
        }

        if (quarry.getFirstModuleOccurance(QuarryModule.class).isFinished())
        {
            worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslationTextComponent(QUARRY_MINER_FINISHED_QUARRY), ChatPriority.BLOCKING));
            return IDLE;
        }

        if (walkToBlock(quarry.getPosition()))
        {
            return getState();
        }

        //Miner is at building
        return LOAD_STRUCTURE;
    }

    @Override
    public IAIState loadRequirements()
    {
        if (job.getWorkOrder() == null)
        {
            final IBuilding quarry = job.findQuarry();
            if (quarry == null || quarry.getFirstModuleOccurance(QuarryModule.class).isFinished())
            {
                return IDLE;
            }

            final String name = Structures.SCHEMATICS_PREFIX + "/" + quarry.getStyle() + "/" + quarry.getSchematicName() + "shaft1";
            final WorkOrderBuildMiner wo = new WorkOrderBuildMiner(name, name, quarry.getRotation(), quarry.getPosition().below(2), false, getOwnBuilding().getPosition());
            getOwnBuilding().getColony().getWorkManager().addWorkOrder(wo, false);
            job.setWorkOrder(wo);
        }

        return super.loadRequirements();
    }

    @Override
    protected IBuilding getBuildingToDump()
    {
        final IBuilding quarry = job.findQuarry();
        return quarry == null ? super.getBuildingToDump() : quarry;
    }

    //todo call it Quarrier
    //todo iterator mode?
    //todo request to quarry and not to hut, can we do that? this should be possible!
    //todo adjust mining order (last - after building already works)

    @Override
    public int getBreakSpeedLevel()
    {
        return getPrimarySkillLevel();
    }

    @Override
    public int getPlaceSpeedLevel()
    {
        return getSecondarySkillLevel();
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return getOwnBuilding().getBuildingLevel() * MAX_BLOCKS_MINED;
    }

    @Override
    protected void updateRenderMetaData()
    {
        worker.setRenderMetadata(getRenderMetaStone() + getRenderMetaTorch());
    }

    /**
     * Get render data to render torches at the backpack if in inventory.
     *
     * @return metaData String if so.
     */
    @NotNull
    private String getRenderMetaTorch()
    {
        if (worker.getCitizenInventoryHandler().hasItemInInventory(Items.TORCH))
        {
            return RENDER_META_TORCH;
        }
        return "";
    }

    /**
     * Get render data to render stone in the backpack if cobble in inventory.
     *
     * @return metaData String if so.
     */
    @NotNull
    private String getRenderMetaStone()
    {
        if (worker.getCitizenInventoryHandler().hasItemInInventory(getMainFillBlock()))
        {
            return RENDER_META_STONE;
        }
        return "";
    }

    @Override
    public IAIState doMining()
    {
        if (blockToMine == null)
        {
            return BUILDING_STEP;
        }

        for (final Direction direction : Direction.values())
        {
            final BlockPos pos = blockToMine.relative(direction);
            final BlockState surroundingState = world.getBlockState(pos);

            final FluidState fluid = world.getFluidState(pos);
            if (surroundingState.getBlock() == Blocks.LAVA || (fluid != null && !fluid.isEmpty() && (fluid.getType() == Fluids.LAVA || fluid.getType() == Fluids.FLOWING_LAVA)) || SurfaceType.isWater(world, pos, surroundingState, fluid))
            {
                setBlockFromInventory(pos, getMainFillBlock());
            }
        }

        final BlockState blockState = world.getBlockState(blockToMine);
        if (!IColonyManager.getInstance().getCompatibilityManager().isOre(blockState))
        {
            blockToMine = getSurroundingOreOrDefault(blockToMine);
        }

        if (world.getBlockState(blockToMine).getBlock() instanceof AirBlock)
        {
            return BUILDING_STEP;
        }

        if (!mineBlock(blockToMine, getCurrentWorkingPosition()))
        {
            worker.swing(Hand.MAIN_HAND);
            return getState();
        }

        blockToMine = getSurroundingOreOrDefault(blockToMine);
        if (IColonyManager.getInstance().getCompatibilityManager().isOre(world.getBlockState(blockToMine)))
        {
            return getState();
        }

        worker.decreaseSaturationForContinuousAction();
        return BUILDING_STEP;
    }

    private BlockPos getSurroundingOreOrDefault(final BlockPos pos)
    {
        for (Direction direction : Direction.values())
        {
            final BlockPos offset = pos.relative(direction);
            if (IColonyManager.getInstance().getCompatibilityManager().isOre(world.getBlockState(offset)))
            {
                return offset;
            }
        }
        return pos;
    }

    /**
     * Get the main fill block. Based on the settings.
     *
     * @return the main fill block.
     */
    private Block getMainFillBlock()
    {
        return getOwnBuilding().getSetting(FILL_BLOCK).getValue().getBlock();
    }

    @Override
    public ItemStack getTotalAmount(final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return null;
        }

        final ItemStack copy = stack.copy();
        copy.setCount(Math.max(super.getTotalAmount(stack).getCount(), copy.getMaxStackSize() / 2));
        return copy;
    }

    @Override
    public IAIState afterStructureLoading()
    {
        return BUILDING_STEP;
    }

    private void setBlockFromInventory(@NotNull final BlockPos location, @NotNull final Block block)
    {
        worker.swing(worker.getUsedItemHand());
        setBlockFromInventory(location, block, block.defaultBlockState());
    }

    private void setBlockFromInventory(@NotNull final BlockPos location, final Block block, final BlockState metadata)
    {
        final int slot;
        if (block instanceof LadderBlock)
        {
            slot = worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(block);
        }
        else
        {
            slot = worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(block);
        }
        if (slot != -1)
        {
            getInventory().extractItem(slot, 1, false);
            //Flag 1+2 is needed for updates
            WorldUtil.setBlockState(world, location, metadata);
        }
    }

    @Override
    public void executeSpecificCompleteActions()
    {
        super.executeSpecificCompleteActions();
        final IBuilding quarry = job.findQuarry();
        if (quarry != null)
        {
            quarry.getFirstModuleOccurance(QuarryModule.class).setFinished();
        }
    }

    @Override
    public boolean shallReplaceSolidSubstitutionBlock(final Block worldBlock, final BlockState worldMetadata)
    {
        return IColonyManager.getInstance().getCompatibilityManager().isOre(worldMetadata);
    }

    @Override
    protected void triggerMinedBlock(@NotNull final BlockState blockToMine)
    {
        super.triggerMinedBlock(blockToMine);
        final double chance = 1 + worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(MORE_ORES);
        if (IColonyManager.getInstance().getCompatibilityManager().isLuckyBlock(blockToMine.getBlock()))
        {
            InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(IColonyManager.getInstance().getCompatibilityManager().getRandomLuckyOre(chance),
              worker.getInventoryCitizen());
        }
    }

    @Override
    public BlockState getSolidSubstitution(final BlockPos ignored)
    {
        return getMainFillBlock().defaultBlockState();
    }
}
