package com.minecolonies.coremod.network.messages.server;

import com.minecolonies.api.network.IMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An abstract request for a builder to build something
 */
public abstract class AbstractBuildRequestMessage implements IMessage
{
    /** The builder who should build this, or ZERO to not specify one yet */
    public BlockPos builder = BlockPos.ZERO;

    /** The anchor position of the build request */
    protected BlockPos pos;

    @Override
    public void toBytes(@NotNull FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeBlockPos(builder);
    }

    @Override
    public void fromBytes(@NotNull FriendlyByteBuf buf)
    {
        pos = buf.readBlockPos();
        builder = buf.readBlockPos();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public abstract void onExecute(@NotNull NetworkEvent.Context ctxIn, boolean isLogicalServer);
}
