package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.tileentities.TileEntityMultiBlock;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Message class which handles updating the minecolonies multiblock.
 */
public class MultiBlockChangeMessage extends AbstractMessage<MultiBlockChangeMessage, IMessage>
{
    /**
     * The direction it should push or pull rom.
     */
    private EnumFacing direction;

    /**
     * The range it should pull to.
     */
    private int range;

    /**
     * The position of the tileEntity.
     */
    private BlockPos pos;

    /**
     * Empty public constructor.
     */
    public MultiBlockChangeMessage()
    {
        super();
    }

    /**
     * Constructor to create the message.
     * @param pos the position of the block.
     * @param facing the way it should be facing.
     * @param range the range it should work.
     */
    public MultiBlockChangeMessage(final BlockPos pos, final EnumFacing facing, final int range)
    {
        super();
        this.pos = pos;
        this.direction = facing;
        this.range = range;
    }

    /**
     * Transformation from a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        pos = BlockPosUtil.readFromByteBuf(buf);
        direction = EnumFacing.values()[buf.readInt()];
        range = buf.readInt();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        BlockPosUtil.writeToByteBuf(buf, pos);
        buf.writeInt(direction.ordinal());
        buf.writeInt(range);
    }

    /**
     * Executes the message on the server thread.
     * Only if the player has the permission, toggle message.
     *
     * @param message the original message.
     * @param player  the player associated.
     */
    @Override
    public void messageOnServerThread(final MultiBlockChangeMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(player.getServerWorld(), player.getPosition());
        final TileEntity entity = player.getServerWorld().getTileEntity(message.pos);
        if (entity instanceof TileEntityMultiBlock && (colony == null || colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS)))
        {
            ((TileEntityMultiBlock) entity).setDirection(message.direction);
            ((TileEntityMultiBlock) entity).setRange(message.range);
            final IBlockState state = player.getServerWorld().getBlockState(message.pos);
            player.getServerWorld().notifyBlockUpdate(message.pos, state, state, 0x3);
        }
    }
}
