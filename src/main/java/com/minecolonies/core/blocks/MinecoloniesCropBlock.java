package com.minecolonies.core.blocks;

import com.minecolonies.api.blocks.AbstractBlockMinecolonies;
import com.minecolonies.core.items.ItemCrop;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract Minecolonies crop type. We have our own to avoid cheesing the crop.s
 */
public class MinecoloniesCropBlock extends AbstractBlockMinecolonies<MinecoloniesCropBlock>
{
    public static String BELL_PEPPER = "bell_pepper";
    public static String CABBAGE = "cabbage";
    public static String CHICKPEA = "chickpea";
    public static String DURUM = "durum";
    public static String EGGPLANT = "eggplant";
    public static String GARLIC = "garlic";
    public static String ONION = "onion";
    public static String SOYBEAN = "soybean";
    public static String TOMATO = "tomato";
    public static String RICE = "rice";

    public static final  IntegerProperty AGE = IntegerProperty.create("age", 0, 6);
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[] {
      Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0)};

    private final Block preferredFarmland;

    private final String        blockName;
    private final TagKey<Biome> preferredBiome;

    //todo general:
    //todo: add food items
    //todo: create the Chef position in the kitchen and move over from restaurant.
    //todo add recipes to the baker/chef

    /**
     * Constructor to create a block of this type.
     * @param blockName the block id.
     */
    public MinecoloniesCropBlock(final String blockName, final Block preferredFarmland, @Nullable final TagKey<Biome> preferredBiome)
    {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY));
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
        this.blockName = blockName;
        this.preferredFarmland = preferredFarmland;
        this.preferredBiome = preferredBiome;
    }

    @NotNull
    @Override
    public VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx)
    {
        return SHAPE_BY_AGE[state.getValue(AGE)];
    }

    /**
     * Check if the block is of max age.
     * @param state the state its at.
     * @return true if max age.
     */
    public final boolean isMaxAge(BlockState state)
    {
        return state.getValue(AGE) >= this.getMaxAge();
    }

    /**
     * Get the default max crop age.
     * @return the max age.
     */
    protected int getMaxAge()
    {
        return 6;
    }

    /**
     * Method to be called to attempt grow this crop.
     * @param state current state.
     * @param level level its in.
     * @param pos pos its at.
     */
    public void attemptGrow(BlockState state, ServerLevel level, BlockPos pos)
    {
        if (level.isAreaLoaded(pos, 1))
        {
            if (level.getRawBrightness(pos, 0) >= 9)
            {
                int i = state.getValue(AGE);
                if (i < this.getMaxAge())
                {
                    level.setBlock(pos, state.setValue(AGE, (i + 1)), 2);
                }
            }
        }
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, LevelReader level, @NotNull BlockPos pos)
    {
        return (level.getRawBrightness(pos, 0) >= 8 || level.canSeeSky(pos)) && super.canSurvive(state, level, pos) && level.getBlockState(pos.below()).getBlock() == preferredFarmland && (preferredBiome == null || level.getBiome(pos).is(preferredBiome));
    }

    @Override
    public InteractionResult use(
      final @NotNull BlockState state,
      final @NotNull Level level,
      final @NotNull BlockPos pos,
      final Player player,
      final @NotNull InteractionHand hand,
      final @NotNull BlockHitResult hitResult)
    {
        if (player.getItemInHand(hand).getItem() == ModItems.compost)
        {
            if (!level.isClientSide)
            {
                attemptGrow(state, (ServerLevel) level, pos);
            }
            if (!player.isCreative())
            {
                player.getItemInHand(hand).shrink(1);
            }
            return InteractionResult.CONSUME;
        }
        return super.use(state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> creator)
    {
        creator.add(AGE);
    }

    @NotNull
    @Override
    public BlockState updateShape(BlockState state, @NotNull Direction dir, @NotNull BlockState newState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos)
    {
        return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, dir, newState, level, pos, neighborPos);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos)
    {
        return state.getFluidState().isEmpty();
    }

    @Override
    public boolean isPathfindable(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull PathComputationType pathComputationType)
    {
        return pathComputationType == PathComputationType.AIR && !this.hasCollision || super.isPathfindable(state, level, pos, pathComputationType);
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return new ResourceLocation(Constants.MOD_ID, blockName);
    }

    @Override
    public void registerBlockItem(final IForgeRegistry<Item> registry, final Item.Properties properties)
    {
        registry.register(getRegistryName(), new ItemCrop(this, properties, preferredBiome));
    }

    /**
     * Get the preferred farmland for this crop.
     * @return the preferred farmland.
     */
    public Block getPreferredFarmland()
    {
        return preferredFarmland;
    }
}
