package com.minecolonies.api.research.effects;

import com.minecolonies.api.research.interfaces.IResearchEffect;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * The modifier research effect, it returns a double modifier.
 */
public abstract class AbstractResearchEffect<T> implements IResearchEffect<T>
{
    /**
     * The String id of the research effect.
     */
    private final String id;

    /**
     * The String id of the research id.
     */
    private String researchId;

    /**
     * The String id of the research branch.
     */
    private String branch;

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


    @Override
    public String getResearchId()
    {
        return researchId;
    }

    @Override
    public String getResearchBranch()
    {
        return branch;
    }

    @Override
    public void setParent(final String researchId, final String branch)
    {
        this.researchId = researchId;
        this.branch = branch;
    }
}
