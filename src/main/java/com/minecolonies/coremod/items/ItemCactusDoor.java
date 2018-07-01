package com.minecolonies.coremod.items;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemCactusDoor extends Item
{
    private final Block block;

    ItemCactusDoor(final Block block, final String name)
    {
        super();
        this.block = block;
        setRegistryName(name);
        super.setUnlocalizedName(Constants.MOD_ID.toLowerCase() + "." + name);
        this.setCreativeTab(ModCreativeTabs.MINECOLONIES);

    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, final EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (facing != EnumFacing.UP)
        {
            return EnumActionResult.FAIL;
        }
        else
            {
            final IBlockState iblockstate = worldIn.getBlockState(pos);
            final Block blockIn = iblockstate.getBlock();
            if (!blockIn.isReplaceable(worldIn, pos))
            {
                pos = pos.offset(facing);
            }

            final ItemStack itemstack = player.getHeldItem(hand);
            if (player.canPlayerEdit(pos, facing, itemstack) && this.block.canPlaceBlockAt(worldIn, pos))
            {
                EnumFacing enumfacing = EnumFacing.fromAngle((double)player.rotationYaw);
                final int i = enumfacing.getFrontOffsetX();
                final int j = enumfacing.getFrontOffsetZ();
                final boolean flag = ((i < 0) && (hitZ < 0.5F) || (i > 0) && (hitZ > 0.5F) || (j < 0) && (hitX > 0.5F) || (j > 0) && (hitX < 0.5F));
                placeDoor(worldIn, pos, enumfacing, this.block, flag);
                SoundType soundtype = worldIn.getBlockState(pos).getBlock().getSoundType(worldIn.getBlockState(pos), worldIn, pos, player);
                worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                itemstack.shrink(1);
                return EnumActionResult.SUCCESS;
            }
            else
                {
                return EnumActionResult.FAIL;
            }
        }
    }

    public static void placeDoor(World worldIn, BlockPos pos, EnumFacing facing, final Block door, boolean isRightHinge)
    {
        final BlockPos blockpos = pos.offset(facing.rotateY());
        final BlockPos blockpos1 = pos.offset(facing.rotateYCCW());
        final int i = (worldIn.getBlockState(blockpos1).isNormalCube() ? 1 : 0) + (worldIn.getBlockState(blockpos1.up()).isNormalCube() ? 1 : 0);
        final int j = (worldIn.getBlockState(blockpos).isNormalCube() ? 1 : 0) + (worldIn.getBlockState(blockpos.up()).isNormalCube() ? 1 : 0);
        final boolean flag = worldIn.getBlockState(blockpos1).getBlock() == door || worldIn.getBlockState(blockpos1.up()).getBlock() == door;
        final boolean flag1 = worldIn.getBlockState(blockpos).getBlock() == door || worldIn.getBlockState(blockpos.up()).getBlock() == door;
        if ((!flag || flag1) && (j <= i))
        {
            if ((flag1 && !flag) || (j < i))
            {
                isRightHinge = false;
            }
        }
        else
            {
            isRightHinge = true;
        }

        final BlockPos blockpos2 = pos.up();
        final boolean flag2 = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(blockpos2);
        IBlockState iblockstate = door.getDefaultState().withProperty(BlockDoor.FACING, facing).withProperty(BlockDoor.HINGE,
                isRightHinge ? BlockDoor.EnumHingePosition.RIGHT : BlockDoor.EnumHingePosition.LEFT).withProperty(BlockDoor.POWERED, flag2).withProperty(BlockDoor.OPEN, flag2);
        worldIn.setBlockState(pos, iblockstate.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER), 2);
        worldIn.setBlockState(blockpos2, iblockstate.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER), 2);
        worldIn.notifyNeighborsOfStateChange(pos, door, false);
        worldIn.notifyNeighborsOfStateChange(blockpos2, door, false);
    }
}
