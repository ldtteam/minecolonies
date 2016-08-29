package com.minecolonies.entity.ai.citizen.deliveryman;

import com.minecolonies.colony.jobs.JobDeliveryman;
import com.minecolonies.entity.ai.basic.AbstractEntityAIInteract;

/**
 * Performs deliveryman work
 * Created: July 18, 2014
 *
 * @author MrIbby
 */
public class EntityAIWorkDeliveryman extends AbstractEntityAIInteract<JobDeliveryman>
{
    /**
     * Initialize the deliveryman and add all his tasks.
     *
     * @param deliveryman the job he has.
     */
    public EntityAIWorkDeliveryman(JobDeliveryman deliveryman)
    {
        super(deliveryman);
    }
}
