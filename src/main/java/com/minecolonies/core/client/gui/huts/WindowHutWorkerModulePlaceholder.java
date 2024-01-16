package com.minecolonies.core.client.gui.huts;

import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.AbstractWindowWorkerModuleBuilding;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * BOWindow for worker. Placeholder for many different jobs.
 *
 * @param <B> Object extending {@link AbstractBuildingView}.
 */
public class WindowHutWorkerModulePlaceholder<B extends IBuildingView> extends AbstractWindowWorkerModuleBuilding<B>
{
    private static final String WORKER_PLACEHOLDER_RESOURCE_SUFFIX = ":gui/windowhutworkerplaceholder.xml";
    private final        String name;

    /**
     * BOWindow for worker placeholder. Used by buildings not listed above this file.
     *
     * @param building AbstractBuilding extending {@link AbstractBuildingView}.
     * @param name     Name of the the view (resource).
     */
    public WindowHutWorkerModulePlaceholder(final B building, final String name)
    {
        super(building, Constants.MOD_ID + WORKER_PLACEHOLDER_RESOURCE_SUFFIX);
        this.name = name.toLowerCase(Locale.ROOT);
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return name.isEmpty() ? super.getBuildingName() : name;
    }
}
