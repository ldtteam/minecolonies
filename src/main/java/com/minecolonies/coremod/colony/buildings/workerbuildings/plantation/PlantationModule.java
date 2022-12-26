package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation;

import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.PlantationField;
import com.minecolonies.coremod.entity.ai.citizen.planter.EntityAIWorkPlanter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for planter modules that determines how the AI should work specific fields.
 */
public abstract class PlantationModule
{

    private final String fieldTag;
    private final String workTag;
    private final Block  block;
    private final int    maxPlants;
    private final int    plantsToRequest;

    protected PlantationModule(final String fieldTag, final String workTag, final Block block, final int maxPlants, final int plantsToRequest)
    {
        this.fieldTag = fieldTag;
        this.workTag = workTag;
        this.block = block;
        this.maxPlants = maxPlants;
        this.plantsToRequest = plantsToRequest;
    }

    /**
     * Get the field tag property.
     *
     * @return the field tag.
     */
    public String getFieldTag()
    {
        return fieldTag;
    }

    /**
     * Get the work tag property.
     *
     * @return the work tag.
     */
    public String getWorkTag()
    {
        return workTag;
    }

    /**
     * Get the block the module uses.
     *
     * @return the block.
     */
    public Block getBlock()
    {
        return block;
    }

    /**
     * Get the item the module uses.
     * (This is obtained through the block using {@link Block#asItem()})
     *
     * @return the item.
     */
    public Item getItem()
    {
        return block.asItem();
    }

    /**
     * Get the maximum amount of plants this module is allowed to handle.
     *
     * @return the maximum amount of plants.
     */
    public int getMaxPlants()
    {
        return maxPlants;
    }

    /**
     * Get the amount of plants to request when the planter no longer has any left.
     *
     * @return the amount of plants to request.
     */
    public int getPlantsToRequest()
    {
        return plantsToRequest;
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

    public enum PlanterMineBlockResult
    {
        NO_TOOL,
        MINING,
        MINED
    }
}
