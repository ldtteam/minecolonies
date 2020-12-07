package com.minecolonies.coremod.research;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.*;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.research.effects.IResearchEffectManager;
import com.minecolonies.api.research.util.ResearchState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.research.util.ResearchConstants.BASE_RESEARCH_TIME;

/**
 * The implementation of the IGlobalResearch interface which represents the research on the global level.
 */
public class GlobalResearch implements IGlobalResearch
{
    /// region JSON Prop Management
    /**
     * The property name that indicates research identifier, used in code or for lookups. Required.
     */
    public static final String RESEARCH_ID_PROP = "id";

    /**
     * The property name that indicates research name, as presented to users.  Consider localization in future. Required.
     */
    public static final String RESEARCH_NAME_PROP = "name";

    /**
     * The property name that indicates research branch. For now, only "civilian", "technology", and "combat" render. Required.
     */
    public static final String RESEARCH_BRANCH_PROP = "branch";

    /**
     * The property name that indicates this recipe removes a research.
     */
    public static final String RESEARCH_REMOVE_PROP = "remove";

    /**
     * The property name for Required University Level.
     */
    public static final String RESEARCH_UNIVERSITY_LEVEL_PROP = "requiredUniversityLevel";

    /**
     * The property name that indicates onlyChild status
     */
    public static final String RESEARCH_EXCLUSIVE_CHILD_PROP = "exclusiveChildResearch";

    /**
     * The property name for parent research id.
     */
    private static final String RESEARCH_PARENT_PROP = "parentResearch";

    /**
     * The property name for the list of requirement objects.
     */
    private static final String RESEARCH_REQUIREMENTS_PROP = "requirements";

    /**
     * The property name for items.
     */
    private static final String RESEARCH_ITEM_NAME_PROP = "item";

    /**
     * The property name for a quantity.
     */
    private static final String RESEARCH_QUANTITY_PROP = "quantity";

    /**
     * The property name for a non-university building requirement.
     */
    private static final String RESEARCH_REQUIRED_BUILDING_PROP = "building";

    /**
     * The property name for a non-parent research requirement.
     */
    private static final String RESEARCH_REQUIRED_RESEARCH_PROP = "research";

    /**
     * The property name for a numeric level.
     */
    private static final String RESEARCH_LEVEL_PROP = "level";

    /**
     * The property name for the research which is only visible, when its requirements are completed.
     */
    private static final String RESEARCH_HIDDEN_PROP = "hidden";

    /**
     * The property name for instant completion of research, when its requirements are completed.
     */
    private static final String RESEARCH_INSTANT_PROP = "instant";

    /**
     * The property name for the list of research completion effects
     */
    private static final String RESEARCH_EFFECTS_PROP = "effects";
    /// endregion

    /**
     * The costList of the research.
     */
    private final List<ItemStorage> costList = new ArrayList<>();

    /**
     * The parent research which has to be completed first.
     */
    private String parent = "";

    /**
     * The string id of the research.
     */
    private final String id;

    /**
     * The resource location of the research, if created through data packs.
     */
    private final ResourceLocation resourceLocation;

    /**
     * The research branch.
     */
    private final String branch;

    /**
     * The pre-localized name for the research.  Used only if name tag is in json.
     */
    private final String name;

    /**
     * The research effects of this research.
     */
    private final List<IResearchEffect<?>> effects = new ArrayList<>();

    /**
     * The depth level in the tree.
     */
    private final int depth;

    /**
     * If the research has an only child.
     */
    private boolean onlyChild;

    /**
     * If the research has an only child.
     */
    private final boolean hidden;

    /**
     * If the research has an only child.
     */
    private final boolean instant;

    /**
     * List of childs of a research.
     */
    private final List<String> childs = new ArrayList<>();

    /**
     * The requirement for this research.
     */
    private List<IResearchRequirement> requirements = new ArrayList<>();

