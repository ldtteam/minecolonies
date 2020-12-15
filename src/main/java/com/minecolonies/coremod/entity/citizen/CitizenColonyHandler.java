package com.minecolonies.coremod.entity.citizen;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenColonyHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.modules.LivingBuildingModule;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.*;
import static com.minecolonies.api.util.constant.CitizenConstants.RANGE_TO_BE_HOME;
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
    protected int colonyId;

    /**
     * The colony reference.
     */
    @Nullable
    protected IColony colony;

    /**
     * Whether the entity is registered to the colony yet.
     */
    protected boolean registered = false;

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
    public IBuildingWorker getWorkBuilding()
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

        this.colonyId = colonyID;
        citizen.setCitizenId(citizenID);

        if (colonyId == 0 || citizen.getCivilianID() == 0)
        {
            citizen.remove();
            return;
        }

        final IColony colony = IColonyManager.getInstance().getColonyByWorld(colonyId, citizen.world);

        if (colony == null)
        {
            Log.getLogger().warn(String.format("EntityCitizen '%s' unable to find Colony #%d", citizen.getUniqueID(), colonyId));
            citizen.remove();
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
        if (citizen.getDataManager().isDirty())
        {
            if (colonyId == 0)
            {
                colonyId = citizen.getDataManager().get(DATA_COLONY_ID);
            }

            if (citizen.getCivilianID() == 0)
            {
                citizen.setCitizenId(citizen.getDataManager().get(DATA_CITIZEN_ID));
            }

            citizen.setFemale(citizen.getDataManager().get(DATA_IS_FEMALE) != 0);
            citizen.setIsChild(citizen.getDataManager().get(DATA_IS_CHILD));
            citizen.setModelId(BipedModelType.valueOf(citizen.getDataManager().get(DATA_MODEL)));
            citizen.setTextureId(citizen.getDataManager().get(DATA_TEXTURE));
            citizen.setRenderMetadata(citizen.getDataManager().get(DATA_RENDER_METADATA));
            citizen.setTexture();
            citizen.getDataManager().setClean();
        }
        citizen.updateArmSwingProg();
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
    public IColony getColony()
    {
        if (colony == null && !citizen.world.isRemote)
        {
            registerWithColony(getColonyId(), citizen.getCivilianID());
        }

        return colony;
    }

    /**
     * Getter for the colonz id.
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
            citizen.getCitizenData().setLastPosition(citizen.getPosition());
        }
    }

    /**
     * Check if a citizen is at home.
     *
     * @return true if so.
     */
    @Override
    public boolean isAtHome()
    {
        @Nullable final IBuilding homeBuilding = getHomeBuilding();

        if (homeBuilding != null)
        {
            if (homeBuilding.hasModule(LivingBuildingModule.class))
            {
                final Tuple<BlockPos, BlockPos> corners = homeBuilding.getCorners();
                return new AxisAlignedBB(corners.getA(), corners.getB())
                         .contains(new Vec3d(citizen.getPosition().getX(), citizen.getPosition().getY(), citizen.getPosition().getZ()));
            }
        }

        @Nullable final BlockPos homePosition = citizen.getHomePosition();
        return homePosition.distanceSq(Math.floor(citizen.posX), citizen.posY, Math.floor(citizen.posZ), false) <= RANGE_TO_BE_HOME;
    }
}
