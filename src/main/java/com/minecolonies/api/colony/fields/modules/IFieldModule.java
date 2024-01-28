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
}
