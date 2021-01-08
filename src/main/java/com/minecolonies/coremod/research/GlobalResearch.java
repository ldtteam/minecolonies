package com.minecolonies.coremod.research;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.*;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.research.util.ResearchState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.research.util.ResearchConstants.BASE_RESEARCH_TIME;
import static com.minecolonies.api.research.util.ResearchConstants.MAX_DEPTH;

/**
 * The implementation of the IGlobalResearch interface which represents the research on the global level.
 */
public class GlobalResearch implements IGlobalResearch
{
    /// region JSON Prop Management
    /**
     * The property name that indicates research name, as presented to users, or a translation key to be transformed.
     */
    public static final String RESEARCH_NAME_PROP = "name";

    /**
     * The property name that indicates research subtitlee, as presented to users, or a translation key.
     * This will not always be visible.
     */
    public static final String RESEARCH_SUBTITLE_PROP = "subtitle";

    /**
     * The property name that indicates research branch. Required.
     */
    public static final String RESEARCH_BRANCH_PROP = "branch";

    /**
     * The property name that indicates research icon.
     */
    public static final String RESEARCH_ICON_PROP = "icon";

    /**
     * The property name that indicates this recipe removes a research.
     */
    public static final String RESEARCH_REMOVE_PROP = "remove";

    /**
     * The property name for Required University Level.
     */
    public static final String RESEARCH_UNIVERSITY_LEVEL_PROP = "researchLevel";

    /**
     * The property name for the sort order tag.  Optional.
     */
    private static final String RESEARCH_SORT_PROP = "sortOrder";

    /**
     * The property name that indicates onlyChild status
     */
    public static final String RESEARCH_EXCLUSIVE_CHILD_PROP = "exclusiveChildResearch";

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
     * The property name for a non-university building requirement.
     */
    private static final String RESEARCH_ALTERNATE_BUILDING_PROP = "alternate-building";

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
     * The property name for automatic start of research, when its requirements are completed.
     * This can temporarily exceed the maximum number of concurrent researches.
     */
    private static final String RESEARCH_AUTOSTART_PROP = "autostart";

    /**
     * The property name for instant completion of research, when its requirements are completed.
     */
    private static final String RESEARCH_INSTANT_PROP = "instant";

    /**
     * The property name for the list of research completion effects
     */
    private static final String RESEARCH_EFFECTS_PROP = "effects";

    /**
     * The property name that indicates research branch-id, used to distinguish branch-setting files. Required for branch settings.
     */
    public static final String RESEARCH_BRANCH_ID_PROP = "branch-id";

    /**
     * The property name for parent research id.  Only applies at the level of branch settings.
     */
    public static final String RESEARCH_BASE_TIME_PROP = "base-time";
    /// endregion

    /**
     * The costList of the research.
     */
    private final List<ItemStorage> costList = new ArrayList<>();

    /**
     * The parent research which has to be completed first.
     */
    private String parent;

    /**
     * The string id of the research.
     */
    private final String id;

    /**
     * The research branch.
     */
    private final String branch;

    /**
     * The research icon's resource location.
     */
    private final String icon;

    /**
     * The pre-localized name for the research.  Used only if name tag is in json.
     */
    private final String name;

    /**
     * Subtitle names for the research.  Optional, and only shows up rarely.
     */
    private final String subtitle;

    /**
     * The research effects of this research.
     */
    private final List<IResearchEffect<?>> effects = new ArrayList<>();

    /**
     * The depth level in the tree.
     */
    private final int depth;

    /**
     * The sort order of the research.
     */
    private final int sortOrder;

    /**
     * If the research has an only child.
     */
    private boolean onlyChild;

    /**
     * If the research has an only child.
     */
    private final boolean hidden;

    /**
     * If the research starts automatically when requirements met.
     */
    private final boolean autostart;

    /**
     * If the research has an only child.
     */
    private final boolean instant;

