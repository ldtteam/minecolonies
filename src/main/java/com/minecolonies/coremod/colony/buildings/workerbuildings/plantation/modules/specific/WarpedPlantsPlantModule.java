package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.BoneMealedPlantModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_NETHER;

/**
 * Planter module for growing {@link Items#WARPED_FUNGUS} and {@link Items#WARPED_ROOTS}.
 * <br/>
 * Requirements:
 * <ol>
 *     <li>All requirements from {@link BoneMealedPlantModule}</li>
 * </ol>
 */
public class WarpedPlantsPlantModule extends BoneMealedPlantModule
{
    /**
     * The chance a worker has to work on this field.
     */
    private static final int CHANCE = 5;

    /**
     * The maximum amount of plants allowed on this field.
     */
    private static final int MAX_PLANTS = 50;

    /**
     * Default constructor.
     *
     * @param field    the field instance this module is working on.
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    public WarpedPlantsPlantModule(final IField field, final String fieldTag, final String workTag, final Item item)
    {
        super(field, fieldTag, workTag, item);
    }

    @Override
    protected int getPercentageChance()
    {
        return CHANCE;
    }

    @Override
    public ResourceLocation getRequiredResearchEffect()
    {
        return PLANTATION_NETHER;
    }

    @Override
    protected int getMaxPlants()
    {
        return MAX_PLANTS;
    }

    @Override
    public ToolType getRequiredTool()
    {
        return ToolType.NONE;
    }
}