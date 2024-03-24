package com.minecolonies.core.colony.jobs.views;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.jobs.IJobView;
import com.minecolonies.api.colony.jobs.registry.IJobRegistry;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.network.FriendlyByteBuf;

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
     * The synched job registry entry.
     */
    private JobEntry entry;

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
    public void deserialize(final FriendlyByteBuf buffer)
    {
        this.asyncRequests.clear();
        final int size = buffer.readInt();
        for (int i = 0; i < size; i++)
        {
            asyncRequests.add(StandardFactoryController.getInstance().deserialize(buffer));
        }
        entry = buffer.readById(IJobRegistry.getInstance());
    }

    @Override
    public String getName()
    {
        return this.entry.getTranslationKey();
    }

    @Override
    public JobEntry getEntry()
    {
        return entry;
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

    @Override
    public void setEntry(final JobEntry entry)
    {
        this.entry = entry;
    }
}
