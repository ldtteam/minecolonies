package com.minecolonies.core.network.messages.client;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.core.client.gui.WindowBuildDecoration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
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
     * The rotation.
     */
    protected Rotation rotation;

    /**
     * If mirrored.
     */
    protected boolean mirror;

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
    protected OpenBuildWindowMessage(final BlockPos pos, final String packName, final String path, final Rotation rotation, final Mirror mirror)
    {
        this.pos = pos;
        this.path = path;
        this.packName = packName;
        this.rotation = rotation;
        this.mirror = mirror != Mirror.NONE;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.path);
        buf.writeUtf(this.packName);
        buf.writeBoolean(this.mirror);
        buf.writeInt(this.rotation.ordinal());
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        this.pos = buf.readBlockPos();
        this.path = buf.readUtf(32767);
        this.packName = buf.readUtf(32767);
        this.mirror = buf.readBoolean();
        this.rotation = Rotation.values()[buf.readInt()];
    }

    @Override
    public final @NotNull LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public final void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        new WindowBuildDecoration(this.pos, this.packName, this.path, this.rotation, this.mirror, this::createWorkOrderMessage).open();
    }

    protected abstract IMessage createWorkOrderMessage(BlockPos builder);
}
