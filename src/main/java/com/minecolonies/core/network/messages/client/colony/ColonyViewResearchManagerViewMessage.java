package com.minecolonies.core.network.messages.client.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.research.IResearchManager;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to synch research manager to colony.
 */
public class ColonyViewResearchManagerViewMessage implements IMessage
{
    private int             colonyId;
    private FriendlyByteBuf researchManagerData;

    /**
     * Dimension of the colony.
     */
    private ResourceKey<Level> dimension;

    /**
     * Empty constructor used when registering the
     */
    public ColonyViewResearchManagerViewMessage()
    {
        super();
    }

    /**
     * Creates a message to send the research manager to the client.
     * @param colony the colony.
     * @param researchManager the research manager.
     */
    public ColonyViewResearchManagerViewMessage(final IColony colony, @NotNull final IResearchManager researchManager)
    {
        super();
        this.colonyId = colony.getID();
        this.dimension = colony.getDimension();

        this.researchManagerData = new FriendlyByteBuf(Unpooled.buffer());

        final CompoundTag researchCompound = new CompoundTag();
        researchManager.writeToNBT(researchCompound);
        this.researchManagerData.writeNbt(researchCompound);
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        colonyId = buf.readInt();
        dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
        researchManagerData = new FriendlyByteBuf(Unpooled.buffer(buf.readableBytes()));
        buf.readBytes(researchManagerData, buf.readableBytes());
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        researchManagerData.resetReaderIndex();
        buf.writeInt(colonyId);
        buf.writeUtf(dimension.location().toString());
        buf.writeBytes(researchManagerData);
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
        final IColonyView colonyView = IColonyManager.getInstance().getColonyView(colonyId, dimension);
        if (colonyView != null)
        {
            colonyView.handleColonyViewResearchManagerUpdate(researchManagerData.readNbt());
        }
    }
}
