package com.minecolonies.core.colony.buildings.workerbuildings.plantation;

import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.plantation.IPlantationModule;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.core.colony.fields.PlantationField;
import com.minecolonies.core.colony.fields.modules.AbstractFieldModule;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for planter modules that determines how the AI should work specific fields.
 */
public abstract class AbstractPlantationModule extends AbstractFieldModule implements IPlantationModule
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
     * @param field    the field instance this module is working on.
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    protected AbstractPlantationModule(
      final IField field, final String fieldTag, final String workTag, final Item item)
    {
        super(field);
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
    public List<BlockPos> getValidWorkingPositions(final @NotNull Level world, final List<BlockPos> workingPositions)
    {
        List<BlockPos> result = new ArrayList<>();
        int maxWorkingPositions = getMaxWorkingPositions();
        for (int i = 0; i < maxWorkingPositions; i++)
        {
            if (workingPositions.size() == i)
            {
                break;
            }
            result.add(workingPositions.get(i));
        }
        return result;
    }

    /**
     * Get the maximum amount of working positions this field is allowed to handle.
     * Defaults to {@link AbstractPlantationModule#DEFAULT_MAX_PLANTS}.
     *
     * @return the maximum amount of plants.
     */
    protected int getMaxWorkingPositions()
    {
        return DEFAULT_MAX_PLANTS;
    }

    @Override
    public List<Item> getValidBonemeal()
    {
        return List.of();
    }

    @Override
    public BlockPos getPositionToWalkTo(final Level world, final BlockPos workingPosition)
    {
        return workingPosition;
    }

    @Override
    public BlockState getPlantingBlockState(final Level world, final BlockPos workPosition, final BlockState blockState)
    {
        return blockState;
    }

    @Override
    public void applyBonemeal(AbstractEntityCitizen worker, BlockPos workPosition, ItemStack stackInSlot, Player fakePlayer)
    {
        BoneMealItem.applyBonemeal(stackInSlot, worker.level(), workPosition, fakePlayer);
        BoneMealItem.addGrowthParticles(worker.level(), workPosition, 1);
    }

    /**
     * Get the working positions on the field.
     *
     * @return a list of working positions.
     */
    protected final List<BlockPos> getWorkingPositions()
    {
        if (field instanceof PlantationField plantationField)
        {
            return plantationField.getWorkingPositions();
        }
        return new ArrayList<>();
    }

    @Override
    public int hashCode()
    {
        return fieldTag.hashCode();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final AbstractPlantationModule that = (AbstractPlantationModule) o;

        return fieldTag.equals(that.fieldTag);
    }
}