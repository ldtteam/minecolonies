package com.minecolonies.core.colony.workorders;

import com.ldtteam.structurize.api.RotationMirror;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.colony.jobs.JobBuilder;
import com.minecolonies.core.entity.ai.workers.util.ConstructionTapeHelper;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Suppression.UNUSED_METHOD_PARAMETERS_SHOULD_BE_REMOVED;
import static com.minecolonies.api.util.constant.TranslationConstants.MESSAGE_NEW_DECORATION_REQUEST;

/**
 * A work order that the build can take to build decorations.
 */
public class WorkOrderDecoration extends AbstractWorkOrder
{
    public static WorkOrderDecoration create(
      @NotNull final WorkOrderType type,
      final String packName,
      final String path,
      final String translationKey,
      final BlockPos location,
      final RotationMirror rotMir,
      final int currentLevel)
    {
        int targetLevel = currentLevel;
        switch (type)
        {
            case BUILD:
                targetLevel = 1;
                break;
            case UPGRADE:
                targetLevel++;
                break;
            case REMOVE:
                targetLevel = 0;
                break;
        }

        return new WorkOrderDecoration(
          packName,
          path,
          translationKey,
          type,
          location,
          rotMir,
          currentLevel,
          targetLevel);
    }

    /**
     * Unused constructor for reflection.
     */
    public WorkOrderDecoration()
    {
        super();
    }

    private WorkOrderDecoration(
      String packName, String path, final String translationKey, WorkOrderType workOrderType, BlockPos location, RotationMirror rotMir, int currentLevel,
      int targetLevel)
    {
        super(packName, path, translationKey, workOrderType, location, rotMir, currentLevel, targetLevel);
    }

    /**
     * Checks if a builder may accept this workOrder.
     * <p>
     * Suppressing Sonar Rule squid:S1172 This rule does "Unused method parameters should be removed" But in this case extending class may need to use the citizen parameter
     *
     * @param citizen which could build it or not
     * @return true if he is able to.
     */
    @SuppressWarnings(UNUSED_METHOD_PARAMETERS_SHOULD_BE_REMOVED)
    public boolean canBuild(@NotNull final ICitizenData citizen)
    {
        return true;
    }

    @Override
    public boolean canBeMadeBy(final IJob<?> job)
    {
        return job instanceof JobBuilder;
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
