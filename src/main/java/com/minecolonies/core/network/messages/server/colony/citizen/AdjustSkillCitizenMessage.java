package com.minecolonies.core.network.messages.server.colony.citizen;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Adjust the skill level of the citizen.
 */
public class AdjustSkillCitizenMessage extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "adjust_skill_citizen", AdjustSkillCitizenMessage::new);

    /**
     * The id of the citizen.
     */
    private final int citizenId;

    /**
     * The skill quantity.
     */
    private final int quantity;

    /**
     * The skill to alter.
     */
    private final Skill skill;

    /**
     * Creates a skill alteration message.
     *
     * @param citizenDataView Citizen of the request.
     * @param quantity        of item needed to be transfered
     * @param skill           the skill to alter.
     * @param colony          the colony of the network message
     */
    public AdjustSkillCitizenMessage(final IColony colony, @NotNull final ICitizenDataView citizenDataView, final int quantity, final Skill skill)
    {
        super(TYPE, colony);
        this.citizenId = citizenDataView.getId();
        this.quantity = quantity;
        this.skill = skill;
    }

    protected AdjustSkillCitizenMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        citizenId = buf.readInt();
        quantity = buf.readInt();
        skill = Skill.values()[buf.readInt()];
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(citizenId);
        buf.writeInt(quantity);
        buf.writeInt(skill.ordinal());
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
        final ICitizenData citizenData = colony.getCitizenManager().getCivilian(citizenId);
        if (citizenData == null)
        {
            Log.getLogger().warn("AdjustSkillCitizenMessage citizenData is null");
            return;
        }

        final Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getEntity();
        if (!optionalEntityCitizen.isPresent())
        {
            Log.getLogger().warn("AdjustSkillCitizenMessage entity citizen is null");
            return;
        }

        final boolean isCreative = player.isCreative();
        if (!isCreative)
        {
            Log.getLogger().warn("AdjustSkillCitizenMessage player must be creative.");
            return;
        }

        citizenData.getCitizenSkillHandler().incrementLevel(skill, quantity);
        citizenData.markDirty(0);
    }
}
