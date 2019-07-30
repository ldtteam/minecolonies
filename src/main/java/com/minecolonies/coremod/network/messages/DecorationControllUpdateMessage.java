package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.blocks.BlockDecorationController;
import com.minecolonies.coremod.tileentities.TileEntityDecorationController;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import org.jetbrains.annotations.NotNull;

/**
 * Message to update the decoration control block.
 */
public class DecorationControllUpdateMessage implements IMessage
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
     * The level to set.
     */
    private int level;

    /**
     * Default constructor for forge
     */
    public DecorationControllUpdateMessage() {super();}

    /**
     * Constructor for the decoration controller update message.
     * @param pos the position of the controller.
     * @param name the name to set.
     * @param level the new level to set.
     */
    public DecorationControllUpdateMessage(@NotNull final BlockPos pos, final String name, final int level)
    {
        super();
        this.pos = pos;
        this.name = name;
        this.level = level;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        this.name = buf.readString();
        this.pos = buf.readBlockPos();
        this.level = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeString(this.name);
        buf.writeBlockPos(this.pos);
        buf.writeInt(this.level);
    }

    @Override
    public void messageOnServerThread(final DecorationControllUpdateMessage message, final ServerPlayerEntity player)
    {
        final TileEntity tileEntity = player.getServerWorld().getTileEntity(message.pos);
        if (tileEntity instanceof TileEntityDecorationController)
        {
            final BlockState state = player.getServerWorld().getBlockState(message.pos);
            final Direction basicFacing = state.getValue(BlockDecorationController.FACING);
            ((TileEntityDecorationController) tileEntity).setSchematicName(message.name);
            ((TileEntityDecorationController) tileEntity).setLevel(message.level);
            ((TileEntityDecorationController) tileEntity).setBasicFacing(basicFacing);
        }
    }
}
