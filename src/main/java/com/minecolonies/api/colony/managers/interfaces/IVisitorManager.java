package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.entity.visitor.IVisitorType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Visitor manager to manage visiting entities
 */
public interface IVisitorManager extends IEntityManager<IVisitorData>
{
    /**
     * Tick the visitor data of all active citizens.
     */
    boolean tickVisitorData();

    /**
     * Spawns a civilian with the specific civilian data.
     *
     * @param visitorType the visitor type.
     * @param data        Data to use when spawn, null when new generation.
     * @param world       THe world.
     * @param spawnPos    the pos to spawn it at.
     * @return the new civilian.
     */
    IVisitorData spawnOrCreateVisitor(IVisitorType visitorType, IVisitorData data, Level world, BlockPos spawnPos);

    /**
     * Creates visitor data for a new visitor
     *
     * @param visitorType the visitor type.
     * @return new IVisitorData
     */
    IVisitorData createAndRegisterVisitorData(final IVisitorType visitorType);
}
