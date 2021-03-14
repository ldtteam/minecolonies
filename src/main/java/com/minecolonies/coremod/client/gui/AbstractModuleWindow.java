package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.ButtonImage;
import com.minecolonies.api.colony.buildings.modules.IBuildingModuleView;
import com.minecolonies.api.colony.buildings.modules.IModuleWindow;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;

import java.util.Random;

/**
 * Window for all the filterable lists.
 */
public abstract class AbstractModuleWindow extends AbstractWindowSkeleton implements IModuleWindow
{
    /**
     * Constructor for the window of the the filterable lists.
     *
     * @param building   {@link AbstractBuildingView}.
     * @param res        the resource String.
     */
    public AbstractModuleWindow(final IBuildingView building, final String res)
    {
        super(res);
        final Random random = new Random();
        int offset = 0;
        if (building.getAllModuleViews().size() > 0)
        {
            final Pane icon = getIcon();
            final ButtonImage image = new ButtonImage();
            image.setImage("minecolonies:textures/gui/modules/tab_side" + random.nextInt(3) + ".png");
            image.setPosition(10, 10 + offset);
            icon.setPosition(10, 10 + offset);
            offset += image.getHeight() + 2;

            this.addChild(image);
            this.addChild(icon);
        }

        for (IBuildingModuleView view : building.getAllModuleViews())
        {
            final Pane icon = view.getWindow().getIcon();
            final ButtonImage image = new ButtonImage();
            image.setImage("minecolonies:textures/gui/modules/tab_side" + random.nextInt(3) + ".png");
            image.setPosition(10, 10 + offset);
            icon.setPosition(10, 10 + offset);
            offset += image.getHeight() + 2;

            this.addChild(image);
            this.addChild(icon);
        }
    }
}
