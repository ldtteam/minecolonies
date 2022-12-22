package com.minecolonies.api.compatibility.dynmap;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.Log;

/**
 * The Dynmap integration proxy, contains all the top level methods to talk to Dynmap.
 * By default, this class does nothing unless the {@link DynmapCompat} class is used to override this class.
 * This will cause {@link DynmapProxy#isDynmapPresent()} to return true.
 */
public class DynmapProxy
{
    private DynmapIntegration integration;

    /**
     * Registers the Dynmap integration.
     */
    public final void registerIntegration()
    {
        if (!isDynmapPresent())
        {
            Log.getLogger().info("Dynmap compat is not active");
            return;
        }
        Log.getLogger().info("Dynmap compat is active");

        new DynmapApiListener(it -> this.integration = it).registerListener();
    }

    /**
     * Default method for when dynmap is not present, returns false
     *
     * @return true if so.
     */
    public boolean isDynmapPresent()
    {
        return false;
    }

    /**
     * Update which triggers generation for a new colony.
     *
     * @param colony The colony to create.
     */
    public void updateColonyCreated(final IColony colony)
    {
        if (isDynmapPresent() && integration != null)
        {
            integration.createColony(colony);
        }
    }

    /**
     * Update which triggers generation for a new colony.
     *
     * @param colony The colony to create.
     */
    public void updateColonyDeleted(final IColony colony)
    {
        if (isDynmapPresent() && integration != null)
        {
            integration.deleteColony(colony);
        }
    }

    /**
     * Update the name for a given colony, only works if Dynmap is present and as soon as the Dynmap compat is active.
     *
     * @param colony The colony to update the name for.
     */
    public void updateColonyName(final IColony colony)
    {
        if (isDynmapPresent() && integration != null)
        {
            integration.updateName(colony);
        }
    }

    /**
     * Update the team color for a given colony, only works if Dynmap is present and as soon as the Dynmap compat is active.
     *
     * @param colony The colony to update the team color for.
     */
    public void updateColonyTeamColor(final IColony colony)
    {
        if (isDynmapPresent() && integration != null)
        {
            integration.updateTeamColor(colony);
        }
    }

    /**
     * Update the borders for a given colony, only works if Dynmap is present and as soon as the Dynmap compat is active.
     *
     * @param colony The colony to update the borders for.
     */
    public void updateColonyBorders(final IColony colony)
    {
        if (isDynmapPresent() && integration != null)
        {
            integration.updateBorders(colony);
        }
    }

    /**
     * Update the citizen count for a given colony, only works if Dynmap is present and as soon as the Dynmap compat is active.
     *
     * @param colony The colony to update the citizen count for.
     */
    public void updateColonyCitizenCount(final IColony colony)
    {
        if (isDynmapPresent() && integration != null)
        {
            integration.updateDescription(colony);
        }
    }
}