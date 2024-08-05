package com.minecolonies.core.colony.buildings.views;

import com.minecolonies.api.colony.IColonyView;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Provides a view of the builder building class.
 */
public abstract class AbstractBuildingBuilderView extends AbstractBuildingView
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
    public void deserialize(@NotNull final RegistryFriendlyByteBuf buf)
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
