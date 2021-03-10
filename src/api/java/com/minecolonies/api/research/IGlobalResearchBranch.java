package com.minecolonies.api.research;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;

public interface IGlobalResearchBranch
{
    TranslationTextComponent getName();

    double getBaseTime();

    int getSortOrder();

    ResearchBranchType getType();

    boolean getHidden();

    CompoundNBT writeToNBT();
}
