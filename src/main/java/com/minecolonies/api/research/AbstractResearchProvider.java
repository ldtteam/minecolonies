package com.minecolonies.api.research;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.minecolonies.api.research.ModResearchCostTypes.LIST_ITEM_COST_ID;
import static com.minecolonies.api.research.ModResearchCostTypes.TAG_ITEM_COST_ID;

/**
 * A class for creating the Research-related JSONs, including Research, ResearchEffects, and (optional) Branches.
 * Note that this does not validate that the resulting research tree is coherent:
 * programmers should make sure that research parents and effects exist, that depth is 1 or one level greater than the parent depth,
 * and that cost and requirement identifiers match real items.
 *
 * Avoid changing research identifiers here unless necessary. If required, update ResearchCompatMap.
 */
public abstract class AbstractResearchProvider implements DataProvider
{
    protected final PackOutput packOutput;

    /**
     * The abstract variant of a ResearchProvider, to register to fires during runData.
     * @param packOutput  the pack output.
     */
    public AbstractResearchProvider(@NotNull final PackOutput packOutput)
    {
        this.packOutput = packOutput;
    }

    /**
     * Creates a collection of Research Branches, holding the human-readable name and time multiplier.
     * Research Branches are optional: if no matching json is present, or no values set,
     * the branch will default to its ResourceLocation.path, at 1.0 research time.
     * @return  A collection of Research Branches, or Collection.EMPTY_LIST.
     */
    protected abstract Collection<ResearchBranch> getResearchBranchCollection();

    /**
     * Creates a collection of ResearchEffects, holding effect levels and (optionally) name and subtitles.
     * ResearchEffects are not strictly mandatory: if no matching effect or effect level is present,
     * the Research will default to a strength of 10 or true when complete.  For non-boolean-like effects, they're strongly encouraged.
     * @return  A collection of ResearchEffects, or Collection.EMPTY_LIST.
     */
    protected abstract Collection<ResearchEffect> getResearchEffectCollection();

    /**
     * Create a collection of Researches, holding the majority of relevant data for the individual research targets.
     * Researches must consist of at least an identifier and a branch.  If the research has no parent, it must be research level 1;
     * if it does have a parent, it must be exactly one level higher than its parent.
     * @return  A collection of Researches.
     */
    protected abstract Collection<Research> getResearchCollection();

    private final List<Tuple<JsonObject, Tuple<String, String>>> research = new ArrayList<>();

    @NotNull
    @Override
    public CompletableFuture<?> run(@NotNull final CachedOutput cache)
    {
        final JsonObject langJson = new JsonObject();

        for(final ResearchBranch branch : getResearchBranchCollection())
        {
            research.add(new Tuple<>(branch.json, new Tuple<>(branch.id.getNamespace(), branch.id.getPath())));
            if(branch.translatedName != null && !branch.translatedName.isEmpty())
            {
                addLanguageKeySafe(langJson, "com." + branch.id.getNamespace() + ".research." + branch.id.getPath().replaceAll("[/]",".") + ".name", branch.translatedName);
            }
            if(branch.translatedSubtitle != null && !branch.translatedSubtitle.isEmpty())
            {
                addLanguageKeySafe(langJson, "com." + branch.id.getNamespace() + ".research." + branch.id.getPath().replaceAll("[/]",".") + ".subtitle", branch.translatedSubtitle);
            }
        }
        for(final ResearchEffect effect : getResearchEffectCollection())
        {
            research.add(new Tuple<>(effect.json, new Tuple<>(effect.id.getNamespace(), effect.id.getPath())));
            if(effect.translatedName != null && !effect.translatedName.isEmpty())
            {
                addLanguageKeySafe(langJson, "com." + effect.id.getNamespace() + ".research." + effect.id.getPath().replaceAll("[/]",".") + ".description", effect.translatedName);
            }
            if(effect.translatedSubtitle != null && !effect.translatedSubtitle.isEmpty())
            {
                addLanguageKeySafe(langJson, "com." + effect.id.getNamespace() + ".research." + effect.id.getPath().replaceAll("[/]",".") + ".subtitle", effect.translatedSubtitle);
            }
        }
        for(final Research research : getResearchCollection())
        {
            this.research.add(new Tuple<>(research.json, new Tuple<>(research.id.getNamespace(), research.id.getPath())));

            if(research.translatedName != null && !research.translatedName.isEmpty())
            {
                addLanguageKeySafe(langJson, "com." + research.id.getNamespace() + ".research." + research.id.getPath().replaceAll("[/]",".") + ".name", research.translatedName);
            }
            if(research.translatedSubtitle != null && !research.translatedSubtitle.isEmpty())
            {
                addLanguageKeySafe(langJson, "com." + research.id.getNamespace() + ".research." + research.id.getPath().replaceAll("[/]",".") + ".subtitle", research.translatedSubtitle);
            }
        }
        return generateAll(cache, langJson);
    }

