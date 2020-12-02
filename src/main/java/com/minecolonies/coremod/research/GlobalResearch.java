package com.minecolonies.coremod.research;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.*;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.research.registry.IResearchEffectRegistry;
import com.minecolonies.api.research.util.ResearchState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the IGlobalResearch interface which represents the research on the global level.
 */
public class GlobalResearch implements IGlobalResearch
{
    //region JSON Prop Management
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
     * The property name for the list of submitted items.
     */
    private static final String RESEARCH_REQUIRED_ITEMS_PROP = "requiredItems";

    /**
     * The property name for the submitted items names
     */
    private static final String RESEARCH_ITEM_NAME_PROP = "itemName";

    /**
     * The property name for the submitted item count
     */
    private static final String RESEARCH_ITEM_COUNT_PROP = "itemCount";

    /**
     * The property name for the list of required buildings.
     */
    private static final String RESEARCH_REQUIRED_BUILDINGS_PROP = "requiredBuildings";

    /**
     * The property name that indicates a required building's name.
     */
    private static final String RESEARCH_REQUIRED_BUILDING_NAME_PROP = "building";

    /**
     * The property name that indicates required buildings.
     */
    private static final String RESEARCH_REQUIRED_BUILDING_LEVEL_PROP = "buildingLevel";

    /**
     * The property name for the list of research completion effects
     */
    private static final String RESEARCH_EFFECTS_PROP = "effects";

    /**
     * The property name for Multiplier Modifier effects, containing string of target statistic.
     */
    private static final String RESEARCH_EFFECT_MULTIPLIER_PROP = "multiplierModifier";

    /**
     * The property name for Addition Modifier effects, containing string of target statistic.
     */
    private static final String RESEARCH_EFFECT_ADDITION = "additionModifier";

    /**
     * The property name for building unlock effects, containing string of hut id.
     */
    public static final String RESEARCH_EFFECT_UNLOCK_BUILDING_PROP = "unlockBuilding";

    /**
     * The property name for ability unlock effects, containing a string of ability id.
     */
    public static final String RESEARCH_EFFECT_UNLOCK_ABILITY_PROP = "unlockAbility";

    /**
     * The property name for values of effects. Boolean for unlockBuilding and unlockAbilities, numeric for Multiplier or Addition modifiers.
     */
    public static final String RESEARCH_EFFECT_VALUE_PROP = "value";
    ///endregion

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
     * The research branch.
     */
    private final String branch;

