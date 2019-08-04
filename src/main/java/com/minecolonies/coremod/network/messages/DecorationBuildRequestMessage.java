package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.blocks.BlockDecorationController;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.tileentities.TileEntityDecorationController;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

/**
 * Adds a entry to the builderRequired map.
 */
public class DecorationBuildRequestMessage implements IMessage
{
    /**
     * The id of the building.
     */
    private BlockPos pos;

    /**
     * The name of the decoration.
     */
    private String name;

    /**
     * The level of the decoration.
     */
    private int level;

    /**
     * The dimension.
     */
    private int dimension;

    /**
     * Empty constructor used when registering the message.
     */
    public DecorationBuildRequestMessage()
    {
        super();
    }

    /**
     * Creates a build request for a decoration.
     * @param pos the position of it.
     * @param name  it's name.
     * @param level the level.
     */
    public DecorationBuildRequestMessage(@NotNull final BlockPos pos, final String name, final int level, final int dimension)
    {
        super();
        this.pos = pos;
        this.name = name;
        this.level = level;
        this.dimension = dimension;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        this.pos = BlockPosUtil.readFromByteBuf(buf);
        this.name = ByteBufUtils.readUTF8String(buf);
        this.level = buf.readInt();
        this.dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        BlockPosUtil.writeToByteBuf(buf, this.pos);
        ByteBufUtils.writeUTF8String(buf, this.name);
        buf.writeInt(this.level);
        buf.writeInt(this.dimension);
    }

    @Override
    public void messageOnServerThread(final DecorationBuildRequestMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromDim(message.dimension, message.pos);
        if (colony == null)
        {
            return;
        }

        //Verify player has permission to change this huts settings
        if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
        {
            return;
        }

        final TileEntity entity = player.getServerWorld().getTileEntity(message.pos);
        if (entity instanceof TileEntityDecorationController)
        {
            final Optional<Map.Entry<Integer, IWorkOrder>> wo = colony.getWorkManager().getWorkOrders().entrySet().stream()
                  .filter(entry -> entry.getValue() instanceof WorkOrderBuildDecoration)
                  .filter(entry -> ((WorkOrderBuildDecoration) entry.getValue()).getBuildingLocation().equals(message.pos)).findFirst();

            if (wo.isPresent())
            {
                colony.getWorkManager().removeWorkOrder(wo.get().getKey());
                return;
            }
            final BlockState state = player.getServerWorld().getBlockState(message.pos);
            final Direction facing = state.getValue(BlockDecorationController.FACING);
            final Direction basic = ((TileEntityDecorationController) entity).getBasicFacing();
            int difference = facing.getHorizontalIndex() - basic.getHorizontalIndex();
            if (difference < 0)
            {
                difference += 4;
            }

            final WorkOrderBuildDecoration order = new WorkOrderBuildDecoration(message.name + message.level,
              message.name + message.level,
              difference,
              message.pos,
              state.getValue(BlockDecorationController.MIRROR));

            if (message.level != ((TileEntityDecorationController) entity).getLevel())
            {
                order.setLevelUp();
            }
            colony.getWorkManager().addWorkOrder(order, false);
        }
    }
}
