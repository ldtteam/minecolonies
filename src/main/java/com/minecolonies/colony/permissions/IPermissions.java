package com.minecolonies.colony.permissions;

import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

public interface IPermissions
{
    public boolean hasPermission(EntityPlayer player, Permissions.Action action);

    public Permissions.Rank getRank(UUID rank);
}
