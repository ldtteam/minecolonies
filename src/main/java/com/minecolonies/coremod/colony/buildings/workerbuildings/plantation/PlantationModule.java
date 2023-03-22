package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation;

import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.PlantationField;
import com.minecolonies.coremod.entity.ai.citizen.planter.EntityAIWorkPlanter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Base class for planter modules that determines how the AI should work specific fields.
 */
public abstract class PlantationModule
{
    /**
     * The default maximum amount of plants the field can have.
     */
    protected static final int DEFAULT_MAX_PLANTS = 20;

    /**
     * The tag that the field anchor block contains in order to select which of these modules to use.
     */
    private final String fieldTag;

    /**
     * The tag that the individual working positions must contain.
     */
    private final String workTag;

    /**
     * The block which is harvested in this module.
     */
    private final Item item;

    /**
     * Default constructor.
     *
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    protected PlantationModule(
      final String fieldTag,
      final String workTag,
      final Item item)
    {
        this.fieldTag = fieldTag;
        this.workTag = workTag;
        this.item = item;
    }

    /**
     * Get the field tag property.
     *
     * @return the field tag.
     */
    public final String getFieldTag()
    {
        return fieldTag;
    }

    /**
     * Get the work tag property.
     *
     * @return the work tag.
     */
    public final String getWorkTag()
    {
        return workTag;
    }

    /**
     * Get the item the module uses.
     * (This is obtained through the block using {@link Block#asItem()})
     *
     * @return the item.
     */
    public final Item getItem()
    {
        return item;
    }

    /**
     * Get the amount of plants to request when the planter no longer has any left.
     * Defaults to quarter stack size of the block.
     *
     * @return the amount of plants to request.
     */
    public int getPlantsToRequest()
    {
        return (int) Math.ceil(new ItemStack(item).getMaxStackSize() / 4d);
    }

    /**
     * Get the required research effect for this module.
     *
     * @return the key of where to find the effect.
     */
    public ResourceLocation getRequiredResearchEffect()
    {
        return null;
    }

    /**
     * Core function for the planter module, is responsible for telling the AI what to do on the specific field.
     *
     * @param field        the field reference to fetch data from.
     * @param planterAI    the AI class of the planter so instructions can be ordered to it.
     * @param worker       the worker entity working on the plantation.
     * @param workPosition the position that has been chosen for work.
     * @param fakePlayer   a fake player class to use.
     * @return a basic enum state telling the planter AI what the AI should be doing next.
     */
    public abstract PlanterAIModuleResult workField(
      @NotNull PlantationField field,
      @NotNull EntityAIWorkPlanter planterAI,
      @NotNull AbstractEntityCitizen worker,
      @NotNull BlockPos workPosition,
      @NotNull FakePlayer fakePlayer);

    /**
     * Determines if the given field needs work.
     *
     * @param field the field reference to fetch data from.
     * @return true if so.
     */
    public boolean needsWork(PlantationField field)
    {
        return getNextWorkingPosition(field) != null;
    }

    /**
     * Determines if there's any work left to do on this field. If so, where.
     *
     * @param field the field reference to fetch data from.
     * @return the position inside the field that needs work or null.
     */
    @Nullable
    public abstract BlockPos getNextWorkingPosition(PlantationField field);

    /**
     * Returns a list of items that are mandatory for this module to function.
     *
     * @return a list of items.
     */
    public abstract List<ItemStack> getRequiredItemsForOperation();

    /**
     * Returns the requested tool type in order to work on this module.
     *
     * @return the tool to work on this module.
     */
    public abstract ToolType getRequiredTool();

    /**
     * Get the appropriate harvesting result for a mine block result.
     *
     * @param result the mine block result.
     * @return the harvesting result.
     */
    protected final PlanterAIModuleResult getHarvestingResultFromMiningResult(PlanterMineBlockResult result)
    {
        return switch (result)
                 {
                     case NO_TOOL -> PlanterAIModuleResult.REQUIRES_ITEMS;
                     case MINING -> PlanterAIModuleResult.HARVESTING;
                     case MINED -> PlanterAIModuleResult.HARVESTED;
                 };
    }

    /**
     * Get the appropriate clearing result for a mine block result.
     *
     * @param result the mine block result.
     * @return the clearing result.
     */
    protected final PlanterAIModuleResult getClearingResultFromMiningResult(PlanterMineBlockResult result)
    {
        return switch (result)
                 {
                     case NO_TOOL -> PlanterAIModuleResult.REQUIRES_ITEMS;
                     case MINING -> PlanterAIModuleResult.CLEARING;
                     case MINED -> PlanterAIModuleResult.CLEARED;
                 };
    }

    /**
     * Returns the list of valid working positions.
     *
     * @param world            the world all of these positions appear in.
     * @param workingPositions the original input working positions.
     * @return the new list.
     */
    public List<BlockPos> getValidWorkingPositions(final @NotNull Level world, final List<BlockPos> workingPositions)
    {
        return workingPositions.stream().distinct().limit(getMaxPlants()).collect(Collectors.toList());
    }

    /**
     * Get the maximum amount of plants this module is allowed to handle.
     * Defaults to {@link PlantationModule#DEFAULT_MAX_PLANTS}.
     *
     * @return the maximum amount of plants.
     */
    public int getMaxPlants()
    {
        return DEFAULT_MAX_PLANTS;
    }

