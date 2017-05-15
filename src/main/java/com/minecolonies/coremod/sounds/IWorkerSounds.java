package com.minecolonies.coremod.sounds;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * I worker sounds interface which contains all methods worker sounds should have.
 */
public interface IWorkerSounds
{
    void playSound(final World worldIn, final BlockPos position, final boolean isFemale);


}
