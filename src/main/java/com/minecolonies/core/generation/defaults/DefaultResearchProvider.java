package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.research.AbstractResearchProvider;
import com.minecolonies.api.research.ResearchBranchType;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.minecolonies.api.research.util.ResearchConstants.*;

/**
 * A class for creating the Research-related JSONs, including Research, ResearchEffects, and (optional) Branches.
 * Note that this does not validate that the resulting research tree is coherent:
 * programmers should make sure that research parents and effects exist, that depth is 1 or one level above the parent depth,
 * and that cost and requirement identifiers match real items.
 *
 * Avoid changing research resource locations here unless necessary.
 * If such a change is required, add the old and new ResearchIds to ResearchCompatMap.
 * ResearchIDs are stored to disk, and if not present on a GlobalResearchTree during colony load, will be lost.
 * Effect and Branch ResourceLocations are not stored to disk, but changing them may cause confusion with outside data packs.
 */
public class DefaultResearchProvider extends AbstractResearchProvider
{
    public DefaultResearchProvider(@NotNull final PackOutput packOutput,
                                   @NotNull final CompletableFuture<HolderLookup.Provider> provider)
    {
        super(packOutput, provider);
    }

    private static final ResourceLocation CIVIL  = new ResourceLocation(Constants.MOD_ID, "civilian");
    private static final ResourceLocation COMBAT = new ResourceLocation(Constants.MOD_ID, "combat");
    private static final ResourceLocation TECH   = new ResourceLocation(Constants.MOD_ID, "technology");
    private static final ResourceLocation UNLOCK = new ResourceLocation(Constants.MOD_ID, "unlockable");

    /**
     * Get a list of all research branches. Conventions: these are not mandatory, and their inclusion simply fixes capitalization and sorting. MineColonies should fully populate new branches
     * for clarity; other data pack makers may not want to do so.  Branch Sort Order should be separated by large values, to allow possible third-party inserts between existing branches.
     * Only use setBranchType for non-default styled branches, and use {@link com.minecolonies.api.research.ResearchBranchType} to do so.
     *
     * @return a complete list of all research branches.
     */
    @Override
    public Collection<ResearchBranch> getResearchBranchCollection()
    {
        final List<ResearchBranch> branches = new ArrayList<>();
        branches.add(new ResearchBranch(CIVIL).setTranslatedBranchName("Civilian").setBranchTimeMultiplier(1.0).setBranchSortOrder(50));
        branches.add(new ResearchBranch(COMBAT).setTranslatedBranchName("Combat").setBranchTimeMultiplier(1.0).setBranchSortOrder(100));
        branches.add(new ResearchBranch(TECH).setTranslatedBranchName("Technology").setBranchTimeMultiplier(1.0).setBranchSortOrder(150));
        branches.add(new ResearchBranch(UNLOCK).setTranslatedBranchName("Unlockables").setBranchTimeMultiplier(0.0).setBranchSortOrder(200).setBranchType(ResearchBranchType.UNLOCKABLES).setHidden(true));
        return branches;
    }

    /**
     * Get a list of all research effects. Conventions: group effects by type (unlock-building, ability, or strength-based), and then by subject (citizen, building, colony).
     * Unlock-building effect ids are automatically generated from hut block ids; all other effect IDs should use {@link com.minecolonies.api.research.util.ResearchConstants}
     *
     * @return a complete list of all research effects.
     */
    @Override
    public Collection<ResearchEffect> getResearchEffectCollection()
    {
        final List<ResearchEffect> effects = new ArrayList<>();

        //Multiplier and Addition modifiers have Research Constant ResourceLocations, and require setLevels.
        effects.add(new ResearchEffect(ARCHER_ARMOR).setTranslatedName("Archer Armor +%3$s%%").setLevels(new double[] {0.05, 0.1, 0.25, 0.5, 1}));
        effects.add(new ResearchEffect(ARCHER_DAMAGE).setTranslatedName("Archer Damage +%s").setLevels(new double[] {0.5, 1, 1.5, 2, 4}));
        effects.add(new ResearchEffect(ARMOR_DURABILITY).setTranslatedName("Guard Armor +%3$s%% Durability").setLevels(new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 1}));
        effects.add(new ResearchEffect(BLOCK_ATTACKS).setTranslatedName("Knight Shield Blocking Chance +%3$s%%").setLevels(new double[] {0.05, 0.1, 0.25, 0.5}));
        effects.add(new ResearchEffect(BLOCK_BREAK_SPEED).setTranslatedName("Citizen Block Break Speed +%3$s%%").setLevels(new double[] {0.1, 0.25, 0.5, 1, 2}));
        effects.add(new ResearchEffect(BLOCK_PLACE_SPEED).setTranslatedName("Citizen Block Place Speed +%3$s%%").setLevels(new double[] {0.1, 0.25, 0.5, 1, 2}));
        effects.add(new ResearchEffect(CITIZEN_CAP).setTranslatedName("Max Citizens to %2$s")
                      .setLevels(new double[] {CitizenConstants.CITIZEN_LIMIT_DEFAULT, CitizenConstants.CITIZEN_LIMIT_OUTPOST, CitizenConstants.CITIZEN_LIMIT_HAMLET,
                        CitizenConstants.CITIZEN_LIMIT_VILLAGE,
                        CitizenConstants.CITIZEN_LIMIT_MAX}));
        effects.add(new ResearchEffect(CITIZEN_INV_SLOTS).setTranslatedName("Citizen Inventory +%s Slots").setLevels(new double[] {9, 18, 27}));
        effects.add(new ResearchEffect(DOUBLE_ARROWS).setTranslatedName("Archer Multishot +%3$s%%").setLevels(new double[] {0.05, 0.1, 0.25, 0.5}));
        effects.add(new ResearchEffect(MECHANIC_ENHANCED_GATES).setTranslatedName("Gates Gain +100% Raider Swarm Resistance").setLevels(new double[] {5, 15}));
        effects.add(new ResearchEffect(FARMING).setTranslatedName("Farmers Harvest +%3$s%% Crops").setLevels(new double[] {0.1, 0.25, 0.5, 0.75, 2}));
        effects.add(new ResearchEffect(FLEEING_DAMAGE).setTranslatedName("Guards Take -%3$s%% Damage When Fleeing").setLevels(new double[] {0.2, 0.3, 0.4, 0.75}));
        effects.add(new ResearchEffect(FLEEING_SPEED).setTranslatedName("Fleeing Guards Gain Swiftness %2$s").setLevels(new double[] {1, 2, 3, 5}));
        effects.add(new ResearchEffect(GROWTH).setTranslatedName("Child Growth Rate +%3$s%%").setLevels(new double[] {0.05, 0.1, 0.25, 0.5, 1}));
        effects.add(new ResearchEffect(HAPPINESS).setTranslatedName("Citizen Happiness +%3$s%%").setLevels(new double[] {0.05, 0.1, 0.15, 0.2, 0.5}));
        effects.add(new ResearchEffect(SATLIMIT).setTranslatedName("Healing Saturation Min %s").setLevels(new double[] {-0.5, -1, -1.5, -2, -5}));
        effects.add(new ResearchEffect(HEALTH_BOOST).setTranslatedName("Citizen HP +%s").setLevels(new double[] {2, 4, 6, 8, 10, 20}));
        effects.add(new ResearchEffect(LEVELING).setTranslatedName("Citizen XP Growth +%3$s%%").setLevels(new double[] {0.05, 0.1, 0.25, 0.5, 1}));
        effects.add(new ResearchEffect(MELEE_ARMOR).setTranslatedName("Knights Armor +%3$s%%").setLevels(new double[] {0.05, 0.1, 0.25, 0.5, 1}));
        effects.add(new ResearchEffect(MELEE_DAMAGE).setTranslatedName("Knight Damage +%s").setLevels(new double[] {0.5, 1, 1.5, 2, 4}));
        effects.add(new ResearchEffect(MINIMUM_STOCK).setTranslatedName("Buildings Can Minimum Stock %3$s%% More").setLevels(new double[] {0.5, 1, 2}));
        effects.add(new ResearchEffect(MORE_ORES).setTranslatedName("Miners Find +%3$s%% More Ores").setLevels(new double[] {0.1, 0.25, 0.5, 1, 2}));
        effects.add(new ResearchEffect(PODZOL_CHANCE).setTranslatedName("Composters Get +%3$s%% More Podzol").setLevels(new double[] {1, 2}));
        effects.add(new ResearchEffect(RECIPES).setTranslatedName("Workers Can Learn +%3$s%% More Recipes").setLevels(new double[] {0.25, 0.5, 1, 2}));
        effects.add(new ResearchEffect(REGENERATION).setTranslatedName("Citizen Regeneration +%3$s%%").setLevels(new double[] {0.1, 0.25, 0.5, 1, 2}));
        effects.add(new ResearchEffect(SATURATION).setTranslatedName("Citizen Saturation Per Meal +%3$s%%").setLevels(new double[] {0.1, 0.25, 0.5, 1, 2}));
        effects.add(new ResearchEffect(TEACHING).setTranslatedName("XP Gain When Studying +%3$s%%").setLevels(new double[] {0.05, 0.1, 0.25, 0.5, 1}));
        effects.add(new ResearchEffect(TOOL_DURABILITY).setTranslatedName("Citizen Tools +%3$s%% Durability").setLevels(new double[] {0.05, 0.1, 0.25, 0.5, 0.9}));
        effects.add(new ResearchEffect(WALKING).setTranslatedName("Citizen Walk Speed +%3$s%%").setLevels(new double[] {0.05, 0.1, 0.15, 0.25}));
        effects.add(new ResearchEffect(WORK_LONGER).setTranslatedName("Citizen Work Day +%sH").setLevels(new double[] {1, 2}));
        effects.add(new ResearchEffect(RESURRECT_CHANCE).setTranslatedName("Improve Resurrection Chance by +%3$s%%").setLevels(new double[] {0.01, 0.03}));
        effects.add(new ResearchEffect(GRAVE_DECAY_BONUS).setTranslatedName("Citizen Graves Take %s More Minutes to Decay").setLevels(new double[] {2, 5}));

        // Guard and Worker unlocks do not need a strength, but do have static ResourceLocations.
        effects.add(new ResearchEffect(ARCHER_USE_ARROWS).setTranslatedName("Archers Use Arrows For +2 Damage"));
        effects.add(new ResearchEffect(DRUID_USE_POTIONS).setTranslatedName("Druids request Magic Potions to unlock new Abilities"));
        effects.add(new ResearchEffect(CRUSHING_11).setTranslatedName("Crusher Recipes Cost -50%"));
        effects.add(new ResearchEffect(KNIGHT_TAUNT).setTranslatedName("Knights Force Mobs to Target Them"));
        effects.add(new ResearchEffect(FIRE_RES).setTranslatedName("Miners Have Fire and Lava Immunity"));
        effects.add(new ResearchEffect(ARROW_PIERCE).setTranslatedName("Archers Gain Piercing II"));
        effects.add(new ResearchEffect(PLANTATION_LARGE).setTranslatedName("Plantations Unlock 1 Additional Field"));
        effects.add(new ResearchEffect(PLANTATION_JUNGLE).setTranslatedName("Plantations Unlock Fields For: Bamboo, Cocoa and Vines"));
        effects.add(new ResearchEffect(PLANTATION_SEA).setTranslatedName("Plantations Unlock Fields For: Kelp, Seagrass and Sea pickles"));
        effects.add(new ResearchEffect(PLANTATION_EXOTIC).setTranslatedName("Plantations Unlock Fields For: Glowberries"));
        effects.add(new ResearchEffect(PLANTATION_NETHER).setTranslatedName("Plantations Unlock Fields For: Crimson/Warped fungi, roots and vines"));
        effects.add(new ResearchEffect(BEEKEEP_2).setTranslatedName("Beekeepers Can Harvest Both Honey Bottles and Combs at Once"));
        effects.add(new ResearchEffect(RAILS).setTranslatedName("Citizens use Rails"));
        effects.add(new ResearchEffect(VINES).setTranslatedName("Citizens can climb Vines"));
        effects.add(new ResearchEffect(RETREAT).setTranslatedName("Guards Flee Under 20% HP"));
        effects.add(new ResearchEffect(SHIELD_USAGE).setTranslatedName("Knights Unlock Shield Use"));
        effects.add(new ResearchEffect(SLEEP_LESS).setTranslatedName("Guards Need %3$s%% Less Sleep"));
        effects.add(new ResearchEffect(GUARD_CRIT).setTranslatedName("Guards have a chance to score critical hits").setLevels(new double[] {0.2, 0.3, 0.4, 0.5}));
        effects.add(new ResearchEffect(KNIGHT_WHIRLWIND).setTranslatedName("Knights Learn Special Attack That Damages and Knocks Back Nearby Enemies"));
        effects.add(new ResearchEffect(WORKING_IN_RAIN).setTranslatedName("Citizens Work in Rain"));
        effects.add(new ResearchEffect(UNDERTAKER_RUN).setTranslatedName("Undertaker unlocks run ability").setTranslatedSubtitle("Teach Undertaker the ability to run towards graves"));
        effects.add(new ResearchEffect(USE_TOTEM).setTranslatedName("Undertaker gains the ability to use Totems of Undying to assist in Resurrection"));
        effects.add(new ResearchEffect(RECIPE_MODE).setTranslatedName("Add the option to select recipes based on Warehouse stock"));
        effects.add(new ResearchEffect(BUILDER_MODE).setTranslatedName("Add the option to select different build-modes for your builders"));
        effects.add(new ResearchEffect(SOFT_SHOES).setTranslatedName("Farmers will no longer trample crops"));
        effects.add(new ResearchEffect(FISH_TREASURE).setTranslatedName("Fishers can find treasure outside the ocean"));
        effects.add(new ResearchEffect(NETHER_LOG).setTranslatedName("Adds expedition log to Nether Mine"));
        effects.add(new ResearchEffect(MASKS).setTranslatedName("Reduce disease transmission"));
        effects.add(new ResearchEffect(VACCINES).setTranslatedName("Citizens are immune for longer after treatment"));
        effects.add(new ResearchEffect(TELESCOPE).setTranslatedName("Farther rallying banner range"));
        effects.add(new ResearchEffect(STANDARD).setTranslatedName("Place Rallying Banner at location"));
        effects.add(new ResearchEffect(MORE_AIR).setTranslatedName("Citizens can stay longer underwater"));
        effects.add(new ResearchEffect(MIN_ORDER).setTranslatedName("Buildings wait a bit longer before placing orders"));

