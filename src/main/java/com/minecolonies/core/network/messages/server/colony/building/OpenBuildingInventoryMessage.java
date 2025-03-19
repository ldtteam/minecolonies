package com.minecolonies.core.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import com.minecolonies.core.tileentities.TileEntityRack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

/**
 * Message sent to open the inventory of a building.
 */
public class OpenBuildingInventoryMessage extends AbstractColonyServerMessage
{
    /**
     * The position of the building.
     */
    private BlockPos buildingPos;

    /**
     * Empty public constructor.
     */
    public OpenBuildingInventoryMessage()
    {
        super();
    }

    /**
     * Creates an open inventory message for a building.
     *
     * @param building the building we're executing on.
     */
    public OpenBuildingInventoryMessage(final IBuildingView building)
    {
        super(building.getColony());
        buildingPos = building.getID();
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        final ServerPlayer player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        final BlockEntity tileEntity = BlockPosUtil.getTileEntity(player.level, buildingPos);
        if (tileEntity instanceof TileEntityRack tileEntityRack)
        {
            NetworkHooks.openScreen(player, tileEntityRack, packetBuffer -> packetBuffer.writeVarInt(colony.getID()).writeBlockPos(tileEntity.getBlockPos()));
        }
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(buildingPos);
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buildingPos = buf.readBlockPos();
    }
}
