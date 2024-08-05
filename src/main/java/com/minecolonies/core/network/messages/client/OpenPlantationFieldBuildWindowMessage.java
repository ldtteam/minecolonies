package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.RotationMirror;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.PlantationFieldBuildRequestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * Message to open the plantation field build window on the client.
 */
public class OpenPlantationFieldBuildWindowMessage extends OpenBuildWindowMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "open_plantation_field_build_window", OpenPlantationFieldBuildWindowMessage::new);

    /**
     * Create a new message.
     *
     * @param pos      the position the plantation field will be anchored at.
     * @param packName the pack of the plantation field.
     * @param path     the path in the pack.
     */
    public OpenPlantationFieldBuildWindowMessage(
      final BlockPos pos,
      final String packName,
      final String path,
      final RotationMirror rotMir)
    {
        super(TYPE, pos, packName, path, rotMir);
    }

    protected OpenPlantationFieldBuildWindowMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
    }

    @Override
    protected AbstractServerPlayMessage createWorkOrderMessage(final BlockPos builder)
    {
        return new PlantationFieldBuildRequestMessage(WorkOrderType.BUILD, pos, packName, path, Minecraft.getInstance().level.dimension(), rotationMirror, builder);
    }
}
