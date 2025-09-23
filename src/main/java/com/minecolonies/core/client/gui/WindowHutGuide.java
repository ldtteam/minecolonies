package com.minecolonies.core.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.huts.WindowHutBuilderModule;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBuilder;
import net.minecraft.resources.ResourceLocation;

import static com.minecolonies.api.util.constant.WindowConstants.GUIDE_CLOSE;
import static com.minecolonies.api.util.constant.WindowConstants.GUIDE_CONFIRM;

/**
 * BOWindow for the builder hut.
 */
public class WindowHutGuide extends AbstractWindowSkeleton
{
    public static final ResourceLocation WINDOW_ID = new ResourceLocation(Constants.MOD_ID, "gui/windowhutguide.xml");

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
        super(WINDOW_ID);
        this.building = building;

        registerButton(GUIDE_CONFIRM, this::closeGuide);
        registerButton(GUIDE_CLOSE, this::closeGuide);
    }

    private void closeGuide()
    {
        close();
        new WindowHutBuilderModule(building, false).open();
    }
}
