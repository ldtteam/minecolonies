package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.RotationMirror;
import com.minecolonies.core.client.gui.WindowBuildDecoration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Message to open the build window, for example for decorations.
 */
public abstract class OpenBuildWindowMessage extends AbstractClientPlayMessage
{
    /**
     * Town hall position to create building on.
     */
    protected final BlockPos pos;

    /**
     * The colony name.
     */
    protected final String path;

    /**
     * The structure pack name.
     */
    protected final String packName;

    /**
     * The rotation and mirror.
     */
    protected final RotationMirror rotationMirror;

    /**
     * Create a new message.
     *
     * @param pos      the position the deco will be anchored at.
     * @param packName the pack of the deco.
     * @param path     the path in the pack.
     */
    protected OpenBuildWindowMessage(final PlayMessageType<?> type, final BlockPos pos, final String packName, final String path, final RotationMirror rotMir)
    {
        super(type);
        this.pos = pos;
        this.path = path;
        this.packName = packName;
        this.rotationMirror = rotMir;
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.path);
        buf.writeUtf(this.packName);
        buf.writeByte(this.rotationMirror.ordinal());
    }

    protected OpenBuildWindowMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.pos = buf.readBlockPos();
        this.path = buf.readUtf(32767);
        this.packName = buf.readUtf(32767);
        this.rotationMirror = RotationMirror.values()[buf.readByte()];
    }

    @Override
    public final void onExecute(final IPayloadContext context, final Player player)
    {
        new WindowBuildDecoration(this.pos, this.packName, this.path, this.rotationMirror, this::createWorkOrderMessage).open();
    }

    protected abstract AbstractServerPlayMessage createWorkOrderMessage(BlockPos builder);
}
