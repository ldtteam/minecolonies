package com.minecolonies.coremod.network.messages.server.colony.building.fields;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.FarmField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

/**
 * Message to change the farmer field plot size.
 */
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
    public FieldPlotResizeMessage() {super();}

    /**
     * @param size      the new radius of the field plot
     * @param direction the specified direction for the new radius
     * @param pos       the position of the scarecrow tile entity
     */
    public FieldPlotResizeMessage(int size, Direction direction, BlockPos pos)
    {
        this.size = size;
        this.direction = direction;
        this.pos = pos;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.size);
        buf.writeInt(this.direction.get2DDataValue());
        buf.writeBlockPos(this.pos);
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        this.size = buf.readInt();
        this.direction = Direction.from2DDataValue(buf.readInt());
        this.pos = buf.readBlockPos();
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        if (isLogicalServer && ctxIn.getSender() != null)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(ctxIn.getSender().level, pos);
            if (colony != null)
            {
                colony.getBuildingManager().updateField(FarmField.class, this.pos, field -> {
                    field.setRadius(this.direction, this.size);
                    BlockState state = colony.getWorld().getBlockState(this.pos);
                    colony.getWorld().sendBlockUpdated(this.pos, state, state, 2);
                });
            }
        }
    }
}
