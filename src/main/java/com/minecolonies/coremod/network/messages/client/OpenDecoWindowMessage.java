package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.client.gui.WindowBuildDecoration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Message to open the deco build window on the client.
 */
public class OpenDecoWindowMessage implements IMessage
{
    /**
     * Town hall position to create building on.
     */
    BlockPos pos;

    /**
     * The colony name.
     */
    String path;

    /**
     * The structure pack name.
     */
    String packName;

    /**
     * If this is a client-only pack.
     */
    Boolean clientPack;

    /**
     * The rotation.
     */
    private Rotation rotation;

    /**
     * If mirrored.
     */
    private boolean mirror;

    public OpenDecoWindowMessage()
    {
        super();
    }

    /**
     * Create a new message.
     * @param pos the position the deco will be anchored at.
     * @param packName the pack of the deco.
     * @param path the path in the pack.
     */
    public OpenDecoWindowMessage(final BlockPos pos, final Boolean clientPack, final String packName, final String path, final Rotation rotation, final Mirror mirror)
    {
        this.pos = pos;
        this.path = path;
        this.packName = packName;
        this.clientPack = clientPack;
        this.rotation = rotation;
        this.mirror = mirror != Mirror.NONE;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.path);
        buf.writeUtf(this.packName);
        buf.writeBoolean(this.clientPack);
        buf.writeBoolean(this.mirror);
        buf.writeInt(this.rotation.ordinal());
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        this.pos = buf.readBlockPos();
        this.path = buf.readUtf(32767);
        this.packName = buf.readUtf(32767);
        this.clientPack = buf.readBoolean();
        this.mirror = buf.readBoolean();
        this.rotation = Rotation.values()[buf.readInt()];
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
        new WindowBuildDecoration(pos, clientPack, packName, path, rotation, mirror).open();
    }
}
