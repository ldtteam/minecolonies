package com.minecolonies.core.entity.ai.workers.util;

/**
 * The different stages a StructureIterator building process can be in.
 */
public enum BuildingProgressStage
{
    CLEAR,
    BUILD_SOLID,
    CLEAR_WATER,
    CLEAR_NON_SOLIDS,
    DECORATE,
    SPAWN,
    REMOVE,
    REMOVE_WATER,
    WEAK_SOLID,
}
