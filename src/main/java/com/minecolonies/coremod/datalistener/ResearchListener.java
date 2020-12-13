package com.minecolonies.coremod.datalistener;

import com.google.gson.*;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.util.Log;

import com.minecolonies.coremod.research.GlobalResearch;
import com.minecolonies.coremod.research.ResearchEffectCategory;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.coremod.research.GlobalResearch.*;
import static com.minecolonies.coremod.research.ResearchEffectCategory.*;

/**
 * Loader for Json-based researches
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

        // First, index and map out all research effects.  We need to be able to map them before creating Researches themselves.
        // Because datapacks, can't assume effects are in one specific location.
        // For now, we'll populate relative levels when doing so, but we probably want to do that dynamically.
        final Map<String, ResearchEffectCategory> effectCategories = parseResearchEffects(object);

        // Next, populate a new map of IGlobalResearches, identified by researchID.
        // This allows us to figure out root/branch relationships more sanely.
        // We need the effectCategories and levels to do this.
        final Map<String, GlobalResearch> researchMap = parseResearches(object, effectCategories);

        // We /shouldn't/ get any removes before the Research they're trying to remove exists,
        // but it can happen if multiple datapacks affect each other.
        // So now that we've loaded everything, then we can start removes.
        parseRemoveResearches(object, researchMap);

        // After we've loaded all researches, we can then try to assign child relationships.
        // This is also the phase where we'd try to support back-calculating university levels for researches without them/with incorrect ones.
        final IGlobalResearchTree researchTree = calcResearchTree(researchMap);

        Log.getLogger().info("Loaded " + researchMap.values().size() + " recipes for " + researchTree.getBranches().size() + " research branches");
    }

    /**
     * Parses out a json map for elements containing ResearchEffects, categorizes those effects, and calculates relative values for each effect level.
     *
     * @param object    A Map containing the resource location of each json file, and the element within that json file.
     * @return          A Map containing the ResearchEffectIds and ResearchEffectCategories each ID corresponds to.
     */
    private Map<String, ResearchEffectCategory> parseResearchEffects(final Map<ResourceLocation, JsonElement> object)
    {
        final Map<String, ResearchEffectCategory> effectCategories = new HashMap<>();
        for(final Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet())
        {
            final JsonObject effectJson = entry.getValue().getAsJsonObject();

            if (effectJson.has(RESEARCH_EFFECT_PROP) && effectJson.has(RESEARCH_ID_PROP)
                  && effectJson.get(RESEARCH_ID_PROP).isJsonPrimitive() && effectJson.get(RESEARCH_ID_PROP).getAsJsonPrimitive().isString())
            {
                final ResearchEffectCategory category;
                if((effectJson.has(RESEARCH_NAME_PROP) && effectJson.get(RESEARCH_NAME_PROP).isJsonPrimitive() && effectJson.get(RESEARCH_NAME_PROP).getAsJsonPrimitive().isString()))
                {
                    category = new ResearchEffectCategory(effectJson.get(RESEARCH_ID_PROP).getAsString(), effectJson.get(RESEARCH_NAME_PROP).getAsString());
                }
                else
                {
                    category = new ResearchEffectCategory(effectJson.get(RESEARCH_ID_PROP).getAsString());
                }
                if (effectJson.has(RESEARCH_EFFECT_LEVELS_PROP) && effectJson.get(RESEARCH_EFFECT_LEVELS_PROP).isJsonArray())
                {
                    for(JsonElement levelElement : effectJson.get(RESEARCH_EFFECT_LEVELS_PROP).getAsJsonArray())
                    {
                        if(levelElement.isJsonPrimitive() && levelElement.getAsJsonPrimitive().isNumber())
                        {
                            category.add(levelElement.getAsNumber().floatValue());
                        }
                    }
                }
                // If no levels are defined, assume temporarily boolean, and store on/off.
                else
                {
                    category.add(10f);
                }
                effectCategories.put(category.getId(), category);
            }
            // Files which declare effect: or effectType:, but lack ID or have the wrong types are malformed.
            else if (effectJson.has(RESEARCH_EFFECT_PROP))
            {
                Log.getLogger().error(entry.getKey() + "is a research effect, but does not contain all required fields.  Research Effects must have effect: and id:string fields.");
            }
        }
        return effectCategories;
    }

    /**
     * Parses out a json map for elements containing Researches, validates that they have required fields, and generates a GlobalResearch for each.
     *
     * @param object    A Map containing the resource location of each json file, and the element within that json file.
     * @return          A Map containing the ResearchIds and the GlobalResearches each ID corresponds to.
     */
    private Map<String, GlobalResearch> parseResearches(final Map<ResourceLocation, JsonElement> object, final Map<String, ResearchEffectCategory> effectCategories)
    {
        final Map<String, GlobalResearch> researchMap = new HashMap<String, GlobalResearch>();
        for(final Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet())
        {
            //Note that we don't actually use the resource folders or file names; those are only for organization purposes.
            final JsonObject researchJson = entry.getValue().getAsJsonObject();

            //Can ignore those effect jsons now:
            if (researchJson.has(RESEARCH_EFFECT_PROP))
            {
                continue;
            }

            //Next, check for remove-type recipes.  We don't want to accidentally add them just because they have too much detail.
            if ((researchJson.has(RESEARCH_REMOVE_PROP) && researchJson.get(RESEARCH_REMOVE_PROP).getAsJsonPrimitive().isBoolean())
                  && researchJson.get(RESEARCH_REMOVE_PROP).getAsJsonPrimitive().getAsBoolean())
            {
                continue;
            }

            //Check for absolute minimum required types, and log as warning if malformed.
            if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get() &&
                  !(researchJson.has(RESEARCH_ID_PROP) && researchJson.get(RESEARCH_ID_PROP).isJsonPrimitive()
                    && researchJson.get(RESEARCH_ID_PROP).getAsJsonPrimitive().isString())
                  || !(researchJson.has(RESEARCH_BRANCH_PROP) && researchJson.get(RESEARCH_BRANCH_PROP).isJsonPrimitive()
                         && researchJson.get(RESEARCH_BRANCH_PROP).getAsJsonPrimitive().isString()))
            {
                Log.getLogger().warn(entry.getKey() + "is a Research, but does not contain all required fields.  Researches must have id:string, and branch:string properties.");
                continue;
            }

            //Missing university level data may not necessarily be a show-stopper, but it is worth warning about.
            if(MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get() &&
                 (researchJson.has(RESEARCH_UNIVERSITY_LEVEL_PROP) && researchJson.get(RESEARCH_ID_PROP).getAsJsonPrimitive().isNumber()))
            {
                Log.getLogger().warn(entry.getKey() + "is a Research, but has invalid or no university level requirements.");
            }

            //Pretty much anything else should be allowed: it's plausible pack designers may want a research type without a cost or effect.
            //It's possible we could dynamically derive university levels from parents, but doing so as a rule will prevent research branches that start at T2 or deeper.
            final GlobalResearch research = new GlobalResearch(researchJson, entry.getKey(), effectCategories);
            if(MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get())
            {
                Log.getLogger().info("Parsed research recipe from " + entry.getKey() + " [" + research.getBranch() + "/" + research.getId() + "]");
                Log.getLogger().info(research.getDesc() + " at " + research.getDepth() + "/" + research.getParent());
                for(IResearchRequirement requirement : research.getResearchRequirement())
                {
                    Log.getLogger().info("Requirement: " + requirement.getDesc());
                }
                for(ItemStorage itemS : research.getCostList())
                {
                    Log.getLogger().info("Cost: " + itemS.toString());
                }
                for(IResearchEffect researchEffect : research.getEffects())
                {
                    Log.getLogger().info("Effect: " + researchEffect.getId() + " " + researchEffect.getDesc());
                }
            }
            researchMap.put(research.getId(), research);
        }
        return researchMap;
    }

    /**
     * Parses out a researches map for elements containing Remove properties, and applies those removals to the researchMap
     *
     * @param object        A Map containing the resource location of each json file, and the element within that json file.
     * @param researchMap   A Map to apply those Research Removes against.
     */
    private void parseRemoveResearches(final Map<ResourceLocation, JsonElement> object, final Map<String, GlobalResearch> researchMap)
    {
        for(final Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet())
        {
            final JsonObject researchJson = entry.getValue().getAsJsonObject();

            //not allowing duplicate id across separate branches for now, so we just need removes and Id.
            if ((researchJson.has(RESEARCH_REMOVE_PROP) && researchJson.get(RESEARCH_REMOVE_PROP).getAsJsonPrimitive().isBoolean())
                  && researchJson.get(RESEARCH_REMOVE_PROP).getAsJsonPrimitive().getAsBoolean()
                  && researchJson.has(RESEARCH_ID_PROP) && researchJson.get(RESEARCH_ID_PROP).getAsJsonPrimitive().isString()
                /*&& (researchJson.has(RESEARCH_BRANCH_PROP) && researchJson.get(RESEARCH_BRANCH_PROP).getAsJsonPrimitive().isString())*/)
            {
                if(researchMap.containsKey(researchJson.get(RESEARCH_ID_PROP).getAsString()))
                {
                    researchMap.remove(researchJson.get(RESEARCH_ID_PROP).getAsString());
                }
            }
            // Files which declare remove:, but lack ID or have the wrong types are malformed.
            else if (researchJson.has(RESEARCH_REMOVE_PROP))
            {
                Log.getLogger().error(entry.getKey() + "is a research remove, but does not contain all required fields.  Research Removes must have remove:boolean and id:string.");
            }
        }
    }

    /**
     * Parses out a GlobalResearch map to apply parent/child relationships between researches, and to graft and warn about inconsistent relationships.
     *
     * @param researchMap   A Map of ResearchIDs to GlobalResearches to turn into a GlobalResearchTree.
     * @return              An IGlobalResearchTree containing the validated researches.
     */
    private IGlobalResearchTree calcResearchTree(final Map<String, GlobalResearch> researchMap)
    {
        final IGlobalResearchTree researchTree =  MinecoloniesAPIProxy.getInstance().getGlobalResearchTree();
        // The research tree should be reset on world unload, but certain events and disconnects break that.  Do it here, too.
        researchTree.reset();
        for (final Map.Entry<String, GlobalResearch> entry : researchMap.entrySet())
        {
            if (!entry.getValue().getParent().isEmpty())
            {
                if (researchMap.containsKey(entry.getValue().getParent()))
                {
                    if (researchMap.get(entry.getValue().getParent()).getBranch().equals(entry.getValue().getBranch()))
                    {
                        researchMap.get(entry.getValue().getParent()).addChild(entry.getValue());
                    }
                    else
                    {
                        //For now, log and re-graft entries with inconsistent parent-child relationships to a separate branch.
                        Log.getLogger()
                          .error(entry.getValue().getBranch() + "/" + entry.getKey() + "could not be attached to " + entry.getValue().getParent() + " on "
                                   + researchMap.get(entry.getValue().getParent()).getBranch());
                        entry.setValue(new GlobalResearch(entry.getValue().getId(), entry.getValue().getBranch(), 0, entry.getValue().getEffects()));
                    }
                }
                else
                {
                    //For now, log and re-graft entries with inconsistent parent-child relationships to a separate branch.
                    Log.getLogger().error(entry.getValue().getBranch() + "/" + entry.getKey() + "could not find parent" + researchMap.containsKey(entry.getValue().getParent()));
                    entry.setValue(new GlobalResearch(entry.getValue().getId(), entry.getValue().getBranch(), 0, entry.getValue().getEffects()));
                }
            }
            researchTree.addResearch(entry.getValue().getBranch(), entry.getValue(), true);
        }
        return researchTree;
    }
}