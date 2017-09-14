package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.shepherd.EntityAIWorkShepherd;
import net.minecraft.entity.passive.EntitySheep;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Asher on 3/9/17.
 */
public class JobShepherd extends AbstractJob
{

    /**
     * The water the fisherman is currently fishing at
     * Contains the location of the water so that the fisherman can path to the fishing spot.
     */
    private List<EntitySheep> sheep = new ArrayList<>();

    /**
     * Instantiates the placeholder job.
     *
     * @param entity the entity.
     */
    public JobShepherd(final CitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.Shepherd";
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @Nullable
    @Override
    public AbstractAISkeleton<JobShepherd> generateAI()
    {
        return new EntityAIWorkShepherd(this);
    }

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    @NotNull
    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.FARMER;
    }

    /**
     * Remove dead sheep from List
     */
    public void removeDeadSheep()
    {
        final List<EntitySheep> sheepToRemove = new ArrayList<>();

        sheep.stream().filter(sheepie ->
                                sheepie == null || !sheepie.isEntityAlive()).forEach(sheepToRemove::add);

        sheep.removeAll(sheepToRemove);
    }

    /**
     * Getter for current sheep.
     *
     * @return Location of the current sheep.
     */
    public List<EntitySheep> getSheep()
    {
        return new ArrayList<>(sheep);
    }

    /**
     * Setter for current sheep.
     *
     * @param sheep New location for the current sheep.
     */
    public void setSheep(final List<EntitySheep> sheep)
    {
        this.sheep = new ArrayList<>(sheep);
    }
}