    /**
     * Enum containing possible states obtained from a mining result.
     */
    public enum PlanterAIModuleState
    {
        /**
         * Something is wrong in the planter AI module, request to reset the AI back to decision state.
         */
        INVALID,
        /**
         * The planter had to do nothing on this position.
         */
        NONE,
        /**
         * The planter is moving to it's working position.
         */
        MOVING,
        /**
         * The planter requires certain items in order to continue operating.
         */
        REQUIRES_ITEMS,
        /**
         * The planter is harvesting a plant.
         */
        HARVESTING,
        /**
         * The planter has harvested a plant.
         */
        HARVESTED,
        /**
         * The planter is planting a plant.
         */
        PLANTING,
        /**
         * The planter has planted a plant.
         */
        PLANTED,
        /**
         * The planter is clearing a working position.
         */
        CLEARING,
        /**
         * The planter has cleared a block working position.
         */
        CLEARED
    }

    /**
     * Enum containing possible states obtained from a mining result.
     */
    public enum PlanterMineBlockResult
    {
        /**
         * The planter doesn't have the tool to mine the defined block.
         */
        NO_TOOL,
        /**
         * The planter is busy mining the block.
         */
        MINING,
        /**
         * The planter has fully mined the block.
         */
        MINED
    }

    /**
     * Class containing possible states that the planter AI can be in.
     */
    public static class PlanterAIModuleResult
    {
        /**
         * Something is wrong in the planter AI module, request to reset the AI back to decision state.
         */
        public static final PlanterAIModuleResult INVALID = new PlanterAIModuleResult(PlanterAIModuleState.INVALID, AIWorkerState.PREPARING, true, true);

        /**
         * The planter had to do nothing on this position.
         */
        public static final PlanterAIModuleResult NONE = new PlanterAIModuleResult(PlanterAIModuleState.NONE, AIWorkerState.PREPARING, true, true);

        /**
         * The planter is moving to it's working position.
         */
        public static final PlanterAIModuleResult MOVING = new PlanterAIModuleResult(PlanterAIModuleState.MOVING, AIWorkerState.PLANTATION_WORK_FIELD, false, false);

        /**
         * The planter requires certain items in order to continue operating.
         */
        public static final PlanterAIModuleResult REQUIRES_ITEMS =
          new PlanterAIModuleResult(PlanterAIModuleState.REQUIRES_ITEMS, AIWorkerState.GATHERING_REQUIRED_MATERIALS, false, false);

        /**
         * The planter is harvesting a plant.
         */
        public static final PlanterAIModuleResult HARVESTING = new PlanterAIModuleResult(PlanterAIModuleState.HARVESTING, AIWorkerState.PLANTATION_WORK_FIELD, false, false);

        /**
         * The planter has harvested a plant.
         */
        public static final PlanterAIModuleResult HARVESTED = new PlanterAIModuleResult(PlanterAIModuleState.HARVESTED, AIWorkerState.PREPARING, true, false);

        /**
         * The planter is planting a plant.
         */
        public static final PlanterAIModuleResult PLANTING = new PlanterAIModuleResult(PlanterAIModuleState.PLANTING, AIWorkerState.PLANTATION_WORK_FIELD, false, false);

        /**
         * The planter has planted a plant.
         */
        public static final PlanterAIModuleResult PLANTED = new PlanterAIModuleResult(PlanterAIModuleState.PLANTED, AIWorkerState.PREPARING, true, false);

        /**
         * The planter is clearing a working position.
         */
        public static final PlanterAIModuleResult CLEARING = new PlanterAIModuleResult(PlanterAIModuleState.CLEARING, AIWorkerState.PLANTATION_WORK_FIELD, false, false);

        /**
         * The planter has cleared a block working position.
         */
        public static final PlanterAIModuleResult CLEARED = new PlanterAIModuleResult(PlanterAIModuleState.CLEARED, AIWorkerState.PREPARING, true, false);

        /**
         * The state to transition to.
         */
        private final PlanterAIModuleState moduleState;

        /**
         * The state to transition to.
         */
        private final AIWorkerState nextState;

        /**
         * Whether to reset the current working position.
         */
        private final boolean resetWorkingPosition;

        /**
         * Whether to reset the current field.
         */
        private final boolean resetCurrentField;

        /**
         * Default constructor.
         *
         * @param moduleState          the module state.
         * @param nextState            the state to transition to.
         * @param resetWorkingPosition whether to reset the current working position.
         * @param resetCurrentField    whether to reset the current field.
         */
        public PlanterAIModuleResult(final PlanterAIModuleState moduleState, final AIWorkerState nextState, final boolean resetWorkingPosition, final boolean resetCurrentField)
        {
            this.moduleState = moduleState;
            this.nextState = nextState;
            this.resetWorkingPosition = resetWorkingPosition;
            this.resetCurrentField = resetCurrentField;
        }

        /**
         * Get the module state.
         *
         * @return the module state.
         */
        public PlanterAIModuleState getModuleState()
        {
            return moduleState;
        }

        /**
         * Get the state to transition to.
         *
         * @return the new state.
         */
        public AIWorkerState getNextState()
        {
            return nextState;
        }

        /**
         * Whether to reset the current working position.
         *
         * @return true if so.
         */
        public boolean shouldResetWorkingPosition()
        {
            return resetWorkingPosition;
        }

        /**
         * Whether to reset the current field.
         *
         * @return true if so.
         */
        public boolean shouldResetCurrentField()
        {
            return resetCurrentField;
        }
    }
}