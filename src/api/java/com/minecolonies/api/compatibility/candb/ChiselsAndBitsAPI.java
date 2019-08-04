package com.minecolonies.api.compatibility.candb;

import mod.chiselsandbits.api.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;

@ChiselsAndBitsAddon
public class ChiselsAndBitsAPI implements IChiselsAndBitsAddon
{
    private static IChiselAndBitsAPI api;

    @Override
    public void onReadyChiselsAndBits(IChiselAndBitsAPI api)
    {
        this.api = api;
    }

    public static ItemStack getBitStack(int stateId)
    {
        try
        {
            return api.getBitItem(Block.getStateById(stateId));
        }
        catch (APIExceptions.InvalidBitItem e)
        {
            return ItemStack.EMPTY;
        }
    }
}