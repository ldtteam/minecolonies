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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

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
     * The property name that indicates this recipe removes a research.
     */
    public static final String RESEARCH_REMOVE_PROP = "remove";

    /**
     * The property name for research branch name keys.  Only applies at the level of branch settings.
     * May be a human-readable text, or a translation key.
     */
    private static final String RESEARCH_BRANCH_NAME_PROP = "branch-name";

    /**
     * The property name for branch's research time modifier.  Only applies at the level of branch settings.
     */
    private static final String RESEARCH_BASE_TIME_PROP = "base-time";

    /**
     * Set up the core loading, with the directory in the data pack that contains this data
     * Directory is: namespace/researches/path
     */
    public ResearchListener()
    {
        super(GSON, "researches");
    }

    @Override
    protected void apply(@NotNull final Map<ResourceLocation, JsonElement> object, @NotNull final IResourceManager resourceManagerIn, @NotNull final IProfiler profilerIn)
    {
        Log.getLogger().info("Beginning load of research for University.");

        // First, index and map out all research effects.  We need to be able to map them before creating Researches themselves.
        // Because data packs, can't assume effects are in one specific location.
        // For now, we'll populate relative levels when doing so, but we probably want to do that dynamically.
        final Map<ResourceLocation, ResearchEffectCategory> effectCategories = parseResearchEffects(object);

        // Next, populate a new map of IGlobalResearches, identified by researchID.
        // This allows us to figure out root/branch relationships more sanely.
        // We need the effectCategories and levels to do this.
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        final Map<ResourceLocation, GlobalResearch> researchMap = parseResearches(object, effectCategories, !(server instanceof DedicatedServer));

        // We /shouldn't/ get any removes before the Research they're trying to remove exists,
        // but it can happen if multiple data packs affect each other.
        // So now that we've loaded everything, then we can start removes.
        parseRemoveResearches(object, researchMap);

        // After we've loaded all researches, we can then try to assign child relationships.
        // This is also the phase where we'd try to support back-calculating university levels for researches without them/with incorrect ones.
        final IGlobalResearchTree researchTree = calcResearchTree(researchMap);

        // Finally, check for branch-specific settings -- these are optional and only apply to the IGlobalResearchTree.
        parseResearchBranches(object, researchTree);

        Log.getLogger().info("Loaded " + researchMap.values().size() + " recipes for " + researchTree.getBranches().size() + " research branches");

        // For dedicated servers, send to any connected players.  On startup, this will be no-one.
        // But it is possible to reload data packs live, and while not supported it's something to handle.

        if(server instanceof DedicatedServer)
        {
            for(ServerPlayerEntity player : server.getPlayerList().getPlayers())
            {
                researchTree.sendGlobalResearchTreePackets(player);
            }
        }
    }

    /**
     * Parses out a json map for elements containing ResearchEffects, categorizes those effects, and calculates relative values for each effect level.
     *
     * @param object    A Map containing the resource location of each json file, and the element within that json file.
     * @return          A Map containing the ResearchEffectIds and ResearchEffectCategories each ID corresponds to.
     */
    private Map<ResourceLocation, ResearchEffectCategory> parseResearchEffects(final Map<ResourceLocation, JsonElement> object)
    {
        final Map<ResourceLocation, ResearchEffectCategory> effectCategories = new HashMap<>();
        for(final Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet())
        {
            final JsonObject effectJson = entry.getValue().getAsJsonObject();

            if (effectJson.has(RESEARCH_EFFECT_PROP))
            {
                final ResearchEffectCategory category;
                if((effectJson.has(RESEARCH_NAME_PROP) && effectJson.get(RESEARCH_NAME_PROP).isJsonPrimitive() && effectJson.get(RESEARCH_NAME_PROP).getAsJsonPrimitive().isString()))
                {
                    if(effectJson.has(RESEARCH_SUBTITLE_PROP) && effectJson.get(RESEARCH_SUBTITLE_PROP).isJsonPrimitive() && effectJson.get(RESEARCH_SUBTITLE_PROP).getAsJsonPrimitive().isString())
                    {
                        category = new ResearchEffectCategory(entry.getKey().toString(), effectJson.get(RESEARCH_NAME_PROP).getAsString(), effectJson.get(RESEARCH_SUBTITLE_PROP).getAsString());
                    }
                    else
                    {
                        category = new ResearchEffectCategory(entry.getKey().toString(), effectJson.get(RESEARCH_NAME_PROP).getAsString());
                    }
                }
                else
                {
                    category = new ResearchEffectCategory(entry.getKey().toString());
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
                // If no levels are defined, assume will go from zero to max level as a single action, and store on/off.
                else
                {
                    category.add(5f);
                }
                effectCategories.put(category.getId(), category);
            }
            // Files which declare effect: or effectType:, but lack ID or have the wrong types are malformed.
            else if (effectJson.has(RESEARCH_EFFECT_PROP))
            {
                Log.getLogger().error(entry.getKey() + " is a research effect, but does not contain all required fields.  Research Effects must have effect: and id:string fields.");
            }
        }
        return effectCategories;
    }

    /**
     * Parses out a json map for elements containing Researches, validates that they have required fields, and generates a GlobalResearch for each.
     *
     * @param object             A Map containing the resource location of each json file, and the element within that json file.
     * @param effectCategories   A Map containing the effect categories by effectId.
     * @param checkResourceLoc   If the client should check resource locations at the time.  This can not be run on the server.
     * @return                   A Map containing the ResearchIds and the GlobalResearches each ID corresponds to.
     */
    private Map<ResourceLocation, GlobalResearch> parseResearches(final Map<ResourceLocation, JsonElement> object, final Map<ResourceLocation, ResearchEffectCategory> effectCategories, boolean checkResourceLoc)
    {
        final Map<ResourceLocation, GlobalResearch> researchMap = new HashMap<>();
        for(final Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet())
        {
            // Note that we don't actually use the resource folders or file names; those are only for organization purposes.
            final JsonObject researchJson = entry.getValue().getAsJsonObject();

            //Can ignore those effects json now:
            if (researchJson.has(RESEARCH_EFFECT_PROP))
            {
                continue;
            }

            //Next, check for remove-type recipes.  We don't want to accidentally add them just because they have too much detail.
            if (researchJson.has(RESEARCH_REMOVE_PROP) && researchJson.get(RESEARCH_REMOVE_PROP).getAsJsonPrimitive().isString())
            {
                continue;
            }

            //And same for research-branch-specific settings, to avoid extraneous warnings.
            if (researchJson.has(RESEARCH_BRANCH_NAME_PROP) || researchJson.has(RESEARCH_BASE_TIME_PROP))
            {
                continue;
            }


            //Check for absolute minimum required types, and log as warning if malformed.
            if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get() &&
                  !(researchJson.has(RESEARCH_BRANCH_PROP) && researchJson.get(RESEARCH_BRANCH_PROP).isJsonPrimitive()
                         && researchJson.get(RESEARCH_BRANCH_PROP).getAsJsonPrimitive().isString()))
            {
                Log.getLogger().warn(entry.getKey() + " is a Research , but does not contain all required fields.  Researches must have a branch:string properties.");
                continue;
            }

            //Missing university level data may not necessarily be a show-stopper, but it is worth warning about.
            if(MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get() &&
                 !(researchJson.has(RESEARCH_UNIVERSITY_LEVEL_PROP) && researchJson.get(RESEARCH_UNIVERSITY_LEVEL_PROP).getAsJsonPrimitive().isNumber()))
            {
                Log.getLogger().warn(entry.getKey() + " is a Research, but has invalid or no university level requirements.");
            }

            //Pretty much anything else should be allowed: it's plausible pack designers may want a research type without a cost or effect.
            //It's possible we could dynamically derive university levels from parents, but doing so as a rule will prevent research branches that start at T2 or deeper.
            final GlobalResearch research = new GlobalResearch(researchJson, entry.getKey(), effectCategories, checkResourceLoc);
            if(MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get())
            {
                Log.getLogger().info("Parsed research recipe from " + entry.getKey() + " [" + research.getBranch() + "/" + research.getId() + "]");
                Log.getLogger().info(research.getName() + " at " + research.getDepth() + "/" + research.getParent());
                for(IResearchRequirement requirement : research.getResearchRequirement())
                {
                    Log.getLogger().info("Requirement: " + requirement.getDesc());
                }
                for(ItemStorage itemS : research.getCostList())
                {
                    Log.getLogger().info("Cost: " + itemS.toString());
                }
                for(IResearchEffect<?> researchEffect : research.getEffects())
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
    private void parseRemoveResearches(final Map<ResourceLocation, JsonElement> object, final Map<ResourceLocation, GlobalResearch> researchMap)
    {
        for(final Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet())
        {
            final JsonObject researchJson = entry.getValue().getAsJsonObject();

            // Users could plausibly want to remove a research json without depending on the Minecraft override behavior.
            // This would mostly be relevant for multiple overlapping data packs, which may have unpredictable load orders.
            // The json for such a removal can have an arbitrary filename, and the remove property points to the specific json to remove.
            if (researchJson.has(RESEARCH_REMOVE_PROP) && researchJson.get(RESEARCH_REMOVE_PROP).getAsJsonPrimitive().isString())
            {
                //hashmap, so don't have to check presence.
                researchMap.remove(new ResourceLocation(researchJson.get(RESEARCH_REMOVE_PROP).getAsString()));
            }
            // Files which declare remove:, but lack ID or have the wrong types are malformed.
            else if (researchJson.has(RESEARCH_REMOVE_PROP))
            {
                Log.getLogger().error(entry.getKey() + " is a research remove, but does not contain all required fields.  Research Removes must have remove:boolean and id:string.");
            }
        }
    }

    /**
     * Parses out a GlobalResearch map to apply parent/child relationships between researches, and to graft and warn about inconsistent relationships.
     *
     * @param researchMap   A Map of ResearchIDs to GlobalResearches to turn into a GlobalResearchTree.
     * @return              An IGlobalResearchTree containing the validated researches.
     */
    private IGlobalResearchTree calcResearchTree(final Map<ResourceLocation, GlobalResearch> researchMap)
    {
        final IGlobalResearchTree researchTree =  MinecoloniesAPIProxy.getInstance().getGlobalResearchTree();
        // The research tree should be reset on world unload, but certain events and disconnects break that.  Do it here, too.
        researchTree.reset();

        // Next, set up child relationships, and handle cases where they're not logically consistent.
        for (final Map.Entry<ResourceLocation, GlobalResearch> entry : researchMap.entrySet())
        {
            if (entry.getValue().getParent().getPath().isEmpty() && entry.getValue().getDepth() > 1)
            {
                //For now, log and re-graft entries with no parent and depth to the root of their branch.
                entry.setValue(new GlobalResearch(entry.getValue().getId(), entry.getValue().getBranch(), 1, entry.getValue().getEffects(),
                  entry.getValue().getIconTextureResourceLocation(), entry.getValue().getIconItemStack(), entry.getValue().isImmutable()));
                Log.getLogger()
                  .error(entry.getValue().getBranch() + "/" + entry.getKey() + "could not be attached to tree: inconsistent depth for parentage.");
            }
            else if (!entry.getValue().getParent().getPath().isEmpty())
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
                        entry.setValue(new GlobalResearch(entry.getValue().getId(), entry.getValue().getBranch(), 1, entry.getValue().getEffects(),
                          entry.getValue().getIconTextureResourceLocation(), entry.getValue().getIconItemStack(), entry.getValue().isImmutable()));
                        Log.getLogger()
                          .error(entry.getValue().getBranch() + "/" + entry.getKey() + "could not be attached to " + entry.getValue().getParent() + " on "
                                   + researchMap.get(entry.getValue().getParent()).getBranch());
                    }
                }
                else
                {
                    //For now, log and re-graft entries with inconsistent parent-child relationships to a separate branch.
                    entry.setValue(new GlobalResearch(entry.getValue().getId(), entry.getValue().getBranch(), 1, entry.getValue().getEffects(),
                      entry.getValue().getIconTextureResourceLocation(), entry.getValue().getIconItemStack(), entry.getValue().isImmutable()));
                    Log.getLogger().error(entry.getValue().getBranch() + "/" + entry.getKey() + " could not find parent " + entry.getValue().getParent());
                }
            }
            researchTree.addResearch(entry.getValue().getBranch(), entry.getValue(), true);
        }

        return researchTree;
    }

    /**
     * Parses out any research branch-specific settings from a Json object, and applies them to a Global Research Tree.
     * @param object         The source json object.
     * @param researchTree   The research tree to apply parsed branch-specific settings onto, if any.
     */
    private void parseResearchBranches(final Map<ResourceLocation, JsonElement> object, IGlobalResearchTree researchTree)
    {
        for (final Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet())
        {
            final JsonObject researchJson = entry.getValue().getAsJsonObject();

            // Research branches can have the "branch-name" or "base-time" id, or both, or neither.
            // We don't need to validate that these apply to an existing branch; if not, it simply won't have an effect.
            if (researchJson.has(RESEARCH_BRANCH_NAME_PROP) && researchJson.get(RESEARCH_BRANCH_NAME_PROP).isJsonPrimitive()
                  && researchJson.get(RESEARCH_BRANCH_NAME_PROP).getAsJsonPrimitive().isString())
            {
                researchTree.setBranchName(entry.getKey(),
                  new TranslationTextComponent(researchJson.get(RESEARCH_BRANCH_NAME_PROP).getAsJsonPrimitive().getAsString()));
            }
            if (researchJson.has(RESEARCH_BASE_TIME_PROP) && researchJson.get(RESEARCH_BASE_TIME_PROP).isJsonPrimitive()
                  && researchJson.get(RESEARCH_BASE_TIME_PROP).getAsJsonPrimitive().isNumber())
            {
                researchTree.setBranchTime(entry.getKey(), researchJson.get(RESEARCH_BASE_TIME_PROP).getAsJsonPrimitive().getAsDouble());
            }
        }
    }
}
