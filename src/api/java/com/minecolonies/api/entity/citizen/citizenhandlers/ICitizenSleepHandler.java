package com.minecolonies.api.entity.citizen.citizenhandlers;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ICitizenSleepHandler
{
    /**
     * Is the citizen a sleep?
     *
     * @return true when a sleep.
     */
    boolean isAsleep();

    /**
     * Returns the orientation of the bed in degrees.
     */
    @OnlyIn(Dist.CLIENT)
    float getBedOrientationInDegrees();

    /**
     * Attempts a sleep interaction with the citizen and the given bed.
     *
     * @param bedLocation The possible location to sleep.
     */
    void trySleep(BlockPos bedLocation);

    /**
     * Called when the citizen wakes up.
     */
    void onWakeUp();

    /**
     * Get the bed location of the citizen.
     * @return the bed location.
     */
    BlockPos getBedLocation();

    /**
     * Get the X render offset.
     * @return the offset.
     */
    float getRenderOffsetX();

    /**
     * Get the z render offset.
     * @return the offset.
     */
    float getRenderOffsetZ();
}
