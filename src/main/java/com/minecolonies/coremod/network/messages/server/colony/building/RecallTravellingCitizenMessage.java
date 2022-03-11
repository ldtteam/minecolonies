package com.minecolonies.coremod.network.messages.server.colony.building;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import com.minecolonies.coremod.util.TeleportHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Used to handle citizen recalls for all travelling citizens.
 */
public class RecallTravellingCitizenMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * Empty public constructor.
     */
    public RecallTravellingCitizenMessage()
    {
        super();
    }

    /**
     * Creates a message to recall all travelling citizens to their hut or the town hall.
     *
     * @param building {@link IBuildingView}
     */
    public RecallTravellingCitizenMessage(@NotNull final IBuildingView building)
    {
        super(building);
    }

    @Override
    protected void onExecute(@NotNull final NetworkEvent.Context ctxIn, final boolean isLogicalServer, @NotNull final IColony colony, @NotNull final IBuilding building)
    {
        colony.getTravelingManager().recallAllTravellingCitizens();
    }

    @Override
    protected void toBytesOverride(final FriendlyByteBuf buf)
    {

    }

    @Override
    protected void fromBytesOverride(final FriendlyByteBuf buf)
    {

    }
}
