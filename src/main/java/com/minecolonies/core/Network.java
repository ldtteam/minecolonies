package com.minecolonies.core;

import com.minecolonies.core.network.NetworkChannel;

public class Network
{
    /**
     * The network instance.
     */
    private static NetworkChannel network;

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
