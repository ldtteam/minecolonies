package com.minecolonies.coremod.colony.managers;

import com.minecolonies.api.research.LocalResearchTree;
import com.minecolonies.api.research.ResearchEffects;
import com.minecolonies.coremod.colony.managers.interfaces.IResearchManager;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

/**
 * Research manager of the colony.
 */
public class ResearchManager implements IResearchManager
{
    /**
     * The research tree of the colony.
     */
    private final LocalResearchTree tree = new LocalResearchTree();

    /**
     * The research effects of the colony.
     */
    private final ResearchEffects effects = new ResearchEffects();

    @Override
    public void readFromNBT(@NotNull final CompoundNBT compound)
    {
        tree.readFromNBT(compound);
        effects.readFromNBT(compound);
    }

    @Override
    public void writeToNBT(@NotNull final CompoundNBT compound)
    {
        effects.writeToNBT(compound);
        tree.writeToNBT(compound);
    }

    @Override
    public LocalResearchTree getResearchTree()
    {
        return this.tree;
    }

    @Override
    public ResearchEffects getResearchEffects()
    {
        return this.effects;
    }
}
