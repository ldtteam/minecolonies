package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.BlockUtils;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBed;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;


/**
 * Message to replace a block from the world with another one.
 */
public class ReplaceBlockMessage extends AbstractMessage<ReplaceBlockMessage, IMessage>
{
    /**
     * Position to scan from.
     */
    private BlockPos from;

    /**
     * Position to scan to.
     */
    private BlockPos to;

    /**
     * The block to remove from the world.
     */
    private ItemStack blockFrom;

    /**
     * The block to remove from the world.
     */
    private ItemStack blockTo;

    /**
     * Empty constructor used when registering the message.
     */
    public ReplaceBlockMessage()
    {
        super();
    }

    /**
     * Create a message to replace a block from the world.
     * @param pos1 start coordinate.
     * @param pos2 end coordinate.
     * @param blockFrom the block to replace.
     * @param blockTo the block to replace it with.
     */
    public ReplaceBlockMessage(@NotNull final BlockPos pos1, @NotNull final BlockPos pos2, @NotNull final ItemStack blockFrom, @NotNull final ItemStack blockTo)
    {
        super();
        this.from = pos1;
        this.to = pos2;
        this.blockFrom = blockFrom;
        this.blockTo = blockTo;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        from = BlockPosUtil.readFromByteBuf(buf);
        to = BlockPosUtil.readFromByteBuf(buf);
        blockTo = ByteBufUtils.readItemStack(buf);
        blockFrom = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        BlockPosUtil.writeToByteBuf(buf, from);
        BlockPosUtil.writeToByteBuf(buf, to);
        ByteBufUtils.writeItemStack(buf, blockTo);
        ByteBufUtils.writeItemStack(buf, blockFrom);
    }

