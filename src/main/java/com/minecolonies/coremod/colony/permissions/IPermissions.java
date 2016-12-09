package com.minecolonies.coremod.colony.permissions;

import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Permission interface.
 */
public interface IPermissions
{
    /**
     * Returns whether the player has the permission for an action.
     *
     * @param player {@link EntityPlayer} player.
     * @param action {@link com.minecolonies.coremod.colony.permissions.Permissions.Action} action.
     * @return true if has permission, otherwise false.
     */
    boolean hasPermission(EntityPlayer player, Permissions.Action action);

    /**
     * Returns whether the player is a member of the colony.
     *
     * @param player {@link EntityPlayer} to check.
     * @return true if the player is a member of the colony.
     */
    boolean isColonyMember(EntityPlayer player);

    /**
     * Get the rank of a UUID.
     *
     * @param player UUID to check rank of.
     * @return Rank of the player.
     */
    @NotNull
    Permissions.Rank getRank(UUID player);
}
