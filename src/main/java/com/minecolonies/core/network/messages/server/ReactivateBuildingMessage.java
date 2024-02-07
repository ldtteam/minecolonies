package com.minecolonies.core.network.messages.server;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Reactivate a building.
 */
public class ReactivateBuildingMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "reactivate_building", ReactivateBuildingMessage::new);

    /**
     * The position to reactivate it.
     */
    private final BlockPos pos;

    /**
     * Reactivate the building.
     *
     * @param pos the position of the building.
     */
    public ReactivateBuildingMessage(final BlockPos pos)
    {
        super(TYPE);
        this.pos = pos;
    }

    /**
     * Reads this packet from a {@link FriendlyByteBuf}.
     *
     * @param buf The buffer begin read from.
     */
    protected ReactivateBuildingMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        pos = buf.readBlockPos();
    }

    /**
     * Writes this packet to a {@link FriendlyByteBuf}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player)
    {
        final Level world = player.getCommandSenderWorld();
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, pos);
        if (colony != null && colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
        {
            final AbstractBuilding building = (AbstractBuilding) colony.getBuildingManager().getBuilding(pos);
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
