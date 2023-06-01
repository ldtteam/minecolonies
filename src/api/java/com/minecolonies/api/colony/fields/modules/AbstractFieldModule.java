package com.minecolonies.api.colony.fields.modules;

import com.minecolonies.api.colony.fields.IField;

/**
 * Abstract class for all field modules.
 */
public abstract class AbstractFieldModule implements IFieldModule
{
    /**
     * The field this module belongs to.
     */
    protected IField field;

    @Override
    public IField getField()
    {
        return field;
    }

    @Override
    public IFieldModule setField(final IField field)
    {
        this.field = field;
        return this;
    }
}