    /**
     * The description of the research.
     */
    private final String desc;

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
     * @param id              it's id.
     * @param desc            it's description text.
     * @param effect          it's effect.
     * @param universityLevel the depth in the tree.
     * @param branch          the branch it is on.
     */
    public GlobalResearch(final String id, final String branch, final String desc, final int universityLevel, final IResearchEffect<?> effect)
    {
        this.id = id;
        this.desc = desc;
        this.effects.add(effect);
        this.depth = universityLevel;
        this.branch = branch;
        IResearchEffectRegistry.getInstance().register(effect, false);
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
            research.setState(ResearchState.IN_PROGRESS);
            localResearchTree.addResearch(branch, research);
        }
    }

    @Override
    public String getDesc()
    {
        return this.desc;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

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
    public GlobalResearch(@NotNull final JsonObject researchJson)
    {
        if (researchJson.has(RESEARCH_ID_PROP) && researchJson.get(RESEARCH_ID_PROP).isJsonPrimitive() && researchJson.get(RESEARCH_ID_PROP).getAsJsonPrimitive().isString())
        {
            this.id = researchJson.get(RESEARCH_ID_PROP).getAsString();
        }
        else
        {
            this.id = "";
        }

        if (researchJson.has(RESEARCH_NAME_PROP) && researchJson.get(RESEARCH_NAME_PROP).isJsonPrimitive() && researchJson.get(RESEARCH_NAME_PROP).getAsJsonPrimitive().isString())
        {
            this.desc = researchJson.get(RESEARCH_NAME_PROP).getAsString();
        }
        else
        {
            this.desc = "ParseError";
        }

        if (researchJson.has(RESEARCH_BRANCH_PROP) && researchJson.get(RESEARCH_BRANCH_PROP).isJsonPrimitive() && researchJson.get(RESEARCH_BRANCH_PROP).getAsJsonPrimitive().isString())
        {
            this.branch = researchJson.get(RESEARCH_BRANCH_PROP).getAsString();
        }
        else
        {
            this.branch = "parseerrors";
        }

        if (researchJson.has(RESEARCH_EXCLUSIVE_CHILD_PROP) && researchJson.get(RESEARCH_EXCLUSIVE_CHILD_PROP).isJsonPrimitive() && researchJson.get(RESEARCH_EXCLUSIVE_CHILD_PROP).getAsJsonPrimitive().isBoolean())
        {
            this.onlyChild = researchJson.get(RESEARCH_EXCLUSIVE_CHILD_PROP).getAsBoolean();
        }

        if (researchJson.has(RESEARCH_UNIVERSITY_LEVEL_PROP) && researchJson.get(RESEARCH_UNIVERSITY_LEVEL_PROP).isJsonPrimitive() && researchJson.get(RESEARCH_UNIVERSITY_LEVEL_PROP).getAsJsonPrimitive().isNumber())
        {
            this.depth = researchJson.get(RESEARCH_UNIVERSITY_LEVEL_PROP).getAsNumber().intValue();
        }
        else
        {
            this.depth = 1;
            Log.getLogger().info("No declared university level for " + this.branch + "/" + this.id );
        }

        if (researchJson.has(RESEARCH_PARENT_PROP) && researchJson.get(RESEARCH_PARENT_PROP).isJsonPrimitive() && researchJson.get(RESEARCH_PARENT_PROP).getAsJsonPrimitive().isString())
        {
            this.parent = researchJson.get(RESEARCH_PARENT_PROP).getAsString();
        }
        else
        {
            this.parent = "";
        }

        if (researchJson.has(RESEARCH_REQUIRED_ITEMS_PROP) && researchJson.get(RESEARCH_REQUIRED_ITEMS_PROP).isJsonArray())
        {
            for (final JsonElement itemArrayElement : researchJson.get(RESEARCH_REQUIRED_ITEMS_PROP).getAsJsonArray())
            {
                if (itemArrayElement.isJsonObject() && itemArrayElement.getAsJsonObject().has(RESEARCH_ITEM_NAME_PROP) && itemArrayElement.getAsJsonObject().get(RESEARCH_ITEM_NAME_PROP).getAsJsonPrimitive().isString())
                {

                    final String[] itemName = itemArrayElement.getAsJsonObject().get(RESEARCH_ITEM_NAME_PROP).getAsString().split(":");
                    final Item item;
                    if (itemName.length == 2)
                    {
                        item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName[0], itemName[1]));
                    }
                    else if (itemName.length == 1)
                    {
                        item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", itemName[0]));
                    }
                    else
                    {
                        Log.getLogger().warn("Invalid ResearchCost formatting for " + this.branch + "/" + this.id);
                        continue;
                    }
                    final ItemStack itemStack = new ItemStack(item);
                    if (itemArrayElement.getAsJsonObject().has(RESEARCH_ITEM_COUNT_PROP) && itemArrayElement.getAsJsonObject().get(RESEARCH_ITEM_COUNT_PROP).getAsJsonPrimitive().isNumber())
                    {
                        itemStack.setCount(itemArrayElement.getAsJsonObject().get(RESEARCH_ITEM_COUNT_PROP).getAsNumber().intValue());
                    }
                    this.costList.add(new ItemStorage(itemStack, false));
                }
            }
        }

        if (researchJson.has(RESEARCH_REQUIRED_BUILDINGS_PROP) && researchJson.get(RESEARCH_REQUIRED_BUILDINGS_PROP).isJsonArray())
        {
            for (final JsonElement itemArrayElement : researchJson.get(RESEARCH_REQUIRED_BUILDINGS_PROP).getAsJsonArray())
            {
                if (itemArrayElement.isJsonObject() &&
                      itemArrayElement.getAsJsonObject().has(RESEARCH_REQUIRED_BUILDING_NAME_PROP) && itemArrayElement.getAsJsonObject().get(RESEARCH_REQUIRED_BUILDING_NAME_PROP).getAsJsonPrimitive().isString()
                      && itemArrayElement.getAsJsonObject().has(RESEARCH_REQUIRED_BUILDING_LEVEL_PROP) && itemArrayElement.getAsJsonObject().get(RESEARCH_REQUIRED_BUILDING_LEVEL_PROP).getAsJsonPrimitive().isNumber())
                {

                    BuildingResearchRequirement requirement = new BuildingResearchRequirement(
                      itemArrayElement.getAsJsonObject().get(RESEARCH_REQUIRED_BUILDING_LEVEL_PROP).getAsNumber().intValue(),
                      itemArrayElement.getAsJsonObject().get(RESEARCH_REQUIRED_BUILDING_NAME_PROP).getAsString());
                    this.requirements.add(requirement);
                }
            }
        }

        if (researchJson.has(RESEARCH_EFFECTS_PROP) && researchJson.get(RESEARCH_EFFECTS_PROP).isJsonArray())
        {

            for (final JsonElement itemArrayElement : researchJson.get(RESEARCH_EFFECTS_PROP).getAsJsonArray())
            {
                if (itemArrayElement.isJsonObject())
                {
                    final IResearchEffect effect;
                    if (itemArrayElement.getAsJsonObject().has(RESEARCH_EFFECT_ADDITION) && itemArrayElement.getAsJsonObject().get(RESEARCH_EFFECT_ADDITION).getAsJsonPrimitive().isString()
                          && itemArrayElement.getAsJsonObject().has(RESEARCH_EFFECT_VALUE_PROP) && itemArrayElement.getAsJsonObject().get(RESEARCH_EFFECT_VALUE_PROP).getAsJsonPrimitive().isNumber())
                    {
                        effect = new AdditionModifierResearchEffect(
                          itemArrayElement.getAsJsonObject().get(RESEARCH_EFFECT_ADDITION).getAsString(),
                          itemArrayElement.getAsJsonObject().get(RESEARCH_EFFECT_VALUE_PROP).getAsDouble());
                    }
                    else if (itemArrayElement.getAsJsonObject().has(RESEARCH_EFFECT_MULTIPLIER_PROP) && itemArrayElement.getAsJsonObject().get(RESEARCH_EFFECT_MULTIPLIER_PROP).getAsJsonPrimitive().isString()
                          && itemArrayElement.getAsJsonObject().has(RESEARCH_EFFECT_VALUE_PROP) && itemArrayElement.getAsJsonObject().get(RESEARCH_EFFECT_VALUE_PROP).getAsJsonPrimitive().isNumber())
                    {
                        effect = new MultiplierModifierResearchEffect(
                          itemArrayElement.getAsJsonObject().get(RESEARCH_EFFECT_MULTIPLIER_PROP).getAsString(),
                          itemArrayElement.getAsJsonObject().get(RESEARCH_EFFECT_VALUE_PROP).getAsDouble());
                    }
                    else if (itemArrayElement.getAsJsonObject().has(RESEARCH_EFFECT_UNLOCK_ABILITY_PROP) && itemArrayElement.getAsJsonObject().get(RESEARCH_EFFECT_UNLOCK_ABILITY_PROP).getAsJsonPrimitive().isString())
                    {
                        final boolean effectResult;
                        if (itemArrayElement.getAsJsonObject().has(RESEARCH_EFFECT_VALUE_PROP) && itemArrayElement.getAsJsonObject().get(RESEARCH_EFFECT_VALUE_PROP).getAsJsonPrimitive().isBoolean())
                        {
                            effectResult = itemArrayElement.getAsJsonObject().get(RESEARCH_EFFECT_VALUE_PROP).getAsBoolean();
                        }
                        else
                        {
                            effectResult = true; // default to unlocking abilities.
                        }
                        effect = new UnlockAbilityResearchEffect(
                          itemArrayElement.getAsJsonObject().get(RESEARCH_EFFECT_UNLOCK_ABILITY_PROP).getAsString(),
                          effectResult);
                        this.effects.add(effect);
                    }
                    else if (itemArrayElement.getAsJsonObject().has(RESEARCH_EFFECT_UNLOCK_BUILDING_PROP) && itemArrayElement.getAsJsonObject().get(RESEARCH_EFFECT_UNLOCK_BUILDING_PROP).getAsJsonPrimitive().isString())
                    {
                        final boolean effectResult;
                        if (itemArrayElement.getAsJsonObject().has(RESEARCH_EFFECT_VALUE_PROP) && itemArrayElement.getAsJsonObject().get(RESEARCH_EFFECT_VALUE_PROP).getAsJsonPrimitive().isBoolean())
                        {
                            effectResult = itemArrayElement.getAsJsonObject().get(RESEARCH_EFFECT_VALUE_PROP).getAsBoolean();
                        }
                        else
                        {
                            effectResult = true; // default to unlocking abilities.
                        }
                        effect = new UnlockBuildingResearchEffect(
                          itemArrayElement.getAsJsonObject().get(RESEARCH_EFFECT_UNLOCK_BUILDING_PROP).getAsString(),
                          effectResult);
                    }
                    else
                    {
                        continue;
                    }
                    this.effects.add(effect);

                    // JSONs are loaded and reloaded regularly, so we need to make their effects separately from
                    // those created in Forge init or otherwise registered only once.
                    IResearchEffectRegistry.getInstance().register(effect, true);
                }
            }
        }

        

    }
}
