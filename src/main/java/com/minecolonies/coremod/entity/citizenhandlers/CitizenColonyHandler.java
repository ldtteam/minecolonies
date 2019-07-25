package com.minecolonies.coremod.entity.citizenhandlers;

import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHome;
import com.minecolonies.coremod.entity.EntityCitizen;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.minecolonies.api.util.constant.CitizenConstants.RANGE_TO_BE_HOME;
import static com.minecolonies.api.util.constant.CitizenConstants.SATURATION_DECREASE_FACTOR;
import static com.minecolonies.coremod.entity.AbstractEntityCitizen.*;

/**
 * Handles all colony related methods for the citizen.
 */
public class CitizenColonyHandler
{
    /**
     * The citizen assigned to this manager.
     */
    private final EntityCitizen citizen;

    /**
     * It's colony id.
     */
    private int colonyId;

    /**
     * The colony reference.
     */
    @Nullable
    private Colony colony;

    /**
     * Constructor for the experience handler.
     * @param citizen the citizen owning the handler.
     */
    public CitizenColonyHandler(final EntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    /**
     * calculate this worker building.
     *
     * @return the building or null if none present.
     */
    @Nullable
    public AbstractBuildingWorker getWorkBuilding()
    {
        return (citizen.getCitizenData() == null) ? null : citizen.getCitizenData().getWorkBuilding();
    }

    /**
     * Assigns a citizen to a colony.
     *
     * @param c    the colony.
     * @param data the data of the new citizen.
     */
    public void initEntityCitizenValues(@Nullable final Colony c, @Nullable final CitizenData data)
    {
        if (c == null)
        {
            colony = null;
            colonyId = 0;
            citizen.setCitizenId(0);
            citizen.setCitizenData(null);
            citizen.setDead();
            return;
        }

        colony = c;
        colonyId = colony.getID();
        citizen.setCitizenId(data.getId());
        citizen.setCitizenData(data);

        citizen.setIsChild(data.isChild());
        citizen.setCustomNameTag(citizen.getCitizenData().getName());

        citizen.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(data.getMaxHealth());
        citizen.setHealth((float) data.getHealth());

        citizen.setFemale(citizen.getCitizenData().isFemale());
        citizen.setTextureId(citizen.getCitizenData().getTextureId());

        citizen.getDataManager().set(DATA_COLONY_ID, colonyId);
        citizen.getDataManager().set(DATA_CITIZEN_ID, citizen.getCitizenId());
        citizen.getDataManager().set(DATA_IS_FEMALE, citizen.isFemale() ? 1 : 0);
        citizen.getDataManager().set(DATA_TEXTURE, citizen.getTextureId());
        citizen.getDataManager().set(DATA_IS_ASLEEP, citizen.getCitizenData().isAsleep());
        citizen.getDataManager().set(DATA_IS_CHILD, citizen.getCitizenData().isChild());
        citizen.getDataManager().set(DATA_BED_POS, citizen.getCitizenData().getBedPos());

        citizen.getCitizenExperienceHandler().updateLevel();

        citizen.getCitizenData().setCitizenEntity(citizen);
        citizen.getCitizenData().setLastPosition(citizen.getPosition());

        citizen.getCitizenJobHandler().onJobChanged(citizen.getCitizenJobHandler().getColonyJob());
    }

    @Nullable
    public AbstractBuilding getHomeBuilding()
    {
        return (citizen.getCitizenData() == null) ? null : citizen.getCitizenData().getHomeBuilding();
    }

    /**
     * Server-specific update for the EntityCitizen.
     */
    public void updateColonyServer()
    {
        if (colonyId == 0)
        {
            final Colony colony = ColonyManager.getColonyByPosFromWorld(citizen.getEntityWorld(), citizen.getPosition());
            if (colony == null)
            {
                citizen.setDead();
            }
            else
            {
                this.colonyId = colony.getID();
                handleNullColony();
            }
            return;
        }

        if (colony == null)
        {
            handleNullColony();
        }
    }

    /**
     * Handles extreme cases like colony or citizen is null.
     */
    private void handleNullColony()
    {
        final Colony c = ColonyManager.getColonyByWorld(colonyId, citizen.world);

        if (c == null)
        {
            Log.getLogger().warn(String.format("EntityCitizen '%s' unable to find Colony #%d", citizen.getUniqueID(), colonyId));
            citizen.setDead();
            return;
        }

        final CitizenData data = c.getCitizenManager().getCitizen(citizen.getCitizenId());
        if (data == null)
        {
            //  Citizen does not exist in the Colony
            Log.getLogger().warn(String.format("EntityCitizen '%s' attempting to register with Colony #%d as Citizen %d, but not known to colony",
              citizen.getUniqueID(),
              colonyId,
              citizen.getCitizenId()));
            citizen.setDead();
            return;
        }

        final Optional<EntityCitizen> entityCitizenOptional = data.getCitizenEntity();
        entityCitizenOptional.filter(entityCitizen -> !citizen.getUniqueID().equals(entityCitizen.getUniqueID()))
          .ifPresent(entityCitizen -> handleExistingCitizen(data, entityCitizen));

        initEntityCitizenValues(c, data);
    }

    private void handleExistingCitizen(@NotNull final CitizenData data, @NotNull final EntityCitizen existingCitizen)
    {
        Log.getLogger().warn(String.format("EntityCitizen '%s' attempting to register with Colony #%d as Citizen #%d, but already have a citizen ('%s')",
          citizen.getUniqueID(),
          colonyId,
          citizen.getCitizenId(),
          existingCitizen.getUniqueID()));
        if (existingCitizen.getUniqueID().equals(citizen.getUniqueID()))
        {
            data.setCitizenEntity(citizen);
        }
        else
        {
            citizen.setDead();
        }
    }

    /**
     * Update the client side of the citizen entity.
     */
    public void updateColonyClient()
    {
        if (citizen.getDataManager().isDirty())
        {
            if (colonyId == 0)
            {
                colonyId = citizen.getDataManager().get(DATA_COLONY_ID);
            }

            if (citizen.getCitizenId() == 0)
            {
                citizen.setCitizenId(citizen.getDataManager().get(DATA_CITIZEN_ID));
            }

            citizen.setFemale(citizen.getDataManager().get(DATA_IS_FEMALE) != 0);
            citizen.setIsChild(citizen.getDataManager().get(DATA_IS_CHILD));
            citizen.getCitizenExperienceHandler().setLevel(citizen.getDataManager().get(DATA_LEVEL));
            citizen.setModelId(RenderBipedCitizen.Model.valueOf(citizen.getDataManager().get(DATA_MODEL)));
            citizen.setTextureId(citizen.getDataManager().get(DATA_TEXTURE));
            citizen.setRenderMetadata(citizen.getDataManager().get(DATA_RENDER_METADATA));
            citizen.setTexture();
            citizen.getDataManager().setClean();
        }
        citizen.updateArmSwingProg();
    }

    /**
     * Get the amount the worker should decrease its saturation by each action done or x blocks traveled.
     * @return the double describing it.
     */
    public double getPerBuildingFoodCost()
    {
        return getWorkBuilding() == null || getWorkBuilding().getBuildingLevel() == 0 ? 1
                 : (SATURATION_DECREASE_FACTOR * Math.pow(2, getWorkBuilding().getBuildingLevel()));
    }

    /**
     * Getter for the colony.
     * @return the colony of the citizen or null.
     */
    @Nullable
    public Colony getColony()
    {
        return colony;
    }

    /**
     * Getter for the colonz id.
     * @return the colony id.
     */
    public int getColonyId()
    {
        return colonyId;
    }

    /**
     * Setter for the colony id.
     * @param colonyId the new colonyId.
     */
    public void setColonyId(final int colonyId)
    {
        this.colonyId = colonyId;
    }

    /**
     * Clears the colony of the citizen.
     */
    public void clearColony()
    {
        initEntityCitizenValues(null, null);
    }

    /**
     * Check if a citizen is at home.
     * @return true if so.
     */
    public boolean isAtHome()
    {
        @Nullable final AbstractBuilding homeBuilding = getHomeBuilding();

        if (homeBuilding instanceof BuildingHome)
        {
            final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners = homeBuilding.getCorners();
            return new AxisAlignedBB(corners.getA().getA(), citizen.posY - 1, corners.getB().getA(),
              corners.getA().getB(),
              citizen.posY + 1,
              corners.getB().getB()).intersectsWithXZ(new Vec3d(citizen.getPosition()));
        }

        @Nullable final BlockPos homePosition = citizen.getHomePosition();
        return homePosition.distanceSq((int) Math.floor(citizen.posX), (int)citizen. posY, (int) Math.floor(citizen.posZ)) <= RANGE_TO_BE_HOME;
    }
}
