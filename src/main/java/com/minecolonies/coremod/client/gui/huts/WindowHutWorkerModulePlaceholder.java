package com.minecolonies.coremod.client.gui.huts;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractWindowWorkerModuleBuilding;
import org.jetbrains.annotations.NotNull;

/**
 * Window for worker. Placeholder for many different jobs.
 *
 * @param <B> Object extending {@link com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker.View}.
 */
public class WindowHutWorkerModulePlaceholder<B extends com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker.View> extends AbstractWindowWorkerModuleBuilding<B>
{
    private static final String WORKER_PLACEHOLDER_RESOURCE_SUFFIX = ":gui/windowhutworkerplaceholder.xml";
    private final        String name;

    /**
     * Window for worker placeholder. Used by buildings not listed above this file.
     *
     * @param building AbstractBuilding extending {@link com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker.View}.
     * @param name     Name of the the view (resource).
     */
    public WindowHutWorkerModulePlaceholder(final B building, final String name)
    {
        super(building, Constants.MOD_ID + WORKER_PLACEHOLDER_RESOURCE_SUFFIX);
        this.name = name;
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts." + name;
    }
}
