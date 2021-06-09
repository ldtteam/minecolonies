package com.minecolonies.coremod.colony.permissions;

import net.minecraftforge.server.permission.DefaultPermissionLevel;
import org.jetbrains.annotations.NotNull;

/**
 * Available forge Permission nodes that can be checked for access.
 * <p>
 * See this url until real documentation becomes available. https://github.com/MinecraftForge/MinecraftForge/pull/3155
 */
public enum ForgePermissionNodes
{
    RANDOM_TELEPORT("com.minecolonies.coremod.RandomTeleport", DefaultPermissionLevel.OP, "Can randomly teleport...."),
    HOME_TELEPORT("com.minecolonies.coremod.HomeTeleport", DefaultPermissionLevel.OP, "Can teleport home...."),
    RAID_ALL_TONIGHT("com.minecolonies.coremod.RaidAllTonight", DefaultPermissionLevel.OP, "Can RaidAllTonight...."),
    RAID_ALL_NOW("com.minecolonies.coremod.RaidAllNow", DefaultPermissionLevel.OP, "Can RaidAllNow...."),
    CHECK_FOR_AUTO_DELETES("com.minecolonies.coremod.CheckForAutoDeletes", DefaultPermissionLevel.OP, "Can RaidAllTonight...."),
    WHO_AM_I("com.minecolonies.coremod.WhoAmI", DefaultPermissionLevel.OP, "Can WhoAmI...."),
    WHERE_AM_I("com.minecolonies.coremod.WhereAmI", DefaultPermissionLevel.OP, "Can WhereAmI...."),
    LIST_COLONIES("com.minecolonies.coremod.ListColonies", DefaultPermissionLevel.OP, "Can ListColonies...."),
    REQUEST_SYSTEM_RESET_ALL("com.minecolonies.coremod.RequestSystemResetAll", DefaultPermissionLevel.OP, "Can RequestSystemResetAll...."),
    BACKUP("com.minecolonies.coremod.Backup", DefaultPermissionLevel.OP, "Can back up colony data."),
    SPAWN_CITIZEN("com.minecolonies.coremod.SpawnCitizen", DefaultPermissionLevel.OP, "Can SpawnCitizen...."),
    LIST_CITIZENS("com.minecolonies.coremod.ListCitizens", DefaultPermissionLevel.ALL, "Can ListCitizens...."),
    KILL_CITIZEN("com.minecolonies.coremod.KillCitizen", DefaultPermissionLevel.ALL, "Can KillCitizen...."),
    RESPAWN_CITIZEN("com.minecolonies.coremod.RespawnCitizen", DefaultPermissionLevel.ALL, "Can RespawnCitizen...."),
    CITIZEN_INFO("com.minecolonies.coremod.CitizenInfo", DefaultPermissionLevel.ALL, "Can view detailed citizen info."),
    CHANGE_COLONY_OWNER("com.minecolonies.coremod.ChangeColonyOwner", DefaultPermissionLevel.OP, "Can change owner of a colony."),
    SHOW_COLONY_INFO("com.minecolonies.coremod.ShowColonyInfo", DefaultPermissionLevel.OP, "Can ShowColonyInfo...."),
    DELETE_COLONY("com.minecolonies.coremod.DeleteColony", DefaultPermissionLevel.OP, "Can DeleteColony...."),
    SET_HAPPINESS_LEVEL_COLONY("com.minecolonies.coremod.SetHappinessLevelColony", DefaultPermissionLevel.OP, "Can SetHappinessLevel...."),
    DISABLE_BARBARIAN_SPAWNS("com.minecolonies.coremod.DisableBarbarianSpawns", DefaultPermissionLevel.OP, "Can DisableBarbarianSpawns...."),
    ADD_OFFICER("com.minecolonies.coremod.AddOfficer", DefaultPermissionLevel.OP, "Can AddOfficer...."),
    REFRESH_COLONY("com.minecolonies.coremod.RefreshColony", DefaultPermissionLevel.OP, "Can RefreshColony...."),
    COLONY_TELEPORT("com.minecolonies.coremod.ColonyTeleport", DefaultPermissionLevel.OP, "Can ColonyTeleport...."),
    MAKE_NOT_AUTO_DELETABLE("com.minecolonies.coremod.MakeNotAutoDeletable", DefaultPermissionLevel.OP, "Can MakeNotAutoDeletable...."),
    DO_RAID_NOW("com.minecolonies.coremod.DoRaidNow", DefaultPermissionLevel.OP, "Can DoRaidNow...."),
    DO_RAID_TONIGHT("com.minecolonies.coremod.DoRaidTonight", DefaultPermissionLevel.OP, "Can DoRaidTonight...."),
    REQUEST_SYSTEM_RESET("com.minecolonies.coremod.RSReset", DefaultPermissionLevel.OP, "Can RSReset...."),
    BARBARIAN_KILL("com.minecolonies.coremod.BarbarianKill", DefaultPermissionLevel.OP, "Can BarbarianKill...."),
    ANIMAL_KILL("com.minecolonies.coremod.AnimalKill", DefaultPermissionLevel.OP, "Can AnimalKill...."),
    MOB_KILL("com.minecolonies.coremod.MobKill", DefaultPermissionLevel.OP, "Can MobKill...."),
    CHICKEN_KILL("com.minecolonies.coremod.ChickenKill", DefaultPermissionLevel.OP, "Can ChickenKill...."),
    COW_KILL("com.minecolonies.coremod.CowKill", DefaultPermissionLevel.OP, "Can CowKill...."),
    PIG_KILL("com.minecolonies.coremod.PigKill", DefaultPermissionLevel.OP, "Can PigKill...."),
    SHEEP_KILL("com.minecolonies.coremod.SheepKill", DefaultPermissionLevel.OP, "Can SheepKill...."),
    SCAN("com.minecolonies.coremod.Scan", DefaultPermissionLevel.OP, "Can scan structures."),
    CLAIM("com.minecolonies.coremod.Claim", DefaultPermissionLevel.OP, "Can claim structures."),
    LOOT_GEN("com.minecolonies.coremod.lootGen", DefaultPermissionLevel.OP, "Can lootGen minecolonies building blocks."),
    LOAD_BACKUP("com.minecolonies.coremod.loadBackup", DefaultPermissionLevel.OP, "Can load colony backups, should be OP only!");

    @NotNull
    private final String                 nodeName;
    @NotNull
    private final DefaultPermissionLevel defaultPermissionLevel;
    @NotNull
    private final String                 description;

    ForgePermissionNodes(@NotNull final String nodeName, @NotNull final DefaultPermissionLevel defaultPermissionLevel, @NotNull final String description)
    {
        this.nodeName = nodeName;
        this.defaultPermissionLevel = defaultPermissionLevel;
        this.description = description;
    }

    public String getNodeName()
    {
        return nodeName;
    }

    public DefaultPermissionLevel getDefaultPermissionLevel()
    {
        return defaultPermissionLevel;
    }

    public String getDescription()
    {
        return description;
    }
}
