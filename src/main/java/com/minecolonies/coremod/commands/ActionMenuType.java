package com.minecolonies.coremod.commands;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.coremod.colony.permissions.ForgePermissionNodes;
import com.minecolonies.coremod.commands.citizencommands.CitizenInfoCommand;
import com.minecolonies.coremod.commands.citizencommands.KillCitizenCommand;
import com.minecolonies.coremod.commands.citizencommands.ListCitizensCommand;
import com.minecolonies.coremod.commands.citizencommands.RespawnCitizenCommand;
import com.minecolonies.coremod.commands.colonycommands.AddOfficerCommand;
import com.minecolonies.coremod.commands.colonycommands.ChangeColonyOwnerCommand;
import com.minecolonies.coremod.commands.colonycommands.ColonyTeleportCommand;
import com.minecolonies.coremod.commands.colonycommands.DeleteColonyCommand;
import com.minecolonies.coremod.commands.colonycommands.DisableBarbarianSpawnsCommand;
import com.minecolonies.coremod.commands.colonycommands.DoRaidNowCommand;
import com.minecolonies.coremod.commands.colonycommands.DoRaidTonightCommand;
import com.minecolonies.coremod.commands.colonycommands.HomeTeleportCommand;
import com.minecolonies.coremod.commands.colonycommands.ListColoniesCommand;
import com.minecolonies.coremod.commands.colonycommands.RefreshColonyCommand;
import com.minecolonies.coremod.commands.colonycommands.ShowColonyInfoCommand;
import com.minecolonies.coremod.commands.colonycommands.requestsystem.RSResetAllCommand;
import com.minecolonies.coremod.commands.colonycommands.requestsystem.RSResetCommand;
import com.minecolonies.coremod.commands.generalcommands.BackupCommand;
import com.minecolonies.coremod.commands.generalcommands.CheckForAutoDeletesCommand;
import com.minecolonies.coremod.commands.generalcommands.RaidAllNowCommand;
import com.minecolonies.coremod.commands.generalcommands.RaidAllTonightCommand;
import com.minecolonies.coremod.commands.generalcommands.RandomTeleportCommand;
import com.minecolonies.coremod.commands.generalcommands.ScanCommand;
import com.minecolonies.coremod.commands.generalcommands.WhereAmICommand;
import com.minecolonies.coremod.commands.generalcommands.WhoAmICommand;
import com.minecolonies.coremod.commands.killcommands.AnimalKillCommand;
import com.minecolonies.coremod.commands.killcommands.BarbarianKillCommand;
import com.minecolonies.coremod.commands.killcommands.ChickenKillCommand;
import com.minecolonies.coremod.commands.killcommands.CowKillCommand;
import com.minecolonies.coremod.commands.killcommands.MobKillCommand;
import com.minecolonies.coremod.commands.killcommands.PigKillCommand;
import com.minecolonies.coremod.commands.killcommands.SheepKillCommand;

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
    HomeTeleport(new ActionMenu(
            "HomeTeleport",
            "home",
            ForgePermissionNodes.HomeTeleport,
            HomeTeleportCommand.class
            )),
    RaidAllTonight(new ActionMenu(
            "RaidAllTonight",
            "raid-tonight",
            ForgePermissionNodes.RaidAllTonight,
            RaidAllTonightCommand.class
            )),
    RaidAllNow(new ActionMenu(
            "RaidAllNow",
            "raid-now",
            ForgePermissionNodes.RaidAllNow,
            RaidAllNowCommand.class
            )),
    CheckForAutoDeletes(new ActionMenu(
            "CheckForAutoDeletes",
            "check",
            ForgePermissionNodes.CheckForAutoDeletes,
            CheckForAutoDeletesCommand.class,
            new ActionArgument("confirmDelete", ActionArgumentType.BOOLEAN, ActionArgumentType.Is.Optional)
            )),
    WhoAmI(new ActionMenu(
            "WhoAmI",
            "whoami",
            ForgePermissionNodes.WhoAmI,
            WhoAmICommand.class
            )),
    WhereAmI(new ActionMenu(
            "WhereAmI",
            "whereami",
            ForgePermissionNodes.WhereAmI,
            WhereAmICommand.class
            )),

    ListColonies(new ActionMenu(
            "ListColonies",
            "list",
            ForgePermissionNodes.ListColonies,
            ListColoniesCommand.class,
            new ActionArgument("page", ActionArgumentType.INTEGER, ActionArgumentType.Is.Optional),
            new ActionArgument("abandonedSinceTimeInHours", ActionArgumentType.INTEGER, ActionArgumentType.Is.Optional)
            )),
    RequestSystemResetAll(new ActionMenu(
            "RequestSystemResetAll",
            "rsResetAll",
            ForgePermissionNodes.RequestSystemResetAll,
            RSResetAllCommand.class
            )),

    ListCitizens(new ActionMenu(
            "ListCitizens",
            "list",
            ForgePermissionNodes.ListCitizens,
            ListCitizensCommand.class,
            new ActionArgument("colony", ActionArgumentType.Colony, ActionArgumentType.Is.Required),
            new ActionArgument("page", ActionArgumentType.INTEGER, ActionArgumentType.Is.Optional)
        )),
    KillCitizen(new ActionMenu(
            "KillCitizen",
            "kill",
            ForgePermissionNodes.KillCitizen,
            KillCitizenCommand.class,
            new ActionArgument("colony", ActionArgumentType.Colony, ActionArgumentType.Is.Required,
                    new ActionArgument("citizen", ActionArgumentType.Citizen, ActionArgumentType.Is.Required))
        )),
    RespawnCitizen(new ActionMenu(
            "RespawnCitizen",
            "respawn",
            ForgePermissionNodes.RespawnCitizen,
            RespawnCitizenCommand.class,
            new ActionArgument("colony", ActionArgumentType.Colony, ActionArgumentType.Is.Required,
                    new ActionArgument("citizen", ActionArgumentType.Citizen, ActionArgumentType.Is.Required))
        )),
    CitizenInfo(new ActionMenu(
            "Info",
            "info",
            ForgePermissionNodes.CitizenInfo,
            CitizenInfoCommand.class,
            new ActionArgument("colony", ActionArgumentType.Colony, ActionArgumentType.Is.Required,
                    new ActionArgument("citizen", ActionArgumentType.Citizen, ActionArgumentType.Is.Required))
        )),

    ShowColonyInfo(new ActionMenu(
            "ShowColonyInfo",
            "info",
            ForgePermissionNodes.ShowColonyInfo,
            ShowColonyInfoCommand.class,
            // TODO: need OR() ActionArgumentType
            new ActionArgument("player", ActionArgumentType.Player, ActionArgumentType.Is.Optional),
            new ActionArgument("colony", ActionArgumentType.Colony, ActionArgumentType.Is.Optional)
            )),
    DeleteColony(new ActionMenu(
            "DeleteColony",
            "delete",
            ForgePermissionNodes.DeleteColony,
            DeleteColonyCommand.class,
            new ActionArgument("colony", ActionArgumentType.Colony, ActionArgumentType.Is.Required),
            new ActionArgument("canDestroy", ActionArgumentType.BOOLEAN, ActionArgumentType.Is.Optional),
            new ActionArgument("confirmDelete", ActionArgumentType.BOOLEAN, ActionArgumentType.Is.Optional)
            )),
    DisableBarbarianSpawns(new ActionMenu(
            "DisableBarbarianSpawns",
            "barbarians",
            ForgePermissionNodes.DisableBarbarianSpawns,
            DisableBarbarianSpawnsCommand.class,
            new ActionArgument("colony", ActionArgumentType.Colony, ActionArgumentType.Is.Required),
            new ActionArgument("disableSpawns", ActionArgumentType.BOOLEAN, ActionArgumentType.Is.Required)
            )),
    AddOfficer(new ActionMenu(
            "AddOfficer",
            "addOfficer",
            ForgePermissionNodes.AddOfficer,
            AddOfficerCommand.class,
            new ActionArgument("colony", ActionArgumentType.Colony, ActionArgumentType.Is.Required),
            new ActionArgument("player", ActionArgumentType.Player, ActionArgumentType.Is.Required)
            )),
    RefreshColony(new ActionMenu(
            "RefreshColony",
            "refresh",
            ForgePermissionNodes.RefreshColony,
            RefreshColonyCommand.class,
            // TODO: need OR() ActionArgumentType
            new ActionArgument("player", ActionArgumentType.Player, ActionArgumentType.Is.Optional),
            new ActionArgument("colony", ActionArgumentType.Colony, ActionArgumentType.Is.Optional)
            )),
    ChangeColonyOwner(new ActionMenu(
            "Ownership Change",
            "ownerchange",
            ForgePermissionNodes.ChangeColonyOwner,
            ChangeColonyOwnerCommand.class,
            new ActionArgument("colony", ActionArgumentType.Colony, ActionArgumentType.Is.Required),
            new ActionArgument("player", ActionArgumentType.Player, ActionArgumentType.Is.Required)
            )),
    ColonyTeleport(new ActionMenu(
            "ColonyTeleport",
            "teleport",
            ForgePermissionNodes.ColonyTeleport,
            ColonyTeleportCommand.class,
            new ActionArgument("colony", ActionArgumentType.Colony, ActionArgumentType.Is.Required)
            )),
    MakeNotAutoDeletable(new ActionMenu(
            "MakeNotAutoDeletable",
            "deletable",
            ForgePermissionNodes.MakeNotAutoDeletable,
            com.minecolonies.coremod.commands.colonycommands.MakeNotAutoDeletable.class,
            new ActionArgument("colony", ActionArgumentType.Colony, ActionArgumentType.Is.Required),
            new ActionArgument("canBeDeleted", ActionArgumentType.BOOLEAN, ActionArgumentType.Is.Required)
            )),
    DoRaidNow(new ActionMenu(
            "DoRaidNow",
            "raid",
            ForgePermissionNodes.DoRaidNow,
            DoRaidNowCommand.class,
            new ActionArgument("colony", ActionArgumentType.Colony, ActionArgumentType.Is.Required)
            )),
    DoRaidTonight(new ActionMenu(
            "DoRaidTonight",
            "raid-tonight",
            ForgePermissionNodes.DoRaidTonight,
            DoRaidTonightCommand.class,
            new ActionArgument("colony", ActionArgumentType.Colony, ActionArgumentType.Is.Required)
            )),
    RSReset(new ActionMenu(
            "Request System Reset",
            "reset",
            ForgePermissionNodes.RSReset,
            RSResetCommand.class,
            new ActionArgument("colony", ActionArgumentType.Colony, ActionArgumentType.Is.Required)
            )),
    BarbarianKill(new ActionMenu(
            "BarbarianKill",
            "barbarians",
            ForgePermissionNodes.BarbarianKill,
            BarbarianKillCommand.class
            )),
    AnimalKill(new ActionMenu(
            "AnimalKill",
            "animals",
            ForgePermissionNodes.AnimalKill,
            AnimalKillCommand.class
            )),
    MobKill(new ActionMenu(
            "MobKill",
            "mob",
            ForgePermissionNodes.MobKill,
            MobKillCommand.class
            )),
    ChickenKill(new ActionMenu(
            "ChickenKill",
            "chicken",
            ForgePermissionNodes.ChickenKill,
            ChickenKillCommand.class
            )),
    CowKill(new ActionMenu(
            "CowKill",
            "cow",
            ForgePermissionNodes.CowKill,
            CowKillCommand.class
            )),
    PigKill(new ActionMenu(
            "PigKill",
            "pig",
            ForgePermissionNodes.PigKill,
            PigKillCommand.class
            )),
    SheepKill(new ActionMenu(
            "SheepKill",
            "sheep",
            ForgePermissionNodes.SheepKill,
            SheepKillCommand.class
            )),
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
