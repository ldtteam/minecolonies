package com.minecolonies.colony.permissions;

import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

public interface IPermissions
{
    boolean hasPermission(EntityPlayer player, Permissions.Action action);

    Permissions.Rank getRank(UUID rank);
}
