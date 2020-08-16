package com.minecolonies.coremod.network.messages.server.colony.building.guard;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.MobEntryView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a message for changing MobEntry priorities
 */
public class MobEntryChangeMessage extends AbstractBuildingServerMessage<AbstractBuildingGuards>
{
    private final List<MobEntryView> mobsToAttack;

    /**
     * Empty standard constructor.
     */
    public MobEntryChangeMessage(final PacketBuffer buf)
    {
        super(buf);
        final int mobSize = buf.readInt();
        this.mobsToAttack = new ArrayList<>(mobSize);
        for (int i = 0; i < mobSize; i++)
        {
            final MobEntryView mobEntry = MobEntryView.readFromByteBuf(buf);
            mobsToAttack.add(mobEntry);
        }
    }

    public MobEntryChangeMessage(
      @NotNull final AbstractBuildingGuards.View building,
      final List<MobEntryView> mobsToAttack
    )
    {
        super(building);
        this.mobsToAttack = new ArrayList<>(mobsToAttack);
    }

    @Override
    public void toBytesOverride(final PacketBuffer buf)
    {
        buf.writeInt(this.mobsToAttack.size());
        for (final MobEntryView entry : this.mobsToAttack)
        {
            MobEntryView.writeToByteBuf(buf, entry);
        }
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final AbstractBuildingGuards building)
    {
        building.setMobsToAttack(mobsToAttack);
    }
}
