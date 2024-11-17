package com.minecolonies.core.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenColonyHandler;
import com.minecolonies.api.util.Log;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.*;
import static com.minecolonies.api.util.constant.CitizenConstants.SATURATION_DECREASE_FACTOR;

/**
 * Handles all colony related methods for the citizen.
 */
public class CitizenColonyHandler implements ICitizenColonyHandler
{
    /**
     * The citizen assigned to this manager.
     */
    protected final AbstractEntityCitizen citizen;

    /**
     * It's colony id.
     */
    protected int colonyId = 0;

    /**
     * The colony reference.
     */
    @Nullable
    protected IColony colony;

    /**
     * Whether the entity is registered to the colony yet.
     */
    protected boolean registered = false;

    private boolean needsClientUpdate = false;

    /**
     * Constructor for the experience handler.
     *
     * @param citizen the citizen owning the handler.
     */
    public CitizenColonyHandler(final AbstractEntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    /**
     * calculate this worker building.
     *
     * @return the building or null if none present.
     */
    @Override
    @Nullable
    public IBuilding getWorkBuilding()
    {
        return (citizen.getCitizenData() == null) ? null : citizen.getCitizenData().getWorkBuilding();
    }

    @Override
    @Nullable
    public IBuilding getHomeBuilding()
    {
        return (citizen.getCitizenData() == null) ? null : citizen.getCitizenData().getHomeBuilding();
    }

    /**
     * Server-specific update for the EntityCitizen.
     *
     * @param colonyID  the id of the colony.
     * @param citizenID the id of the citizen.
     */
    @Override
    public void registerWithColony(final int colonyID, final int citizenID)
    {
        if (registered)
        {
            return;
        }

        if (citizen.level.getEntity(citizen.getId()) != citizen)
        {
            Log.getLogger().warn("Registering too early, entity not added to world!", new Exception());
            citizen.discard();
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

        if (colony.getWorld() == null)
        {
            // Wait until colony loads world
            return;
        }

        this.colony = colony;
        colony.getCitizenManager().registerCivilian(citizen);
        registered = true;
    }

    /**
     * Update the client side of the citizen entity.
     */
    @Override
    public void updateColonyClient()
    {
        if (needsClientUpdate)
        {
            if (colonyId == 0)
            {
                colonyId = citizen.getEntityData().get(DATA_COLONY_ID);
            }

            if (colonyId == 0)
            {
                citizen.discard();
                return;
            }
            colony = IColonyManager.getInstance().getColonyView(colonyId, citizen.level.dimension());

            if (citizen.getCivilianID() == 0)
            {
                citizen.setCitizenId(citizen.getEntityData().get(DATA_CITIZEN_ID));
            }

            citizen.setFemale(citizen.getEntityData().get(DATA_IS_FEMALE) != 0);
            citizen.setIsChild(citizen.getEntityData().get(DATA_IS_CHILD));
            citizen.setModelId(new ResourceLocation(citizen.getEntityData().get(DATA_MODEL)));
            citizen.setTextureId(citizen.getEntityData().get(DATA_TEXTURE));
            citizen.setRenderMetadata(citizen.getEntityData().get(DATA_RENDER_METADATA));
            citizen.setTexture();

            needsClientUpdate = false;
        }
    }

    @Override
    public void onSyncDataUpdate(final EntityDataAccessor<?> data)
    {
        if (data.equals(DATA_COLONY_ID) || data.equals(DATA_CITIZEN_ID) || data.equals(DATA_IS_FEMALE) || data.equals(DATA_IS_CHILD) || data.equals(DATA_MODEL)
              || data.equals(DATA_TEXTURE)
              || data.equals(DATA_TEXTURE_SUFFIX) || data.equals(DATA_STYLE) || data.equals(DATA_RENDER_METADATA))
        {
            needsClientUpdate = true;
        }
    }

    @Override
    public boolean registered()
    {
        return registered;
    }

    /**
     * Get the amount the worker should decrease its saturation by each action done or x blocks traveled.
     *
     * @return the double describing it.
     */
    @Override
    public double getPerBuildingFoodCost()
    {
        return getWorkBuilding() == null || getWorkBuilding().getBuildingLevel() == 0 ? 1
                 : (SATURATION_DECREASE_FACTOR * Math.pow(2, getWorkBuilding().getBuildingLevel()));
    }

    /**
     * Getter for the colony.
     *
     * @return the colony of the citizen or null.
     */
    @Override
    @Nullable
    public IColony getColonyOrRegister()
    {
        if (colony == null && !citizen.level.isClientSide)
        {
            registerWithColony(getColonyId(), citizen.getCivilianID());
        }

        return colony;
    }

    @Override
    public @Nullable IColony getColony()
    {
        return colony;
    }

    /**
     * Getter for the colony id.
     *
     * @return the colony id.
     */
    @Override
    public int getColonyId()
    {
        return colonyId;
    }

    /**
     * Setter for the colony id.
     *
     * @param colonyId the new colonyId.
     */
    @Override
    public void setColonyId(final int colonyId)
    {
        this.colonyId = colonyId;
    }

    @Override
    public void onCitizenRemoved()
    {
        if (citizen.getCitizenData() != null && registered && colony != null)
        {
            colony.getCitizenManager().unregisterCivilian(citizen);
            citizen.getCitizenData().setLastPosition(citizen.blockPosition());
        }
    }
}
