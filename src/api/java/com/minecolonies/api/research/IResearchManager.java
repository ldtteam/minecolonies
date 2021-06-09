package com.minecolonies.api.research;

import com.minecolonies.api.research.effects.IResearchEffectManager;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Research manager of the colony holding the tree and effects.
 */
public interface IResearchManager
{
    /**
     * Reads all stats from nbt.
     *
     * @param compound the compound.
     */
    void readFromNBT(@NotNull final CompoundNBT compound);

    /**
     * Write all stats to nbt.
     *
     * @param statsCompound the compound.
     */
    void writeToNBT(@NotNull final CompoundNBT statsCompound);

    /**
     * Get the instance of the researchTree.
     *
     * @return the ResearchTree object.
     */
    ILocalResearchTree getResearchTree();

    /**
     * Get an instance of the research effects.
     *
     * @return the ResearchEffects object.
     */
    IResearchEffectManager getResearchEffects();

    /**
     * Gets the Research Effect Identifier for a given Block
     * Format is namespace:effects/path
     * @param block       The block to get a research identifier for.
     * @return            The string format of that research identifier.
     */
    ResourceLocation getResearchEffectIdFrom(Block block);

    /**
     * Checks if any autostart research has its prerequisites filled,
     * and if so, prompts the player for resources or begins research if no resources required.
     */
    void checkAutoStartResearch();
}
