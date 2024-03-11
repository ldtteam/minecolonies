package com.minecolonies.api.colony;

import com.minecolonies.api.entity.visitor.AbstractEntityVisitor;
import com.minecolonies.api.entity.visitor.IVisitorExtraData;
import net.minecraft.world.entity.EntityType;

/**
 * View data for visitors
 */
public interface IVisitorViewData extends ICitizenDataView
{
    /**
     * Get the entity type for this visitor.
     *
     * @return the entity type.
     */
    EntityType<? extends AbstractEntityVisitor> getEntityType();

    /**
     * Get any bit of additional information for this visitor.
     *
     * @return the extra data container.
     */
    <T> T getExtraDataValue(final IVisitorExtraData<T> extraData);
}
