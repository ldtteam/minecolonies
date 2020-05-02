package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.coremod.entity.mobs.EntityMercenary;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fml.network.NetworkEvent;
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
        final PlayerEntity player = ctxIn.getSender();
        if (player == null) return;

        EntityMercenary.spawnMercenariesInColony(colony);
        colony.getWorld()
          .playSound(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, null, 1.0f, 1.0f, true);

    }

    @Override
    protected void toBytesOverride(final PacketBuffer buf)
    {

    }

    @Override
    protected void fromBytesOverride(final PacketBuffer buf)
    {

    }
}
