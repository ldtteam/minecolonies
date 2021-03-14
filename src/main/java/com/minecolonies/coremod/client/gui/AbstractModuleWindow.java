package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonHandler;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.Image;
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
            final ButtonImage image = new ButtonImage();
            image.setImage("minecolonies:textures/gui/modules/tab_side" + (random.nextInt(3) + 1) + ".png");
            image.setPosition(-20, 10 + offset);
            image.setSize(32, 26);
            image.setHandler(button -> building.getWindow().open());

            final ButtonImage iconImage = new ButtonImage();
            iconImage.setImage("minecolonies:textures/gui/modules/main.png");
            iconImage.setID("main");
            iconImage.setPosition(-15, 13 + offset);
            iconImage.setSize(20, 20);
            iconImage.setHandler(button -> building.getWindow().open());

            offset += image.getHeight() + 2;

            this.addChild(image);
            this.addChild(iconImage);
        }

        for (IBuildingModuleView view : building.getAllModuleViews())
        {
            final ButtonImage image = new ButtonImage();
            image.setImage("minecolonies:textures/gui/modules/tab_side" + (random.nextInt(3) + 1) + ".png");
            image.setPosition(-20, 10 + offset);
            image.setSize(32, 26);
            image.setHandler(button -> view.getWindow().open());

            final String icon = view.getIcon();
            final ButtonImage iconImage = new ButtonImage();
            iconImage.setImage("minecolonies:textures/gui/modules/" + icon + ".png");
            iconImage.setSize(20, 20);
            iconImage.setID(icon);
            iconImage.setPosition(-15, 13 + offset);
            iconImage.setHandler(button -> view.getWindow().open());

            offset += image.getHeight() + 2;

            this.addChild(image);
            this.addChild(iconImage);
        }
    }
}
