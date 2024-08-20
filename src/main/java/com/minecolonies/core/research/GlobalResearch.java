package com.minecolonies.core.research;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.research.*;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.research.util.ResearchState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.research.util.ResearchConstants.MAX_DEPTH;

/**
 * The implementation of the IGlobalResearch interface which represents the research on the global level.
 */
public class GlobalResearch implements IGlobalResearch
{
    /// region JSON Prop Management
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
     * The property name that indicates research icon.
     */
    private static final String RESEARCH_ICON_PROP = "icon";

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
    private static final String RESEARCH_REQUIREMENTS_PROP = "requirements";

    /**
     * The property name for items.
     */
    public static final String RESEARCH_ITEM_NAME_PROP = "item";

    /**
     * The property name for items list.
     */
    public static final String RESEARCH_ITEM_LIST_PROP = "items";

    /**
     * The property name for item tags.
     */
    public static final String RESEARCH_ITEM_TAG_PROP = "tag";

    /**
     * The property name for a quantity.
     */
    public static final String RESEARCH_QUANTITY_PROP = "quantity";

    /**
     * The property name for a non-university building requirement.
     */
    private static final String RESEARCH_REQUIRED_BUILDING_PROP = "building";

    /**
     * The property name for a mandatory non-university building requirement.
     * Mandatory buildings require one of the building of at least the specified level.
     */
    private static final String RESEARCH_MANDATORY_BUILDING_PROP = "mandatory-building";

    /**
     * The property name for alternate non-university building requirement.
     * Alternate buildings require only one of the alternate-building requirements to be completed for the entire requirement to be fulfilled.
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
     * The property name for the research which is only visible when its requirements are fulfilled.
     */
    private static final String RESEARCH_HIDDEN_PROP = "hidden";

    /**
     * The property name for automatic start of research, when its requirements are fulfilled.
     * This can temporarily exceed the maximum number of concurrent researches.
     */
    private static final String RESEARCH_AUTOSTART_PROP = "autostart";

    /**
     * The property name for instant(ish) completion of research, when its requirements are completed.
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
    private final List<SizedIngredient> costList = new ArrayList<>();

    /**
     * The id of the parent research which has to be completed first.
     */
    private ResourceLocation parent;

    /**
     * The id of the research.
     */
    private final ResourceLocation id;

    /**
     * The research branch id.
     */
    private final ResourceLocation branch;

    /**
     * The research icon's texture, as a resource location.
     * Optional, and will prevent an itemIcon from being drawn.
     */
    private final ResourceLocation textureIcon;

    /**
     * The research icon's as an item.
     * Optional, and will not be drawn if a valid textureIcon is present.
     */
    private final ItemStack itemIcon;

    /**
     * The pre-localized name for the research.
     */
    private final TranslatableContents name;

