package com.minecolonies.api.compatibility.candb;

import mod.chiselsandbits.api.*;

@ChiselsAndBitsAddon
public class ChiselsAndBitsAPI implements IChiselsAndBitsAddon
{
    private static IChiselAndBitsAPI api;

    @Override
    public void onReadyChiselsAndBits(IChiselAndBitsAPI api)
    {
        this.api = api;
    }

    public static IChiselAndBitsAPI instance()
    {
        return api;
    }
}