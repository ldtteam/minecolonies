package com.minecolonies.core.datalistener;

import com.google.gson.*;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.research.*;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.research.*;
import com.minecolonies.core.util.GsonHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.Tuple;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.minecolonies.core.research.GlobalResearchBranch.*;

/**
 * Loader for Json-based researches
 */
public class ResearchListener extends SimpleJsonResourceReloadListener
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Generator functions for default parsed values.
     */
    private static final Function<ResourceLocation, String> DEFAULT_RESEARCH_NAME          =
        (effectId) -> String.format("com.%s.research.%s.name", effectId.getNamespace(), effectId.getPath().replaceAll("[ /]", "."));
    private static final Function<ResourceLocation, String> DEFAULT_RESEARCH_EFFECT_NAME   =
        (effectId) -> String.format("com.%s.research.%s.description", effectId.getNamespace(), effectId.getPath().replaceAll("[ /]", "."));
    private static final Supplier<JsonArray>                DEFAULT_RESEARCH_EFFECT_LEVELS = () -> {
        final JsonArray defaultArray = new JsonArray();
        defaultArray.add(1);
        return defaultArray;
    };

    //region [JSON properties]
    /**
     * The property name that indicates this recipe describes a research effect.
     */
    public static final String EFFECT_PROP = "effect";

    /**
     * The property name that indicates this recipe describes a research effect.
     */
    public static final String EFFECT_LEVELS_PROP = "levels";

    /**
     * The optional property name that indicates research name, as presented to users, or a translation key to be transformed.
     * If not present, a translation key will be auto-generated from the ResourceLocation.
     */
    public static final String RESEARCH_NAME_PROP = "name";

    /**
     * The optional property name that indicates research subtitle, as presented to users, or a translation key.
     */
    public static final String RESEARCH_SUBTITLE_PROP = "subtitle";

    /**
     * The property name that indicates research branch. Required.
     */
    public static final String RESEARCH_BRANCH_PROP = "branch";

    /**
     * The property name for Required University Level.
     */
    public static final String RESEARCH_LEVEL_PROP = "researchLevel";

    /**
     * The property name for the sort order tag. Optional.
     */
    public static final String RESEARCH_SORT_PROP = "sortOrder";

    /**
     * The property name for the sort order tag. Optional.
     */
    public static final String RESEARCH_REQUIREMENT_TYPE_PROP = "type";

    /**
     * The property name for the research which is only visible when its requirements are fulfilled.
     */
    public static final String RESEARCH_HIDDEN_PROP = "hidden";

    /**
     * The property name for automatic start of research, when its requirements are fulfilled.
     * This can temporarily exceed the maximum number of concurrent researches.
     */
    public static final String RESEARCH_AUTOSTART_PROP = "autostart";

    /**
     * The property name for instant(ish) completion of research, when its requirements are completed.
     */
    public static final String RESEARCH_INSTANT_PROP = "instant";

    /**
     * The property name for the list of research completion effects
     */
    public static final String RESEARCH_EFFECTS_PROP = "effects";

    /**
     * The property name of a single research effect its ID.
     */
    public static final String RESEARCH_EFFECTS_EFFECT_ID_PROP = "id";

    /**
     * The property name of a single research effect its level.
     */
    public static final String RESEARCH_EFFECTS_LEVEL_PROP = "level";

    /**
     * The property name that indicates this recipe removes research.
     */
    public static final String RESEARCH_REMOVE_PROP = "remove";

    /**
     * The property name that indicates onlyChild status
     */
    private static final String RESEARCH_EXCLUSIVE_CHILD_PROP = "exclusiveChildResearch";

    /**
     * The property name for parent research id.
     */
    private static final String RESEARCH_PARENT_PROP = "parentResearch";

    /**
     * The property name for preventing research resets.
     */
    private static final String RESEARCH_NO_RESET_PROP = "no-reset";

    /**
     * The property name for the list of requirement objects.
     */
    private static final String RESEARCH_REQUIREMENTS_PROP = "requirements";;

    /**
     * The property name for the list of cost objects.
     */
    private static final String RESEARCH_COSTS_PROP = "costs";
    //endregion [JSON properties]

    /**
     * Set up the core loading, with the directory in the data pack that contains this data
     * Directory is: namespace/researches/path
     */
    public ResearchListener()
    {
        super(GSON, "researches");
    }

    @Override
    protected void apply(@NotNull final Map<ResourceLocation, JsonElement> object, @NotNull final ResourceManager resourceManagerIn, @NotNull final ProfilerFiller profilerIn)
    {
        Log.getLogger().info("Research loading...");

        // First, index and map out all research effects.  We need to be able to map them before creating Researches themselves.
        // Because data packs, can't assume effects are in one specific location.
        // For now, we'll populate relative levels when doing so, but we probably want to do that dynamically.
        final Map<ResourceLocation, ResearchEffectCategory> effectCategories = parseResearchEffectCategories(object);

        // We /shouldn't/ get any removes before the Research they're trying to remove exists,
        // but it can happen if multiple data packs affect each other.
        // Instead, get lists of research locations for researches and branches to not load and quit their parsing early.
        final Tuple<Collection<ResourceLocation>, Collection<ResourceLocation>> removeResearchesAndBranches = parseRemoveResearches(object);

        // Next, populate a new map of IGlobalResearches, identified by researchID.
        // This allows us to figure out root/branch relationships more sanely.
        // We need the effectCategories and levels to do this.
        final Map<ResourceLocation, GlobalResearch> researchMap = parseResearches(object, effectCategories, removeResearchesAndBranches.getA(), removeResearchesAndBranches.getB());

        // After we've loaded all researches, we can then try to assign child relationships.
        // This is also the phase where we'd try to support back-calculating university levels for researches without them/with incorrect ones.
        final IGlobalResearchTree researchTree = calcResearchTree(researchMap);

        // Finally, check for branch-specific settings -- these are optional and only apply to the IGlobalResearchTree.
        parseResearchBranches(object, researchTree);

        Log.getLogger().info("Research loaded. Located {} branches, {} researches and {} effects.", researchTree.getBranches().size(), researchMap.size(), effectCategories.size());
    }

    /**
     * Parses out a json map for elements containing ResearchEffects, categorizes those effects, and calculates relative values for each effect level.
     *
     * @param object a map containing the resource location of each json file, and the element within that json file.
     * @return a map containing the ResearchEffectIds and ResearchEffectCategories each ID corresponds to.
     */
    private Map<ResourceLocation, ResearchEffectCategory> parseResearchEffectCategories(final Map<ResourceLocation, JsonElement> object)
    {
        final Map<ResourceLocation, ResearchEffectCategory> effectCategories = new HashMap<>();
        for (final Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet())
        {
            final ResourceLocation effectId = entry.getKey();
            final JsonObject effectJson = entry.getValue().getAsJsonObject();

            if (effectJson.has(EFFECT_PROP))
            {
                final String effectName = GsonHelper.getAsString(effectJson, RESEARCH_NAME_PROP, DEFAULT_RESEARCH_EFFECT_NAME, effectId);
                final String effectSubtitle = GsonHelper.getAsString(effectJson, RESEARCH_SUBTITLE_PROP, "");

                final List<Double> levels = new ArrayList<>();
                for (final JsonElement levelElement : GsonHelper.getAsJsonArray(effectJson, EFFECT_LEVELS_PROP, DEFAULT_RESEARCH_EFFECT_LEVELS))
                {
                    if (GsonHelper.isNumberValue(levelElement))
                    {
                        levels.add(levelElement.getAsNumber().doubleValue());
                    }
                }

                effectCategories.put(effectId, new ResearchEffectCategory(effectId, effectName, effectSubtitle, levels));
            }
        }
        return effectCategories;
    }

    /**
     * Parses out a json map for elements containing Researches, validates that they have required fields, and generates a {@link GlobalResearch} for each.
     *
     * @param object           a map containing the resource location of each json file, and the element within that json file.
     * @param effectCategories a map containing the effect categories by effectId.
     * @param removeResearches a collection of researches to remove, if present.
     * @param removeBranches   a collection of research branches to remove, including all component researches, if present.
     * @return a map containing the ResearchIds and the GlobalResearches each ID corresponds to.
     */
    private Map<ResourceLocation, GlobalResearch> parseResearches(final Map<ResourceLocation, JsonElement> object, final Map<ResourceLocation, ResearchEffectCategory> effectCategories, final Collection<ResourceLocation> removeResearches, final Collection<ResourceLocation> removeBranches)
    {
        final Map<ResourceLocation, GlobalResearch> researchMap = new HashMap<>();
        for (final Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet())
        {
            final ResourceLocation researchId = entry.getKey();
            final JsonObject researchJson = entry.getValue().getAsJsonObject();

            // Ignore json effect and branch files
            if (researchJson.has(EFFECT_PROP) || researchJson.has(RESEARCH_BRANCH_NAME_PROP)
                || researchJson.has(RESEARCH_BASE_TIME_PROP) || researchJson.has(RESEARCH_BRANCH_TYPE_PROP))
            {
                continue;
            }

            // Cancel removed individual researches first, to save parsing time.
            if (removeResearches.contains(researchId))
            {
                if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get())
                {
                    Log.getLogger().info("{} was removed by data pack.", researchId);
                }
                continue;
            }

            final ResourceLocation branch = GsonHelper.getAsResourceLocation(researchJson, RESEARCH_BRANCH_PROP, null);

            // Check for absolute minimum required types, and log as warning if malformed.
            if (branch == null)
            {
                Log.getLogger().warn("Research '{}' is missing the required '{}' property.", researchId, RESEARCH_BRANCH_PROP);
                continue;
            }
            // Now that we've confirmed a branch exists at all, cancel the add if it's from a removed branch.
            else if (removeBranches.contains(new ResourceLocation(researchJson.get(RESEARCH_BRANCH_PROP).getAsString())))
            {
                if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get())
                {
                    Log.getLogger().info("{} was removed, as its branch had been removed by data pack.", researchId);
                }
                continue;
            }

            final ResourceLocation parent = GsonHelper.getAsResourceLocation(researchJson, RESEARCH_PARENT_PROP, null);
            final TranslatableContents name =
                new TranslatableContents(GsonHelper.getAsString(researchJson, RESEARCH_NAME_PROP, DEFAULT_RESEARCH_NAME, researchId), null, TranslatableContents.NO_ARGS);
            final TranslatableContents subtitle = new TranslatableContents(GsonHelper.getAsString(researchJson, RESEARCH_SUBTITLE_PROP, ""), null, TranslatableContents.NO_ARGS);
            final int depth = GsonHelper.getAsInt(researchJson, RESEARCH_LEVEL_PROP, 1);
            final int sortOrder = GsonHelper.getAsInt(researchJson, RESEARCH_SORT_PROP, 1000);
            final boolean onlyChild = GsonHelper.getAsBoolean(researchJson, RESEARCH_EXCLUSIVE_CHILD_PROP, false);
            final boolean hidden = GsonHelper.getAsBoolean(researchJson, RESEARCH_HIDDEN_PROP, false);
            final boolean instant = GsonHelper.getAsBoolean(researchJson, RESEARCH_INSTANT_PROP, false);
            final boolean autostart = GsonHelper.getAsBoolean(researchJson, RESEARCH_AUTOSTART_PROP, false);
            final boolean immutable = GsonHelper.getAsBoolean(researchJson, RESEARCH_NO_RESET_PROP, false);

            final Tuple<List<IResearchCost>, List<IResearchRequirement>> requirements =
                parseResearchRequirements(researchId, GsonHelper.getAsJsonArray(researchJson, RESEARCH_REQUIREMENTS_PROP, new JsonArray()));
            final List<IResearchCost> costs = parseResearchCosts(researchId,
                GsonHelper.getAsJsonArray(researchJson, RESEARCH_COSTS_PROP, new JsonArray()),
                GsonHelper.getAsJsonArray(researchJson, RESEARCH_REQUIREMENTS_PROP, new JsonArray()));
            final List<GlobalResearchEffect> effects =
                parseResearchEffects(researchId, GsonHelper.getAsJsonArray(researchJson, RESEARCH_EFFECTS_PROP, new JsonArray()), effectCategories);

            final GlobalResearch research = new GlobalResearch(researchId, parent, branch, name, subtitle, depth, sortOrder, onlyChild, hidden, autostart, instant, immutable);
            // TODO: 1.22 remove the json requirements array as a potential input here, costs should move to a separate list to get them mixed out with requirements
            requirements.getA().forEach(research::addCost);
            requirements.getB().forEach(research::addRequirement);
            costs.forEach(research::addCost);
            effects.forEach(research::addEffect);

            researchMap.put(researchId, research);
        }
        return researchMap;
    }

    /**
     * Parses a JSON object for research requirements.
     *
     * @param researchId a json object to retrieve the ID from.
     * @param jsonArray  the array of requirements.
     */
    private Tuple<List<IResearchCost>, List<IResearchRequirement>> parseResearchRequirements(final ResourceLocation researchId, final JsonArray jsonArray)
    {
        final List<IResearchCost> costs = new ArrayList<>();
        final List<IResearchRequirement> requirements = new ArrayList<>();
        for (int index = 0; index < jsonArray.size(); index++)
        {
            final JsonObject requirementJson = jsonArray.get(index).getAsJsonObject();

            final ResourceLocation type = GsonHelper.getAsResourceLocation(requirementJson, RESEARCH_REQUIREMENT_TYPE_PROP, null);
            if (type == null)
            {
                Log.getLogger().warn("Research '{}' requirement #{} is missing the required '{}' property.", researchId, index, RESEARCH_REQUIREMENT_TYPE_PROP);
                continue;
            }

            final ModResearchRequirements.ResearchRequirementEntry researchRequirementEntry = IMinecoloniesAPI.getInstance().getResearchRequirementRegistry().getValue(type);
            if (researchRequirementEntry != null)
            {
                try
                {
                    requirements.add(researchRequirementEntry.readFromJson(requirementJson));
                }
                catch (Exception ex)
                {
                    Log.getLogger().warn("Research '{}' requirement #{} is invalid. {}", researchId, index, ex.getMessage());
                }
                continue;
            }

            final ModResearchCosts.ResearchCostEntry researchCostEntry = IMinecoloniesAPI.getInstance().getResearchCostRegistry().getValue(type);
            if (researchCostEntry != null)
            {
                try
                {
                    costs.add(researchCostEntry.readFromJson(requirementJson));
                }
                catch (Exception ex)
                {
                    Log.getLogger().warn("Research '{}' requirement #{} is invalid. {}", researchId, index, ex.getMessage());
                }
                continue;
            }

            Log.getLogger().warn("Research '{}' requirement #{} is invalid, type '{}' does not exist.", researchId, index, type);
        }
        return new Tuple<>(costs, requirements);
    }

    /**
     * Parses a JSON object for research costs.
     *
     * @param researchId       a json object to retrieve the ID from.
     * @param jsonCosts        the array of requirements.
     * @param jsonRequirements the array of requirements.
     */
    private List<IResearchCost> parseResearchCosts(final ResourceLocation researchId, final JsonArray jsonCosts, final JsonArray jsonRequirements)
    {
        final List<IResearchCost> costs = new ArrayList<>();
        for (int index = 0; index < jsonCosts.size(); index++)
        {
            final JsonObject jsonCost = jsonCosts.get(index).getAsJsonObject();

            final ResourceLocation type = GsonHelper.getAsResourceLocation(jsonCost, RESEARCH_REQUIREMENT_TYPE_PROP, null);
            if (type == null)
            {
                Log.getLogger().warn("Research '{}' cost #{} is missing the required '{}' property.", researchId, index, RESEARCH_REQUIREMENT_TYPE_PROP);
                continue;
            }

            final ModResearchCosts.ResearchCostEntry researchCostEntry = IMinecoloniesAPI.getInstance().getResearchCostRegistry().getValue(type);
            if (researchCostEntry != null)
            {
                try
                {
                    costs.add(researchCostEntry.readFromJson(jsonCost));
                }
                catch (Exception ex)
                {
                    Log.getLogger().warn("Research '{}' cost #{} is invalid. {}", researchId, index, ex.getMessage());
                }
            }
        }

        return costs;
    }

    /**
     * Parses a JSON object for research effects IDs and their levels.
     *
     * @param researchId               a json object to retrieve the ID from.
     * @param researchEffectCategories the Map of {@link ResearchEffectCategory} used to convert ResearchEffectIds into absolute effects and descriptions.
     */
    private List<GlobalResearchEffect> parseResearchEffects(
        final ResourceLocation researchId,
        final JsonArray researchEffectsArray,
        final Map<ResourceLocation, ResearchEffectCategory> researchEffectCategories)
    {
        final List<GlobalResearchEffect> effects = new ArrayList<>();
        for (int index = 0; index < researchEffectsArray.size(); index++)
        {
            final JsonElement researchEffectElement = researchEffectsArray.get(index);
            final JsonObject researchEffectJson = researchEffectElement.getAsJsonObject();

            final ResourceLocation effectId;
            final int effectLevel;
            if (researchEffectJson.has(RESEARCH_EFFECTS_EFFECT_ID_PROP))
            {
                effectId = GsonHelper.getAsResourceLocation(researchEffectJson, RESEARCH_EFFECTS_EFFECT_ID_PROP, null);
                effectLevel = GsonHelper.getAsInt(researchEffectJson, RESEARCH_EFFECTS_LEVEL_PROP, 1);

                if (effectId == null)
                {
                    Log.getLogger().warn("Research '{}' effect #{} is missing the required '{}' property.", researchId, index, RESEARCH_EFFECTS_EFFECT_ID_PROP);
                }
            }
            else
            {
                Log.getLogger()
                    .warn(
                        "Research '{}' effect #{} key is the effect ID, this method is deprecated, please use the object setup instead, for more information on the new structure see the wiki.",
                        researchId,
                        index);

                // TODO: 1.22 - Remove migration code for effect setup
                final Iterator<Map.Entry<String, JsonElement>> iterator = researchEffectElement.getAsJsonObject().entrySet().iterator();
                if (iterator.hasNext())
                {
                    final Map.Entry<String, JsonElement> next = researchEffectElement.getAsJsonObject().entrySet().iterator().next();
                    effectId = new ResourceLocation(next.getKey());
                    if (!GsonHelper.isNumberValue(next.getValue()))
                    {
                        Log.getLogger().warn("Research '{}' effect #{} value is not a number.", researchId, index);
                        continue;
                    }
                    effectLevel = next.getValue().getAsInt();
                }
                else
                {
                    Log.getLogger().warn("Research '{}' effect #{} is empty.", researchId, index);
                    continue;
                }
            }

            final ResearchEffectCategory researchEffectCategory = researchEffectCategories.get(effectId);
            if (researchEffectCategory == null)
            {
                Log.getLogger().warn("Research '{}' effect #{} looking for non-existent research effect {}", researchId, index, effectId);
                continue;
            }
            if (effectLevel > researchEffectCategory.getMaxLevel())
            {
                Log.getLogger().warn("Research '{}' effect #{} requested higher effect strength than exists.", researchId, index);
                continue;
            }

            effects.add(new GlobalResearchEffect(effectId,
                researchEffectCategory.getName(),
                researchEffectCategory.getSubtitle(),
                researchEffectCategory.get(effectLevel),
                researchEffectCategory.getDisplay(effectLevel)));
        }
        return effects;
    }

    /**
     * Parses out a researches map for elements containing Remove properties, and applies those removals to the researchMap
     *
     * @param object        A Map containing the resource location of each json file, and the element within that json file.
     * @return              A Tuple containing resource locations of Researches (A) and Branches (B) to remove from the global research tree.
     */
    private Tuple<Collection<ResourceLocation>, Collection<ResourceLocation>> parseRemoveResearches(final Map<ResourceLocation, JsonElement> object)
    {
        final Collection<ResourceLocation> removeResearches = new HashSet<>();
        final Collection<ResourceLocation> removeBranches = new HashSet<>();
        for (final Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet())
        {
            final JsonObject researchJson = entry.getValue().getAsJsonObject();

            if (researchJson.has(RESEARCH_REMOVE_PROP))
            {
                // Removing an entire branch, and all research on that branch.
                if (researchJson.has(RESEARCH_BRANCH_NAME_PROP) || researchJson.has(RESEARCH_BASE_TIME_PROP))
                {
                    // Accept arrays, if the data pack makers wants to remove multiple branches.
                    if (researchJson.get(RESEARCH_REMOVE_PROP).isJsonArray())
                    {
                        for (final JsonElement remove : researchJson.get(RESEARCH_REMOVE_PROP).getAsJsonArray())
                        {
                            if (remove.isJsonPrimitive() && remove.getAsJsonPrimitive().isString())
                            {
                                removeBranches.add(new ResourceLocation(remove.getAsString()));
                            }
                        }
                    }

                    // Accept individual strings.
                    // Users could plausibly want to remove a research json without depending on the Minecraft override behavior.
                    // This would mostly be relevant for multiple overlapping data packs, which may have unpredictable load orders.
                    // The json for such a removal can have an arbitrary filename, and the remove property points to the specific json to remove.
                    else if (researchJson.get(RESEARCH_REMOVE_PROP).isJsonPrimitive() && researchJson.get(RESEARCH_REMOVE_PROP).getAsJsonPrimitive().isString())
                    {
                        removeBranches.add(new ResourceLocation(researchJson.get(RESEARCH_REMOVE_PROP).getAsJsonPrimitive().getAsString()));
                    }
                    // Lastly, accept just boolean true, for the simple case of removing this particular branch and all component researches.
                    else if (researchJson.get(RESEARCH_REMOVE_PROP).isJsonPrimitive() && researchJson.get(RESEARCH_REMOVE_PROP).getAsJsonPrimitive().isBoolean()
                        && researchJson.get(RESEARCH_REMOVE_PROP).getAsBoolean())
                    {
                        removeBranches.add(entry.getKey());
                    }
                }

                // Users could plausibly want to remove a research json without depending on the Minecraft override behavior.
                // This would mostly be relevant for multiple overlapping data packs, which may have unpredictable load orders.
                // The json for such a removal can have an arbitrary filename, and the remove property points to the specific json to remove.
                else if (researchJson.get(RESEARCH_REMOVE_PROP).isJsonArray())
                {
                    for (final JsonElement remove : researchJson.get(RESEARCH_REMOVE_PROP).getAsJsonArray())
                    {
                        if (remove.isJsonPrimitive() && remove.getAsJsonPrimitive().isString())
                        {
                            removeResearches.add(new ResourceLocation(remove.getAsString()));
                        }
                    }
                }
                // Removing individual researches by name.
                else if (researchJson.get(RESEARCH_REMOVE_PROP).isJsonPrimitive() && researchJson.get(RESEARCH_REMOVE_PROP).getAsJsonPrimitive().isString())
                {
                    removeResearches.add(new ResourceLocation(researchJson.get(RESEARCH_REMOVE_PROP).getAsString()));
                }
                // Removes with a boolean true, but are not branch removes.
                else if (researchJson.get(RESEARCH_REMOVE_PROP).isJsonPrimitive() && researchJson.get(RESEARCH_REMOVE_PROP).getAsJsonPrimitive().isBoolean()
                    && researchJson.get(RESEARCH_REMOVE_PROP).getAsBoolean())
                {
                    removeResearches.add(entry.getKey());
                }
                // Files which declare remove, but are malformed should be reported to help diagnose the error.
                else
                {
                    Log.getLogger()
                        .error("{} is a research remove, but does not contain all required fields.  Research Removes must have remove:boolean and id:string.", entry.getKey());
                }
            }
        }
        return new Tuple<>(removeResearches, removeBranches);
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

        // Next, handle cases where the tree fails to follow depth restrictions.
        int previousResearchCount;
        int currentResearchCount = researchMap.size();
        do
        {
            final Iterator<Map.Entry<ResourceLocation, GlobalResearch>> iterator = researchMap.entrySet().iterator();
            while (iterator.hasNext())
            {
                final Map.Entry<ResourceLocation, GlobalResearch> entry = iterator.next();
                final ResourceLocation researchId = entry.getKey();
                final GlobalResearch research = entry.getValue();
                final @Nullable GlobalResearch parent = researchMap.get(research.getParent());

                if (parent == null && research.getDepth() > 1)
                {
                    Log.getLogger().error("Research '{}' could not be attached to tree. Parent does not exist.", researchId);
                    iterator.remove();
                    continue;
                }

                final int depthOffset = research.getDepth() - Optional.ofNullable(parent).map(GlobalResearch::getDepth).orElse(0);
                if (depthOffset != 1)
                {
                    if (depthOffset < 1)
                    {
                        Log.getLogger()
                            .error("Research '{}' could not be attached to tree. Chosen parent is invalid, parent is a child or a sibling of this research.", researchId);
                    }
                    if (depthOffset > 1)
                    {
                        Log.getLogger()
                            .error("Research '{}' could not be attached to tree. Parent cannot be set multiple levels deep, research must be a direct child of the parent.",
                                researchId);
                    }
                    iterator.remove();
                }
            }
            previousResearchCount = currentResearchCount;
            currentResearchCount = researchMap.size();
        }
        while (currentResearchCount != previousResearchCount);

        // Last, set up child relationships and add researches to the tree
        for (final GlobalResearch research : researchMap.values())
        {
            if (research.getParent() != null)
            {
                final GlobalResearch parent = researchMap.get(research.getParent());
                parent.addChild(research);
            }
            researchTree.addResearch(research.getBranch(), research, true);
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
        // We don't need check branches that don't have loaded researches, but we do want to create these properties for all branches.
        for (final ResourceLocation branchId : researchTree.getBranches())
        {
            if(object.containsKey(branchId))
            {
                researchTree.addBranchData(branchId, new GlobalResearchBranch(branchId, object.get(branchId).getAsJsonObject()));
            }
            else
            {
                researchTree.addBranchData(branchId, new GlobalResearchBranch(branchId));
            }
        }
    }
}
