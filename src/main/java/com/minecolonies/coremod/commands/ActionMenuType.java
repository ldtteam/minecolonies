package com.minecolonies.coremod.commands;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.coremod.colony.permissions.ForgePermissionNodes;
import com.minecolonies.coremod.commands.citizencommands.CitizenInfoCommand;
import com.minecolonies.coremod.commands.colonycommands.ChangeColonyOwnerCommand;
import com.minecolonies.coremod.commands.generalcommands.BackupCommand;
import com.minecolonies.coremod.commands.generalcommands.RandomTeleportCommand;
import com.minecolonies.coremod.commands.generalcommands.ScanCommand;

public enum ActionMenuType implements MenuType
{
    RandomTeleport(new ActionMenu(
            "Random Teleport",
            "rtp",
            ForgePermissionNodes.RandomTeleport,
            RandomTeleportCommand.class,
            new ActionArgument("player", ActionArgumentType.Player, ActionArgumentType.Is.Optional)
            )),
    Backup(new ActionMenu(
            "Backup",
            "backup",
            ForgePermissionNodes.Backup,
            BackupCommand.class
            )),
//    HomeTeleport,
//    RaidAllTonight,
//    RaidAllNow,
//    CheckForAutoDeletes,
//    WhoAmI(),
//    WhereAmI(),
//
//    ListColonies(),
//    RequestSystemResetAll(),
//
//    ListCitizens,
//    KillCitizen,
//    RespawnCitizen,
    CitizenInfo(new ActionMenu(
            "Info",
            "info",
            ForgePermissionNodes.CitizenInfo,
            CitizenInfoCommand.class,
            new ActionArgument("colony", ActionArgumentType.Colony, ActionArgumentType.Is.Required,
                    new ActionArgument("citizen", ActionArgumentType.Citizen, ActionArgumentType.Is.Required))
        )),

//    ShowColonyInfo,
//    DeleteColony,
//    DisableBarbarianSpawns,
//    AddOfficer,
//    RefreshColony,
    ChangeColonyOwner(new ActionMenu(
            "Ownership Change",
            "ownerchange",
            ForgePermissionNodes.ChangeColonyOwner,
            ChangeColonyOwnerCommand.class,
            new ActionArgument("colony", ActionArgumentType.Colony, ActionArgumentType.Is.Required),
            new ActionArgument("player", ActionArgumentType.Player, ActionArgumentType.Is.Required)
            )),
//    ColonyTeleport,
//    MakeNotAutoDeletable,
//    DoRaidNow,
//    DoRaidTonight,
//    RequestSystem,
//
//    BarbarianKill,
//    AnimalKill,
//    MobKill,
//    ChickenKill,
//    CowKill,
//    PigKill,
//    SheepKill
    
    Scan(new ActionMenu(
            "Scan",
            "scan",
            ForgePermissionNodes.Scan,
            ScanCommand.class,
            new ActionArgument("x1", ActionArgumentType.CoordinateX, ActionArgumentType.Is.Required),
            new ActionArgument("y1", ActionArgumentType.CoordinateY, ActionArgumentType.Is.Required),
            new ActionArgument("z1", ActionArgumentType.CoordinateZ, ActionArgumentType.Is.Required),
            new ActionArgument("x2", ActionArgumentType.CoordinateX, ActionArgumentType.Is.Required),
            new ActionArgument("y2", ActionArgumentType.CoordinateY, ActionArgumentType.Is.Required),
            new ActionArgument("z2", ActionArgumentType.CoordinateZ, ActionArgumentType.Is.Required)
            )),
    ;

    @NotNull private ActionMenu menu;

    ActionMenuType(@NotNull final ActionMenu menu)
    {
        this.menu = menu;
        this.menu.setMenuType(this);
    }

    public boolean isNavigationMenu()
    {
        return false;
    }

    public ActionMenu getMenu()
    {
        return menu;
    }
}
