package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewRemoveBuildingMessage implements IMessage, IMessageHandler<ColonyViewRemoveBuildingMessage, IMessage>
{
    private int      colonyId;
    private BlockPos buildingId;

    /**
     * Empty constructor used when registering the message.
     */
    public ColonyViewRemoveBuildingMessage()
    {
        super();
    }

    /**
     * Creates an object for the building remove message.
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
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
    }

    @Nullable
    @Override
    public IMessage onMessage(@NotNull final ColonyViewRemoveBuildingMessage message, final MessageContext ctx)
    {
        return ColonyManager.handleColonyViewRemoveBuildingMessage(message.colonyId, message.buildingId);
    }
}
