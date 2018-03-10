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
    ChangeColonyOwner ("com.minecolonies.coremod.ChangeColonyOwner", DefaultPermissionLevel.OP, "Can change owner of a colony."),
    RandomTeleport ("com.minecolonies.coremod.RandomTeleport", DefaultPermissionLevel.OP, "Can randomly teleport...."),
    Backup ("com.minecolonies.coremod.Backup", DefaultPermissionLevel.OP, "Can back up colony data."),
    CitizenInfo ("com.minecolonies.coremod.CitizenInfo", DefaultPermissionLevel.ALL, "Can view detailed citizen info."),
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
