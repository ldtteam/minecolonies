package com.minecolonies.coremod.colony.buildings.views;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Provides a view of the builder building class.
 */
public abstract class AbstractBuildingBuilderView extends AbstractBuildingWorkerView
{
    /**
     * The name of the worker at this building.
     */
    private String workerName;

    /**
     * Public constructor of the view, creates an instance of it.
     *
     * @param c the colony.
     * @param l the position.
     */
    public AbstractBuildingBuilderView(final IColonyView c, final BlockPos l)
    {
        super(c, l);
    }

    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
    {
        super.deserialize(buf);
        workerName = buf.readUtf(32767);
    }

    /**
     * Get the name of the worker assigned to this building.
     *
     * @return the name.
     */
    public String getWorkerName()
    {
        return workerName;
    }
}
