package com.minecolonies.coremod.commands;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.coremod.colony.permissions.ForgePermissionNodes;
import com.minecolonies.coremod.commands.generalcommands.BackupCommand;

public enum NavigationMenuType implements MenuType
{
//    Colonies (new NavigationMenu("Colonies",
//        ActionMenuType.ListColonies,
//        ActionMenuType.RequestSystemResetAll
//    )),

    Citizens(new NavigationMenu("Citizens",
//        ActionMenuType.ListCitizens,
//        ActionMenuType.KillCitizen,
//        ActionMenuType.RespawnCitizen,
        ActionMenuType.CitizenInfo
    )),

    Colony(new NavigationMenu("Colony",
//        ActionMenuType.ShowColonyInfo,
//        ActionMenuType.DeleteColony,
//        ActionMenuType.DisableBarbarianSpawns,
//        ActionMenuType.AddOfficer,
//        ActionMenuType.RefreshColony,
        ActionMenuType.ChangeColonyOwner
//        ActionMenuType.ColonyTeleport,
//        ActionMenuType.MakeNotAutoDeletable,
//        ActionMenuType.DoRaidNow,
//        ActionMenuType.DoRaidTonight,
//        ActionMenuType.RequestSystem
    )),


//    Delete (new NavigationMenu("Kill",
//        ActionMenuType.BarbarianKill,
//        ActionMenuType.AnimalKill,
//        ActionMenuType.MobKill,
//        ActionMenuType.ChickenKill,
//        ActionMenuType.CowKill,
//        ActionMenuType.PigKill,
//        ActionMenuType.SheepKill
//    )),

    MineColonies(new NavigationMenu("MineColonies",
//            NavigationMenuType.Colonies,
//            NavigationMenuType.Delete,
            NavigationMenuType.Colony,
            NavigationMenuType.Citizens,
            ActionMenuType.RandomTeleport,
            ActionMenuType.Backup,
//            ActionMenuType.HomeTeleport,
//            ActionMenuType.RaidAllTonight,
//            ActionMenuType.RaidAllNow,
//            ActionMenuType.CheckForAutoDeletes,
//            ActionMenuType.WhoAmI,
//            ActionMenuType.WhereAmI,
            ActionMenuType.Scan
    )),
    
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
