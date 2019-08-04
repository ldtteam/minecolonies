package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenStuckHandler;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Utils;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

/**
 * Handler taking care of citizens getting stuck.
 */
public class CitizenStuckHandler implements ICitizenStuckHandler
{
    /**
     * The citizen assigned to this manager.
     */
    private final AbstractEntityCitizen citizen;

    /**
     * Field to try moving away from a location in order to pass it.
     */
    private int movingAwayAttempts = 0;

    /**
     * Time the entity is at the same position already.
     */
    private int stuckTime = 0;

    /**
     * Constructor for the experience handler.
     * @param citizen the citizen owning the handler.
     */
    public CitizenStuckHandler(final EntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    /**
     * Called in the citizen every few ticks to check if stuck.
     */
    @Override
    public void update()
    {
        if (citizen.getTicksExisted() % TICKS_20 == 0)
        {
            checkIfStuck();

            if (citizen.getTicksExisted() % (MAX_STUCK_TIME * TICKS_SECOND) == 0)
            {
                movingAwayAttempts = 0;
            }
        }
    }

    /**
     * Let worker AIs check if the citizen is stuck to not track it on their own.
     * @return true if tried to move away already.
     */
    @Override
    public boolean isStuck()
    {
        return stuckTime >= MIN_STUCK_TIME + citizen.getRandom().nextInt(MIN_STUCK_TIME) && movingAwayAttempts > MOVE_AWAY_RETRIES;
    }

    /**
     * Check if the citizen is stuck and try to get him back on track.
     */
    private void checkIfStuck()
    {
        if (citizen.getCurrentPosition() == null)
        {
            citizen.setCurrentPosition(citizen.getPosition());
            return;
        }

        if (citizen.getNavigator().getDestination() == null || citizen.getNavigator().getDestination().distanceSq(citizen.getPosition().getX(), citizen.getPosition().getY(), citizen.getPosition().getZ()) < MOVE_AWAY_RANGE)
        {
            stuckTime = 0;
            return;
        }

        if (!new AxisAlignedBB(citizen.getCurrentPosition()).expand(1, 1, 1)
               .intersects(new AxisAlignedBB(citizen.getPosition())) && movingAwayAttempts <= MOVE_AWAY_RETRIES)
        {
            stuckTime = 0;
            citizen.setCurrentPosition(citizen.getPosition());
            return;
        }

        stuckTime++;

        if (stuckTime >= MIN_STUCK_TIME + citizen.getRandom().nextInt(MIN_STUCK_TIME) && movingAwayAttempts <= MOVE_AWAY_RETRIES)
        {
            stuckTime = 0;
            movingAwayAttempts++;
            citizen.getNavigator().moveAwayFromXYZ(citizen.getCurrentPosition(), MIN_MOVE_AWAY_RANGE + citizen.getRandom().nextInt(MOVE_AWAY_RANGE), 1);
            return;
        }

        if (stuckTime >= MAX_STUCK_TIME)
        {
            if (citizen.getNavigator().getDestination().distanceSq(citizen.getPosition().getX(), citizen.getPosition().getY(), citizen.getPosition().getZ()) < MOVE_AWAY_RANGE
                  || (citizen.getNavigator().getDestination().getY() - citizen.getPosition().getY() > 2))
            {
                stuckTime = 0;
                return;
            }

            movingAwayAttempts = 0;

            final BlockPos destination = BlockPosUtil.getFloor(citizen.getNavigator().getDestination().up(), CompatibilityUtils.getWorldFromCitizen(citizen));
            @Nullable final BlockPos spawnPoint =
              Utils.scanForBlockNearPoint
                      (CompatibilityUtils.getWorldFromCitizen(citizen), destination, 1, 1, 1, 3,
                        Blocks.AIR,
                        Blocks.SNOW_LAYER,
                        Blocks.TALLGRASS,
                        Blocks.RED_FLOWER,
                        Blocks.YELLOW_FLOWER,
                        Blocks.CARPET);

            WorkerUtil.setSpawnPoint(spawnPoint, citizen);
            if (citizen.getCitizenColonyHandler().getColony() != null)
            {
                Log.getLogger().info("Teleported stuck citizen " + citizen.getName() + " from colony: " + citizen.getCitizenColonyHandler().getColonyId() + " to target location");
            }
            stuckTime = 0;
        }

        citizen.setCurrentPosition(citizen.getPosition());
    }
}
