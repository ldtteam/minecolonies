package com.minecolonies.api.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;

public interface IGlobalResearchBranch
{
    /**
     * Get the human-readable name or translation key for the Research Branch
     * @return Human-readable name or translation key.
     */
    TranslatableContents getName();

    /**
     * Get the optional human-readable subtitle or its translation key for the Research Branch
     * @return Human-readable subtitle or translation key.
     */
    TranslatableContents getSubtitle();

    /**
     * Get the base progress requirements for non-instant research on the branch.
     * @param depth The university level for the research.
     * @return The number of progress 'ticks' required to complete the research for the included depth.
     *         Each tick is (on average) 25 seconds, but this depends on researcher availability and may be reduced by stored researcher time.
     */
    int getBaseTime(final int depth);

    /**
     * Get the base progress time requirements for non-instant research on the branch.
     * Use only for direct display purposes, as it is likely to be inaccurate. Favor getBaseTime for any mathematical use.
     * @param depth The university level for the research.
     * @return The number of hours required to complete a research for the included depth, to a rough estimate.
     */
    double getHoursTime(final int depth);

    /**
     * Get the sort order numeral for the branch's placement on the University Hut GUI.
     * @return The sort order.
     */
    int getSortOrder();

    /**
     * Get the branch style type identifier.
     * @return the branch's style, used for logic and presentation.
     */
    ResearchBranchType getType();

    /**
     * Get if the branch should be hidden, even from the selection list, until at least one research is eligible to begin.
     * @return True if the branch shouldn't be visible until at least one research may begin.
     */
    boolean getHidden();

    /**
     * Write the Branch characteristics to an NBT for serialization.
     * @return a compoundNBT containing the necessary traits to make the branch data on a client.
     */
    CompoundTag writeToNBT();
}
