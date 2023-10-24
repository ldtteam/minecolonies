package com.minecolonies.coremod.research;

import com.google.gson.JsonObject;
import com.minecolonies.api.research.IGlobalResearchBranch;
import com.minecolonies.api.research.ResearchBranchType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;

import static com.minecolonies.api.research.util.ResearchConstants.BASE_RESEARCH_TIME;

public class GlobalResearchBranch implements IGlobalResearchBranch
{
    /**
     * The property name for research branch name keys.  Only applies at the level of branch settings.
     * May be a human-readable text, or a translation key.
     */
    public static final String RESEARCH_BRANCH_NAME_PROP = "branch-name";

    /**
     * The property name for the subtitle tag.
     */
    private static final String RESEARCH_SUBTITLE_PROP = "subtitle";

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

    /**
     * The translation key or human-readable name of the branch.
     */
    private final TranslatableContents name;

    /**
     * The optional subtitle translation key or human-readable subtitle for the branch.
     */
    private final TranslatableContents subtitle;

    /**
     * The research branch styling type.
     */
    private final ResearchBranchType type;

    /**
     * The branch's research time multiplier for non-instant research.
     */
    private final double baseTime;

    /**
     * The branch's sorting order on the University GUI.  Higher numbers go toward the bottom.
     */
    private final int sortOrder;

    /**
     * If the branch should not be listed on the University GUI if all primary research for the branch is hidden.
     * Has no effect if any research on the branch is set to not hidden, or if any research has no requirements.
     */
    private final boolean hidden;

    @Override
    public TranslatableContents getName(){return this.name;}

    @Override
    public TranslatableContents getSubtitle(){return this.subtitle;}

    @Override
    public int getBaseTime(final int depth)
    {
        return (int)(BASE_RESEARCH_TIME * this.baseTime * Math.pow(2, depth - 1));
    }

    @Override
    public double getHoursTime(final int depth)
    {
        return (getBaseTime(depth) * 25.0) / 60 / 60;
    }

    @Override
    public int getSortOrder(){return this.sortOrder;}

    @Override
    public ResearchBranchType getType(){return this.type;}

    @Override
    public boolean getHidden(){return this.hidden;}

    /**
     * Creates a GlobalResearchBranch containing default values for a given BranchID.
     * @param id   The Branch's ResourceID.
     */
    public GlobalResearchBranch(final ResourceLocation id)
    {
        if(id.getPath().isEmpty())
        {
            // yes, technically "/.json" is a valid file name and "" is a valid resource location.
            this.name = new TranslatableContents("", null, TranslatableContents.NO_ARGS);
        }
        else
        {
            this.name = new TranslatableContents(id.getPath().substring(0, 1).toUpperCase() + id.getPath().substring(1), null, TranslatableContents.NO_ARGS);
        }
        this.subtitle = new TranslatableContents("", null, TranslatableContents.NO_ARGS);
        this.baseTime = 1.0;
        this.type = ResearchBranchType.DEFAULT;
        this.hidden = false;
        // branches without a declared sortOrder will float to the bottom of the list.
        this.sortOrder = 1000;
    }

    /**
     * Creates a GlobalResearchBranch from a jsonObject.
     * @param id            The Branch's ResourceID.
     * @param researchJson  the Json containing research branch data.
     */
    public GlobalResearchBranch(final ResourceLocation id, final JsonObject researchJson)
    {
        // Research branches can have all, only some, or none of these traits.
        if (researchJson.has(RESEARCH_BRANCH_NAME_PROP) && researchJson.get(RESEARCH_BRANCH_NAME_PROP).isJsonPrimitive()
              && researchJson.get(RESEARCH_BRANCH_NAME_PROP).getAsJsonPrimitive().isString())
        {
            this.name = new TranslatableContents(researchJson.get(RESEARCH_BRANCH_NAME_PROP).getAsJsonPrimitive().getAsString(), null, TranslatableContents.NO_ARGS);
        }
        else
        {
            if(id.getPath().isEmpty())
            {
                // yes, technically "/.json" is a valid file name.
                this.name = new TranslatableContents("", null, TranslatableContents.NO_ARGS);
            }
            else
            {
                this.name = new TranslatableContents(id.getPath().substring(0, 1).toUpperCase() + id.getPath().substring(1), null, TranslatableContents.NO_ARGS);
            }
        }
        if (researchJson.has(RESEARCH_SUBTITLE_PROP) && researchJson.get(RESEARCH_SUBTITLE_PROP).isJsonPrimitive()
              && researchJson.get(RESEARCH_SUBTITLE_PROP).getAsJsonPrimitive().isString())
        {
            this.subtitle = new TranslatableContents(researchJson.get(RESEARCH_SUBTITLE_PROP).getAsJsonPrimitive().getAsString(), null, TranslatableContents.NO_ARGS);
        }
        else
        {
            this.subtitle = new TranslatableContents("", null, TranslatableContents.NO_ARGS);
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
            // branches without a declared sortOrder will float to the bottom in most normal ranges, and then be sorted by alphabetic order.
            this.sortOrder = 1000;
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

    /**
     * Reassembles a GlobalResearchBranch from its NBT transmission.
     * @param nbt  The nbt containing the Research Branch data.
     */
    public GlobalResearchBranch(final CompoundTag nbt)
    {
        this.name = new TranslatableContents(nbt.getString(RESEARCH_BRANCH_NAME_PROP), null, TranslatableContents.NO_ARGS);
        this.subtitle = new TranslatableContents(nbt.getString(RESEARCH_SUBTITLE_PROP), null, TranslatableContents.NO_ARGS);
        this.type = ResearchBranchType.valueOfTag(nbt.getString(RESEARCH_BRANCH_TYPE_PROP));
        this.baseTime = nbt.getDouble(RESEARCH_BASE_TIME_PROP);
        this.sortOrder = nbt.getInt(RESEARCH_SORT_PROP);
        this.hidden = nbt.getBoolean(RESEARCH_HIDDEN_PROP);
    }

    @Override
    public CompoundTag writeToNBT()
    {
        final CompoundTag nbt = new CompoundTag();
        nbt.putString(RESEARCH_BRANCH_NAME_PROP, this.name.getKey());
        nbt.putString(RESEARCH_SUBTITLE_PROP, this.subtitle.getKey());
        nbt.putString(RESEARCH_BRANCH_TYPE_PROP, this.type.tag);
        nbt.putDouble(RESEARCH_BASE_TIME_PROP, this.baseTime);
        nbt.putInt(RESEARCH_SORT_PROP, this.sortOrder);
        nbt.putBoolean(RESEARCH_HIDDEN_PROP, this.hidden);
        return nbt;
    }
}
