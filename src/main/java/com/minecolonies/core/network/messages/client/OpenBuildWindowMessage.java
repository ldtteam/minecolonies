package com.minecolonies.core.network.messages.client;

import com.ldtteam.structurize.api.RotationMirror;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.core.client.gui.WindowBuildDecoration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to open the build window, for example for decorations.
 */
public abstract class OpenBuildWindowMessage implements IMessage
{
    /**
     * Town hall position to create building on.
     */
    protected BlockPos pos;

    /**
     * The colony name.
     */
    protected String path;

    /**
     * The structure pack name.
     */
    protected String packName;

    /**
     * The rotation and mirror.
     */
    protected RotationMirror rotationMirror;

    protected OpenBuildWindowMessage()
    {
        super();
    }

    /**
     * Create a new message.
     *
     * @param pos      the position the deco will be anchored at.
     * @param packName the pack of the deco.
     * @param path     the path in the pack.
     */
    protected OpenBuildWindowMessage(final BlockPos pos, final String packName, final String path, final RotationMirror rotMir)
    {
        this.pos = pos;
        this.path = path;
        this.packName = packName;
        this.rotationMirror = rotMir;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.path);
        buf.writeUtf(this.packName);
        buf.writeByte(this.rotationMirror.ordinal());
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        this.pos = buf.readBlockPos();
        this.path = buf.readUtf(32767);
        this.packName = buf.readUtf(32767);
        this.rotationMirror = RotationMirror.values()[buf.readByte()];
    }

    @Override
    public final @NotNull LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public final void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        new WindowBuildDecoration(this.pos, this.packName, this.path, this.rotationMirror, this::createWorkOrderMessage).open();
    }

    protected abstract IMessage createWorkOrderMessage(BlockPos builder);
}
