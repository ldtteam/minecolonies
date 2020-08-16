package com.minecolonies.coremod.network.messages.server;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.tileentities.AbstractScarecrowTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

public class FieldPlotResizeMessage implements IMessage
{
    /**
     * The new radius of the field plot
     */
    public final int size;

    /**
     * The specified direction for the new radius
     */
    public final Direction direction;

    /**
     * The position of the scarecrow tile entity
     */
    public final BlockPos pos;

    /**
     * Forge default constructor
     */
    public FieldPlotResizeMessage(final PacketBuffer buf)
    {
        this.size = buf.readInt();
        this.direction = Direction.byHorizontalIndex(buf.readInt());
        this.pos = buf.readBlockPos();
    }

    /**
     * @param size      the new radius of the field plot
     * @param direction the specified direction for the new radius
     * @param pos       the position of the scarecrow tile entity
     */
    public FieldPlotResizeMessage(final int size, final Direction direction, final BlockPos pos)
    {
        this.size = size;
        this.direction = direction;
        this.pos = pos;
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeInt(this.size);
        buf.writeInt(this.direction.getHorizontalIndex());
        buf.writeBlockPos(this.pos);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final TileEntity te = ctxIn.getSender().getEntityWorld().getTileEntity(this.pos);
        if (te instanceof AbstractScarecrowTileEntity)
        {
            ((AbstractScarecrowTileEntity) te).setRadius(this.direction, this.size);
        }
    }
}
