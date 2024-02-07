package com.minecolonies.core.network.messages.server.colony;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.entity.mobs.EntityMercenary;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * The message sent when activating mercenaries
 */
public class HireMercenaryMessage extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "hire_mercenary", HireMercenaryMessage::new);

    public HireMercenaryMessage(final IColony colony)
    {
        super(TYPE, colony);
    }

    protected HireMercenaryMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
        EntityMercenary.spawnMercenariesInColony(colony);
        colony.getWorld()
          .playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.ILLUSIONER_CAST_SPELL, null, 1.0f, 1.0f, true);
    }
}
