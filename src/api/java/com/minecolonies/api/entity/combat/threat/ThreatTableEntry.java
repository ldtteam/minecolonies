package com.minecolonies.api.entity.combat.threat;

import net.minecraft.world.entity.LivingEntity;

import java.util.Objects;

/**
 * Data entry in the threat table
 */
public class ThreatTableEntry
{
    /**
     * Threat value
     */
    private int threat = 10;

    /**
     * Entity which caused the threat
     */
    private final LivingEntity entity;

    /**
     * Time at which it was last seen
     */
    private long lastSeen;

    public ThreatTableEntry(final LivingEntity entity)
    {
        this.entity = Objects.requireNonNull(entity);
        this.lastSeen = entity.level().getGameTime();
    }

    /**
     * Adds threat
     */
    protected void addThreat(final int threat)
    {
        if (threat == 0)
        {
            return;
        }

        this.threat = Math.max(0, this.threat + threat);
        lastSeen = entity.level().getGameTime();
    }

    /**
     * Set the threat value directly
     *
     * @param threat
     */
    protected void setThreat(final int threat)
    {
        this.threat = threat;
    }

    /**
     * Get the threat value
     *
     * @return
     */
    public int getThreat()
    {
        return threat;
    }

    /**
     * Get the target entity
     *
     * @return target
     */
    public LivingEntity getEntity()
    {
        return entity;
    }

    /**
     * Get the worldtime of where it was last seen
     *
     * @return
     */
    public long getLastSeen()
    {
        return lastSeen;
    }

    /**
     * Set the world time of when it was last seen
     *
     * @param gameTime
     */
    public void setLastSeen(final long gameTime)
    {
        this.lastSeen = gameTime;
    }
}