    /**
     * Create the new research.
     *
     * @param id              its id.
     * @param effect          its effect.
     * @param universityLevel the depth in the tree.
     * @param branch          the branch it is on.
     */
    public GlobalResearch(final String id, final String branch, final String name, final int universityLevel, final IResearchEffect<?> effect)
    {
        this.id = id;
        this.effects.add(effect);
        this.name = name;
        this.depth = universityLevel;
        this.branch = branch;
        this.resourceLocation = new ResourceLocation("minecolonies","staticresearch");
        this.hidden = false;
        this.instant = false;
        Log.getLogger().info("Statically assigned recipe [" + branch + "/" + id + "]");
    }

    /**
     * Create the new research with multiple effects
     *
     * @param id              its id.
     * @param effects          its effects.
     * @param universityLevel the depth in the tree.
     * @param branch          the branch it is on.
     */
    public GlobalResearch(final String id, final String branch, final int universityLevel, final List<IResearchEffect<?>> effects)
    {
        this.id = id;
        this.name = id;
        this.effects.addAll(effects);
        this.depth = universityLevel;
        this.branch = branch;
        this.resourceLocation = new ResourceLocation("minecolonies","staticresearch");
        this.hidden = false;
        this.instant = false;
        Log.getLogger().info("Statically assigned recipe [" + branch + "/" + id + "]");
    }

    @Override
    public boolean canResearch(final int uni_level, @NotNull final ILocalResearchTree localTree)
    {
        final IGlobalResearch parentResearch = parent.isEmpty() ? null : IGlobalResearchTree.getInstance().getResearch(branch, parent);
        final ILocalResearch localParentResearch = parent.isEmpty() ? null : localTree.getResearch(branch, parentResearch.getId());
        final ILocalResearch localResearch = localTree.getResearch(this.getBranch(), this.getId());

        return localResearch == null && canDisplay(uni_level) && (parentResearch == null || localParentResearch != null && localParentResearch.getState() == ResearchState.FINISHED)
                 && (parentResearch == null || !parentResearch.hasResearchedChild(localTree) || !parentResearch.hasOnlyChild()) && (depth < 6
                                                                                                                                      || !localTree.branchFinishedHighestLevel(
          branch));
    }

    @Override
    public boolean canDisplay(final int uni_level)
    {
        return uni_level >= depth;
    }

