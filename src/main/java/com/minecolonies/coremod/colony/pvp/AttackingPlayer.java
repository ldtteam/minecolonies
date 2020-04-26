package com.minecolonies.coremod.colony.pvp;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.colony.Colony;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an attacking player.
 */
public class AttackingPlayer
{
    /**
     * The player which is attacking.
     */
    private final EntityPlayer player;

    /**
     * The guards coming with him.
     */
    private final List<AbstractEntityCitizen> guards = new ArrayList<>();

    /**
     * Creates a new Attacking player.
     * @param player the attacking player.
     */
    public AttackingPlayer(final EntityPlayer player)
    {
        this.player = player;
    }

    /**
     * Get the attacking player.
     * @return the EntityPlayer.
     */
    public EntityPlayer getPlayer()
    {
        return player;
    }

    /**
     * Check if guard is part of valid attack.
     *
     * @param citizen the attacking guard.
     * @param colony  the colony.
     * @return true if so.
     */
    public static boolean isValidAttack(final AbstractEntityCitizen citizen, final Colony colony)
    {
        final IColony guardColony = citizen.getCitizenColonyHandler().getColony();
        if (guardColony == null)
        {
            return false;
        }

        if (colony.getPermissions().getRank(guardColony.getPermissions().getOwner()) == Rank.HOSTILE)
        {
            return true;
        }

        return guardColony.getPermissions().getRank(colony.getPermissions().getOwner()) == Rank.HOSTILE
                 && guardColony.getRaiderManager().getColonyRaidLevel() <= colony.getRaiderManager().getColonyRaidLevel() * 2;
    }

    /**
     * Getter for a copy of the guard list.
     * @return an immutable copy the list.
     */
    public List<AbstractEntityCitizen> getGuards()
    {
        return ImmutableList.copyOf(guards);
    }

    /**
     * Adds a new guard to the list.
     * @param guard the guard to add.
     */
    public boolean addGuard(final AbstractEntityCitizen guard)
    {
        if (!guards.contains(guard))
        {
            guards.add(guard);
            return true;
        }
        return false;
    }

    /**
     * Check if the attack is valid.
     * @return true if so.
     */
    public boolean isValidAttack(final Colony colony)
    {
        if (guards.isEmpty())
        {
            return false;
        }

        return AttackingPlayer.isValidAttack(guards.get(0), colony);
    }

    /**
     * Removes a guard to the list.
     * @param guard the guard to remove.
     */
    public void removeGuard(final AbstractEntityCitizen guard)
    {
        guards.add(guard);
    }

    /**
     * Refreshes the list and checks if all are still alive.
     */
    public void refreshList(final Colony colony)
    {
        for (final AbstractEntityCitizen citizen : new ArrayList<>(guards))
        {
            if (citizen.isDead() || !colony.isCoordInColony(colony.getWorld(), citizen.getPosition()))
            {
                guards.remove(citizen);
            }
        }
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final AttackingPlayer that = (AttackingPlayer) o;
        return Objects.equals(player, that.player);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(player);
    }
}