    /**
     * Subtitle names for the research.  Optional, and only shows up rarely.
     */
    private final TranslatableContents subtitle;

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
    private final List<ResourceLocation> children = new ArrayList<>();

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
        this.id = ResourceLocation.parse(id);
        this.effects.add(effect);
        this.name = new TranslatableContents(name, null, TranslatableContents.NO_ARGS);
        this.subtitle = new TranslatableContents("", null, TranslatableContents.NO_ARGS);
        this.depth = universityLevel;
        this.sortOrder = 1;
        this.branch = ResourceLocation.parse(branch);
        this.hidden = false;
        this.instant = false;
        this.autostart = false;
        this.immutable = false;
        this.itemIcon = ItemStack.EMPTY;
        this.textureIcon = ResourceLocation.parse("");
        if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get())
        {
            Log.getLogger().info("Statically assigned recipe [" + branch + "/" + id + "]");
        }
    }

    /**
     * Create the new research with multiple effects
     *
     * @param id              its id.
     * @param effects         its effects.
     * @param universityLevel the depth in the tree.
     * @param branch          the branch it is on.
     * @param iconTexture     a resource location for a valid texture file for the research's icon.
     * @param iconItemStack   an itemStack used as an alternative icon.
     * @param immutable       if the research can not be reset once unlocked.
     */
    public GlobalResearch(final ResourceLocation id, final ResourceLocation branch, final int universityLevel, final List<IResearchEffect<?>> effects, final ResourceLocation iconTexture, final ItemStack iconItemStack, final boolean immutable)
    {
        this.id = id;
        final String autogenKey = "com." + this.id.getNamespace() + ".research." + this.id.getPath().replaceAll("[ /]",".");
        this.name = new TranslatableContents(autogenKey + ".name", null, TranslatableContents.NO_ARGS);
        this.parent = ResourceLocation.parse("");
        this.subtitle = new TranslatableContents("", null, TranslatableContents.NO_ARGS);
        this.effects.addAll(effects);
        this.depth = universityLevel;
        this.sortOrder = 1;
        this.branch = branch;
        this.hidden = false;
        this.autostart = false;
        this.instant = false;
        this.immutable = immutable;
        if (FMLEnvironment.dist.isClient())
        {
            this.textureIcon = validateIconTextures(iconTexture);
        }
        else
        {
            this.textureIcon = iconTexture;
        }
        this.itemIcon = iconItemStack;
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
     * @param iconTexture     a resource location for the optional research's icon texture.
     * @param iconStack       an iconStack used as an optional research icon.
     * @param subtitle        An optional short description of the research, in plaintext or as a translation key.  This will only show rarely.
     * @param onlyChild       if the research allows only one child research to be completed.
     * @param hidden          if the research is only visible when eligible to be researched.
     * @param autostart       if the research should begin automatically, or notify the player, when it is eligible.
     * @param instant         if the research should be completed instantly (ish) from when begun.
     * @param immutable       if the research can not be reset once unlocked.
     */
    public GlobalResearch(final ResourceLocation id, final ResourceLocation branch, final ResourceLocation parent, final TranslatableContents desc, final int universityLevel, final int sortOrder,
      final ResourceLocation iconTexture, final ItemStack iconStack, final TranslatableContents subtitle, final boolean onlyChild, final boolean hidden, final boolean autostart, final boolean instant, final boolean immutable)
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
        this.immutable = immutable;
        this.itemIcon = iconStack;
        this.textureIcon = iconTexture;
        if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get())
        {
            Log.getLogger().info("Client received research [" + branch + "/" + id + "]");
        }
    }

    @Override
    public boolean canResearch(final int uni_level, @NotNull final ILocalResearchTree localTree)
    {
        final IGlobalResearch parentResearch = parent.getPath().isEmpty() ? null : IGlobalResearchTree.getInstance().getResearch(branch, parent);
        final ILocalResearch localParentResearch = parent.getPath().isEmpty() ? null : localTree.getResearch(branch, parentResearch.getId());
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
        if (costList.isEmpty())
        {
            return true;
        }

        for (final SizedIngredient ingredient : costList)
        {
            if (ingredient.ingredient().hasNoItems())
            {
                return false;
            }

            final int requiredCount = ingredient.count();
            final int totalCount = InventoryUtils.getItemCountInItemHandler(inventory, ingredient.ingredient());
            if (totalCount < requiredCount)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<SizedIngredient> getCostList()
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
                research.setProgress(IGlobalResearchTree.getInstance().getBranchData(branch).getBaseTime(research.getDepth()));
            }
            research.setState(ResearchState.IN_PROGRESS);
            localResearchTree.addResearch(branch, research);
        }
    }

    @NotNull
    @Override
    public ResourceLocation getId()
    {
        return this.id;
    }

    @Override
    public TranslatableContents getName() { return this.name; }

    @Override
    public TranslatableContents getSubtitle()
    {
        return this.subtitle;
    }

    @NotNull
    @Override
    public ResourceLocation getParent()
    {
        return this.parent;
    }

    @Override
    public ResourceLocation getBranch()
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
        for (final ResourceLocation child : this.children)
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
    public void addChild(final ResourceLocation child)
    {
        this.children.add(child);
    }

    @Override
    public void addCosts(final List<SizedIngredient> cost)
    {
        costList.addAll(cost);
    }

    public void addEffect(final IResearchEffect<?> effect)
    {
        effects.add(effect);
    }

    public void addRequirement(final IResearchRequirement requirement)
    {
        this.requirements.add(requirement);
    }

    @Override
    public List<IResearchRequirement> getResearchRequirement()
    {
        return this.requirements;
    }

    @Override
    public void setParent(final ResourceLocation id)
    {
        this.parent = id;
    }

    @Override
    public List<ResourceLocation> getChildren()
    {
        return this.children;
    }

    @Override
    public List<IResearchEffect<?>> getEffects()
    {
        return this.effects;
    }

    @Override
    public ResourceLocation getIconTextureResourceLocation() {return this.textureIcon;}

    @Override
    public ItemStack getIconItemStack() {return this.itemIcon;}

    /**
     * Parse a Json object into a new GlobalResearch.
     *
     * @param researchJson     the json representing the recipe
     * @param resourceLocation the json location.
     * @param effectCategories a map of effect categories, by id.
     * @param checkIcons       if icons need to be validated.  This can only be performed on the client, and should only need to be done once.
     * @param provider         registry provider.
     */
    public GlobalResearch(@NotNull final JsonObject researchJson, final ResourceLocation resourceLocation, final Map<ResourceLocation, ResearchEffectCategory> effectCategories, final boolean checkIcons, final HolderLookup.Provider provider)
    {
        this.id = resourceLocation;
        final String autogenKey = "com." + this.id.getNamespace() + ".research." + this.id.getPath().replaceAll("[ /]",".");
        this.name = new TranslatableContents(getStringSafe(researchJson, RESEARCH_NAME_PROP, autogenKey + ".name"), null, TranslatableContents.NO_ARGS);
        this.subtitle = new TranslatableContents(getStringSafe(researchJson, RESEARCH_SUBTITLE_PROP, ""), null, TranslatableContents.NO_ARGS);
        this.branch = ResourceLocation.parse(getBranch(researchJson, resourceLocation));
        this.depth = getUniversityLevel(researchJson);
        this.sortOrder = getSortOrder(researchJson);
        this.parent = ResourceLocation.parse(getStringSafe(researchJson, RESEARCH_PARENT_PROP, ""));
        this.onlyChild = getBooleanSafe(researchJson, RESEARCH_EXCLUSIVE_CHILD_PROP);
        this.instant = getBooleanSafe(researchJson, RESEARCH_INSTANT_PROP);
        this.autostart = getBooleanSafe(researchJson, RESEARCH_AUTOSTART_PROP);
        this.hidden = getBooleanSafe(researchJson, RESEARCH_HIDDEN_PROP);
        this.immutable = getBooleanSafe(researchJson, RESEARCH_NO_RESET_PROP);
        final String iconString = getStringSafe(researchJson, RESEARCH_ICON_PROP,"");
        // Assume icon values with a '.' are texture files.
        if(iconString.contains("."))
        {
            final ResourceLocation unsafeIconTexture = ResourceLocation.parse(iconString);
            if (checkIcons && FMLEnvironment.dist.isClient())
            {
                this.textureIcon = validateIconTextures(unsafeIconTexture);
            }
            else
            {
                this.textureIcon = unsafeIconTexture;
            }
            this.itemIcon = ItemStack.EMPTY;
        }
        else
        {
            this.textureIcon = ResourceLocation.parse("");
            this.itemIcon = parseIconItemStacks(iconString);
        }

        parseRequirements(researchJson, provider);
        parseEffects(researchJson, effectCategories);
    }

    /**
     * Gets the branch for a research from a JSON object, if it exists and is valid, or "parse errors" otherwise.
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
            return "parse errors";
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
     * Validates the Icon Texture is a valid file.  This should only be called on client.
     * This should be run when the client first receives the data, to avoid exceptions in the GUI thread.
     * @param icon                The unvalidated ResourceLocation representing a texture file location.
     * @return                    The validated ResourceLocation, or minecraft:"" if not valid.
     */
    private ResourceLocation validateIconTextures(final ResourceLocation icon)
    {
        try
        {
            Minecraft.getInstance().getResourceManager().getResourceOrThrow(icon);
            return icon;
        }
        catch (IOException notFoundError)
        {
            Log.getLogger()
              .info("Resource file for Minecraft:" + icon.toString() + " not found for " + this.branch + "/" + this.id + " : " + notFoundError.getLocalizedMessage());
        }
        return ResourceLocation.parse("");
    }

    /**
     * Parse the Icon. Returns an empty item stack if the string is invalid.
     *
     * @param icon The unvalidated string representing an icon's resource location or texture file location.
     * @return The ItemStack for a given string representation, if a valid item is registered, or ItemStack.Empty if invalid.
     */
    private ItemStack parseIconItemStacks(final String icon)
    {
        final String[] iconParts = icon.split(":");
        final String[] outputString = new String[2];
        int count;
        // Do preliminary validation here, as later uses will always be in UI space.
        if (iconParts.length > 3)
        {
            Log.getLogger().info("Malformed icon property for " + this.branch + "/" + this.id + ".  Icons may contain at most namespace:identifier:count.");
            return ItemStack.EMPTY;
        }

        if (iconParts.length == 3)
        {
            try
            {
                count = Integer.parseInt(iconParts[2]);
            }
            catch (NumberFormatException parseError)
            {
                Log.getLogger().info("Non-integer count assigned to icon of " + this.branch + "/" + this.id + " : " + parseError.getLocalizedMessage());
                count = 1;
            }
        }
        else
        {
            count = 1;
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
        final Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(outputString[0], outputString[1]));
        if (item.equals(Items.AIR))
        {
            return ItemStack.EMPTY;
        }
        final ItemStack is = new ItemStack(item);
        is.setCount(Math.min(count, is.getMaxStackSize()));

        return is;
    }

    /**
     * Gets a string from a json safely, if present, a default string if not present, and an empty string if malformed or empty.
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
                return "";
            }
        }
        else
        {
            return defaultRet;
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
     * @param researchJson A json object to evaluate for requirements properties.
     * @param provider     Registry provider.
     */
    private void parseRequirements(final JsonObject researchJson, final HolderLookup.Provider provider)
    {
        if (researchJson.has(RESEARCH_REQUIREMENTS_PROP) && researchJson.get(RESEARCH_REQUIREMENTS_PROP).isJsonArray())
        {
            for (final JsonElement reqArrayElement : researchJson.get(RESEARCH_REQUIREMENTS_PROP).getAsJsonArray())
            {
                if (!reqArrayElement.isJsonObject())
                {
                    continue;
                }

                final JsonObject rootObject = reqArrayElement.getAsJsonObject();

                // ItemRequirements. If no count, assumes 1x.
                if (rootObject.has(RESEARCH_ITEM_LIST_PROP) && rootObject.get(RESEARCH_ITEM_LIST_PROP).isJsonArray())
                {
                    final List<SizedIngredient> ingredients = Utils.deserializeCodecMessFromJson(SizedIngredient.FLAT_CODEC.listOf(), provider, rootObject.get(RESEARCH_ITEM_LIST_PROP));
                    costList.addAll(ingredients);
                }
                else if (rootObject.has(RESEARCH_ITEM_LIST_PROP) && rootObject.get(RESEARCH_ITEM_LIST_PROP).isJsonObject())
                {
                    final SizedIngredient ingredient = Utils.deserializeCodecMessFromJson(SizedIngredient.FLAT_CODEC, provider, rootObject.get(RESEARCH_ITEM_LIST_PROP));
                    costList.add(ingredient);
                }
                // Building Requirements. If no level, assume 1x.
                else if (rootObject.has(RESEARCH_REQUIRED_BUILDING_PROP)
                           && rootObject.get(RESEARCH_REQUIRED_BUILDING_PROP).isJsonPrimitive()
                           && rootObject.get(RESEARCH_REQUIRED_BUILDING_PROP).getAsJsonPrimitive().isString())
                {
                    int level = 1;
                    if (rootObject.has(RESEARCH_LEVEL_PROP) && rootObject.get(RESEARCH_LEVEL_PROP).isJsonPrimitive() && rootObject.get(RESEARCH_LEVEL_PROP)
                                                                                                                          .getAsJsonPrimitive()
                                                                                                                          .isNumber())
                    {
                        level = rootObject.get(RESEARCH_LEVEL_PROP).getAsNumber().intValue();
                    }
                    this.requirements.add(new BuildingResearchRequirement(level, rootObject.get(RESEARCH_REQUIRED_BUILDING_PROP).getAsString(), false));
                }
                // Research Requirements.
                else if (rootObject.has(RESEARCH_REQUIRED_RESEARCH_PROP)
                           && rootObject.get(RESEARCH_REQUIRED_RESEARCH_PROP).isJsonPrimitive()
                           && rootObject.get(RESEARCH_REQUIRED_RESEARCH_PROP).getAsJsonPrimitive().isString())
                {
                    if (rootObject.has(RESEARCH_NAME_PROP) && rootObject.get(RESEARCH_NAME_PROP).isJsonPrimitive() && rootObject
                                                                                                                        .get(RESEARCH_NAME_PROP)
                                                                                                                        .getAsJsonPrimitive()
                                                                                                                        .isString())
                    {
                        this.requirements.add(new ResearchResearchRequirement(ResourceLocation.parse(rootObject
                                                                                                     .get(RESEARCH_REQUIRED_RESEARCH_PROP)
                                                                                                     .getAsString()),
                          Component.translatableEscape(rootObject.get(RESEARCH_NAME_PROP).getAsString())));
                    }
                    else
                    {
                        this.requirements.add(new ResearchResearchRequirement(ResourceLocation.parse(rootObject.get(RESEARCH_REQUIRED_RESEARCH_PROP).getAsString())));
                    }
                }
                // Alternate Building Requirements.  Requires at least one building type at a specific level out of all Alternate Buildings.
                // Only supports one group of alternates for a given research, for now:
                // House:4 OR Fisher:2 OR TownHall:1 OR Mine:3 is supported.
                // House:4 OR Fisher:2 AND TownHall:1 OR Mine:3 is not.
                else if (rootObject.has(RESEARCH_ALTERNATE_BUILDING_PROP)
                           && rootObject.get(RESEARCH_ALTERNATE_BUILDING_PROP).isJsonPrimitive()
                           && rootObject.get(RESEARCH_ALTERNATE_BUILDING_PROP).getAsJsonPrimitive().isString())
                {
                    parseAndAssignAlternateBuildingRequirement(rootObject);
                }
                // Mandatory Building Requirements.  Requires that the colony have one building of at least the required level.
                else if (rootObject.has(RESEARCH_MANDATORY_BUILDING_PROP)
                           && rootObject.get(RESEARCH_MANDATORY_BUILDING_PROP).isJsonPrimitive()
                           && rootObject.get(RESEARCH_MANDATORY_BUILDING_PROP).getAsJsonPrimitive().isString()
                           && rootObject.has(RESEARCH_LEVEL_PROP) && rootObject.get(RESEARCH_LEVEL_PROP).isJsonPrimitive()
                           && rootObject.get(RESEARCH_LEVEL_PROP).getAsJsonPrimitive().isNumber())
                {
                    this.requirements.add(new BuildingResearchRequirement(rootObject.get(RESEARCH_LEVEL_PROP).getAsNumber().intValue(),
                      rootObject.get(RESEARCH_MANDATORY_BUILDING_PROP).getAsString(),
                      true));
                }
                else
                {
                    Log.getLogger().warn("Invalid Research Requirement formatting for {}/{}", this.branch, this.id);
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
    private void parseEffects(final JsonObject researchJson, final Map<ResourceLocation, ResearchEffectCategory> effectCategories)
    {
        if (researchJson.has(RESEARCH_EFFECTS_PROP) && researchJson.get(RESEARCH_EFFECTS_PROP).isJsonArray())
        {
            for (final JsonElement itemArrayElement : researchJson.get(RESEARCH_EFFECTS_PROP).getAsJsonArray())
            {
                if(itemArrayElement.isJsonObject())
                {
                    for(final Map.Entry<String, JsonElement> entry : itemArrayElement.getAsJsonObject().entrySet() )
                    {
                        final ResourceLocation effect = ResourceLocation.parse(entry.getKey());
                        if(effectCategories.containsKey(effect))
                        {
                            final int strength;
                            if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isNumber() && effectCategories.containsKey(effect))
                            {
                                final int requested = entry.getValue().getAsNumber().intValue();
                                final int max = effectCategories.get(effect).getMaxLevel();
                                if(requested <= max)
                                {
                                    strength = entry.getValue().getAsNumber().intValue();
                                }
                                else
                                {
                                    //if trying to go above max strength, give to max strength, but warn.
                                    strength = effectCategories.get(effect).getMaxLevel();
                                    Log.getLogger().warn("Research " + this.id + " requested higher effect strength than exists.");
                                }
                            }
                            // default to a strength of MAX_DEPTH, which exceeds building levels, for unlocks or parse errors.
                            else
                            {
                                Log.getLogger().warn("Research " + this.id + " did not have a valid effect strength.");
                                strength = MAX_DEPTH;
                            }
                            this.effects.add(new GlobalResearchEffect(effect,
                              effectCategories.get(effect).get(strength), effectCategories.get(effect).getDisplay(strength), effectCategories.get(effect).getName(), effectCategories.get(effect).getSubtitle()));
                        }
                        else
                        {
                            if(MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get())
                            {
                                Log.getLogger().warn(this.branch + "/" + this.id + " looking for non-existent research effects " + entry);
                            }
                            // if no research effect available, assume it's intended as a binary unlock, and set to the current building max level.
                            // Official research should use an effect properly to allow subtitles, but this will
                            // work, and properly assign an auto-generation-based translation key.
                            this.effects.add(new GlobalResearchEffect(effect,MAX_DEPTH, MAX_DEPTH));
                        }
                    }
                }
            }
        }
    }

    /**
     * Parse the research item requirement count value from the provided json object.
     *
     * @param jsonObject the input json object.
     * @return the count number.
     */
    public static int parseItemCount(final JsonObject jsonObject)
    {
        int count = 1;
        if (jsonObject.has(RESEARCH_QUANTITY_PROP) &&
              jsonObject.get(RESEARCH_QUANTITY_PROP).isJsonPrimitive() &&
              jsonObject.get(RESEARCH_QUANTITY_PROP).getAsJsonPrimitive().isNumber())
        {
            count = Math.max(jsonObject.get(RESEARCH_QUANTITY_PROP).getAsInt(), 1);
        }
        return count;
    }
}
