package com.minecolonies.api.colony;

import com.minecolonies.api.entity.visitor.AbstractEntityVisitor;
import com.minecolonies.api.entity.visitor.IVisitorExtraData;
import net.minecraft.world.entity.EntityType;

/**
 * Data for colony visitors, based on citizen data
 */
public interface IVisitorData extends ICitizenData
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
     * @param extraData the extra data key.
     * @return the extra data container.
     */
    <T> T getExtraDataValue(final IVisitorExtraData<T> extraData);

    /**
     * Set any bit of additional information for this visitor.
     *
     * @param extraData the extra data key.
     * @param value     the new value for the extra data key.
     */
    <T> void setExtraDataValue(final IVisitorExtraData<T> extraData, final T value);
}
