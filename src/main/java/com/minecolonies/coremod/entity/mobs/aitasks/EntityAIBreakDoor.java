package com.minecolonies.coremod.entity.mobs.aitasks;

import net.minecraft.entity.EntityLiving;

/**
 * Break door entity AI with mutex.
 */
public class EntityAIBreakDoor extends net.minecraft.entity.ai.EntityAIBreakDoor
{
    public EntityAIBreakDoor(final EntityLiving entityIn)
    {
        super(entityIn);
        setMutexBits(3);
    }
}
