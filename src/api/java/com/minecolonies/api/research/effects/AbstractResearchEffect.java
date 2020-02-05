package com.minecolonies.api.research.effects;

/**
 * Abstract research effect.
 */
public abstract class AbstractResearchEffect<T> implements IResearchEffect<T>
{
    /**
     * The String id of the research effect.
     */
    private final String id;

    /**
     * The constructor to create a new research effect.
     * @param id the id to unlock.
     */
    public AbstractResearchEffect(final String id)
    {
        this.id = id;
    }

    @Override
    public String getId()
    {
        return this.id;
    }
}
