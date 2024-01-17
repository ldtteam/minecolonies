package com.minecolonies.core.colony.workorders;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.colony.jobs.JobBuilder;
import com.minecolonies.core.entity.ai.citizen.builder.ConstructionTapeHelper;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.MESSAGE_NEW_DECORATION_REQUEST;

/**
 * A work order that the build can take to build plantation fields.
 */
public class WorkOrderPlantationField extends AbstractWorkOrder
{
    /**
     * Unused constructor for reflection.
     */
    public WorkOrderPlantationField()
    {
        super();
    }

    private WorkOrderPlantationField(
      String packName, String path, final String translationKey, WorkOrderType workOrderType, BlockPos location, int rotation, boolean isMirrored, int currentLevel,
      int targetLevel)
    {
        super(packName, path, translationKey, workOrderType, location, rotation, isMirrored, currentLevel, targetLevel);
    }

    public static WorkOrderPlantationField create(
      @NotNull final WorkOrderType type,
      final String packName,
      final String path,
      final String translationKey,
      final BlockPos location,
      final int rotation,
      final boolean mirror,
      final int currentLevel)
    {
        int targetLevel = 1;
        if (type == WorkOrderType.REMOVE)
        {
            targetLevel = 0;
        }

        return new WorkOrderPlantationField(
          packName,
          path,
          translationKey,
          type,
          location,
          rotation,
          mirror,
          currentLevel,
          targetLevel);
    }

    @Override
    public boolean canBeMadeBy(final IJob<?> job)
    {
        return job instanceof JobBuilder;
    }

    @Override
    public boolean canBuild(final @NotNull ICitizenData citizen)
    {
        return true;
    }

    @Override
    public boolean isValid(final IColony colony)
    {
        return super.isValid(colony) && this.getStructurePath() != null && !this.getStructurePath().isEmpty();
    }

    @Override
    public void onAdded(final IColony colony, final boolean readingFromNbt)
    {
        super.onAdded(colony, readingFromNbt);
        if (!readingFromNbt && colony != null && colony.getWorld() != null)
        {
            ConstructionTapeHelper.placeConstructionTape(this, colony.getWorld(), colony);
            MessageUtils.format(MESSAGE_NEW_DECORATION_REQUEST, colony.getName()).sendTo(colony).forManagers();
        }
    }

    @Override
    public void onRemoved(final IColony colony)
    {
        super.onRemoved(colony);
        ConstructionTapeHelper.removeConstructionTape(this, colony.getWorld());
    }
}
