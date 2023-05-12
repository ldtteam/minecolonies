package com.minecolonies.coremod.network.messages.server;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.tileentities.AbstractScarecrowTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

public class FieldPlotResizeMessage implements IMessage
{
    /**
     * The new radius of the field plot
     */
    public int size;

    /**
     * The specified direction for the new radius
     */
    public Direction direction;

    /**
     * The position of the scarecrow tile entity
     */
    public BlockPos pos;

    /**
     * Forge default constructor
     */
    public FieldPlotResizeMessage () { super(); }

    /**
     * @param size the new radius of the field plot
     * @param direction the specified direction for the new radius
     * @param pos the position of the scarecrow tile entity
     */
    public FieldPlotResizeMessage (int size, Direction direction, BlockPos pos)
    {
        this.size = size;
        this.direction = direction;
        this.pos = pos;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeInt(this.size);
        buf.writeInt(this.direction.get2DDataValue());
        buf.writeBlockPos(this.pos);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buf)
    {
        this.size = buf.readInt();
        this.direction = Direction.from2DDataValue(buf.readInt());
        this.pos = buf.readBlockPos();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() { return LogicalSide.SERVER; }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer)
    {
        final BlockEntity te = ctxIn.getSender().getCommandSenderWorld().getBlockEntity(this.pos);
        if (te instanceof AbstractScarecrowTileEntity)
        {
            ((AbstractScarecrowTileEntity) te).setRadius(this.direction, this.size);
        }
    }
}
