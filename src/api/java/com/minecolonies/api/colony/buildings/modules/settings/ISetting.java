package com.minecolonies.api.colony.buildings.modules.settings;

import com.ldtteam.blockout.views.Box;

public interface ISetting
{
    /**
     * todo we need sth like this, allowing to draw the setting inside a list. So we just have a list that we fill with settings.size and then we go over each, give it its own little pane and fill it in.
     * @param key
     */
    void addHandlersToBox(final String key, final Box rowPane, final ISettingsModuleView settingsModuleView);

    void trigger();
}
