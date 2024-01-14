package com.minecolonies.core.colony.fields.modules;

import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.modules.IFieldModule;

/**
 * Abstract class for all field modules.
 */
public abstract class AbstractFieldModule implements IFieldModule
{
    /**
     * The field this module belongs to.
     */
    protected IField field;

    /**
     * Default constructor.
     *
     * @param field the field instance this module is working on.
     */
    protected AbstractFieldModule(final IField field)
    {
        this.field = field;
    }

    @Override
    public IField getField()
    {
        return field;
    }
}
