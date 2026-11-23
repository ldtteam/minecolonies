package com.minecolonies.core.colony.buildings.modules.settings;

import java.util.List;

/**
 * Stores the automatic-only patrol mode setting used by Stables.
 */
public class StablePatrolModeSetting extends GuardPatrolModeSetting
{
    /**
     * Creates an automatic-only patrol mode setting.
     */
    public StablePatrolModeSetting()
    {
        super(List.of(AUTO), 0);
    }
}