    protected CompletableFuture<?> generateAll(CachedOutput cache, final JsonObject langJson)
    {
        CompletableFuture<?>[] futures = new CompletableFuture<?>[this.research.size() + 1];
        int i = 0;

        final PackOutput.PathProvider langPath = this.packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "lang");
        final PackOutput.PathProvider researchesPath = this.packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "researches");

        for (Tuple<JsonObject, Tuple<String, String>> model : this.research)
        {
            final Path target = researchesPath.json(new ResourceLocation(model.getB().getA(), model.getB().getB()));
            futures[i++] = DataProvider.saveStable(cache, model.getA(), target);
        }

        futures[i] = DataProvider.saveStable(cache, langJson, langPath.json(new ResourceLocation(Constants.MOD_ID, "default")));

        return CompletableFuture.allOf(futures);
    }

    /**
     * Safely add a language key, removing any previous instances if already present.
     * @param langJson      The json to add the key onto.
     * @param key           The tag, generally a translation key.
     * @param property      The property, generally translated text.
     */
    private void addLanguageKeySafe(final JsonElement langJson, final String key, final String property)
    {
        if(langJson.getAsJsonObject().has(key))
        {
            langJson.getAsJsonObject().remove(key);
        }
        langJson.getAsJsonObject().addProperty(key, property);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Research Data Provider";
    }

    /**
     * A Builder-like class for producing Researches.
     */
    protected static class Research
    {
        final public JsonObject       json = new JsonObject();
        final public ResourceLocation id;
        /**
         * The university level of the research.
         */
        public int researchLevel;
        /**
         *  A Translated Name to add to the output language file.
         */
        public String translatedName;
        /**
         *  A Translated Subtitle to add to the output language file.
         */
        public String translatedSubtitle;

        /**
         * Creates a Research for later assembly into a JSON file.
         * @param id            A unique identifier.  Suggested path format is branch/name.json.
         * @param branch        A branch unique identifier.  This will determine the location of a branch JSON, if present.
         */
        public Research(final ResourceLocation id, final ResourceLocation branch)
        {
            this.id = id;
            this.researchLevel = 1;
            this.json.addProperty("branch", branch.toString());
            this.json.addProperty("researchLevel", 1);
        }

        /**
         * Set the Parent Research.  Parent Research must be unlocked and complete before this research is available.
         * For now, all research above level 1 must have one parent.
         * If not set, assumes level 1.
         * @param parent              The parent research.
         * @return this
         */
        public Research setParentResearch(Research parent)
        {
            this.json.addProperty("parentResearch", parent.id.toString());
            this.json.remove("researchLevel");
            this.researchLevel = parent.researchLevel + 1;
            this.json.addProperty("researchLevel", this.researchLevel);
            return this;
        }

        /**
         * Set the research's name.  This may be a human-readable name.
         * If using a translation key, use setTranslatedName instead.
         * @param name              The research's displayed name, as a human-readable string or translation key.
         * @return this
         */
        public Research setName(final String name)
        {
            this.json.addProperty("name", name);
            return this;
        }

        /**
         * Set the Translated Name.  This will use the auto-generation key, and add a matching translation text to the language output file.
         * @param translatedName    The research's human-readable name.
         * @return this
         */
        public Research setTranslatedName(final String translatedName)
        {
            this.translatedName = translatedName;
            return this;
        }

        /**
         * Set the subtitle, as a human-readable name.
         * If using a translation key, use setTranslatedSubtitle.
         * @param subtitle    The research's human-readable subtitle.
         * @return this
         */
        public Research setSubtitle(final String subtitle)
        {
            this.json.addProperty("subtitle", subtitle);
            return this;
        }

        /**
         * Sets a subtitle translation key.  This will use an auto-generated key, and add a matching translation text to the language output file.
         * @param translatedSubtitle    The research's human-readable subtitle.
         * @return this
         */
        public Research setTranslatedSubtitle(final String translatedSubtitle)
        {
            this.translatedSubtitle = translatedSubtitle;
            this.json.addProperty("subtitle", "com." + id.getNamespace() + ".research." + id.getPath().replaceAll("[ /]",".") + ".subtitle");
            return this;
        }

        /**
         * Sets the sort order for the research.  Sibling research with a greater number will be placed lower on the University Window.
         * Only applies for research with siblings.
         * @param sortNum               The numeric value for vertical sorting priority.
         * @return this
         */
        public Research setSortOrder(final int sortNum)
        {
            this.json.addProperty("sortOrder", sortNum);
            return this;
        }

        /**
         * Sets only child status.  OnlyChild research will allow at most one descendant
         * to be completed at a time, and makes any descendant research resettable.
         * Only applies to research with multiple immediate descendants.
         * @return this
         */
        public Research setOnlyChild()
        {
            this.json.addProperty("exclusiveChildResearch", true);
            return this;
        }

        /**
         * Sets NoReset status.  NoReset research can not be undone once complete, even if level 6 or descending from an onlyChild research.
         * This is most relevant for researches that may cause inconsistent or incoherent behavior if reset.
         * Only required when a Research is level 6, or where it or an ancestor research is onlyChild.
         * @return this
         */
        public Research setNoReset()
        {
            this.json.addProperty("no-reset", true);
            return this;
        }

        /**
         * Sets autoStart status.  Once all requirements are met, autostart Research will either begin (if no item costs are set)
         * or notify colony players of its existence (if the research has item costs).
         * @return this
         */
        public Research setAutostart()
        {
            this.json.addProperty("autostart", true);
            return this;
        }

        /**
         * Sets instant status.  Once begun, this research will complete Soon(tm), regardless of branch time multipliers or research depth.
         * @return this
         */
        public Research setInstant()
        {
            this.json.addProperty("instant", true);
            return this;
        }

        /**
         * Sets hidden status.  Hidden research will not be visible in the University BOWindow until all requirements are met.
         * Research branches where all research is hidden or a descendant of a hidden research will be locked until at least one research is available.
         * Locked branches will notify what requirements will unlock the branch on mouseover.
         * @return this
         */
        public Research setHidden()
        {
            this.json.addProperty("hidden", true);
            return this;
        }

        /**
         * Sets an Item research icon.  This icon will only be displayed after research is completed.
         * @param item  The itemStack, including count, to use as an icon.
         * @return this
         */
        public Research setIcon(final ItemStack item)
        {
            if(json.has("icon"))
            {
                json.remove("icon");
            }
            this.json.addProperty("icon", BuiltInRegistries.ITEM.getKey(item.getItem()).toString() + ":" + item.getCount());
            return this;
        }

        /**
         * Sets an Item research icon.  This icon will only be displayed after research is completed.
         * @param item  The item to use as an icon.
         * @return this
         */
        public Research setIcon(final Item item)
        {
            if(json.has("icon"))
            {
                json.remove("icon");
            }
            this.json.addProperty("icon", BuiltInRegistries.ITEM.getKey(item).toString());
            return this;
        }

        /**
         * Sets an Item research icon.  This icon will only be displayed after research is completed.
         * @param item  The item to use as an icon.
         * @param count The number to mark the icon.
         * @return this
         */
        public Research setIcon(final Item item, final int count)
        {
            if(json.has("icon"))
            {
                json.remove("icon");
            }
            this.json.addProperty("icon", BuiltInRegistries.ITEM.getKey(item).toString() + ":" + count);
            return this;
        }

        /**
         * Sets a texture research icon.  This icon will only be displayed after research is completed.
         * Overrides ItemStack and Item icons.
         * @param texture  The location of the texture file to use as an icon.
         * @return this
         */
        public Research setIcon(final ResourceLocation texture)
        {
            this.json.addProperty("icon", texture.toString());
            return this;
        }

        /**
         * Creates a Building-requirement related json property, with sanitization.
         * Temporary workaround for discrepancies between schematic ID and modBuildings ID.
         * @param propertyType     The type of building requirement.  Currently supports : 'building', 'mandatory-building', and 'alternate-building'
         * @param buildingName     The schematic name for the building.
         * @param level            The required level or sum of levels.
         * @return The json object
         */
        private JsonObject makeSafeBuildingProperty(final String propertyType, final String buildingName, final int level)
        {
            JsonObject req = new JsonObject();
            req.addProperty(propertyType, buildingName);
            req.addProperty("level", level);
            return req;
        }

        /**
         * Adds a building research requirement.  The colony must have at least as many levels of this building to begin the research
         * cumulative across all buildings of that type.  (ie, guardtower 8 is fulfilled by eight level-1 guard towers, four level-2 guard towers, two level-3 and a level-2 guard tower, etc)
         * See ModBuildings for a list of supported buildings.  Whenever possible, use the public static String BUILDINGNAME_ID constants from ModBuildings.
         * Multiple different buildings can be added as different BuildingRequirements, and all must be fulfilled to begin research.
         * @param buildingName  The name of the building to require.  Derived from SchematicName.
         * @param level         The required sum of levels across the colony.
         * @return this
         */
        public Research addBuildingRequirement(final String buildingName, final int level)
        {
            final JsonArray reqArray;
            if(this.json.has("requirements") && this.json.get("requirements").isJsonArray())
            {
                reqArray = this.json.getAsJsonArray("requirements");
                this.json.remove("requirements");
            }
            else
            {
                reqArray = new JsonArray();
            }
            reqArray.add(makeSafeBuildingProperty("building", buildingName, level));
            this.json.add("requirements", reqArray);
            return this;
        }

        /**
         * Adds a mandatory building research requirement.  The colony must have one building at this specific level or greater.
         * (ie, guardtower 3 is fulfilled by one level-3 to level-5 guard tower, but no number of lower-level guard towers.)
         * This does not test whether the result is possible (eg, tavern-4 will not throw an exception, but can never be achieved in-game)
         * See ModBuildings for a list of supported buildings.  Whenever possible, use the public static String BUILDINGNAME_ID constants from ModBuildings.
         * Multiple different buildings can be added as different BuildingRequirements, and all must be fulfilled to begin research.
         * @param buildingName  The name of the building to require.  Derived from SchematicName.
         * @param level         The required sum of levels across the colony.
         * @return this
         */
        public Research addMandatoryBuildingRequirement(final String buildingName, final int level)
        {
            final JsonArray reqArray;
            if(this.json.has("requirements") && this.json.get("requirements").isJsonArray())
            {
                reqArray = this.json.getAsJsonArray("requirements");
                this.json.remove("requirements");
            }
            else
            {
                reqArray = new JsonArray();
            }
            reqArray.add(makeSafeBuildingProperty("mandatory-building", buildingName, level));
            this.json.add("requirements", reqArray);
            return this;
        }

        /**
         * Sets an alternate building research requirement.
         * The colony must have at least as many levels of at least one alternate building to begin the research,
         * cumulative across all buildings of that type.  Ie, AlternateBuildingRequirement of Tavern 3 / CitizenHouse 2 / University 2
         * would be fulfilled by any one of those buildings level 3, or by two citizen houses.
         * See ModBuildings for a list of supported buildings.  Whenever possible, use the public static String BUILDINGNAME_ID constants from ModBuildings.
         * Only one of all added Alternate Buildings is required.  AlternateBuildingRequirements do not bypass normal BuildingRequirements.
         * @param buildingName          The required building.
         * @param level                 The required sum of levels across the colony.
         * @return this
         */
        public Research addAlternateBuildingRequirement(final String buildingName, final int level)
        {
            final JsonArray reqArray;
            if(this.json.has("requirements") && this.json.get("requirements").isJsonArray())
            {
                reqArray = this.json.getAsJsonArray("requirements");
                this.json.remove("requirements");
            }
            else
            {
                reqArray = new JsonArray();
            }
            reqArray.add(makeSafeBuildingProperty("alternate-building", buildingName, level));
            this.json.add("requirements", reqArray);
            return this;
        }

        /**
         * The non-parent required research, which must be completed in addition to Parent research to begin this research.
         * This uses an auto-generated language key.  To override, add an additional string parameter.
         * Multiple ResearchRequirements are supported.  ResearchRequirements can apply from other branches.
         * If the research requirement does not exist, it is fulfilled automatically.
         * @param researchReq       The required research.
         * @return this
         */
        public Research addResearchRequirement(final ResourceLocation researchReq)
        {
            final JsonArray reqArray;
            if(this.json.has("requirements") && this.json.get("requirements").isJsonArray())
            {
                reqArray = this.json.getAsJsonArray("requirements");
                this.json.remove("requirements");
            }
            else
            {
                reqArray = new JsonArray();
            }
            JsonObject req = new JsonObject();
            req.addProperty("research", researchReq.toString());
            reqArray.add(req);
            this.json.add("requirements", reqArray);
            return this;
        }

        /**
         * The non-parent required research, which must be completed in addition to Parent research to begin this research.
         * This manually sets a requirement description.  To use the auto-generated key from the research itself, remove the String param.
         * Multiple ResearchRequirements are supported.  ResearchRequirements can apply from other branches.
         * If the research requirement does not exist, it is fulfilled automatically.
         * @param researchReq  The id of the required research.
         * @param name         The human-readable name of the required research.
         * @return this
         */
        public Research addResearchRequirement(final ResourceLocation researchReq, final String name)
        {
            final JsonArray reqArray;
            if(this.json.has("requirements") && this.json.get("requirements").isJsonArray())
            {
                reqArray = this.json.getAsJsonArray("requirements");
                this.json.remove("requirements");
            }
            else
            {
                reqArray = new JsonArray();
            }
            JsonObject req = new JsonObject();
            req.addProperty("research", researchReq.toString());
            req.addProperty("name", name);
            reqArray.add(req);
            this.json.add("requirements", reqArray);
            return this;
        }

        /**
         * Adds an item cost to the research. This will be consumed when beginning the research, and will not be refunded.
         * Multiple ItemCosts are supported, but for UI reasons it's encouraged to keep to 5 or less.
         *
         * @param item  The item to require.
         * @param count The number of the item to require.
         * @return this.
         */
        public Research addItemCost(final Item item, final int count)
        {
            return addItemCost(List.of(item), count);
        }

        /**
         * Adds an item cost to the research. This will be consumed when beginning the research, and will not be refunded.
         * Multiple ItemCosts are supported, but for UI reasons it's encouraged to keep to 5 or less.
         *
         * @param items The item to require.
         * @param count The number of the item to require.
         * @return this.
         */
        public Research addItemCost(final List<Item> items, final int count)
        {
            final JsonArray reqArray = getRequirementsArray();

            JsonArray itemArr = new JsonArray();
            for (Item item : items)
            {
                itemArr.add(BuiltInRegistries.ITEM.getKey(item).toString());
            }

            JsonObject itemObj = new JsonObject();
            itemObj.add("items", itemArr);

            JsonObject req = new JsonObject();
            req.addProperty("type", LIST_ITEM_COST_ID.toString());
            req.add("item",itemObj);
            req.addProperty("quantity", count);
            reqArray.add(req);

            this.json.add("requirements", reqArray);
            return this;
        }

        /**
         * Adds an item cost to the research. This will be consumed when beginning the research, and will not be refunded.
         * Multiple ItemCosts are supported, but for UI reasons it's encouraged to keep to 5 or less.
         *
         * @param tag   The tag to require.
         * @param count The number of the item to require.
         * @return this.
         */
        public Research addItemCost(final TagKey<Item> tag, final int count)
        {
            final JsonArray reqArray = getRequirementsArray();

            JsonObject itemObj = new JsonObject();
            itemObj.addProperty("tag", tag.location().toString());

            JsonObject req = new JsonObject();
            req.addProperty("type", TAG_ITEM_COST_ID.toString());
            req.add("item", itemObj);
            req.addProperty("quantity", count);
            reqArray.add(req);

            this.json.add("requirements", reqArray);
            return this;
        }

        /**
         * Internal method to ensure the requirements array exists.
         *
         * @return the requirements array.
         */
        private JsonArray getRequirementsArray()
        {
            final JsonArray reqArray;
            if (this.json.has("requirements") && this.json.get("requirements").isJsonArray())
            {
                reqArray = this.json.getAsJsonArray("requirements");
                this.json.remove("requirements");
            }
            else
            {
                reqArray = new JsonArray();
            }
            return reqArray;
        }

        /**
         * Add an effect to the research.  Research Effects are applied on completion,
         * and remain unless the colony is destroyed or the research is undone.
         * Multiple Effects are supported.
         * @param effect    the id of the research effect to apply on completion.
         * @param level     the strength of the research effect to apply on completion.
         * @return this
         */
        public Research addEffect(final ResourceLocation effect, final int level)
        {
            final JsonArray effects;
            if(this.json.has("effects") && this.json.get("effects").isJsonArray())
            {
                effects = this.json.getAsJsonArray("effects");
                this.json.remove("effects");
            }
            else
            {
                effects = new JsonArray();
            }
            JsonObject eff = new JsonObject();
            eff.addProperty(effect.toString(), level);
            effects.add(eff);
            this.json.add("effects", effects);
            return this;
        }

        /**
         * Add an unlock building effect to the research.  Research Effects are applied on completion,
         * and remain unless the colony is destroyed or the research is undone.
         * See ModBuildings for a list of supported buildings.  Whenever possible, use the public static String BUILDINGNAME_ID constants from ModBuildings
         * Buildings with no applicable research effects loaded default to unlocked.
         * Multiple Effects are supported.
         * @param buildingBlock    the building block to lock behind this research.
         * @param level            the strength of the research effect to apply on completion.
         *                    Automatically generated effects will unlock up to Building Tier 10 at Level 1.
         *                    Manually generated effects can limited to individual tiers based on strength.
         * @return this
         */
        public Research addEffect(final AbstractBlockHut<?> buildingBlock, int level)
        {
            final JsonArray effects;
            if(this.json.has("effects") && this.json.get("effects").isJsonArray())
            {
                effects = this.json.getAsJsonArray("effects");
                this.json.remove("effects");
            }
            else
            {
                effects = new JsonArray();
            }
            final ResourceLocation registryName = BuiltInRegistries.BLOCK.getKey(buildingBlock);
            JsonObject eff = new JsonObject();
            eff.addProperty(registryName.getNamespace() + ":effects/" + registryName.getPath(), level);
            effects.add(eff);
            this.json.add("effects", effects);
            return this;
        }

        /**
         * Sets the Research to be removed, for its own ResourceLocation.
         * Prevents load (though not data gen) of all other settings.  Not compatible with other variants of setRemove.
         * @return this
         */
        public Research setRemove()
        {
            this.json.addProperty("remove", true);
            return this;
        }

        /**
         * Sets the Research JSON to remove a different individual research, by ResourceLocation.
         * Prevents load (though not data gen) of all other settings.  Not compatible with other variants of setRemove.
         * @param researchId  The target research.
         * @return this
         */
        public Research setRemove(final ResourceLocation researchId)
        {
            this.json.addProperty("remove", researchId.toString());
            return this;
        }

        /**
         * Sets the Research JSON to remove multiple individual research, by ResourceLocations.
         * Prevents load (though not data gen) of all other settings.  Not compatible with other variants of setRemove.
         * @param researchIds  The target research.
         * @return this
         */
        public Research setRemove(final Collection<ResourceLocation> researchIds)
        {
            JsonArray removes = new JsonArray();
            for(ResourceLocation rem : researchIds)
            {
                removes.add(rem.toString());
            }
            this.json.add("remove", removes);
            return this;
        }

        /**
         * Add the Research to a collection, and return the same research.
         * essentially the same as List.add() with a useful return.
         * @param list      The list to add the Research onto.
         * @return this
         */
        public Research addToList(final Collection<Research> list)
        {
            list.add(this);
            return this;
        }
    }

    /**
     * A Builder-like class for producing Research Effects.
     */
    protected static class ResearchEffect
    {
        final public JsonObject       json = new JsonObject();
        final public ResourceLocation id;
        /**
         *  A Translated Name to add to the output language file.
         */
        public String translatedName;
        /**
         *  A Translated Subtitle to add to the output language file.
         */
        public String translatedSubtitle;

        /**
         * Creates a new instances of a ResearchEffect.
         * @param id    A unique identifier.  Suggested path format is effects/name.json.
         */
        public ResearchEffect(final ResourceLocation id)
        {
            this.id = id;
            this.json.addProperty("effect", true);
        }

        /**
         * Creates a new instances of a ResearchEffect that locks and unlocks a Building
         * See ModBuildings for a list of supported buildings.
         * @param buildingBlock    A Building hut block.  This will auto-generate an unlock effect ID of effects/blockhutname.json.
         */
        public ResearchEffect(final AbstractBlockHut<?> buildingBlock)
        {
            final ResourceLocation registryName = BuiltInRegistries.BLOCK.getKey(buildingBlock);
            this.id = new ResourceLocation(registryName.getNamespace(), "effects/" + registryName.getPath());
            this.json.addProperty("effect", true);
        }

        /**
         * Create a new instance of a ResearchEffect with an effectType.
         * @param id    A unique identifier.  Suggested path format is effects/name.json.
         * @param type  The type of the research effect.
         */
        public ResearchEffect(final ResourceLocation id, final String type)
        {
            this.id = id;
            this.json.addProperty("effect", true);
            this.json.addProperty("effectType", type);
        }

        /**
         * Set the levels of the research effect, starting at level 1.
         * Level 0 will be automatically populated during JsonLoad on the server, and set to a value of zero.
         * @param strengths  The strengths of the research effect.
         * @return this
         */
        public ResearchEffect setLevels(final double[] strengths)
        {
            JsonArray child = new JsonArray();
            for(double str : strengths)
            {
                child.add(str);
            }
            this.json.add("levels", child);
            return this;
        }

        /**
         * Set the research name as a human-readable string.
         * For translation keys, use setTranslatedName instead.
         * @param name      The human-readable name.
         * @return this
         */
        public ResearchEffect setName(final String name)
        {
            this.json.addProperty("name", name);
            return this;
        }

        /**
         * Set the research name, using an auto-generated translation key, and add a matching translation text to the language output file.
         * @param name      The human-readable name.
         * @return this
         */
        public ResearchEffect setTranslatedName(final String name)
        {
            this.translatedName = name;
            return this;
        }

        /**
         * Adds a human-readable subtitle to the Research Effect json.
         * @param subtitle      A human-readable subtitle.  This will only show on tooltips.
         * @return this
         */
        public ResearchEffect setSubtitle(final String subtitle)
        {
            this.json.addProperty("subtitle", subtitle);
            return this;
        }

        /**
         * Adds a translated subtitle to the language file, and the translation key to the ResearchEffect json.
         * @param subtitle      A human-readable subtitle.  This will only show on tooltips.
         * @return this
         */
        public ResearchEffect setTranslatedSubtitle(final String subtitle)
        {
            this.translatedSubtitle = subtitle;
            this.json.addProperty("subtitle", "com." + this.id.getNamespace() + ".research." + this.id.getPath().replaceAll("[/]",".") + ".subtitle");
            return this;
        }
    }

    /**
     * A Builder-like class for producing Research Branches
     */
    protected static class ResearchBranch
    {
        final public JsonObject       json = new JsonObject();
        final public ResourceLocation id;
        /**
         *  A Translated Name to add to the output language file.
         */
        public String translatedName;
        /**
         *  A Translated Subtitle to add to the output language file.
         */
        public String translatedSubtitle;

        /**
         * Creates a Research Branch.
         * @param id    A unique identifier.  Suggested path format is name.json.
         */
        public ResearchBranch(final ResourceLocation id)
        {
            this.id = id;
        }

        /**
         * Sets the Branch name.  This may be a human-readable string.
         * For translation keys, use setTranslatedBranchName instead.
         * @param branchName  The human-readable string.
         * @return this
         */
        public ResearchBranch setBranchName(final String branchName)
        {
            this.json.addProperty("branch-name", branchName);
            return this;
        }

        /**
         * Sets the Branch Translated name for later recording to the language file, and assigns an auto-generated translation key.
         * @param translatedBranchName  The human-readable string.
         * @return this
         */
        public ResearchBranch setTranslatedBranchName(final String translatedBranchName)
        {
            this.translatedName = translatedBranchName;
            this.json.addProperty("branch-name", "com." + id.getNamespace() + ".research." + id.getPath().replaceAll("[ /]",".") + ".name");
            return this;
        }

        /**
         * Set the subtitle, as a human-readable name.
         * If using a translation key, use setTranslatedSubtitle.
         * @param subtitle    The research's human-readable subtitle.
         * @return this
         */
        public ResearchBranch setSubtitle(final String subtitle)
        {
            this.json.addProperty("subtitle", subtitle);
            return this;
        }

        /**
         * Sets a subtitle translation key.  This will use an auto-generated key, and add a matching translation text to the language output file.
         * @param translatedSubtitle    The research's human-readable subtitle.
         * @return this
         */
        public ResearchBranch setTranslatedSubtitle(final String translatedSubtitle)
        {
            this.translatedSubtitle = translatedSubtitle;
            this.json.addProperty("subtitle", "com." + id.getNamespace() + ".research." + id.getPath().replaceAll("[ /]",".") + ".subtitle");
            return this;
        }

        /**
         * Sets the Branch Time Multiplier.  This increases or decreases the worker time required
         * to complete all research on this branch.  Larger numbers go slower, while lower numbers go faster.
         * Very low values may have unpredictable results.  Defaults to 1.0
         * @param branchTimeMultiplier  The multiplier to set.
         * @return this
         */
        public ResearchBranch setBranchTimeMultiplier(final double branchTimeMultiplier)
        {
            this.json.addProperty("base-time", branchTimeMultiplier);
            return this;
        }

        /**
         * Sets a research branch type.  This styles presentation of the branch at the university, and
         * can have logic or colony ramifications.  See {@link com.minecolonies.api.research.ResearchBranchType} for details.
         * Branches with no branch-type will default to ResearchBranchTyupe.DEFAULT
         * @param type      The style of the research branch.
         * @return this
         */
        public ResearchBranch setBranchType(final ResearchBranchType type)
        {
            this.json.addProperty("branch-type", type.tag);
            return this;
        }

        /**
         * The sorting order of the research branches within the Minecolonies University Window.
         * Higher numbers will appear lower on the list.  Defaults to 0, accepts negative numbers.
         * Builtin branches should be separated by large ranges, to allow possible inserts by third parties.
         * @param sortOrder   The sorting order of the branches.
         * @return this
         */
        public ResearchBranch setBranchSortOrder(final int sortOrder)
        {
            this.json.addProperty("sortOrder", sortOrder);
            return this;
        }

        /**
         * If true, hides a research branch within the University BOWindow until at least one research has at least one research that isn't hidden, or with requirements met, or complete.
         * This is mostly intended to avoid spoilers, or to prevent branches with many primary researches from showing a giant and useless tooltip.
         * Only applies to branches where all primary researches are hidden.
         * Defaults to false.
         * @param hidden If true, hides the branch until at least one research is available.
         * @return this
         */
        public ResearchBranch setHidden(final boolean hidden)
        {
            this.json.addProperty("hidden", hidden);
            return this;
        }

        /**
         * Sets the Research Branch JSON to remove all researches attached to its branch.
         * Avoid use where stacking data packs are possible, as only last JSON for a ResourceLocation wins.
         * Not compatible with other variants of setRemove.
         * @return this
         */
        public ResearchBranch setRemove()
        {
            this.json.addProperty("base-time", 1.0);
            this.json.addProperty("remove", true);
            return this;
        }

        /**
         * Sets the Research Branch JSON to remove a different branch and all dependent researches, by ResourceLocation.
         * Not compatible with other variants of setRemove.
         * @param branchId  The target research branch.
         * @return this
         */
        public ResearchBranch setRemove(final ResourceLocation branchId)
        {
            this.json.addProperty("base-time", 1.0);
            this.json.addProperty("remove", branchId.toString());
            return this;
        }

        /**
         * Sets the Research JSON to remove different branches and all dependent researches, by ResourceLocation.
         * Not compatible with other variants of setRemove.
         * @param branchIds  The target research branch.
         * @return this
         */
        public ResearchBranch setRemove(final Collection<ResourceLocation> branchIds)
        {
            this.json.addProperty("base-time", 1.0);
            final JsonArray removes = new JsonArray();
            for(ResourceLocation rem : branchIds)
            {
                removes.add(rem.toString());
            }
            this.json.add("remove", removes);
            return this;
        }
    }
}
