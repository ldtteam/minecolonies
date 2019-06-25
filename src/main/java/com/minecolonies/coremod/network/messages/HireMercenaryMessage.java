package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.entity.ai.mobs.EntityMercenary;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * The message sent when activating mercenaries
 */
public class HireMercenaryMessage extends AbstractMessage<HireMercenaryMessage, IMessage>
{
    /**
     * Colony id for the mercenary event
     */
    int colonyID = 0;

    /**
     * Dimension id, needed to get the colony
     */
    int dimension = 0;

    /**
     * Default constructor for forge
     */
    public HireMercenaryMessage() {super();}

    public HireMercenaryMessage(ColonyView colony)
    {
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
    public void messageOnServerThread(final HireMercenaryMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColonyByDimension(message.colonyID, message.dimension);

        if (colony != null)
        {
            EntityMercenary.spawnMercenariesInColony(colony);
            colony.getWorld()
              .playSound(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), SoundEvents.ENTITY_ILLAGER_CAST_SPELL, null, 1.0f, 1.0f, true);
        }
    }
}
