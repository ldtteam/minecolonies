package com.minecolonies.coremod.client.render.worldevent.highlightmanager;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.coremod.client.render.worldevent.WorldEventContext;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * Highlight render data for marking a citizen.
 */
public class CitizenRenderData implements IHighlightRenderData
{
    /**
     * Which citizen should be marked.
     */
    private final int citizenId;

    /**
     * How long the citizen should be marked.
     */
    private final Duration duration;

    /**
     * Default constructor.
     */
    public CitizenRenderData(final int citizenId, final Duration duration)
    {
        this.citizenId = citizenId;
        this.duration = duration;
    }

    @Override
    public void startRender(final WorldEventContext context)
    {
        final EntityCitizen citizenEntity = getCitizenEntity(context);
        if (citizenEntity != null)
        {
            citizenEntity.setGlowingTag(true);
        }
    }

    @Override
    public void render(final WorldEventContext context)
    {
        // No-op
    }

    @Override
    public void stopRender(final WorldEventContext context)
    {
        final EntityCitizen citizenEntity = getCitizenEntity(context);
        if (citizenEntity != null)
        {
            citizenEntity.setGlowingTag(false);
        }
    }

    @Override
    public @Nullable Duration getDuration()
    {
        return duration;
    }

    @Nullable
    private EntityCitizen getCitizenEntity(final WorldEventContext context)
    {
        final IColonyView colony = context.nearestColony;
        if (colony == null)
        {
            return null;
        }

        final ICitizenDataView citizen = colony.getCitizen(citizenId);
        if (citizen == null)
        {
            return null;
        }

        return colony.getWorld().getEntity(citizen.getEntityId()) instanceof EntityCitizen entityCitizen ? entityCitizen : null;
    }
}
