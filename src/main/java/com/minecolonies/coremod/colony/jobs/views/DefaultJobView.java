package com.minecolonies.coremod.colony.jobs.views;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.jobs.IJobView;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.network.PacketBuffer;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic job information on the client side, valid for all job types.
 */
public class DefaultJobView implements IJobView
{
    /**
     * A set of tokens that point to requests for which we do not wait.
     */
    private final Set<IToken<?>> asyncRequests = new HashSet<>();

    /**
     * The colony View this belongs to.
     */
    private final IColonyView colonyView;

    /**
     * The synched job name.
     */
    private String name;

    /**
     * Instantiate the default job view.
     * @param iColonyView the colony it belongs to.
     * @param iCitizenDataView the citizen it belongs to.
     */
    public DefaultJobView(final IColonyView iColonyView, final ICitizenDataView iCitizenDataView)
    {
        this.colonyView = iColonyView;
    }

    @Override
    public void deserialize(final PacketBuffer buffer)
    {
        this.asyncRequests.clear();
        this.name = buffer.readUtf(32767);
        final int size = buffer.readInt();
        for (int i = 0; i < size; i++)
        {
            asyncRequests.add(StandardFactoryController.getInstance().deserialize(buffer));
        }
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    /**
     * Get the colony view this job belongs to.
     * @return the view.
     */
    protected IColonyView getColonyView()
    {
        return colonyView;
    }

    /**
     * Get a set of async requests connected to this job.
     *
     * @return a set of ITokens.
     */
    @Override
    public Set<IToken<?>> getAsyncRequests()
    {
        return asyncRequests;
    }
}
