package com.minecolonies.coremod.blocks;

import com.minecolonies.api.blocks.AbstractBlockMinecolonies;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.tileentities.TileEntityCompostedDirt;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemColored;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Block that if activated with BoneMeal or Compost by an AI will produce flowers by intervals until it deactivates
 */
public class BlockCompostedDirt extends AbstractBlockMinecolonies<BlockCompostedDirt> implements ITileEntityProvider
{
    private static final String BLOCK_NAME = "composted_dirt";

    private static final float BLOCK_HARDNESS = 5f;

    private static final float RESISTANCE = 1f;

    private final static AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0,0,0,1,1,1);

    public BlockCompostedDirt()
    {
        super(Material.GROUND);
        this.initBlock();
    }

    private void initBlock()
    {
        setRegistryName(BLOCK_NAME);
        setTranslationKey(String.format("%s.%s", Constants.MOD_ID.toLowerCase(), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
    }

    @Override
    public void registerItemBlock(final IForgeRegistry<Item> registry)
    {
        registry.register((new ItemColored(this, true)).setRegistryName(this.getRegistryName()));
    }

    @Override
    public boolean isOpaqueCube(final IBlockState state)
    {
        return false;
    }

    @Override
    public boolean hasTileEntity(final IBlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(final World world, final int i)
    {
        return new TileEntityCompostedDirt();
    }

    @NotNull
    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos)
    {
        return BOUNDING_BOX;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState, final IBlockAccess worldIn, final BlockPos pos)
    {
        return BOUNDING_BOX;
    }

    @Override
    public IBlockState getActualState(final IBlockState state, final IBlockAccess worldIn, final BlockPos pos)
    {
        return super.getActualState(state, worldIn, pos);
    }

    @Override
    public boolean isPassable(final IBlockAccess worldIn, final BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean isFullCube(final IBlockState state)
    {
        return true;
    }

    @Override
    public boolean canSustainPlant(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EnumFacing direction, final IPlantable plantable)
    {
        return true;
    }

}
