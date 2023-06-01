package com.minecolonies.api.colony.fields.plantation;

import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.modules.IFieldModule;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.constant.ToolType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Interface for planter modules that determines how the AI should work specific fields.
 */
public interface IPlantationModule extends IFieldModule
{
    /**
     * Get the field tag property.
     *
     * @return the field tag.
     */
    String getFieldTag();

    /**
     * Get the work tag property.
     *
     * @return the work tag.
     */
    String getWorkTag();

    /**
     * Get the item the module uses.
     *
     * @return the item.
     */
    Item getItem();

    /**
     * Get the amount of plants to request when the planter no longer has any left.
     * Defaults to quarter stack size of the block.
     *
     * @return the amount of plants to request.
     */
    int getPlantsToRequest();

    /**
     * Get the required research effect for this module.
     * Null if there is no research required for this field.
     *
     * @return the key of where to find the effect.
     */
    ResourceLocation getRequiredResearchEffect();

    /**
     * Core function for the planter module, is responsible for telling the AI what to do on the specific field.
     *
     * @param field        the field reference to fetch data from.
     * @param planterAI    the AI class of the planter so basic instructions can be ordered to it.
     * @param worker       the worker entity working on the plantation.
     * @param workPosition the position that has been chosen for work.
     * @param fakePlayer   a fake player class to use.
     * @return a basic enum state telling the planter AI what the AI should be doing next.
     */
    PlanterAIModuleResult workField(
      @NotNull IField field,
      @NotNull BasicPlanterAI planterAI,
      @NotNull AbstractEntityCitizen worker,
      @NotNull BlockPos workPosition,
      @NotNull FakePlayer fakePlayer);

    /**
     * Obtains the next working position for the given field, if any.
     *
     * @param field the field instance.
     * @return the next position to work on, or null.
     */
    @Nullable BlockPos getNextWorkingPosition(IField field);

    /**
     * Get a list of actual valid working positions, based on the tags read from the plantation field.
     *
     * @param world            the level the tags were read from.
     * @param workingPositions the list of initially parsed working positions, based on work tag.
     * @return the list of actually valid parsed working positions.
     */
    List<BlockPos> getValidWorkingPositions(final @NotNull Level world, final List<BlockPos> workingPositions);

    /**
     * Get whether a field needs work or not.
     *
     * @param field the field instance.
     * @return whether the field needs work.
     */
    boolean needsWork(IField field);

    /**
     * Returns the maximum amount of actions that can be performed on a field, before the planter must forcefully switch to a new field.
     *
     * @return the maximum of actions the planter can perform on the field per cycle.
     */
    int getActionLimit();

    /**
     * Returns a list of items that are mandatory for this module to function.
     *
     * @return a list of items.
     */
    List<ItemStack> getRequiredItemsForOperation();

    /**
     * Returns the requested tool type in order to work on this module.
     *
     * @return the tool to work on this module.
     */
    ToolType getRequiredTool();

    /**
     * Hashcode implementation for this field.
     */
    int hashCode();

    /**
     * Equals implementation for this field.
     */
    boolean equals(Object other);

    /**
     * Enum containing possible states obtained from a mining result.
     */
    enum PlanterAIModuleState
    {
        /**
         * Something is wrong in the planter AI module, request to reset the AI back to decision state.
         */
        INVALID(false),
        /**
         * The planter had to do nothing on this position.
         */
        NONE(false),
        /**
         * The planter is moving to it's working position.
         */
        MOVING(false),
        /**
         * The planter requires certain items in order to continue operating.
         */
        REQUIRES_ITEMS(false),
        /**
         * The planter is harvesting a plant.
         */
        HARVESTING(false),
        /**
         * The planter has harvested a plant.
         */
        HARVESTED(true),
        /**
         * The planter is planting a plant.
         */
        PLANTING(false),
        /**
         * The planter has planted a plant.
         */
        PLANTED(true),
        /**
         * The planter is clearing a working position.
         */
        CLEARING(false),
        /**
         * The planter has cleared a block working position.
         */
        CLEARED(true);

        /**
         * Whether the module state represents a performed action.
         */
        private final boolean isAction;

