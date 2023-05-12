package com.minecolonies.coremod.network.messages.client.colony;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.Colony;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewMessage implements IMessage
{
    /**
     * The colony id.
     */
    private int colonyId;

    /**
     * If this is a new subscription.
     */
    private boolean isNewSubscription;

    /**
     * The buffer with the data.
     */
    private FriendlyByteBuf colonyBuffer;

    /**
     * The dimension of the colony.
     */
    private ResourceKey<Level> dim;

    /**
     * Empty constructor used when registering the
     */
    public ColonyViewMessage()
    {
        super();
    }

    /**
     * Add or Update a ColonyView on the client.
     *
     * @param colony            Colony of the view to update.
     * @param buf               the bytebuffer.
     * @param isNewSubscription Boolean whether or not this is a new subscription.
     */
    public ColonyViewMessage(@NotNull final Colony colony, final FriendlyByteBuf buf, final boolean isNewSubscription)
    {
        this.colonyId = colony.getID();
        this.isNewSubscription = isNewSubscription;
        this.dim = colony.getDimension();
        this.colonyBuffer = new FriendlyByteBuf(buf.copy());
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        final FriendlyByteBuf newBuf = new FriendlyByteBuf(buf.retain());
        colonyId = newBuf.readInt();
        isNewSubscription = newBuf.readBoolean();
        dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(newBuf.readUtf(32767)));
        colonyBuffer = newBuf;
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeBoolean(isNewSubscription);
        buf.writeUtf(dim.location().toString());
        buf.writeBytes(colonyBuffer);
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
            IColonyManager.getInstance().handleColonyViewMessage(colonyId, colonyBuffer, Minecraft.getInstance().level, isNewSubscription, dim);
        }
        colonyBuffer.release();
    }
}
