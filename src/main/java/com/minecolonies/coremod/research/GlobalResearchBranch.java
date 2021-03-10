package com.minecolonies.coremod.research;

import com.google.gson.JsonObject;
import com.minecolonies.api.research.IGlobalResearchBranch;
import com.minecolonies.api.research.ResearchBranchType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class GlobalResearchBranch implements IGlobalResearchBranch
{
    /**
     * The property name for research branch name keys.  Only applies at the level of branch settings.
     * May be a human-readable text, or a translation key.
     */
    public static final String RESEARCH_BRANCH_NAME_PROP = "branch-name";

    /**
     * The property name for the branch type tag.
     */
    public static final String RESEARCH_BRANCH_TYPE_PROP = "branch-type";

    /**
     * The property name for branch's research time modifier.  Only applies at the level of branch settings.
     */
    public static final String RESEARCH_BASE_TIME_PROP = "base-time";

    /**
     * The property name for the research which is only visible when its requirements are fulfilled.
     */
    private static final String RESEARCH_HIDDEN_PROP = "hidden";

    /**
     * The property name for the sort order tag.
     */
    private static final String RESEARCH_SORT_PROP = "sortOrder";

    private final TranslationTextComponent name;
    private final ResearchBranchType type;
    private final double baseTime;
    private final int sortOrder;
    private final boolean hidden;

    @Override
    public TranslationTextComponent getName(){return this.name;}

    @Override
    public double getBaseTime(){return this.baseTime;}

    @Override
    public int getSortOrder(){return this.sortOrder;}

    @Override
    public ResearchBranchType getType(){return this.type;}

    @Override
    public boolean getHidden(){return this.hidden;}

    public GlobalResearchBranch(final ResourceLocation id)
    {
        if(id.getPath().isEmpty())
        {
            // yes, technically "/.json" is a valid file name.
            this.name = new TranslationTextComponent("");
        }
        else
        {
            this.name = new TranslationTextComponent(id.getPath().substring(0, 1).toUpperCase() + id.getPath().substring(1));
        }
        this.baseTime = 1.0;
        this.type = ResearchBranchType.DEFAULT;
        this.hidden = false;
        // branches without a declared sortOrder will float to the 'top'.
        // note that this does support negative numbers.
        this.sortOrder = 0;
    }

    public GlobalResearchBranch(final ResourceLocation id, final JsonObject researchJson)
    {
        // Research branches can have all, only some, or none of these traits.
        if (researchJson.has(RESEARCH_BRANCH_NAME_PROP) && researchJson.get(RESEARCH_BRANCH_NAME_PROP).isJsonPrimitive()
              && researchJson.get(RESEARCH_BRANCH_NAME_PROP).getAsJsonPrimitive().isString())
        {
            this.name = new TranslationTextComponent(researchJson.get(RESEARCH_BRANCH_NAME_PROP).getAsJsonPrimitive().getAsString());
        }
        else
        {
            if(id.getPath().isEmpty())
            {
                // yes, technically "/.json" is a valid file name.
                this.name = new TranslationTextComponent("");
            }
            else
            {
                this.name = new TranslationTextComponent(id.getPath().substring(0, 1).toUpperCase() + id.getPath().substring(1));
            }
        }
        if (researchJson.has(RESEARCH_BASE_TIME_PROP) && researchJson.get(RESEARCH_BASE_TIME_PROP).isJsonPrimitive()
              && researchJson.get(RESEARCH_BASE_TIME_PROP).getAsJsonPrimitive().isNumber())
        {
            this.baseTime = researchJson.get(RESEARCH_BASE_TIME_PROP).getAsJsonPrimitive().getAsDouble();
        }
        else
        {
            this.baseTime = 1.0;
        }
        if (researchJson.has(RESEARCH_SORT_PROP) && researchJson.get(RESEARCH_SORT_PROP).isJsonPrimitive()
              && researchJson.get(RESEARCH_SORT_PROP).getAsJsonPrimitive().isNumber())
        {
            this.sortOrder = researchJson.get(RESEARCH_SORT_PROP).getAsJsonPrimitive().getAsInt();
        }
        else
        {
            // branches without a declared sortOrder will float to the 'top' and then be sorted by alphabetic order.
            // note that this does support negative numbers.
            this.sortOrder = 0;
        }
        if (researchJson.has(RESEARCH_BRANCH_TYPE_PROP) && researchJson.get(RESEARCH_BRANCH_TYPE_PROP).isJsonPrimitive()
              && researchJson.get(RESEARCH_BRANCH_TYPE_PROP).getAsJsonPrimitive().isString())
        {
            this.type = ResearchBranchType.valueOfTag(researchJson.get(RESEARCH_BRANCH_TYPE_PROP).getAsJsonPrimitive().getAsString());
        }
        else
        {
            this.type = ResearchBranchType.DEFAULT;
        }
        if (researchJson.has(RESEARCH_HIDDEN_PROP) && researchJson.get(RESEARCH_HIDDEN_PROP).isJsonPrimitive()
              && researchJson.get(RESEARCH_HIDDEN_PROP).getAsJsonPrimitive().isBoolean())
        {
            this.hidden = researchJson.get(RESEARCH_HIDDEN_PROP).getAsJsonPrimitive().getAsBoolean();
        }
        else
        {
            this.hidden = false;
        }
    }

    public GlobalResearchBranch(final CompoundNBT nbt)
    {
        this.name = new TranslationTextComponent(nbt.getString(RESEARCH_BRANCH_NAME_PROP));
        this.type = ResearchBranchType.valueOfTag(nbt.getString(RESEARCH_BRANCH_TYPE_PROP));
        this.baseTime = nbt.getDouble(RESEARCH_BASE_TIME_PROP);
        this.sortOrder = nbt.getInt(RESEARCH_SORT_PROP);
        this.hidden = nbt.getBoolean(RESEARCH_HIDDEN_PROP);
    }

    @Override
    public CompoundNBT writeToNBT()
    {
        final CompoundNBT nbt = new CompoundNBT();
        nbt.putString(RESEARCH_BRANCH_NAME_PROP, this.name.getKey());
        nbt.putString(RESEARCH_BRANCH_TYPE_PROP, this.type.tag);
        nbt.putDouble(RESEARCH_BASE_TIME_PROP, this.baseTime);
        nbt.putInt(RESEARCH_SORT_PROP, this.sortOrder);
        nbt.putBoolean(RESEARCH_HIDDEN_PROP, this.hidden);
        return nbt;
    }
}
