package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.coremod.blocks.interfaces.IBlockMinecolonies;
import com.minecolonies.coremod.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Random;

public abstract class AbstractBlockDoor<B extends AbstractBlockDoor<B>> extends BlockDoor implements IBlockMinecolonies<B>
{
    public AbstractBlockDoor(final Material materialIn, final Block block)
    {
        super(materialIn);
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
    public Item getItemDropped(IBlockState state, Random rand, final int fortune)
    {
        return ModItems.itemCactusDoor;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, final EntityPlayer player)
    {
        return new ItemStack(ModItems.itemCactusDoor);
    }
}
