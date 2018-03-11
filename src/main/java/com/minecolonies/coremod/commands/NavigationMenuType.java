package com.minecolonies.coremod.commands;

import org.jetbrains.annotations.NotNull;

public enum NavigationMenuType implements MenuType
{
    Colonies(new NavigationMenu("colonies",
        ActionMenuType.ListColonies,
        ActionMenuType.RequestSystemResetAll
    )),

    Citizens(new NavigationMenu("citizens",
        ActionMenuType.ListCitizens,
        ActionMenuType.KillCitizen,
        ActionMenuType.RespawnCitizen,
        ActionMenuType.CitizenInfo
    )),

    Colony(new NavigationMenu("colony",
        ActionMenuType.ShowColonyInfo,
        ActionMenuType.DeleteColony,
        ActionMenuType.DisableBarbarianSpawns,
        ActionMenuType.AddOfficer,
        ActionMenuType.RefreshColony,
        ActionMenuType.ChangeColonyOwner,
        ActionMenuType.ColonyTeleport,
        ActionMenuType.MakeNotAutoDeletable,
        ActionMenuType.DoRaidNow,
        ActionMenuType.DoRaidTonight
    )),

    RequestSystem(new NavigationMenu("rs",
            ActionMenuType.RSReset
    )),


    Delete(new NavigationMenu("kill",
        ActionMenuType.BarbarianKill,
        ActionMenuType.AnimalKill,
        ActionMenuType.MobKill,
        ActionMenuType.ChickenKill,
        ActionMenuType.CowKill,
        ActionMenuType.PigKill,
        ActionMenuType.SheepKill
    )),

    MineColonies(new NavigationMenu("mineColonies",
            NavigationMenuType.Colonies,
            NavigationMenuType.Delete,
            NavigationMenuType.Colony,
            NavigationMenuType.Citizens,
            ActionMenuType.RandomTeleport,
            ActionMenuType.Backup,
            ActionMenuType.HomeTeleport,
            ActionMenuType.RaidAllTonight,
            ActionMenuType.RaidAllNow,
            ActionMenuType.CheckForAutoDeletes,
            ActionMenuType.WhoAmI,
            ActionMenuType.WhereAmI,
            ActionMenuType.Scan
    ))

    ;

    @NotNull private NavigationMenu menu;

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
