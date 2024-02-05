package com.minecolonies.core.network.messages.client.colony;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.core.colony.Colony;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewRemoveBuildingMessage implements IMessage
{
    private int      colonyId;
    private BlockPos buildingId;

    /**
     * Empty constructor used when registering the
     */
    public ColonyViewRemoveBuildingMessage()
    {
        super();
    }

    /**
     * Creates an object for the building remove
     *
     * @param colony   Colony the building is in.
     * @param building AbstractBuilding that is removed.
     */
    public ColonyViewRemoveBuildingMessage(@NotNull final Colony colony, final BlockPos building)
    {
        this.colonyId = colony.getID();
        this.buildingId = building;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        if (Minecraft.getInstance().level != null)
        {
            IColonyManager.getInstance().handleColonyViewRemoveBuildingMessage(colonyId, buildingId, Minecraft.getInstance().level.dimension());
        }
    }
}
