package com.minecolonies.coremod.entity.citizenhandlers;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Utils;
import com.minecolonies.coremod.entity.EntityCitizen;
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
public class CitizenStuckHandler
{
    /**
     * The citizen assigned to this manager.
     */
    private final EntityCitizen citizen;

    /**
     * Field to try moving away from a location in order to pass it.
     */
    private boolean triedMovingAway = false;

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
    public void onUpdate()
    {
        if (citizen.ticksExisted % TICKS_20 == 0)
        {
            checkIfStuck();

            if (citizen.ticksExisted % (MAX_STUCK_TIME * TICKS_SECOND) == 0)
            {
                triedMovingAway = false;
            }
        }
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

        if (citizen.getNavigator().getDestination() == null || citizen.getNavigator().getDestination().distanceSq(citizen.posX, citizen.posY, citizen.posZ) < MOVE_AWAY_RANGE)
        {
            return;
        }

        if (!new AxisAlignedBB(citizen.getCurrentPosition()).expand(1, 1, 1)
               .intersects(new AxisAlignedBB(citizen.getPosition())) && !triedMovingAway)
        {
            stuckTime = 0;
            citizen.setCurrentPosition(citizen.getPosition());
            return;
        }

        stuckTime++;

        if (stuckTime >= MIN_STUCK_TIME + citizen.getRandom().nextInt(MIN_STUCK_TIME) && !triedMovingAway)
        {
            triedMovingAway = true;
            citizen.getNavigator().moveAwayFromXYZ(citizen.getCurrentPosition(), citizen.getRandom().nextInt(MOVE_AWAY_RANGE), 1);
            return;
        }

        if (stuckTime >= MAX_STUCK_TIME)
        {
            if (citizen.getNavigator().getDestination().distanceSq(citizen.posX, citizen.posY, citizen.posZ) < MOVE_AWAY_RANGE)
            {
                stuckTime = 0;
                return;
            }

            triedMovingAway = false;

            final BlockPos destination = BlockPosUtil.getFloor(citizen.getNavigator().getDestination().up(), CompatibilityUtils.getWorld(citizen));
            @Nullable final BlockPos spawnPoint =
              Utils.scanForBlockNearPoint
                      (CompatibilityUtils.getWorld(citizen), destination, 1, 1, 1, 3,
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
