package com.minecolonies.coremod.blocks.interfaces;

import com.minecolonies.coremod.entity.EntityCitizen;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Interface class of a required class any seat block has to implement for
 * Minecolonies to be able to use as a seat.
 *
 * @author kevin
 *
 * @param <B>
 */
public interface IBlockMinecoloniesSeat<B extends IBlockMinecoloniesSeat<B>>
{
    /**
     * Registery block at gameregistry.
     *
     * @param registry the registry to use.
     * @return the block itself.
     */
    B registerBlock(IForgeRegistry<Block> registry);

    /**
     * Registery block at gameregistry.
     *
     * @param registry the registry to use.
     */
    void registerItemBlock(IForgeRegistry<Item> registry);

    /**
     * Call to have the citizen stand from the sitting position.
     *
     * @param world pointer to the world
     */
    void dismountSeat(World world);

    /**
     * Call to have the citizen to start sitting.
     *
     * @param world  pointer to the world
     * @param chairPosition Block Pos of the chair
     * @param citizen   pointer to the citizen that will sit
     * @return  indicate if the citizen started sitting.
     */
    boolean startSeating(World world, BlockPos chairPosition, EntityCitizen citizen);

    /**
     * @return indicates if the seat is being used.
     */
    boolean isSeatBeingUsed();

}
