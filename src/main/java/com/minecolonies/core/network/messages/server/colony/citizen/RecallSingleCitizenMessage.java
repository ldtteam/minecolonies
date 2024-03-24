package com.minecolonies.core.network.messages.server.colony.citizen;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import com.minecolonies.core.util.TeleportHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.minecolonies.api.util.constant.TranslationConstants.WARNING_CITIZEN_RECALL_FAILED;

/**
 * Recalls the citizen to the location.
 */
public class RecallSingleCitizenMessage extends AbstractBuildingServerMessage<IBuilding>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "recall_single_citizen", RecallSingleCitizenMessage::new);

    /**
     * The citizen id.
     */
    private final int citizenId;

    /**
     * Object creation for the recall.
     *
     * @param building  View of the building the citizen should be teleported to.
     * @param citizenid the id of the citizen.
     */
    public RecallSingleCitizenMessage(final IBuildingView building, final int citizenid)
    {
        super(TYPE, building);
        this.citizenId = citizenid;
    }

    protected RecallSingleCitizenMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);

        citizenId = buf.readInt();
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        super.toBytes(buf);

        buf.writeInt(citizenId);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final IBuilding building)
    {
        final ICitizenData citizenData = colony.getCitizenManager().getCivilian(citizenId);
        citizenData.setLastPosition(building.getPosition());
        Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getEntity();
        if (!optionalEntityCitizen.isPresent())
        {
            citizenData.updateEntityIfNecessary();
            optionalEntityCitizen = citizenData.getEntity();
        }

        if (optionalEntityCitizen.isPresent() && optionalEntityCitizen.get().getTicksExisted() == 0)
        {
            citizenData.updateEntityIfNecessary();
        }

        final BlockPos loc = building.getID();
        if (optionalEntityCitizen.isPresent() && !TeleportHelper.teleportCitizen(optionalEntityCitizen.get(), colony.getWorld(), loc))
        {
            MessageUtils.format(WARNING_CITIZEN_RECALL_FAILED).sendTo(player);
        }
    }
}
