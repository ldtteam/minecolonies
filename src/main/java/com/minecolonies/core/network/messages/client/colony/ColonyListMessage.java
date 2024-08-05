package com.minecolonies.core.network.messages.client.colony;

import com.ldtteam.common.network.AbstractPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.map.WindowColonyMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
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

    public ColonyListMessage()
    {
        this(Collections.emptyList());
    }

    /**
     * Creates a message to handle colony views.
     */
    public ColonyListMessage(final List<IColony> colonies)
    {
        super(TYPE);
        this.colonies = colonies;
        this.colonyInfo = null;
    }

    protected ColonyListMessage(@NotNull final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        colonies = null;
        colonyInfo = buf.readList(b -> {
            final ColonyInfo info = new ColonyInfo(b.readInt());
            info.center = b.readBlockPos();
            info.name = b.readUtf(32767);
            info.citizencount = b.readInt();
            info.owner = b.readUtf(32767);
            return info;
        });
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        buf.writeCollection(colonies, (b, colony) ->{
            b.writeInt(colony.getID());
            b.writeBlockPos(colony.getCenter());
            b.writeUtf(colony.getName());
            b.writeInt(colony.getCitizenManager().getCurrentCitizenCount());
            b.writeUtf(colony.getPermissions().getOwnerName());
        });
    }

    @Override
    protected void onClientExecute(final IPayloadContext context, final Player player)
    {
        WindowColonyMap.setColonies(colonyInfo);
    }

    @Override
    protected void onServerExecute(final IPayloadContext context, final ServerPlayer player)
    {
        new ColonyListMessage(IColonyManager.getInstance().getColonies(player.level())).sendToPlayer(player);
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
