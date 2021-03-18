package com.minecolonies.api.research;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;

public interface IGlobalResearchBranch
{
    /**
     * Get the human-readable name or translation key for the Research Branch
     * @return Human-readable name or translation key.
     */
    TranslationTextComponent getName();

    /**
     * Get the optional human-readable subtitle or its translation key for the Research Branch
     * @return Human-readable subtitle or translation key.
     */
    TranslationTextComponent getSubtitle();

    /**
     * Get the base time requirements for non-instant research on the branch.
     * @return A multiplier for research time.  Defaults to 1.0
     */
    double getBaseTime();

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
    CompoundNBT writeToNBT();
}
