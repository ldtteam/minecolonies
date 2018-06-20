package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.coremod.blocks.interfaces.IBlockMinecolonies;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractBlockSlab<B extends AbstractBlockSlab<B>> extends BlockSlab implements IBlockMinecolonies<B>
{
    private static final PropertyBool UGH = PropertyBool.create("ugh");

    /**
     * Constructor of abstract class.
     * @param materialIn the input material.
     */
    public AbstractBlockSlab(final Material materialIn)
    {
        super(materialIn);
    }

    @Override
    @SuppressWarnings(Suppression.UNCHECKED)
    public B registerBlock(final IForgeRegistry<Block> registry)
    {
        registry.register(this);
        return (B) this;
    }

    @Override
    public void registerItemBlock(final IForgeRegistry<Item> registry)
    {
        /**
         * Ignore, we do our own soup.
         */
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    @Override
    public int getMetaFromState(final IBlockState state)
    {
        if (isDouble())
        {
            return 0;
        }

        if (state.getValue(HALF) == EnumBlockHalf.TOP)
        {
            return 1;
        }
        return 2;
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    @Override
    public IBlockState getStateFromMeta(final int meta)
    {
        if (meta == 0)
        {
            return this.getDefaultState();
        }

        if (meta == 1)
        {
            return this.getDefaultState().withProperty(HALF, EnumBlockHalf.TOP);
        }
        return this.getDefaultState().withProperty(HALF, EnumBlockHalf.BOTTOM);
    }

    /**
     * There is only one variant so only one name.
     *
     * @param i the meta.
     * @return the name.
     */
    @NotNull
    public String getUnlocalizedName(final int i)
    {
        return getUnlocalizedName();
    }

    @NotNull
    @Override
    public IProperty<?> getVariantProperty()
    {
        return UGH;
    }

    @NotNull
    @Override
    public Comparable<?> getTypeForItem(@NotNull final ItemStack itemStack)
    {
        return true;
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return this.isDouble() ? new BlockStateContainer(this, UGH) : new BlockStateContainer(this, HALF, UGH);
    }
}
