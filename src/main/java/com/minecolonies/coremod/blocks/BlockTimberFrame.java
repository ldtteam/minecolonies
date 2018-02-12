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
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
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

    @Override
    public IBlockState getActualState(final IBlockState state, final IBlockAccess world, final BlockPos pos)
    {
        final IBlockState upState = world.getBlockState(pos.up());
        final IBlockState downState = world.getBlockState(pos.down());
        final boolean up = isConnectable(upState);
        final boolean down = isConnectable(downState);

        if(!isConnectable(state) || state.getValue(TYPE) == TimberFrameType.HORIZONTALNOCAP || (!up && !down))
        {
            return super.getActualState(state, world, pos);
        }
        else
        {
            if(up && down)
            {
                return state.withProperty(TYPE, TimberFrameType.SIDEFRAMED);
            }
            else if(down)
            {
                return state.withProperty(TYPE, TimberFrameType.GATEFRAMED);
            }
            else
            {
                return state.withProperty(TYPE, TimberFrameType.DOWNGATED);
            }
        }
    }

    private static boolean isConnectable(final IBlockState state)
    {
        return state.getBlock() instanceof BlockTimberFrame && (state.getValue(TYPE) == TimberFrameType.SIDEFRAMED
                || state.getValue(TYPE) == TimberFrameType.GATEFRAMED
                || state.getValue(TYPE) == TimberFrameType.DOWNGATED
                || state.getValue(TYPE) == TimberFrameType.HORIZONTALNOCAP);
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
    public MapColor getMapColor(@NotNull final IBlockState state)
    {
        return state.getValue(TYPE).getMapColor();
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    @NotNull
    @Override
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
    public void getSubBlocks(final Item itemIn, final CreativeTabs tab, final NonNullList<ItemStack> list)
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
