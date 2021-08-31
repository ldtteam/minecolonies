package com.minecolonies.coremod.client.gui.huts;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractWindowWorkerModuleBuilding;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingWorkerView;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * BOWindow for worker. Placeholder for many different jobs.
 *
 * @param <B> Object extending {@link AbstractBuildingWorkerView}.
 */
public class WindowHutWorkerModulePlaceholder<B extends AbstractBuildingWorkerView> extends AbstractWindowWorkerModuleBuilding<B>
{
    private static final String WORKER_PLACEHOLDER_RESOURCE_SUFFIX = ":gui/windowhutworkerplaceholder.xml";
    private final        String name;

    /**
     * BOWindow for worker placeholder. Used by buildings not listed above this file.
     *
     * @param building AbstractBuilding extending {@link AbstractBuildingWorkerView}.
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
        return "com.minecolonies.coremod.gui.workerhuts." + name;
    }
}
