package com.minecolonies.coremod.colony.workorders;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_LEVEL;
import static com.minecolonies.api.util.constant.Suppression.UNUSED_METHOD_PARAMETERS_SHOULD_BE_REMOVED;

/**
 * A work order that the build can take to build decorations.
 */
public class WorkOrderDecoration extends AbstractWorkOrder
{
    public static WorkOrderDecoration create(@NotNull final WorkOrderType type,
                                             final String structureName,
                                             final String workOrderName,
                                             final int rotation,
                                             final BlockPos location,
                                             final boolean mirror)
    {
        WorkOrderDecoration wo = new WorkOrderDecoration(
                structureName,
                workOrderName,
                type,
                location,
                rotation,
                mirror,
                building.getBuildingLevel(),
                targetLevel);
        return wo;
    }

    private boolean levelUp = false;

    /**
     * Unused constructor for reflection.
     */
    public WorkOrderDecoration()
    {
        super();
    }

    public WorkOrderDecoration(String structureName, String workOrderName, WorkOrderType workOrderType, BlockPos location, int rotation, boolean isMirrored, int currentLevel,
                               int targetLevel, boolean levelUp)
    {
        super(structureName, workOrderName, workOrderType, location, rotation, isMirrored, currentLevel, targetLevel);
        this.levelUp = levelUp;
    }

    /**
     * Make a decoration level up with this.
     */
    public void setLevelUp()
    {
        this.levelUp = true;
    }

    /**
     * Read the WorkOrder data from the CompoundNBT.
     *
     * @param compound NBT Tag compound.
     */
    @Override
    public void read(@NotNull final CompoundNBT compound, final IWorkManager manager)
    {
        super.read(compound, manager);
        levelUp = compound.getBoolean(TAG_LEVEL);
    }

    /**
     * Save the Work Order to an CompoundNBT.
     *
     * @param compound NBT tag compound.
     */
    @Override
    public void write(@NotNull final CompoundNBT compound)
    {
        super.write(compound);
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
    protected boolean canBuild(@NotNull final ICitizenData citizen)
    {
        return true;
    }

    @Override
    public boolean isValid(final IColony colony)
    {
        return super.isValid(colony) && this.getStructureName() != null;
    }

    @Override
    public void onAdded(final IColony colony, final boolean readingFromNbt)
    {
        super.onAdded(colony, readingFromNbt);
        if (!readingFromNbt && colony != null && colony.getWorld() != null)
        {
            ConstructionTapeHelper.placeConstructionTape(this, colony.getWorld());
            LanguageHandler.sendPlayersMessage(
                    colony.getImportantMessageEntityPlayers(),
                    "com.minecolonies.coremod.decoorderadded", colony.getName());
        }
    }


    @Override
    public void onRemoved(final IColony colony)
    {
        super.onRemoved(colony);
        ConstructionTapeHelper.removeConstructionTape(this, colony.getWorld());
    }

}