    /**
     * If the research can be reset or unlearned after being unlocked.
     */
    private final boolean immutable;

    /**
     * List of children of a research.
     */
    private final List<String> children = new ArrayList<>();

    /**
     * The requirement for this research.
     */
    private final List<IResearchRequirement> requirements = new ArrayList<>();

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
        this.subtitle = "";
        this.depth = universityLevel;
        this.sortOrder = 1;
        this.branch = branch;
        this.hidden = false;
        this.instant = false;
        this.autostart = false;
        this.immutable = false;
        this.icon = "";
        if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get())
        {
            Log.getLogger().info("Statically assigned recipe [" + branch + "/" + id + "]");
        }
    }

    /**
     * Create the new research with multiple effects
     *
     * @param id              its id.
     * @param effects          its effects.
     * @param universityLevel the depth in the tree.
     * @param branch          the branch it is on.
     * @param icon            a string of format namespace:item:count pointing to an item or block, or namespace:texture, to be used as an icon.
     * @param immutable       if the research can not be reset once unlocked.
     */
    public GlobalResearch(final String id, final String branch, final int universityLevel, final List<IResearchEffect<?>> effects, final String icon, final boolean immutable)
    {
        this.id = id;
        this.name = id;
        this.subtitle = "";
        this.effects.addAll(effects);
        this.depth = universityLevel;
        this.sortOrder = 1;
        this.branch = branch;
        this.hidden = false;
        this.autostart = false;
        this.instant = false;
        this.icon = icon;
        this.immutable = immutable;
        if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get())
        {
            Log.getLogger().info("Statically assigned recipe [" + branch + "/" + id + "]");
        }
    }

    /**
     * Create the new research with multiple effects
     *
     * @param id              its id.
     * @param branch          the branch it is on.
     * @param parent          the research's parent, if one is present, or an empty string if not.
     * @param desc            the optional name of the research.  If "", a key generated from the id will be used instead.
     * @param universityLevel the depth in the tree.
     * @param sortOrder       the relative vertical order of the research's display, in relation to its siblings.
     * @param icon            a string of format namespace:item:count pointing to an item or block, or namespace:texture, to be used as an icon.
     * @param subtitle        An optional short description of the research, in plaintext or as a translation key.  This will only show rarely.
     * @param onlyChild       if the research allows only one child research to be completed.
     * @param hidden          if the research is only visible when eligible to be researched.
     * @param autostart       if the research should begin automatically, or notify the player, when it is eligible.
     * @param instant         if the research should be completed instantly (ish) from when begun.
     * @param immutable       if the research can not be reset once unlocked.
     */
    public GlobalResearch(final String id, final String branch, final String parent, final String desc, final int universityLevel, final int sortOrder, final String icon,
      final String subtitle, final boolean onlyChild, final boolean hidden, final boolean autostart, final boolean instant, final boolean immutable)
    {
        this.id = id;
        this.name = desc;
        this.subtitle = subtitle;
        this.branch = branch;
        this.parent = parent;
        this.depth = universityLevel;
        this.sortOrder = sortOrder;
        this.onlyChild = onlyChild;
        this.hidden = hidden;
        this.autostart = autostart;
        this.instant = instant;
        this.icon = validateIcons(icon);
        this.immutable = immutable;
        if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get())
        {
            Log.getLogger().info("Client received recipe [" + branch + "/" + id + "]");
        }
    }

    @Override
    public boolean canResearch(final int uni_level, @NotNull final ILocalResearchTree localTree)
    {
        final IGlobalResearch parentResearch = parent.isEmpty() ? null : IGlobalResearchTree.getInstance().getResearch(branch, parent);
        final ILocalResearch localParentResearch = parent.isEmpty() ? null : localTree.getResearch(branch, parentResearch.getId());
        final ILocalResearch localResearch = localTree.getResearch(this.getBranch(), this.getId());

        return (localResearch == null)
                 && canDisplay(uni_level)
                 && (parentResearch == null || localParentResearch != null && localParentResearch.getState() == ResearchState.FINISHED)
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
    public void startResearch(@NotNull final ILocalResearchTree localResearchTree)
    {
        if (localResearchTree.getResearch(this.branch, this.id) == null)
        {
            final ILocalResearch research = new LocalResearch(this.id, this.branch, this.depth);
            if (this.instant)
            {
                research.setProgress((int)(BASE_RESEARCH_TIME * IGlobalResearchTree.getInstance().getBranchTime(branch) * Math.pow(2, research.getDepth() - 1)));
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
    public String getName()
    {
        if(this.name.isEmpty())
        {
           return "com." + this.id.split(":")[0] + ".research." + this.id.split(":")[1].replaceAll("[ /:]",".") + ".name";
        }
        return this.name;
    }

    @Override
    public String getSubtitle()
    {
        return this.subtitle;
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
    public int getSortOrder()
    {
        return this.sortOrder;
    }

    @Override
    public boolean isInstant()
    {
        return this.instant;
    }

    @Override
    public boolean isHidden()
    {
        return this.hidden;
    }

    @Override
    public boolean isAutostart()
    {
        return this.autostart;
    }

    @Override
    public boolean isImmutable()
    {
        return this.immutable;
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
        for (final String child : this.children)
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
        this.children.add(child.getId());
        child.setParent(this.getId());
    }

    @Override
    public void addChild(final String child)
    {
        this.children.add(child);
    }

    @Override
    public void addCost(final String cost)
    {
        final String[] costParts = cost.split(":");
        if(costParts.length > 1)
        {
            final ItemStack is = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(costParts[0], costParts[1])));
            if(costParts.length > 2)
            {
                is.setCount(Integer.parseInt(costParts[2]));
            }
            costList.add(new ItemStorage(is));
        }
    }

    @Override
    public void addEffect(final String effect)
    {
        final String[] effectParts = effect.split("`");
        effects.add(new GlobalResearchEffect(effectParts));
    }

    @Override
    public void addRequirement(final String requirement)
    {
        String[] reqParts = requirement.split("`");
        if(reqParts.length < 2)
        {
            return;
        }
        switch(reqParts[0])
        {
            case ResearchResearchRequirement.type:
                this.requirements.add(new ResearchResearchRequirement(reqParts));
                break;
            case BuildingResearchRequirement.type:
                this.requirements.add(new BuildingResearchRequirement(reqParts));
                break;
            case AlternateBuildingResearchRequirement.type:
                this.requirements.add(new AlternateBuildingResearchRequirement(reqParts));
                break;
        }
    }

    @Override
    public void setRequirement(final List<IResearchRequirement> requirements)
    {
        this.requirements.addAll(requirements);
    }

    @Override
    public void setEffects(final List<IResearchEffect<?>> effects) { this.effects.addAll(effects); }

    @Override
    public void setCosts(final List<ItemStorage> costs) { this.costList.addAll(costs); }

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
    public List<String> getChildren()
    {
        return this.children;
    }

    @Override
    public List<IResearchEffect<?>> getEffects()
    {
        return this.effects;
    }

    @Override
    public String getIcon() {return this.icon;}

    /**
     * Parse a Json object into a new GlobalResearch.
     *
     * @param researchJson      the json representing the recipe
     * @param resourceLocation  the json's location.
     * @param effectCategories  a map of effect categories, by id.
     * @param validateIcons     if icons need to be validated.  This can only be performed on the client, and should only need to be done once.
     */
    public GlobalResearch(@NotNull final JsonObject researchJson, final ResourceLocation resourceLocation, final Map<String, ResearchEffectCategory> effectCategories, final boolean validateIcons)
    {
        this.id = resourceLocation.toString();
        final String autogenKey = "com." + this.id.split(":")[0] + ".research." + this.id.split(":")[1].replaceAll("[ /:]",".");
        this.name = getStringSafe(researchJson, RESEARCH_NAME_PROP, autogenKey + ".name");
        this.subtitle = getStringSafe(researchJson, RESEARCH_SUBTITLE_PROP, autogenKey + ".subtitle");
        this.branch = getBranch(researchJson, resourceLocation);
        if (validateIcons && MineColonies.proxy.isClient())
        {
            this.icon = validateIcons(getStringSafe(researchJson, RESEARCH_ICON_PROP,""));
        }
        else
        {
            this.icon = getStringSafe(researchJson, RESEARCH_ICON_PROP, "");
        }
        this.depth = getUniversityLevel(researchJson);
        this.sortOrder = getSortOrder(researchJson);
        this.parent = getStringSafe(researchJson, RESEARCH_PARENT_PROP, "");
        this.onlyChild = getBooleanSafe(researchJson, RESEARCH_EXCLUSIVE_CHILD_PROP);
        this.instant = getBooleanSafe(researchJson, RESEARCH_INSTANT_PROP);
        this.autostart = getBooleanSafe(researchJson, RESEARCH_AUTOSTART_PROP);
        this.hidden = getBooleanSafe(researchJson, RESEARCH_HIDDEN_PROP);
        this.immutable = getBooleanSafe(researchJson, RESEARCH_NO_RESET_PROP);

        parseRequirements(researchJson);
        parseEffects(researchJson, effectCategories);
    }

    /**
     * Gets the branch for a research from a JSON object, if it exists and is valid, or "parseerrors" otherwise.
     *
     * @param researchJson        A json object to retrieve the ID from.
     * @param resourceLocation    The {@link ResourceLocation} of the json being parsed.
     * @return                    The Research Branch as a String.
     */
    private String getBranch(final JsonObject researchJson, final ResourceLocation resourceLocation)
    {
        if (researchJson.has(RESEARCH_BRANCH_PROP) && researchJson.get(RESEARCH_BRANCH_PROP).isJsonPrimitive() && researchJson.get(RESEARCH_BRANCH_PROP).getAsJsonPrimitive().isString())
        {
            return researchJson.get(RESEARCH_BRANCH_PROP).getAsString();
        }
        else
        {
            Log.getLogger().error("Error in Research Branch for" + resourceLocation);
            return "parseerrors";
        }
    }

    /**
     * Gets the required university level from a JSON object, if it exists and is valid, or returns 1 if not.
     *
     * @param researchJson        A json object to retrieve the requiredUniversityLevel from.
     * @return                    The required university level as an integer.
     */
    private int getUniversityLevel(final JsonObject researchJson)
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

    /**
     * Gets the sort order JSON object, if it exists and is valid, or returns 0 if not.
     *
     * @param researchJson        A json object to retrieve the requiredUniversityLevel from.
     * @return                    The required university level as an integer.
     */
    private int getSortOrder(final JsonObject researchJson)
    {
        if (researchJson.has(RESEARCH_SORT_PROP) && researchJson.get(RESEARCH_SORT_PROP).isJsonPrimitive() && researchJson.get(RESEARCH_SORT_PROP).getAsJsonPrimitive().isNumber())
        {
            return researchJson.get(RESEARCH_SORT_PROP).getAsNumber().intValue();
        }
        else
        {
            return 0;
        }
    }

    /**
     * Gets the optional icon location from a research json, if present.  If not available, or if requests a file or block that does not exist, returns an empty string.
     * @param icon                The unvalidated string representing an icon's resource location or texture file location.
     * @return                    The string, or an empty string if the texture does not exist.
     */
    private String validateIcons(final String icon)
    {
        final String[] iconParts = icon.split(":");
        final String[] outputString = new String[3];
        // Do preliminary validation here, as later uses will always be in UI space.
        if (iconParts.length > 3)
        {
            Log.getLogger().info("Malformed icon property for " + this.branch + "/" + this.id + ".  Icons may contain at most namespace:identifier:count.");
            return "";
        }

        if (iconParts.length == 3)
        {
            try
            {
                Integer.parseInt(iconParts[2]);
                outputString[2] = iconParts[2];
            }
            catch (NumberFormatException parseError)
            {
                Log.getLogger().info("Non-integer count assigned to icon of " + this.branch + "/" + this.id + " : " + parseError.getLocalizedMessage());
                outputString[2] = "1";
            }
        }
        else
        {
            outputString[2] = "1";
        }

        if (iconParts.length == 1)
        {
            outputString[0] = "minecraft";
            outputString[1] = iconParts[0];
        }
        else
        {
            outputString[0] = iconParts[0];
            outputString[1] = iconParts[1];
        }

        // If looking for a texture file, check if the file exists here, both to better assist debugging, and to avoid exceptions in GUI thread.
        // For non-texture-file missing values, Forge will automatically replace with minecraft:air.
        if (outputString[1].contains("."))
        {
            try
            {
                Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(outputString[0], outputString[1]));
            }
            catch (IOException notFoundError)
            {
                Log.getLogger()
                  .info("Resource file for Minecraft:" + iconParts[1] + " not found for " + this.branch + "/" + this.id + " : " + notFoundError.getLocalizedMessage());
                outputString[0] = "minecraft";
                outputString[1] = "air";
            }
        }
        return outputString[0] + ":" + outputString[1] + ":" + outputString[2];
    }

    /**
     * Gets a string from a json safely, if present, a default string if present but malformed or empty, and returns an empty string otherwise.
     *
     * @param researchJson        A json object to retrieve the Name from.
     * @param propertyName        The name of the property to retrieve.
     * @param defaultRet          The fallback String if not present or if not valid.
     * @return                    The Research Name as a String.
     */
    private String getStringSafe(final JsonObject researchJson, final String propertyName, final String defaultRet)
    {
        if (researchJson.has(propertyName))
        {
            if(researchJson.get(propertyName).isJsonPrimitive() && researchJson.get(propertyName).getAsJsonPrimitive().isString())
            {
                return researchJson.get(propertyName).getAsString();
            }
            else
            {
                return defaultRet;
            }
        }
        else
        {
            return "";
        }
    }

    /**
     * Gets a boolean value from a JSON object, if it exists and is valid, or false otherwise.
     *
     * @param researchJson        A json object to retrieve the ID from.
     * @param property            The property name being searched for.
     * @return                    True if the field is present and set true, false if false, if not a boolean, or if not present.
     */
    private boolean getBooleanSafe(final JsonObject researchJson, final String property)
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

    /**
     * Gets the Research Building, Item, and Research requirements, and if present and valid, assigns them in the GlobalResearch.
     *
     * @param researchJson        A json object to evaluate for requirements properties.
     */
    private void parseRequirements(final JsonObject researchJson)
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

                // Research Requirements.
                // TODO: Only partially implemented.  Needs a GUI implementation in WindowResearchTree.
                else if(reqArrayElement.isJsonObject() && reqArrayElement.getAsJsonObject().has(RESEARCH_REQUIRED_RESEARCH_PROP) &&
                          reqArrayElement.getAsJsonObject().get(RESEARCH_REQUIRED_RESEARCH_PROP).isJsonPrimitive() && reqArrayElement.getAsJsonObject().get(RESEARCH_REQUIRED_RESEARCH_PROP).getAsJsonPrimitive().isString())
                {
                    this.requirements.add(new ResearchResearchRequirement(reqArrayElement.getAsJsonObject().get(RESEARCH_REQUIRED_RESEARCH_PROP).getAsString(), this.name));
                }

                // Alternate Building Requirements.  Requires at least one building type at a specific level out of all Alternate Buildings.
                // Only supports one group of alternates for a given research, for now:
                // House:4 OR Fisher:2 OR TownHall:1 OR Mine:3 is supported.
                // House:4 OR Fisher:2 AND TownHall:1 OR Mine:3 is not.
                else if(reqArrayElement.isJsonObject() && reqArrayElement.getAsJsonObject().has(RESEARCH_ALTERNATE_BUILDING_PROP) &&
                          reqArrayElement.getAsJsonObject().get(RESEARCH_ALTERNATE_BUILDING_PROP).isJsonPrimitive() && reqArrayElement.getAsJsonObject().get(RESEARCH_ALTERNATE_BUILDING_PROP).getAsJsonPrimitive().isString())
                {
                    parseAndAssignAlternateBuildingRequirement(reqArrayElement.getAsJsonObject());
                }

                else
                {
                    Log.getLogger().warn("Invalid Research Requirement formatting for " + this.branch + "/" + this.id);
                }
            }
        }
    }

    /**
     * Parses a JSON object for Research Alternate Building Requirements, and adds them to the requirements list.
     * @param requirementJson       A validated JSON Object containing a RESEARCH_ALTERNATE_BUILDING_PROP
     */
    private void parseAndAssignAlternateBuildingRequirement(final JsonObject requirementJson)
    {
        final int level;
        if(requirementJson.has(RESEARCH_LEVEL_PROP) && requirementJson.get(RESEARCH_LEVEL_PROP).isJsonPrimitive()
             && requirementJson.get(RESEARCH_LEVEL_PROP).getAsJsonPrimitive().isNumber())
        {
            level = requirementJson.get(RESEARCH_LEVEL_PROP).getAsNumber().intValue();
        }
        else
        {
            level = 1;
        }
        for(IResearchRequirement requirement : requirements)
        {
            if(requirement instanceof AlternateBuildingResearchRequirement)
            {
                ((AlternateBuildingResearchRequirement)requirement).add(requirementJson.get(RESEARCH_ALTERNATE_BUILDING_PROP).getAsString(), level);
                return;
            }
        }
        this.requirements.add(new AlternateBuildingResearchRequirement().add(requirementJson.getAsJsonObject().get(RESEARCH_ALTERNATE_BUILDING_PROP).getAsString(), level));
    }

    /**
     * Parses a JSON object for Research Effects IDs and their levels, and if present and valid,
     * finds the equivalent Research Effect values from the Effect Categories for those levels.
     * Builds and assigns IResearchEffect if valid.
     *
     * @param researchJson        A json object to retrieve the ID from.
     * @param effectCategories    The Map of {@link ResearchEffectCategory} used to convert ResearchEffectIds into absolute effects and descriptions.
     */
    private void parseEffects(final JsonObject researchJson, final Map<String, ResearchEffectCategory> effectCategories)
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
                                final int requested = entry.getValue().getAsNumber().intValue();
                                final int max = effectCategories.get(entry.getKey()).getMaxLevel();
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
                            // default to a strength of MAX_DEPTH, which exceeds building levels, for unlocks or parse errors.
                            else
                            {
                                Log.getLogger().warn("Research " + this.id + " did not have a valid effect strength.");
                                strength = MAX_DEPTH;
                            }
                            this.effects.add(new GlobalResearchEffect(entry.getKey(),
                              effectCategories.get(entry.getKey()).get(strength), effectCategories.get(entry.getKey()).getDisplay(strength)));
                        }
                        else
                        {
                            if(MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get())
                            {
                                Log.getLogger().warn(this.branch + "/" + this.id + " looking for non-existent research effects " + entry);
                            }
                            // if no research effect available, assume it's intended as a binary unlock, and set to the current building max level.
                            // Official research should use an effect properly to allow subtitles, but this will
                            // work, and properly assign an autogeneration-based translation key.
                            this.effects.add(new GlobalResearchEffect(entry.getKey(),MAX_DEPTH, MAX_DEPTH));
                        }
                    }
                }
            }
        }

    }
}
