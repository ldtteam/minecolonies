package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.IVisitorData;

/**
 * Visitor manager to manage visiting entities
 */
public interface IVisitorManager extends IEntityManager
{
    /**
     * Gets the visitor data for the given citizen
     *
     * @param citizenId id to get data for
     * @param <T>       data type
     * @return visitor data
     */
    <T extends IVisitorData> T getVisitor(int citizenId);
}
