package com.minecolonies.api.entity.visitor;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

/**
 * Abstract class for visitor entities.
 */
public abstract class AbstractEntityVisitor extends AbstractEntityCitizen
{
    /**
     * Constructor for a new citizen typed entity.
     *
     * @param type  the Entity type.
     * @param world the world.
     */
    protected AbstractEntityVisitor(final EntityType<? extends PathfinderMob> type, final Level world)
    {
        super(type, world);
    }
}