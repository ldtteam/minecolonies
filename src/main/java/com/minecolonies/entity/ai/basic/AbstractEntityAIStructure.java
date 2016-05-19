package com.minecolonies.entity.ai.basic;

import com.minecolonies.colony.jobs.Job;
import com.minecolonies.entity.ai.util.AIState;
import com.minecolonies.entity.ai.util.AITarget;
import com.minecolonies.entity.ai.util.Structure;

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
     * The current structure task to be build.
     */
    private Structure currentStructure;

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
                /**
                 * Check if we have to build something.
                 */
                new AITarget(this::isThereAStructureToBuild, () -> AIState.START_BUILDING),
                /**
                 * Select the appropriate State to do next.
                 */
                new AITarget(AIState.START_BUILDING, this::startBuilding),
                /**
                 * Clear out the building area
                 * todo: implement
                 */
                new AITarget(AIState.CLEAR_STEP, () -> AIState.IDLE),
                /**
                 * Build the structure and foundation of the building
                 * todo: implement
                 */
                new AITarget(AIState.BUILDING_STEP, () -> AIState.IDLE),
                /**
                 * Decorate the Building with torches etc.
                 * todo: implement
                 */
                new AITarget(AIState.DECORATION_STEP, () -> AIState.IDLE),
                /**
                 * Spawn entities on the structure
                 * todo: implement
                 */
                new AITarget(AIState.SPAWN_STEP, () -> AIState.IDLE),
                /**
                 * Finalize the building and give back control to the ai.
                 * todo: implement
                 */
                new AITarget(AIState.COMPLETE_BUILD, () -> AIState.IDLE)
                            );
    }

    /**
     * Check if there is a Structure to be build.
     *
     * @return true if we should start building.
     */
    private boolean isThereAStructureToBuild()
    {
        return currentStructure != null;
    }

    /**
     * Start building this Structure.
     * <p>
     * Will determine where to start.
     *
     * @return the new State to start in.
     */
    private AIState startBuilding()
    {
        switch (currentStructure.getStage())
        {
            case CLEAR:
                return AIState.CLEAR_STEP;
            case BUILD:
                return AIState.BUILDING_STEP;
            case DECORATE:
                return AIState.DECORATION_STEP;
            case SPAWN:
                return AIState.SPAWN_STEP;
            default:
                return AIState.COMPLETE_BUILD;
        }
    }
}
