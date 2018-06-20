package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.coremod.blocks.interfaces.IBlockMinecolonies;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.registries.IForgeRegistry;

public abstract class AbstractBlockTrapdoor<B extends AbstractBlockTrapdoor<B>> extends BlockTrapDoor implements IBlockMinecolonies<B>
{
    public AbstractBlockTrapdoor(final Material materialIn)
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
}
