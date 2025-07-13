package com.minecolonies.core.blocks;

import com.minecolonies.api.blocks.AbstractBlockMinecolonies;
import com.minecolonies.api.blocks.interfaces.ITickableBlockMinecolonies;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.items.ItemColonySign;
import com.minecolonies.core.tileentities.TileEntityColonySign;
import com.minecolonies.core.tileentities.TileEntityGrave;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;
import static com.minecolonies.core.items.ItemColonySign.TAG_COLONY;

/**
 * Creates a colony sign block.
 */
public class BlockColonySign extends AbstractBlockMinecolonies<BlockColonySign> implements ITickableBlockMinecolonies
{
    /**
     * Property if it's a sign of two connected colonies or not.
     */
    public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 5F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "colonysign";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * Smaller shape.
     */
    private static final VoxelShape SHAPE = Shapes.box(0.1, 0.1, 0.1, 0.9, 0.9, 0.9);

    /**
     * Constructor for the colony sign.
     */
    public BlockColonySign()
    {
        super(Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(BLOCK_HARDNESS, RESISTANCE).noCollission());
        this.registerDefaultState(this.defaultBlockState().setValue(CONNECTED, false));
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return new ResourceLocation(Constants.MOD_ID, BLOCK_NAME);
    }

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
    {
        return SHAPE;
    }

    @Override
    public void setPlacedBy(@NotNull final Level worldIn, @NotNull final BlockPos pos, final BlockState state, final LivingEntity placer, final ItemStack stack)
    {
        if (worldIn.isClientSide)
        {
            super.setPlacedBy(worldIn, pos, state, placer, stack);
            return;
        }

        final TileEntityColonySign tileEntityColonySign = (TileEntityColonySign) worldIn.getBlockEntity(pos);
        final CompoundTag stackCompound = stack.getOrCreateTag();
        if (!stackCompound.contains(TAG_COLONY))
        {
            return;
        }
        final int colonyId = stackCompound.getInt(TAG_COLONY);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, worldIn.dimension());
        tileEntityColonySign.setColonyAndAnchor(colony, stackCompound.contains(TAG_POS) ? BlockPosUtil.read(stackCompound, TAG_POS) : null);
        super.setPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    public void destroy(final LevelAccessor level, final BlockPos pos, final BlockState state)
    {
        super.destroy(level, pos, state);
    }

    @Override
    public void onRemove(final BlockState currentState, final Level level, final BlockPos pos, final BlockState p_60518_, final boolean p_60519_)
    {
        BlockEntity tileEntity = level.getBlockEntity(pos);
        if (!level.isClientSide && tileEntity instanceof TileEntityColonySign tileEntityColonySign)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(tileEntityColonySign.getColonyId(), level.dimension());
            if (colony != null)
            {
                colony.getConnectionManager().removeConnectionNode(pos);
            }
        }
        super.onRemove(currentState, level, pos, p_60518_, p_60519_);
    }

    @Override
    public RenderShape getRenderShape(final BlockState p_60550_)
    {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(HorizontalDirectionalBlock.FACING, CONNECTED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos blockPos, final BlockState blockState)
    {
        return new TileEntityColonySign(blockPos, blockState);
    }

    @Override
    public void registerBlockItem(final IForgeRegistry<Item> registry, final Item.Properties properties)
    {
        registry.register(getRegistryName(), new ItemColonySign(properties));
    }
}
