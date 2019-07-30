package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.IBuildingWorker;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingComposter;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class used for setting whether dirt or compost should be retrived from the bin.
 */
public class ComposterRetrievalMessage implements IMessage
{
    /**
     * The colony id
     */
    private int colonyId;

    /**
     * The composter's building id.
     */
    private BlockPos buildingId;

    /**
     * Whether the composter should retrieve dirt or not.
     */
    private boolean retrieveDirt;

    /**
     * The dimension of the message.
     */
    private int dimension;

    /**
     * Empty standard constructor.
     */
    public ComposterRetrievalMessage()
    {
        super();
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Creates a message which will be sent to set the retrieval setting in the composter.
     *
     * @param building      the building view of the composter
     * @param retrieve whether or not dirt should be retrieved.
     */
    public ComposterRetrievalMessage(final BuildingComposter.View building, final boolean retrieve)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.retrieveDirt = retrieve;
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        retrieveDirt = buf.readBoolean();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeBoolean(retrieveDirt);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final ComposterRetrievalMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {
            //Verify player has permission to change this hut's settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final IBuildingWorker building = colony.getBuildingManager().getBuilding(message.buildingId, AbstractBuildingWorker.class);
            if (building instanceof BuildingComposter)
            {
                ((BuildingComposter) building).setShouldRetrieveDirtFromCompostBin(message.retrieveDirt);
            }

        }
    }
}
