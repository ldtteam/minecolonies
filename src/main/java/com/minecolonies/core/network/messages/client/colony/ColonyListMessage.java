package com.minecolonies.core.network.messages.client.colony;

import com.ldtteam.common.network.AbstractPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.map.WindowColonyMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Add or Update a AbstractBuilding.View to a ColonyView on the client.
 */
public class ColonyListMessage extends AbstractPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forBothSides(Constants.MOD_ID, "colony_list", ColonyListMessage::new);

    /**
     * List of colonies
     */
    private final List<IColony>    colonies;
    private final List<ColonyInfo> colonyInfo;

    /**
     * Creates a message to handle colony views.
     */
    public ColonyListMessage(final List<IColony> colonies)
    {
        super(TYPE);
        this.colonies = colonies;
        this.colonyInfo = null;
    }

    protected ColonyListMessage(@NotNull final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        colonies = null;
        colonyInfo = new ArrayList<>();
        final int count = buf.readInt();
        for (int i = 0; i < count; i++)
        {
            final ColonyInfo info = new ColonyInfo(buf.readInt());
            info.center = buf.readBlockPos();
            info.name = buf.readUtf(32767);
            info.citizencount = buf.readInt();
            info.owner = buf.readUtf(32767);
            colonyInfo.add(info);
        }
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(colonies.size());
        for (final IColony colony : colonies)
        {
            buf.writeInt(colony.getID());
            buf.writeBlockPos(colony.getCenter());
            buf.writeUtf(colony.getName());
            buf.writeInt(colony.getCitizenManager().getCurrentCitizenCount());
            buf.writeUtf(colony.getPermissions().getOwnerName());
        }
    }

    @Override
    protected void onClientExecute(final PlayPayloadContext context, final Player player)
    {
        WindowColonyMap.setColonies(colonyInfo);
    }

    @Override
    protected void onServerExecute(final PlayPayloadContext context, final ServerPlayer player)
    {
        Network.getNetwork().sendToPlayer(new ColonyListMessage(IColonyManager.getInstance().getColonies(player.level())), player);
    }

    public static class ColonyInfo
    {
        private final int      id;
        private       BlockPos center;
        private       String   name;
        private       int      citizencount;
        private       String   owner;

        public ColonyInfo(final int id)
        {
            this.id = id;
        }

        public int getId()
        {
            return id;
        }

        public BlockPos getCenter()
        {
            return center;
        }

        public String getName()
        {
            return name;
        }

        public int getCitizencount()
        {
            return citizencount;
        }

        public String getOwner()
        {
            return owner;
        }
    }
}
