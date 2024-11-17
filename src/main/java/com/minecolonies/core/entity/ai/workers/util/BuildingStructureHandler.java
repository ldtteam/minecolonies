package com.minecolonies.core.entity.ai.workers.util;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.placement.structure.AbstractStructureHandler;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.colony.jobs.AbstractJobStructure;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIStructure;
import com.minecolonies.core.util.WorkerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;

import static com.minecolonies.api.util.constant.CitizenConstants.RUN_AWAY_SPEED;
import static com.minecolonies.api.util.constant.StatisticsConstants.BLOCKS_PLACED;

/**
 * Represents a build task for the StructureIterator AI.
 * <p>
 * It internally uses a structure it transparently loads.
 */
public class BuildingStructureHandler<J extends AbstractJobStructure<?, J>, B extends AbstractBuildingStructureBuilder> extends AbstractStructureHandler
{
    /**
     * Amount of xp the builder gains for placing a block.
     */
    private static final double XP_EACH_BLOCK = 0.05D;

    /**
     * The structure AI handling this task.
     */
    private final AbstractEntityAIStructure<J, B> structureAI;

    /**
     * The total number of stages.
     */
    private final Stage[] stages;

    /**
     * The building associated with this placement.
     */
    private IBuilding building;

    /**
     * The current structure stage.
     */
    private int stage;

    /**
     * The minecolonies AI specific creative structure placer.
     *
     * @param world             the world.
     * @param worldPos          the pos it is placed at.
     * @param blueprintFuture   the structure.
     * @param settings          the placement settings.
     * @param entityAIStructure the AI handling this structure.
     */
    public BuildingStructureHandler(
      final Level world,
      final BlockPos worldPos,
      final Future<Blueprint> blueprintFuture,
      final PlacementSettings settings,
      final AbstractEntityAIStructure<J, B> entityAIStructure,
      final Stage[] stages)
    {
        super(world, worldPos, blueprintFuture, settings);
        setupBuilding();
        this.structureAI = entityAIStructure;
        this.stages = stages;
        this.stage = 0;
    }

    /**
     * The minecolonies AI specific creative structure placer.
     *
     * @param world             the world.
     * @param worldPos          the pos it is placed at.
     * @param blueprint         the blueprint.
     * @param settings          the placement settings.
     * @param entityAIStructure the AI handling this structure.
     */
    public BuildingStructureHandler(
      final Level world,
      final BlockPos worldPos,
      final Blueprint blueprint,
      final PlacementSettings settings,
      final AbstractEntityAIStructure<J, B> entityAIStructure,
      final Stage[] stages)
    {
        super(world, worldPos, blueprint, settings);
        setupBuilding();
        this.structureAI = entityAIStructure;
        this.stages = stages;
        this.stage = 0;
    }

    /**
     * Setup the building to register things to.
     */
    private void setupBuilding()
    {
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(getWorld(), getWorldPos());
        if (colony != null)
        {
            this.building = colony.getBuildingManager().getBuilding(getWorldPos());
        }
    }

    /**
     * Get the current stage we're in.
     *
     * @return the current Stage.
     */
    @Nullable
    public Stage getStage()
    {
        if (this.stage >= stages.length)
        {
            return null;
        }
        return stages[stage];
    }

    /**
     * Go to the next stage.
     */
    public boolean nextStage()
    {
        return ++this.stage < stages.length;
    }

    /**
     * Set the current stage from memory.
     *
     * @param stage the stage to set.
     */
    public void setStage(final Stage stage)
    {
        for (int i = 0; i < stages.length; i++)
        {
            if (stages[i] == stage)
            {
                this.stage = i;
                return;
            }
        }
    }

    @Override
    public void prePlacementLogic(final BlockPos worldPos, final BlockState blockState, final List<ItemStack> requiredItems)
    {
        WorkerUtil.faceBlock(worldPos, structureAI.getWorker());
        //Move out of the way when placing blocks
        structureAI.getWorker().setItemSlot(EquipmentSlot.MAINHAND, requiredItems.isEmpty() ? ItemStackUtils.EMPTY : requiredItems.get(0));

        if (Mth.floor(structureAI.getWorker().getX()) == worldPos.getX()
              && Mth.abs(worldPos.getY() - (int) structureAI.getWorker().getY()) <= 1
              && Mth.floor(structureAI.getWorker().getZ()) == worldPos.getZ()
              && structureAI.getWorker().getNavigation().isDone())
        {
            structureAI.getWorker().getNavigation().moveAwayFromXYZ(worldPos, RUN_AWAY_SPEED, 1, true);
        }

        structureAI.getWorker().swing(InteractionHand.MAIN_HAND);
    }

    @Nullable
    @Override
    public IItemHandler getInventory()
    {
        return structureAI.getWorker().getInventoryCitizen();
    }

