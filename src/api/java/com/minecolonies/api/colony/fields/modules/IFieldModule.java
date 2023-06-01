package com.minecolonies.api.colony.fields.modules;

import com.minecolonies.api.colony.fields.IField;

/**
 * Default interface for all field modules.
 */
public interface IFieldModule
{
    /**
     * Get the field of the module.
     *
     * @return the field.
     */
    IField getField();

    /**
     * Set the field of the module.
     *
     * @param field the field to set.
     * @return the module itself.
     */
    IFieldModule setField(final IField field);
}
