package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.RotationMirror;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.DecorationBuildRequestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Message to open the deco build window on the client.
 */
public class OpenDecoBuildWindowMessage extends OpenBuildWindowMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "open_deco_build_window", OpenDecoBuildWindowMessage::new);

    /**
     * Create a new message.
     *
     * @param pos      the position the deco will be anchored at.
     * @param packName the pack of the deco.
     * @param path     the path in the pack.
     */
    public OpenDecoBuildWindowMessage(
      final BlockPos pos,
      final String packName,
      final String path,
      final RotationMirror rotationMirror)
    {
        super(TYPE, pos, packName, path, rotationMirror);
    }

    protected OpenDecoBuildWindowMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
    }

    @Override
    public AbstractServerPlayMessage createWorkOrderMessage(final BlockPos builder)
    {
        return new DecorationBuildRequestMessage(WorkOrderType.BUILD, pos, packName, path, Minecraft.getInstance().level.dimension(), rotationMirror, builder);
    }
}