        /**
         * Default constructor
         */
        PlanterAIModuleState(final boolean isAction)
        {
            this.isAction = isAction;
        }

        /**
         * Whether the module state represents a performed action.
         *
         * @return true if so
         */
        public boolean hasPerformedAction()
        {
            return isAction;
        }
    }

    /**
     * Enum containing possible states obtained from a mining result.
     */
    enum PlanterMineBlockResult
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
     * Enum containing the reset state of the result state.
     */
    enum PlanterAIModuleResultResetState
    {
        /**
         * Do not reset the position/field.
         */
        NONE,
        /**
         * Reset the current position.
         */
        POSITION,
        /**
         * Reset the current field.
         */
        FIELD
    }

    /**
     * Class containing possible states that the planter AI can be in.
     */
    class PlanterAIModuleResult
    {
        /**
         * Something is wrong in the planter AI module, request to reset the AI back to decision state.
         */
        public static final PlanterAIModuleResult INVALID = new PlanterAIModuleResult(PlanterAIModuleState.INVALID, PlanterAIModuleResultResetState.FIELD);

        /**
         * The planter had to do nothing on this position.
         */
        public static final PlanterAIModuleResult NONE = new PlanterAIModuleResult(PlanterAIModuleState.NONE, PlanterAIModuleResultResetState.FIELD);

        /**
         * The planter is moving to it's working position.
         */
        public static final PlanterAIModuleResult MOVING = new PlanterAIModuleResult(PlanterAIModuleState.MOVING, PlanterAIModuleResultResetState.NONE);

        /**
         * The planter requires certain items in order to continue operating.
         */
        public static final PlanterAIModuleResult REQUIRES_ITEMS = new PlanterAIModuleResult(PlanterAIModuleState.REQUIRES_ITEMS, PlanterAIModuleResultResetState.NONE);

        /**
         * The planter is harvesting a plant.
         */
        public static final PlanterAIModuleResult HARVESTING = new PlanterAIModuleResult(PlanterAIModuleState.HARVESTING, PlanterAIModuleResultResetState.NONE);

        /**
         * The planter has harvested a plant.
         */
        public static final PlanterAIModuleResult HARVESTED = new PlanterAIModuleResult(PlanterAIModuleState.HARVESTED, PlanterAIModuleResultResetState.POSITION);

        /**
         * The planter is planting a plant.
         */
        public static final PlanterAIModuleResult PLANTING = new PlanterAIModuleResult(PlanterAIModuleState.PLANTING, PlanterAIModuleResultResetState.NONE);

        /**
         * The planter has planted a plant.
         */
        public static final PlanterAIModuleResult PLANTED = new PlanterAIModuleResult(PlanterAIModuleState.PLANTED, PlanterAIModuleResultResetState.POSITION);

        /**
         * The planter is clearing a working position.
         */
        public static final PlanterAIModuleResult CLEARING = new PlanterAIModuleResult(PlanterAIModuleState.CLEARING, PlanterAIModuleResultResetState.NONE);

        /**
         * The planter has cleared a block working position.
         */
        public static final PlanterAIModuleResult CLEARED = new PlanterAIModuleResult(PlanterAIModuleState.CLEARED, PlanterAIModuleResultResetState.POSITION);

        /**
         * The state to transition to.
         */
        private final PlanterAIModuleState moduleState;

        /**
         * The reset state of the result.
         */
        private final PlanterAIModuleResultResetState resetState;

        /**
         * Default constructor.
         *
         * @param moduleState the module state.
         * @param resetState  the reset state.
         */
        public PlanterAIModuleResult(final PlanterAIModuleState moduleState, final PlanterAIModuleResultResetState resetState)
        {
            this.moduleState = moduleState;
            this.resetState = resetState;
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
         * Whether to reset the current working position.
         *
         * @return true if so.
         */
        public boolean shouldResetWorkingPosition()
        {
            return resetState == PlanterAIModuleResultResetState.POSITION || resetState == PlanterAIModuleResultResetState.FIELD;
        }

        /**
         * Whether to reset the current field.
         *
         * @return true if so.
         */
        public boolean shouldResetCurrentField()
        {
            return resetState == PlanterAIModuleResultResetState.FIELD;
        }
    }
}
