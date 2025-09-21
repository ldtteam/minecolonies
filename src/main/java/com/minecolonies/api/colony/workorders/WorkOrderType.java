package com.minecolonies.api.colony.workorders;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Types of workorders
 */
public enum WorkOrderType
{
    BUILD(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILD_COMPLETE),
    UPGRADE(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILD_COMPLETE),
    REPAIR(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_REPAIRING_COMPLETE),
    REMOVE(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_DECONSTRUCTION_COMPLETE);

    /**
     * Translation message for completion
     */
    private final String completionMessageID;

    WorkOrderType(final String completionMessage)
    {
        this.completionMessageID = completionMessage;
    }

    /**
     * Translation constant for the message type
     *
     * @return
     */
    public String getCompletionMessageID()
    {
        return completionMessageID;
    }
}
