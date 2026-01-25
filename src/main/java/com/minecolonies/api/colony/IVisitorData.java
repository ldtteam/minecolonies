package com.minecolonies.api.colony;

import com.minecolonies.api.entity.visitor.IVisitorExtraData;
import com.minecolonies.api.entity.visitor.IVisitorType;
import org.jetbrains.annotations.NotNull;

/**
 * Data for colony visitors, based on citizen data
 */
public interface IVisitorData extends ICitizenData
{
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
