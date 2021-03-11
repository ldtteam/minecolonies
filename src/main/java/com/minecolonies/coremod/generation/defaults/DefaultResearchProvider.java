package com.minecolonies.coremod.generation.defaults;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.research.AbstractResearchProvider;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Items;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    public DefaultResearchProvider(final DataGenerator generator)
    {
        super(generator);
    }

    private static final ResourceLocation CIVIL  = new ResourceLocation(Constants.MOD_ID, "civilian");
    private static final ResourceLocation COMBAT = new ResourceLocation(Constants.MOD_ID, "combat");
    private static final ResourceLocation TECH   = new ResourceLocation(Constants.MOD_ID, "technology");

    /**
     * Get a list of all research branches. Conventions: these are not mandatory, and their inclusion simply fixes capitalization. MineColonies should fully populate new branches
     * for clarity; other data pack makers may not want to do so.
     *
     * @return a complete list of all research branches.
     */
    @Override
    public Collection<ResearchBranch> getResearchBranchCollection()
    {
        final List<ResearchBranch> branches = new ArrayList<>();
        branches.add(new ResearchBranch(CIVIL).setTranslatedBranchName("Civilian").setBranchTimeMultiplier(1.0));
        branches.add(new ResearchBranch(COMBAT).setTranslatedBranchName("Combat").setBranchTimeMultiplier(1.0));
        branches.add(new ResearchBranch(TECH).setTranslatedBranchName("Technology").setBranchTimeMultiplier(1.0));
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
        effects.add(new ResearchEffect(CITIZEN_CAP).setTranslatedName("Increase Max Citizens +%s").setLevels(new double[] {25, 50, 75, 175}));
        effects.add(new ResearchEffect(CITIZEN_INV_SLOTS).setTranslatedName("Citizen Inventory +%s Slots").setLevels(new double[] {9, 18, 27}));
        effects.add(new ResearchEffect(DOUBLE_ARROWS).setTranslatedName("Archer Multishot +%3$s%%").setLevels(new double[] {0.05, 0.1, 0.25, 0.5}));
        effects.add(new ResearchEffect(MECHANIC_ENHANCED_GATES).setTranslatedName("Gates Gain +100% Raider Swarm Resistance").setLevels(new double[] {5, 15}));
        effects.add(new ResearchEffect(FARMING).setTranslatedName("Farmers Harvest +%3$s%% Crops").setLevels(new double[] {0.1, 0.25, 0.5, 0.75, 2}));
        effects.add(new ResearchEffect(FLEEING_DAMAGE).setTranslatedName("Guards Take -%3$s%% Damage When Fleeing").setLevels(new double[] {0.2, 0.3, 0.4, 1}));
        effects.add(new ResearchEffect(FLEEING_SPEED).setTranslatedName("Fleeing Citizens Gain Swiftness %2$s").setLevels(new double[] {1, 2, 3, 5}));
        effects.add(new ResearchEffect(GROWTH).setTranslatedName("Child Growth Rate +%3$s%%").setLevels(new double[] {0.05, 0.1, 0.25, 0.5, 1}));
        effects.add(new ResearchEffect(HAPPINESS).setTranslatedName("Citizen Happiness +%3$s%%").setLevels(new double[] {0.05, 0.1, 0.1, 0.2, 0.5}));
        effects.add(new ResearchEffect(SATLIMIT).setTranslatedName("Healing Saturation Min %s").setLevels(new double[] {-0.5, -1, -1.5, -2, -5}));
        effects.add(new ResearchEffect(HEALTH_BOOST).setTranslatedName("Citizen HP +%s").setLevels(new double[] {2, 4, 6, 8, 10, 20}));
        effects.add(new ResearchEffect(LEVELING).setTranslatedName("Citizen XP Growth +%3$s%%").setLevels(new double[] {0.05, 0.1, 0.25, 0.5, 1}));
        effects.add(new ResearchEffect(MELEE_ARMOR).setTranslatedName("Knights Armor +%3$s%%").setLevels(new double[] {0.05, 0.1, 0.25, 0.5, 1}));
        effects.add(new ResearchEffect(MELEE_DAMAGE).setTranslatedName("Knight Damage +%s").setLevels(new double[] {0.5, 1, 1.5, 2, 4}));
        effects.add(new ResearchEffect(MINIMUM_STOCK).setTranslatedName("Buildings Can Minimum Stock %3$s%% More").setLevels(new double[] {0.5, 1, 2}));
        effects.add(new ResearchEffect(MORE_ORES).setTranslatedName("Miners Find +%3$s%% More Ores").setLevels(new double[] {0.1, 0.25, 0.5, 1, 2}));
        effects.add(new ResearchEffect(RECIPES).setTranslatedName("Workers Can Learn +%3$s%% More Recipes").setLevels(new double[] {0.25, 0.5, 1, 2}));
        effects.add(new ResearchEffect(REGENERATION).setTranslatedName("Citizen Regeneration +%3$s%%").setLevels(new double[] {0.1, 0.25, 0.5, 1, 2}));
        effects.add(new ResearchEffect(SATURATION).setTranslatedName("Citizen Saturation Per Meal +%3$s%%").setLevels(new double[] {0.1, 0.25, 0.5, 1, 2}));
        effects.add(new ResearchEffect(TEACHING).setTranslatedName("XP Gain When Studying +%3$s%%").setLevels(new double[] {0.05, 0.1, 0.25, 0.5, 1}));
        effects.add(new ResearchEffect(TOOL_DURABILITY).setTranslatedName("Citizen Tools +%3$s%% Durability").setLevels(new double[] {0.05, 0.1, 0.25, 0.5, 0.9}));
        effects.add(new ResearchEffect(WALKING).setTranslatedName("Citizen Walk Speed +%3$s%%").setLevels(new double[] {0.05, 0.1, 0.15, 0.25}));
        effects.add(new ResearchEffect(WORK_LONGER).setTranslatedName("Citizen Work Day +%sH").setLevels(new double[] {1, 2}));

        // Guard and Worker unlocks do not need a strength, but do have static ResourceLocations.
        effects.add(new ResearchEffect(ARCHER_USE_ARROWS).setTranslatedName("Archers Use Arrows For +2 Damage"));
        effects.add(new ResearchEffect(CRUSHING_11).setTranslatedName("Crusher Recipes Cost -50%"));
        effects.add(new ResearchEffect(KNIGHT_TAUNT).setTranslatedName("Knights Force Mobs to Target Them"));
        effects.add(new ResearchEffect(FIRE_RES).setTranslatedName("Miners Have Fire and Lava Immunity"));
        effects.add(new ResearchEffect(ARROW_PIERCE).setTranslatedName("Archers Gain Piercing II"));
        effects.add(new ResearchEffect(PLANT_2).setTranslatedName("Plantations Grow Two Crops at Once"));
        effects.add(new ResearchEffect(RAILS).setTranslatedName("Citizens use Rails"));
        effects.add(new ResearchEffect(RETREAT).setTranslatedName("Guards Flee Under 20% HP"));
        effects.add(new ResearchEffect(SHIELD_USAGE).setTranslatedName("Knights Unlock Shield Use"));
        effects.add(new ResearchEffect(SLEEP_LESS).setTranslatedName("Guards Need %3$s%% Less Sleep"));
        effects.add(new ResearchEffect(KNIGHT_WHIRLWIND).setTranslatedName("Knights Learn Special Attack That Damages and Knocks Back Nearby Enemies"));
        effects.add(new ResearchEffect(WORKING_IN_RAIN).setTranslatedName("Citizens Work in Rain"));

        // Building-focused unlocks are derived from the block hut name.  Do not manually add ResourceLocations as a string, as some building blocks have surprising names.
        effects.add(new ResearchEffect(ModBuildings.archery.getBuildingBlock()).setTranslatedName("Unlocks Archery"));
        effects.add(new ResearchEffect(ModBuildings.barracks.getBuildingBlock()).setTranslatedName("Unlocks Barracks"));
        effects.add(new ResearchEffect(ModBuildings.blacksmith.getBuildingBlock()).setTranslatedName("Unlocks Blacksmith's Hut"));
        effects.add(new ResearchEffect(ModBuildings.combatAcademy.getBuildingBlock()).setTranslatedName("Unlocks Combat Academy"));
        effects.add(new ResearchEffect(ModBuildings.composter.getBuildingBlock()).setTranslatedName("Unlocks Composter's Hut"));
        effects.add(new ResearchEffect(ModBuildings.concreteMixer.getBuildingBlock()).setTranslatedName("Unlocks Concrete Mixer's Hut"));
        effects.add(new ResearchEffect(ModBuildings.crusher.getBuildingBlock()).setTranslatedName("Unlocks Crusher's Hut"));
        effects.add(new ResearchEffect(ModBuildings.dyer.getBuildingBlock()).setTranslatedName("Unlocks Dyer's Hut"));
        effects.add(new ResearchEffect(ModBuildings.fletcher.getBuildingBlock()).setTranslatedName("Unlocks Fletcher's Hut"));
        effects.add(new ResearchEffect(ModBuildings.florist.getBuildingBlock()).setTranslatedName("Unlocks Flower Shop"));
        effects.add(new ResearchEffect(ModBuildings.glassblower.getBuildingBlock()).setTranslatedName("Unlocks Glassblower's Hut"));
        effects.add(new ResearchEffect(ModBuildings.hospital.getBuildingBlock()).setTranslatedName("Unlocks Hospital"));
        effects.add(new ResearchEffect(ModBuildings.library.getBuildingBlock()).setTranslatedName("Unlocks Library"));
        effects.add(new ResearchEffect(ModBuildings.mechanic.getBuildingBlock()).setTranslatedName("Unlocks Mechanic's Hut"));
        effects.add(new ResearchEffect(ModBuildings.mysticalSite.getBuildingBlock()).setTranslatedName("Unlocks Mystical Site"));
        effects.add(new ResearchEffect(ModBuildings.plantation.getBuildingBlock()).setTranslatedName("Unlocks Plantation"));
        effects.add(new ResearchEffect(ModBuildings.sawmill.getBuildingBlock()).setTranslatedName("Unlocks Sawmill"));
        effects.add(new ResearchEffect(ModBuildings.school.getBuildingBlock()).setTranslatedName("Unlocks School"));
        effects.add(new ResearchEffect(ModBuildings.sifter.getBuildingBlock()).setTranslatedName("Unlocks Sifter's Hut"));
        effects.add(new ResearchEffect(ModBuildings.smeltery.getBuildingBlock()).setTranslatedName("Unlocks Smeltery"));
        effects.add(new ResearchEffect(ModBuildings.stoneMason.getBuildingBlock()).setTranslatedName("Unlocks Stonemason's Hut"));
        effects.add(new ResearchEffect(ModBuildings.stoneSmelter.getBuildingBlock()).setTranslatedName("Unlocks Stone Smeltery"));

        // Crafter-recipe-only unlocks do not require static effect resource locations; the crafter recipe json checks against the research id resource locaiton itself.
        // Assigning them for now to handle text cleanly, and to allow researches with both recipe and non-recipe effects.
        effects.add(new ResearchEffect(new ResourceLocation(Constants.MOD_ID, "effects/knowledgeoftheendunlock")).setTranslatedName(
          "Stonemasons Learn Endstone Recipe and Bakers Learn Chorus Bread Recipe"));
        effects.add(new ResearchEffect(new ResourceLocation(Constants.MOD_ID, "effects/morescrollsunlock")).setTranslatedName(
          "Enchanter Learns Scroll Recipes to Locate Workers and Summon Guards"));
        effects.add(new ResearchEffect(new ResourceLocation(Constants.MOD_ID, "effects/platearmorunlock")).setTranslatedName("Blacksmith Learns Plate Armor Recipes"));

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

        return researches;
    }

    public Collection<Research> getCivilResearch(Collection<Research> r)
    {
        //Primary Research #1
        final Research stamina = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/stamina"), CIVIL).setTranslatedName("Stamina")
                                   .setOnlyChild()
                                   .setIcon(ModBlocks.blockHutHospital.asItem())
                                   .addItemCost(Items.CARROT, 1)
                                   .addEffect(ModBuildings.hospital.getBuildingBlock(), 1)
                                   .addToList(r);
        final Research bandAid = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/bandaid"), CIVIL).setParentResearch(stamina)
                                   .setTranslatedName("Band Aid")
                                   .setIcon(new ResourceLocation("minecolonies:textures/icons/research/regeneration1.png"))
                                   .addBuildingRequirement(ModBuildings.LIBRARY_ID, 2)
                                   .addItemCost(Items.GOLDEN_CARROT, 1)
                                   .addEffect(REGENERATION, 1)
                                   .addToList(r);
        final Research healingCream = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/healingcream"), CIVIL).setParentResearch(bandAid)
                                        .setTranslatedName("Healing Cream")
                                        .setTranslatedSubtitle("You missed a spot...")
                                        .setIcon(new ResourceLocation("minecolonies:textures/icons/research/regeneration2.png"))
                                        .addBuildingRequirement(ModBuildings.LIBRARY_ID, 3)
                                        .addItemCost(Items.GOLDEN_CARROT, 1)
                                        .addEffect(REGENERATION, 2)
                                        .addToList(r);
        final Research bandages = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/bandages"), CIVIL).setParentResearch(healingCream)
                                    .setTranslatedName("Bandages")
                                    .setIcon(new ResourceLocation("minecolonies:textures/icons/research/regeneration3.png"))
                                    .addBuildingRequirement(ModBuildings.LIBRARY_ID, 4)
                                    .addItemCost(Items.GOLDEN_CARROT, 16)
                                    .addEffect(REGENERATION, 3)
                                    .addToList(r);
        final Research compress = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/compress"), CIVIL).setParentResearch(bandages)
                                    .setTranslatedName("Compress")
                                    .setIcon(new ResourceLocation("minecolonies:textures/icons/research/regeneration4.png"))
                                    .addBuildingRequirement(ModBuildings.LIBRARY_ID, 5)
                                    .addItemCost(Items.GOLDEN_CARROT, 32)
                                    .addEffect(REGENERATION, 4)
                                    .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/cast"), CIVIL).setParentResearch(compress)
          .setTranslatedName("Cast")
          .setIcon(new ResourceLocation("minecolonies:textures/icons/research/regeneration5.png"))
          .addItemCost(Items.GOLDEN_CARROT, 64)
          .addEffect(REGENERATION, 5)
          .addToList(r);

        final Research resistance = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/resistance"), CIVIL).setParentResearch(stamina)
                                      .setTranslatedName("Resistance")
                                      .setSortOrder(2)
                                      .setIcon(Items.GLASS_BOTTLE)
                                      .addBuildingRequirement(ModBuildings.COOK_ID, 2)
                                      .addItemCost(Items.GOLDEN_APPLE, 1)
                                      .addEffect(SATLIMIT, 1)
                                      .addToList(r);
        final Research resilience = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/resilience"), CIVIL).setParentResearch(resistance)
                                      .setTranslatedName("Resilience")
                                      .setIcon(Items.POTION)
                                      .addBuildingRequirement(ModBuildings.COOK_ID, 3)
                                      .addItemCost(Items.GOLDEN_APPLE, 8)
                                      .addEffect(SATLIMIT, 2)
                                      .addToList(r);
        final Research vitality = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/vitality"), CIVIL).setParentResearch(resilience)
                                    .setTranslatedName("Vitality")
                                    .setIcon(Items.SPLASH_POTION)
                                    .addBuildingRequirement(ModBuildings.COOK_ID, 4)
                                    .addItemCost(Items.GOLDEN_APPLE, 16)
                                    .addEffect(SATLIMIT, 3)
                                    .addToList(r);
        final Research fortitude = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/fortitude"), CIVIL).setParentResearch(vitality)
                                     .setTranslatedName("Fortitude")
                                     .setIcon(Items.HONEY_BOTTLE)
                                     .addBuildingRequirement(ModBuildings.COOK_ID, 5)
                                     .addItemCost(Items.GOLDEN_APPLE, 32)
                                     .addEffect(SATLIMIT, 4)
                                     .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/indefatigability"), CIVIL).setParentResearch(fortitude)
          .setTranslatedName("Indefatigability")
          .setIcon(Items.EXPERIENCE_BOTTLE)
          .addItemCost(Items.GOLDEN_APPLE, 64)
          .addEffect(SATLIMIT, 5)
          .addToList(r);

        //Primary Research #2
        final Research keen = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/keen"), CIVIL).setTranslatedName("Keen")
                                .setSortOrder(2)
                                .setIcon(ModBlocks.blockHutLibrary.asItem())
                                .addBuildingRequirement(ModBuildings.HOME_ID, 3)
                                .addItemCost(Items.BOOK, 3)
                                .addEffect(ModBuildings.library.getBuildingBlock(), 1)
                                .addToList(r);
        final Research outpost = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/outpost"), CIVIL).setParentResearch(keen)
                                   .setTranslatedName("Outpost")
                                   .setIcon(ModBlocks.blockHutHome.asItem(), 50)
                                   .addBuildingRequirement(ModBuildings.HOME_ID, 4)
                                   .addItemCost(Items.COOKED_BEEF, 64)
                                   .addEffect(CITIZEN_CAP, 1)
                                   .addToList(r);
        final Research hamlet = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/hamlet"), CIVIL).setParentResearch(outpost)
                                  .setTranslatedName("Hamlet")
                                  .setIcon(ModBlocks.blockHutHome.asItem(), 75)
                                  .addBuildingRequirement(ModBuildings.HOME_ID, 5)
                                  .addItemCost(Items.COOKED_BEEF, 128)
                                  .addEffect(CITIZEN_CAP, 2)
                                  .addToList(r);
        final Research village = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/village"), CIVIL).setParentResearch(hamlet)
                                   .setTranslatedName("Village")
                                   .setIcon(ModBlocks.blockHutHome.asItem(), 100)
                                   .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 4)
                                   .addItemCost(Items.COOKED_BEEF, 256)
                                   .addEffect(CITIZEN_CAP, 3)
                                   .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/city"), CIVIL).setParentResearch(village)
          .setTranslatedName("City")
          .setIcon(ModBlocks.blockHutHome.asItem(), 200)
          .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 5)
          .addItemCost(Items.COOKED_BEEF, 512)
          .addEffect(CITIZEN_CAP, 4)
          .addToList(r);

        final Research diligent = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/diligent"), CIVIL).setParentResearch(keen)
                                    .setTranslatedName("Diligent")
                                    .setSortOrder(2)
                                    .setIcon(Items.EXPERIENCE_BOTTLE)
                                    .addBuildingRequirement(ModBuildings.LIBRARY_ID, 2)
                                    .addItemCost(Items.BOOK, 6)
                                    .addEffect(LEVELING, 1)
                                    .addToList(r);
        final Research studious = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/studious"), CIVIL).setParentResearch(diligent)
                                    .setTranslatedName("Studious")
                                    .setIcon(Items.EXPERIENCE_BOTTLE, 2)
                                    .addBuildingRequirement(ModBuildings.LIBRARY_ID, 3)
                                    .addItemCost(Items.BOOK, 12)
                                    .addEffect(LEVELING, 2)
                                    .addToList(r);
        final Research scholarly = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/scholarly"), CIVIL).setParentResearch(studious)
                                     .setTranslatedName("Scholarly")
                                     .setTranslatedSubtitle("Homework for the next decade... check!")
                                     .setIcon(Items.EXPERIENCE_BOTTLE, 3)
                                     .addBuildingRequirement(ModBuildings.LIBRARY_ID, 4)
                                     .addItemCost(Items.BOOK, 24)
                                     .addEffect(LEVELING, 3)
                                     .addToList(r);
        final Research reflective = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/reflective"), CIVIL).setParentResearch(scholarly)
                                      .setTranslatedName("Reflective")
                                      .setTranslatedSubtitle("Let me think about that for a moment.")
                                      .setIcon(Items.EXPERIENCE_BOTTLE, 4)
                                      .addBuildingRequirement(ModBuildings.LIBRARY_ID, 5)
                                      .addItemCost(Items.BOOK, 48)
                                      .addEffect(LEVELING, 4)
                                      .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/academic"), CIVIL).setParentResearch(reflective)
          .setTranslatedName("Academic")
          .setTranslatedSubtitle("Think about what you thought when you thought of what you will think now.")
          .setIcon(Items.EXPERIENCE_BOTTLE, 5)
          .addItemCost(Items.BOOK, 128)
          .addEffect(LEVELING, 5)
          .addToList(r);

        final Research rails = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/rails"), CIVIL).setParentResearch(keen)
                                 .setTranslatedName("Rails")
                                 .setTranslatedSubtitle("Research is progressing right on track.")
                                 .setSortOrder(3)
                                 .setIcon(Items.DETECTOR_RAIL)
                                 .addBuildingRequirement(ModBuildings.DELIVERYMAN_ID, 3)
                                 .addItemCost(Items.RAIL, 64)
                                 .addEffect(RAILS, 1)
                                 .addToList(r);
        final Research nimble = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/nimble"), CIVIL).setParentResearch(rails)
                                  .setTranslatedName("Nimble")
                                  .setTranslatedSubtitle("Not that we get time to exercise. It must be the morning commute.")
                                  .setIcon(new ResourceLocation("minecolonies:textures/icons/research/speed1.png"))
                                  .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 3)
                                  .addItemCost(Items.RABBIT_FOOT, 1)
                                  .addEffect(WALKING, 1)
                                  .addToList(r);
        final Research agile = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/agile"), CIVIL).setParentResearch(nimble)
                                 .setTranslatedName("Agile")
                                 .setTranslatedSubtitle("So this is how it feels to be young again...")
                                 .setIcon(new ResourceLocation("minecolonies:textures/icons/research/speed2.png"))
                                 .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 4)
                                 .addItemCost(Items.RABBIT_FOOT, 10)
                                 .addEffect(WALKING, 2)
                                 .addToList(r);
        final Research swift = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/swift"), CIVIL).setParentResearch(agile)
                                 .setTranslatedName("Swift")
                                 .setTranslatedSubtitle("They'll never see me coming.")
                                 .setIcon(new ResourceLocation("minecolonies:textures/icons/research/speed3.png"))
                                 .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 5)
                                 .addItemCost(Items.RABBIT_FOOT, 32)
                                 .addEffect(WALKING, 3)
                                 .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/athlete"), CIVIL).setParentResearch(swift)
          .setTranslatedName("Athlete")
          .setTranslatedSubtitle("Try thinking as fast as your feet now!")
          .setIcon(new ResourceLocation("minecolonies:textures/icons/research/speed4.png"))
          .addItemCost(Items.RABBIT_FOOT, 64)
          .addEffect(WALKING, 4)
          .addToList(r);

        // Primary Research #3
        final Research firstAid = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/firstaid"), CIVIL).setTranslatedName("First Aid")
                                    .setTranslatedSubtitle("First aid, second hand.")
                                    .setSortOrder(3)
                                    .setIcon(new ResourceLocation("minecolonies:textures/icons/research/hp1.png"))
                                    .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 1)
                                    .addItemCost(Items.HAY_BLOCK, 8)
                                    .addEffect(HEALTH_BOOST, 1)
                                    .addToList(r);
        final Research firstAid2 = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/firstaid2"), CIVIL).setParentResearch(firstAid)
                                     .setTranslatedName("First Aid II")
                                     .setTranslatedSubtitle("Second Aid?")
                                     .setIcon(new ResourceLocation("minecolonies:textures/icons/research/hp2.png"))
                                     .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 2)
                                     .addItemCost(Items.HAY_BLOCK, 16)
                                     .addEffect(HEALTH_BOOST, 2)
                                     .addToList(r);
        final Research lifesaver = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/lifesaver"), CIVIL).setParentResearch(firstAid2)
                                     .setTranslatedName("Lifesaver")
                                     .setIcon(new ResourceLocation("minecolonies:textures/icons/research/hp3.png"))
                                     .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 3)
                                     .addItemCost(Items.HAY_BLOCK, 32)
                                     .addEffect(HEALTH_BOOST, 3)
                                     .addToList(r);
        final Research lifesaver2 = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/lifesaver2"), CIVIL).setParentResearch(lifesaver)
                                      .setTranslatedName("Lifesaver II")
                                      .setIcon(new ResourceLocation("minecolonies:textures/icons/research/hp4.png"))
                                      .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 4)
                                      .addItemCost(Items.HAY_BLOCK, 64)
                                      .addEffect(HEALTH_BOOST, 4)
                                      .addToList(r);
        final Research guardianAngel = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/guardianangel"), CIVIL).setParentResearch(lifesaver2)
                                         .setTranslatedName("Guardian Angel")
                                         .setIcon(new ResourceLocation("minecolonies:textures/icons/research/hp5.png"))
                                         .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 5)
                                         .addItemCost(Items.HAY_BLOCK, 128)
                                         .addEffect(HEALTH_BOOST, 5)
                                         .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/guardianangel2"), CIVIL).setParentResearch(guardianAngel)
          .setTranslatedName("Guardian Angel II")
          .setIcon(new ResourceLocation("minecolonies:textures/icons/research/hp6.png"))
          .addItemCost(Items.HAY_BLOCK, 256)
          .addEffect(HEALTH_BOOST, 6)
          .addToList(r);

        final Research circus = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/circus"), CIVIL).setParentResearch(firstAid)
                                  .setTranslatedName("Circus")
                                  .setSortOrder(2)
                                  .setIcon(new ResourceLocation("minecolonies:textures/icons/research/happy1.png"))
                                  .addBuildingRequirement(ModBuildings.COOK_ID, 2)
                                  .addItemCost(Items.CAKE, 1)
                                  .addEffect(HAPPINESS, 1)
                                  .addToList(r);
        final Research festival = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/festival"), CIVIL).setParentResearch(circus)
                                    .setTranslatedName("Festival")
                                    .setTranslatedSubtitle("We Researchers may not be there, so don't look for us.")
                                    .setIcon(new ResourceLocation("minecolonies:textures/icons/research/happy2.png"))
                                    .addBuildingRequirement(ModBuildings.COOK_ID, 3)
                                    .addItemCost(Items.CAKE, 9)
                                    .addEffect(HAPPINESS, 2)
                                    .addToList(r);
        final Research spectacle = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/spectacle"), CIVIL).setParentResearch(festival)
                                     .setTranslatedName("Spectacle")
                                     .setIcon(new ResourceLocation("minecolonies:textures/icons/research/happy3.png"))
                                     .addBuildingRequirement(ModBuildings.COOK_ID, 4)
                                     .addItemCost(Items.CAKE, 18)
                                     .addEffect(HAPPINESS, 3)
                                     .addToList(r);
        final Research opera = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/opera"), CIVIL).setParentResearch(spectacle)
                                 .setTranslatedName("Opera")
                                 .setTranslatedSubtitle("Ear plugs not included.")
                                 .setIcon(new ResourceLocation("minecolonies:textures/icons/research/happy4.png"))
                                 .addBuildingRequirement(ModBuildings.COOK_ID, 5)
                                 .addItemCost(Items.CAKE, 27)
                                 .addEffect(HAPPINESS, 4)
                                 .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/theater"), CIVIL).setParentResearch(opera)
          .setTranslatedName("Theater")
          .setTranslatedSubtitle("Oh don't be so dramatic!")
          .setIcon(new ResourceLocation("minecolonies:textures/icons/research/happy5.png"))
          .addItemCost(Items.ENCHANTED_GOLDEN_APPLE, 16)
          .addEffect(HAPPINESS, 5)
          .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/nightowl"), CIVIL).setParentResearch(circus)
          .setTranslatedName("Night Owl")
          .setTranslatedSubtitle("Overtime penalty rates need not apply.")
          .setSortOrder(2)
          .setIcon(Items.CLOCK, 1)
          .addBuildingRequirement(ModBuildings.LIBRARY_ID, 2)
          .addItemCost(Items.GOLDEN_CARROT, 25)
          .addEffect(WORK_LONGER, 1)
          .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/nightowl2"), CIVIL).setParentResearch(festival)
          .setTranslatedName("Night Owl II")
          .setTranslatedSubtitle("Got any coffee?")
          .setSortOrder(2)
          .setIcon(Items.CLOCK, 2)
          .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 3)
          .addItemCost(Items.GOLDEN_CARROT, 75)
          .addEffect(WORK_LONGER, 2)
          .addToList(r);

        final Research gourmand = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/gourmand"), CIVIL).setParentResearch(firstAid)
                                    .setTranslatedName("Gourmand")
                                    .setSortOrder(3)
                                    .setIcon(new ResourceLocation("minecolonies:textures/icons/research/saturation1.png"))
                                    .addBuildingRequirement(ModBuildings.COOK_ID, 2)
                                    .addItemCost(Items.COOKIE, 32)
                                    .addEffect(SATURATION, 1)
                                    .addToList(r);
        final Research gorger = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/gorger"), CIVIL).setParentResearch(gourmand)
                                  .setTranslatedName("Gorger")
                                  .setTranslatedSubtitle("MORE!???")
                                  .setIcon(new ResourceLocation("minecolonies:textures/icons/research/saturation2.png"))
                                  .addBuildingRequirement(ModBuildings.COOK_ID, 3)
                                  .addItemCost(Items.COOKIE, 64)
                                  .addEffect(SATURATION, 2)
                                  .addToList(r);
        final Research stuffer = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/stuffer"), CIVIL).setParentResearch(gorger)
                                   .setTranslatedName("Stuffer")
                                   .setIcon(new ResourceLocation("minecolonies:textures/icons/research/saturation3.png"))
                                   .addBuildingRequirement(ModBuildings.COOK_ID, 4)
                                   .addItemCost(Items.COOKIE, 128)
                                   .addEffect(SATURATION, 3)
                                   .addToList(r);
        final Research epicure = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/epicure"), CIVIL).setParentResearch(stuffer)
                                   .setTranslatedName("Epicure")
                                   .setIcon(new ResourceLocation("minecolonies:textures/icons/research/saturation4.png"))
                                   .addBuildingRequirement(ModBuildings.COOK_ID, 5)
                                   .addItemCost(Items.COOKIE, 256)
                                   .addEffect(SATURATION, 4)
                                   .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/glutton"), CIVIL).setParentResearch(epicure)
          .setTranslatedName("Glutton")
          .setTranslatedSubtitle("I think I'm finally satisfied... so what's for next course?")
          .setIcon(new ResourceLocation("minecolonies:textures/icons/research/saturation5.png"))
          .addItemCost(Items.COOKIE, 512)
          .addEffect(SATURATION, 5)
          .addToList(r);

        // Primary Research #4.
        final Research higherLearning = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/higherlearning"), CIVIL).setTranslatedName("Higher Learning")
                                          .setTranslatedSubtitle("Well, we had to find something to occupy them.")
                                          .setSortOrder(4)
                                          .setOnlyChild()
                                          .setIcon(ModBlocks.blockHutSchool.asItem())
                                          .addBuildingRequirement("citizen", 3)
                                          .addItemCost(Items.BOOK, 3)
                                          .addEffect(ModBuildings.school.getBuildingBlock(), 1)
                                          .addToList(r);
        final Research moreBooks = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/morebooks"), CIVIL).setParentResearch(higherLearning)
                                     .setTranslatedName("More Books")
                                     .setTranslatedSubtitle("Of course I'm right, I read it in a book!")
                                     .setIcon(new ResourceLocation("minecolonies:textures/icons/research/xpgain1.png"))
                                     .addBuildingRequirement(ModBuildings.SCHOOL_ID, 1)
                                     .addItemCost(Items.BOOK, 6)
                                     .addEffect(TEACHING, 1)
                                     .addToList(r);
        final Research bookworm = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/bookworm"), CIVIL).setParentResearch(moreBooks)
                                    .setTranslatedName("Bookworm")
                                    .setTranslatedSubtitle("We all know the early bird gets the book!")
                                    .setIcon(new ResourceLocation("minecolonies:textures/icons/research/xpgain2.png"))
                                    .addBuildingRequirement(ModBuildings.SCHOOL_ID, 3)
                                    .addItemCost(Items.BOOKSHELF, 6)
                                    .addEffect(TEACHING, 2)
                                    .addToList(r);
        final Research bachelor = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/bachelor"), CIVIL).setParentResearch(bookworm)
                                    .setTranslatedName("Bachelor")
                                    .setTranslatedSubtitle("They now look like they know a lot, whether they do or not.")
                                    .setIcon(new ResourceLocation("minecolonies:textures/icons/research/xpgain3.png"))
                                    .addBuildingRequirement(ModBuildings.LIBRARY_ID, 3)
                                    .addItemCost(Items.BOOKSHELF, 12)
                                    .addEffect(TEACHING, 3)
                                    .addToList(r);
        final Research master = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/master"), CIVIL).setParentResearch(bachelor)
                                  .setTranslatedName("Master")
                                  .setTranslatedSubtitle("At least they get a fancy title this time.")
                                  .setIcon(new ResourceLocation("minecolonies:textures/icons/research/xpgain4.png"))
                                  .addBuildingRequirement(ModBuildings.LIBRARY_ID, 5)
                                  .addItemCost(Items.BOOKSHELF, 32)
                                  .addEffect(TEACHING, 4)
                                  .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/phd"), CIVIL).setParentResearch(master)
          .setTranslatedName("PhD")
          .setTranslatedSubtitle("Not that sort of doctor.")
          .setIcon(new ResourceLocation("minecolonies:textures/icons/research/xpgain5.png"))
          .addItemCost(Items.BOOKSHELF, 64)
          .addEffect(TEACHING, 5)
          .addToList(r);

        final Research nurture = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/nurture"), CIVIL).setParentResearch(higherLearning)
                                   .setTranslatedName("Nurture")
                                   .setTranslatedSubtitle("It's just part of our nature now.")
                                   .setSortOrder(2)
                                   .setIcon(new ResourceLocation("minecolonies:textures/icons/research/childgrowth1.png"))
                                   .addBuildingRequirement(ModBuildings.SCHOOL_ID, 1)
                                   .addItemCost(Items.COOKED_CHICKEN, 32)
                                   .addEffect(GROWTH, 1)
                                   .addToList(r);
        final Research hormones = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/hormones"), CIVIL).setParentResearch(nurture)
                                    .setTranslatedName("Hormones")
                                    .setTranslatedSubtitle("These are safe, right?")
                                    .setIcon(new ResourceLocation("minecolonies:textures/icons/research/childgrowth2.png"))
                                    .addBuildingRequirement(ModBuildings.SCHOOL_ID, 3)
                                    .addItemCost(Items.COOKED_CHICKEN, 64)
                                    .addEffect(GROWTH, 2)
                                    .addToList(r);
        final Research puberty = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/puberty"), CIVIL).setParentResearch(hormones)
                                   .setTranslatedName("Puberty")
                                   .setTranslatedSubtitle("My voice sounds weird...")
                                   .setIcon(new ResourceLocation("minecolonies:textures/icons/research/childgrowth3.png"))
                                   .addBuildingRequirement(ModBuildings.LIBRARY_ID, 3)
                                   .addItemCost(Items.COOKED_CHICKEN, 128)
                                   .addEffect(GROWTH, 3)
                                   .addToList(r);
        final Research growth = new Research(new ResourceLocation(Constants.MOD_ID, "civilian/growth"), CIVIL).setParentResearch(puberty)
                                  .setTranslatedName("Growth")
                                  .setIcon(new ResourceLocation("minecolonies:textures/icons/research/childgrowth4.png"))
                                  .addBuildingRequirement(ModBuildings.LIBRARY_ID, 5)
                                  .addItemCost(Items.COOKED_CHICKEN, 256)
                                  .addEffect(GROWTH, 4)
                                  .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/beanstalk"), CIVIL).setParentResearch(growth)
          .setTranslatedName("Beanstalk")
          .setTranslatedSubtitle("That's one heck of a growth spurt!")
          .setIcon(new ResourceLocation("minecolonies:textures/icons/research/childgrowth5.png"))
          .addItemCost(Items.COOKED_CHICKEN, 512)
          .addEffect(GROWTH, 5)
          .addToList(r);

        // Primary Research #5
        new Research(new ResourceLocation(Constants.MOD_ID, "civilian/ambition"), CIVIL).setTranslatedName("Ambition")
          .setSortOrder(5)
          .setIcon(ModBlocks.blockHutMysticalSite.asItem())
          .addItemCost(Items.DIAMOND, 1)
          .addEffect(ModBuildings.mysticalSite.getBuildingBlock(), 1)
          .addToList(r);
        return r;
    }

    public Collection<Research> getCombatResearch(Collection<Research> r)
    {
        // Primary Research # 1
        final Research accuracy = new Research(new ResourceLocation(Constants.MOD_ID, "combat/accuracy"), COMBAT).setTranslatedName("Accuracy")
                                    .setOnlyChild()
                                    .setIcon(Items.LIME_BED)
                                    .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 1)
                                    .addItemCost(Items.IRON_INGOT, 16)
                                    .addEffect(SLEEP_LESS, 1)
                                    .addToList(r);
        final Research quickDraw = new Research(new ResourceLocation(Constants.MOD_ID, "combat/quickdraw"), COMBAT).setParentResearch(accuracy)
                                     .setTranslatedName("Quick Draw")
                                     .setIcon(Items.WOODEN_SWORD)
                                     .addBuildingRequirement(ModBuildings.BARRACKS_ID, 3)
                                     .addItemCost(Items.IRON_BLOCK, 2)
                                     .addEffect(MELEE_DAMAGE, 1)
                                     .addToList(r);
        final Research powerAttack = new Research(new ResourceLocation(Constants.MOD_ID, "combat/powerattack"), COMBAT).setParentResearch(quickDraw)
                                       .setTranslatedName("Power Attack")
                                       .setIcon(Items.STONE_SWORD)
                                       .addBuildingRequirement(ModBuildings.COMBAT_ACADEMY_ID, 3)
                                       .addItemCost(Items.IRON_BLOCK, 4)
                                       .addEffect(MELEE_DAMAGE, 2)
                                       .addToList(r);
        final Research cleave = new Research(new ResourceLocation(Constants.MOD_ID, "combat/cleave"), COMBAT).setParentResearch(powerAttack)
                                  .setTranslatedName("Cleave")
                                  .setIcon(Items.IRON_SWORD)
                                  .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 10)
                                  .addItemCost(Items.IRON_BLOCK, 8)
                                  .addEffect(MELEE_DAMAGE, 3)
                                  .addToList(r);
        final Research mightyCleave = new Research(new ResourceLocation(Constants.MOD_ID, "combat/mightycleave"), COMBAT).setParentResearch(cleave)
                                        .setTranslatedName("Mighty Cleave")
                                        .setIcon(Items.GOLDEN_SWORD)
                                        .addBuildingRequirement(ModBuildings.BARRACKS_ID, 5)
                                        .addItemCost(Items.IRON_BLOCK, 16)
                                        .addEffect(MELEE_DAMAGE, 4)
                                        .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/savagestrike"), COMBAT).setParentResearch(mightyCleave)
          .setTranslatedName("Savage Strike")
          .setIcon(Items.DIAMOND_SWORD)
          .addItemCost(Items.IRON_BLOCK, 32)
          .addEffect(MELEE_DAMAGE, 5)
          .addToList(r);

        final Research preciseShot = new Research(new ResourceLocation(Constants.MOD_ID, "combat/preciseshot"), COMBAT).setParentResearch(accuracy)
                                       .setTranslatedName("Precise Shot")
                                       .setSortOrder(2)
                                       .setIcon(Items.ARROW)
                                       .addBuildingRequirement(ModBuildings.BARRACKS_ID, 3)
                                       .addItemCost(Items.FLINT, 16)
                                       .addEffect(ARCHER_DAMAGE, 1)
                                       .addToList(r);
        final Research penetratingShot = new Research(new ResourceLocation(Constants.MOD_ID, "combat/penetratingshot"), COMBAT).setParentResearch(preciseShot)
                                           .setTranslatedName("Penetrating Shot")
                                           .setIcon(Items.BOW)
                                           .addBuildingRequirement(ModBuildings.ARCHERY_ID, 3)
                                           .addItemCost(Items.FLINT, 32)
                                           .addEffect(ARCHER_DAMAGE, 2)
                                           .addToList(r);
        final Research piercingShot = new Research(new ResourceLocation(Constants.MOD_ID, "combat/piercingshot"), COMBAT).setParentResearch(penetratingShot)
                                        .setTranslatedName("Piercing Shot")
                                        .setIcon(Items.CROSSBOW)
                                        .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 10)
                                        .addItemCost(Items.FLINT, 64)
                                        .addEffect(ARCHER_DAMAGE, 3)
                                        .addToList(r);
        final Research woundingShot = new Research(new ResourceLocation(Constants.MOD_ID, "combat/woundingshot"), COMBAT).setParentResearch(piercingShot)
                                        .setTranslatedName("Wounding Shot")
                                        .setIcon(ModItems.firearrow)
                                        .addBuildingRequirement(ModBuildings.BARRACKS_ID, 5)
                                        .addItemCost(Items.FLINT, 128)
                                        .addEffect(ARCHER_DAMAGE, 4)
                                        .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/deadlyaim"), COMBAT).setParentResearch(woundingShot)
          .setTranslatedName("Deadly Aim")
          .setTranslatedSubtitle("Just don't aim at me!")
          .setIcon(Items.TIPPED_ARROW)
          .addItemCost(Items.FLINT, 256)
          .addEffect(ARCHER_DAMAGE, 5)
          .addToList(r);

        // Primary Research #2
        final Research tacticTraining = new Research(new ResourceLocation(Constants.MOD_ID, "combat/tactictraining"), COMBAT).setTranslatedName("Tactic Training")
                                          .setSortOrder(2)
                                          .setIcon(ModBlocks.blockHutBarracksTower.asItem())
                                          .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 3)
                                          .addItemCost(Items.IRON_BLOCK, 3)
                                          .addEffect(ModBuildings.barracks.getBuildingBlock(), 1)
                                          .addToList(r);
        final Research improvedSwords = new Research(new ResourceLocation(Constants.MOD_ID, "combat/improvedswords"), COMBAT).setParentResearch(tacticTraining)
                                          .setTranslatedName("Improved Swords")
                                          .setTranslatedSubtitle("Pointy end goes into the zombie.")
                                          .setIcon(ModBlocks.blockHutCombatAcademy.asItem())
                                          .addBuildingRequirement(ModBuildings.BARRACKS_ID, 3)
                                          .addItemCost(Items.IRON_BLOCK, 6)
                                          .addEffect(ModBuildings.combatAcademy.getBuildingBlock(), 1)
                                          .addToList(r);
        final Research squireTraining = new Research(new ResourceLocation(Constants.MOD_ID, "combat/squiretraining"), COMBAT).setParentResearch(improvedSwords)
                                          .setTranslatedName("Squire Training")
                                          .setTranslatedSubtitle("First lesson: how to mop the floors.")
                                          .setIcon(Items.IRON_INGOT)
                                          .addBuildingRequirement(ModBuildings.COMBAT_ACADEMY_ID, 3)
                                          .addItemCost(Items.SHIELD, 4)
                                          .addEffect(BLOCK_ATTACKS, 1)
                                          .addToList(r);
        final Research knightTraining = new Research(new ResourceLocation(Constants.MOD_ID, "combat/knighttraining"), COMBAT).setParentResearch(squireTraining)
                                          .setTranslatedName("Knight Training")
                                          .setIcon(Items.BARREL)
                                          .addBuildingRequirement(ModBuildings.COMBAT_ACADEMY_ID, 4)
                                          .addItemCost(Items.SHIELD, 8)
                                          .addEffect(BLOCK_ATTACKS, 2)
                                          .addToList(r);
        final Research captainTraining = new Research(new ResourceLocation(Constants.MOD_ID, "combat/captaintraining"), COMBAT).setParentResearch(knightTraining)
                                           .setTranslatedName("Captain Training")
                                           .setIcon(Items.IRON_BARS)
                                           .addBuildingRequirement(ModBuildings.COMBAT_ACADEMY_ID, 5)
                                           .addItemCost(Items.SHIELD, 16)
                                           .addEffect(BLOCK_ATTACKS, 3)
                                           .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/captainoftheguard"), COMBAT).setParentResearch(captainTraining)
          .setTranslatedName("Captain of the Guard")
          .setIcon(Items.IRON_BLOCK)
          .addItemCost(Items.SHIELD, 27)
          .addEffect(BLOCK_ATTACKS, 4)
          .addToList(r);

        final Research improvedBows = new Research(new ResourceLocation(Constants.MOD_ID, "combat/improvedbows"), COMBAT).setParentResearch(tacticTraining)
                                        .setTranslatedName("Improved Bows")
                                        .setTranslatedSubtitle("How far back can this bend before snapping?")
                                        .setSortOrder(2)
                                        .setIcon(ModBlocks.blockHutArchery.asItem())
                                        .addBuildingRequirement(ModBuildings.BARRACKS_ID, 3)
                                        .addItemCost(Items.IRON_BLOCK, 6)
                                        .addEffect(ModBuildings.archery.getBuildingBlock(), 1)
                                        .addToList(r);
        final Research trickShot = new Research(new ResourceLocation(Constants.MOD_ID, "combat/trickshot"), COMBAT).setParentResearch(improvedBows)
                                     .setTranslatedName("Trick Shot")
                                     .setIcon(Items.ARROW)
                                     .addBuildingRequirement(ModBuildings.ARCHERY_ID, 3)
                                     .addItemCost(Items.BOW, 5)
                                     .addEffect(DOUBLE_ARROWS, 1)
                                     .addToList(r);
        final Research multiShot = new Research(new ResourceLocation(Constants.MOD_ID, "combat/multishot"), COMBAT).setParentResearch(trickShot)
                                     .setTranslatedName("Multishot")
                                     .setTranslatedSubtitle("Known side effects include double vision double vision.")
                                     .setIcon(Items.TIPPED_ARROW)
                                     .addBuildingRequirement(ModBuildings.ARCHERY_ID, 4)
                                     .addItemCost(Items.BOW, 9)
                                     .addEffect(DOUBLE_ARROWS, 2)
                                     .addToList(r);
        final Research rapidShot = new Research(new ResourceLocation(Constants.MOD_ID, "combat/rapidshot"), COMBAT).setParentResearch(multiShot)
                                     .setTranslatedName("Rapid Shot")
                                     .setTranslatedSubtitle("Please leave the bow with more than you brought in.")
                                     .setIcon(ModItems.firearrow)
                                     .addBuildingRequirement(ModBuildings.ARCHERY_ID, 5)
                                     .addItemCost(Items.BOW, 18)
                                     .addEffect(DOUBLE_ARROWS, 3)
                                     .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/masterbowman"), COMBAT).setParentResearch(rapidShot)
          .setTranslatedName("Master Bowman")
          .setIcon(Items.BLAZE_ROD)
          .addItemCost(Items.BOW, 27)
          .addEffect(DOUBLE_ARROWS, 4)
          .addToList(r);

        // Primary Research #3
        final Research avoidance = new Research(new ResourceLocation(Constants.MOD_ID, "combat/avoidance"), COMBAT).setTranslatedName("Avoidance")
                                     .setSortOrder(3)
                                     .setOnlyChild()
                                     .setIcon(Items.SHIELD)
                                     .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 3)
                                     .addItemCost(Items.IRON_BLOCK, 3)
                                     .addEffect(SHIELD_USAGE, 1)
                                     .addToList(r);
        final Research parry = new Research(new ResourceLocation(Constants.MOD_ID, "combat/parry"), COMBAT).setParentResearch(avoidance)
                                 .setTranslatedName("Parry")
                                 .setIcon(Items.LEATHER_CHESTPLATE)
                                 .addBuildingRequirement(ModBuildings.SMELTERY_ID, 1)
                                 .addItemCost(Items.IRON_INGOT, 16)
                                 .addEffect(MELEE_ARMOR, 1)
                                 .addToList(r);
        final Research riposte = new Research(new ResourceLocation(Constants.MOD_ID, "combat/riposte"), COMBAT).setParentResearch(parry)
                                   .setTranslatedName("Riposte")
                                   .setTranslatedSubtitle("Oh yeah? Well, I, uh, um...")
                                   .setIcon(Items.IRON_CHESTPLATE)
                                   .addBuildingRequirement(ModBuildings.COMBAT_ACADEMY_ID, 1)
                                   .addItemCost(Items.IRON_INGOT, 32)
                                   .addEffect(MELEE_ARMOR, 2)
                                   .addToList(r);
        final Research duelist = new Research(new ResourceLocation(Constants.MOD_ID, "combat/duelist"), COMBAT).setParentResearch(riposte)
                                   .setTranslatedName("Duelist")
                                   .setIcon(Items.CHAINMAIL_CHESTPLATE)
                                   .addBuildingRequirement(ModBuildings.SMELTERY_ID, 3)
                                   .addItemCost(Items.IRON_INGOT, 64)
                                   .addEffect(MELEE_ARMOR, 3)
                                   .addToList(r);
        final Research provost = new Research(new ResourceLocation(Constants.MOD_ID, "combat/provost"), COMBAT).setParentResearch(duelist)
                                   .setTranslatedName("Provost")
                                   .setIcon(Items.GOLDEN_CHESTPLATE)
                                   .addBuildingRequirement(ModBuildings.COMBAT_ACADEMY_ID, 5)
                                   .addItemCost(Items.DIAMOND, 16)
                                   .addEffect(MELEE_ARMOR, 4)
                                   .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/masterswordsman"), COMBAT).setParentResearch(provost)
          .setTranslatedName("Master Swordsman")
          .setIcon(Items.DIAMOND_CHESTPLATE)
          .addItemCost(Items.DIAMOND, 64)
          .addEffect(MELEE_ARMOR, 5)
          .addToList(r);

        final Research dodge = new Research(new ResourceLocation(Constants.MOD_ID, "combat/dodge"), COMBAT).setParentResearch(avoidance)
                                 .setTranslatedName("Dodge")
                                 .setTranslatedSubtitle("If you can dodge a hammer...")
                                 .setSortOrder(2)
                                 .setIcon(ModItems.pirateBoots_2.asItem())
                                 .addBuildingRequirement(ModBuildings.SMELTERY_ID, 1)
                                 .addItemCost(Items.LEATHER, 16)
                                 .addEffect(ARCHER_ARMOR, 1)
                                 .addToList(r);
        final Research improvedDodge = new Research(new ResourceLocation(Constants.MOD_ID, "combat/improveddodge"), COMBAT).setParentResearch(dodge)
                                         .setTranslatedName("Improved Dodge")
                                         .setTranslatedSubtitle("Dip")
                                         .setIcon(ModItems.pirateBoots_1)
                                         .addBuildingRequirement(ModBuildings.ARCHERY_ID, 1)
                                         .addItemCost(Items.LEATHER, 32)
                                         .addEffect(ARCHER_ARMOR, 2)
                                         .addToList(r);
        final Research evasion = new Research(new ResourceLocation(Constants.MOD_ID, "combat/evasion"), COMBAT).setParentResearch(improvedDodge)
                                   .setTranslatedName("Evasion")
                                   .setTranslatedSubtitle("\"Duck!\" \"No, that's a goose.\"")
                                   .setIcon(Items.IRON_BOOTS)
                                   .addBuildingRequirement(ModBuildings.SMELTERY_ID, 3)
                                   .addItemCost(Items.LEATHER, 64)
                                   .addEffect(ARCHER_ARMOR, 3)
                                   .addToList(r);
        final Research improvedEvasion = new Research(new ResourceLocation(Constants.MOD_ID, "combat/improvedevasion"), COMBAT).setParentResearch(evasion)
                                           .setTranslatedName("Improved Evasion")
                                           .setTranslatedSubtitle("Dive")
                                           .setIcon(Items.GOLDEN_BOOTS)
                                           .addBuildingRequirement(ModBuildings.ARCHERY_ID, 5)
                                           .addItemCost(Items.DIAMOND, 16)
                                           .addEffect(ARCHER_ARMOR, 4)
                                           .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/agilearcher"), COMBAT).setParentResearch(improvedEvasion)
          .setTranslatedName("Agile Archer")
          .setTranslatedSubtitle("Dodge... Again!")
          .setIcon(Items.DIAMOND_BOOTS)
          .addItemCost(Items.DIAMOND, 64)
          .addEffect(ARCHER_ARMOR, 5)
          .addToList(r);

        // Primary Research #4
        final Research improvedLeather = new Research(new ResourceLocation(Constants.MOD_ID, "combat/improvedleather"), COMBAT).setTranslatedName("Improved Leather")
                                           .setTranslatedSubtitle("Becoming more like the real thing every day.")
                                           .setSortOrder(4)
                                           .setIcon(Items.LEATHER_HELMET)
                                           .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 1)
                                           .addItemCost(Items.LEATHER, 32)
                                           .addEffect(ARMOR_DURABILITY, 1)
                                           .addToList(r);
        final Research boiledLeather = new Research(new ResourceLocation(Constants.MOD_ID, "combat/boiledleather"), COMBAT).setParentResearch(improvedLeather)
                                         .setTranslatedName("Boiled Leather")
                                         .setTranslatedSubtitle("Extra leathery!")
                                         .setIcon(Items.TURTLE_HELMET)
                                         .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 2)
                                         .addItemCost(Items.LEATHER, 64)
                                         .addEffect(ARMOR_DURABILITY, 2)
                                         .addToList(r);
        final Research ironSkin = new Research(new ResourceLocation(Constants.MOD_ID, "combat/ironskin"), COMBAT).setParentResearch(boiledLeather)
                                    .setTranslatedName("Iron Skin")
                                    .setIcon(Items.CHAINMAIL_HELMET)
                                    .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 3)
                                    .addItemCost(Items.IRON_INGOT, 16)
                                    .addEffect(ARMOR_DURABILITY, 3)
                                    .addToList(r);
        final Research ironArmor = new Research(new ResourceLocation(Constants.MOD_ID, "combat/ironarmor"), COMBAT).setParentResearch(ironSkin)
                                     .setTranslatedName("Iron Armor")
                                     .setIcon(Items.IRON_HELMET)
                                     .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 4)
                                     .addItemCost(Items.IRON_INGOT, 32)
                                     .addEffect(ARMOR_DURABILITY, 4)
                                     .addToList(r);
        final Research steelArmor = new Research(new ResourceLocation(Constants.MOD_ID, "combat/steelarmor"), COMBAT).setParentResearch(ironArmor)
                                      .setTranslatedName("Steel Armor")
                                      .setSortOrder(1)
                                      .setIcon(Items.GOLDEN_HELMET)
                                      .addBuildingRequirement(ModBuildings.TOWNHALL_ID, 5)
                                      .addItemCost(Items.IRON_INGOT, 64)
                                      .addEffect(ARMOR_DURABILITY, 5)
                                      .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/platearmor"), COMBAT).setParentResearch(ironArmor)
                                     .setTranslatedName("Plate Armor")
                                     .setSortOrder(2)
                                     .setIcon(ModItems.plateArmorHelmet)
                                     .addMandatoryBuildingRequirement(ModBuildings.BLACKSMITH_ID, 4)
                                     .addItemCost(Items.IRON_INGOT, 32)
                                     .addEffect(PLATE_ARMOR, 1)
                                     .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/diamondskin"), COMBAT).setParentResearch(steelArmor)
          .setTranslatedName("Diamond Skin")
          .setIcon(Items.DIAMOND_HELMET)
          .addItemCost(Items.DIAMOND, 64)
          .addEffect(ARMOR_DURABILITY, 6)
          .addToList(r);

        final Research regeneration = new Research(new ResourceLocation(Constants.MOD_ID, "combat/regeneration"), COMBAT).setParentResearch(improvedLeather)
                                        .setTranslatedName("Regeneration")
                                        .setSortOrder(2)
                                        .setOnlyChild()
                                        .setIcon(Items.GLISTERING_MELON_SLICE)
                                        .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 2)
                                        .addItemCost(Items.EMERALD, 1)
                                        .addEffect(RETREAT, 1)
                                        .addToList(r);
        final Research feint = new Research(new ResourceLocation(Constants.MOD_ID, "combat/feint"), COMBAT).setParentResearch(regeneration)
                                 .setTranslatedName("Feint")
                                 .setIcon(Items.LEATHER_BOOTS)
                                 .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 4)
                                 .addItemCost(Items.EMERALD, 8)
                                 .addEffect(FLEEING_DAMAGE, 1)
                                 .addToList(r);
        final Research fear = new Research(new ResourceLocation(Constants.MOD_ID, "combat/fear"), COMBAT).setParentResearch(feint)
                                .setTranslatedName("Fear")
                                .setIcon(Items.IRON_BOOTS)
                                .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 8)
                                .addItemCost(Items.EMERALD, 16)
                                .addEffect(FLEEING_DAMAGE, 2)
                                .addToList(r);
        final Research retreat = new Research(new ResourceLocation(Constants.MOD_ID, "combat/retreat"), COMBAT).setParentResearch(fear)
                                   .setTranslatedName("Retreat")
                                   .setTranslatedSubtitle("For strategic purposes, I assure you.")
                                   .setIcon(Items.GOLDEN_BOOTS)
                                   .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 12)
                                   .addItemCost(Items.EMERALD, 32)
                                   .addEffect(FLEEING_DAMAGE, 3)
                                   .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/fullretreat"), COMBAT).setParentResearch(retreat)
          .setTranslatedName("Full Retreat")
          .setIcon(Items.DIAMOND_BOOTS)
          .addItemCost(Items.EMERALD, 64)
          .addEffect(FLEEING_DAMAGE, 4)
          .addToList(r);

        final Research avoid = new Research(new ResourceLocation(Constants.MOD_ID, "combat/avoid"), COMBAT).setParentResearch(regeneration)
                                 .setTranslatedName("Avoid")
                                 .setSortOrder(2)
                                 .setIcon(Items.FEATHER)
                                 .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 4)
                                 .addItemCost(Items.EMERALD, 8)
                                 .addEffect(FLEEING_SPEED, 1)
                                 .addToList(r);
        final Research evade = new Research(new ResourceLocation(Constants.MOD_ID, "combat/evade"), COMBAT).setParentResearch(avoid)
                                 .setTranslatedName("Evade")
                                 .setIcon(Items.FEATHER, 2)
                                 .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 8)
                                 .addItemCost(Items.EMERALD, 16)
                                 .addEffect(FLEEING_SPEED, 2)
                                 .addToList(r);
        final Research flee = new Research(new ResourceLocation(Constants.MOD_ID, "combat/flee"), COMBAT).setParentResearch(evade)
                                .setTranslatedName("Flee")
                                .setTranslatedSubtitle("Sometimes it's better just to run.")
                                .setIcon(Items.FEATHER, 3)
                                .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 12)
                                .addItemCost(Items.EMERALD, 32)
                                .addEffect(FLEEING_SPEED, 3)
                                .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/hotfoot"), COMBAT).setParentResearch(flee)
          .setTranslatedName("Hotfoot")
          .setIcon(Items.CHICKEN)
          .addItemCost(Items.EMERALD, 64)
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
            .addItemCost(Items.ROTTEN_FLESH, 8)
            .addItemCost(Items.BONE, 8)
            .addItemCost(Items.SPIDER_EYE, 8)
            .addEffect(KNIGHT_TAUNT, 1)
            .addToList(r);
        final Research arrowUse = new Research(new ResourceLocation(Constants.MOD_ID, "combat/arrowuse"), COMBAT).setParentResearch(taunt)
                                    .setTranslatedName("Consume Arrows")
                                    .setTranslatedSubtitle("They work better with ammo.")
                                    .setIcon(Items.ARROW)
                                    .addBuildingRequirement(ModBuildings.GUARD_TOWER_ID, 2)
                                    .addItemCost(Items.ARROW, 64)
                                    .addEffect(ARCHER_USE_ARROWS, 1)
                                    .addToList(r);
        final Research arrowPierce = new Research(new ResourceLocation(Constants.MOD_ID, "combat/arrowpierce"), COMBAT).setParentResearch(arrowUse)
                                       .setTranslatedName("Arrow Piercing")
                                       .setIcon(Items.ENCHANTED_BOOK)
                                       .addBuildingRequirement(ModBuildings.ARCHERY_ID, 1)
                                       .addItemCost(Items.ARROW, 64)
                                       .addItemCost(Items.REDSTONE, 64)
                                       .addEffect(ARROW_PIERCE, 1)
                                       .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "combat/whirlwind"), COMBAT).setParentResearch(arrowPierce)
          .setTranslatedName("Whirlwind")
          .setIcon(ModItems.scimitar)
          .addBuildingRequirement(ModBuildings.BARRACKS_ID, 4)
          .addItemCost(Items.REDSTONE, 64)
          .addItemCost(Items.GOLD_INGOT, 64)
          .addItemCost(Items.LAPIS_LAZULI, 128)
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
                                         .addItemCost(Items.BONE_MEAL, 64)
                                         .addEffect(ModBuildings.composter.getBuildingBlock(), 1)
                                         .addToList(r);
        final Research flowerPower = new Research(new ResourceLocation(Constants.MOD_ID, "technology/flowerpower"), TECH).setParentResearch(biodegradable)
                                       .setTranslatedName("Flower Power")
                                       .setIcon(ModBlocks.blockHutFlorist.asItem())
                                       .addBuildingRequirement(ModBuildings.COMPOSTER_ID, 3)
                                       .addItemCost(ModItems.compost, 64)
                                       .addEffect(ModBuildings.florist.getBuildingBlock(), 1)
                                       .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/rainbowheaven"), TECH).setParentResearch(flowerPower)
          .setTranslatedName("Rainbow Heaven")
          .setTranslatedSubtitle("Now in color! And 3D!")
          .setIcon(ModBlocks.blockHutComposter.asItem())
          .addBuildingRequirement(ModBuildings.FLORIST_ID, 3)
          .addItemCost(Items.POPPY, 64)
          .addEffect(ModBuildings.dyer.getBuildingBlock(), 1)
          .addToList(r);

        final Research letItGrow = new Research(new ResourceLocation(Constants.MOD_ID, "technology/letitgrow"), TECH).setParentResearch(biodegradable)
                                     .setTranslatedName("Let It Grow")
                                     .setTranslatedSubtitle("Just one tiny seed is all we really need.")
                                     .setSortOrder(2)
                                     .setIcon(ModBlocks.blockHutPlantation.asItem())
                                     .addBuildingRequirement(ModBuildings.FARMER_ID, 3)
                                     .addItemCost(ModItems.compost, 16)
                                     .addEffect(ModBuildings.plantation.getBuildingBlock(), 1)
                                     .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/doubletrouble"), TECH).setParentResearch(letItGrow)
          .setTranslatedName("Double Trouble")
          .setTranslatedSubtitle("Double Trouble")
          .setIcon(Items.GREEN_DYE)
          .addBuildingRequirement("plantation", 3)
          .addItemCost(Items.BAMBOO, 64)
          .addItemCost(Items.SUGAR_CANE, 64)
          .addItemCost(Items.CACTUS, 64)
          .addEffect(PLANT_2, 1)
          .addToList(r);

        final Research bonemeal = new Research(new ResourceLocation(Constants.MOD_ID, "technology/bonemeal"), TECH).setParentResearch(biodegradable)
                                    .setTranslatedName("Bonemeal")
                                    .setTranslatedSubtitle("And to think this stuff feeds our plants...")
                                    .setSortOrder(3)
                                    .setIcon(Items.WHEAT_SEEDS)
                                    .addBuildingRequirement(ModBuildings.MINER_ID, 3)
                                    .addItemCost(Items.WHEAT_SEEDS, 64)
                                    .addEffect(FARMING, 1)
                                    .addToList(r);
        final Research dung = new Research(new ResourceLocation(Constants.MOD_ID, "technology/dung"), TECH).setParentResearch(bonemeal)
                                .setTranslatedName("Dung")
                                .setTranslatedSubtitle("Fresh or not, here it comes!")
                                .setIcon(Items.BONE_MEAL)
                                .addBuildingRequirement(ModBuildings.MINER_ID, 4)
                                .addItemCost(Items.WHEAT_SEEDS, 128)
                                .addEffect(FARMING, 2)
                                .addToList(r);
        final Research compost = new Research(new ResourceLocation(Constants.MOD_ID, "technology/compost"), TECH).setParentResearch(dung)
                                   .setTranslatedName("Compost")
                                   .setIcon(Items.BONE)
                                   .addBuildingRequirement(ModBuildings.MINER_ID, 5)
                                   .addItemCost(Items.WHEAT_SEEDS, 256)
                                   .addEffect(FARMING, 3)
                                   .addToList(r);
        final Research fertilizer = new Research(new ResourceLocation(Constants.MOD_ID, "technology/fertilizer"), TECH).setParentResearch(compost)
                                      .setTranslatedName("Fertilizer")
                                      .setTranslatedSubtitle("Ah, that's the stuff!")
                                      .setIcon(Items.BONE_BLOCK)
                                      .addBuildingRequirement(ModBuildings.SMELTERY_ID, 3)
                                      .addItemCost(Items.WHEAT_SEEDS, 512)
                                      .addEffect(FARMING, 4)
                                      .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/magiccompost"), TECH).setParentResearch(fertilizer)
          .setTranslatedName("Magic Compost")
          .setIcon(ModBlocks.blockBarrel.asItem())
          .addItemCost(Items.WHEAT_SEEDS, 2048)
          .addEffect(FARMING, 5)
          .addToList(r);

        // Primary Research #2
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/morescrolls"), TECH).setTranslatedName("More Scrolls")
          .setSortOrder(2)
          .setIcon(ModItems.scrollHighLight)
          .addBuildingRequirement("enchanter", 3)
          .addItemCost(Items.PAPER, 64)
          .addItemCost(ModItems.ancientTome, 1)
          .addItemCost(Items.LAPIS_LAZULI, 64)
          .addEffect(new ResourceLocation("minecolonies:effects/morescrollsunlock"), 1)
          .addToList(r);

        // Primary Research #3
        final Research stoneCake = new Research(new ResourceLocation(Constants.MOD_ID, "technology/stonecake"), TECH).setTranslatedName("Stone Cake")
                                     .setTranslatedSubtitle("Don't break a tooth!")
                                     .setSortOrder(3)
                                     .setIcon(ModBlocks.blockHutStonemason.asItem())
                                     .addBuildingRequirement(ModBuildings.MINER_ID, 3)
                                     .addItemCost(Items.CHISELED_STONE_BRICKS, 64)
                                     .addEffect(ModBuildings.stoneMason.getBuildingBlock(), 1)
                                     .addToList(r);
        final Research rockingRoll = new Research(new ResourceLocation(Constants.MOD_ID, "technology/rockingroll"), TECH).setParentResearch(stoneCake)
                                       .setTranslatedName("Rocking Roll")
                                       .setIcon(ModBlocks.blockHutCrusher.asItem())
                                       .addBuildingRequirement("stonemason", 1)
                                       .addItemCost(Items.STONE, 64)
                                       .addEffect(ModBuildings.crusher.getBuildingBlock(), 1)
                                       .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/gildedhammer"), TECH).setParentResearch(rockingRoll)
          .setTranslatedName("Gilded Hammer")
          .setTranslatedSubtitle("When in doubt, cover in shiny stuff.")
          .setIcon(Items.GOLD_BLOCK)
          .addBuildingRequirement("crusher", 3)
          .addItemCost(Items.GRAVEL, 64)
          .addItemCost(Items.SAND, 64)
          .addItemCost(Items.CLAY, 64)
          .addEffect(CRUSHING_11, 1)
          .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "technology/pavetheroad"), TECH).setParentResearch(rockingRoll)
          .setTranslatedName("Pave the Road")
          .setTranslatedSubtitle("Not something you want to get mixed up in.")
          .setSortOrder(2)
          .setIcon(ModBlocks.blockHutConcreteMixer.asItem())
          .addBuildingRequirement("crusher", 1)
          .addItemCost(Items.WHITE_CONCRETE, 32)
          .addEffect(ModBuildings.concreteMixer.getBuildingBlock(), 1)
          .addToList(r);

        // Primary Research #4
        final Research woodwork = new Research(new ResourceLocation(Constants.MOD_ID, "technology/woodwork"), TECH).setTranslatedName("Woodwork")
                                    .setTranslatedSubtitle("Where oh where would a wood worker work if a wood worker would work wood?")
                                    .setSortOrder(4)
                                    .setIcon(ModBlocks.blockHutSawmill.asItem())
                                    .addBuildingRequirement("lumberjack", 3)
                                    .addItemCost(Items.OAK_PLANKS, 64)
                                    .addEffect(ModBuildings.sawmill.getBuildingBlock(), 1)
                                    .addToList(r);
        final Research stringWork = new Research(new ResourceLocation(Constants.MOD_ID, "technology/stringwork"), TECH).setParentResearch(woodwork)
                                      .setTranslatedName("Stringwork")
                                      .setIcon(ModBlocks.blockHutFletcher.asItem())
                                      .addBuildingRequirement(ModBuildings.SAWMILL_ID, 1)
                                      .addItemCost(Items.STRING, 16)
                                      .addEffect(ModBuildings.fletcher.getBuildingBlock(), 1)
                                      .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/hotboots"), TECH).setParentResearch(stringWork)
          .setTranslatedName("Hot Boots")
          .setTranslatedSubtitle("Warmer on the outside.")
          .setIcon(Items.CAMPFIRE)
          .addBuildingRequirement(ModBuildings.FLETCHER_ID, 1)
          .addItemCost(Items.LEATHER, 32)
          .addItemCost(Items.IRON_INGOT, 16)
          .addEffect(FIRE_RES, 1)
          .addToList(r);

        final Research sieving = new Research(new ResourceLocation(Constants.MOD_ID, "technology/sieving"), TECH).setParentResearch(woodwork)
                                   .setTranslatedName("Sieving")
                                   .setTranslatedSubtitle("How did that get in there?")
                                   .setSortOrder(2)
                                   .setIcon(ModBlocks.blockHutSifter.asItem())
                                   .addBuildingRequirement(ModBuildings.FISHERMAN_ID, 3)
                                   .addItemCost(Items.STRING, 64)
                                   .addEffect(ModBuildings.sifter.getBuildingBlock(), 1)
                                   .addToList(r);
        final Research space = new Research(new ResourceLocation(Constants.MOD_ID, "technology/space"), TECH).setParentResearch(sieving)
                                 .setTranslatedName("Space")
                                 .setTranslatedSubtitle("Antidisinterdimensionalitarianism!")
                                 .setIcon(Items.CHEST)
                                 .addBuildingRequirement(ModBuildings.MINER_ID, 3)
                                 .addItemCost(ModBlocks.blockRack.asItem(), 16)
                                 .addEffect(MINIMUM_STOCK, 1)
                                 .addToList(r);
        final Research capacity = new Research(new ResourceLocation(Constants.MOD_ID, "technology/capacity"), TECH).setParentResearch(space)
                                    .setTranslatedName("Capacity")
                                    .setTranslatedSubtitle("Don't ask how we fit it all.")
                                    .setIcon(Items.CHEST_MINECART)
                                    .addBuildingRequirement(ModBuildings.MINER_ID, 4)
                                    .addItemCost(ModBlocks.blockRack.asItem(), 32)
                                    .addEffect(MINIMUM_STOCK, 2)
                                    .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/fullstock"), TECH).setParentResearch(capacity)
          .setTranslatedName("Full Stock!")
          .setTranslatedSubtitle("We might be able to squeeze in one more.")
          .setIcon(Items.ENDER_CHEST)
          .addBuildingRequirement(ModBuildings.MINER_ID, 5)
          .addItemCost(ModBlocks.blockRack.asItem(), 64)
          .addEffect(MINIMUM_STOCK, 3)
          .addToList(r);

        final Research memoryAid = new Research(new ResourceLocation(Constants.MOD_ID, "technology/memoryaid"), TECH).setParentResearch(woodwork)
                                     .setTranslatedName("Memory Aid")
                                     .setTranslatedSubtitle("It's the thought that counts.")
                                     .setSortOrder(3)
                                     .setIcon(Items.PAPER)
                                     .addBuildingRequirement(ModBuildings.SAWMILL_ID, 1)
                                     .addItemCost(Items.PAPER, 32)
                                     .addEffect(RECIPES, 1)
                                     .addToList(r);
        final Research cheatSheet = new Research(new ResourceLocation(Constants.MOD_ID, "technology/cheatsheet"), TECH).setParentResearch(memoryAid)
                                      .setTranslatedName("Cheat Sheet")
                                      .setTranslatedSubtitle("So THAT's what I should be making!")
                                      .setIcon(Items.BOOK)
                                      .addBuildingRequirement(ModBuildings.SAWMILL_ID, 2)
                                      .addItemCost(Items.PAPER, 64)
                                      .addEffect(RECIPES, 2)
                                      .addToList(r);
        final Research recipeBook = new Research(new ResourceLocation(Constants.MOD_ID, "technology/recipebook"), TECH).setParentResearch(cheatSheet)
                                      .setTranslatedName("Recipe Book")
                                      .setIcon(Items.ENCHANTED_BOOK)
                                      .addBuildingRequirement(ModBuildings.SAWMILL_ID, 3)
                                      .addItemCost(Items.PAPER, 128)
                                      .addEffect(RECIPES, 3)
                                      .addToList(r);
        final Research rtm = new Research(new ResourceLocation(Constants.MOD_ID, "technology/rtm"), TECH).setParentResearch(recipeBook)
                               .setTranslatedName("RTM")
                               .setTranslatedSubtitle("I saw some information on this somewhere...")
                               .setIcon(Items.BOOKSHELF)
                               .addBuildingRequirement(ModBuildings.SAWMILL_ID, 4)
                               .addItemCost(Items.PAPER, 256)
                               .addEffect(RECIPES, 4)
                               .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/rainman"), TECH).setParentResearch(rtm)
          .setTranslatedName("Rainman")
          .setTranslatedSubtitle("Raindrops are falling on my head...")
          .setIcon(Items.SPLASH_POTION)
          .addItemCost(Items.SALMON_BUCKET, 27)
          .addEffect(WORKING_IN_RAIN, 1)
          .addToList(r);

        final Research deepPockets = new Research(new ResourceLocation(Constants.MOD_ID, "technology/deeppockets"), TECH).setParentResearch(cheatSheet)
                                       .setTranslatedName("Deep Pockets")
                                       .setSortOrder(2)
                                       .setIcon(Items.PINK_SHULKER_BOX)
                                       .addBuildingRequirement(ModBuildings.LIBRARY_ID, 4)
                                       .addItemCost(Items.EMERALD, 64)
                                       .addEffect(CITIZEN_INV_SLOTS, 1)
                                       .addToList(r);
        final Research loaded = new Research(new ResourceLocation(Constants.MOD_ID, "technology/loaded"), TECH).setParentResearch(deepPockets)
                                  .setTranslatedName("Loaded")
                                  .setIcon(Items.RED_SHULKER_BOX)
                                  .addBuildingRequirement(ModBuildings.LIBRARY_ID, 5)
                                  .addItemCost(Items.EMERALD, 128)
                                  .addEffect(CITIZEN_INV_SLOTS, 2)
                                  .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/heavilyloaded"), TECH).setParentResearch(loaded)
          .setTranslatedName("Heavily Loaded")
          .setIcon(Items.BLUE_SHULKER_BOX)
          .addItemCost(Items.EMERALD, 256)
          .addEffect(CITIZEN_INV_SLOTS, 3)
          .addToList(r);

        // Primary Research #5
        final Research hot = new Research(new ResourceLocation(Constants.MOD_ID, "technology/hot"), TECH).setTranslatedName("Hot!")
                               .setSortOrder(5)
                               .setIcon(ModBlocks.blockHutSmeltery.asItem())
                               .addBuildingRequirement(ModBuildings.MINER_ID, 2)
                               .addItemCost(Items.LAVA_BUCKET, 4)
                               .addEffect(ModBuildings.smeltery.getBuildingBlock(), 1)
                               .addToList(r);
        final Research isThisRedstone = new Research(new ResourceLocation(Constants.MOD_ID, "technology/isthisredstone"), TECH).setParentResearch(hot)
                                          .setTranslatedName("Is This Redstone?")
                                          .setIcon(Items.REDSTONE)
                                          .addItemCost(Items.REDSTONE, 128)
                                          .addEffect(BLOCK_BREAK_SPEED, 1)
                                          .addToList(r);
        final Research redstonePowered = new Research(new ResourceLocation(Constants.MOD_ID, "technology/redstonepowered"), TECH).setParentResearch(isThisRedstone)
                                           .setTranslatedName("Redstone Powered")
                                           .setTranslatedSubtitle("Like magic, but SCIENCE!")
                                           .setIcon(Items.REDSTONE_TORCH)
                                           .addItemCost(Items.REDSTONE, 256)
                                           .addEffect(BLOCK_BREAK_SPEED, 2)
                                           .addToList(r);
        final Research heavyMachinery = new Research(new ResourceLocation(Constants.MOD_ID, "technology/heavymachinery"), TECH).setParentResearch(redstonePowered)
                                          .setTranslatedName("Heavy Machinery")
                                          .setIcon(Items.REDSTONE_BLOCK)
                                          .addItemCost(Items.REDSTONE, 512)
                                          .addEffect(BLOCK_BREAK_SPEED, 3)
                                          .addToList(r);
        final Research whatIsThisSpeed = new Research(new ResourceLocation(Constants.MOD_ID, "technology/whatisthisspeed"), TECH).setParentResearch(heavyMachinery)
                                           .setTranslatedName("What Is This Speed?")
                                           .setTranslatedSubtitle("We stopped trying to calculate it after a while.")
                                           .setIcon(Items.COMPARATOR)
                                           .addItemCost(Items.REDSTONE, 1024)
                                           .addEffect(BLOCK_BREAK_SPEED, 4)
                                           .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/lightning"), TECH).setParentResearch(whatIsThisSpeed)
          .setTranslatedName("Lightning")
          .setTranslatedSubtitle("BAM! And the block is gone!")
          .setIcon(Items.REPEATER)
          .addItemCost(Items.REDSTONE, 2048)
          .addEffect(BLOCK_BREAK_SPEED, 5)
          .addToList(r);

        final Research theFlintstones = new Research(new ResourceLocation(Constants.MOD_ID, "technology/theflintstones"), TECH).setParentResearch(hot)
                                          .setTranslatedName("The Flintstones")
                                          .setTranslatedSubtitle("Yabba Dabba Doo!")
                                          .setIcon(ModBlocks.blockHutStoneSmeltery.asItem())
                                          .addBuildingRequirement(ModBuildings.SMELTERY_ID, 3)
                                          .addItemCost(Items.STONE_BRICKS, 64)
                                          .addEffect(ModBuildings.stoneSmelter.getBuildingBlock(), 1)
                                          .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/knowtheend"), TECH).setParentResearch(theFlintstones)
          .setTranslatedName("Know the End")
          .setTranslatedSubtitle("Unlock the secrets of the most mysterious dimension.")
          .setIcon(ModItems.chorusBread)
          .addBuildingRequirement("baker", 3)
          .addItemCost(Items.CHORUS_FRUIT, 64)
          .addEffect(new ResourceLocation("minecolonies:effects/knowledgeoftheendunlock"), 1)
          .addToList(r);

        new Research(new ResourceLocation(Constants.MOD_ID, "technology/thoselungs"), TECH).setParentResearch(hot)
          .setTranslatedName("Those Lungs!")
          .setTranslatedSubtitle("You'll definitely be needing those in some form.")
          .setIcon(ModBlocks.blockHutGlassblower.asItem())
          .addBuildingRequirement(ModBuildings.SMELTERY_ID, 3)
          .addItemCost(Items.GLASS, 64)
          .addEffect(ModBuildings.glassblower.getBuildingBlock(), 1)
          .addToList(r);

        // Primary Research #6
        final Research hittingIron = new Research(new ResourceLocation(Constants.MOD_ID, "technology/hittingiron"), TECH).setTranslatedName("Hitting Iron!")
                                       .setTranslatedSubtitle("We're still ironing out the details.")
                                       .setIcon(ModBlocks.blockHutBlacksmith.asItem())
                                       .addBuildingRequirement(ModBuildings.MINER_ID, 3)
                                       .addItemCost(Items.ANVIL, 1)
                                       .addEffect(ModBuildings.blacksmith.getBuildingBlock(), 1)
                                       .addToList(r);
        final Research strong = new Research(new ResourceLocation(Constants.MOD_ID, "technology/strong"), TECH).setParentResearch(hittingIron)
                                  .setTranslatedName("Strong")
                                  .setIcon(Items.WOODEN_PICKAXE)
                                  .addBuildingRequirement(ModBuildings.BLACKSMITH_ID, 1)
                                  .addItemCost(Items.DIAMOND, 8)
                                  .addEffect(TOOL_DURABILITY, 1)
                                  .addToList(r);
        final Research hardened = new Research(new ResourceLocation(Constants.MOD_ID, "technology/hardened"), TECH).setParentResearch(strong)
                                    .setTranslatedName("Hardened")
                                    .setIcon(Items.STONE_PICKAXE)
                                    .addBuildingRequirement(ModBuildings.BLACKSMITH_ID, 2)
                                    .addItemCost(Items.DIAMOND, 16)
                                    .addEffect(TOOL_DURABILITY, 2)
                                    .addToList(r);
        final Research reinforced = new Research(new ResourceLocation(Constants.MOD_ID, "technology/reinforced"), TECH).setParentResearch(hardened)
                                      .setTranslatedName("Reinforced")
                                      .setIcon(Items.IRON_PICKAXE)
                                      .addBuildingRequirement(ModBuildings.BLACKSMITH_ID, 3)
                                      .addItemCost(Items.DIAMOND, 32)
                                      .addEffect(TOOL_DURABILITY, 3)
                                      .addToList(r);
        final Research steelBracing = new Research(new ResourceLocation(Constants.MOD_ID, "technology/steelbracing"), TECH).setParentResearch(reinforced)
                                        .setTranslatedName("Steel Bracing")
                                        .setIcon(Items.GOLDEN_PICKAXE)
                                        .addBuildingRequirement(ModBuildings.BLACKSMITH_ID, 5)
                                        .addItemCost(Items.DIAMOND, 64)
                                        .addEffect(TOOL_DURABILITY, 4)
                                        .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/diamondcoated"), TECH).setParentResearch(steelBracing)
          .setTranslatedName("Diamond Coated")
          .setIcon(Items.DIAMOND_PICKAXE)
          .addItemCost(Items.DIAMOND, 128)
          .addEffect(TOOL_DURABILITY, 5)
          .addToList(r);

        final Research ability = new Research(new ResourceLocation(Constants.MOD_ID, "technology/ability"), TECH).setParentResearch(hittingIron)
                                   .setTranslatedName("Ability")
                                   .setIcon(Items.GLOWSTONE_DUST)
                                   .addBuildingRequirement(ModBuildings.MINER_ID, 1)
                                   .addItemCost(Items.IRON_INGOT, 64)
                                   .addEffect(BLOCK_PLACE_SPEED, 1)
                                   .addToList(r);
        final Research skills = new Research(new ResourceLocation(Constants.MOD_ID, "technology/skills"), TECH).setParentResearch(ability)
                                  .setTranslatedName("Skills")
                                  .setTranslatedSubtitle("Everything in its place.")
                                  .setIcon(Items.GLOWSTONE)
                                  .addBuildingRequirement(ModBuildings.MINER_ID, 2)
                                  .addItemCost(Items.IRON_INGOT, 128)
                                  .addEffect(BLOCK_PLACE_SPEED, 2)
                                  .addToList(r);
        final Research tools = new Research(new ResourceLocation(Constants.MOD_ID, "technology/tools"), TECH).setParentResearch(skills)
                                 .setTranslatedName("Tools")
                                 .setTranslatedSubtitle("Like breaking stuff, but in reverse!")
                                 .setIcon(Items.REDSTONE_LAMP)
                                 .addBuildingRequirement(ModBuildings.BLACKSMITH_ID, 4)
                                 .addItemCost(Items.IRON_INGOT, 256)
                                 .addEffect(BLOCK_PLACE_SPEED, 3)
                                 .addToList(r);
        final Research seemsAutomatic = new Research(new ResourceLocation(Constants.MOD_ID, "technology/seemsautomatic"), TECH).setParentResearch(tools)
                                          .setTranslatedName("Seems Automatic")
                                          .setTranslatedSubtitle("It all happened so fast...")
                                          .setIcon(Items.BLAZE_POWDER)
                                          .addBuildingRequirement(ModBuildings.BLACKSMITH_ID, 5)
                                          .addItemCost(Items.IRON_INGOT, 512)
                                          .addEffect(BLOCK_PLACE_SPEED, 4)
                                          .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/madness"), TECH).setParentResearch(seemsAutomatic)
          .setTranslatedName("Madness!")
          .setIcon(Items.SPECTRAL_ARROW)
          .addItemCost(Items.IRON_INGOT, 1024)
          .addEffect(BLOCK_PLACE_SPEED, 5)
          .addToList(r);

        final Research veinminer = new Research(new ResourceLocation(Constants.MOD_ID, "technology/veinminer"), TECH).setParentResearch(hittingIron)
                                     .setTranslatedName("Veinminer")
                                     .setIcon(Items.IRON_BLOCK)
                                     .addBuildingRequirement(ModBuildings.MINER_ID, 1)
                                     .addItemCost(Items.IRON_ORE, 32)
                                     .addEffect(MORE_ORES, 1)
                                     .addToList(r);
        final Research goodVeins = new Research(new ResourceLocation(Constants.MOD_ID, "technology/goodveins"), TECH).setParentResearch(veinminer)
                                     .setTranslatedName("Good Veins")
                                     .setIcon(Items.COAL_BLOCK)
                                     .addBuildingRequirement(ModBuildings.MINER_ID, 2)
                                     .addItemCost(Items.IRON_ORE, 64)
                                     .addEffect(MORE_ORES, 2)
                                     .addToList(r);
        final Research richVeins = new Research(new ResourceLocation(Constants.MOD_ID, "technology/richveins"), TECH).setParentResearch(goodVeins)
                                     .setTranslatedName("Rich Veins")
                                     .setIcon(Items.GOLD_BLOCK)
                                     .addBuildingRequirement(ModBuildings.BLACKSMITH_ID, 4)
                                     .addItemCost(Items.GOLD_ORE, 32)
                                     .addEffect(MORE_ORES, 3)
                                     .addToList(r);
        final Research amazingVeins = new Research(new ResourceLocation(Constants.MOD_ID, "technology/amazingveins"), TECH).setParentResearch(richVeins)
                                        .setTranslatedName("Amazing Veins")
                                        .setIcon(Items.LAPIS_BLOCK)
                                        .addBuildingRequirement(ModBuildings.BLACKSMITH_ID, 5)
                                        .addItemCost(Items.GOLD_ORE, 64)
                                        .addEffect(MORE_ORES, 4)
                                        .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/motherlode"), TECH).setParentResearch(amazingVeins)
          .setTranslatedName("Motherlode")
          .setIcon(Items.DIAMOND_BLOCK)
          .addItemCost(Items.DIAMOND_ORE, 64)
          .addEffect(MORE_ORES, 5)
          .addToList(r);

        final Research whatYaNeed = new Research(new ResourceLocation(Constants.MOD_ID, "technology/whatyaneed"), TECH).setParentResearch(hittingIron)
                                      .setTranslatedName("What ya Need?")
                                      .setTranslatedSubtitle("It's not a rhetorical question...")
                                      .setIcon(ModBlocks.blockHutMechanic.asItem())
                                      .addBuildingRequirement(ModBuildings.BLACKSMITH_ID, 3)
                                      .addItemCost(Items.REDSTONE, 64)
                                      .addEffect(ModBuildings.mechanic.getBuildingBlock(), 1)
                                      .addToList(r);
        final Research enhanced_gates1 = new Research(new ResourceLocation(Constants.MOD_ID, "technology/enhanced_gates1"), TECH).setParentResearch(whatYaNeed)
                                           .setTranslatedName("Enhanced Gates I")
                                           .setIcon(ModItems.woodgate)
                                           .addItemCost(ModItems.woodgate, 64)
                                           .addItemCost(ModItems.ancientTome, 2)
                                           .addItemCost(Items.IRON_BLOCK, 5)
                                           .addEffect(MECHANIC_ENHANCED_GATES, 1)
                                           .addToList(r);
        new Research(new ResourceLocation(Constants.MOD_ID, "technology/enhanced_gates2"), TECH).setParentResearch(enhanced_gates1)
          .setTranslatedName("Enhanced Gates II")
          .setIcon(ModItems.irongate)
          .addItemCost(ModItems.irongate, 64)
          .addItemCost(ModItems.ancientTome, 2)
          .addItemCost(Items.OBSIDIAN, 32)
          .addEffect(MECHANIC_ENHANCED_GATES, 2)
          .addToList(r);

        return r;
    }
}
