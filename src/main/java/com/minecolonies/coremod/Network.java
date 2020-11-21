package com.minecolonies.coremod;

import com.minecolonies.coremod.network.NetworkChannel;

public class Network
{
    /**
     * The network instance.
     */
    public static NetworkChannel network;

    /**
     * Get the network handler.
     *
     * @return the network handler.
     */
    public static NetworkChannel getNetwork()
    {
        if (network == null)
        {
            network = new NetworkChannel("net-channel");
        }
        return network;
    }
}
