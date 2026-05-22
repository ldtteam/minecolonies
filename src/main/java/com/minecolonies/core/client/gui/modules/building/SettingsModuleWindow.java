package com.minecolonies.core.client.gui.modules.building;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.Box;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.colony.buildings.moduleviews.SettingsModuleView;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.WindowConstants.LIST_SETTINGS;

/**
 * BOWindow for all the settings of a hut.
 */
public class SettingsModuleWindow extends AbstractModuleWindow<SettingsModuleView>
{
    /**
     * Resource scrolling list.
     */
    private final ScrollingList settingsList;

    /**
     * @param moduleView the assigned module view.
     */
    public SettingsModuleWindow(final SettingsModuleView moduleView)
    {
        super(moduleView, new ResourceLocation(Constants.MOD_ID, "gui/layouthuts/layoutsettings.xml"));
        settingsList = window.findPaneOfTypeByID(LIST_SETTINGS, ScrollingList.class);
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
        settingsList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return moduleView.getSettingsToShow().size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ISettingKey<? extends ISetting> key = moduleView.getSettingsToShow().get(index);
                final ISetting<?> setting = moduleView.getSetting(key);
                if (setting == null)
                {
                    return;
                }

                final Box box = rowPane.findPaneOfTypeByID("box", Box.class);
                final Text idField = box.findPaneOfTypeByID("id", Text.class);

                if (idField != null && !idField.getTextAsString().equals(key.getUniqueId().toString()))
                {
                    box.getChildren().clear();
                }

                if (box.getChildren().isEmpty())
                {
                    Loader.createFromXMLFile(setting.getLayoutItem(), (View) rowPane);
                    setting.setupHandler(key, rowPane, moduleView, buildingView, SettingsModuleWindow.this);
                    final Text rowIdField = rowPane.findPaneOfTypeByID("id", Text.class);
                    if (rowIdField != null)
                    {
                        rowIdField.setText(Component.literal(key.getUniqueId().toString()));
                    }
                    else
                    {
                        Log.getLogger()
                          .warn(
                            "Settings for class \"{}\" it's window does not provide an \"id\" field. Make sure this exists so the view can be properly recycled when the settings list is modified!",
                            setting.getClass().getName());
                    }
                    final Text rowDescriptionField = rowPane.findPaneOfTypeByID("desc", Text.class);
                    if (rowDescriptionField != null)
                    {
                        rowDescriptionField.setText(Component.translatable("com.minecolonies.coremod.setting." + key.getUniqueId().toString()));
                    }
                }

                setting.render(key, rowPane, moduleView, buildingView, SettingsModuleWindow.this);
            }
        });
    }
}
