package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.entity.mobs.EntityMercenary;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

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
    public void fromBytes(final PacketBuffer byteBuf)
    {
        colonyID = byteBuf.readInt();
        dimension = byteBuf.readInt();
    }

    @Override
    public void toBytes(final PacketBuffer byteBuf)
    {
        byteBuf.writeInt(colonyID);
        byteBuf.writeInt(dimension);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, dimension);

        if (colony != null)
        {
            final PlayerEntity player = ctxIn.getSender();
            EntityMercenary.spawnMercenariesInColony(colony);
            colony.getWorld()
              .playSound(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, null, 1.0f, 1.0f, true);
        }
    }
}
