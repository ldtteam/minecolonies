package com.minecolonies.core.network.messages.client.colony;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.constant.Constants;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Add or Update a AbstractBuilding.View to a ColonyView on the client.
 */
public class ColonyViewBuildingViewMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "colony_view_building_view", ColonyViewBuildingViewMessage::new);

    private final int          colonyId;
    private final BlockPos     buildingId;
    private final FriendlyByteBuf buildingData;

    /**
     * Dimension of the colony.
     */
    private final ResourceKey<Level> dimension;


    /**
     * Creates a message to handle colony views.
     *
     * @param building AbstractBuilding to add or update a view.
     */
    public ColonyViewBuildingViewMessage(@NotNull final IBuilding building)
    {
        this(building, true);
    }

    /**
     * Creates a message to handle colony views.
     *
     * @param building AbstractBuilding to add or update a view.
     */
    public ColonyViewBuildingViewMessage(@NotNull final IBuilding building, final boolean fullSync)
    {
        super(TYPE);
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.buildingData = new FriendlyByteBuf(Unpooled.buffer());
        building.serializeToView(this.buildingData, fullSync);
        this.dimension = building.getColony().getDimension();
    }

    protected ColonyViewBuildingViewMessage(@NotNull final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
        buildingData = new FriendlyByteBuf(Unpooled.buffer(buf.readableBytes()));
        buf.readBytes(buildingData, buf.readableBytes());
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buildingData.resetReaderIndex();
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeUtf(dimension.location().toString());
        buf.writeBytes(buildingData);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final Player player)
    {
        IColonyManager.getInstance().handleColonyBuildingViewMessage(colonyId, buildingId, buildingData, dimension);
    }
}
