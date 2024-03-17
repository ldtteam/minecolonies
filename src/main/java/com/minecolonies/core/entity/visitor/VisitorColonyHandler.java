package com.minecolonies.core.entity.visitor;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.entity.citizen.citizenhandlers.CitizenColonyHandler;
import net.minecraft.world.entity.Entity;

/**
 * Colony handler for visitors
 */
public class VisitorColonyHandler extends CitizenColonyHandler
{
    /**
     * Constructor for the experience handler.
     *
     * @param citizen the citizen owning the handler.
     */
    public VisitorColonyHandler(final AbstractEntityCitizen citizen)
    {
        super(citizen);
    }

    @Override
    public void registerWithColony(final int colonyID, final int citizenID)
    {
        if (registered)
        {
            return;
        }

        this.colonyId = colonyID;
        citizen.setCitizenId(citizenID);

        if (colonyId == 0 || citizen.getCivilianID() == 0)
        {
            citizen.remove(Entity.RemovalReason.DISCARDED);
            return;
        }

        final IColony colony = IColonyManager.getInstance().getColonyByWorld(colonyId, citizen.level);

        if (colony == null)
        {
            Log.getLogger().warn(String.format("EntityCitizen '%s' unable to find Colony #%d", citizen.getUUID(), colonyId));
            citizen.remove(Entity.RemovalReason.DISCARDED);
            return;
        }

        this.colony = colony;
        colony.getVisitorManager().registerCivilian(citizen);
        registered = true;
    }

    @Override
    public void onCitizenRemoved()
    {
        if (citizen.getCitizenData() != null && registered && colony != null)
        {
            colony.getVisitorManager().unregisterCivilian(citizen);
            citizen.getCitizenData().setLastPosition(citizen.blockPosition());
        }
    }
}
