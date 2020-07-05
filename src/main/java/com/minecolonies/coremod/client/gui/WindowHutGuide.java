package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Text;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the builder hut.
 */
public class WindowHutGuide extends AbstractWindowSkeleton
{
    /**
     * Color constants for builder list.
     */
    private final BuildingBuilder.View building;

    /**
     * Constructor for window builder hut.
     *
     * @param building {@link BuildingBuilder.View}.
     */
    public WindowHutGuide(final BuildingBuilder.View building)
    {
        super(Constants.MOD_ID + GUIDE_RESOURCE_SUFFIX);
        registerButton(GUIDE_CONFIRM, this::closeGuide);
        registerButton(GUIDE_CLOSE, this::closeGuide);

        this.building = building;
        findPaneOfTypeByID("questionmark", Text.class).setTextContent(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.guide.questionmark"));
        findPaneOfTypeByID("arrow", Text.class).setTextContent(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.guide.arrow"));
        findPaneOfTypeByID("chest", Text.class).setTextContent(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.guide.chest"));
    }

    private void closeGuide()
    {
        close();
        new WindowHutBuilder(building, false).open();
    }
}
