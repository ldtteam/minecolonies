package com.minecolonies.core.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.core.entity.mobs.EntityMercenary;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * The message sent when activating mercenaries
 */
public class HireMercenaryMessage extends AbstractColonyServerMessage
{
    public HireMercenaryMessage()
    {
    }

    public HireMercenaryMessage(final IColony colony)
    {
        super(colony);
    }

    @Nullable
    @Override
    public Action permissionNeeded()
    {
        return super.permissionNeeded();
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        final Player player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        EntityMercenary.spawnMercenariesInColony(colony);
        colony.getWorld()
          .playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.ILLUSIONER_CAST_SPELL, null, 1.0f, 1.0f, true);
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
