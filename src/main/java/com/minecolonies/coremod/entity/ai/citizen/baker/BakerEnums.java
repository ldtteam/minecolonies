package com.minecolonies.coremod.entity.ai.citizen.baker;

/**
 * All enums the baker needs.
 */
public final class BakerEnums
{
    /**
     * Private constructor to hide the implicit one.
     */
    private BakerEnums()
    {
        /**
         * Intentionally left empty.
         */
    }

    public enum ProductState
    {
        UNCRAFTED,
        RAW,
        PREPARED,
        BAKING,
        BAKED
    }
}
