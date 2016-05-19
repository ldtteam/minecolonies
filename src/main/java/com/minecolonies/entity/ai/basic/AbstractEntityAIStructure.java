package com.minecolonies.entity.ai.basic;

import com.minecolonies.colony.jobs.Job;

/**
 * This base ai class is used by ai's who need to build entire structures.
 * These structures have to be supplied as schematics files.
 * <p>
 * Once an ai starts building a structure, control over it is only given back once that is done.
 * <p>
 * If the ai resets, the structure is gone,
 * so just restart building and no progress will be reset
 *
 * @param <J> the job type this AI has to do.
 */
public abstract class AbstractEntityAIStructure<J extends Job> extends AbstractEntityAIInteract<J>
{

    /**
     * Creates this ai base class and set's up important things.
     * <p>
     * Always use this constructor!
     *
     * @param job the job class of the ai using this base class
     */
    protected AbstractEntityAIStructure(J job)
    {
        super(job);
        this.registerTargets(
            //none jet
        );

    }
}
