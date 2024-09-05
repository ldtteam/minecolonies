package com.minecolonies.api.colony.fields.plantation;

import com.minecolonies.api.colony.fields.modules.IFieldModule;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.tools.registry.ToolTypeEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
     * @param world           the world reference that can be used for block state lookups.
     * @param workingPosition the position that has been chosen for work.
     * @return a basic enum state telling the planter AI what the AI should be doing next.
     */
    PlantationModuleResult.Builder decideFieldWork(Level world, @NotNull BlockPos workingPosition);

    /**
     * Obtains the next working position for the given field, if any.
     *
     * @param world the world reference that can be used for block state lookups.
     * @return the next position to work on, or null.
     */
    @Nullable BlockPos getNextWorkingPosition(Level world);

    /**
     * Get a list of actual valid working positions, based on the tags read from the plantation field.
     *
     * @param world            the level the tags were read from.
     * @param workingPositions the list of initially parsed working positions, based on work tag.
     * @return the list of actually valid parsed working positions.
     */
    List<BlockPos> getValidWorkingPositions(final @NotNull Level world, final List<BlockPos> workingPositions);

    /**
     * Returns the maximum amount of actions that can be performed on a field, before the planter must forcefully switch to a new field.
     *
     * @return the maximum of actions the planter can perform on the field per cycle.
     */
    int getActionLimit();

    /**
     * Returns which items are considered valid bonemeal items.
     * Defaults to an empty list. (Most modules should not need any bonemeal).
     */
    List<Item> getValidBonemeal();

    /**
     * Determines where the planter should walk to, in order to most effectively work on the given working position.
     * Defaults to the same block.
     *
     * @param world           the world reference that can be used for block state lookups.
     * @param workingPosition the original working position.
     * @return the position for the planter to walk to.
     */
    BlockPos getPositionToWalkTo(Level world, BlockPos workingPosition);

    /**
     * Generate a block state for a new plant.
     *
     * @param world        the world reference that can be used for block state lookups.
     * @param workPosition the position that has been chosen for work.
     * @param blockState   the default block state for the block.
     */
    BlockState getPlantingBlockState(Level world, BlockPos workPosition, BlockState blockState);

    /**
     * Logic for applying bonemeal to the working position.
     *
     * @param worker       the worker entity working on the plantation.
     * @param workPosition the position that has been chosen for work.
     * @param stackInSlot  the item stack to use for the planting.
     * @param fakePlayer   a fake player class to use.
     */
    void applyBonemeal(final AbstractEntityCitizen worker, final BlockPos workPosition, final ItemStack stackInSlot, final Player fakePlayer);

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
    ToolTypeEntry getRequiredTool();

    /**
     * Hashcode implementation for this field.
     */
    int hashCode();

    /**
     * Equals implementation for this field.
     */
    boolean equals(Object other);

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
     * The different actions this module is able to perform.
     */
    enum ActionToPerform
    {
        NONE(false),
        PLANT(true),
        BONEMEAL(true),
        HARVEST(true),
        CLEAR(false);

        /**
         * Whether this action increases the action counter for the field.
         */
        private final boolean increasesActionCount;

        /**
         * Default constructor.
         */
        ActionToPerform(final boolean increasesActionCount)
        {
            this.increasesActionCount = increasesActionCount;
        }

        /**
         * Whether this action increases the action counter for the field.
         *
         * @return true if so.
         */
        public boolean increasesActionCount()
        {
            return increasesActionCount;
        }
    }

    /**
     * Class containing possible states that the planter AI can be in.
     */
    class PlantationModuleResult
    {
        /**
         * Simplest action, indicating no work was needed on the given position.
         */
        public static final PlantationModuleResult.Builder NONE = new PlantationModuleResult.Builder().pickNewField();

        /**
         * The original plantation module.
         */
        private final IPlantationModule module;

        /**
         * The working position where the planter was told to perform work.
         */
        private final BlockPos workingPosition;

        /**
         * The action for the planter to perform.
         */
        private final ActionToPerform action;

        /**
         * The position to actually perform the action on.
         */
        @Nullable
        private final BlockPos actionPosition;

        /**
         * The reset state of the result.
         */
        private final PlanterAIModuleResultResetState resetState;

        /**
         * Default constructor.
         *
         * @param module          the plantation module.
         * @param workingPosition the working position.
         * @param action          the action to perform on this position.
         * @param actionPosition  the position to actually perform the action on.
         * @param resetState      the reset state.
         */
        private PlantationModuleResult(
          final IPlantationModule module,
          final BlockPos workingPosition,
          final ActionToPerform action,
          final @Nullable BlockPos actionPosition,
          final PlanterAIModuleResultResetState resetState)
        {
            this.module = module;
            this.workingPosition = workingPosition;
            this.action = action;
            this.actionPosition = actionPosition;
            this.resetState = resetState;
        }

        /**
         * Get the plantation module this result was generated from.
         *
         * @return the plantation module.
         */
        public IPlantationModule getModule()
        {
            return module;
        }

        /**
         * Get the working position.
         *
         * @return the working position.
         */
        public BlockPos getWorkingPosition()
        {
            return workingPosition;
        }

        /**
         * What kind of action to perform.
         *
         * @return the action type.
         */
        public ActionToPerform getAction()
        {
            return action;
        }

        /**
         * Get the position to actually perform the action on.
         *
         * @return the position to actually perform the action on.
         */
        public @Nullable BlockPos getActionPosition()
        {
            return actionPosition;
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

        /**
         * Builder class for the {@link PlantationModuleResult}.
         */
        public static class Builder
        {
            /**
             * The action for the planter to perform.
             */
            private ActionToPerform action;

            /**
             * The position to work perform the action on.
             */
            @Nullable
            private BlockPos actionPosition;

            /**
             * The reset state of the result.
             */
            private PlanterAIModuleResultResetState resetState;

            /**
             * Default constructor.
             */
            public Builder()
            {
                action = ActionToPerform.NONE;
                resetState = PlanterAIModuleResultResetState.NONE;
            }

            /**
             * Tell the planter he should go to a new field after this work is finished.
             *
             * @return the builder instance for chaining.
             */
            public Builder pickNewField()
            {
                resetState = PlanterAIModuleResultResetState.FIELD;
                return this;
            }

            /**
             * Tell the planter he should go to a new position on the same field after this work is finished.
             *
             * @return the builder instance for chaining.
             */
            public Builder pickNewPosition()
            {
                resetState = PlanterAIModuleResultResetState.POSITION;
                return this;
            }

            /**
             * Tell the planter he should plant the given item on the position.
             *
             * @param position the position where to plant on.
             * @return the builder instance for chaining.
             */
            public Builder plant(BlockPos position)
            {
                action = ActionToPerform.PLANT;
                actionPosition = position;
                return this;
            }

            /**
             * Tell the planter he should use bonemeal on the given position.
             *
             * @param position the position where to use bonemeal on.
             * @return the builder instance for chaining.
             */
            public Builder bonemeal(BlockPos position)
            {
                action = ActionToPerform.BONEMEAL;
                actionPosition = position;
                return this;
            }

            /**
             * Tell the planter he should harvest an item at the given position.
             *
             * @param position the position where to harvest the plant from.
             * @return the builder instance for chaining.
             */
            public Builder harvest(BlockPos position)
            {
                action = ActionToPerform.HARVEST;
                actionPosition = position;
                return this;
            }

            /**
             * Tell the planter he should clear an obstructed work position.
             *
             * @param position the position to clear.
             * @return the builder instance for chaining.
             */
            public Builder clear(BlockPos position)
            {
                action = ActionToPerform.CLEAR;
                actionPosition = position;
                return this;
            }

            /**
             * Finishes the construction of the result.
             *
             * @return the result instance.
             */
            public PlantationModuleResult build(IPlantationModule module, BlockPos workingPosition)
            {
                return new PlantationModuleResult(module, workingPosition, action, actionPosition, resetState);
            }
        }
    }
}
