package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.tileentities.TileEntityDecorationController;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Message to update the decoration control block.
 */
public class DecorationControllUpdateMessage extends AbstractMessage<DecorationControllUpdateMessage, IMessage>
{
    /**
     * The position of the block.
     */
    private BlockPos pos;

    /**
     * The name to set.
     */
    private String name;

    /**
     * Default constructor for forge
     */
    public DecorationControllUpdateMessage() {super();}

    public DecorationControllUpdateMessage(@NotNull final BlockPos pos, final String name)
    {
        super();
        this.pos = pos;
        this.name = name;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        this.name = ByteBufUtils.readUTF8String(buf);
        this.pos = BlockPosUtil.readFromByteBuf(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, this.name);
        BlockPosUtil.writeToByteBuf(buf, this.pos);
    }

    @Override
    public void messageOnServerThread(final DecorationControllUpdateMessage message, final EntityPlayerMP player)
    {
        final TileEntity tileEntity = player.getServerWorld().getTileEntity(message.pos);
        if (tileEntity instanceof TileEntityDecorationController)
        {
            ((TileEntityDecorationController) tileEntity).setSchematicName(message.name);
        }
    }
}
