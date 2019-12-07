package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.permissions.ForgePermissionNodes;
import com.minecolonies.coremod.commands.citizencommands.*;
import com.minecolonies.coremod.commands.colonycommands.*;
import com.minecolonies.coremod.commands.colonycommands.requestsystem.RSResetAllCommand;
import com.minecolonies.coremod.commands.colonycommands.requestsystem.RSResetCommand;
import com.minecolonies.coremod.commands.generalcommands.*;
import com.minecolonies.coremod.commands.killcommands.*;
import org.jetbrains.annotations.NotNull;

//PMD.AvoidDuplicateLiterals: We want to have literals used instead of constants as we are defining commands
//and do not necessarily want one command's syntax dependent on another command
//PMD.ExcessiveImports: This class DOES have a high degree of coupling by design.
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.ExcessiveImports"})
public enum ActionMenuType implements IMenuType
{
    RANDOM_TELEPORT(new ActionMenu(
            "Random Teleport",
            "rtp",
            ForgePermissionNodes.RANDOM_TELEPORT,
            RandomTeleportCommand.class,
            new ActionArgument("player", ActionArgumentType.ONLINE_PLAYER, ActionArgumentType.Is.OPTIONAL)
            )),
    BACKUP(new ActionMenu(
            "Backup",
            "backup",
            ForgePermissionNodes.BACKUP,
            BackupCommand.class
            )),
    HOME_TELEPORT(new ActionMenu(
            "HomeTeleport",
            "home",
            ForgePermissionNodes.HOME_TELEPORT,
            HomeTeleportCommand.class
            )),
    LOAD_BACKUP(new ActionMenu(
      "LoadBackup",
      "loadBackup",
      ForgePermissionNodes.LOAD_BACKUP,
      LoadColonyBackupCommand.class,
      new ActionArgument("colony", ActionArgumentType.COLONY, ActionArgumentType.Is.REQUIRED)

    )),
    RAID_ALL_TONIGHT(new ActionMenu(
            "RaidAllTonight",
            "raid-tonight",
            ForgePermissionNodes.RAID_ALL_TONIGHT,
            RaidAllTonightCommand.class
            )),
    RAID_ALL_NOW(new ActionMenu(
            "RaidAllNow",
            "raid-now",
            ForgePermissionNodes.RAID_ALL_NOW,
            RaidAllNowCommand.class
            )),
    CHECK_FOR_AUTO_DELETES(new ActionMenu(
            "CheckForAutoDeletes",
            "check",
            ForgePermissionNodes.CHECK_FOR_AUTO_DELETES,
            CheckForAutoDeletesCommand.class,
            new ActionArgument("confirmDelete", ActionArgumentType.BOOLEAN, ActionArgumentType.Is.OPTIONAL)
            )),
    WHO_AM_I(new ActionMenu(
            "WhoAmI",
            "whoami",
            ForgePermissionNodes.WHO_AM_I,
            WhoAmICommand.class
            )),
    WHERE_AM_I(new ActionMenu(
            "WhereAmI",
            "whereami",
            ForgePermissionNodes.WHERE_AM_I,
            WhereAmICommand.class
            )),

    LIST_COLONIES(new ActionMenu(
            "ListColonies",
            "list",
            ForgePermissionNodes.LIST_COLONIES,
            ListColoniesCommand.class,
            new ActionArgument("page", ActionArgumentType.INTEGER, ActionArgumentType.Is.OPTIONAL),
            new ActionArgument("abandonedSinceTimeInHours", ActionArgumentType.INTEGER, ActionArgumentType.Is.OPTIONAL)
            )),
    REQUEST_SYSTEM_RESET_ALL(new ActionMenu(
            "RequestSystemResetAll",
            "rsResetAll",
            ForgePermissionNodes.REQUEST_SYSTEM_RESET_ALL,
            RSResetAllCommand.class
            )),

    SPAWN_CITIZENS(new ActionMenu(
      "SpawnCitizen",
      "spawn",
      ForgePermissionNodes.SPAWN_CITIZEN,
      SpawnCitizenCommand.class,
      new ActionArgument("colony", ActionArgumentType.COLONY, ActionArgumentType.Is.REQUIRED))),
    LIST_CITIZENS(new ActionMenu(
            "ListCitizens",
            "list",
            ForgePermissionNodes.LIST_CITIZENS,
            ListCitizensCommand.class,
            new ActionArgument("colony", ActionArgumentType.COLONY, ActionArgumentType.Is.REQUIRED),
            new ActionArgument("page", ActionArgumentType.INTEGER, ActionArgumentType.Is.OPTIONAL)
        )),
    KILL_CITIZEN(new ActionMenu(
            "KillCitizen",
            "kill",
            ForgePermissionNodes.KILL_CITIZEN,
            KillCitizenCommand.class,
            new ActionArgument("colony", ActionArgumentType.COLONY, ActionArgumentType.Is.REQUIRED,
                    new ActionArgument("citizen", ActionArgumentType.CITIZEN, ActionArgumentType.Is.REQUIRED))
        )),
    RESPAWN_CITIZEN(new ActionMenu(
            "RespawnCitizen",
            "respawn",
            ForgePermissionNodes.RESPAWN_CITIZEN,
            RespawnCitizenCommand.class,
            new ActionArgument("colony", ActionArgumentType.COLONY, ActionArgumentType.Is.REQUIRED,
                    new ActionArgument("citizen", ActionArgumentType.CITIZEN, ActionArgumentType.Is.REQUIRED))
        )),
    CITIZEN_INFO(new ActionMenu(
            "Info",
            "info",
            ForgePermissionNodes.CITIZEN_INFO,
            CitizenInfoCommand.class,
            new ActionArgument("colony", ActionArgumentType.COLONY, ActionArgumentType.Is.REQUIRED,
                    new ActionArgument("citizen", ActionArgumentType.CITIZEN, ActionArgumentType.Is.REQUIRED))
        )),

