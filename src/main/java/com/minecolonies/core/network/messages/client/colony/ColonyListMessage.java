package com.minecolonies.core.network.messages.client.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.map.WindowColonyMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Add or Update a AbstractBuilding.View to a ColonyView on the client.
 */
public class ColonyListMessage implements IMessage
{
    /**
     * List of colonies
     */
    List<IColony>    colonies   = new ArrayList<>();
    List<ColonyInfo> colonyInfo = new ArrayList<>();

    /**
     * Empty constructor used when registering the
     */
    public ColonyListMessage()
    {
        super();
    }

    /**
     * Creates a message to handle colony views.
     */
    public ColonyListMessage(final List<IColony> colonies)
    {
        super();
        this.colonies = colonies;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
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
    public void toBytes(@NotNull final FriendlyByteBuf buf)
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
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        if (!isLogicalServer)
        {
            WindowColonyMap.setColonies(colonyInfo);
        }
        else if (ctxIn.getSender() != null)
        {
            Network.getNetwork().sendToPlayer(new ColonyListMessage(IColonyManager.getInstance().getColonies(ctxIn.getSender().level)), ctxIn.getSender());
        }
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
