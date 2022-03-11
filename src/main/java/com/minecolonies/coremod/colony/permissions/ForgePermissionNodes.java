package com.minecolonies.coremod.colony.permissions;

import org.jetbrains.annotations.NotNull;

/**
 * Available forge Permission nodes that can be checked for access.
 * <p>
 * See this url until real documentation becomes available. https://github.com/MinecraftForge/MinecraftForge/pull/3155
 */
public enum ForgePermissionNodes
{
    RANDOM_TELEPORT("com.minecolonies.coremod.RandomTeleport", "Can randomly teleport...."),
    HOME_TELEPORT("com.minecolonies.coremod.HomeTeleport", "Can teleport home...."),
    RAID_ALL_TONIGHT("com.minecolonies.coremod.RaidAllTonight", "Can RaidAllTonight...."),
    RAID_ALL_NOW("com.minecolonies.coremod.RaidAllNow", "Can RaidAllNow...."),
    CHECK_FOR_AUTO_DELETES("com.minecolonies.coremod.CheckForAutoDeletes", "Can RaidAllTonight...."),
    WHO_AM_I("com.minecolonies.coremod.WhoAmI", "Can WhoAmI...."),
    WHERE_AM_I("com.minecolonies.coremod.WhereAmI", "Can WhereAmI...."),
    LIST_COLONIES("com.minecolonies.coremod.ListColonies", "Can ListColonies...."),
    REQUEST_SYSTEM_RESET_ALL("com.minecolonies.coremod.RequestSystemResetAll", "Can RequestSystemResetAll...."),
    BACKUP("com.minecolonies.coremod.Backup", "Can back up colony data."),
    SPAWN_CITIZEN("com.minecolonies.coremod.SpawnCitizen", "Can SpawnCitizen...."),
    LIST_CITIZENS("com.minecolonies.coremod.ListCitizens", "Can ListCitizens...."),
    KILL_CITIZEN("com.minecolonies.coremod.KillCitizen", "Can KillCitizen...."),
    RESPAWN_CITIZEN("com.minecolonies.coremod.RespawnCitizen", "Can RespawnCitizen...."),
    CITIZEN_INFO("com.minecolonies.coremod.CitizenInfo", "Can view detailed citizen info."),
    CHANGE_COLONY_OWNER("com.minecolonies.coremod.ChangeColonyOwner", "Can change owner of a colony."),
    SHOW_COLONY_INFO("com.minecolonies.coremod.ShowColonyInfo", "Can ShowColonyInfo...."),
    DELETE_COLONY("com.minecolonies.coremod.DeleteColony", "Can DeleteColony...."),
    SET_HAPPINESS_LEVEL_COLONY("com.minecolonies.coremod.SetHappinessLevelColony", "Can SetHappinessLevel...."),
    DISABLE_BARBARIAN_SPAWNS("com.minecolonies.coremod.DisableBarbarianSpawns", "Can DisableBarbarianSpawns...."),
    ADD_OFFICER("com.minecolonies.coremod.AddOfficer", "Can AddOfficer...."),
    REFRESH_COLONY("com.minecolonies.coremod.RefreshColony", "Can RefreshColony...."),
    COLONY_TELEPORT("com.minecolonies.coremod.ColonyTeleport", "Can ColonyTeleport...."),
    MAKE_NOT_AUTO_DELETABLE("com.minecolonies.coremod.MakeNotAutoDeletable", "Can MakeNotAutoDeletable...."),
    DO_RAID_NOW("com.minecolonies.coremod.DoRaidNow", "Can DoRaidNow...."),
    DO_RAID_TONIGHT("com.minecolonies.coremod.DoRaidTonight", "Can DoRaidTonight...."),
    REQUEST_SYSTEM_RESET("com.minecolonies.coremod.RSReset", "Can RSReset...."),
    BARBARIAN_KILL("com.minecolonies.coremod.BarbarianKill", "Can BarbarianKill...."),
    ANIMAL_KILL("com.minecolonies.coremod.AnimalKill", "Can AnimalKill...."),
    MOB_KILL("com.minecolonies.coremod.MobKill", "Can MobKill...."),
    CHICKEN_KILL("com.minecolonies.coremod.ChickenKill", "Can ChickenKill...."),
    COW_KILL("com.minecolonies.coremod.CowKill", "Can CowKill...."),
    PIG_KILL("com.minecolonies.coremod.PigKill", "Can PigKill...."),
    SHEEP_KILL("com.minecolonies.coremod.SheepKill", "Can SheepKill...."),
    SCAN("com.minecolonies.coremod.Scan", "Can scan structures."),
    CLAIM("com.minecolonies.coremod.Claim", "Can claim structures."),
    LOOT_GEN("com.minecolonies.coremod.lootGen", "Can lootGen minecolonies building blocks."),
    LOAD_BACKUP("com.minecolonies.coremod.loadBackup", "Can load colony backups, should be OP only!");

    @NotNull
    private final String                 nodeName;
    @NotNull
    private final String                 description;

    ForgePermissionNodes(@NotNull final String nodeName, @NotNull final String description)
    {
        this.nodeName = nodeName;
        this.description = description;
    }

    public String getNodeName()
    {
        return nodeName;
    }

    public String getDescription()
    {
        return description;
    }
}
