package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation;

import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.plantation.BasicPlanterAI;
import com.minecolonies.api.colony.fields.plantation.IPlantationModule;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.colony.fields.PlantationField;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base class for planter modules that determines how the AI should work specific fields.
 */
public abstract class AbstractPlantationModule implements IPlantationModule
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
    protected AbstractPlantationModule(
      final String fieldTag,
      final String workTag,
      final Item item)
    {
        this.fieldTag = fieldTag;
        this.workTag = workTag;
        this.item = item;
    }

    @Override
    public final String getFieldTag()
    {
        return fieldTag;
    }

    @Override
    public final String getWorkTag()
    {
        return workTag;
    }

    @Override
    public final Item getItem()
    {
        return item;
    }

    @Override
    public int getPlantsToRequest()
    {
        return (int) Math.ceil(new ItemStack(item).getMaxStackSize() / 4d);
    }

    @Override
    public ResourceLocation getRequiredResearchEffect()
    {
        return null;
    }

    @Override
    public abstract PlanterAIModuleResult workField(
      @NotNull IField field,
      @NotNull BasicPlanterAI planterAI,
      @NotNull AbstractEntityCitizen worker,
      @NotNull BlockPos workPosition,
      @NotNull FakePlayer fakePlayer);

    @Override
    public List<BlockPos> getValidWorkingPositions(final @NotNull Level world, final List<BlockPos> workingPositions)
    {
        return workingPositions.stream().distinct().limit(getMaxPlants()).collect(Collectors.toList());
    }

    @Override
    public boolean needsWork(IField field)
    {
        return getNextWorkingPosition(field) != null;
    }

    /**
     * Get the maximum amount of plants this module is allowed to handle.
     * Defaults to {@link AbstractPlantationModule#DEFAULT_MAX_PLANTS}.
     *
     * @return the maximum amount of plants.
     */
    public int getMaxPlants()
    {
        return DEFAULT_MAX_PLANTS;
    }

    /**
     * Get the working positions on the field.
     *
     * @param field the field instance.
     * @return a list of working positions.
     */
    protected final List<BlockPos> getWorkingPositions(IField field)
    {
        if (field instanceof PlantationField plantationField)
        {
            return plantationField.getWorkingPositions();
        }
        return new ArrayList<>();
    }

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
}