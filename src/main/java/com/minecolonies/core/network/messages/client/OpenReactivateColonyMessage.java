package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.townhall.WindowTownHallColonyReactivate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * Message to open the colony founding covenant.
 */
public class OpenReactivateColonyMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "open_reactivate_colony", OpenReactivateColonyMessage::new);

    /**
     * Colony pos at which we are trying to place.
     */
    private String closestName;
    private int      closestDistance;
    private BlockPos townHallPos;

    /**
     * Default constructor
     **/
    public OpenReactivateColonyMessage(FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(type);
        this.closestName = buf.readUtf(32767);
        this.closestDistance = buf.readInt();
        this.townHallPos = buf.readBlockPos();
    }

    public OpenReactivateColonyMessage(final String closestName, final int closestDistance, final BlockPos townHallPos)
    {
        super(TYPE);
        this.closestName = closestName;
        this.closestDistance = closestDistance;
        this.townHallPos = townHallPos;
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final Player player)
    {
        new WindowTownHallColonyReactivate(townHallPos, closestName, closestDistance).open();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeUtf(closestName);
        buf.writeInt(closestDistance);
        buf.writeBlockPos(townHallPos);
    }
}
