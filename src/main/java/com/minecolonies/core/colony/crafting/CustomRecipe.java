package com.minecolonies.core.colony.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.*;
import com.minecolonies.api.items.ModToolTypes;
import com.minecolonies.api.items.registry.ToolTypeEntry;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static com.minecolonies.api.util.constant.NbtTagConstants.COUNT_PROP;
import static com.minecolonies.api.util.constant.NbtTagConstants.ITEM_PROP;

/**
 * This class represents a recipe loaded from custom data that is available to a crafter
 * but not to a player
 */
public class CustomRecipe
{
    /**
     * The property name that indicates type for the recipe
     */
    public static final String RECIPE_TYPE_PROP = "type";

    /**
     * The recipe type
     */
    public static final String RECIPE_TYPE_RECIPE = "recipe";

    /**
     * The multiple output recipe type
     */
    public static final String RECIPE_TYPE_RECIPE_MULT_OUT = "recipe-multi-out";

    /**
     * The multiple input recipe type
     */
    public static final String RECIPE_TYPE_RECIPE_MULT_IN = "recipe-multi-in";

    /**
     * The recipe template type
     */
    public static final String RECIPE_TYPE_TEMPLATE = "recipe-template";

    /**
     * The remove type
     */
    public static final String RECIPE_TYPE_REMOVE = "remove";

    /**
     * The property name that indicates the recipe to remove
     */
    public static final String RECIPE_ID_TO_REMOVE_PROP = "recipe-id-to-remove";

    /**
     * The property name that indicates crafter type for the recipe
     */
    public static final String RECIPE_CRAFTER_PROP = "crafter";

    /**
     * The property name for the inputs array
     */
    public static final String RECIPE_INPUTS_PROP = "inputs";

    /**
     * The property name for the inputs array
     */
    public static final String RECIPE_SECONDARY_PROP = "additional-output";

    /**
     * The property name for the alternate output array
     */
    public static final String RECIPE_ALTERNATE_PROP = "alternate-output";

    /**
     * The property name for the result item
     */
    public static final String RECIPE_RESULT_PROP = "result";

    /**
     * The property namefor the result loottable
     */
    public static final String RECIPE_LOOTTABLE_PROP = "loot-table";

    /**
     * The property name for the required tool
     */
    public static final String RECIPE_TOOL_PROP = "tool";

    /**
     * The property name for the intermediate block ID
     */
    public static final String RECIPE_INTERMEDIATE_PROP = "intermediate";

    /**
     * The property name for the required research id
     */
    public static final String RECIPE_RESEARCHID_PROP = "research-id";

    /**
     * The property name for the research id that invalidates this recipe
     */
    public static final String RECIPE_EXCLUDED_RESEARCHID_PROP = "not-research-id";

    /**
     * The property name for the minimum level the building must be
     */
    public static final String RECIPE_BUILDING_MIN_LEVEL_PROP = "min-building-level";

    /**
     * The property name for the maximum level the building can be
     */
    public static final String RECIPE_BUILDING_MAX_LEVEL_PROP = "max-building-level";

    /**
     * The property name for if a recipe of the inputs must exist for the recipe to be valid
     */
    public static final String RECIPE_MUST_EXIST = "must-exist";

    /**
     * The property name to enable tooltip display (and transmission to the client).
     */
    public static final String RECIPE_SHOW_TOOLTIP = "show-tooltip";

    /**
     * The property name for a recipe template tag.
     */
    public static final String RECIPE_TAG = "tag";

    /**
     * The property name for a recipe template filter.
     */
    public static final String RECIPE_FILTER = "filter";

    /**
     * The crafter name for this instance, defaults to 'unknown'
     */
    private String crafter = "unknown";

    /**
     * The recipe id for this instance, used for removal and lookup
     */
    private ResourceLocation recipeId = null;

    /**
     * The list of ItemStacks for input to the recipe
     */
    private List<ItemStorage> inputs = new ArrayList<>();

    /**
     * The list of ItemStacks for alternate (multi-recipe) outputs from the recipe
     */
    private List<ItemStack> altOutputs = new ArrayList<>();

    /**
     * the result ItemStack
     */
    private ItemStack result = null;

