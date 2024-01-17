package com.minecolonies.core.colony.crafting;

import com.minecolonies.api.network.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The message used to synchronize crafter recipes from a server to a client.
 */
public class CustomRecipeManagerMessage implements IMessage
{
    /**
     * The buffer with the data.
     */
    private FriendlyByteBuf managerBuffer;

    /**
     * Empty constructor used when registering the message
     */
    public CustomRecipeManagerMessage()
    {
        super();
    }

    /**
     * Add or Update a CustomRecipeManager on the client.
     *
     * @param buf               the bytebuffer.
     */
    public CustomRecipeManagerMessage(final FriendlyByteBuf buf)
    {
        this.managerBuffer = new FriendlyByteBuf(buf.copy());
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        managerBuffer = new FriendlyByteBuf(buf.retain());
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBytes(managerBuffer);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        if (Minecraft.getInstance().level != null)
        {
            CustomRecipeManager.getInstance().handleCustomRecipeManagerMessage(managerBuffer);
        }
        managerBuffer.release();
    }
}