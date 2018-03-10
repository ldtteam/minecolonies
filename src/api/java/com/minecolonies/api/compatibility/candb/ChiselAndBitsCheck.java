package com.minecolonies.api.compatibility.candb;

import mod.chiselsandbits.chiseledblock.BlockChiseled;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.shared.TinkerCommons;

/**
 * This class is to store a check to see if a block is a chiselsandbits block.
 */
public final class ChiselAndBitsCheck extends ChiselAndBitsProxy
{
    private static final String CANDB = "chiselsandbits";

    /**
     * Check if block is c&b block.
     *
     * @param block the block.
     * @return if the block is a c&b block.
     */
    public static boolean isChiselAndBitsBlock(@NotNull final Block block)
    {
        return new ChiselAndBitsCheck().checkForChiselAndBitsBlock(block);
    }

    /**
     * Check if block is c&b block.
     *
     * @param block the block.
     * @return if the block is a c&b block.
     */
    @Override
    @Optional.Method(modid = CANDB)
    public boolean checkForChiselAndBitsBlock(@NotNull final Block block)
    {
        return block instanceof BlockChiseled;
    }
}