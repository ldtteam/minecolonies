package com.minecolonies.core.colony.workorders;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBuilder;
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
      final int rotation,
      final boolean mirror,
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
          rotation,
          mirror,
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
      String packName, String path, final String translationKey, WorkOrderType workOrderType, BlockPos location, int rotation, boolean isMirrored, int currentLevel,
      int targetLevel)
    {
        super(packName, path, translationKey, workOrderType, location, rotation, isMirrored, currentLevel, targetLevel);
    }

    /**
     * Checks if a builder may accept this workOrder.
     *
     * @param building which could build it or not
     * @return true if he is able to.
     */
    @SuppressWarnings(UNUSED_METHOD_PARAMETERS_SHOULD_BE_REMOVED)
    public boolean canBuild(final IBuilding building)
    {
        return building instanceof BuildingBuilder && building.getBuildingLevel() > 0;
    }

    /**
     * Checks if a builder may accept this workOrder while ignoring the distance to the builder.
     * <p>
     * @param position position of the builders own hut.
     * @param level    level of the builders hut.
     * @return true if so.
     */
    @SuppressWarnings(UNUSED_METHOD_PARAMETERS_SHOULD_BE_REMOVED)
    public boolean canBuildIgnoringDistance(@NotNull IBuilding building, final BlockPos position, final int level)
    {
        return level > 0;
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