    /**
     * The list of ItemStacks for additional outputs to the recipe
     */
    private List<ItemStack> secondary = new ArrayList<>();

    /**
     * The Intermediate Block
     */
    private Block intermediate = Blocks.AIR;

    /**
     * IDs of the required research (AND). Empty if none required
     */
    private Set<ResourceLocation> researchIds = new HashSet<>();

    /**
     * IDs of the exclusionary research (NAND). Empty if nothing excludes this recipe
     */
    private Set<ResourceLocation> excludedResearchIds = new HashSet<>();

    /**
     * The Minimum Level the building has to be for this recipe to be valid
     */
    private int minBldgLevel = 0;

    /**
     * The Maximum Level the building can to be for this recipe to be valid
     */
    private int maxBldgLevel = 5;

    /**
     * If true, the recipe inputs must match an already existing recipe's inputs
     */
    private boolean mustExist = false;

    /**
     * If true, display the recipe's requirements and crafter in an Inventory / JEI tooltip.
     */
    private boolean showTooltip = false;

    /**
     * The loottable to use for possible additional outputs
     */
    private ResourceLocation lootTable;

    /**
     * The tool required to craft this recipe
     */
    private IToolType requiredTool = ModToolTypes.none.get();

    /**
     * Cache of the recipe storage for performance
     */
    private RecipeStorage cachedRecipeStorage;

    /**
     * This class can only be created by the parse static
     */
    private CustomRecipe()
    {
    }