    @Override
    public void triggerSuccess(final BlockPos pos, final List<ItemStack> list, final boolean placement)
    {
        final BlockPos worldPos = getProgressPosInWorld(pos);
        final BlockState state = getBluePrint().getBlockState(pos);
        if (building != null)
        {
            building.registerBlockPosition(state, worldPos, this.getWorld());
        }

        if (placement)
        {
            structureAI.getWorker().getCitizenExperienceHandler().addExperience(XP_EACH_BLOCK);

            for (final ItemStack stack : list)
            {
                structureAI.reduceNeededResources(stack);
                structureAI.getWorker()
                  .getCitizenColonyHandler()
                  .getColonyOrRegister()
                  .getStatisticsManager()
                  .increment(BLOCKS_PLACED, structureAI.getWorker().getCitizenColonyHandler().getColonyOrRegister().getDay());
            }
            
            BlockState blockStateForSound;
            if (state.getBlock() == com.ldtteam.structurize.blocks.ModBlocks.blockSolidSubstitution.get()) 
            {
                // If the builder is placing a substitution block, use the sound of the substituted block
                // fancyPlacement() could be checked here, but is always true for this Handler.
                blockStateForSound = structureAI.getSolidSubstitution(pos);
            }
            else 
            {
                // If the block is not a substitution block, use the sound of the block itself
                blockStateForSound = state;
            }
            structureAI.getWorker().queueSound(blockStateForSound.getSoundType().getPlaceSound(), worldPos, 10, 0, (blockStateForSound.getSoundType().getVolume() + 1.0F) * 0.5F, blockStateForSound.getSoundType().getPitch() * 0.8F);
        }

        if (state.getBlock() == ModBlocks.blockWayPoint)
        {
            structureAI.getWorker().getCitizenColonyHandler().getColonyOrRegister().addWayPoint(worldPos, state);
        }
    }

    @Override
    public void triggerEntitySuccess(final BlockPos blockPos, final List<ItemStack> list, final boolean placement)
    {
        if (placement)
        {
            structureAI.getWorker().getCitizenExperienceHandler().addExperience(XP_EACH_BLOCK);

            for (final ItemStack stack : list)
            {
                structureAI.reduceNeededResources(stack);
            }
        }
    }

    @Override
    public boolean hasRequiredItems(@NotNull final List<ItemStack> requiredItems)
    {
        final List<ItemStack> itemList = new ArrayList<>();
        for (final ItemStack stack : requiredItems)
        {
            if (ModEquipmentTypes.flint_and_steel.get().checkIsEquipment(stack))
            {
                if (structureAI.checkForToolOrWeapon(ModEquipmentTypes.flint_and_steel.get()))
                {
                    return false;
                }
            }

            itemList.add(stack);
        }

        return AbstractEntityAIStructure.hasListOfResInInvOrRequest(structureAI, itemList, itemList.size() > 1) == AbstractEntityAIStructure.ItemCheckResult.SUCCESS;
    }

    @Override
    public void consume(final List<ItemStack> requiredItems)
    {
        if (this.getInventory() != null)
        {
            for (final ItemStack tempStack : requiredItems)
            {
                if (!ItemStackUtils.isEmpty(tempStack))
                {
                    InventoryUtils.reduceStackInItemHandler(this.getInventory(), tempStack);
                }
            }
        }
    }

    @Override
    public boolean isCreative()
    {
        return Constants.BUILDER_INF_RESOURECES;
    }

    @Override
    public int getStepsPerCall()
    {
        return 1;
    }

    @Override
    public int getMaxBlocksCheckedPerCall()
    {
        return 10000;
    }

    @Override
    public boolean isStackFree(@Nullable final ItemStack itemStack)
    {
        return itemStack == null
                 || itemStack.isEmpty()
                 || itemStack.is(ItemTags.LEAVES)
                 || itemStack.getItem() == new ItemStack(ModBlocks.blockDecorationPlaceholder, 1).getItem();
    }

    @Override
    public boolean allowReplace()
    {
        return getStage() != null && getStage() != Stage.CLEAR;
    }

    @Override
    public ItemStack getHeldItem()
    {
        return structureAI.getWorker().getMainHandItem();
    }

    @Override
    public BlockState getSolidBlockForPos(final BlockPos blockPos)
    {
        return structureAI.getSolidSubstitution(blockPos);
    }

    @Override
    public BlockState getSolidBlockForPos(final BlockPos worldPos, @Nullable final Function<BlockPos, BlockState> virtualBlocks)
    {
        return structureAI.getSolidSubstitution(worldPos);
    }

    @Override
    public boolean replaceWithSolidBlock(final BlockState blockState)
    {
        return !BlockUtils.isGoodFloorBlock(blockState) || structureAI.shallReplaceSolidSubstitutionBlock(blockState.getBlock(), blockState);
    }

    @Override
    public boolean fancyPlacement()
    {
        return true;
    }

    @Override
    public boolean shouldBlocksBeConsideredEqual(final BlockState state1, final BlockState state2)
    {
        final Block block1 = state1.getBlock();
        final Block block2 = state2.getBlock();

        if (block1 == Blocks.FLOWER_POT || block2 == Blocks.FLOWER_POT)
        {
            return block1 == block2;
        }

        return (block1 == Blocks.GRASS_BLOCK && block2 == Blocks.DIRT)
                 || (block2 == Blocks.GRASS_BLOCK && block1 == Blocks.DIRT)
                 || (block1 == ModBlocks.blockRack && block2 == ModBlocks.blockRack);
    }

    /**
     * The different stages a StructureIterator building process can be in.
     */
    public enum Stage
    {
        CLEAR,
        BUILD_SOLID,
        CLEAR_WATER,
        CLEAR_NON_SOLIDS,
        DECORATE,
        SPAWN,
        REMOVE,
        REMOVE_WATER,
        WEAK_SOLID,
    }
}
