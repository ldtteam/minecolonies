package com.minecolonies.coremod.datalistener;

import com.google.gson.*;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.registry.IResearchEffectRegistry;
import com.minecolonies.api.util.Log;

import com.minecolonies.coremod.research.GlobalResearch;
import com.minecolonies.coremod.research.registry.ResearchEffectRegistry;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Loader for Json based crafter specific recipes
 */
public class ResearchListener extends JsonReloadListener
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Set up the core loading, with the directory in the datapack that contains this data
     * Directory is: <namespace>/researches/<path>
     */
    public ResearchListener()
    {
        super(GSON, "researches");
    }

    @Override
    protected void apply(final Map<ResourceLocation, JsonElement> object, final IResourceManager resourceManagerIn, final IProfiler profilerIn)
    {
        Log.getLogger().info("Beginning load of research for University.");

        // First, populate a new map of IGlobalResearches, identified by researchID.
        // This allows us to figure out root/branch relationships more sanely.
        final Map<String, GlobalResearch> researchMap = new HashMap<String, GlobalResearch>();
        for(final Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet())
        {
            //Note that we don't actually use the resource folders or file names; those are only for organization purposes.
            JsonObject researchJson = entry.getValue().getAsJsonObject();

            //Check for absolute minimum required types, and log as warning if malformed.
            if (!researchJson.has(GlobalResearch.RESEARCH_ID_PROP)
                  || !researchJson.has(GlobalResearch.RESEARCH_NAME_PROP)
                  || !researchJson.has(GlobalResearch.RESEARCH_BRANCH_PROP)
                  || !researchJson.has(GlobalResearch.RESEARCH_UNIVERSITY_LEVEL_PROP))
            {
                Log.getLogger().warn(entry.getKey() + "missing required fields");
                continue;
            }

            //Pretty much anything else should be allowed: it's plausible pack designers may want a research type without a cost or effect.
            //It's possible we could dynamically derive university levels from parents, but doing so as a rule will prevent research branches that start at T2 or deeper.
            final GlobalResearch research = new GlobalResearch(researchJson);
            researchMap.put(research.getId(), research);
        }

        // After we've loaded all researches, we can then try to assign child relationships.
        // This is also the phase where we'd try to support back-calculating university levels for researches without them/with incorrect ones.
        IGlobalResearchTree researchTree =  MinecoloniesAPIProxy.getInstance().getGlobalResearchTree();
        // Datapacks are loaded every time the world starts, and so have to be cleared out or can see unexpected behavior.
        researchTree.clearBranches();

        for(final Map.Entry<String, GlobalResearch> entry : researchMap.entrySet())
        {
            if(!entry.getValue().getParent().isEmpty())
            {
                if(researchMap.containsKey(entry.getValue().getParent()))
                {
                    if (researchMap.get(entry.getValue().getParent()).getBranch().equals(entry.getValue().getBranch()))
                    {
                        researchMap.get(entry.getValue().getParent()).addChild(entry.getValue());
                    }
                    else
                    {
                        //For now, just log malformed parent/child relationships.  It may be preferable to handle well enough to display it as a solo branch, to aid debugging.
                        Log.getLogger().error(entry.getValue().getBranch() + "/" + entry.getKey() + "could not be attached to " + entry.getValue().getParent() + " on " + researchMap.get(entry.getValue().getParent()).getBranch());
                    }
                }
                else
                {
                    //For now, just log malformed parent/child relationships.  It may be preferable to handle well enough to display it as a solo branch, to aid debugging.
                    Log.getLogger().error(entry.getValue().getBranch() + "/" + entry.getKey() + "could not find parent" + researchMap.containsKey(entry.getValue().getParent()));
                    continue;
                }
            }
            researchTree.addResearch(entry.getValue().getBranch(), entry.getValue());
        }

        Log.getLogger().info("Loaded " + researchMap.values().size() + " recipes for " + researchTree.getBranches().size() + " branches");
    }
}