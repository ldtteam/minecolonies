package com.minecolonies.core.network.messages.client.colony;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.ColonyView;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
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
public class OpenBuildingUIMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "open_building_ui", OpenBuildingUIMessage::new);

    private int          colonyId;
    private BlockPos     buildingId;

    /**
     * Dimension of the colony.
     */
    private ResourceKey<Level> dimension;

    /**
     * Empty constructor used when registering the
     */
    public OpenBuildingUIMessage(@NotNull final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(type);
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
    }

    /**
     * Creates a message to handle colony views.
     *
     * @param building AbstractBuilding to add or update a view.
     */
    public OpenBuildingUIMessage(@NotNull final IBuilding building)
    {
        super(TYPE);
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeUtf(dimension.location().toString());
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final Player player)
    {
        if (IColonyManager.getInstance().getColonyView(colonyId, dimension) instanceof ColonyView colonyView && colonyView.getBuilding(buildingId) instanceof AbstractBuildingView buildingView)
        {
            buildingView.openGui(false);
        }
    }
}
