package com.minecolonies.core.network.messages.server.colony.citizen;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

/**
 * Message sent to open a citizen inventory.
 */
public class OpenCitizenInventoryMessage extends AbstractColonyServerMessage
{
    /**
     * The entity its id.
     */
    private int entityID;

    /***
     * The inventory name.
     */
    private String name;

    /**
     * Empty public constructor.
     */
    public OpenCitizenInventoryMessage()
    {
        super();
    }

    /**
     * Creates an open inventory message for the citizen.
     *
     * @param citizen the citizen.
     */
    public OpenCitizenInventoryMessage(final ICitizenDataView citizen)
    {
        super(citizen.getColony());
        this.entityID = citizen.getEntityId();
        this.name = citizen.getName();
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        final ServerPlayer player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        final Entity entity = CompatibilityUtils.getWorldFromEntity(player).getEntity(entityID);
        if (entity instanceof AbstractEntityCitizen citizen)
        {
            if (!StringUtil.isNullOrEmpty(name))
            {
                citizen.getInventoryCitizen().setCustomName(name);
            }

            NetworkHooks.openScreen(player,
              citizen,
              packetBuffer -> packetBuffer.writeVarInt(citizen.getCitizenColonyHandler().getColonyId()).writeVarInt(citizen.getCivilianID()));
        }
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(entityID);
        buf.writeUtf(name);
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        entityID = buf.readInt();
        name = buf.readUtf();
    }
}
