package com.minecolonies.coremod.colony.pvp;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.mobs.util.MobEventsUtils;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents an attacking player.
 */
public class AttackingPlayer
{
    /**
     * The player which is attacking.
     */
    private final PlayerEntity player;

    /**
     * The guards coming with him.
     */
    private final List<EntityCitizen> guards = new ArrayList<>();

    /**
     * Creates a new Attacking player.
     * @param player the attacking player.
     */
    public AttackingPlayer(final PlayerEntity player)
    {
        this.player = player;
    }

    /**
     * Get the attacking player.
     * @return the PlayerEntity.
     */
    public PlayerEntity getPlayer()
    {
        return player;
    }

    /**
     * Getter for a copy of the guard list.
     * @return an immutable copy the list.
     */
    public List<EntityCitizen> getGuards()
    {
        return ImmutableList.copyOf(guards);
    }

    /**
     * Adds a new guard to the list.
     * @param guard the guard to add.
     */
    public boolean addGuard(final EntityCitizen guard)
    {
        if (!guards.contains(guard))
        {
            guards.add(guard);
            return true;
        }
        return false;
    }

    /**
     * Removes a guard to the list.
     * @param guard the guard to remove.
     */
    public void removeGuard(final EntityCitizen guard)
    {
        guards.add(guard);
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
     * Check if guard is part of valid attack.
     * @param citizen the attacking guard.
     * @param colony the colony.
     * @return true if so.
     */
    public static boolean isValidAttack(final EntityCitizen citizen, final Colony colony)
    {
        final Colony guardColony = citizen.getCitizenColonyHandler().getColony();
        if (guardColony == null)
        {
            return false;
        }

        if (colony.getPermissions().getRank(guardColony.getPermissions().getOwner()) == Rank.HOSTILE)
        {
            return true;
        }

        return guardColony.getPermissions().getRank(colony.getPermissions().getOwner()) == Rank.HOSTILE && MobEventsUtils.getColonyRaidLevel(guardColony) <= MobEventsUtils.getColonyRaidLevel(colony) * 2;
    }

    /**
     * Refreshes the list and checks if all are still alive.
     */
    public void refreshList(final Colony colony)
    {
        for (final EntityCitizen citizen : new ArrayList<>(guards))
        {
            if (citizen.isDead || !colony.isCoordInColony(colony.getWorld(), citizen.getPosition()))
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
