package com.minecolonies.entity.ai;

import com.minecolonies.MineColonies;
import com.minecolonies.entity.EntityCitizen;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

/**
 * EntityCitizen go home AI
 * Created: May 25, 2014
 *
 * @author Colton
 */
public class EntityAIGoHome extends EntityAIBase
{
    private EntityCitizen citizen;

    public EntityAIGoHome(EntityCitizen citizen)
    {
        setMutexBits(1);
        this.citizen = citizen;
    }

    @Override
    public boolean shouldExecute()
    {
        return citizen.getDesiredActivity() == EntityCitizen.DesiredActivity.SLEEP &&
                !citizen.isAtHome() &&
                citizen.getNavigator().noPath();
    }

    @Override
    public void startExecuting()
    {
        ChunkCoordinates pos = citizen.getHomePosition();
        if(pos == null)
        {
            MineColonies.logger.error("EntityCitizen has null townhall (And no home)");
            return;
        }

        citizen.getNavigator().tryMoveToXYZ((double)pos.posX + 0.5D, (double)pos.posY, (double)pos.posZ + 0.5D, 1.0D);
    }

    @Override
    public boolean continueExecuting()
    {
        return !citizen.getNavigator().noPath();
    }
}
