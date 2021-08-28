package com.minecolonies.api.research.effects;

import net.minecraft.resources.ResourceLocation;

/**
 * Abstract research effect.
 */
public abstract class AbstractResearchEffect<T> implements IResearchEffect<T>
{
    /**
     * The String id of the research effect.
     */
    private final ResourceLocation id;

    /**
     * The constructor to create a new research effect.
     *
     * @param id the id to unlock.
     */
    public AbstractResearchEffect(final ResourceLocation id)
    {
        this.id = id;
    }

    @Override
    public ResourceLocation getId()
    {
        return this.id;
    }
}
