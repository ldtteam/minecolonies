package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation;

import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.PlantationField;
import com.minecolonies.coremod.entity.ai.citizen.planter.EntityAIWorkPlanter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for planter modules that determines how the AI should work specific fields.
 */
public abstract class PlantationModule
{
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
    private final Block block;

    /**
     * The maximum amount of plants allowed on this module type.
     */
    private final int maxPlants;

    /**
     * The amount of plants to request when the planter has run out.
     */
    private final int plantsToRequest;

    /**
     * A research effect which has to be researched first before this module can be used.
     */
    @Nullable
    private final ResourceLocation requiredResearchEffect;

    /**
     * Default constructor.
     *
     * @param fieldTag               the tag of the field anchor block.
     * @param workTag                the tag of the working positions.
     * @param block                  the block which is harvested.
     * @param maxPlants              the maximum allowed plants.
     * @param plantsToRequest        the amount of plants to request when the planter has none left.
     * @param requiredResearchEffect the research effect required before this field type can be used.
     */
    protected PlantationModule(
      final String fieldTag,
      final String workTag,
      final Block block,
      final int maxPlants,
      final int plantsToRequest,
      final @Nullable ResourceLocation requiredResearchEffect)
    {
        this.fieldTag = fieldTag;
        this.workTag = workTag;
        this.block = block;
        this.maxPlants = maxPlants;
        this.plantsToRequest = plantsToRequest;
        this.requiredResearchEffect = requiredResearchEffect;
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
     * Get the block the module uses.
     *
     * @return the block.
     */
    public final Block getBlock()
    {
        return block;
    }

    /**
     * Get the item the module uses.
     * (This is obtained through the block using {@link Block#asItem()})
     *
     * @return the item.
     */
    public final Item getItem()
    {
        return block.asItem();
    }

    /**
     * Get the maximum amount of plants this module is allowed to handle.
     *
     * @return the maximum amount of plants.
     */
    public final int getMaxPlants()
    {
        return maxPlants;
    }

    /**
     * Get the amount of plants to request when the planter no longer has any left.
     *
     * @return the amount of plants to request.
     */
    public final int getPlantsToRequest()
    {
        return plantsToRequest;
    }

    /**
     * Get the required research effect for this module.
     *
     * @return the key of where to find the effect.
     */
    public final ResourceLocation getRequiredResearchEffect()
    {
        return requiredResearchEffect;
    }

    /**
     * Core function for the planter module, is responsible for telling the AI what to do on the specific field.
     *
     * @param field               the field reference to fetch data from.
     * @param entityAIWorkPlanter the AI class of the planter so instructions can be ordered to it.
     * @param workPosition        the position that has been chosen for work.
     * @return a basic enum state telling the planter AI what the AI should be doing next.
     */
    @NotNull
    public abstract PlantationModule.PlanterAIModuleResult workField(PlantationField field, EntityAIWorkPlanter entityAIWorkPlanter, BlockPos workPosition);

    /**
     * Determines if there's any work left to do on this field. If so, where.
     *
     * @param field the field reference to fetch data from.
     * @return the position inside the field that needs work or null.
     */
    @Nullable
    public abstract BlockPos getNextWorkingPosition(PlantationField field);

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
     * Enum containing possible states that the planter AI can be in.
     */
    public enum PlanterAIModuleResult
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
     * Builder class for the plantation modules.
     *
     * @param <T>
     */
    public abstract static class Builder<T extends Builder<T>>
    {
        /**
         * The tag that the field anchor block contains in order to select which of these modules to use.
         */
        protected final String fieldTag;

        /**
         * The tag that the individual working positions must contain.
         */
        protected final String workTag;

        /**
         * The block which is harvested in this module.
         */
        protected final Block block;

        /**
         * The maximum amount of plants allowed on this module type.
         * Defaults to 20.
         */
        protected int maxPlants;

        /**
         * The amount of plants to request when the planter has run out.
         * Defaults to quarter of the stack size of the {@link Builder#block}.
         */
        protected int plantsToRequest;

        /**
         * A research effect which has to be researched first before this module can be used.
         */
        @Nullable
        protected ResourceLocation requiredResearchEffect;

        /**
         * Default constructor.
         *
         * @param fieldTag the tag of the field anchor block.
         * @param workTag  the tag of the working positions.
         * @param block    the block which is harvested.
         */
        protected Builder(String fieldTag, String workTag, Block block)
        {
            this.fieldTag = fieldTag;
            this.workTag = workTag;
            this.block = block;
            this.plantsToRequest = (int) Math.ceil(new ItemStack(block.asItem()).getMaxStackSize() / 4d);
            this.maxPlants = 20;
        }

        /**
         * Create the plantation module instance.
         *
         * @return the plantation module instance.
         */
        public abstract PlantationModule build();

        /**
         * Sets a required research effect that needs to be met before this field can be used.
         *
         * @param requiredResearchEffect the resource location to the research effect.
         * @return the builder instance.
         */
        public T withRequiredResearchEffect(ResourceLocation requiredResearchEffect)
        {
            this.requiredResearchEffect = requiredResearchEffect;
            return self();
        }

        @SuppressWarnings("unchecked")
        private T self()
        {
            return (T) this;
        }

        /**
         * Sets the amount of plants to request whenever the planter runs out of the block.
         * Defaults to quarter of the stack size of the {@link Builder#block}.
         *
         * @param plantsToRequest the amount of plants to request.
         * @return the builder instance.
         */
        public T withPlantsToRequest(int plantsToRequest)
        {
            this.plantsToRequest = plantsToRequest;
            return self();
        }

        /**
         * Sets the maximum amount of allowed plants on this field.
         * Defaults to 20.
         *
         * @param maxPlants the maximum amount of plants.
         * @return the builder instance.
         */
        public T withMaxPlants(int maxPlants)
        {
            this.maxPlants = maxPlants;
            return self();
        }
    }
}
