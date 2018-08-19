package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.blocks.AbstractBlockMinecoloniesContainer;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static com.minecolonies.api.util.constant.Suppression.DEPRECATION;
import static net.minecraft.util.EnumFacing.NORTH;
import static net.minecraft.util.EnumFacing.fromAngle;

/**
 * The class handling the fieldBlocks, placement and activation.
 */
public class BlockHutField extends AbstractBlockMinecoloniesContainer<BlockHutField>
{
    /**
     * The position it faces.
     */
    public static final  PropertyDirection FACING           = BlockHorizontal.FACING;
    /**
     * Hardness of the block.
     */
    private static final float             HARDNESS         = 10F;
    /**
     * Resistance of the block.
     */
    private static final float             RESISTANCE       = 10F;
    /**
     * Start of the collision box at y.
     */
    private static final double            BOTTOM_COLLISION = 0.0;

    /**
     * Start of the collision box at x and z.
     */
    private static final double START_COLLISION = 0.1;

    /**
     * End of the collision box.
     */
    private static final double END_COLLISION = 0.9;

    /**
     * Height of the collision box.
     */
    private static final double HEIGHT_COLLISION = 2.5;

    /**
     * Registry name for this block.
     */
    private static final String REGISTRY_NAME = "blockHutField";

    /**
     * Constructor called on block placement.
     */
    public BlockHutField()
    {
        super(Material.WOOD);
        initBlock();
    }

    /**
     * Method called by constructor.
     * Sets basic details of the block.
     */
    private void initBlock()
    {
        setRegistryName(REGISTRY_NAME);
        setTranslationKey(Constants.MOD_ID.toLowerCase(Locale.ENGLISH) + "." + REGISTRY_NAME);
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        //Blast resistance for creepers etc. makes them explosion proof.
        setResistance(RESISTANCE);
        //Hardness of 10 takes a long time to mine to not loose progress.
        setHardness(HARDNESS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, NORTH));
    }

    @NotNull
    @Override
    public EnumBlockRenderType getRenderType(final IBlockState state)
    {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public int getMetaFromState(@NotNull final IBlockState state)
    {
        return state.getValue(FACING).getIndex();
    }

    //todo: remove once we no longer need to support this
    @SuppressWarnings(DEPRECATION)
    @Override
    public boolean isFullCube(final IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isPassable(final IBlockAccess worldIn, final BlockPos pos)
    {
        return false;
    }

    //todo: remove once we no longer need to support this
    @NotNull
    @SuppressWarnings(DEPRECATION)
    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos)
    {
        return new AxisAlignedBB((float) START_COLLISION,
                                  (float) BOTTOM_COLLISION,
                                  (float) START_COLLISION,
                                  (float) END_COLLISION,
                                  (float) HEIGHT_COLLISION,
                                  (float) END_COLLISION);
    }

    //todo: remove once we no longer need to support this
    @SuppressWarnings(DEPRECATION)
    @Override
    public boolean isOpaqueCube(final IBlockState state)
    {
        return false;
    }

    @NotNull
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public boolean onBlockActivated(
                                     final World worldIn,
                                     final BlockPos pos,
                                     final IBlockState state,
                                     final EntityPlayer playerIn,
                                     final EnumHand hand,
                                     final EnumFacing facing,
                                     final float hitX,
                                     final float hitY,
                                     final float hitZ)
    {
        //If the world is server, open the inventory of the field.
        if (!worldIn.isRemote)
        {
            @Nullable final Colony colony = ColonyManager.getColony(worldIn, pos);
            if (colony != null)
            {
                playerIn.openGui(MineColonies.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
                return true;
            }
        }
        return false;
    }

    // =======================================================================
    // ======================= Rendering & IBlockState =======================
    // =======================================================================

    @SuppressWarnings(DEPRECATION)
    @NotNull
    @Override
    public IBlockState getStateForPlacement(
                                             final World worldIn,
                                             final BlockPos pos,
                                             final EnumFacing facing,
                                             final float hitX,
                                             final float hitY,
                                             final float hitZ,
                                             final int meta,
                                             final EntityLivingBase placer)
    {
        @NotNull final EnumFacing enumFacing = (placer == null) ? NORTH : fromAngle(placer.rotationYaw);
        return this.getDefaultState().withProperty(FACING, enumFacing);
    }

    @Override
    public void onBlockPlacedBy(@NotNull final World worldIn, @NotNull final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack stack)
    {
        //Only work on server side.
        if (worldIn.isRemote)
        {
            return;
        }

        if (placer instanceof EntityPlayer)
        {
            @Nullable final Colony colony = ColonyManager.getColony(worldIn, pos);

            if (colony != null)
            {
                final ScarecrowTileEntity scareCrow = (ScarecrowTileEntity) worldIn.getTileEntity(pos);
                if (scareCrow != null)
                {
                    colony.getBuildingManager().addNewField(scareCrow, pos, worldIn);
                }
            }
        }
    }

    @Override
    public void onExplosionDestroy(final World worldIn, final BlockPos pos, final Explosion explosionIn)
    {
        notifyColonyAboutDestruction(worldIn, pos);
        super.onExplosionDestroy(worldIn, pos, explosionIn);
    }

    @Override
    public void onBlockHarvested(final World worldIn, final BlockPos pos, final IBlockState state, final EntityPlayer player)
    {
        notifyColonyAboutDestruction(worldIn, pos);
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public void onPlayerDestroy(final World worldIn, final BlockPos pos, final IBlockState state)
    {
        notifyColonyAboutDestruction(worldIn, pos);
        super.onPlayerDestroy(worldIn, pos, state);
    }

    /**
     * Notify the colony about the destruction of the field.
     * @param worldIn the world.
     * @param pos the position.
     */
    private static void notifyColonyAboutDestruction(final World worldIn, final BlockPos pos)
    {
        @Nullable final Colony colony = ColonyManager.getColony(worldIn, pos);
        if (colony != null)
        {
            colony.getBuildingManager().removeField(pos);
        }
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public boolean hasTileEntity(final IBlockState state)
    {
        return true;
    }

    @NotNull
    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta)
    {
        return new ScarecrowTileEntity();
    }
    // =======================================================================
    // ===================== END of Rendering & Meta-Data ====================
    // =======================================================================
}
