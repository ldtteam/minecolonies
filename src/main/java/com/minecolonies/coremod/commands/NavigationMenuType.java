package com.minecolonies.coremod.commands;

import org.jetbrains.annotations.NotNull;

public enum NavigationMenuType implements IMenuType
{
    COLONIES(new NavigationMenu("colonies",
        ActionMenuType.LIST_COLONIES,
        ActionMenuType.REQUEST_SYSTEM_RESET_ALL
    )),

    CITIZENS(new NavigationMenu("citizens",
        ActionMenuType.SPAWN_CITIZENS,
        ActionMenuType.LIST_CITIZENS,
        ActionMenuType.KILL_CITIZEN,
        ActionMenuType.RESPAWN_CITIZEN,
        ActionMenuType.CITIZEN_INFO
    )),

    COLONY(new NavigationMenu("colony",
        ActionMenuType.SHOW_COLONY_INFO,
        ActionMenuType.DELETE_COLONY,
        ActionMenuType.SET_HAPPINESS_LEVEL_COLONY,
        ActionMenuType.DISABLE_BARBARIAN_SPAWNS,
        ActionMenuType.ADD_OFFICER,
        ActionMenuType.REFRESH_COLONY,
        ActionMenuType.CHANGE_COLONY_OWNER,
        ActionMenuType.COLONY_TELEPORT,
        ActionMenuType.MAKE_NOT_AUTO_DELETABLE,
        ActionMenuType.DO_RAID_NOW,
        ActionMenuType.DO_RAID_TONIGHT,
        ActionMenuType.CLAIM,
        ActionMenuType.LOAD_BACKUP
    )),

    REQUEST_SYSTEM(new NavigationMenu("rs",
            ActionMenuType.REQUEST_SYSTEM_RESET
    )),


    DELETE(new NavigationMenu("kill",
        ActionMenuType.BARBARIAN_KILL,
        ActionMenuType.ANIMAL_KILL,
        ActionMenuType.MOB_KILL,
        ActionMenuType.CHICKEN_KILL,
        ActionMenuType.COW_KILL,
        ActionMenuType.PIG_KILL,
        ActionMenuType.SHEEP_KILL
    )),

    MINECOLONIES(new NavigationMenu("mineColonies",
            NavigationMenuType.COLONIES,
            NavigationMenuType.DELETE,
            NavigationMenuType.COLONY,
            NavigationMenuType.CITIZENS,
            NavigationMenuType.REQUEST_SYSTEM,
            ActionMenuType.RANDOM_TELEPORT,
            ActionMenuType.BACKUP,
            ActionMenuType.HOME_TELEPORT,
            ActionMenuType.RAID_ALL_TONIGHT,
            ActionMenuType.RAID_ALL_NOW,
            ActionMenuType.CHECK_FOR_AUTO_DELETES,
            ActionMenuType.WHO_AM_I,
            ActionMenuType.WHERE_AM_I,
            ActionMenuType.SCAN,
            ActionMenuType.LOOT_GEN
    ))

    ;

    @NotNull private final NavigationMenu menu;

    NavigationMenuType(@NotNull final NavigationMenu menu)
    {
        this.menu = menu;
        this.menu.setMenuType(this);
    }

    public boolean isNavigationMenu()
    {
        return true;
    }

    public NavigationMenu getMenu()
    {
        return menu;
    }
}
