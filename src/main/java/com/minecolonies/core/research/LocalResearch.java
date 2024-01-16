package com.minecolonies.core.research;

import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.ILocalResearch;
import com.minecolonies.api.research.ILocalResearchTree;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.research.effects.IResearchEffectManager;
import com.minecolonies.api.research.util.ResearchState;
import net.minecraft.resources.ResourceLocation;

/**
 * The implementation of the ILocalResearch interface which represents one type of research, stored in each colony.
 */
public class LocalResearch implements ILocalResearch
{
    /**
     * Depth of research.
     */
    private final int depth;

    /**
     * The current research state.
     */
    private ResearchState state;

    /**
     * The id of the research.
     */
    private final ResourceLocation id;

    /**
     * The research branch.
     */
    private final ResourceLocation branch;

    /**
     * The progress of the research.
     */
    private int progress;

    /**
     * Create the new research.
     *
     * @param id     it's id.
     * @param depth  the depth in the tree.
     * @param branch the branch it is on.
     */
    public LocalResearch(final ResourceLocation id, final ResourceLocation branch, final int depth)
    {
        this.id = id;
        this.depth = depth;
        this.branch = branch;
    }

    @Override
    public boolean research(final IResearchEffectManager effects, final ILocalResearchTree tree)
    {
        if (state == ResearchState.IN_PROGRESS)
        {
            progress++;
            if (progress >= IGlobalResearchTree.getInstance().getBranchData(branch).getBaseTime(this.depth))
            {
                state = ResearchState.FINISHED;
                for(final IResearchEffect<?> effect : IGlobalResearchTree.getInstance().getResearch(this.branch, this.getId()).getEffects())
                {
                    effects.applyEffect(effect);
                }
                tree.finishResearch(this.id);
                return true;
            }
        }
        return false;
    }

    @Override
    public int getProgress()
    {
        return progress;
    }

    @Override
    public ResourceLocation getId()
    {
        return this.id;
    }

    @Override
    public ResearchState getState()
    {
        return this.state;
    }

    @Override
    public ResourceLocation getBranch()
    {
        return this.branch;
    }

    @Override
    public int getDepth()
    {
        return depth;
    }

    @Override
    public void setState(final ResearchState value)
    {
        this.state = value;
    }

    @Override
    public void setProgress(final int progress)
    {
        this.progress = progress;
    }
}
