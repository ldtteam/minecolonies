package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.*;
import com.minecolonies.coremod.entity.ai.mobs.EntityMercenary;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * The message sent when activating mercenaries
 */
public class HireMercenaryMessage implements IMessage
{
    /**
     * Colony id for the mercenary event
     */
    private int colonyID = 0;

    /**
     * Dimension id, needed to get the colony
     */
    private int dimension = 0;

    /**
     * Default constructor for forge
     */
    public HireMercenaryMessage() {super();}

    public HireMercenaryMessage(final IColonyView colony)
    {
        super();
        this.colonyID = colony.getID();
        this.dimension = colony.getDimension();
    }

    @Override
    public void fromBytes(final ByteBuf byteBuf)
    {
        colonyID = byteBuf.readInt();
        dimension = byteBuf.readInt();
    }

    @Override
    public void toBytes(final ByteBuf byteBuf)
    {
        byteBuf.writeInt(colonyID);
        byteBuf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final HireMercenaryMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyID, message.dimension);

        if (colony != null)
        {
            EntityMercenary.spawnMercenariesInColony(colony);
            colony.getWorld()
              .playSound(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), SoundEvents.ENTITY_ILLAGER_CAST_SPELL, null, 1.0f, 1.0f, true);
        }
    }
}