    SHOW_COLONY_INFO(new ActionMenu(
            "ShowColonyInfo",
            "info",
            ForgePermissionNodes.SHOW_COLONY_INFO,
            ShowColonyInfoCommand.class,
            // TODO: need OR() ActionArgumentType
            new ActionArgument("player", ActionArgumentType.PLAYER, ActionArgumentType.Is.OPTIONAL),
            new ActionArgument("colony", ActionArgumentType.COLONY, ActionArgumentType.Is.OPTIONAL)
            )),
    DELETE_COLONY(new ActionMenu(
      "DeleteColony",
      "delete",
      ForgePermissionNodes.DELETE_COLONY,
      DeleteColonyCommand.class,
      new ActionArgument("colony", ActionArgumentType.COLONY, ActionArgumentType.Is.REQUIRED),
      new ActionArgument("canDestroy", ActionArgumentType.BOOLEAN, ActionArgumentType.Is.OPTIONAL),
      new ActionArgument("confirmDelete", ActionArgumentType.BOOLEAN, ActionArgumentType.Is.OPTIONAL)
    )),
    SET_HAPPINESS_LEVEL_COLONY(new ActionMenu(
      "Set Happiness Level",
      "shl",
      ForgePermissionNodes.DELETE_COLONY,
      SetHappinessLevelColonyCommand.class,
      new ActionArgument("colony", ActionArgumentType.COLONY, ActionArgumentType.Is.REQUIRED),
      new ActionArgument("level", ActionArgumentType.DOUBLE, ActionArgumentType.Is.OPTIONAL)
    )),
    DISABLE_BARBARIAN_SPAWNS(new ActionMenu(
            "DisableBarbarianSpawns",
            "barbarians",
            ForgePermissionNodes.DISABLE_BARBARIAN_SPAWNS,
            DisableBarbarianSpawnsCommand.class,
            new ActionArgument("colony", ActionArgumentType.COLONY, ActionArgumentType.Is.REQUIRED),
            new ActionArgument("disableSpawns", ActionArgumentType.BOOLEAN, ActionArgumentType.Is.REQUIRED)
            )),
    ADD_OFFICER(new ActionMenu(
            "AddOfficer",
            "addOfficer",
            ForgePermissionNodes.ADD_OFFICER,
            AddOfficerCommand.class,
            new ActionArgument("colony", ActionArgumentType.COLONY, ActionArgumentType.Is.REQUIRED),
            new ActionArgument("player", ActionArgumentType.PLAYER, ActionArgumentType.Is.REQUIRED)
            )),
    REFRESH_COLONY(new ActionMenu(
            "RefreshColony",
            "refresh",
            ForgePermissionNodes.REFRESH_COLONY,
            RefreshColonyCommand.class,
            // TODO: need OR() ActionArgumentType
            new ActionArgument("player", ActionArgumentType.PLAYER, ActionArgumentType.Is.OPTIONAL),
            new ActionArgument("colony", ActionArgumentType.COLONY, ActionArgumentType.Is.OPTIONAL)
            )),
    CHANGE_COLONY_OWNER(new ActionMenu(
            "Ownership Change",
            "ownerchange",
            ForgePermissionNodes.CHANGE_COLONY_OWNER,
            ChangeColonyOwnerCommand.class,
            new ActionArgument("colony", ActionArgumentType.COLONY, ActionArgumentType.Is.REQUIRED),
            new ActionArgument("player", ActionArgumentType.PLAYER, ActionArgumentType.Is.REQUIRED)
            )),
    COLONY_TELEPORT(new ActionMenu(
            "ColonyTeleport",
            "teleport",
            ForgePermissionNodes.COLONY_TELEPORT,
            ColonyTeleportCommand.class,
            new ActionArgument("colony", ActionArgumentType.COLONY, ActionArgumentType.Is.REQUIRED)
            )),
    MAKE_NOT_AUTO_DELETABLE(new ActionMenu(
            "MakeNotAutoDeletable",
            "deletable",
            ForgePermissionNodes.MAKE_NOT_AUTO_DELETABLE,
            MakeNotAutoDeletableCommand.class,
            new ActionArgument("colony", ActionArgumentType.COLONY, ActionArgumentType.Is.REQUIRED),
            new ActionArgument("canBeDeleted", ActionArgumentType.BOOLEAN, ActionArgumentType.Is.REQUIRED)
            )),
    DO_RAID_NOW(new ActionMenu(
            "DoRaidNow",
            "raid",
            ForgePermissionNodes.DO_RAID_NOW,
            DoRaidNowCommand.class,
            new ActionArgument("colony", ActionArgumentType.COLONY, ActionArgumentType.Is.REQUIRED)
            )),
    DO_RAID_TONIGHT(new ActionMenu(
            "DoRaidTonight",
            "raid-tonight",
            ForgePermissionNodes.DO_RAID_TONIGHT,
            DoRaidTonightCommand.class,
            new ActionArgument("colony", ActionArgumentType.COLONY, ActionArgumentType.Is.REQUIRED)
            )),
    REQUEST_SYSTEM_RESET(new ActionMenu(
            "Request System Reset",
            "reset",
            ForgePermissionNodes.REQUEST_SYSTEM_RESET,
            RSResetCommand.class,
            new ActionArgument("colony", ActionArgumentType.COLONY, ActionArgumentType.Is.REQUIRED)
            )),
    BARBARIAN_KILL(new ActionMenu(
            "RaiderKill",
            "raiders",
            ForgePermissionNodes.BARBARIAN_KILL,
            RaiderKillCommand.class
            )),
    ANIMAL_KILL(new ActionMenu(
            "AnimalKill",
            "animals",
            ForgePermissionNodes.ANIMAL_KILL,
            AnimalKillCommand.class
            )),
    MOB_KILL(new ActionMenu(
            "MobKill",
            "mob",
            ForgePermissionNodes.MOB_KILL,
            MobKillCommand.class
            )),
    CHICKEN_KILL(new ActionMenu(
            "ChickenKill",
            "chicken",
            ForgePermissionNodes.CHICKEN_KILL,
            ChickenKillCommand.class
            )),
    COW_KILL(new ActionMenu(
            "CowKill",
            "cow",
            ForgePermissionNodes.COW_KILL,
            CowKillCommand.class
            )),
    PIG_KILL(new ActionMenu(
            "PigKill",
            "pig",
            ForgePermissionNodes.PIG_KILL,
            PigKillCommand.class
            )),
    SHEEP_KILL(new ActionMenu(
            "SheepKill",
            "sheep",
            ForgePermissionNodes.SHEEP_KILL,
            SheepKillCommand.class
            )),
    SCAN(new ActionMenu(
            "Scan",
            "scan",
            ForgePermissionNodes.SCAN,
            ScanCommand.class,
            new ActionArgument("player", ActionArgumentType.STRING, ActionArgumentType.Is.OPTIONAL),
            new ActionArgument("x1", ActionArgumentType.COORDINATE_X, ActionArgumentType.Is.REQUIRED),
            new ActionArgument("y1", ActionArgumentType.COORDINATE_Y, ActionArgumentType.Is.REQUIRED),
            new ActionArgument("z1", ActionArgumentType.COORDINATE_Z, ActionArgumentType.Is.REQUIRED),
            new ActionArgument("x2", ActionArgumentType.COORDINATE_X, ActionArgumentType.Is.REQUIRED),
            new ActionArgument("y2", ActionArgumentType.COORDINATE_Y, ActionArgumentType.Is.REQUIRED),
            new ActionArgument("z2", ActionArgumentType.COORDINATE_Z, ActionArgumentType.Is.REQUIRED),
            new ActionArgument("name", ActionArgumentType.STRING, ActionArgumentType.Is.OPTIONAL)
            )),
    CLAIM(new ActionMenu(
            "Claim",
            "claim",
            ForgePermissionNodes.CLAIM,
            ClaimChunksCommand.class,
            new ActionArgument("colony", ActionArgumentType.INTEGER, ActionArgumentType.Is.REQUIRED),
            new ActionArgument("dimension", ActionArgumentType.INTEGER, ActionArgumentType.Is.REQUIRED),
            new ActionArgument("range", ActionArgumentType.INTEGER, ActionArgumentType.Is.OPTIONAL),
            new ActionArgument("add", ActionArgumentType.BOOLEAN, ActionArgumentType.Is.OPTIONAL)
            )),
    LOOT_GEN(new ActionMenu(
      "lootGen",
      "lootGen",
      ForgePermissionNodes.LOOT_GEN,
      LootGenCommand.class,
      new ActionArgument("building", ActionArgumentType.STRING, ActionArgumentType.Is.REQUIRED),
      new ActionArgument("paste", ActionArgumentType.BOOLEAN, ActionArgumentType.Is.OPTIONAL),
      new ActionArgument("level", ActionArgumentType.INTEGER, ActionArgumentType.Is.OPTIONAL)
    )),;

    @NotNull private final ActionMenu menu;

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
