package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.controls.*;
import com.minecolonies.api.colony.buildings.modules.IBuildingModuleView;
import com.minecolonies.api.colony.buildings.modules.IModuleWindow;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Locale;
import java.util.Random;

/**
 * Generic module window class. This creates the navigational menu.
 */
public abstract class AbstractModuleWindow extends AbstractWindowSkeleton implements IModuleWindow
{
    /**
     * Building view matching the module.
     */
    protected final IBuildingView buildingView;

    /**
     * Constructor for the window of the the filterable lists.
     *
     * @param building   {@link AbstractBuildingView}.
     * @param res        the resource String.
     */
    public AbstractModuleWindow(final IBuildingView building, final String res)
    {
        super(res);
        this.buildingView = building;
        final Random random = new Random(building.getID().hashCode());
        int offset = 0;

        //todo We have to move this to 0 as soon as we're finished with modularization and remove the switch views in favor of a sidenav xml.
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

            PaneBuilders.tooltipBuilder().hoverPane(iconImage).build().setText(new TranslationTextComponent("com.minecolonies.coremod.gui.maintab"));
        }

        for (IBuildingModuleView view : building.getAllModuleViews())
        {
            if (!view.isPageVisible()) continue;

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

            PaneBuilders.tooltipBuilder().hoverPane(iconImage).build().setText(new TranslationTextComponent(view.getDesc().toLowerCase(Locale.US)));
        }
    }
}
