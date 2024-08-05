package com.minecolonies.core.network.messages.server.colony.building;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import com.minecolonies.core.util.TeleportHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.minecolonies.api.util.constant.TranslationConstants.WARNING_CITIZEN_RECALL_FAILED;

/**
 * Used to handle citizen recalls to their hut.
 */
public class RecallCitizenHutMessage extends AbstractBuildingServerMessage<IBuilding>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "recall_citizen_hut", RecallCitizenHutMessage::new);

    /**
     * Creates a message to recall all citizens to their hut.
     *
     * @param building {@link AbstractBuildingView}
     */
    public RecallCitizenHutMessage(@NotNull final AbstractBuildingView building)
    {
        super(TYPE, building);
    }

    protected RecallCitizenHutMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final  IBuilding building)
    {
        final BlockPos location = building.getPosition();
        final Level world = colony.getWorld();
        for (final ICitizenData citizenData : building.getAllAssignedCitizen())
        {
            Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getEntity();
            if (!optionalEntityCitizen.isPresent())
            {
                Log.getLogger().warn(String.format("Citizen #%d:%d has gone AWOL, respawning them!", colony.getID(), citizenData.getId()));
                citizenData.setNextRespawnPosition(EntityUtils.getSpawnPoint(world, location));
                citizenData.updateEntityIfNecessary();
                optionalEntityCitizen = citizenData.getEntity();
            }

            if (optionalEntityCitizen.isPresent() && !TeleportHelper.teleportCitizen(optionalEntityCitizen.get(), world, location))
            {
                MessageUtils.format(WARNING_CITIZEN_RECALL_FAILED).sendTo(player);
            }
        }
    }
}
