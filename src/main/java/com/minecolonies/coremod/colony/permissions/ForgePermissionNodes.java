package com.minecolonies.coremod.colony.permissions;

import net.minecraftforge.server.permission.DefaultPermissionLevel;

/**
 * Available forge Permission nodes that can be checked for access.
 *
 * See this url until real documentation becomes available.
 * https://github.com/MinecraftForge/MinecraftForge/pull/3155
 *
 */
public enum ForgePermissionNodes
{
    RandomTeleport ("com.minecolonies.coremod.RandomTeleport", DefaultPermissionLevel.OP, "Can randomly teleport...."),
    HomeTeleport ("com.minecolonies.coremod.HomeTeleport", DefaultPermissionLevel.OP, "Can teleport home...."),
    RaidAllTonight ("com.minecolonies.coremod.RaidAllTonight", DefaultPermissionLevel.OP, "Can RaidAllTonight...."),
    RaidAllNow ("com.minecolonies.coremod.RaidAllNow", DefaultPermissionLevel.OP, "Can RaidAllNow...."),
    CheckForAutoDeletes ("com.minecolonies.coremod.CheckForAutoDeletes", DefaultPermissionLevel.OP, "Can RaidAllTonight...."),
    WhoAmI ("com.minecolonies.coremod.WhoAmI", DefaultPermissionLevel.OP, "Can WhoAmI...."),
    WhereAmI ("com.minecolonies.coremod.WhereAmI", DefaultPermissionLevel.OP, "Can WhereAmI...."),
    ListColonies ("com.minecolonies.coremod.ListColonies", DefaultPermissionLevel.OP, "Can ListColonies...."),
    RequestSystemResetAll ("com.minecolonies.coremod.RequestSystemResetAll", DefaultPermissionLevel.OP, "Can RequestSystemResetAll...."),
    Backup ("com.minecolonies.coremod.Backup", DefaultPermissionLevel.OP, "Can back up colony data."),
    ListCitizens ("com.minecolonies.coremod.ListCitizens", DefaultPermissionLevel.ALL, "Can ListCitizens...."),
    KillCitizen ("com.minecolonies.coremod.KillCitizen", DefaultPermissionLevel.ALL, "Can KillCitizen...."),
    RespawnCitizen ("com.minecolonies.coremod.RespawnCitizen", DefaultPermissionLevel.ALL, "Can RespawnCitizen...."),
    CitizenInfo ("com.minecolonies.coremod.CitizenInfo", DefaultPermissionLevel.ALL, "Can view detailed citizen info."),
    ChangeColonyOwner ("com.minecolonies.coremod.ChangeColonyOwner", DefaultPermissionLevel.OP, "Can change owner of a colony."),
    ShowColonyInfo ("com.minecolonies.coremod.ShowColonyInfo", DefaultPermissionLevel.OP, "Can ShowColonyInfo...."),
    DeleteColony ("com.minecolonies.coremod.DeleteColony", DefaultPermissionLevel.OP, "Can DeleteColony...."),
    DisableBarbarianSpawns ("com.minecolonies.coremod.DisableBarbarianSpawns", DefaultPermissionLevel.OP, "Can DisableBarbarianSpawns...."),
    AddOfficer ("com.minecolonies.coremod.AddOfficer", DefaultPermissionLevel.OP, "Can AddOfficer...."),
    RefreshColony ("com.minecolonies.coremod.RefreshColony", DefaultPermissionLevel.OP, "Can RefreshColony...."),
    ColonyTeleport ("com.minecolonies.coremod.ColonyTeleport", DefaultPermissionLevel.OP, "Can ColonyTeleport...."),
    MakeNotAutoDeletable ("com.minecolonies.coremod.MakeNotAutoDeletable", DefaultPermissionLevel.OP, "Can MakeNotAutoDeletable...."),
    DoRaidNow ("com.minecolonies.coremod.DoRaidNow", DefaultPermissionLevel.OP, "Can DoRaidNow...."),
    DoRaidTonight ("com.minecolonies.coremod.DoRaidTonight", DefaultPermissionLevel.OP, "Can DoRaidTonight...."),
    RSReset ("com.minecolonies.coremod.RSReset", DefaultPermissionLevel.OP, "Can RSReset...."),
    BarbarianKill ("com.minecolonies.coremod.BarbarianKill", DefaultPermissionLevel.OP, "Can BarbarianKill...."),
    AnimalKill ("com.minecolonies.coremod.AnimalKill", DefaultPermissionLevel.OP, "Can AnimalKill...."),
    MobKill ("com.minecolonies.coremod.MobKill", DefaultPermissionLevel.OP, "Can MobKill...."),
    ChickenKill ("com.minecolonies.coremod.ChickenKill", DefaultPermissionLevel.OP, "Can ChickenKill...."),
    CowKill ("com.minecolonies.coremod.CowKill", DefaultPermissionLevel.OP, "Can CowKill...."),
    PigKill ("com.minecolonies.coremod.PigKill", DefaultPermissionLevel.OP, "Can PigKill...."),
    SheepKill ("com.minecolonies.coremod.SheepKill", DefaultPermissionLevel.OP, "Can SheepKill...."),
    Scan ("com.minecolonies.coremod.Scan", DefaultPermissionLevel.OP, "Can scan structures."),
    ;

    private String nodeName;
    private DefaultPermissionLevel defaultPermissionLevel;
    private String description;

    private ForgePermissionNodes(String nodeName, DefaultPermissionLevel defaultPermissionLevel, String description)
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
