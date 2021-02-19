package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.modules.HomeBuildingModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWonder;
import com.minecolonies.coremod.network.messages.server.colony.building.RecallCitizenHutMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.home.AssignUnassignMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the home building.
 */
public class WindowHutWonder extends AbstractWindowBuilding<BuildingWonder.View>
{
    /**
     * Suffix describing the window xml.
     */
    private static final String WONDER_BUILDING_RESOURCE_SUFFIX = ":gui/windowhutwonder.xml";

    /**
     * The building the view is relates to.
     */
    private final BuildingWonder.View wonder;

    /**
     * Creates the Window object.
     *
     * @param building View of the home building.
     */
    public WindowHutWonder(final BuildingWonder.View building)
    {
        super(building, Constants.MOD_ID + WONDER_BUILDING_RESOURCE_SUFFIX);
        this.wonder = building;
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        refreshView();
    }

    /**
     * Refresh the view.
     */
    private void refreshView()
    {
    }
    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.wonderhut";
    }
}
