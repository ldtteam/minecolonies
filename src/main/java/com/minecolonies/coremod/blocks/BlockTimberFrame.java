package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemColored;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

import static com.minecolonies.api.util.constant.Suppression.DEPRECATION;

/**
 * Decorative block
 */
public class BlockTimberFrame extends AbstractBlockMinecolonies<BlockTimberFrame>
{

    private static final PropertyEnum<TimberFrameType> TYPE       = PropertyEnum.create("type", TimberFrameType.class);

    /**
     * This blocks name.
     */
    public static final String                      BLOCK_NAME     = "blockTimberFrame";
    /**
     * The hardness this block has.
     */
    private static final float                      BLOCK_HARDNESS = 3F;
    /**
     * The resistance this block has.
     */
    private static final float                      RESISTANCE     = 1F;
    /**
     * Constructor for the TimberFrame
     */
    BlockTimberFrame(final String name)
    {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, TimberFrameType.PLAIN));
        initBlock(name);
    }
    /**
     * initialize the block
     * sets the creative tab, as well as the resistance and the hardness.
     */
    private void initBlock(final String name)
    {
        setRegistryName(name);
        setUnlocalizedName(String.format("%s.%s", Constants.MOD_ID.toLowerCase(Locale.US), name));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
    }

    /**
     * Registery block at gameregistry.
     *
     * @param registry the registry to use.
     */
    @Override
    public void registerItemBlock(final IForgeRegistry<Item> registry)
    {
        registry.register((new ItemColored(this, true)).setRegistryName(this.getRegistryName()));
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    @NotNull
    @Override
    @Deprecated
    public MapColor getMapColor(@NotNull final IBlockState state)
    {
        return state.getValue(TYPE).getMapColor();
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    @NotNull
    @Override
    @Deprecated
    public IBlockState getStateFromMeta(final int meta)
    {
        return this.getDefaultState().withProperty(TYPE, TimberFrameType.byMetadata(meta));
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    @Override
    public int damageDropped(@NotNull final IBlockState state)
    {
        return state.getValue(TYPE).getMetadata();
    }

    @Override
    public void getSubBlocks(@NotNull final Item itemIn, @NotNull final CreativeTabs tab, @NotNull final List<ItemStack> list)
    {
        for (final TimberFrameType type : TimberFrameType.values())
        {
            list.add(new ItemStack(itemIn, 1, type.getMetadata()));
        }
    }
    /**
     * Convert the BlockState into the correct metadata value
     */
    @Override
    public int getMetaFromState(@NotNull final IBlockState state)
    {
        return state.getValue(TYPE).getMetadata();
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, TYPE);
    }

    @SuppressWarnings(DEPRECATION)
    @Override
    public boolean isOpaqueCube(@NotNull final IBlockState state)
    {
        return true;
    }
}
