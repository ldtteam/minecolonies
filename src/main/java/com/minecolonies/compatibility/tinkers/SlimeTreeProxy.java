package com.minecolonies.compatibility.tinkers;

/**
 * Slime Tree Proxy.
 */
public class SlimeTreeProxy
{
    /**
     * This is the fallback for when tinkers is not present!
     * @return always false.
     */
    protected boolean checkForTinkersSlimeBlock()
    {
        return false;
    }
}