    @Override
    public boolean hasEnoughResources(final IItemHandler inventory)
    {
        for (final ItemStorage cost : costList)
        {
            final int count = InventoryUtils.getItemCountInItemHandler(inventory, stack -> !ItemStackUtils.isEmpty(stack) && stack.isItemEqual(cost.getItemStack()));
            if (count < cost.getAmount())
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<ItemStorage> getCostList()
    {
        return ImmutableList.copyOf(costList);
    }

    @Override
    public void startResearch(@NotNull final PlayerEntity player, @NotNull final ILocalResearchTree localResearchTree)
    {
        if (localResearchTree.getResearch(this.branch, this.id) == null)
        {
            final ILocalResearch research = new LocalResearch(this.id, this.branch, this.depth);
            if (this.instant)
            {
                research.setProgress((int)(BASE_RESEARCH_TIME * Math.pow(2, research.getDepth() - 1)));
            }
            research.setState(ResearchState.IN_PROGRESS);
            localResearchTree.addResearch(branch, research);
        }
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public String getDesc()
    {
        if (this.name.isEmpty())
        {
            return TranslationConstants.RESEARCH + id + ".name";
        }
        else
        {
            return this.name;
        }
    }

    @Override
    public ResourceLocation getResourceLocation() { return this.resourceLocation; }

    @Override
    public String getParent()
    {
        return this.parent;
    }

    @Override
    public String getBranch()
    {
        return this.branch;
    }

    @Override
    public int getDepth()
    {
        return this.depth;
    }

    @Override
    public boolean isHidden()
    {
        return this.hidden;
    }

    @Override
    public boolean hasOnlyChild()
    {
        return onlyChild;
    }

    @Override
    public void setOnlyChild(final boolean onlyChild)
    {
        this.onlyChild = onlyChild;
    }

    @Override
    public boolean hasResearchedChild(@NotNull final ILocalResearchTree localTree)
    {
        for (final String child : this.childs)
        {
            final IGlobalResearch childResearch = IGlobalResearchTree.getInstance().getResearch(branch, child);
            final ILocalResearch localResearch = localTree.getResearch(childResearch.getBranch(), childResearch.getId());
            if (localResearch != null)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addChild(final IGlobalResearch child)
    {
        this.childs.add(child.getId());
        child.setParent(this.getId());
    }

    @Override
    public void setRequirement(final List<IResearchRequirement> requirements)
    {
        this.requirements = requirements;
    }

    @Override
    public List<IResearchRequirement> getResearchRequirement()
    {
        return this.requirements;
    }

    @Override
    public void setParent(final String id)
    {
        this.parent = id;
    }

    @Override
    public ImmutableList<String> getChilds()
    {
        return ImmutableList.copyOf(this.childs);
    }

    @Override
    public List<IResearchEffect<?>> getEffects()
    {
        return effects;
    }

    /**
     * Parse a Json object into a new GlobalResearch.
     *
     * @param researchJson the json representing the recipe
     * @return new instance of ResearchRecipe
     */
    public GlobalResearch(@NotNull final JsonObject researchJson, ResourceLocation resourceLocation, Map<String, ResearchEffectCategory> effectCategories)
    {
        this.resourceLocation = resourceLocation;

        this.id = getResearchId(researchJson, resourceLocation);
        this.name = getResearchName(researchJson);
        this.branch = getBranch(researchJson, resourceLocation);
        this.depth = getUniversityLevel(researchJson);
        this.parent = getParent(researchJson);
        this.onlyChild = getBooleanSafe(researchJson, RESEARCH_EXCLUSIVE_CHILD_PROP);
        this.instant = getBooleanSafe(researchJson, RESEARCH_INSTANT_PROP);
        this.hidden = getBooleanSafe(researchJson, RESEARCH_HIDDEN_PROP);

        parseRequirements(researchJson);
        parseEffects(researchJson, effectCategories);
    }

    private String getResearchId(JsonObject researchJson, ResourceLocation resourceLocation)
    {
        if (researchJson.has(RESEARCH_ID_PROP) && researchJson.get(RESEARCH_ID_PROP).isJsonPrimitive() && researchJson.get(RESEARCH_ID_PROP).getAsJsonPrimitive().isString())
        {
            return researchJson.get(RESEARCH_ID_PROP).getAsString();
        }
        else
        {
            Log.getLogger().error("Error in Research ID for" + resourceLocation);
            return "";
        }
    }

    private String getResearchName(JsonObject researchJson)
    {
        if (researchJson.has(RESEARCH_NAME_PROP) && researchJson.get(RESEARCH_NAME_PROP).isJsonPrimitive() && researchJson.get(RESEARCH_NAME_PROP).getAsJsonPrimitive().isString())
        {
            return researchJson.get(RESEARCH_NAME_PROP).getAsString();
        }
        else
        {
            return "";
        }
    }

    private String getBranch(JsonObject researchJson, ResourceLocation resourceLocation)
    {
        if (researchJson.has(RESEARCH_BRANCH_PROP) && researchJson.get(RESEARCH_BRANCH_PROP).isJsonPrimitive() && researchJson.get(RESEARCH_BRANCH_PROP).getAsJsonPrimitive().isString())
        {
            return researchJson.get(RESEARCH_BRANCH_PROP).getAsString();
        }
        else
        {
            Log.getLogger().error("Error in Research Branch for" + resourceLocation);
            return "parserrors";
        }
    }

    private int getUniversityLevel(JsonObject researchJson)
    {
        if (researchJson.has(RESEARCH_UNIVERSITY_LEVEL_PROP) && researchJson.get(RESEARCH_UNIVERSITY_LEVEL_PROP).isJsonPrimitive() && researchJson.get(RESEARCH_UNIVERSITY_LEVEL_PROP).getAsJsonPrimitive().isNumber())
        {
            return researchJson.get(RESEARCH_UNIVERSITY_LEVEL_PROP).getAsNumber().intValue();
        }
        else
        {
            Log.getLogger().info("No declared university level for " + this.branch + "/" + this.id );
            return 1;
        }
    }

    private String getParent(JsonObject researchJson)
    {
        if (researchJson.has(RESEARCH_PARENT_PROP) && researchJson.get(RESEARCH_PARENT_PROP).isJsonPrimitive() && researchJson.get(RESEARCH_PARENT_PROP).getAsJsonPrimitive().isString())
        {
            return researchJson.get(RESEARCH_PARENT_PROP).getAsString();
        }
        else
        {
            return "";
        }
    }

    private boolean getBooleanSafe(JsonObject researchJson, String property)
    {
        if (researchJson.has(property) && researchJson.get(property).isJsonPrimitive() && researchJson.get(property).getAsJsonPrimitive().isBoolean())
        {
            return researchJson.get(property).getAsBoolean();
        }
        else
        {
            return false;
        }
    }

    private void parseRequirements(JsonObject researchJson)
    {
        if (researchJson.has(RESEARCH_REQUIREMENTS_PROP) && researchJson.get(RESEARCH_REQUIREMENTS_PROP).isJsonArray())
        {
            for (final JsonElement reqArrayElement : researchJson.get(RESEARCH_REQUIREMENTS_PROP).getAsJsonArray())
            {
                // ItemRequirements.  If no count, assumes 1x.
                if(reqArrayElement.isJsonObject() && reqArrayElement.getAsJsonObject().has(RESEARCH_ITEM_NAME_PROP) &&
                     reqArrayElement.getAsJsonObject().get(RESEARCH_ITEM_NAME_PROP).isJsonPrimitive() && reqArrayElement.getAsJsonObject().get(RESEARCH_ITEM_NAME_PROP).getAsJsonPrimitive().isString())
                {
                    final String[] itemName = reqArrayElement.getAsJsonObject().get(RESEARCH_ITEM_NAME_PROP).getAsString().split(":");
                    final Item item;
                    if  (itemName.length == 2)
                    {
                        item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName[0], itemName[1]));
                    }
                    else if (itemName.length == 1)
                    {
                        item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", itemName[0]));
                    }
                    else
                    {
                        item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "cobblestone"));
                    }
                    final ItemStack itemStack = new ItemStack(item);
                    if(reqArrayElement.getAsJsonObject().has(RESEARCH_QUANTITY_PROP) && reqArrayElement.getAsJsonObject().get(RESEARCH_QUANTITY_PROP).isJsonPrimitive()
                         && reqArrayElement.getAsJsonObject().get(RESEARCH_QUANTITY_PROP).getAsJsonPrimitive().isNumber())
                    {
                        itemStack.setCount(reqArrayElement.getAsJsonObject().get(RESEARCH_QUANTITY_PROP).getAsNumber().intValue());
                    }
                    this.costList.add(new ItemStorage(itemStack, false));
                }
                // Building Requirements.  If no level, assume 1x.
                else if(reqArrayElement.isJsonObject() && reqArrayElement.getAsJsonObject().has(RESEARCH_REQUIRED_BUILDING_PROP) &&
                          reqArrayElement.getAsJsonObject().get(RESEARCH_REQUIRED_BUILDING_PROP).isJsonPrimitive() && reqArrayElement.getAsJsonObject().get(RESEARCH_REQUIRED_BUILDING_PROP).getAsJsonPrimitive().isString())
                {
                    final int level;
                    if(reqArrayElement.getAsJsonObject().has(RESEARCH_LEVEL_PROP) && reqArrayElement.getAsJsonObject().get(RESEARCH_LEVEL_PROP).isJsonPrimitive()
                         && reqArrayElement.getAsJsonObject().get(RESEARCH_LEVEL_PROP).getAsJsonPrimitive().isNumber())
                    {
                        level = reqArrayElement.getAsJsonObject().get(RESEARCH_LEVEL_PROP).getAsNumber().intValue();
                    }
                    else
                    {
                        level = 1;
                    }
                    BuildingResearchRequirement requirement = new BuildingResearchRequirement(level, reqArrayElement.getAsJsonObject().get(RESEARCH_REQUIRED_BUILDING_PROP).getAsString());
                    this.requirements.add(requirement);
                }
                // Research Requirements.  Only partially implemented.
                else if(reqArrayElement.isJsonObject() && reqArrayElement.getAsJsonObject().has(RESEARCH_REQUIRED_RESEARCH_PROP) &&
                          reqArrayElement.getAsJsonObject().get(RESEARCH_REQUIRED_RESEARCH_PROP).isJsonPrimitive() && reqArrayElement.getAsJsonObject().get(RESEARCH_REQUIRED_RESEARCH_PROP).getAsJsonPrimitive().isString())
                {
                    this.requirements.add(new ResearchResearchRequirement(reqArrayElement.getAsJsonObject().get(RESEARCH_REQUIRED_RESEARCH_PROP).getAsString(), this.name));
                }
                else
                {
                    Log.getLogger().warn("Invalid Research Requirement formatting for " + this.branch + "/" + this.id);
                }
            }
        }
    }

    private void parseEffects(JsonObject researchJson, Map<String, ResearchEffectCategory> effectCategories)
    {
        if (researchJson.has(RESEARCH_EFFECTS_PROP) && researchJson.get(RESEARCH_EFFECTS_PROP).isJsonArray())
        {
            for (final JsonElement itemArrayElement : researchJson.get(RESEARCH_EFFECTS_PROP).getAsJsonArray())
            {
                if(itemArrayElement.isJsonObject())
                {
                    for(final Map.Entry<String, JsonElement> entry : itemArrayElement.getAsJsonObject().entrySet() )
                    {
                        if(effectCategories.containsKey(entry.getKey()))
                        {
                            final int strength;
                            if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isNumber() && effectCategories.containsKey(entry.getKey()))
                            {
                                int requested = entry.getValue().getAsNumber().intValue();
                                int max = effectCategories.get(entry.getKey()).getMaxLevel();
                                if(requested <= max)
                                {
                                    strength = entry.getValue().getAsNumber().intValue();
                                }
                                else
                                {
                                    //if trying to go above max strength, give to max strength, but warn.
                                    strength = effectCategories.get(entry.getKey()).getMaxLevel();
                                    Log.getLogger().warn("Research " + this.id + " requested higher effect strength than exists.");
                                }
                            }
                            // default to a strength of 1, for unlocks or parse errors.
                            else
                            {
                                Log.getLogger().warn("Research " + this.id + " did not have a valid effect strength.");
                                strength = 1;
                            }
                            final IResearchEffect effect;
                            if(effectCategories.get(entry.getKey()).getType().contains("multiplier"))
                            {
                                effect = new MultiplierModifierResearchEffect(entry.getKey(),
                                  effectCategories.get(entry.getKey()).getAbsolute(strength), effectCategories.get(entry.getKey()).getRelative(strength));
                            }
                            else if(effectCategories.get(entry.getKey()).getType().contains("addition"))
                            {
                                effect = new AdditionModifierResearchEffect(entry.getKey(),
                                  effectCategories.get(entry.getKey()).getAbsolute(strength), effectCategories.get(entry.getKey()).getRelative(strength));
                            }
                            else if(effectCategories.get(entry.getKey()).getType().contains("unlockAbility"))
                            {
                                effect = new UnlockAbilityResearchEffect(entry.getKey(), strength);
                            }
                            else if(effectCategories.get(entry.getKey()).getType().contains("unlockBuilding"))
                            {
                                effect = new UnlockBuildingResearchEffect(entry.getKey(), strength);
                            }
                            else
                            {
                                effect = new UnlockAbilityResearchEffect("", 1);
                            }
                            this.effects.add(effect);
                        }
                        else
                        {
                            Log.getLogger().error(this.branch + "/" + this.id + "looking for non-existent research effects" + entry);
                        }
                    }
                }
            }
        }

    }
}
