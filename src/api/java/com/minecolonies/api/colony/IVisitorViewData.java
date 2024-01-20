package com.minecolonies.api.colony;

import com.minecolonies.api.entity.visitor.AbstractEntityVisitor;
import com.minecolonies.api.entity.visitor.IVisitorExtraData;
import com.minecolonies.api.entity.visitor.IVisitorType;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

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
     * Get the type of the visitor.
     *
     * @return the visitor type.
     */
    @NotNull
    IVisitorType getVisitorType();

    /**
     * Get any bit of additional information for this visitor.
     *
     * @return the extra data container.
     */
    <T> T getExtraDataValue(final IVisitorExtraData<T> extraData);
}
