package com.minecolonies.coremod.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.Box;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.client.gui.AbstractModuleWindow;
import com.minecolonies.coremod.colony.buildings.moduleviews.SettingsModuleView;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import static com.minecolonies.api.util.constant.WindowConstants.DESC_LABEL;
import static com.minecolonies.api.util.constant.WindowConstants.LIST_SETTINGS;

/**
 * BOWindow for all the settings of a hut.
 */
public class SettingsModuleWindow extends AbstractModuleWindow
{
    /**
     * Resource scrolling list.
     */
    private final ScrollingList settingsList;

    /**
     * The building this belongs to.
     */
    protected final IBuildingView building;

    /**
     * The module view.
     */
    private final SettingsModuleView moduleView;

    /**
     * @param building   the building it belongs to.
     * @param res   the building res id.
     * @param moduleView   the assigned module view.
     */
    public SettingsModuleWindow(
      final String res,
      final IBuildingView building,
      final SettingsModuleView moduleView)
    {
        super(building, res);

        window.findPaneOfTypeByID(DESC_LABEL, Text.class).setText(Component.translatable(moduleView.getDesc().toLowerCase(Locale.US)));
        this.building = building;

        settingsList = window.findPaneOfTypeByID(LIST_SETTINGS, ScrollingList.class);
        this.moduleView = moduleView;
    }

    @Override
    public void onOpened()
    {
        updateSettingsList();
    }

    /**
     * Updates the resource list in the GUI with the info we need.
     */
    private void updateSettingsList()
    {
        settingsList.enable();
        settingsList.show();

        //Creates a dataProvider for the unemployed resourceList.
        settingsList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return moduleView.getActiveSettings().size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ISettingKey<?> key = moduleView.getActiveSettings().get(index);
                if (rowPane.findPaneOfTypeByID("box", Box.class).getChildren().isEmpty())
                {
                    moduleView.getSettings().get(key).setupHandler(key, rowPane, moduleView, building, SettingsModuleWindow.this);
                }
                else if (!rowPane.findPaneOfTypeByID("id", Text.class).getText().getString().equals(key.getUniqueId().toString()))
                {
                    ((View) rowPane).getChildren().clear();
                    moduleView.getSettings().get(key).setupHandler(key, rowPane, moduleView, building, SettingsModuleWindow.this);
                }
                moduleView.getSettings().get(key).render(key, rowPane, moduleView, building, SettingsModuleWindow.this);
            }
        });
    }
}
