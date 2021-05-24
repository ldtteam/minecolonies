package com.minecolonies.coremod.client.gui.modules;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.client.gui.AbstractModuleWindow;
import com.minecolonies.coremod.colony.buildings.moduleviews.SettingsModuleView;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for all the settings of a hut.
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

        window.findPaneOfTypeByID(DESC_LABEL, Text.class).setText(new TranslationTextComponent(moduleView.getDesc().toLowerCase(Locale.US)));
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
        final List<ISettingKey<?>> list = new ArrayList<>(moduleView.getSettings().keySet());

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
                return list.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ISettingKey<?> key = list.get(index);
                moduleView.getSettings().get(key).addHandlersToBox(key, rowPane, moduleView, building, SettingsModuleWindow.this);
            }
        });
    }
}