    @Override
    public void messageOnServerThread(final ReplaceBlockMessage message, final EntityPlayerMP player)
    {
        if (!player.capabilities.isCreativeMode)
        {
            return;
        }

        final World world = player.getServerWorld();
        final FakePlayer fakePlayer = new FakePlayer(player.getServerWorld(), new GameProfile(player.getUniqueID(), "placeStuffForMePl0x"));
        for(int x = Math.min(message.from.getX(), message.to.getX()); x <= Math.max(message.from.getX(), message.to.getX()); x++)
        {
            for (int y = Math.min(message.from.getY(), message.to.getY()); y <= Math.max(message.from.getY(), message.to.getY()); y++)
            {
                for (int z = Math.min(message.from.getZ(), message.to.getZ()); z <= Math.max(message.from.getZ(), message.to.getZ()); z++)
                {
                    final BlockPos here = new BlockPos(x, y, z);
                    final IBlockState blockState = world.getBlockState(here);
                    final ItemStack stack = BlockUtils.getItemStackFromBlockState(blockState);
                    if (correctBlockToRemoveOrReplace(stack, blockState, message.blockFrom))
                    {
                        if ((blockState.getBlock() instanceof BlockDoor && blockState.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.UPPER)
                                || (blockState.getBlock() instanceof BlockBed && blockState.getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD))
                        {
                            continue;
                        }

                        world.setBlockToAir(here);
                        final ItemStack stackToPlace = message.blockTo.copy();
                        stackToPlace.setCount(stackToPlace.getMaxStackSize());
                        fakePlayer.setHeldItem(EnumHand.MAIN_HAND, stackToPlace);

                        if (message.blockTo.getItem() instanceof ItemBed)
                        {
                            fakePlayer.rotationYaw = blockState.getValue(BlockBed.FACING).getHorizontalIndex() * 90;
                        }
                        final EnumFacing facing = (message.blockTo.getItem() instanceof ItemDoor
                                || message.blockTo.getItem() instanceof ItemBed
                                || message.blockTo.getItem() instanceof ItemSlab)? EnumFacing.UP : EnumFacing.NORTH;
                        ForgeHooks.onPlaceItemIntoWorld(stackToPlace, fakePlayer, world, here, facing, 0, 0, 0, EnumHand.MAIN_HAND);

                        final IBlockState newBlockState= world.getBlockState(here);
                        if (newBlockState.getBlock() instanceof BlockStairs && blockState.getBlock() instanceof BlockStairs)
                        {
                            IBlockState transformation = newBlockState.withProperty(BlockStairs.FACING, blockState.getValue(BlockStairs.FACING));
                            transformation = transformation.withProperty(BlockStairs.HALF, blockState.getValue(BlockStairs.HALF));
                            transformation = transformation.withProperty(BlockStairs.SHAPE, blockState.getValue(BlockStairs.SHAPE));
                            world.setBlockState(here, transformation);
                        }
                        else if(newBlockState.getBlock() instanceof BlockHorizontal && blockState.getBlock() instanceof BlockHorizontal
                                && !(blockState.getBlock() instanceof BlockBed))
                        {
                            final IBlockState transformation = newBlockState.withProperty(BlockHorizontal.FACING, blockState.getValue(BlockHorizontal.FACING));
                            world.setBlockState(here, transformation);
                        }
                        else if(newBlockState.getBlock() instanceof BlockDirectional && blockState.getBlock() instanceof BlockDirectional)
                        {
                            final IBlockState transformation = newBlockState.withProperty(BlockDirectional.FACING, blockState.getValue(BlockDirectional.FACING));
                            world.setBlockState(here, transformation);
                        }
                        else if(newBlockState.getBlock() instanceof BlockSlab && blockState.getBlock() instanceof BlockSlab)
                        {
                            final IBlockState transformation;
                            if (blockState.getBlock() instanceof BlockDoubleStoneSlab || blockState.getBlock() instanceof BlockDoubleStoneSlabNew)
                            {
                                transformation = blockState.withProperty(BlockDoubleStoneSlab.VARIANT, newBlockState.getValue(BlockDoubleStoneSlab.VARIANT));
                            }
                            else
                            {
                                transformation = newBlockState.withProperty(BlockSlab.HALF, blockState.getValue(BlockSlab.HALF));
                            }
                            world.setBlockState(here, transformation);
                        }
                        else if(newBlockState.getBlock() instanceof BlockLog && blockState.getBlock() instanceof BlockLog)
                        {
                            final IBlockState transformation = newBlockState.withProperty(BlockLog.LOG_AXIS, blockState.getValue(BlockLog.LOG_AXIS));
                            world.setBlockState(here, transformation);
                        }
                        else if(newBlockState.getBlock() instanceof BlockRotatedPillar && blockState.getBlock() instanceof BlockRotatedPillar)
                        {
                            final IBlockState transformation = newBlockState.withProperty(BlockRotatedPillar.AXIS, blockState.getValue(BlockRotatedPillar.AXIS));
                            world.setBlockState(here, transformation);
                        }
                        else if(newBlockState.getBlock() instanceof BlockTrapDoor && blockState.getBlock() instanceof BlockTrapDoor)
                        {
                            IBlockState transformation = newBlockState.withProperty(BlockTrapDoor.HALF, blockState.getValue(BlockTrapDoor.HALF));
                            transformation = transformation.withProperty(BlockTrapDoor.FACING, blockState.getValue(BlockTrapDoor.FACING));
                            transformation = transformation.withProperty(BlockTrapDoor.OPEN, blockState.getValue(BlockTrapDoor.OPEN));
                            world.setBlockState(here, transformation);
                        }
                        else if(newBlockState.getBlock() instanceof BlockDoor && blockState.getBlock() instanceof BlockDoor)
                        {
                            final IBlockState transformation = newBlockState.withProperty(BlockDoor.FACING, blockState.getValue(BlockDoor.FACING));
                            world.setBlockState(here, transformation);
                        }
                        else if (stackToPlace.getItem() == Items.LAVA_BUCKET)
                        {
                            world.setBlockState(here, Blocks.LAVA.getDefaultState());
                        }
                        else if (stackToPlace.getItem() == Items.WATER_BUCKET)
                        {
                            world.setBlockState(here, Blocks.WATER.getDefaultState());
                        }
                    }
                }
            }
        }
    }

    /**
     * Is this the correct block to remove it or replace it.
     * @param worldStack the world stack to check.
     * @param worldState the world state to check.
     * @param compareStack the comparison stack.
     * @return true if so.
     */
    public static boolean correctBlockToRemoveOrReplace(final ItemStack worldStack, final IBlockState worldState, final ItemStack compareStack)
    {
        return worldStack != null && (worldStack.isItemEqual(compareStack)
                || (compareStack.getItem() == Items.LAVA_BUCKET && (worldState.getBlock() == Blocks.LAVA || worldState.getBlock() == Blocks.FLOWING_LAVA))
                || (compareStack.getItem() == Items.WATER_BUCKET && (worldState.getBlock() == Blocks.WATER || worldState.getBlock() == Blocks.FLOWING_WATER)));
    }
}