    /**
     * Parse a Json object into a Custom recipe
     *
     * @param recipeId the recipe id
     * @param recipeJson the json representing the recipe
     * @return new instance of CustomRecipe
     */
    public static CustomRecipe parse(@NotNull final ResourceLocation recipeId, @NotNull final JsonObject recipeJson)
    {
        final CustomRecipe recipe = new CustomRecipe();
        recipe.recipeId = recipeId;

        if (recipeJson.has(RECIPE_CRAFTER_PROP))
        {
            recipe.crafter = recipeJson.get(RECIPE_CRAFTER_PROP).getAsString();
        }
        if (recipeJson.has(RECIPE_INPUTS_PROP))
        {
            for (JsonElement e : recipeJson.get(RECIPE_INPUTS_PROP).getAsJsonArray())
            {
                if (e.isJsonObject())
                {
                    JsonObject ingredient = e.getAsJsonObject();
                    ItemStorage parsed = new ItemStorage(ingredient);
                    if(!parsed.isEmpty()) {
                        recipe.inputs.add(parsed);
                    }
                }
            }
        }

        if (recipeJson.has(RECIPE_RESULT_PROP))
        {
            recipe.result = ItemStackUtils.idToItemStack(recipeJson.get(RECIPE_RESULT_PROP).getAsString());
        }
        else
        {
            recipe.result = ItemStack.EMPTY;
        }

        if (recipeJson.has(RECIPE_LOOTTABLE_PROP))
        {
            recipe.lootTable = new ResourceLocation(recipeJson.get(RECIPE_LOOTTABLE_PROP).getAsString());
        }

        if (recipeJson.has(RECIPE_TOOL_PROP))
        {
            recipe.requiredTool = ToolTypeEntry.getToolType(recipeJson.get(RECIPE_TOOL_PROP).getAsString());
        }

        if (recipeJson.has(RECIPE_SECONDARY_PROP))
        {
            for (JsonElement e : recipeJson.get(RECIPE_SECONDARY_PROP).getAsJsonArray())
            {
                if (e.isJsonObject())
                {
                    JsonObject ingredient = e.getAsJsonObject();
                    if (ingredient.has(ITEM_PROP))
                    {
                        final ItemStack stack = ItemStackUtils.idToItemStack(ingredient.get(ITEM_PROP).getAsString());
                        if(ingredient.has(COUNT_PROP))
                        {
                            stack.setCount(ingredient.get(COUNT_PROP).getAsInt());
                        }
                        recipe.secondary.add(stack);
                    }

                }
            }
        }


        if (recipeJson.has(RECIPE_ALTERNATE_PROP))
        {
            for (JsonElement e : recipeJson.get(RECIPE_ALTERNATE_PROP).getAsJsonArray())
            {
                if (e.isJsonObject())
                {
                    JsonObject ingredient = e.getAsJsonObject();
                    if (ingredient.has(ITEM_PROP))
                    {
                        final ItemStack stack = ItemStackUtils.idToItemStack(ingredient.get(ITEM_PROP).getAsString());
                        if(ingredient.has(COUNT_PROP))
                        {
                            stack.setCount(ingredient.get(COUNT_PROP).getAsInt());
                        }
                        recipe.altOutputs.add(stack);
                    }

                }
            }
        }

        if (recipeJson.has(COUNT_PROP) && !ItemStackUtils.isEmpty(recipe.result))
        {
            recipe.result.setCount(recipeJson.get(COUNT_PROP).getAsInt());
        }
        if (recipeJson.has(RECIPE_INTERMEDIATE_PROP))
        {
            recipe.intermediate = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(recipeJson.get(RECIPE_INTERMEDIATE_PROP).getAsString()));
        }
        else
        {
            recipe.intermediate = Blocks.AIR;
        }
        final JsonElement researchIds = recipeJson.get(RECIPE_RESEARCHID_PROP);
        if (researchIds != null && researchIds.isJsonArray())
        {
            recipe.researchIds.addAll(researchIds.getAsJsonArray().asList().stream()
                    .map(json -> new ResourceLocation(json.getAsString())).toList());
        }
        else if (researchIds != null)
        {
            recipe.researchIds.add(new ResourceLocation(researchIds.getAsString()));
        }
        final JsonElement excludedResearchIds = recipeJson.get(RECIPE_EXCLUDED_RESEARCHID_PROP);
        if (excludedResearchIds != null && excludedResearchIds.isJsonArray())
        {
            recipe.excludedResearchIds.addAll(excludedResearchIds.getAsJsonArray().asList().stream()
                    .map(json -> new ResourceLocation(json.getAsString())).toList());
        }
        else if (excludedResearchIds != null)
        {
            recipe.excludedResearchIds.add(new ResourceLocation(excludedResearchIds.getAsString()));
        }
        if(recipeJson.has(RECIPE_BUILDING_MIN_LEVEL_PROP))
        {
            recipe.minBldgLevel= recipeJson.get(RECIPE_BUILDING_MIN_LEVEL_PROP).getAsInt();
        }
        if(recipeJson.has(RECIPE_BUILDING_MAX_LEVEL_PROP))
        {
            recipe.maxBldgLevel= recipeJson.get(RECIPE_BUILDING_MAX_LEVEL_PROP).getAsInt();
        }
        if(recipeJson.has(RECIPE_MUST_EXIST))
        {
            recipe.mustExist = recipeJson.get(RECIPE_MUST_EXIST).getAsBoolean();
        }
        if(recipeJson.has(RECIPE_SHOW_TOOLTIP))
        {
            recipe.showTooltip = recipeJson.get(RECIPE_SHOW_TOOLTIP).getAsBoolean();
        }

