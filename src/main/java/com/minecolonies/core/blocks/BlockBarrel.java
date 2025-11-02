package com.minecolonies.core.blocks;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.blocks.interfaces.IMinecoloniesBlock;
import com.minecolonies.api.blocks.interfaces.IMinecoloniesTickableBlock;
import com.minecolonies.api.blocks.types.BarrelType;
import com.minecolonies.api.tileentities.AbstractTileEntityBarrel;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.tileentities.TileEntityBarrel;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class BlockBarrel extends HorizontalDirectionalBlock implements EntityBlock, IMinecoloniesTickableBlock, IMinecoloniesBlock<BlockItem>
{
    public static final EnumProperty<BarrelType> VARIANT = EnumProperty.create("variant", BarrelType.class);

    public static final MapCodec<BlockBarrel> CODEC = simpleCodec(BlockBarrel::new);

    /**
     * The hardness this block has.
     */
    private static final float  BLOCK_HARDNESS = 5F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "barrel_block";

    /**
     * The resistance this block has.
     */
    private static final float  RESISTANCE     = 1F;

    public BlockBarrel()
    {
        this(Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(BLOCK_HARDNESS, RESISTANCE));
    }

    public BlockBarrel(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(VARIANT, BarrelType.ZERO));
    }

    @Override
    @NotNull
    protected MapCodec<BlockBarrel> codec()
    {
        return CODEC;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return new ResourceLocation(Constants.MOD_ID, BLOCK_NAME);
    }

    @Override
    public BlockItem createBlockItem()
    {
        return new BlockItem(this, new Item.Properties());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, VARIANT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos blockPos, @NotNull final BlockState blockState)
    {
        return new TileEntityBarrel(blockPos, blockState);
    }

    @NotNull
    @Override
    public ItemInteractionResult useItemOn(
      final ItemStack stack,
      final BlockState state,
      final Level worldIn,
      final BlockPos pos,
      final Player player,
      final InteractionHand hand,
      final BlockHitResult ray)
    {
        final ItemStack itemstack = player.getInventory().getSelected();
        final BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof TileEntityBarrel tileEntityBarrel && !worldIn.isClientSide)
        {
            tileEntityBarrel.useBarrel(player, itemstack, ray.getDirection());
            tileEntityBarrel.updateBlock(worldIn);
        }

        return ItemInteractionResult.SUCCESS;
    }

    @NotNull
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
    {
        return Shapes.box(0, 0, 0, 1, 1.5, 1);
    }

    /**
     * Convert the BlockState into the correct metadata value.
     *
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @NotNull
    @Override
    @Deprecated
    public BlockState rotate(@NotNull final BlockState state, final Rotation rot)
    {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @NotNull
    @Override
    @Deprecated
    public BlockState mirror(@NotNull final BlockState state, final Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public boolean canSurvive(final BlockState state, final LevelReader worldIn, final BlockPos pos)
    {
        return !worldIn.isEmptyBlock(pos.below())
                 && worldIn.getBlockState(pos.below()).getBlock() != ModBlocks.blockBarrel;
    }

    public static BlockState changeStateOverFullness(@NotNull final AbstractTileEntityBarrel te, @NotNull final BlockState blockState)
    {
        /*
         * 12.8 -> the number of items needed to go up on a state (having 6 filling states)
         * So item/12.8 -> meta of the state we should get
         */
        BarrelType type = BarrelType.byMetadata((int) Math.round(te.getItems() / 12.8));

        /*
         * We check if the barrel is marked as empty, but it has items inside. If so, means that it
         * does not have all the items needed to go on TWENTY state, but we need to mark it so the player
         * knows it have some items inside
         */
        if (type.equals(BarrelType.ZERO) && te.getItems() > 0)
        {
            type = BarrelType.TWENTY;
        }
        else if (te.getItems() == AbstractTileEntityBarrel.MAX_ITEMS)
        {
            type = BarrelType.WORKING;
        }
        if (te.isDone())
        {
            type = BarrelType.DONE;
        }

        return blockState.setValue(VARIANT, type).setValue(FACING, blockState.getValue(FACING));
    }
}
