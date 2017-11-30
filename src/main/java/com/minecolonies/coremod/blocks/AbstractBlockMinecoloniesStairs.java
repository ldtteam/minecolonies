package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Suppression;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import slimeknights.mantle.block.BlockStairsBase;

import static com.minecolonies.api.util.constant.Suppression.DEPRECATION;

public abstract class AbstractBlockMinecoloniesStairs<B extends AbstractBlockMinecoloniesStairs<B>> extends BlockStairs implements IBlockMinecolonies<B>
{
    protected AbstractBlockMinecoloniesStairs(final IBlockState modelState)
    {
        super(modelState);
    }

    /**
     * Registery block at gameregistry.
     *
     * @param registry the registry to use.
     * @return the block itself.
     */
    @Override
    @SuppressWarnings(Suppression.UNCHECKED)
    public B registerBlock(final IForgeRegistry<Block> registry)
    {
        registry.register(this);
        return (B) this;
    }

    /**
     * Registery block at gameregistry.
     *
     * @param registry the registry to use.
     */
    @Override
    public void registerItemBlock(final IForgeRegistry<Item> registry)
    {
        registry.register((new ItemBlock(this)).setRegistryName(this.getRegistryName()));
    }

    @Override
    public boolean doesSideBlockRendering(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EnumFacing face)
    {
        return false;
    }
}