        return recipe;
    }

    /**
     * Parse a recipe template into a list of recipes.  See {@link ItemStackUtils#parseIdTemplate}
     * for details on the template replacement format.
     *
     * @param baseId       the base recipe path
     * @param templateJson the recipe template
     * @return a list of recipes for items discovered from the template
     */
    @NotNull
    public static List<CustomRecipe> parseTemplate(@NotNull final ResourceLocation baseId,
                                                   @NotNull final JsonObject templateJson)
    {
        final List<CustomRecipe> recipes = new ArrayList<>();

        final ResourceLocation tagId = new ResourceLocation(GsonHelper.getAsString(templateJson, RECIPE_TAG));
        final JsonObject baseRecipeJson = GsonHelper.getAsJsonObject(templateJson, RECIPE_TYPE_RECIPE);

        final Predicate<ResourceLocation> filter;
        final JsonElement filterJson = templateJson.get(RECIPE_FILTER);
        if (filterJson != null && filterJson.isJsonObject())
        {
            final Predicate<ResourceLocation> include = parseArrayOrStringFilter(filterJson.getAsJsonObject().get("include"), true);
            final Predicate<ResourceLocation> exclude = parseArrayOrStringFilter(filterJson.getAsJsonObject().get("exclude"), false);
            filter = id -> include.test(id) && !exclude.test(id);
        }
        else
        {
            filter = parseArrayOrStringFilter(filterJson, true);
        }

        final boolean logStatus = IMinecoloniesAPI.getInstance().getConfig().getServer().auditCraftingTags.get();

        for (final Item item : ForgeRegistries.ITEMS.tags().getTag(ItemTags.create(tagId)))
        {
            final ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
            if (!filter.test(itemId)) { continue; }

            final ResourceLocation recipeId = new ResourceLocation(baseId.getNamespace(), baseId.getPath() + '/' + itemId.getNamespace() + '/' + itemId.getPath());
            final JsonObject recipeJson = populateTemplate(baseId, baseRecipeJson, itemId, logStatus);
            if (recipeJson != null)
            {
                recipes.add(parse(recipeId, recipeJson));
            }
        }

        return recipes;
    }

    @NotNull
    private static Predicate<ResourceLocation> parseArrayOrStringFilter(@Nullable final JsonElement filterJson,
                                                                        final boolean defaultResult)
    {
        if (filterJson == null)
        {
            return id -> defaultResult;
        }
        else if (filterJson.isJsonArray())
        {
            final List<String> strings = StreamSupport.stream(filterJson.getAsJsonArray().spliterator(), false)
                    .map(JsonElement::getAsString).toList();
            return id -> strings.stream().anyMatch(f -> id.toString().contains(f));
        }
        else
        {
            final String filterString = filterJson.getAsString();
            return id -> id.toString().contains(filterString);
        }
    }

    @Nullable
    private static JsonObject populateTemplate(@NotNull final ResourceLocation templateId,
                                               @NotNull final JsonObject baseRecipeJson,
                                               @NotNull final ResourceLocation itemId,
                                               final boolean logStatus)
    {
        final JsonObject recipeJson = baseRecipeJson.deepCopy();

        if (recipeJson.has(RECIPE_INPUTS_PROP))
        {
            for (final JsonElement e : recipeJson.get(RECIPE_INPUTS_PROP).getAsJsonArray())
            {
                if (e.isJsonObject())
                {
                    final Tuple<Boolean, String> result = populateTemplateItem(e.getAsJsonObject(), ITEM_PROP, itemId);
                    if (Boolean.FALSE.equals(result.getA()))
                    {
                        if (logStatus)
                        {
                            Log.getLogger().error("Template {} with {}: rejecting {} {}",
                                    templateId, itemId, RECIPE_INPUTS_PROP, result.getB());
                        }
                        return null;
                    }
                }
            }
        }

        final Tuple<Boolean, String> output = populateTemplateItem(recipeJson, RECIPE_RESULT_PROP, itemId);
        if (Boolean.FALSE.equals(output.getA()))
        {
            if (logStatus)
            {
                Log.getLogger().error("Template {} with {}: rejecting {} {}",
                        templateId, itemId, RECIPE_RESULT_PROP, output.getB());
            }
            return null;
        }

        if (recipeJson.has(RECIPE_SECONDARY_PROP))
        {
            for (final JsonElement e : recipeJson.get(RECIPE_SECONDARY_PROP).getAsJsonArray())
            {
                if (e.isJsonObject())
                {
                    final Tuple<Boolean, String> result = populateTemplateItem(e.getAsJsonObject(), ITEM_PROP, itemId);
                    if (Boolean.FALSE.equals(result.getA()))
                    {
                        if (logStatus)
                        {
                            Log.getLogger().error("Template {} with {}: rejecting {} {}",
                                    templateId, itemId, RECIPE_SECONDARY_PROP, result.getB());
                        }
                        return null;
                    }
                }
            }
        }

        if (recipeJson.has(RECIPE_ALTERNATE_PROP))
        {
            for (final Iterator<JsonElement> iterator = recipeJson.get(RECIPE_ALTERNATE_PROP).getAsJsonArray().iterator(); iterator.hasNext(); )
            {
                final JsonElement e = iterator.next();
                if (e.isJsonObject())
                {
                    final Tuple<Boolean, String> result = populateTemplateItem(e.getAsJsonObject(), ITEM_PROP, itemId);
                    if (Boolean.FALSE.equals(result.getA()))
                    {
                        if (logStatus)
                        {
                            Log.getLogger().warn("Template {} with {}: ignoring {} {}",
                                    templateId, itemId, RECIPE_ALTERNATE_PROP, result.getB());
                        }

                        iterator.remove();
                    }
                }
            }
        }

        if (!recipeJson.has(RECIPE_RESULT_PROP) && !recipeJson.has(RECIPE_LOOTTABLE_PROP) &&
                (!recipeJson.has(RECIPE_ALTERNATE_PROP) || recipeJson.getAsJsonArray(RECIPE_ALTERNATE_PROP).isEmpty()))
        {
            if (logStatus)
            {
                Log.getLogger().warn("Template {} with {}: rejecting, no outputs", templateId, itemId);
            }
            return null;
        }

        if (logStatus)
        {
            Log.getLogger().info("Template {} with {}: success", templateId, itemId);
        }
        return recipeJson;
    }

    private static Tuple<Boolean, String> populateTemplateItem(@NotNull final JsonObject obj,
                                                               @NotNull final String prop,
                                                               @NotNull final ResourceLocation itemId)
    {
        if (obj.has(prop))
        {
            final Tuple<Boolean, String> result = ItemStackUtils.parseIdTemplate(GsonHelper.getAsString(obj, prop), itemId);
            obj.addProperty(prop, result.getB());
            return result;
        }

        return new Tuple<>(true, null);
    }

    /**
     * Creates a custom recipe from its components.
     * @param crafter           The crafter for the recipe.
     * @param minBldgLevel      Minimum level before the recipe can be learned.
     * @param maxBldgLevel      Maximum level before buildings in the colony will remove the recipe, if learned.
     * @param mustExist         If true, the custom recipe will only be learned if another recipe with the same output is taught to the building.
     * @param showTooltip       If a tooltip describing the recipe should be attached to the item.  Only one recipe per output should have showTooltip set to true.
     * @param recipeId          The identifier for the recipe, as a resource location.
     * @param researchReqs      Research IDs that the colony must have to begin the research.
     * @param researchExcludes  Research IDs that will cause buildings in the colony to remove the recipe, if learned.
     * @param lootTable         The loot table's resource location, if one is present.
     * @param requiredTool      The tool required for this craft, if any.  (In addition to any tools inferred from the recipe itself.)
     * @param inputs            The consumed items, as ItemStorages.
     * @param primaryOutput     The primary output of the recipe.
     * @param secondaryOutput   The secondary outputs of the recipe. Most often items like buckets or tools.
     * @param altOutputs        Alternative outputs of the recipe.  Used to allow one taught recipe to result in multiple effective choices for the request system.
     */
    public CustomRecipe(final String crafter, final int minBldgLevel, final int maxBldgLevel, final boolean mustExist, final boolean showTooltip, final ResourceLocation recipeId,
      final Set<ResourceLocation> researchReqs, final Set<ResourceLocation> researchExcludes, @Nullable final ResourceLocation lootTable, final IToolType requiredTool,
      final List<ItemStorage> inputs, final ItemStack primaryOutput, final List<ItemStack> secondaryOutput, final List<ItemStack> altOutputs, Block intermediate)
    {
        this.crafter = crafter;
        this.recipeId = recipeId;
        this.researchIds = researchReqs;
        this.excludedResearchIds = researchExcludes;
        this.minBldgLevel = minBldgLevel;
        this.maxBldgLevel = maxBldgLevel;
        this.mustExist = mustExist;
        this.showTooltip = showTooltip;
        this.inputs = inputs;
        this.result = primaryOutput;
        this.secondary = secondaryOutput;
        this.altOutputs = altOutputs;
        this.lootTable = lootTable;
        this.requiredTool = requiredTool;
        this.intermediate = intermediate;
    }

    /**
     * Get the name of the crafter this recipe applies to
     * @return crafter name
     */
    public String getCrafter()
    {
        return crafter;
    }

    /**
     * Get the ID for this recipe
     * @return Recipe Resource Location
     */
    public ResourceLocation getRecipeId()
    {
        return recipeId;
    }

    /**
     * Gets the input items for this recipe
     * @return input ItemStorages
     */
    public List<ItemStorage> getInputs()
    {
        return inputs;
    }

    /**
     * Get the primary output for the recipe
     * @return primary output ItemStack
     */
    public ItemStack getPrimaryOutput()
    {
        return result;
    }

    /**
     * Get the secondary outputs for the recipe.
     * @return secondary output ItemStacks
     */
    public List<ItemStack> getSecondaryOutput()
    {
        return secondary;
    }

    /**
     * Get the alternative outputs for the recipe.
     *
     * @return alternative output ItemStacks
     */
    public List<ItemStack> getAltOutputs()
    {
        return altOutputs;
    }

    /**
     * Get the Loot Table, if one is present.
     * @return Loot Table resource location
     */
    public ResourceLocation getLootTable()
    {
        return lootTable;
    }

    /**
     * Get the required tool, if any.
     * @return the tool required to perform this craft
     */
    @NotNull
    public IToolType getRequiredTool()
    {
        return requiredTool;
    }

    /**
     * Get the IDs of research required before this recipe is valid.  All researches must be done before this recipe
     * is valid.
     * @return The research IDs or empty if there is no such requirement.
     */
    public Set<ResourceLocation> getRequiredResearchIds() { return this.researchIds; }

    /**
     * Get the IDs of research after which this recipe is no longer valid.  All researches must be done before this
     * recipe is no longer valid.
     * @return The research IDs or empty if there is no such requirement.
     */
    public Set<ResourceLocation> getExcludedResearchIds() { return this.excludedResearchIds; }

    /**
     * Get the minimum (inclusive) building level required before this recipe is valid.
     * @return The minimum building level (0 means no such requirement).
     */
    public int getMinBuildingLevel() { return this.minBldgLevel; }

    /**
     * Get the maximum (inclusive) building level required to still consider this recipe valid.
     * @return The maximum building level (the recipe is no longer valid at higher levels).
     */
    public int getMaxBuildingLevel() { return this.maxBldgLevel; }

    /**
     * Check to see if the recipe is currently valid for the building
     * This does research checks, to verify that the appropriate researches are in the correct states
     * @param building      Building to check recipe against.
     */
    public boolean isValidForBuilding(IBuilding building)
    {
        final IColony colony = building.getColony();
        if (isPrecursorRecipeMissing(building))
        {
            return false;
        }

        final boolean requiredEffectPresent = researchIds.stream().allMatch(r -> isUnlockEffectResearched(r, colony));
        final boolean excludedEffectPresent = excludedResearchIds.stream().allMatch(r -> isUnlockEffectResearched(r, colony));
        final int bldgLevel = building.getBuildingLevel();

        return requiredEffectPresent
                 && (excludedResearchIds.isEmpty() || !excludedEffectPresent)
                 && (bldgLevel >= minBldgLevel)
                 && (bldgLevel <= maxBldgLevel);
    }

    /**
     * Check if a given researchId has been completed and has an unlock ability effect.
     * @param researchId    The id of the research to check for.
     * @param colony        The colony being checked against.
     */
    private boolean isUnlockEffectResearched(ResourceLocation researchId, IColony colony)
    {
        //Check first if the research effect exists.
        if (!IGlobalResearchTree.getInstance().hasResearchEffect(researchId) && !IGlobalResearchTree.getInstance().hasResearch(researchId))
        {
            // If there's nothing registered with this effect, and no research with this key, we'll default to acting as if research is not yet completed.
            return false;
        }
        else
        {
            if (IGlobalResearchTree.getInstance().hasResearchEffect(researchId) && colony.getResearchManager().getResearchEffects().getEffectStrength(researchId) > 0)
            {
                // Research effect queried, present, and set to true.
                return true;
            }
            if (IGlobalResearchTree.getInstance().hasResearch(researchId) && colony.getResearchManager().getResearchTree().hasCompletedResearch(researchId))
            {
                // Research ID queried and present.
                // This will allow simple Recipe-style unlocks to exist without needing an extra effect category.
                return true;
            }
        }
        // Research ID queried and present or present as an effect, but not completed or does not have an unlock effect.
        return false;
    }

    /**
     * Check if a precursor recipe is missing from the building.
     * @param building      The building which would contain the precursor recipe.
     * @return              True if a precursor recipe was required and not present.
     */
    private boolean isPrecursorRecipeMissing(IBuilding building)
    {
        if(mustExist)
        {
            final IRecipeStorage compareStorage = this.getRecipeStorage();
            final ResourceLocation recipeSource = this.getRecipeId();
            for (final ICraftingBuildingModule module : building.getModulesByType(ICraftingBuildingModule.class))
            {
                for (IToken<?> recipeToken : module.getRecipes())
                {
                    final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(recipeToken);
                    if ((storage.getRecipeSource() != null && storage.getRecipeSource().equals(recipeSource)) || (
                      ItemStackUtils.compareItemStacksIgnoreStackSize(storage.getPrimaryOutput(), compareStorage.getPrimaryOutput(), false, true) &&
                        storage.getCleanedInput().containsAll(compareStorage.getCleanedInput())
                        && compareStorage.getCleanedInput()
                             .containsAll(storage.getCleanedInput())))
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        // if no precursor needed.
        return false;
    }

    /**
     * Get the recipe storage represented by this recipe
     * @return Recipe Storage
     */
    public IRecipeStorage getRecipeStorage()
    {
        if(cachedRecipeStorage == null)
        {
            if(altOutputs.isEmpty())
            {
                cachedRecipeStorage = StandardFactoryController.getInstance().getNewInstance(
                    TypeConstants.RECIPE,
                    StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                    inputs,
                    1,
                    result,
                    intermediate,
                    this.getRecipeId(),
                    ModRecipeTypes.CLASSIC_ID,
                    null, //alternate outputs
                    secondary, //secondary output
                    lootTable,
                    requiredTool
                    );
            }
            else
            {
                cachedRecipeStorage = StandardFactoryController.getInstance().getNewInstance(
                    TypeConstants.RECIPE,
                    StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                    inputs,
                    1,
                    result,
                    intermediate,
                    this.getRecipeId(),
                    ModRecipeTypes.MULTI_OUTPUT_ID,
                    altOutputs, //alternate outputs
                    secondary, //secondary output
                    lootTable,
                    requiredTool
                    );
            }
            IRecipeManager recipeManager = IColonyManager.getInstance().getRecipeManager();
            IToken<?> cachedRecipeToken = recipeManager.getRecipeId(cachedRecipeStorage);
            if(cachedRecipeToken != null && !cachedRecipeToken.equals(cachedRecipeStorage.getToken()))
            {
                cachedRecipeStorage = (RecipeStorage) recipeManager.getRecipes().get(cachedRecipeToken);
            }
            recipeManager.registerUse(cachedRecipeStorage.getToken());
        }
        return cachedRecipeStorage;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(result, researchIds.hashCode(), excludedResearchIds.hashCode(), lootTable, requiredTool, inputs.hashCode());
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final CustomRecipe that = (CustomRecipe) o;


        return ItemStackUtils.compareItemStacksIgnoreStackSize(result, that.result)
            && researchIds.equals(that.researchIds)
            && excludedResearchIds.equals(that.excludedResearchIds)
            && Objects.equals(lootTable, that.lootTable)
            && Objects.equals(requiredTool, that.requiredTool)
            && inputs.equals(that.inputs);
    }

    /**
     * Does this require it to already be there? 
     */
    public boolean getMustExist()
    {
        return mustExist;
    }

    /**
     * What is the intermediate block for this recipe?
     */
    public Block getIntermediate()
    {
        return intermediate;
    }

    /**
     * Should tooltip information be displayed on the client in inventory and JEI?
     */
    public boolean getShowTooltip()
    {
        return showTooltip;
    }

    /**
     * Serialize to network.
     *
     * @param packetBuffer buffer to serialize into.
     */
    public void serialize(@NotNull final FriendlyByteBuf packetBuffer)
    {
        packetBuffer.writeUtf(getCrafter());
        packetBuffer.writeResourceLocation(getRecipeStorage().getRecipeSource());
        serializeIds(packetBuffer, getRequiredResearchIds());
        serializeIds(packetBuffer, getExcludedResearchIds());
        packetBuffer.writeBoolean(getLootTable() != null);
        if(getLootTable() != null)
        {
            packetBuffer.writeResourceLocation(getLootTable());
        }
        packetBuffer.writeUtf(getRequiredTool().getName());
        packetBuffer.writeVarInt(getMinBuildingLevel());
        packetBuffer.writeVarInt(getMaxBuildingLevel());
        packetBuffer.writeBoolean(getMustExist());
        packetBuffer.writeBoolean(getShowTooltip());
        packetBuffer.writeVarInt(getInputs().size());
        for(final ItemStorage input : getInputs())
        {
            StandardFactoryController.getInstance().serialize(packetBuffer, input);
        }
        packetBuffer.writeItem(getPrimaryOutput());
        packetBuffer.writeVarInt(getSecondaryOutput().size());
        for(final ItemStack secondary : getSecondaryOutput())
        {
            packetBuffer.writeItem(secondary);
        }
        packetBuffer.writeVarInt(getAltOutputs().size());
        for(final ItemStack alts : getAltOutputs())
        {
            packetBuffer.writeItem(alts);
        }
        packetBuffer.writeResourceLocation(ForgeRegistries.BLOCKS.getKey(getIntermediate()));
    }

    /**
     * Deserialize from network.
     * @param buffer network buffer.
     * @return       deserialized recipe.
     */
    public static CustomRecipe deserialize(@NotNull final FriendlyByteBuf buffer)
    {
        final String crafter = buffer.readUtf();
        final ResourceLocation recipeId = buffer.readResourceLocation();
        final Set<ResourceLocation> researchReq = deserializeIds(buffer);
        final Set<ResourceLocation> researchExclude = deserializeIds(buffer);
        final ResourceLocation lootTable;
        if(buffer.readBoolean())
        {
            lootTable = buffer.readResourceLocation();
        }
        else
        {
            lootTable = null;
        }
        final IToolType requiredTool = ToolTypeEntry.getToolType(buffer.readUtf());
        final int minBldgLevel = buffer.readVarInt();
        final int maxBldgLevel = buffer.readVarInt();
        final boolean mustExist = buffer.readBoolean();
        final boolean showTooltip = buffer.readBoolean();
        final List<ItemStorage> inputs = new ArrayList<>();
        for(int numInputs = buffer.readVarInt(); numInputs > 0; numInputs--)
        {
            inputs.add(StandardFactoryController.getInstance().deserialize(buffer));
        }
        final ItemStack primaryOutput = buffer.readItem();
        final List<ItemStack> secondaryOutput = new ArrayList<>();
        for(int numSec = buffer.readVarInt(); numSec > 0; numSec--)
        {
            secondaryOutput.add(buffer.readItem());
        }
        final List<ItemStack> altOutputs = new ArrayList<>();
        for(int numAlts = buffer.readVarInt(); numAlts > 0; numAlts--)
        {
            altOutputs.add(buffer.readItem());
        }

        final Block intermediate = ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation());

        return new CustomRecipe(crafter, minBldgLevel, maxBldgLevel, mustExist, showTooltip, recipeId,
                researchReq, researchExclude, lootTable, requiredTool,
                inputs, primaryOutput, secondaryOutput, altOutputs, intermediate);
    }

    /**
     * Serialize a set of {@link ResourceLocation}.
     * @param buffer the buffer to serialize into.
     * @param ids    the set to be serialized.
     */
    private static void serializeIds(@NotNull final FriendlyByteBuf buffer, @NotNull final Set<ResourceLocation> ids)
    {
        buffer.writeVarInt(ids.size());
        for (final ResourceLocation id : ids)
        {
            buffer.writeResourceLocation(id);
        }
    }

    /**
     * Deserialize a set of {@link ResourceLocation}.
     * @param buffer the buffer to deserialize from.
     * @return       the deserialized set.
     */
    private static Set<ResourceLocation> deserializeIds(@NotNull final FriendlyByteBuf buffer)
    {
        final Set<ResourceLocation> ids = new HashSet<>();

        final int size = buffer.readVarInt();
        for (int i = 0; i < size; ++i)
        {
            ids.add(buffer.readResourceLocation());
        }

        return Set.copyOf(ids);
    }
}
