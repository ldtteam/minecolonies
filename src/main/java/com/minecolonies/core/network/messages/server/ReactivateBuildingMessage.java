package com.minecolonies.core.network.messages.server;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Reactivate a building.
 */
public class ReactivateBuildingMessage implements IMessage
{
    /**
     * The position to reactivate it.
     */
    private BlockPos pos;

    /**
     * Empty constructor used when registering the
     */
    public ReactivateBuildingMessage()
    {
        super();
    }

    /**
     * Reactivate the building.
     *
     * @param pos the position of the building.
     */
    public ReactivateBuildingMessage(final BlockPos pos)
    {
        super();
        this.pos = pos;
    }

    /**
     * Reads this packet from a {@link FriendlyByteBuf}.
     *
     * @param buf The buffer begin read from.
     */
    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        pos = buf.readBlockPos();
    }

    /**
     * Writes this packet to a {@link FriendlyByteBuf}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
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
        final ServerPlayer player = ctxIn.getSender();
        final Level world = player.getCommandSenderWorld();
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, pos);
        if (colony != null && colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
        {
            AbstractBuilding building = (AbstractBuilding) colony.getBuildingManager().getBuilding(pos);
            if (building == null)
            {
                final BlockEntity tileEntity = world.getBlockEntity(pos);
                if (tileEntity instanceof final TileEntityColonyBuilding hut)
                {
                    if (!colony.getBuildingManager().canPlaceAt(tileEntity.getBlockState().getBlock(), pos, player))
                    {
                        return;
                    }

                    hut.reactivate();
                    colony.getBuildingManager().addNewBuilding(hut, world);
                }
            }
        }
    }
}