        // Building-focused unlocks are derived from the block hut name.  Do not manually add ResourceLocations as a string, as some building blocks have surprising names.
        effects.add(new ResearchEffect(ModBuildings.archery.get().getBuildingBlock()).setTranslatedName("Unlocks Archery").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.barracks.get().getBuildingBlock()).setTranslatedName("Unlocks Barracks").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.blacksmith.get().getBuildingBlock()).setTranslatedName("Unlocks Blacksmith's Hut").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.combatAcademy.get().getBuildingBlock()).setTranslatedName("Unlocks Combat Academy").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.composter.get().getBuildingBlock()).setTranslatedName("Unlocks Composter's Hut").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.concreteMixer.get().getBuildingBlock()).setTranslatedName("Unlocks Concrete Mixer's Hut").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.crusher.get().getBuildingBlock()).setTranslatedName("Unlocks Crusher's Hut").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.dyer.get().getBuildingBlock()).setTranslatedName("Unlocks Dyer's Hut").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.fletcher.get().getBuildingBlock()).setTranslatedName("Unlocks Fletcher's Hut").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.florist.get().getBuildingBlock()).setTranslatedName("Unlocks Flower Shop").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.graveyard.get().getBuildingBlock()).setTranslatedName("Unlocks Graveyard").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.glassblower.get().getBuildingBlock()).setTranslatedName("Unlocks Glassblower's Hut").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.hospital.get().getBuildingBlock()).setTranslatedName("Unlocks Hospital").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.library.get().getBuildingBlock()).setTranslatedName("Unlocks Library").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.mechanic.get().getBuildingBlock()).setTranslatedName("Unlocks Mechanic's Hut").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.mysticalSite.get().getBuildingBlock()).setTranslatedName("Unlocks Mystical Site").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.plantation.get().getBuildingBlock()).setTranslatedName("Unlocks Plantation").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.sawmill.get().getBuildingBlock()).setTranslatedName("Unlocks Sawmill").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.school.get().getBuildingBlock()).setTranslatedName("Unlocks School").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.sifter.get().getBuildingBlock()).setTranslatedName("Unlocks Sifter's Hut").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.smeltery.get().getBuildingBlock()).setTranslatedName("Unlocks Smeltery").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.stoneMason.get().getBuildingBlock()).setTranslatedName("Unlocks Stonemason's Hut").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.stoneSmelter.get().getBuildingBlock()).setTranslatedName("Unlocks Stone Smeltery").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.netherWorker.get().getBuildingBlock()).setTranslatedName("Unlocks Nether Mine").setLevels(new double[] {5}));
        effects.add(new ResearchEffect(ModBuildings.alchemist.get().getBuildingBlock()).setTranslatedName("Unlocks Alchemist").setLevels(new double[] {5}));


        // Crafter-recipe-only unlocks
        effects.add(new ResearchEffect(THE_END).setTranslatedName("Stonemasons Learn Endstone Recipe and Bakers Learn Chorus Bread Recipe"));
        effects.add(new ResearchEffect(THE_DEPTHS).setTranslatedName("Crusher Learns Deepslate and Tuff Recipes"));
        effects.add(new ResearchEffect(MORE_SCROLLS).setTranslatedName("Enchanter Learns Scroll Recipes to Locate Workers and Summon Guards"));
        effects.add(new ResearchEffect(PLATE_ARMOR).setTranslatedName("Blacksmith Learns Plate Armor Recipes"));

        //Sifter Mesh triggers
        effects.add(new ResearchEffect(SIFTER_STRING).setTranslatedName("Fletcher Learns How to Make String Meshes for the Sifter"));
        effects.add(new ResearchEffect(SIFTER_FLINT).setTranslatedName("Stonemason Learns How to Make Flint Meshes for the Sifter"));
        effects.add(new ResearchEffect(SIFTER_IRON).setTranslatedName("Blacksmith Learns How to Make Iron Meshes for the Sifter"));
        effects.add(new ResearchEffect(SIFTER_DIAMOND).setTranslatedName("Mechanic Learns How to Make Diamond Meshes for the Sifter"));

        return effects;
    }

    /**
     * Get a list of all researches. Conventions: group research by branch first.  Inside each branch, follow conventional English reading order: from left-to-right from a primary
     * research until its first descendants are complete, then to any splits from that subbranch, by sortOrder, then to the next primary research. Set local (final) variables for
     * any research with descendants, and reference that variable for parent and parent research level.
     *
     * @return a complete list of all research.
     */
    @Override
    public Collection<Research> getResearchCollection()
    {
        final List<Research> researches = new ArrayList<>();

        researches.addAll(getCivilResearch(researches));
        researches.addAll(getCombatResearch(researches));
        researches.addAll(getTechnologyResearch(researches));
        researches.addAll(getAchievementResearch(researches));

        return researches;
    }

    public Collection<Research> getCivilResearch(Collection<Research> r)
    {
        //Primary Research #1
        final Research stamina = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/stamina"), CIVIL).setTranslatedName("Stamina")
                                   .setOnlyChild()
                                   .setIcon(ModBlocks.blockHutHospital.asItem())
                                   .addItemCost(Items.CARROT, 1, provider)
                                   .addEffect(ModBuildings.hospital.get().getBuildingBlock(), 1)
                                   .addToList(r);
        final Research bandAid = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/bandaid"), CIVIL).setParentResearch(stamina)
                                   .setTranslatedName("Band Aid")
                                   .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/regeneration1.png"))
                                   .addBuildingRequirement(ModBuildings.LIBRARY_ID, 2)
                                   .addItemCost(Items.GOLDEN_CARROT, 1, provider)
                                   .addEffect(REGENERATION, 1)
                                   .addToList(r);
        final Research healingCream = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/healingcream"), CIVIL).setParentResearch(bandAid)
                                        .setTranslatedName("Healing Cream")
                                        .setTranslatedSubtitle("You missed a spot...")
                                        .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/regeneration2.png"))
                                        .addBuildingRequirement(ModBuildings.LIBRARY_ID, 3)
                                        .addItemCost(Items.GOLDEN_CARROT, 8, provider)
                                        .addEffect(REGENERATION, 2)
                                        .addToList(r);
        final Research bandages = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/bandages"), CIVIL).setParentResearch(healingCream)
                                    .setTranslatedName("Bandages")
                                    .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/regeneration3.png"))
                                    .addBuildingRequirement(ModBuildings.LIBRARY_ID, 4)
                                    .addItemCost(Items.GOLDEN_CARROT, 16, provider)
                                    .addEffect(REGENERATION, 3)
                                    .addToList(r);
        final Research compress = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/compress"), CIVIL).setParentResearch(bandages)
                                    .setTranslatedName("Compress")
                                    .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/regeneration4.png"))
                                    .addBuildingRequirement(ModBuildings.LIBRARY_ID, 5)
                                    .addItemCost(Items.GOLDEN_CARROT, 32, provider)
                                    .addEffect(REGENERATION, 4)
                                    .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/cast"), CIVIL).setParentResearch(compress)
          .setTranslatedName("Cast")
          .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/regeneration5.png"))
          .addItemCost(Items.GOLDEN_CARROT, 64, provider)
          .addEffect(REGENERATION, 5)
          .addToList(r);

        final Research resistance = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/resistance"), CIVIL).setParentResearch(stamina)
                                      .setTranslatedName("Resistance")
                                      .setSortOrder(2)
                                      .setIcon(Items.GLASS_BOTTLE)
                                      .addBuildingRequirement(ModBuildings.COOK_ID, 2)
                                      .addItemCost(Items.GOLDEN_APPLE, 1, provider)
                                      .addEffect(SATLIMIT, 1)
                                      .addToList(r);
        final Research resilience = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/resilience"), CIVIL).setParentResearch(resistance)
                                      .setTranslatedName("Resilience")
                                      .setIcon(Items.POTION)
                                      .addBuildingRequirement(ModBuildings.COOK_ID, 3)
                                      .addItemCost(Items.GOLDEN_APPLE, 8, provider)
                                      .addEffect(SATLIMIT, 2)
                                      .addToList(r);
        final Research vitality = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/vitality"), CIVIL).setParentResearch(resilience)
                                    .setTranslatedName("Vitality")
                                    .setIcon(Items.SPLASH_POTION)
                                    .addBuildingRequirement(ModBuildings.COOK_ID, 4)
                                    .addItemCost(Items.GOLDEN_APPLE, 16, provider)
                                    .addEffect(SATLIMIT, 3)
                                    .addToList(r);
        final Research fortitude = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/fortitude"), CIVIL).setParentResearch(vitality)
                                     .setTranslatedName("Fortitude")
                                     .setIcon(Items.HONEY_BOTTLE)
                                     .addBuildingRequirement(ModBuildings.COOK_ID, 5)
                                     .addItemCost(Items.GOLDEN_APPLE, 32, provider)
                                     .addEffect(SATLIMIT, 4)
                                     .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/indefatigability"), CIVIL).setParentResearch(fortitude)
          .setTranslatedName("Indefatigability")
          .setIcon(Items.EXPERIENCE_BOTTLE)
          .addItemCost(Items.GOLDEN_APPLE, 64, provider)
          .addEffect(SATLIMIT, 5)
          .addToList(r);

        //Primary Research #2
        final Research keen = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/keen"), CIVIL).setTranslatedName("Keen")
                                .setSortOrder(2)
                                .setIcon(ModBlocks.blockHutLibrary.asItem())
                                .addBuildingRequirement(ModBuildings.HOME_ID, 3)
                                .addItemCost(Items.BOOK, 3, provider)
                                .addEffect(ModBuildings.library.get().getBuildingBlock(), 1)
                                .addToList(r);
        final Research outpost = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/outpost"), CIVIL).setParentResearch(keen)
                                   .setTranslatedName("Outpost")
                                   .setIcon(ModBlocks.blockHutHome.asItem(), 50)
                                   .addBuildingRequirement(ModBuildings.HOME_ID, 4)
                                   .addItemCost(Items.COOKED_BEEF, 64, provider)
                                   .addEffect(CITIZEN_CAP, 2)
                                   .addToList(r);
        final Research hamlet = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/hamlet"), CIVIL).setParentResearch(outpost)
                                  .setTranslatedName("Hamlet")
                                  .setIcon(ModBlocks.blockHutHome.asItem(), 75)
                                  .addBuildingRequirement(ModBuildings.HOME_ID, 5)
                                  .addItemCost(Items.COOKED_BEEF, 128, provider)
                                  .addEffect(CITIZEN_CAP, 3)
                                  .addToList(r);
        final Research village = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/village"), CIVIL).setParentResearch(hamlet)
                                   .setTranslatedName("Village")
                                   .setIcon(ModBlocks.blockHutHome.asItem(), 100)
                                   .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 4)
                                   .addItemCost(Items.COOKED_BEEF, 256, provider)
                                   .addEffect(CITIZEN_CAP, 4)
                                   .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/city"), CIVIL).setParentResearch(village)
          .setTranslatedName("City")
          .setIcon(ModBlocks.blockHutHome.asItem(), 200)
          .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 5)
          .addItemCost(Items.COOKED_BEEF, 512, provider)
          .addEffect(CITIZEN_CAP, 5)
          .addToList(r);

        final Research diligent = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/diligent"), CIVIL).setParentResearch(keen)
                                    .setTranslatedName("Diligent")
                                    .setSortOrder(2)
                                    .setIcon(Items.EXPERIENCE_BOTTLE)
                                    .addBuildingRequirement(ModBuildings.LIBRARY_ID, 2)
                                    .addItemCost(Items.BOOK, 6, provider)
                                    .addEffect(LEVELING, 1)
                                    .addToList(r);
        final Research studious = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/studious"), CIVIL).setParentResearch(diligent)
                                    .setTranslatedName("Studious")
                                    .setIcon(Items.EXPERIENCE_BOTTLE, 2)
                                    .addBuildingRequirement(ModBuildings.LIBRARY_ID, 3)
                                    .addItemCost(Items.BOOK, 12, provider)
                                    .addEffect(LEVELING, 2)
                                    .addToList(r);
        final Research scholarly = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/scholarly"), CIVIL).setParentResearch(studious)
                                     .setTranslatedName("Scholarly")
                                     .setTranslatedSubtitle("Homework for the next decade... check!")
                                     .setIcon(Items.EXPERIENCE_BOTTLE, 3)
                                     .addBuildingRequirement(ModBuildings.LIBRARY_ID, 4)
                                     .addItemCost(Items.BOOK, 24, provider)
                                     .addEffect(LEVELING, 3)
                                     .addToList(r);
        final Research reflective = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/reflective"), CIVIL).setParentResearch(scholarly)
                                      .setTranslatedName("Reflective")
                                      .setTranslatedSubtitle("Let me think about that for a moment.")
                                      .setIcon(Items.EXPERIENCE_BOTTLE, 4)
                                      .addBuildingRequirement(ModBuildings.LIBRARY_ID, 5)
                                      .addItemCost(Items.BOOK, 48, provider)
                                      .addEffect(LEVELING, 4)
                                      .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/academic"), CIVIL).setParentResearch(reflective)
          .setTranslatedName("Academic")
          .setTranslatedSubtitle("Think about what you thought when you thought of what you will think now.")
          .setIcon(Items.EXPERIENCE_BOTTLE, 5)
          .addItemCost(Items.BOOK, 128, provider)
          .addEffect(LEVELING, 5)
          .addToList(r);

        final Research rails = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/rails"), CIVIL).setParentResearch(keen)
                                 .setTranslatedName("Rails")
                                 .setTranslatedSubtitle("Research is progressing right on track.")
                                 .setSortOrder(3)
                                 .setIcon(Items.DETECTOR_RAIL)
                                 .addBuildingRequirement(ModBuildings.DELIVERYMAN_ID, 3)
                                 .addItemCost(Items.RAIL, 64, provider)
                                 .addEffect(RAILS, 1)
                                 .addToList(r);
        final Research nimble = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/nimble"), CIVIL).setParentResearch(rails)
                                  .setTranslatedName("Nimble")
                                  .setTranslatedSubtitle("Not that we get time to exercise. It must be the morning commute.")
                                  .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/speed1.png"))
                                  .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 3)
                                  .addItemCost(Items.RABBIT_FOOT, 1, provider)
                                  .addEffect(WALKING, 1)
                                  .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/vines"), CIVIL).setParentResearch(keen)
                                 .setTranslatedName("Aaaiooooiooo")
                                 .setTranslatedSubtitle("Me Tarzan, you Jane.")
                                 .setSortOrder(4)
                                 .setIcon(Items.VINE)
                                 .addBuildingRequirement(ModBuildings.HOME_ID, 3)
                                 .addItemCost(Items.VINE, 64, provider)
                                 .addEffect(VINES, 1)
                                 .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/moq"), CIVIL).setParentResearch(rails)
                .setTranslatedName("Minimum Order Quantity")
                .setTranslatedSubtitle("Work smarter, not harder.")
                .setSortOrder(10)
                .setIcon(ModItems.clipboard)
                .addBuildingRequirement(ModBuildings.DELIVERYMAN_ID, 9)
                .addItemCost(ModItems.clipboard, 1, provider)
                .addItemCost(Items.BOOK, 16, provider)
                .addEffect(MIN_ORDER, 1)
                .addToList(r);
        final Research agile = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/agile"), CIVIL).setParentResearch(nimble)
                                 .setTranslatedName("Agile")
                                 .setTranslatedSubtitle("So this is how it feels to be young again...")
                                 .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/speed2.png"))
                                 .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 4)
                                 .addItemCost(Items.RABBIT_FOOT, 10, provider)
                                 .addEffect(WALKING, 2)
                                 .addToList(r);
        final Research swift = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/swift"), CIVIL).setParentResearch(agile)
                                 .setTranslatedName("Swift")
                                 .setTranslatedSubtitle("They'll never see me coming.")
                                 .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/speed3.png"))
                                 .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 5)
                                 .addItemCost(Items.RABBIT_FOOT, 32, provider)
                                 .addEffect(WALKING, 3)
                                 .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/athlete"), CIVIL).setParentResearch(swift)
          .setTranslatedName("Athlete")
          .setTranslatedSubtitle("Try thinking as fast as your feet now!")
          .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/speed4.png"))
          .addItemCost(Items.RABBIT_FOOT, 64, provider)
          .addEffect(WALKING, 4)
          .addToList(r);

        // Primary Research #3
        final Research firstAid = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/firstaid"), CIVIL).setTranslatedName("First Aid")
                                    .setTranslatedSubtitle("First aid, second hand.")
                                    .setSortOrder(3)
                                    .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/hp1.png"))
                                    .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 1)
                                    .addItemCost(Items.HAY_BLOCK, 8, provider)
                                    .addEffect(HEALTH_BOOST, 1)
                                    .addToList(r);
        final Research firstAid2 = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/firstaid2"), CIVIL).setParentResearch(firstAid)
                                     .setTranslatedName("First Aid II")
                                     .setTranslatedSubtitle("Second Aid?")
                                     .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/hp2.png"))
                                     .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 2)
                                     .addItemCost(Items.HAY_BLOCK, 16, provider)
                                     .addEffect(HEALTH_BOOST, 2)
                                     .addToList(r);
        final Research lifesaver = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/lifesaver"), CIVIL).setParentResearch(firstAid2)
                                     .setTranslatedName("Lifesaver")
                                     .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/hp3.png"))
                                     .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 3)
                                     .addItemCost(Items.HAY_BLOCK, 32, provider)
                                     .addEffect(HEALTH_BOOST, 3)
                                     .addToList(r);
        final Research lifesaver2 = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/lifesaver2"), CIVIL).setParentResearch(lifesaver)
                                      .setTranslatedName("Lifesaver II")
                                      .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/hp4.png"))
                                      .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 4)
                                      .addItemCost(Items.HAY_BLOCK, 64, provider)
                                      .addEffect(HEALTH_BOOST, 4)
                                      .addToList(r);
        final Research guardianAngel = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/guardianangel"), CIVIL).setParentResearch(lifesaver2)
                                         .setTranslatedName("Guardian Angel")
                                         .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/hp5.png"))
                                         .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 5)
                                         .addItemCost(Items.HAY_BLOCK, 128, provider)
                                         .addEffect(HEALTH_BOOST, 5)
                                         .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/guardianangel2"), CIVIL).setParentResearch(guardianAngel)
          .setTranslatedName("Guardian Angel II")
          .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/hp6.png"))
          .addItemCost(Items.HAY_BLOCK, 256, provider)
          .addEffect(HEALTH_BOOST, 6)
          .addToList(r);

        final Research masks = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/masks"), CIVIL).setParentResearch(firstAid2)
          .setTranslatedName("Masks")
          .setTranslatedSubtitle("Solidarity")
          .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/hp4.png"))
          .addBuildingRequirement(ModBuildings.LIBRARY_ID, 3)
          .addItemCost(Items.WHITE_WOOL, 32, provider)
          .addEffect(MASKS, 1)
          .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/vaccines"), CIVIL).setParentResearch(masks)
          .setTranslatedName("Vaccines")
          .setTranslatedSubtitle("Obvious Measures")
          .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/hp5.png"))
          .addBuildingRequirement(ModBuildings.HOSPITAL_ID, 3)
          .addItemCost(Items.EGG, 64, provider)
          .addEffect(VACCINES, 1)
          .addToList(r);

        final Research circus = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/circus"), CIVIL).setParentResearch(firstAid)
                                  .setTranslatedName("Circus")
                                  .setSortOrder(2)
                                  .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/happy1.png"))
                                  .addBuildingRequirement(ModBuildings.COOK_ID, 2)
                                  .addItemCost(Items.CAKE, 1, provider)
                                  .addEffect(HAPPINESS, 1)
                                  .addToList(r);
        final Research festival = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/festival"), CIVIL).setParentResearch(circus)
                                    .setTranslatedName("Festival")
                                    .setTranslatedSubtitle("We Researchers may not be there, so don't look for us.")
                                    .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/happy2.png"))
                                    .addBuildingRequirement(ModBuildings.COOK_ID, 3)
                                    .addItemCost(Items.CAKE, 9, provider)
                                    .addEffect(HAPPINESS, 2)
                                    .addToList(r);
        final Research spectacle = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/spectacle"), CIVIL).setParentResearch(festival)
                                     .setTranslatedName("Spectacle")
                                     .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/happy3.png"))
                                     .addBuildingRequirement(ModBuildings.COOK_ID, 4)
                                     .addItemCost(Items.CAKE, 18, provider)
                                     .addEffect(HAPPINESS, 3)
                                     .addToList(r);
        final Research opera = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/opera"), CIVIL).setParentResearch(spectacle)
                                 .setTranslatedName("Opera")
                                 .setTranslatedSubtitle("Ear plugs not included.")
                                 .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/happy4.png"))
                                 .addBuildingRequirement(ModBuildings.COOK_ID, 5)
                                 .addItemCost(Items.CAKE, 27, provider)
                                 .addEffect(HAPPINESS, 4)
                                 .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/theater"), CIVIL).setParentResearch(opera)
          .setTranslatedName("Theater")
          .setTranslatedSubtitle("Oh don't be so dramatic!")
          .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/happy5.png"))
          .addItemCost(Items.ENCHANTED_GOLDEN_APPLE, 16, provider)
          .addEffect(HAPPINESS, 5)
          .addToList(r);

        final Research night_owl = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/nightowl"), CIVIL).setParentResearch(circus)
          .setTranslatedName("Night Owl")
          .setTranslatedSubtitle("Overtime penalty rates need not apply.")
          .setSortOrder(2)
          .setIcon(Items.CLOCK, 1)
          .addBuildingRequirement(ModBuildings.LIBRARY_ID, 2)
          .addItemCost(Items.GOLDEN_CARROT, 25, provider)
          .addEffect(WORK_LONGER, 1)
          .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/nightowl2"), CIVIL).setParentResearch(night_owl)
          .setTranslatedName("Night Owl II")
          .setTranslatedSubtitle("Got any coffee?")
          .setSortOrder(2)
          .setIcon(Items.CLOCK, 2)
          .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 3)
          .addItemCost(Items.GOLDEN_CARROT, 75, provider)
          .addEffect(WORK_LONGER, 2)
          .addToList(r);

        final Research gourmand = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/gourmand"), CIVIL).setParentResearch(firstAid)
                                    .setTranslatedName("Gourmand")
                                    .setSortOrder(3)
                                    .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/saturation1.png"))
                                    .addBuildingRequirement(ModBuildings.COOK_ID, 2)
                                    .addItemCost(Items.COOKIE, 32, provider)
                                    .addEffect(SATURATION, 1)
                                    .addToList(r);
        final Research gorger = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/gorger"), CIVIL).setParentResearch(gourmand)
                                  .setTranslatedName("Gorger")
                                  .setTranslatedSubtitle("MORE!???")
                                  .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/saturation2.png"))
                                  .addBuildingRequirement(ModBuildings.COOK_ID, 3)
                                  .addItemCost(Items.COOKIE, 64, provider)
                                  .addEffect(SATURATION, 2)
                                  .addToList(r);
        final Research stuffer = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/stuffer"), CIVIL).setParentResearch(gorger)
                                   .setTranslatedName("Stuffer")
                                   .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/saturation3.png"))
                                   .addBuildingRequirement(ModBuildings.COOK_ID, 4)
                                   .addItemCost(Items.COOKIE, 128, provider)
                                   .addEffect(SATURATION, 3)
                                   .addToList(r);
        final Research epicure = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/epicure"), CIVIL).setParentResearch(stuffer)
                                   .setTranslatedName("Epicure")
                                   .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/saturation4.png"))
                                   .addBuildingRequirement(ModBuildings.COOK_ID, 5)
                                   .addItemCost(Items.COOKIE, 256, provider)
                                   .addEffect(SATURATION, 4)
                                   .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/glutton"), CIVIL).setParentResearch(epicure)
          .setTranslatedName("Glutton")
          .setTranslatedSubtitle("I think I'm finally satisfied... so what's for next course?")
          .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/saturation5.png"))
          .addItemCost(Items.COOKIE, 512, provider)
          .addEffect(SATURATION, 5)
          .addToList(r);

        // Primary Research #4.
        final Research higherLearning = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/higherlearning"), CIVIL).setTranslatedName("Higher Learning")
                                          .setTranslatedSubtitle("Well, we had to find something to occupy them.")
                                          .setSortOrder(4)
                                          .setOnlyChild()
                                          .setIcon(ModBlocks.blockHutSchool.asItem())
                                          .addBuildingRequirement("residence", 3)
                                          .addItemCost(Items.BOOK, 3, provider)
                                          .addEffect(ModBuildings.school.get().getBuildingBlock(), 1)
                                          .addToList(r);
        final Research moreBooks = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/morebooks"), CIVIL).setParentResearch(higherLearning)
                                     .setTranslatedName("More Books")
                                     .setTranslatedSubtitle("Of course I'm right, I read it in a book!")
                                     .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/xpgain1.png"))
                                     .addBuildingRequirement(ModBuildings.SCHOOL_ID, 1)
                                     .addItemCost(Items.BOOK, 6, provider)
                                     .addEffect(TEACHING, 1)
                                     .addToList(r);
        final Research bookworm = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/bookworm"), CIVIL).setParentResearch(moreBooks)
                                    .setTranslatedName("Bookworm")
                                    .setTranslatedSubtitle("We all know the early bird gets the book!")
                                    .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/xpgain2.png"))
                                    .addBuildingRequirement(ModBuildings.SCHOOL_ID, 3)
                                    .addItemCost(Items.BOOKSHELF, 6, provider)
                                    .addEffect(TEACHING, 2)
                                    .addToList(r);
        final Research bachelor = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/bachelor"), CIVIL).setParentResearch(bookworm)
                                    .setTranslatedName("Bachelor")
                                    .setTranslatedSubtitle("They now look like they know a lot, whether they do or not.")
                                    .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/xpgain3.png"))
                                    .addBuildingRequirement(ModBuildings.LIBRARY_ID, 3)
                                    .addItemCost(Items.BOOKSHELF, 12, provider)
                                    .addEffect(TEACHING, 3)
                                    .addToList(r);
        final Research master = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/master"), CIVIL).setParentResearch(bachelor)
                                  .setTranslatedName("Master")
                                  .setTranslatedSubtitle("At least they get a fancy title this time.")
                                  .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/xpgain4.png"))
                                  .addBuildingRequirement(ModBuildings.LIBRARY_ID, 5)
                                  .addItemCost(Items.BOOKSHELF, 32, provider)
                                  .addEffect(TEACHING, 4)
                                  .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/phd"), CIVIL).setParentResearch(master)
          .setTranslatedName("PhD")
          .setTranslatedSubtitle("Not that sort of doctor.")
          .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/xpgain5.png"))
          .addItemCost(Items.BOOKSHELF, 64, provider)
          .addEffect(TEACHING, 5)
          .addToList(r);

        final Research nurture = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/nurture"), CIVIL).setParentResearch(higherLearning)
                                   .setTranslatedName("Nurture")
                                   .setTranslatedSubtitle("It's just part of our nature now.")
                                   .setSortOrder(2)
                                   .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/childgrowth1.png"))
                                   .addBuildingRequirement(ModBuildings.SCHOOL_ID, 1)
                                   .addItemCost(Items.COOKED_CHICKEN, 32, provider)
                                   .addEffect(GROWTH, 1)
                                   .addToList(r);
        final Research hormones = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/hormones"), CIVIL).setParentResearch(nurture)
                                    .setTranslatedName("Hormones")
                                    .setTranslatedSubtitle("These are safe, right?")
                                    .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/childgrowth2.png"))
                                    .addBuildingRequirement(ModBuildings.SCHOOL_ID, 3)
                                    .addItemCost(Items.COOKED_CHICKEN, 64, provider)
                                    .addEffect(GROWTH, 2)
                                    .addToList(r);
        final Research puberty = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/puberty"), CIVIL).setParentResearch(hormones)
                                   .setTranslatedName("Puberty")
                                   .setTranslatedSubtitle("My voice sounds weird...")
                                   .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/childgrowth3.png"))
                                   .addBuildingRequirement(ModBuildings.LIBRARY_ID, 3)
                                   .addItemCost(Items.COOKED_CHICKEN, 128, provider)
                                   .addEffect(GROWTH, 3)
                                   .addToList(r);
        final Research growth = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/growth"), CIVIL).setParentResearch(puberty)
                                  .setTranslatedName("Growth")
                                  .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/childgrowth4.png"))
                                  .addBuildingRequirement(ModBuildings.LIBRARY_ID, 5)
                                  .addItemCost(Items.COOKED_CHICKEN, 256, provider)
                                  .addEffect(GROWTH, 4)
                                  .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/beanstalk"), CIVIL).setParentResearch(growth)
          .setTranslatedName("Beanstalk")
          .setTranslatedSubtitle("That's one heck of a growth spurt!")
          .setIcon(new ResourceLocation("minecolonies", "textures/icons/research/childgrowth5.png"))
          .addItemCost(Items.COOKED_CHICKEN, 512, provider)
          .addEffect(GROWTH, 5)
          .addToList(r);

        // Primary Research #5
        final Research ambition = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/ambition"), CIVIL).setTranslatedName("Ambition")
          .setSortOrder(5)
          .setIcon(ModBlocks.blockHutMysticalSite.asItem())
          .addItemCost(Items.DIAMOND, 1, provider)
          .addEffect(ModBuildings.mysticalSite.get().getBuildingBlock(), 1)
          .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/air"), CIVIL).setTranslatedName("Scuba")
          .setParentResearch(ambition)
          .setSortOrder(1)
          .setIcon(Items.POTION)
          .addItemCost(Items.HEART_OF_THE_SEA, 1, provider)
          .addEffect(MORE_AIR, 1)
          .addToList(r);

        // Primary Research #6
        final Research remembrance = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/remembrance"), CIVIL)
                .setTranslatedName("Remembrance")
                .setSortOrder(6)
                .setIcon(ModBlocks.blockHutGraveyard.asItem())
                .addEffect(ModBuildings.graveyard.get().getBuildingBlock(), 1)
                .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 2)
                .addItemCost(Items.BONE, 8, provider)
                .setTranslatedSubtitle("Our fallen shall not be forgotten!")
                .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/undertakeremergency"), CIVIL)
                .setParentResearch(remembrance)
                .setTranslatedName("Undertaker Emergency")
                .setSortOrder(3)
                .setIcon(ModBlocks.blockHutGraveyard.asItem())
                .addBuildingRequirement(ModBuildings.GRAVEYARD_ID, 2)
                .addItemCost(Items.IRON_BOOTS, 1, provider)
                .addEffect(UNDERTAKER_RUN, 1)
                .addToList(r);

        final Research resurrectChance1 = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/resurrectchance1"), CIVIL)
                .setParentResearch(remembrance)
                .setTranslatedName("Resurrection Chance I")
                .setTranslatedSubtitle("Use the right words")
                .setSortOrder(1)
                .setIcon(ModBlocks.blockHutGraveyard.asItem())
                .addBuildingRequirement(ModBuildings.GRAVEYARD_ID, 3)
                .addItemCost(Items.GHAST_TEAR, 1, provider)
                .addEffect(RESURRECT_CHANCE, 1)
                .addToList(r);

        final Research resurrectChance2 = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/resurrectchance2"), CIVIL)
                .setParentResearch(resurrectChance1)
                .setTranslatedName("Resurrection Chance II")
                .setTranslatedSubtitle("Dance around and wave your hands")
                .setSortOrder(1)
                .setIcon(ModBlocks.blockHutGraveyard.asItem())
                .addBuildingRequirement(ModBuildings.GRAVEYARD_ID, 5)
                .addItemCost(Items.CHORUS_FRUIT, 16, provider)
                .addEffect(RESURRECT_CHANCE, 2)
                .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/raisingthedead"), CIVIL)
                .setParentResearch(resurrectChance2)
                .setTranslatedName("Raising The Dead")
                .setTranslatedSubtitle("Magic totems are for more than just looks")
                .setSortOrder(1)
                .setIcon(Items.TOTEM_OF_UNDYING.asItem())
                .addBuildingRequirement(ModBuildings.GRAVEYARD_ID, 5)
                .addItemCost(Items.TOTEM_OF_UNDYING, 1, provider)
                .addEffect(USE_TOTEM, 1)
                .addToList(r);

        final Research decayBonus1 = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/gravedecaybonus1"), CIVIL)
                .setParentResearch(remembrance)
                .setTranslatedName("Grave Decay I")
                .setTranslatedSubtitle("Dig deeper before death")
                .setSortOrder(2)
                .setIcon(ModBlocks.blockGrave.asItem())
                .addBuildingRequirement(ModBuildings.GRAVEYARD_ID, 3)
                .addItemCost(Items.ROTTEN_FLESH, 64, provider)
                .addEffect(GRAVE_DECAY_BONUS, 1)
                .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/gravedecaybonus2"), CIVIL)
                .setParentResearch(decayBonus1)
                .setTranslatedName("Grave Decay II")
                .setTranslatedSubtitle("I don't want to leave yet!")
                .setSortOrder(2)
                .setIcon(ModBlocks.blockGrave.asItem())
                .addBuildingRequirement(ModBuildings.GRAVEYARD_ID, 5)
                .addItemCost(Items.NETHER_WART_BLOCK, 8, provider)
                .addEffect(GRAVE_DECAY_BONUS, 2)
                .addToList(r);

        return r;
    }

    public Collection<Research> getCombatResearch(Collection<Research> r)
    {
        // Primary Research # 1
        final Research accuracy = new Research(new ResourceLocation(Constants.MOD_ID, "combat/accuracy"), COMBAT).setTranslatedName("Accuracy")
                                    .setOnlyChild()
                                    .setIcon(Items.IRON_SWORD)
                                    .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 1)
                                    .addItemCost(Items.IRON_INGOT, 16, provider)
                                    .addEffect(GUARD_CRIT, 1)
                                    .addToList(r);
        final Research quickDraw = new Research(new ResourceLocation(Constants.MOD_ID, "combat/quickdraw"), COMBAT).setParentResearch(accuracy)
                                     .setTranslatedName("Quick Draw")
                                     .setIcon(Items.WOODEN_SWORD)
                                     .addBuildingRequirement(ModBuildings.BARRACKS_ID, 3)
                                     .addItemCost(Items.IRON_BLOCK, 2, provider)
                                     .addEffect(MELEE_DAMAGE, 1)
                                     .addToList(r);
        final Research powerAttack = new Research(new ResourceLocation(Constants.MOD_ID, "combat/powerattack"), COMBAT).setParentResearch(quickDraw)
                                       .setTranslatedName("Power Attack")
                                       .setIcon(Items.STONE_SWORD)
                                       .addBuildingRequirement(ModBuildings.COMBAT_ACADEMY_ID, 3)
                                       .addItemCost(Items.IRON_BLOCK, 4, provider)
                                       .addEffect(MELEE_DAMAGE, 2)
                                       .addToList(r);
        final Research cleave = new Research(new ResourceLocation(Constants.MOD_ID, "combat/cleave"), COMBAT).setParentResearch(powerAttack)
                                  .setTranslatedName("Cleave")
                                  .setIcon(Items.IRON_SWORD)
                                  .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 10)
                                  .addItemCost(Items.IRON_BLOCK, 8, provider)
                                  .addEffect(MELEE_DAMAGE, 3)
                                  .addToList(r);
        final Research mightyCleave = new Research(new ResourceLocation(Constants.MOD_ID, "combat/mightycleave"), COMBAT).setParentResearch(cleave)
                                        .setTranslatedName("Mighty Cleave")
                                        .setIcon(Items.GOLDEN_SWORD)
                                        .addBuildingRequirement(ModBuildings.BARRACKS_ID, 5)
                                        .addItemCost(Items.IRON_BLOCK, 16, provider)
                                        .addEffect(MELEE_DAMAGE, 4)
                                        .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/savagestrike"), COMBAT).setParentResearch(mightyCleave)
          .setTranslatedName("Savage Strike")
          .setIcon(Items.DIAMOND_SWORD)
          .addItemCost(Items.IRON_BLOCK, 32, provider)
          .addEffect(MELEE_DAMAGE, 5)
          .addToList(r);

        final Research preciseShot = new Research(new ResourceLocation(Constants.MOD_ID, "combat/preciseshot"), COMBAT).setParentResearch(accuracy)
                                       .setTranslatedName("Precise Shot")
                                       .setSortOrder(2)
                                       .setIcon(Items.ARROW)
                                       .addBuildingRequirement(ModBuildings.BARRACKS_ID, 3)
                                       .addItemCost(Items.FLINT, 16, provider)
                                       .addEffect(ARCHER_DAMAGE, 1)
                                       .addToList(r);
        final Research penetratingShot = new Research(new ResourceLocation(Constants.MOD_ID, "combat/penetratingshot"), COMBAT).setParentResearch(preciseShot)
                                           .setTranslatedName("Penetrating Shot")
                                           .setIcon(Items.BOW)
                                           .addBuildingRequirement(ModBuildings.ARCHERY_ID, 3)
                                           .addItemCost(Items.FLINT, 32, provider)
                                           .addEffect(ARCHER_DAMAGE, 2)
                                           .addToList(r);
        final Research piercingShot = new Research(new ResourceLocation(Constants.MOD_ID, "combat/piercingshot"), COMBAT).setParentResearch(penetratingShot)
                                        .setTranslatedName("Piercing Shot")
                                        .setIcon(Items.CROSSBOW)
                                        .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 10)
                                        .addItemCost(Items.FLINT, 64, provider)
                                        .addEffect(ARCHER_DAMAGE, 3)
                                        .addToList(r);
        final Research woundingShot = new Research(new ResourceLocation(Constants.MOD_ID, "combat/woundingshot"), COMBAT).setParentResearch(piercingShot)
                                        .setTranslatedName("Wounding Shot")
                                        .setIcon(ModItems.firearrow)
                                        .addBuildingRequirement(ModBuildings.BARRACKS_ID, 5)
                                        .addItemCost(Items.FLINT, 128, provider)
                                        .addEffect(ARCHER_DAMAGE, 4)
                                        .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/deadlyaim"), COMBAT).setParentResearch(woundingShot)
          .setTranslatedName("Deadly Aim")
          .setTranslatedSubtitle("Just don't aim at me!")
          .setIcon(Items.TIPPED_ARROW)
          .addItemCost(Items.FLINT, 256, provider)
          .addEffect(ARCHER_DAMAGE, 5)
          .addToList(r);

        // Primary Research #2
        final Research tacticTraining = new Research(new ResourceLocation(Constants.MOD_ID, "combat/tactictraining"), COMBAT).setTranslatedName("Tactic Training")
                                          .setSortOrder(2)
                                          .setIcon(ModBlocks.blockHutBarracksTower.asItem())
                                          .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 3)
                                          .addItemCost(Items.IRON_BLOCK, 3, provider)
                                          .addEffect(ModBuildings.barracks.get().getBuildingBlock(), 1)
                                          .addToList(r);
        final Research improvedSwords = new Research(new ResourceLocation(Constants.MOD_ID, "combat/improvedswords"), COMBAT).setParentResearch(tacticTraining)
                                          .setTranslatedName("Improved Swords")
                                          .setTranslatedSubtitle("Pointy end goes into the zombie.")
                                          .setIcon(ModBlocks.blockHutCombatAcademy.asItem())
                                          .addBuildingRequirement(ModBuildings.BARRACKS_ID, 3)
                                          .addItemCost(Items.IRON_BLOCK, 6, provider)
                                          .addEffect(ModBuildings.combatAcademy.get().getBuildingBlock(), 1)
                                          .addToList(r);
        final Research squireTraining = new Research(new ResourceLocation(Constants.MOD_ID, "combat/squiretraining"), COMBAT).setParentResearch(improvedSwords)
                                          .setTranslatedName("Squire Training")
                                          .setTranslatedSubtitle("First lesson: how to mop the floors.")
                                          .setIcon(Items.IRON_INGOT)
                                          .addBuildingRequirement(ModBuildings.COMBAT_ACADEMY_ID, 3)
                                          .addItemCost(Items.SHIELD, 4, provider)
                                          .addEffect(BLOCK_ATTACKS, 1)
                                          .addToList(r);
        final Research knightTraining = new Research(new ResourceLocation(Constants.MOD_ID, "combat/knighttraining"), COMBAT).setParentResearch(squireTraining)
                                          .setTranslatedName("Knight Training")
                                          .setIcon(Items.BARREL)
                                          .addBuildingRequirement(ModBuildings.COMBAT_ACADEMY_ID, 4)
                                          .addItemCost(Items.SHIELD, 8, provider)
                                          .addEffect(BLOCK_ATTACKS, 2)
                                          .addToList(r);
        final Research captainTraining = new Research(new ResourceLocation(Constants.MOD_ID, "combat/captaintraining"), COMBAT).setParentResearch(knightTraining)
                                           .setTranslatedName("Captain Training")
                                           .setIcon(Items.IRON_BARS)
                                           .addBuildingRequirement(ModBuildings.COMBAT_ACADEMY_ID, 5)
                                           .addItemCost(Items.SHIELD, 16, provider)
                                           .addEffect(BLOCK_ATTACKS, 3)
                                           .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/captainoftheguard"), COMBAT).setParentResearch(captainTraining)
          .setTranslatedName("Captain of the Guard")
          .setIcon(Items.IRON_BLOCK)
          .addItemCost(Items.SHIELD, 27, provider)
          .addEffect(BLOCK_ATTACKS, 4)
          .addToList(r);

        final Research improvedBows = new Research(new ResourceLocation(Constants.MOD_ID, "combat/improvedbows"), COMBAT).setParentResearch(tacticTraining)
                                        .setTranslatedName("Improved Bows")
                                        .setTranslatedSubtitle("How far back can this bend before snapping?")
                                        .setSortOrder(2)
                                        .setIcon(ModBlocks.blockHutArchery.asItem())
                                        .addBuildingRequirement(ModBuildings.BARRACKS_ID, 3)
                                        .addItemCost(Items.IRON_BLOCK, 6, provider)
                                        .addEffect(ModBuildings.archery.get().getBuildingBlock(), 1)
                                        .addToList(r);
        final Research trickShot = new Research(new ResourceLocation(Constants.MOD_ID, "combat/trickshot"), COMBAT).setParentResearch(improvedBows)
                                     .setTranslatedName("Trick Shot")
                                     .setIcon(Items.ARROW)
                                     .addBuildingRequirement(ModBuildings.ARCHERY_ID, 3)
                                     .addItemCost(Items.BOW, 5, provider)
                                     .addEffect(DOUBLE_ARROWS, 1)
                                     .addToList(r);
        final Research multiShot = new Research(new ResourceLocation(Constants.MOD_ID, "combat/multishot"), COMBAT).setParentResearch(trickShot)
                                     .setTranslatedName("Multishot")
                                     .setTranslatedSubtitle("Known side effects include double vision double vision.")
                                     .setIcon(Items.TIPPED_ARROW)
                                     .addBuildingRequirement(ModBuildings.ARCHERY_ID, 4)
                                     .addItemCost(Items.BOW, 9, provider)
                                     .addEffect(DOUBLE_ARROWS, 2)
                                     .addToList(r);
        final Research rapidShot = new Research(new ResourceLocation(Constants.MOD_ID, "combat/rapidshot"), COMBAT).setParentResearch(multiShot)
                                     .setTranslatedName("Rapid Shot")
                                     .setTranslatedSubtitle("Please leave the bow with more than you brought in.")
                                     .setIcon(ModItems.firearrow)
                                     .addBuildingRequirement(ModBuildings.ARCHERY_ID, 5)
                                     .addItemCost(Items.BOW, 18, provider)
                                     .addEffect(DOUBLE_ARROWS, 3)
                                     .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/masterbowman"), COMBAT).setParentResearch(rapidShot)
          .setTranslatedName("Master Bowman")
          .setIcon(Items.BLAZE_ROD)
          .addItemCost(Items.BOW, 27, provider)
          .addEffect(DOUBLE_ARROWS, 4)
          .addToList(r);

        final Research coffee = new Research(new ResourceLocation(Constants.MOD_ID, "combat/coffee"), COMBAT).setParentResearch(tacticTraining)
                                  .setTranslatedName("Coffee")
                                  .setTranslatedSubtitle("Keeps guards awake")
                                  .setIcon(Items.LIME_BED)
                                  .addItemCost(Items.GOLDEN_CARROT, 4, provider)
                                  .addEffect(SLEEP_LESS, 1)
                                  .addToList(r);

        // Primary Research #3
        final Research avoidance = new Research(new ResourceLocation(Constants.MOD_ID, "combat/avoidance"), COMBAT).setTranslatedName("Avoidance")
                                     .setSortOrder(3)
                                     .setOnlyChild()
                                     .setIcon(Items.SHIELD)
                                     .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 3)
                                     .addItemCost(Items.IRON_BLOCK, 3, provider)
                                     .addEffect(SHIELD_USAGE, 1)
                                     .addToList(r);
        final Research parry = new Research(new ResourceLocation(Constants.MOD_ID, "combat/parry"), COMBAT).setParentResearch(avoidance)
                                 .setTranslatedName("Parry")
                                 .setIcon(Items.LEATHER_CHESTPLATE)
                                 .addBuildingRequirement(ModBuildings.SMELTERY_ID, 1)
                                 .addItemCost(Items.IRON_INGOT, 16, provider)
                                 .addEffect(MELEE_ARMOR, 1)
                                 .addToList(r);
        final Research riposte = new Research(new ResourceLocation(Constants.MOD_ID, "combat/riposte"), COMBAT).setParentResearch(parry)
                                   .setTranslatedName("Riposte")
                                   .setTranslatedSubtitle("Oh yeah? Well, I, uh, um...")
                                   .setIcon(Items.IRON_CHESTPLATE)
                                   .addBuildingRequirement(ModBuildings.COMBAT_ACADEMY_ID, 1)
                                   .addItemCost(Items.IRON_INGOT, 32, provider)
                                   .addEffect(MELEE_ARMOR, 2)
                                   .addToList(r);
        final Research duelist = new Research(new ResourceLocation(Constants.MOD_ID, "combat/duelist"), COMBAT).setParentResearch(riposte)
                                   .setTranslatedName("Duelist")
                                   .setIcon(Items.CHAINMAIL_CHESTPLATE)
                                   .addBuildingRequirement(ModBuildings.SMELTERY_ID, 3)
                                   .addItemCost(Items.IRON_INGOT, 64, provider)
                                   .addEffect(MELEE_ARMOR, 3)
                                   .addToList(r);
        final Research provost = new Research(new ResourceLocation(Constants.MOD_ID, "combat/provost"), COMBAT).setParentResearch(duelist)
                                   .setTranslatedName("Provost")
                                   .setIcon(Items.GOLDEN_CHESTPLATE)
                                   .addBuildingRequirement(ModBuildings.COMBAT_ACADEMY_ID, 5)
                                   .addItemCost(Items.DIAMOND, 16, provider)
                                   .addEffect(MELEE_ARMOR, 4)
                                   .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/masterswordsman"), COMBAT).setParentResearch(provost)
          .setTranslatedName("Master Swordsman")
          .setIcon(Items.DIAMOND_CHESTPLATE)
          .addItemCost(Items.DIAMOND, 64, provider)
          .addEffect(MELEE_ARMOR, 5)
          .addToList(r);

        final Research dodge = new Research(new ResourceLocation(Constants.MOD_ID, "combat/dodge"), COMBAT).setParentResearch(avoidance)
                                 .setTranslatedName("Dodge")
                                 .setTranslatedSubtitle("If you can dodge a hammer...")
                                 .setSortOrder(2)
                                 .setIcon(ModItems.pirateBoots_2.asItem())
                                 .addBuildingRequirement(ModBuildings.SMELTERY_ID, 1)
                                 .addItemCost(Items.LEATHER, 16, provider)
                                 .addEffect(ARCHER_ARMOR, 1)
                                 .addToList(r);
        final Research improvedDodge = new Research(new ResourceLocation(Constants.MOD_ID, "combat/improveddodge"), COMBAT).setParentResearch(dodge)
                                         .setTranslatedName("Improved Dodge")
                                         .setTranslatedSubtitle("Dip")
                                         .setIcon(ModItems.pirateBoots_1)
                                         .addBuildingRequirement(ModBuildings.ARCHERY_ID, 1)
                                         .addItemCost(Items.LEATHER, 32, provider)
                                         .addEffect(ARCHER_ARMOR, 2)
                                         .addToList(r);
        final Research evasion = new Research(new ResourceLocation(Constants.MOD_ID, "combat/evasion"), COMBAT).setParentResearch(improvedDodge)
                                   .setTranslatedName("Evasion")
                                   .setTranslatedSubtitle("\"Duck!\" \"No, that's a goose.\"")
                                   .setIcon(Items.IRON_BOOTS)
                                   .addBuildingRequirement(ModBuildings.SMELTERY_ID, 3)
                                   .addItemCost(Items.LEATHER, 64, provider)
                                   .addEffect(ARCHER_ARMOR, 3)
                                   .addToList(r);
        final Research improvedEvasion = new Research(new ResourceLocation(Constants.MOD_ID, "combat/improvedevasion"), COMBAT).setParentResearch(evasion)
                                           .setTranslatedName("Improved Evasion")
                                           .setTranslatedSubtitle("Dive")
                                           .setIcon(Items.GOLDEN_BOOTS)
                                           .addBuildingRequirement(ModBuildings.ARCHERY_ID, 5)
                                           .addItemCost(Items.DIAMOND, 16, provider)
                                           .addEffect(ARCHER_ARMOR, 4)
                                           .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/agilearcher"), COMBAT).setParentResearch(improvedEvasion)
          .setTranslatedName("Agile Archer")
          .setTranslatedSubtitle("Dodge... Again!")
          .setIcon(Items.DIAMOND_BOOTS)
          .addItemCost(Items.DIAMOND, 64, provider)
          .addEffect(ARCHER_ARMOR, 5)
          .addToList(r);

        // Primary Research #4
        final Research improvedLeather = new Research(new ResourceLocation(Constants.MOD_ID, "combat/improvedleather"), COMBAT).setTranslatedName("Improved Leather")
                                           .setTranslatedSubtitle("Becoming more like the real thing every day.")
                                           .setSortOrder(4)
                                           .setIcon(Items.LEATHER_HELMET)
                                           .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 1)
                                           .addItemCost(Items.LEATHER, 32, provider)
                                           .addEffect(ARMOR_DURABILITY, 1)
                                           .addToList(r);
        final Research boiledLeather = new Research(new ResourceLocation(Constants.MOD_ID, "combat/boiledleather"), COMBAT).setParentResearch(improvedLeather)
                                         .setTranslatedName("Boiled Leather")
                                         .setTranslatedSubtitle("Extra leathery!")
                                         .setIcon(Items.TURTLE_HELMET)
                                         .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 2)
                                         .addItemCost(Items.LEATHER, 64, provider)
                                         .addEffect(ARMOR_DURABILITY, 2)
                                         .addToList(r);
        final Research ironSkin = new Research(new ResourceLocation(Constants.MOD_ID, "combat/ironskin"), COMBAT).setParentResearch(boiledLeather)
                                    .setTranslatedName("Iron Skin")
                                    .setIcon(Items.CHAINMAIL_HELMET)
                                    .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 3)
                                    .addItemCost(Items.IRON_INGOT, 16, provider)
                                    .addEffect(ARMOR_DURABILITY, 3)
                                    .addToList(r);
        final Research ironArmor = new Research(new ResourceLocation(Constants.MOD_ID, "combat/ironarmor"), COMBAT).setParentResearch(ironSkin)
                                     .setTranslatedName("Iron Armor")
                                     .setIcon(Items.IRON_HELMET)
                                     .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 4)
                                     .addItemCost(Items.IRON_INGOT, 32, provider)
                                     .addEffect(ARMOR_DURABILITY, 4)
                                     .addToList(r);
        final Research steelArmor = new Research(new ResourceLocation(Constants.MOD_ID, "combat/steelarmor"), COMBAT).setParentResearch(ironArmor)
                                      .setTranslatedName("Steel Armor")
                                      .setSortOrder(1)
                                      .setIcon(Items.GOLDEN_HELMET)
                                      .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 5)
                                      .addItemCost(Items.IRON_INGOT, 64, provider)
                                      .addEffect(ARMOR_DURABILITY, 5)
                                      .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/platearmor"), COMBAT).setParentResearch(ironArmor)
                                     .setTranslatedName("Plate Armor")
                                     .setSortOrder(2)
                                     .setIcon(ModItems.plateArmorHelmet)
                                     .addMandatoryBuildingRequirement(ModBuildings.BLACKSMITH_ID, 4)
                                     .addItemCost(Items.IRON_INGOT, 32, provider)
                                     .addEffect(PLATE_ARMOR, 1)
                                     .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/diamondskin"), COMBAT).setParentResearch(steelArmor)
          .setTranslatedName("Diamond Skin")
          .setIcon(Items.DIAMOND_HELMET)
          .addItemCost(Items.DIAMOND, 64, provider)
          .addEffect(ARMOR_DURABILITY, 6)
          .addToList(r);

        final Research telescope = new Research(new ResourceLocation(Constants.MOD_ID, "combat/telescope"), COMBAT).setParentResearch(ironSkin)
          .setTranslatedName("Telescope")
          .setIcon(ModItems.bannerRallyGuards)
          .addBuildingRequirement(ModBuildings.BARRACKS_ID, 3)
          .addItemCost(Items.EMERALD, 16, provider)
          .addEffect(TELESCOPE, 1)
          .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "combat/standard"), COMBAT).setParentResearch(telescope)
          .setTranslatedName("Standard")
          .setIcon(ModItems.bannerRallyGuards)
          .addBuildingRequirement(ModBuildings.BARRACKS_ID, 4)
          .addItemCost(Items.EMERALD, 32, provider)
          .addEffect(STANDARD, 1)
          .addToList(r);

        final Research regeneration = new Research(new ResourceLocation(Constants.MOD_ID, "combat/regeneration"), COMBAT).setParentResearch(improvedLeather)
                                        .setTranslatedName("Regeneration")
                                        .setSortOrder(2)
                                        .setOnlyChild()
                                        .setIcon(Items.GLISTERING_MELON_SLICE)
                                        .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 2)
                                        .addItemCost(Items.EMERALD, 1, provider)
                                        .addEffect(RETREAT, 1)
                                        .addToList(r);
        final Research feint = new Research(new ResourceLocation(Constants.MOD_ID, "combat/feint"), COMBAT).setParentResearch(regeneration)
                                 .setTranslatedName("Feint")
                                 .setIcon(Items.LEATHER_BOOTS)
                                 .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 4)
                                 .addItemCost(Items.EMERALD, 8, provider)
                                 .addEffect(FLEEING_DAMAGE, 1)
                                 .addToList(r);
        final Research fear = new Research(new ResourceLocation(Constants.MOD_ID, "combat/fear"), COMBAT).setParentResearch(feint)
                                .setTranslatedName("Fear")
                                .setIcon(Items.IRON_BOOTS)
                                .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 8)
                                .addItemCost(Items.EMERALD, 16, provider)
                                .addEffect(FLEEING_DAMAGE, 2)
                                .addToList(r);
        final Research retreat = new Research(new ResourceLocation(Constants.MOD_ID, "combat/retreat"), COMBAT).setParentResearch(fear)
                                   .setTranslatedName("Retreat")
                                   .setTranslatedSubtitle("For strategic purposes, I assure you.")
                                   .setIcon(Items.GOLDEN_BOOTS)
                                   .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 12)
                                   .addItemCost(Items.EMERALD, 32, provider)
                                   .addEffect(FLEEING_DAMAGE, 3)
                                   .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/fullretreat"), COMBAT).setParentResearch(retreat)
          .setTranslatedName("Full Retreat")
          .setIcon(Items.DIAMOND_BOOTS)
          .addItemCost(Items.EMERALD, 64, provider)
          .addEffect(FLEEING_DAMAGE, 4)
          .addToList(r);

        final Research avoid = new Research(new ResourceLocation(Constants.MOD_ID, "combat/avoid"), COMBAT).setParentResearch(regeneration)
                                 .setTranslatedName("Avoid")
                                 .setSortOrder(2)
                                 .setIcon(Items.FEATHER)
                                 .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 4)
                                 .addItemCost(Items.EMERALD, 8, provider)
                                 .addEffect(FLEEING_SPEED, 1)
                                 .addToList(r);
        final Research evade = new Research(new ResourceLocation(Constants.MOD_ID, "combat/evade"), COMBAT).setParentResearch(avoid)
                                 .setTranslatedName("Evade")
                                 .setIcon(Items.FEATHER, 2)
                                 .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 8)
                                 .addItemCost(Items.EMERALD, 16, provider)
                                 .addEffect(FLEEING_SPEED, 2)
                                 .addToList(r);
        final Research flee = new Research(new ResourceLocation(Constants.MOD_ID, "combat/flee"), COMBAT).setParentResearch(evade)
                                .setTranslatedName("Flee")
                                .setTranslatedSubtitle("Sometimes it's better just to run.")
                                .setIcon(Items.FEATHER, 3)
                                .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 12)
                                .addItemCost(Items.EMERALD, 32, provider)
                                .addEffect(FLEEING_SPEED, 3)
                                .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/hotfoot"), COMBAT).setParentResearch(flee)
          .setTranslatedName("Hotfoot")
          .setIcon(Items.CHICKEN)
          .addItemCost(Items.EMERALD, 64, provider)
          .addEffect(FLEEING_SPEED, 4)
          .addToList(r);

        // Primary Research #5
        final Research taunt =
          new Research(new ResourceLocation(Constants.MOD_ID, "combat/taunt"), COMBAT)
            .setTranslatedName("Taunt")
            .setTranslatedSubtitle("Your mother was a hamster and your father smelt of elderberries!")
            .setSortOrder(5)
            .setIcon(Items.CHAIN)
            .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 1)
            .addItemCost(Items.ROTTEN_FLESH, 8, provider)
            .addItemCost(Items.BONE, 8, provider)
            .addItemCost(Items.SPIDER_EYE, 8, provider)
            .addEffect(KNIGHT_TAUNT, 1)
            .addToList(r);
        final Research arrowUse = new Research(new ResourceLocation(Constants.MOD_ID, "combat/arrowuse"), COMBAT).setParentResearch(taunt)
                                    .setTranslatedName("Consume Arrows")
                                    .setTranslatedSubtitle("They work better with ammo.")
                                    .setIcon(Items.ARROW)
                                    .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 2)
                                    .addItemCost(Items.ARROW, 64, provider)
                                    .addEffect(ARCHER_USE_ARROWS, 1)
                                    .addToList(r);
        final Research arrowPierce = new Research(new ResourceLocation(Constants.MOD_ID, "combat/arrowpierce"), COMBAT).setParentResearch(arrowUse)
                                       .setTranslatedName("Arrow Piercing")
                                       .setIcon(Items.ENCHANTED_BOOK)
                                       .addBuildingRequirement(ModBuildings.ARCHERY_ID, 1)
                                       .addItemCost(Items.ARROW, 64, provider)
                                       .addItemCost(Items.REDSTONE, 64, provider)
                                       .addEffect(ARROW_PIERCE, 1)
                                       .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/druidpotion"), COMBAT).setParentResearch(arrowUse)
          .setTranslatedName("Panoramix")
          .setIcon(ModItems.mistletoe)
          .addBuildingRequirement(ModBuildings.BARRACKS_ID, 3)
          .addItemCost(ModItems.mistletoe, 64, provider)
          .addEffect(DRUID_USE_POTIONS, 1)
          .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/whirlwind"), COMBAT).setParentResearch(arrowPierce)
          .setTranslatedName("Whirlwind")
          .setIcon(ModItems.scimitar)
          .addBuildingRequirement(ModBuildings.BARRACKS_ID, 4)
          .addItemCost(Items.REDSTONE, 64, provider)
          .addItemCost(Items.GOLD_INGOT, 64, provider)
          .addItemCost(Items.LAPIS_LAZULI, 128, provider)
          .addEffect(KNIGHT_WHIRLWIND, 1)
          .addToList(r);

        return r;
    }

    public Collection<Research> getTechnologyResearch(Collection<Research> r)
    {
        // Primary Research #1
        final Research biodegradable = new Research(new ResourceLocation(Constants.MOD_ID, "technology/biodegradable"), TECH).setTranslatedName("Biodegradable")
                                         .setTranslatedSubtitle("Where science meets dirt.")
                                         .setIcon(ModBlocks.blockHutComposter.asItem())
                                         .addBuildingRequirement(ModBuildings.FARMER_ID, 3)
                                         .addItemCost(Items.BONE_MEAL, 64, provider)
                                         .addEffect(ModBuildings.composter.get().getBuildingBlock(), 1)
                                         .addToList(r);

        final Research podzolChance = new Research(new ResourceLocation(Constants.MOD_ID, "technology/podzolchance"), TECH).setParentResearch(biodegradable)
                                        .setTranslatedName("Podzol Chance")
                                        .setTranslatedSubtitle("Eww, sticky!")
                                        .setIcon(Items.PODZOL)
                                        .addBuildingRequirement(ModBuildings.COMPOSTER_ID, 2)
                                        .addItemCost(Items.PODZOL, 8, provider)
                                        .addEffect(PODZOL_CHANCE, 1)
                                        .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/podzolchance2"), TECH).setParentResearch(podzolChance)
                                        .setTranslatedName("Podzol Chance II")
                                        .setTranslatedSubtitle("Much easier than Silk Touch.")
                                        .setIcon(Items.PODZOL)
                                        .addBuildingRequirement(ModBuildings.COMPOSTER_ID, 3)
                                        .addItemCost(Items.PODZOL, 32, provider)
                                        .addEffect(PODZOL_CHANCE, 2)
                                        .addToList(r);

        final Research flowerPower = new Research(new ResourceLocation(Constants.MOD_ID, "technology/flowerpower"), TECH).setParentResearch(biodegradable)
                                       .setTranslatedName("Flower Power")
                                       .setIcon(ModBlocks.blockHutFlorist.asItem())
                                       .addBuildingRequirement(ModBuildings.COMPOSTER_ID, 3)
                                       .addItemCost(ModItems.compost, 64, provider)
                                       .addEffect(ModBuildings.florist.get().getBuildingBlock(), 1)
                                       .addToList(r);
        final Research rainbowHeaven = new Research(new ResourceLocation(Constants.MOD_ID, "technology/rainbowheaven"), TECH).setParentResearch(biodegradable)
          .setTranslatedName("Rainbow Heaven")
          .setTranslatedSubtitle("Now in color! And 3D!")
          .setIcon(ModBlocks.blockHutComposter.asItem())
          .addBuildingRequirement(ModBuildings.COMPOSTER_ID, 3)
          .addItemCost(Items.POPPY, 64, provider)
          .addEffect(ModBuildings.dyer.get().getBuildingBlock(), 1)
          .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/honeypot"), TECH).setParentResearch(rainbowHeaven)
          .setTranslatedName("Honey Pot")
          .setTranslatedSubtitle("Wasn't going to eat it. Just going to taste it.")
          .setIcon(Items.HONEYCOMB.asItem())
          .addBuildingRequirement(ModBuildings.BEEKEEPER_ID, 3)
          .addItemCost(Items.BEEHIVE, 16, provider)
          .addEffect(BEEKEEP_2, 1)
          .addToList(r);

        final Research letItGrow = new Research(new ResourceLocation(Constants.MOD_ID, "technology/letitgrow"), TECH).setParentResearch(biodegradable)
                                     .setTranslatedName("Let It Grow")
                                     .setTranslatedSubtitle("Just one tiny seed is all we really need.")
                                     .setSortOrder(2)
                                     .setIcon(ModBlocks.blockHutPlantation.asItem())
                                     .addBuildingRequirement(ModBuildings.FARMER_ID, 3)
                                     .addItemCost(ModItems.compost, 16, provider)
                                     .addEffect(ModBuildings.plantation.get().getBuildingBlock(), 1)
                                     .addToList(r);
        final Research cropRotation = new Research(new ResourceLocation(Constants.MOD_ID, "technology/croprotation"), TECH).setParentResearch(letItGrow)
                                        .setTranslatedName("Crop Rotation")
                                        .setTranslatedSubtitle("Bigger = better")
                                        .setIcon(Items.GREEN_DYE)
                                        .addBuildingRequirement(ModBuildings.PLANTATION_ID, 3)
                                        .addItemCost(Items.SUGAR_CANE, 32, provider)
                                        .addItemCost(Items.CACTUS, 32, provider)
                                        .addEffect(PLANTATION_LARGE, 1)
                                        .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "technology/junglemaster"), TECH).setParentResearch(letItGrow)
          .setTranslatedName("Jungle Master")
          .setTranslatedSubtitle("Wimoweh Wimoweh Wimoweh")
          .setIcon(Items.VINE)
          .addBuildingRequirement(ModBuildings.PLANTATION_ID, 2)
          .addItemCost(Items.BAMBOO, 16, provider)
          .addItemCost(Items.COCOA_BEANS, 16, provider)
          .addItemCost(Items.VINE, 16, provider)
          .addEffect(PLANTATION_JUNGLE, 1)
          .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "technology/takingdivinglessons"), TECH).setParentResearch(letItGrow)
          .setTranslatedName("Taking Diving Lessons")
          .setTranslatedSubtitle("*Drowning Noises*")
          .setIcon(Items.KELP)
          .addBuildingRequirement(ModBuildings.PLANTATION_ID, 2)
          .addItemCost(Items.KELP, 16, provider)
          .addItemCost(Items.SEAGRASS, 16, provider)
          .addItemCost(Items.SEA_PICKLE, 16, provider)
          .addEffect(PLANTATION_SEA, 1)
          .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "technology/exoticfruits"), TECH).setParentResearch(letItGrow)
          .setTranslatedName("Exotic Fruits")
          .setTranslatedSubtitle("Too Dark Here")
          .setIcon(Items.GLOW_BERRIES)
          .addBuildingRequirement(ModBuildings.PLANTATION_ID, 3)
          .addItemCost(Items.GLOW_BERRIES, 32, provider)
          .addEffect(PLANTATION_EXOTIC, 1)
          .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "technology/gargamel"), TECH).setParentResearch(cropRotation)
          .setTranslatedName("Gargamel")
          .setTranslatedSubtitle("The Root of all Evil")
          .setIcon(Items.CRIMSON_FUNGUS)
          .addBuildingRequirement(ModBuildings.PLANTATION_ID, 3)
          .addBuildingRequirement(ModBuildings.NETHERWORKER_ID, 3)
          .addItemCost(Items.CRIMSON_FUNGUS, 16, provider)
          .addItemCost(Items.WARPED_FUNGUS, 16, provider)
          .addEffect(PLANTATION_NETHER, 1)
          .addToList(r);

        final Research bonemeal = new Research(new ResourceLocation(Constants.MOD_ID, "technology/bonemeal"), TECH).setParentResearch(biodegradable)
                                    .setTranslatedName("Bonemeal")
                                    .setTranslatedSubtitle("And to think this stuff feeds our plants...")
                                    .setSortOrder(3)
                                    .setIcon(Items.WHEAT_SEEDS)
                                    .addBuildingRequirement(ModBuildings.MINER_ID, 3)
                                    .addItemCost(Items.WHEAT_SEEDS, 64, provider)
                                    .addEffect(FARMING, 1)
                                    .addToList(r);
        final Research dung = new Research(new ResourceLocation(Constants.MOD_ID, "technology/dung"), TECH).setParentResearch(bonemeal)
                                .setTranslatedName("Dung")
                                .setTranslatedSubtitle("Fresh or not, here it comes!")
                                .setIcon(Items.BONE_MEAL)
                                .addBuildingRequirement(ModBuildings.MINER_ID, 4)
                                .addItemCost(Items.WHEAT_SEEDS, 128, provider)
                                .addEffect(FARMING, 2)
                                .addToList(r);
        final Research compost = new Research(new ResourceLocation(Constants.MOD_ID, "technology/compost"), TECH).setParentResearch(dung)
                                   .setTranslatedName("Compost")
                                   .setIcon(Items.BONE)
                                   .addBuildingRequirement(ModBuildings.MINER_ID, 5)
                                   .addItemCost(Items.WHEAT_SEEDS, 256, provider)
                                   .addEffect(FARMING, 3)
                                   .addToList(r);
        final Research fertilizer = new Research(new ResourceLocation(Constants.MOD_ID, "technology/fertilizer"), TECH).setParentResearch(compost)
                                      .setTranslatedName("Fertilizer")
                                      .setTranslatedSubtitle("Ah, that's the stuff!")
                                      .setIcon(Items.BONE_BLOCK)
                                      .addBuildingRequirement(ModBuildings.SMELTERY_ID, 3)
                                      .addItemCost(Items.WHEAT_SEEDS, 512, provider)
                                      .addEffect(FARMING, 4)
                                      .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/magiccompost"), TECH).setParentResearch(fertilizer)
          .setTranslatedName("Magic Compost")
          .setIcon(ModBlocks.blockBarrel.asItem())
          .addItemCost(Items.WHEAT_SEEDS, 2048, provider)
          .addEffect(FARMING, 5)
          .addToList(r);

        // Primary Research #2
        final Research softShoes = new Research(new ResourceLocation(Constants.MOD_ID, "technology/softshoes"), TECH).setTranslatedName("Soft Shoes")
        .setTranslatedSubtitle("Tiptoe through the tulips.")
        .setSortOrder(2)
        .setIcon(Items.LEATHER_BOOTS)
        .addItemCost(Items.WHITE_WOOL, 16, provider)
        .addItemCost(Items.FEATHER, 16, provider)
        .addEffect(SOFT_SHOES, 1)
        .addToList(r);

        // Primary Research #3
        final Research moreScrolls = new Research(new ResourceLocation(Constants.MOD_ID, "technology/morescrolls"), TECH).setTranslatedName("More Scrolls")
          .setSortOrder(3)
          .setIcon(ModItems.scrollHighLight)
          .addBuildingRequirement("enchanter", 3)
          .addItemCost(Items.PAPER, 64, provider)
          .addItemCost(ModItems.ancientTome, 1, provider)
          .addItemCost(Items.LAPIS_LAZULI, 64, provider)
          .addEffect(new ResourceLocation("minecolonies", "effects/morescrollsunlock"), 1)
          .addToList(r);

        final Research netherminer = new Research(new ResourceLocation(Constants.MOD_ID, "technology/opennether"), TECH).setParentResearch(moreScrolls)
                                    .setTranslatedName("Open the Nether")
                                    .setTranslatedSubtitle("It's a dangerous job, but it must be done!")
                                    .setSortOrder(1)
                                    .setIcon(ModBlocks.blockHutNetherWorker.asItem())
                                    .addItemCost(Items.GILDED_BLACKSTONE, 3, provider)
                                    .addEffect(ModBuildings.netherWorker.get().getBuildingBlock(), 1)
                                    .addToList(r);

        final Research alchemist = new Research(new ResourceLocation(Constants.MOD_ID, "technology/alchemist"), TECH).setParentResearch(netherminer)
          .setTranslatedName("Magic Potions")
          .setTranslatedSubtitle("These Romans are crazy")
          .setIcon(ModBlocks.blockHutAlchemist.asItem())
          .addItemCost(Items.NETHER_WART, 16, provider)
          .addEffect(ModBuildings.alchemist.get().getBuildingBlock(), 1)
          .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "technology/netherlog"), TECH).setParentResearch(netherminer)
                .setTranslatedName("Gaze into the Pits")
                .setTranslatedSubtitle("Always use proper lenses to avoid eye damage")
                .setSortOrder(4)
                .setIcon(Items.ENDER_EYE)
                .addItemCost(Items.ENDER_EYE, 16, provider)
                .addItemCost(ModItems.ancientTome, 1, provider)
                .addBuildingRequirement(ModBuildings.NETHERWORKER_ID, 1)
                .addEffect(NETHER_LOG, 1)
                .addToList(r);

        // this is intended to be a side branch (since it has a very high cost) -- there's still room for a "main line" level 4 research!
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/oceanheart"), TECH).setParentResearch(alchemist)
                .setTranslatedName("Ocean's Heart")
                .setTranslatedSubtitle("With great mystic power comes great mystic loot!")
                .setSortOrder(4)
                .setIcon(Items.FISHING_ROD)
                .addBuildingRequirement(ModBuildings.FISHERMAN_ID, 4)
                .addItemCost(Items.HEART_OF_THE_SEA, 1, provider)
                .addEffect(FISH_TREASURE, 1)
                .addToList(r);

        // Primary Research #4
        final Research stoneCake = new Research(new ResourceLocation(Constants.MOD_ID, "technology/stonecake"), TECH).setTranslatedName("Stone Cake")
                                     .setTranslatedSubtitle("Don't break a tooth!")
                                     .setSortOrder(4)
                                     .setIcon(ModBlocks.blockHutStonemason.asItem())
                                     .addBuildingRequirement(ModBuildings.MINER_ID, 3)
                                     .addItemCost(Items.CHISELED_STONE_BRICKS, 64, provider)
                                     .addEffect(ModBuildings.stoneMason.get().getBuildingBlock(), 1)
                                     .addToList(r);
        final Research rockingRoll = new Research(new ResourceLocation(Constants.MOD_ID, "technology/rockingroll"), TECH).setParentResearch(stoneCake)
                                       .setTranslatedName("Rocking Roll")
                                       .setIcon(ModBlocks.blockHutCrusher.asItem())
                                       .addBuildingRequirement("stonemason", 1)
                                       .addItemCost(Items.STONE, 64, provider)
                                       .addEffect(ModBuildings.crusher.get().getBuildingBlock(), 1)
                                       .addToList(r);

        final Research theFlintstones = new Research(new ResourceLocation(Constants.MOD_ID, "technology/theflintstones"), TECH).setParentResearch(stoneCake)
                                          .setTranslatedName("The Flintstones")
                                          .setTranslatedSubtitle("Yabba Dabba Doo!")
                                          .setIcon(ModBlocks.blockHutStoneSmeltery.asItem())
                                          .addBuildingRequirement(ModBuildings.STONE_MASON_ID, 1)
                                          .addItemCost(Items.BRICK, 64, provider)
                                          .addEffect(ModBuildings.stoneSmelter.get().getBuildingBlock(), 1)
                                          .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/knowtheend"), TECH).setParentResearch(theFlintstones)
          .setTranslatedName("Know the End")
          .setTranslatedSubtitle("Unlock the secrets of the most mysterious dimension.")
          .setIcon(ModItems.chorusBread)
          .addBuildingRequirement("baker", 3)
          .addItemCost(Items.CHORUS_FRUIT, 64, provider)
          .addEffect(new ResourceLocation("minecolonies", "effects/knowledgeoftheendunlock"), 1)
          .addToList(r);


        new Research(new ResourceLocation(Constants.MOD_ID, "technology/gildedhammer"), TECH).setParentResearch(rockingRoll)
          .setTranslatedName("Gilded Hammer")
          .setTranslatedSubtitle("When in doubt, cover in shiny stuff.")
          .setIcon(Items.GOLD_BLOCK)
          .addBuildingRequirement("crusher", 3)
          .addItemCost(Items.GRAVEL, 64, provider)
          .addItemCost(Items.SAND, 64, provider)
          .addItemCost(Items.CLAY, 64, provider)
          .addEffect(CRUSHING_11, 1)
          .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "technology/depthknowledge"), TECH).setParentResearch(rockingRoll)
          .setTranslatedName("Knowledge of the Depth")
          .setTranslatedSubtitle("Gotta go deep for that one.")
          .setIcon(Items.COBBLED_DEEPSLATE)
          .addBuildingRequirement("crusher", 3)
          .addItemCost(Items.DEEPSLATE, 64, provider)
          .addEffect(THE_DEPTHS, 1)
          .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "technology/pavetheroad"), TECH).setParentResearch(rockingRoll)
          .setTranslatedName("Pave the Road")
          .setTranslatedSubtitle("Not something you want to get mixed up in.")
          .setSortOrder(2)
          .setIcon(ModBlocks.blockHutConcreteMixer.asItem())
          .addBuildingRequirement("crusher", 1)
          .addItemCost(ModTags.concreteItems, 32, provider)
          .addEffect(ModBuildings.concreteMixer.get().getBuildingBlock(), 1)
          .addToList(r);

        // Primary Research #5
        final Research woodwork = new Research(new ResourceLocation(Constants.MOD_ID, "technology/woodwork"), TECH).setTranslatedName("Woodwork")
                                    .setTranslatedSubtitle("Where oh where would a wood worker work if a wood worker would work wood?")
                                    .setSortOrder(5)
                                    .setIcon(ModBlocks.blockHutSawmill.asItem())
                                    .addBuildingRequirement("lumberjack", 3)
                                    .addItemCost(ItemTags.PLANKS, 64, provider)
                                    .addEffect(ModBuildings.sawmill.get().getBuildingBlock(), 1)
                                    .addToList(r);
        final Research stringWork = new Research(new ResourceLocation(Constants.MOD_ID, "technology/stringwork"), TECH).setParentResearch(woodwork)
                                      .setTranslatedName("Stringwork")
                                      .setIcon(ModBlocks.blockHutFletcher.asItem())
                                      .addBuildingRequirement(ModBuildings.SAWMILL_ID, 1)
                                      .addItemCost(Items.STRING, 16, provider)
                                      .addEffect(ModBuildings.fletcher.get().getBuildingBlock(), 1)
                                      .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/hotboots"), TECH).setParentResearch(stringWork)
          .setTranslatedName("Hot Boots")
          .setTranslatedSubtitle("Warmer on the outside.")
          .setIcon(Items.CAMPFIRE)
          .addBuildingRequirement(ModBuildings.FLETCHER_ID, 1)
          .addItemCost(Items.LEATHER, 32, provider)
          .addItemCost(Items.IRON_INGOT, 16, provider)
          .addEffect(FIRE_RES, 1)
          .addToList(r);

        final Research sieving = new Research(new ResourceLocation(Constants.MOD_ID, "technology/sieving"), TECH).setParentResearch(woodwork)
                                   .setTranslatedName("Sieving")
                                   .setTranslatedSubtitle("How did that get in there?")
                                   .setSortOrder(2)
                                   .setIcon(ModBlocks.blockHutSifter.asItem())
                                   .addBuildingRequirement(ModBuildings.FISHERMAN_ID, 3)
                                   .addItemCost(Items.STRING, 64, provider)
                                   .addEffect(ModBuildings.sifter.get().getBuildingBlock(), 1)
                                   .addToList(r);
        final Research space = new Research(new ResourceLocation(Constants.MOD_ID, "technology/space"), TECH).setParentResearch(sieving)
                                 .setTranslatedName("Space")
                                 .setTranslatedSubtitle("Antidisinterdimensionalitarianism!")
                                 .setIcon(Items.CHEST)
                                 .addBuildingRequirement(ModBuildings.MINER_ID, 3)
                                 .addItemCost(ModBlocks.blockRack.asItem(), 16, provider)
                                 .addEffect(MINIMUM_STOCK, 1)
                                 .addToList(r);
        final Research capacity = new Research(new ResourceLocation(Constants.MOD_ID, "technology/capacity"), TECH).setParentResearch(space)
                                    .setTranslatedName("Capacity")
                                    .setTranslatedSubtitle("Don't ask how we fit it all.")
                                    .setIcon(Items.CHEST_MINECART)
                                    .addBuildingRequirement(ModBuildings.MINER_ID, 4)
                                    .addItemCost(ModBlocks.blockRack.asItem(), 32, provider)
                                    .addEffect(MINIMUM_STOCK, 2)
                                    .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/fullstock"), TECH).setParentResearch(capacity)
          .setTranslatedName("Full Stock!")
          .setTranslatedSubtitle("We might be able to squeeze in one more.")
          .setIcon(Items.ENDER_CHEST)
          .addBuildingRequirement(ModBuildings.MINER_ID, 5)
          .addItemCost(ModBlocks.blockRack.asItem(), 64, provider)
          .addEffect(MINIMUM_STOCK, 3)
          .addToList(r);

        final Research memoryAid = new Research(new ResourceLocation(Constants.MOD_ID, "technology/memoryaid"), TECH).setParentResearch(woodwork)
                                     .setTranslatedName("Memory Aid")
                                     .setTranslatedSubtitle("It's the thought that counts.")
                                     .setSortOrder(3)
                                     .setIcon(Items.PAPER)
                                     .addBuildingRequirement(ModBuildings.SAWMILL_ID, 1)
                                     .addItemCost(Items.PAPER, 32, provider)
                                     .addEffect(RECIPES, 1)
                                     .addToList(r);

        final Research cheatSheet = new Research(new ResourceLocation(Constants.MOD_ID, "technology/cheatsheet"), TECH).setParentResearch(memoryAid)
                                      .setTranslatedName("Cheat Sheet")
                                      .setTranslatedSubtitle("So THAT's what I should be making!")
                                      .setIcon(Items.BOOK)
                                      .addBuildingRequirement(ModBuildings.SAWMILL_ID, 2)
                                      .addItemCost(Items.PAPER, 64, provider)
                                      .addEffect(RECIPES, 2)
                                      .setSortOrder(1)
                                      .addToList(r);
        final Research recipeBook = new Research(new ResourceLocation(Constants.MOD_ID, "technology/recipebook"), TECH).setParentResearch(cheatSheet)
                                      .setTranslatedName("Recipe Book")
                                      .setIcon(Items.ENCHANTED_BOOK)
                                      .addBuildingRequirement(ModBuildings.SAWMILL_ID, 3)
                                      .addItemCost(Items.PAPER, 128, provider)
                                      .addEffect(RECIPES, 3)
                                      .addToList(r);
        final Research rtm = new Research(new ResourceLocation(Constants.MOD_ID, "technology/rtm"), TECH).setParentResearch(recipeBook)
                               .setTranslatedName("RTM")
                               .setTranslatedSubtitle("I saw some information on this somewhere...")
                               .setIcon(Items.BOOKSHELF)
                               .addBuildingRequirement(ModBuildings.SAWMILL_ID, 4)
                               .addItemCost(Items.PAPER, 256, provider)
                               .addEffect(RECIPES, 4)
                               .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/rainman"), TECH).setParentResearch(rtm)
          .setTranslatedName("Rainman")
          .setTranslatedSubtitle("Raindrops are falling on my head...")
          .setIcon(Items.SPLASH_POTION)
          .addItemCost(Items.SALMON_BUCKET, 27, provider)
          .addEffect(WORKING_IN_RAIN, 1)
          .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "technology/warehousemaster"), TECH).setParentResearch(memoryAid)
                                      .setTranslatedName("Warehouse Master")
                                      .setTranslatedSubtitle("So many items to choose from!")
                                      .setIcon(ModBlocks.blockRack.asItem())
                                      .addBuildingRequirement(ModBuildings.SAWMILL_ID, 3)
                                      .addItemCost(ModBlocks.blockRack.asItem(), 3, provider)
                                      .addEffect(RECIPE_MODE, 1)
                                      .setSortOrder(2)
                                      .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "technology/buildermodes"), TECH).setParentResearch(memoryAid)
          .setTranslatedName("Builder Modes")
          .setTranslatedSubtitle("Possibility Overload!")
          .setIcon(ModBlocks.blockHutBuilder.asItem())
          .addBuildingRequirement(ModBuildings.BUILDER_ID, 3)
          .addItemCost(Items.DIAMOND_AXE, 1, provider)
          .addEffect(BUILDER_MODE, 1)
          .setSortOrder(3)
          .addToList(r);

        final Research deepPockets = new Research(new ResourceLocation(Constants.MOD_ID, "technology/deeppockets"), TECH).setParentResearch(cheatSheet)
                                       .setTranslatedName("Deep Pockets")
                                       .setSortOrder(2)
                                       .setIcon(Items.PINK_SHULKER_BOX)
                                       .addBuildingRequirement(ModBuildings.LIBRARY_ID, 4)
                                       .addItemCost(Items.EMERALD, 64, provider)
                                       .addEffect(CITIZEN_INV_SLOTS, 1)
                                       .addToList(r);
        final Research loaded = new Research(new ResourceLocation(Constants.MOD_ID, "technology/loaded"), TECH).setParentResearch(deepPockets)
                                  .setTranslatedName("Loaded")
                                  .setIcon(Items.RED_SHULKER_BOX)
                                  .addBuildingRequirement(ModBuildings.LIBRARY_ID, 5)
                                  .addItemCost(Items.EMERALD, 128, provider)
                                  .addEffect(CITIZEN_INV_SLOTS, 2)
                                  .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/heavilyloaded"), TECH).setParentResearch(loaded)
          .setTranslatedName("Heavily Loaded")
          .setIcon(Items.BLUE_SHULKER_BOX)
          .setNoReset()
          .addItemCost(Items.EMERALD, 256, provider)
          .addEffect(CITIZEN_INV_SLOTS, 3)
          .addToList(r);

        // Primary Research #6
        final Research hot = new Research(new ResourceLocation(Constants.MOD_ID, "technology/hot"), TECH).setTranslatedName("Hot!")
                               .setSortOrder(6)
                               .setIcon(ModBlocks.blockHutSmeltery.asItem())
                               .addBuildingRequirement(ModBuildings.MINER_ID, 2)
                               .addItemCost(Items.LAVA_BUCKET, 4, provider)
                               .addEffect(ModBuildings.smeltery.get().getBuildingBlock(), 1)
                               .addToList(r);
        final Research isThisRedstone = new Research(new ResourceLocation(Constants.MOD_ID, "technology/isthisredstone"), TECH).setParentResearch(hot)
                                          .setTranslatedName("Is This Redstone?")
                                          .setIcon(Items.REDSTONE)
                                          .addItemCost(Items.REDSTONE, 128, provider)
                                          .addEffect(BLOCK_BREAK_SPEED, 1)
                                          .addToList(r);
        final Research redstonePowered = new Research(new ResourceLocation(Constants.MOD_ID, "technology/redstonepowered"), TECH).setParentResearch(isThisRedstone)
                                           .setTranslatedName("Redstone Powered")
                                           .setTranslatedSubtitle("Like magic, but SCIENCE!")
                                           .setIcon(Items.REDSTONE_TORCH)
                                           .addItemCost(Items.REDSTONE, 256, provider)
                                           .addEffect(BLOCK_BREAK_SPEED, 2)
                                           .addToList(r);
        final Research heavyMachinery = new Research(new ResourceLocation(Constants.MOD_ID, "technology/heavymachinery"), TECH).setParentResearch(redstonePowered)
                                          .setTranslatedName("Heavy Machinery")
                                          .setIcon(Items.REDSTONE_BLOCK)
                                          .addItemCost(Items.REDSTONE, 512, provider)
                                          .addEffect(BLOCK_BREAK_SPEED, 3)
                                          .addToList(r);
        final Research whatIsThisSpeed = new Research(new ResourceLocation(Constants.MOD_ID, "technology/whatisthisspeed"), TECH).setParentResearch(heavyMachinery)
                                           .setTranslatedName("What Is This Speed?")
                                           .setTranslatedSubtitle("We stopped trying to calculate it after a while.")
                                           .setIcon(Items.COMPARATOR)
                                           .addItemCost(Items.REDSTONE, 1024, provider)
                                           .addEffect(BLOCK_BREAK_SPEED, 4)
                                           .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/lightning"), TECH).setParentResearch(whatIsThisSpeed)
          .setTranslatedName("Lightning")
          .setTranslatedSubtitle("BAM! And the block is gone!")
          .setIcon(Items.REPEATER)
          .addItemCost(Items.REDSTONE, 2048, provider)
          .addEffect(BLOCK_BREAK_SPEED, 5)
          .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "technology/thoselungs"), TECH).setParentResearch(hot)
          .setTranslatedName("Those Lungs!")
          .setTranslatedSubtitle("You'll definitely be needing those in some form.")
          .setIcon(ModBlocks.blockHutGlassblower.asItem())
          .addBuildingRequirement(ModBuildings.SMELTERY_ID, 3)
          .addItemCost(Items.GLASS, 64, provider)
          .addEffect(ModBuildings.glassblower.get().getBuildingBlock(), 1)
          .addToList(r);

        // Primary Research #7
        final Research hittingIron = new Research(new ResourceLocation(Constants.MOD_ID, "technology/hittingiron"), TECH).setTranslatedName("Hitting Iron!")
                                       .setSortOrder(7)
                                       .setTranslatedSubtitle("We're still ironing out the details.")
                                       .setIcon(ModBlocks.blockHutBlacksmith.asItem())
                                       .addBuildingRequirement(ModBuildings.MINER_ID, 3)
                                       .addItemCost(Items.ANVIL, 1, provider)
                                       .addEffect(ModBuildings.blacksmith.get().getBuildingBlock(), 1)
                                       .addToList(r);
        final Research strong = new Research(new ResourceLocation(Constants.MOD_ID, "technology/strong"), TECH).setParentResearch(hittingIron)
                                  .setTranslatedName("Strong")
                                  .setIcon(Items.WOODEN_PICKAXE)
                                  .addBuildingRequirement(ModBuildings.BLACKSMITH_ID, 1)
                                  .addItemCost(Items.DIAMOND, 8, provider)
                                  .addEffect(TOOL_DURABILITY, 1)
                                  .addToList(r);
        final Research hardened = new Research(new ResourceLocation(Constants.MOD_ID, "technology/hardened"), TECH).setParentResearch(strong)
                                    .setTranslatedName("Hardened")
                                    .setIcon(Items.STONE_PICKAXE)
                                    .addBuildingRequirement(ModBuildings.BLACKSMITH_ID, 2)
                                    .addItemCost(Items.DIAMOND, 16, provider)
                                    .addEffect(TOOL_DURABILITY, 2)
                                    .addToList(r);
        final Research reinforced = new Research(new ResourceLocation(Constants.MOD_ID, "technology/reinforced"), TECH).setParentResearch(hardened)
                                      .setTranslatedName("Reinforced")
                                      .setIcon(Items.IRON_PICKAXE)
                                      .addBuildingRequirement(ModBuildings.BLACKSMITH_ID, 3)
                                      .addItemCost(Items.DIAMOND, 32, provider)
                                      .addEffect(TOOL_DURABILITY, 3)
                                      .addToList(r);
        final Research steelBracing = new Research(new ResourceLocation(Constants.MOD_ID, "technology/steelbracing"), TECH).setParentResearch(reinforced)
                                        .setTranslatedName("Steel Bracing")
                                        .setIcon(Items.GOLDEN_PICKAXE)
                                        .addBuildingRequirement(ModBuildings.BLACKSMITH_ID, 5)
                                        .addItemCost(Items.DIAMOND, 64, provider)
                                        .addEffect(TOOL_DURABILITY, 4)
                                        .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/diamondcoated"), TECH).setParentResearch(steelBracing)
          .setTranslatedName("Diamond Coated")
          .setIcon(Items.DIAMOND_PICKAXE)
          .addItemCost(Items.DIAMOND, 128, provider)
          .addEffect(TOOL_DURABILITY, 5)
          .addToList(r);

        final Research ability = new Research(new ResourceLocation(Constants.MOD_ID, "technology/ability"), TECH).setParentResearch(hittingIron)
                                   .setTranslatedName("Ability")
                                   .setIcon(Items.GLOWSTONE_DUST)
                                   .addBuildingRequirement(ModBuildings.MINER_ID, 1)
                                   .addItemCost(Items.IRON_INGOT, 64, provider)
                                   .addEffect(BLOCK_PLACE_SPEED, 1)
                                   .addToList(r);
        final Research skills = new Research(new ResourceLocation(Constants.MOD_ID, "technology/skills"), TECH).setParentResearch(ability)
                                  .setTranslatedName("Skills")
                                  .setTranslatedSubtitle("Everything in its place.")
                                  .setIcon(Items.GLOWSTONE)
                                  .addBuildingRequirement(ModBuildings.MINER_ID, 2)
                                  .addItemCost(Items.IRON_INGOT, 128, provider)
                                  .addEffect(BLOCK_PLACE_SPEED, 2)
                                  .addToList(r);
        final Research tools = new Research(new ResourceLocation(Constants.MOD_ID, "technology/tools"), TECH).setParentResearch(skills)
                                 .setTranslatedName("Tools")
                                 .setTranslatedSubtitle("Like breaking stuff, but in reverse!")
                                 .setIcon(Items.REDSTONE_LAMP)
                                 .addBuildingRequirement(ModBuildings.BLACKSMITH_ID, 4)
                                 .addItemCost(Items.IRON_INGOT, 256, provider)
                                 .addEffect(BLOCK_PLACE_SPEED, 3)
                                 .addToList(r);
        final Research seemsAutomatic = new Research(new ResourceLocation(Constants.MOD_ID, "technology/seemsautomatic"), TECH).setParentResearch(tools)
                                          .setTranslatedName("Seems Automatic")
                                          .setTranslatedSubtitle("It all happened so fast...")
                                          .setIcon(Items.BLAZE_POWDER)
                                          .addBuildingRequirement(ModBuildings.BLACKSMITH_ID, 5)
                                          .addItemCost(Items.IRON_INGOT, 512, provider)
                                          .addEffect(BLOCK_PLACE_SPEED, 4)
                                          .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/madness"), TECH).setParentResearch(seemsAutomatic)
          .setTranslatedName("Madness!")
          .setIcon(Items.SPECTRAL_ARROW)
          .addItemCost(Items.IRON_INGOT, 1024, provider)
          .addEffect(BLOCK_PLACE_SPEED, 5)
          .addToList(r);

        final Research veinminer = new Research(new ResourceLocation(Constants.MOD_ID, "technology/veinminer"), TECH).setParentResearch(hittingIron)
                                     .setTranslatedName("Veinminer")
                                     .setIcon(Items.IRON_BLOCK)
                                     .addBuildingRequirement(ModBuildings.MINER_ID, 1)
                                     .addItemCost(ItemTags.IRON_ORES, 32, provider)
                                     .addEffect(MORE_ORES, 1)
                                     .addToList(r);
        final Research goodVeins = new Research(new ResourceLocation(Constants.MOD_ID, "technology/goodveins"), TECH).setParentResearch(veinminer)
                                     .setTranslatedName("Good Veins")
                                     .setIcon(Items.COAL_BLOCK)
                                     .addBuildingRequirement(ModBuildings.MINER_ID, 2)
                                     .addItemCost(ItemTags.IRON_ORES, 64, provider)
                                     .addEffect(MORE_ORES, 2)
                                     .addToList(r);
        final Research richVeins = new Research(new ResourceLocation(Constants.MOD_ID, "technology/richveins"), TECH).setParentResearch(goodVeins)
                                     .setTranslatedName("Rich Veins")
                                     .setIcon(Items.GOLD_BLOCK)
                                     .addBuildingRequirement(ModBuildings.BLACKSMITH_ID, 4)
                                     .addItemCost(ItemTags.GOLD_ORES, 32, provider)
                                     .addEffect(MORE_ORES, 3)
                                     .addToList(r);
        final Research amazingVeins = new Research(new ResourceLocation(Constants.MOD_ID, "technology/amazingveins"), TECH).setParentResearch(richVeins)
                                        .setTranslatedName("Amazing Veins")
                                        .setIcon(Items.LAPIS_BLOCK)
                                        .addBuildingRequirement(ModBuildings.BLACKSMITH_ID, 5)
                                        .addItemCost(ItemTags.GOLD_ORES, 64, provider)
                                        .addEffect(MORE_ORES, 4)
                                        .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/motherlode"), TECH).setParentResearch(amazingVeins)
          .setTranslatedName("Motherlode")
          .setIcon(Items.DIAMOND_BLOCK)
          .addItemCost(ItemTags.DIAMOND_ORES, 64, provider)
          .addEffect(MORE_ORES, 5)
          .addToList(r);

        final Research whatYaNeed = new Research(new ResourceLocation(Constants.MOD_ID, "technology/whatyaneed"), TECH).setParentResearch(hittingIron)
                                      .setTranslatedName("What ya Need?")
                                      .setTranslatedSubtitle("It's not a rhetorical question...")
                                      .setIcon(ModBlocks.blockHutMechanic.asItem())
                                      .addBuildingRequirement(ModBuildings.BLACKSMITH_ID, 3)
                                      .addItemCost(Items.REDSTONE, 64, provider)
                                      .addEffect(ModBuildings.mechanic.get().getBuildingBlock(), 1)
                                      .addToList(r);
        final Research enhanced_gates1 = new Research(new ResourceLocation(Constants.MOD_ID, "technology/enhanced_gates1"), TECH).setParentResearch(whatYaNeed)
                                           .setTranslatedName("Enhanced Gates I")
                                           .setIcon(ModItems.woodgate)
                                           .addItemCost(ModItems.woodgate, 64, provider)
                                           .addItemCost(ModItems.ancientTome, 2, provider)
                                           .addItemCost(Items.IRON_BLOCK, 5, provider)
                                           .addEffect(MECHANIC_ENHANCED_GATES, 1)
                                           .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/enhanced_gates2"), TECH).setParentResearch(enhanced_gates1)
          .setTranslatedName("Enhanced Gates II")
          .setIcon(ModItems.irongate)
          .addItemCost(ModItems.irongate, 64, provider)
          .addItemCost(ModItems.ancientTome, 2, provider)
          .addItemCost(Items.OBSIDIAN, 32, provider)
          .addEffect(MECHANIC_ENHANCED_GATES, 2)
          .addToList(r);

        return r;
    }

    public Collection<Research> getAchievementResearch(Collection<Research> r)
    {
        Research stringmesh = new Research(new ResourceLocation(Constants.MOD_ID, "unlockable/stringmesh"), UNLOCK)
            .setTranslatedName("String Mesh")
            .setIcon(ModItems.sifterMeshString)
            .addMandatoryBuildingRequirement(ModBuildings.SIFTER_ID, 1)
            .setHidden()
            .setAutostart()
            .setInstant()
            .addEffect(new ResourceLocation(Constants.MOD_ID, "effects/sifterstringunlock"), 1)
            .addToList(r);

        Research flintmesh = new Research(new ResourceLocation(Constants.MOD_ID, "unlockable/flintmesh"), UNLOCK)
            .setTranslatedName("Flint Mesh")
            .setParentResearch(stringmesh)
            .setIcon(ModItems.sifterMeshString)
            .addMandatoryBuildingRequirement(ModBuildings.SIFTER_ID, 3)
            .setHidden()
            .setAutostart()
            .setInstant()
            .addEffect(new ResourceLocation(Constants.MOD_ID, "effects/sifterflintunlock"), 1)
            .addToList(r);

        Research ironmesh = new Research(new ResourceLocation(Constants.MOD_ID, "unlockable/ironmesh"), UNLOCK)
            .setTranslatedName("Iron Mesh")
            .setParentResearch(flintmesh)
            .setIcon(ModItems.sifterMeshString)
            .addMandatoryBuildingRequirement(ModBuildings.SIFTER_ID, 4)
            .setHidden()
            .setAutostart()
            .setInstant()
            .addEffect(new ResourceLocation(Constants.MOD_ID, "effects/sifterironunlock"), 1)
            .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "unlockable/diamondmesh"), UNLOCK)
            .setTranslatedName("Diamond Mesh")
            .setParentResearch(ironmesh)
            .setIcon(ModItems.sifterMeshString)
            .addMandatoryBuildingRequirement(ModBuildings.SIFTER_ID, 5)
            .setHidden()
            .setAutostart()
            .setInstant()
            .addEffect(new ResourceLocation(Constants.MOD_ID, "effects/sifterdiamondunlock"), 1)
            .addToList(r);

        return r;
    }
}
