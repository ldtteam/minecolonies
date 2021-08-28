package com.minecolonies.coremod.client.gui.huts;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.coremod.client.gui.WindowHireWorkerSchool;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSchool;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

/**
 * Window for the school hut.
 */
public class WindowHutSchoolModule extends WindowHutWorkerModulePlaceholder<BuildingSchool.View>
{
    /**
     * Window for worker placeholder. Used by buildings not listed above this file.
     *
     * @param building AbstractBuilding extending {@link com.minecolonies.coremod.colony.buildings.AbstractBuildingWorkerView}.
     * @param name     Name of the the view (resource).
     */
    public WindowHutSchoolModule(final BuildingSchool.View building, final String name)
    {
        super(building, name);
    }

    @Override
    protected void hireClicked(@NotNull final Button button)
    {
        if (building.getBuildingLevel() == 0)
        {
            LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, "com.minecolonies.coremod.gui.workerhuts.level0");
            return;
        }

        @NotNull final WindowHireWorkerSchool window = new WindowHireWorkerSchool(building.getColony(), building.getPosition());
        window.open();
    }
}
