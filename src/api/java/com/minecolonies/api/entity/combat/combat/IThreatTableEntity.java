package com.minecolonies.api.entity.combat.combat;

/**
 * Entities implement this for the necessary hooks
 */
public interface IThreatTableEntity
{
    /**
     * Get the entities threat table
     *
     * @return
     */
    public ThreatTable getThreatTable();
}
