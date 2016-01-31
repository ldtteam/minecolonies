package com.minecolonies.entity.ai;

/**
 * Sets the status of the node
 * AVAILABLE means it can be mined
 * IN_PROGRESS means it is currently being mined
 * COMPLETED means it has been mined and all torches/wood structure has been placed
 * <p>
 * this doesn't have to be final, more stages can be added or this doesn't have to be used
 * <p>
 * Added as an extra class because it made problems on loading time
 */
enum NodeStatus
{
    AVAILABLE,
    IN_PROGRESS,
    COMPLETED,
    LADDER
}
