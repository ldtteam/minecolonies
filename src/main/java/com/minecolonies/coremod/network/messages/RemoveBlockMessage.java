package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.BlockUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Message to remove a block from the world.
 */
public class RemoveBlockMessage extends AbstractMessage<RemoveBlockMessage, IMessage>
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
    private ItemStack block;

    /**
     * Empty constructor used when registering the message.
     */
    public RemoveBlockMessage()
    {
        super();
    }

    /**
     * Create a message to remove a block from the world.
     * @param pos1 start coordinate.
     * @param pos2 end coordinate.
     * @param stack the block to remove.
     */
    public RemoveBlockMessage(@NotNull final BlockPos pos1, @NotNull final BlockPos pos2, @NotNull final ItemStack stack)
    {
        super();
        this.from = pos1;
        this.to = pos2;
        this.block = stack;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        from = BlockPosUtil.readFromByteBuf(buf);
        to = BlockPosUtil.readFromByteBuf(buf);
        block = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        BlockPosUtil.writeToByteBuf(buf, from);
        BlockPosUtil.writeToByteBuf(buf, to);
        ByteBufUtils.writeItemStack(buf, block);
    }

    @Override
    public void messageOnServerThread(final RemoveBlockMessage message, final EntityPlayerMP player)
    {
        if (!player.capabilities.isCreativeMode)
        {
            return;
        }
        
        final World world = player.getServerWorld();
        for(int x = Math.min(message.from.getX(), message.to.getX()); x <= Math.max(message.from.getX(), message.to.getX()); x++)
        {
            for (int y = Math.min(message.from.getY(), message.to.getY()); y <= Math.max(message.from.getY(), message.to.getY()); y++)
            {
                for (int z = Math.min(message.from.getZ(), message.to.getZ()); z <= Math.max(message.from.getZ(), message.to.getZ()); z++)
                {
                    final BlockPos here = new BlockPos(x, y, z);
                    final IBlockState blockState = world.getBlockState(here);
                    final ItemStack stack = BlockUtils.getItemStackFromBlockState(blockState);
                    if (ReplaceBlockMessage.correctBlockToRemoveOrReplace(stack, blockState, message.block))
                    {
                        world.setBlockToAir(here);
                    }
                }
            }
        }
    }
}
