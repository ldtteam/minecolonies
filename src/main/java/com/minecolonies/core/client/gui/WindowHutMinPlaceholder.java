package com.minecolonies.core.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;

/**
 * BOWindow for worker. Placeholder for many different jobs.
 *
 * @param <B> Object extending {@link AbstractBuildingView}.
 */
public class WindowHutMinPlaceholder<B extends AbstractBuildingView> extends AbstractWindowModuleBuilding<B>
{
    private static final String WORKER_PLACEHOLDER_RESOURCE_SUFFIX = ":gui/layouthuts/layouthutpageactionsmin.xml";

    /**
     * BOWindow for worker placeholder. Used by buildings not listed above this file.
     *
     * @param building AbstractBuilding extending {@link AbstractBuildingView}.
     */
    public WindowHutMinPlaceholder(final B building)
    {
        super(building, Constants.MOD_ID + WORKER_PLACEHOLDER_RESOURCE_SUFFIX);
    }
}
