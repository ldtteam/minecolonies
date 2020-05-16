package com.minecolonies.coremod.research;

import com.minecolonies.api.research.IGlobalResearchTree;

import static com.minecolonies.api.research.util.ResearchConstants.*;

/**
 * The class which loads the global research tree.
 */
public class ResearchInitializer
{
    /**
     * Method to fill the research tree with the elements.
     * @param researchTree the research tree to fill.
     */
    public static void fillResearchTree(final IGlobalResearchTree researchTree)
    {
        buildCombatTree(researchTree);

        buildCivilianTree(researchTree);

        buildTechnologyTree(researchTree);
    }

    private static void buildTechnologyTree(final IGlobalResearchTree researchTree)
    {
        final GlobalResearch biodegradable = new GlobalResearch("biodegradable", "technology", "Biodegradable", 1, new UnlockBuildingResearchEffect("Composter", true));
        biodegradable.setRequirement(new BuildingResearchRequirement(3, "farmer"));

        final GlobalResearch flowerpower = new GlobalResearch("flowerpower", "technology", "Flower power", 2, new UnlockBuildingResearchEffect("Florist", true));
        flowerpower.setRequirement(new BuildingResearchRequirement(3, "composter"));

        final GlobalResearch rainbowheaven = new GlobalResearch("rainbowheaven", "technology", "Rainbow Heaven", 3, new UnlockBuildingResearchEffect("Dyer", true));
        rainbowheaven.setRequirement(new BuildingResearchRequirement(3, "florist"));

        biodegradable.addChild(flowerpower);
        flowerpower.addChild(rainbowheaven);

        final GlobalResearch letitgrow = new GlobalResearch("letitgrow", "technology", "Let it Grow", 2, new UnlockBuildingResearchEffect("Plantation", true));
        letitgrow.setRequirement(new BuildingResearchRequirement(3, "farmer"));

        biodegradable.addChild(letitgrow);

        final GlobalResearch bonemeal = new GlobalResearch("bonemeal", "technology", "Bonemeal", 2, new MultiplierModifierResearchEffect(FARMING, 0.1));
        bonemeal.setRequirement(new BuildingResearchRequirement(4, "farmer"));

        final GlobalResearch dung = new GlobalResearch("dung", "technology", "Dung", 3, new MultiplierModifierResearchEffect(FARMING, 0.25));
        dung.setRequirement(new BuildingResearchRequirement(8, "farmer"));

        final GlobalResearch compost = new GlobalResearch("compost", "technology", "Compost", 4, new MultiplierModifierResearchEffect(FARMING, 0.5));
        compost.setRequirement(new BuildingResearchRequirement(12, "farmer"));

        final GlobalResearch fertilizer = new GlobalResearch("fertilizer", "technology", "Fertilizer", 5, new MultiplierModifierResearchEffect(FARMING, 0.75));
        fertilizer.setRequirement(new BuildingResearchRequirement(5, "composter"));

        final GlobalResearch magicCompost = new GlobalResearch("magiccompost", "technology", "Magic Compost", 6, new MultiplierModifierResearchEffect(FARMING, 2.0));

        biodegradable.addChild(bonemeal);
        bonemeal.addChild(dung);
        dung.addChild(compost);
        compost.addChild(fertilizer);
        fertilizer.addChild(magicCompost);

        final GlobalResearch hot = new GlobalResearch("hot", "technology", "Hot!", 1, new UnlockBuildingResearchEffect("Smeltery", true));
        hot.setRequirement(new BuildingResearchRequirement(2, "miner"));

        final GlobalResearch isthisredstone = new GlobalResearch("isthisredstone", "technology", "Is this Redstone?", 2, new MultiplierModifierResearchEffect(BLOCK_BREAK_SPEED, 0.1));
        bonemeal.setRequirement(new BuildingResearchRequirement(3, "miner"));

        final GlobalResearch redstonepowered = new GlobalResearch("redstonepowered", "technology", "Redstone powered", 3, new MultiplierModifierResearchEffect(BLOCK_BREAK_SPEED, 0.25));
        dung.setRequirement(new BuildingResearchRequirement(4, "miner"));

        final GlobalResearch heavymachinery = new GlobalResearch("heavymachinery", "technology", "Heavy Machinery", 4, new MultiplierModifierResearchEffect(BLOCK_BREAK_SPEED, 0.5));
        compost.setRequirement(new BuildingResearchRequirement(5, "miner"));

        final GlobalResearch whatisthisspeed = new GlobalResearch("whatisthisspeed", "technology", "What is this speed?", 5, new MultiplierModifierResearchEffect(BLOCK_BREAK_SPEED, 1.0));
        fertilizer.setRequirement(new BuildingResearchRequirement(3, "smeltery"));

        final GlobalResearch lightning = new GlobalResearch("lightning", "technology", "Lightning", 6, new MultiplierModifierResearchEffect(BLOCK_BREAK_SPEED, 2.0));

        hot.addChild(isthisredstone);
        isthisredstone.addChild(redstonepowered);
        redstonepowered.addChild(heavymachinery);
        heavymachinery.addChild(whatisthisspeed);
        whatisthisspeed.addChild(lightning);

        final GlobalResearch theflintstones = new GlobalResearch("theflintstones", "technology", "The Flintstones", 2, new UnlockBuildingResearchEffect("Stonesmeltery", true));
        theflintstones.setRequirement(new BuildingResearchRequirement(3, "smeltery"));

        final GlobalResearch rockingroll = new GlobalResearch("rockingroll", "technology", "Rocking Roll", 3, new UnlockBuildingResearchEffect("Crusher", true));
        rockingroll.setRequirement(new BuildingResearchRequirement(1, "stonemason"));

        hot.addChild(theflintstones);
        theflintstones.addChild(rockingroll);

        final GlobalResearch thoselungs = new GlobalResearch("thoselungs", "technology", "Those lungs!", 2, new UnlockBuildingResearchEffect("Glassblower", true));
        thoselungs.setRequirement(new BuildingResearchRequirement(3, "smeltery"));

        hot.addChild(thoselungs);

        final GlobalResearch woodwork = new GlobalResearch("woodwork", "technology", "Woodwork", 1, new UnlockBuildingResearchEffect("Sawmill", true));
        woodwork.setRequirement(new BuildingResearchRequirement(3, "lumberjack"));

        final GlobalResearch sieving = new GlobalResearch("sieving", "technology", "Sieving", 2, new UnlockBuildingResearchEffect("Sifter", true));
        sieving.setRequirement(new BuildingResearchRequirement(3, "fisherman"));

        final GlobalResearch space = new GlobalResearch("space", "technology", "Space", 3, new MultiplierModifierResearchEffect(MINIMUM_STOCK, 0.5));
        space.setRequirement(new BuildingResearchRequirement(3, "miner"));

        final GlobalResearch capacity = new GlobalResearch("capacity", "technology", "Capacity", 4, new MultiplierModifierResearchEffect(MINIMUM_STOCK, 1.0));
        capacity.setRequirement(new BuildingResearchRequirement(4, "miner"));

        final GlobalResearch fullstock = new GlobalResearch("fullstock", "technology", "Full Stock!", 5, new MultiplierModifierResearchEffect(MINIMUM_STOCK, 2.0));
        fullstock.setRequirement(new BuildingResearchRequirement(5, "miner"));

        final GlobalResearch stringwork = new GlobalResearch("stringwork", "technology", "Stringwork", 2, new UnlockBuildingResearchEffect("Fletcher", true));
        stringwork.setRequirement(new BuildingResearchRequirement(1, "sawmill"));

        woodwork.addChild(stringwork);

        woodwork.addChild(sieving);
        sieving.addChild(space);
        space.addChild(capacity);
        capacity.addChild(fullstock);

        final GlobalResearch memoryaid = new GlobalResearch("memoryaid", "technology", "Memory Aid", 2, new MultiplierModifierResearchEffect(RECIPES, 0.25));
        memoryaid.setRequirement(new BuildingResearchRequirement(1, "sawmill"));

        final GlobalResearch cheatsheet = new GlobalResearch("cheatsheet", "technology", "Cheat Sheet", 3, new MultiplierModifierResearchEffect(RECIPES, 0.5));
        cheatsheet.setRequirement(new BuildingResearchRequirement(2, "sawmill"));

        final GlobalResearch recipebook = new GlobalResearch("recipebook", "technology", "Recipe book", 4, new MultiplierModifierResearchEffect(RECIPES, 1.0));
        recipebook.setRequirement(new BuildingResearchRequirement(3, "sawmill"));

        final GlobalResearch rtm = new GlobalResearch("rtm", "technology", "RTM", 5, new MultiplierModifierResearchEffect(RECIPES, 2.0));
        rtm.setRequirement(new BuildingResearchRequirement(4, "sawmill"));

        final GlobalResearch rainman = new GlobalResearch("rainman", "technology", "Rainman", 6, new UnlockAbilityResearchEffect(WORKING_IN_RAIN, true));

        woodwork.addChild(memoryaid);
        memoryaid.addChild(cheatsheet);
        cheatsheet.addChild(recipebook);
        recipebook.addChild(rtm);
        rtm.addChild(rainman);

        final GlobalResearch deeppockets = new GlobalResearch("deeppockets", "technology", "Deep Pockets", 4, new AdditionModifierResearchEffect(INV_SLOTS, 9));
        deeppockets.setRequirement(new BuildingResearchRequirement(4, "library"));

        final GlobalResearch loaded = new GlobalResearch("loaded", "technology", "Loaded", 5, new AdditionModifierResearchEffect(INV_SLOTS, 18));
        loaded.setRequirement(new BuildingResearchRequirement(5, "library"));

        final GlobalResearch heavilyloaded = new GlobalResearch("heavilyloaded", "technology", "Heavily Loaded", 6, new AdditionModifierResearchEffect(INV_SLOTS, 27));

        cheatsheet.addChild(deeppockets);
        deeppockets.addChild(loaded);
        loaded.addChild(heavilyloaded);

        final GlobalResearch hittingiron = new GlobalResearch("hittingiron", "technology", "Hitting Iron!", 1, new UnlockBuildingResearchEffect("Blacksmith", true));
        hittingiron.setRequirement(new BuildingResearchRequirement(3, "miner"));

        final GlobalResearch stonecake = new GlobalResearch("stonecake", "technology", "Stone Cake", 2, new UnlockBuildingResearchEffect("Stonemason", true));
        stonecake.setRequirement(new BuildingResearchRequirement(1, "blacksmith"));

        final GlobalResearch hardened = new GlobalResearch("hardened", "technology", "Hardened", 3, new MultiplierModifierResearchEffect(TOOL_DURABILITY, 0.1));
        hardened.setRequirement(new BuildingResearchRequirement(2, "blacksmith"));

        final GlobalResearch reinforced = new GlobalResearch("reinforced", "technology", "Reinforced", 4, new MultiplierModifierResearchEffect(TOOL_DURABILITY, 0.25));
        reinforced.setRequirement(new BuildingResearchRequirement(3, "blacksmith"));

        final GlobalResearch steelbracing = new GlobalResearch("steelbracing", "technology", "Steel Bracing", 5, new MultiplierModifierResearchEffect(TOOL_DURABILITY, 0.5));
        steelbracing.setRequirement(new BuildingResearchRequirement(4, "blacksmith"));

        final GlobalResearch diamondcoated = new GlobalResearch("diamondcoated", "technology", "Diamond Coated", 6, new MultiplierModifierResearchEffect(TOOL_DURABILITY, 0.9));
        steelbracing.setRequirement(new BuildingResearchRequirement(5, "blacksmith"));

        hittingiron.addChild(stonecake);
        stonecake.addChild(hardened);
        hardened.addChild(reinforced);
        reinforced.addChild(steelbracing);
        steelbracing.addChild(diamondcoated);

        final GlobalResearch ability = new GlobalResearch("ability", "technology", "Ability", 2, new MultiplierModifierResearchEffect(BLOCK_PLACE_SPEED, 0.1));
        ability.setRequirement(new BuildingResearchRequirement(1, "miner"));

        final GlobalResearch skills = new GlobalResearch("skills", "technology", "Skills", 3, new MultiplierModifierResearchEffect(BLOCK_PLACE_SPEED, 0.25));
        skills.setRequirement(new BuildingResearchRequirement(2, "miner"));

        final GlobalResearch tools = new GlobalResearch("tools", "technology", "Tools", 4, new MultiplierModifierResearchEffect(BLOCK_PLACE_SPEED, 0.5));
        tools.setRequirement(new BuildingResearchRequirement(4, "blacksmith"));

        final GlobalResearch seemsautomatic = new GlobalResearch("seemsautomatic", "technology", "Seems automatic", 5, new MultiplierModifierResearchEffect(BLOCK_PLACE_SPEED, 1.0));
        seemsautomatic.setRequirement(new BuildingResearchRequirement(5, "blacksmith"));

        final GlobalResearch madness = new GlobalResearch("madness", "technology", "Madness!", 6, new MultiplierModifierResearchEffect(BLOCK_PLACE_SPEED, 2.0));

        hittingiron.addChild(ability);
        ability.addChild(skills);
        skills.addChild(tools);
        tools.addChild(seemsautomatic);
        seemsautomatic.addChild(madness);

        final GlobalResearch veinminer = new GlobalResearch("veinminer", "technology", "Veinminer", 2, new MultiplierModifierResearchEffect(MORE_ORES, 0.1));
        veinminer.setRequirement(new BuildingResearchRequirement(1, "miner"));

        final GlobalResearch goodveins = new GlobalResearch("goodveins", "technology", "Good Veins", 3, new MultiplierModifierResearchEffect(MORE_ORES, 0.25));
        goodveins.setRequirement(new BuildingResearchRequirement(2, "miner"));

        final GlobalResearch richveins = new GlobalResearch("richveins", "technology", "Rich Veins", 4, new MultiplierModifierResearchEffect(MORE_ORES, 0.5));
        richveins.setRequirement(new BuildingResearchRequirement(4, "blacksmith"));

        final GlobalResearch amazingveins = new GlobalResearch("amazingveins", "technology", "Amazing Veins", 5, new MultiplierModifierResearchEffect(MORE_ORES, 1.0));
        amazingveins.setRequirement(new BuildingResearchRequirement(5, "blacksmith"));

        final GlobalResearch motherlode = new GlobalResearch("motherlode", "technology", "Motherlode", 6, new MultiplierModifierResearchEffect(MORE_ORES, 2.0));

        hittingiron.addChild(veinminer);
        veinminer.addChild(goodveins);
        goodveins.addChild(richveins);
        richveins.addChild(amazingveins);
        amazingveins.addChild(motherlode);

        final GlobalResearch whatyaneed = new GlobalResearch("whatyaneed", "technology", "What ya Need?", 2, new UnlockBuildingResearchEffect("Mechanic", true));
        whatyaneed.setRequirement(new BuildingResearchRequirement(3, "blacksmith"));

        hittingiron.addChild(whatyaneed);

        researchTree.addResearch(whatyaneed.getBranch(), whatyaneed);
        researchTree.addResearch(stringwork.getBranch(), stringwork);
        researchTree.addResearch(thoselungs.getBranch(), thoselungs);
        researchTree.addResearch(rainbowheaven.getBranch(), rainbowheaven);

        researchTree.addResearch(deeppockets.getBranch(), deeppockets);
        researchTree.addResearch(loaded.getBranch(), loaded);
        researchTree.addResearch(heavilyloaded.getBranch(), heavilyloaded);

        researchTree.addResearch(veinminer.getBranch(), veinminer);
        researchTree.addResearch(goodveins.getBranch(), goodveins);
        researchTree.addResearch(richveins.getBranch(), richveins);
        researchTree.addResearch(amazingveins.getBranch(), amazingveins);
        researchTree.addResearch(motherlode.getBranch(), motherlode);

        researchTree.addResearch(ability.getBranch(), ability);
        researchTree.addResearch(skills.getBranch(), skills);
        researchTree.addResearch(tools.getBranch(), tools);
        researchTree.addResearch(seemsautomatic.getBranch(), seemsautomatic);
        researchTree.addResearch(madness.getBranch(), madness);

        researchTree.addResearch(hittingiron.getBranch(), hittingiron);
        researchTree.addResearch(stonecake.getBranch(), stonecake);
        researchTree.addResearch(hardened.getBranch(), hardened);
        researchTree.addResearch(reinforced.getBranch(), reinforced);
        researchTree.addResearch(steelbracing.getBranch(), steelbracing);
        researchTree.addResearch(diamondcoated.getBranch(), diamondcoated);

        researchTree.addResearch(memoryaid.getBranch(), memoryaid);
        researchTree.addResearch(cheatsheet.getBranch(), cheatsheet);
        researchTree.addResearch(recipebook.getBranch(), recipebook);
        researchTree.addResearch(rtm.getBranch(), rtm);
        researchTree.addResearch(rainman.getBranch(), rainman);

        researchTree.addResearch(woodwork.getBranch(), woodwork);
        researchTree.addResearch(sieving.getBranch(), sieving);
        researchTree.addResearch(space.getBranch(), space);
        researchTree.addResearch(capacity.getBranch(), capacity);
        researchTree.addResearch(fullstock.getBranch(), fullstock);

        researchTree.addResearch(theflintstones.getBranch(), theflintstones);
        researchTree.addResearch(rockingroll.getBranch(), rockingroll);

        researchTree.addResearch(hot.getBranch(), hot);
        researchTree.addResearch(isthisredstone.getBranch(), isthisredstone);
        researchTree.addResearch(redstonepowered.getBranch(), redstonepowered);
        researchTree.addResearch(heavymachinery.getBranch(), heavymachinery);
        researchTree.addResearch(whatisthisspeed.getBranch(), whatisthisspeed);
        researchTree.addResearch(lightning.getBranch(), lightning);

        researchTree.addResearch(biodegradable.getBranch(), biodegradable);
        researchTree.addResearch(flowerpower.getBranch(), flowerpower);

        researchTree.addResearch(letitgrow.getBranch(), letitgrow);

        researchTree.addResearch(bonemeal.getBranch(), bonemeal);
        researchTree.addResearch(dung.getBranch(), dung);
        researchTree.addResearch(compost.getBranch(), compost);
        researchTree.addResearch(fertilizer.getBranch(), fertilizer);
        researchTree.addResearch(magicCompost.getBranch(), magicCompost);

    }

    private static void buildCivilianTree(final IGlobalResearchTree researchTree)
    {
        final GlobalResearch higherlearning = new GlobalResearch("higherlearning", "civilian", "Higher Learning", 1, new UnlockBuildingResearchEffect("School", true));
        higherlearning.setRequirement(new BuildingResearchRequirement(3, "citizen"));
        higherlearning.setOnlyChild(true);

        final GlobalResearch morebooks = new GlobalResearch("morebooks", "civilian", "More Books", 2, new MultiplierModifierResearchEffect("Teaching", 0.05));
        morebooks.setRequirement(new BuildingResearchRequirement(1, "school"));

        final GlobalResearch bookworm = new GlobalResearch("bookworm", "civilian", "Bookworm", 3, new MultiplierModifierResearchEffect("Teaching", 0.1));
        bookworm.setRequirement(new BuildingResearchRequirement(3, "school"));

        final GlobalResearch bachelor = new GlobalResearch("bachelor", "civilian", "Bachelor", 4, new MultiplierModifierResearchEffect("Teaching", 0.25));
        bachelor.setRequirement(new BuildingResearchRequirement(3, "library"));

        final GlobalResearch master = new GlobalResearch("master", "civilian", "Master", 5, new MultiplierModifierResearchEffect("Teaching", 0.50));
        master.setRequirement(new BuildingResearchRequirement(5, "library"));

        final GlobalResearch phd = new GlobalResearch("phd", "civilian", "P.h.D", 6, new MultiplierModifierResearchEffect("Teaching", 1));

        higherlearning.addChild(morebooks);
        morebooks.addChild(bookworm);
        bookworm.addChild(bachelor);
        bachelor.addChild(master);
        master.addChild(phd);

        final GlobalResearch nurture = new GlobalResearch("nurture", "civilian", "Nurture", 2, new MultiplierModifierResearchEffect("Growth", 0.05));
        nurture.setRequirement(new BuildingResearchRequirement(1, "school"));

        final GlobalResearch hormones = new GlobalResearch("hormones", "civilian", "Hormones", 3, new MultiplierModifierResearchEffect("Growth", 0.1));
        hormones.setRequirement(new BuildingResearchRequirement(3, "school"));

        final GlobalResearch puberty = new GlobalResearch("puberty", "civilian", "Puberty", 4, new MultiplierModifierResearchEffect("Growth", 0.25));
        puberty.setRequirement(new BuildingResearchRequirement(3, "library"));

        final GlobalResearch growth = new GlobalResearch("growth", "civilian", "Growth", 5, new MultiplierModifierResearchEffect("Growth", 0.50));
        growth.setRequirement(new BuildingResearchRequirement(5, "library"));

        final GlobalResearch beanstalk = new GlobalResearch("beanstalk", "civilian", "Beanstalk", 6, new MultiplierModifierResearchEffect("Growth", 1));

        higherlearning.addChild(nurture);
        nurture.addChild(hormones);
        hormones.addChild(puberty);
        puberty.addChild(growth);
        growth.addChild(beanstalk);

        final GlobalResearch keen = new GlobalResearch("keen", "civilian", "Keen", 1, new UnlockBuildingResearchEffect("Library", true));
        keen.setRequirement(new BuildingResearchRequirement(3, "citizen"));

        final GlobalResearch outpost = new GlobalResearch("outpost", "civilian", "Outpost", 2, new AdditionModifierResearchEffect("Citizen-Cap", 25));
        outpost.setRequirement(new BuildingResearchRequirement(4, "citizen"));

        final GlobalResearch hamlet = new GlobalResearch("hamlet", "civilian", "Hamlet", 3, new AdditionModifierResearchEffect("Citizen-Cap", 50));
        hamlet.setRequirement(new BuildingResearchRequirement(5, "citizen"));

        final GlobalResearch village = new GlobalResearch("village", "civilian", "Village", 4, new AdditionModifierResearchEffect("Citizen-Cap", 75));
        village.setRequirement(new BuildingResearchRequirement(4, "townhall"));

        final GlobalResearch city = new GlobalResearch("city", "civilian", "City", 5, new AdditionModifierResearchEffect("Citizen-Cap", 175));
        city.setRequirement(new BuildingResearchRequirement(5, "townhall"));

        keen.addChild(outpost);
        outpost.addChild(hamlet);
        hamlet.addChild(village);
        village.addChild(city);

        final GlobalResearch diligent = new GlobalResearch("diligent", "civilian", "Diligent", 2, new MultiplierModifierResearchEffect("Leveling", 0.1));
        diligent.setRequirement(new BuildingResearchRequirement(2, "library"));

        final GlobalResearch studious = new GlobalResearch("studious", "civilian", "Studious", 3, new MultiplierModifierResearchEffect("Leveling", 0.25));
        studious.setRequirement(new BuildingResearchRequirement(3, "library"));

        final GlobalResearch scholarly = new GlobalResearch("scholarly", "civilian", "Scholarly", 4, new MultiplierModifierResearchEffect("Leveling", 0.5));
        scholarly.setRequirement(new BuildingResearchRequirement(4, "library"));

        final GlobalResearch reflective = new GlobalResearch("reflective", "civilian", "Reflective", 5, new MultiplierModifierResearchEffect("Leveling", 1.0));
        reflective.setRequirement(new BuildingResearchRequirement(5, "library"));

        final GlobalResearch academic = new GlobalResearch("academic", "civilian", "Academic", 6, new MultiplierModifierResearchEffect("Leveling", 2.0));

        keen.addChild(diligent);
        diligent.addChild(studious);
        studious.addChild(scholarly);
        scholarly.addChild(reflective);
        reflective.addChild(academic);

        final GlobalResearch rails = new GlobalResearch("rails", "civilian", "Rails", 2, new UnlockAbilityResearchEffect("Rails", true));
        rails.setRequirement(new BuildingResearchRequirement(3, "deliveryman"));

        final GlobalResearch nimble = new GlobalResearch("nimble", "civilian", "Nimble", 3, new MultiplierModifierResearchEffect("Walking", 0.05));
        nimble.setRequirement(new BuildingResearchRequirement(3, "townhall"));

        final GlobalResearch agile = new GlobalResearch("agile", "civilian", "Agile", 4, new MultiplierModifierResearchEffect("Walking", 0.1));
        agile.setRequirement(new BuildingResearchRequirement(4, "townhall"));

        final GlobalResearch swift = new GlobalResearch("swift", "civilian", "Swift", 5, new MultiplierModifierResearchEffect("Walking", 0.25));
        swift.setRequirement(new BuildingResearchRequirement(5, "townhall"));

        final GlobalResearch athlete = new GlobalResearch("athlete", "civilian", "Athlete", 6, new MultiplierModifierResearchEffect("Walking", 1.0));

        keen.addChild(rails);
        rails.addChild(nimble);
        nimble.addChild(agile);
        agile.addChild(swift);
        swift.addChild(athlete);

        final GlobalResearch firstaid = new GlobalResearch("firstaid", "civilian", "First Aid", 1, new AdditionModifierResearchEffect("Health", 1));
        firstaid.setRequirement(new BuildingResearchRequirement(1, "townhall"));

        final GlobalResearch firstaid2 = new GlobalResearch("firstaid2", "civilian", "First Aid II", 2, new AdditionModifierResearchEffect("Health", 1));
        firstaid2.setRequirement(new BuildingResearchRequirement(2, "townhall"));

        final GlobalResearch livesaver = new GlobalResearch("livesaver", "civilian", "Livesaver", 3, new AdditionModifierResearchEffect("Health", 1));
        livesaver.setRequirement(new BuildingResearchRequirement(3, "townhall"));

        final GlobalResearch livesaver2 = new GlobalResearch("livesaver2", "civilian", "Livesaver II", 4, new AdditionModifierResearchEffect("Health", 1));
        livesaver2.setRequirement(new BuildingResearchRequirement(4, "townhall"));

        final GlobalResearch guardianangel = new GlobalResearch("guardianangel", "civilian", "Guardian Angel", 5, new AdditionModifierResearchEffect("Health", 1));
        guardianangel.setRequirement(new BuildingResearchRequirement(5, "townhall"));

        final GlobalResearch guardianangel2 = new GlobalResearch("guardianangel2", "civilian", "Guardian Angel II", 6, new AdditionModifierResearchEffect("Health", 5));

        firstaid.addChild(firstaid2);
        firstaid2.addChild(livesaver);
        livesaver.addChild(livesaver2);
        livesaver2.addChild(guardianangel);
        guardianangel.addChild(guardianangel2);

        final GlobalResearch stamina = new GlobalResearch("stamina", "civilian", "Stamina", 1, new UnlockBuildingResearchEffect("Hospital", true));
        stamina.setOnlyChild(true);

        final GlobalResearch bandaid = new GlobalResearch("bandaid", "civilian", "Band Aid", 2, new MultiplierModifierResearchEffect("Regeneration", 0.1));
        bandaid.setRequirement(new BuildingResearchRequirement(2, "library"));

        final GlobalResearch healingcream = new GlobalResearch("healingcream", "civilian", "Healing Cream", 3, new MultiplierModifierResearchEffect("Regeneration", 0.25));
        healingcream.setRequirement(new BuildingResearchRequirement(3, "library"));

        final GlobalResearch bandages = new GlobalResearch("bandages", "civilian", "Bandages", 4, new MultiplierModifierResearchEffect("Regeneration", 0.5));
        bandages.setRequirement(new BuildingResearchRequirement(4, "library"));

        final GlobalResearch compress = new GlobalResearch("compress", "civilian", "Compress", 5, new MultiplierModifierResearchEffect("Regeneration", 1.0));
        compress.setRequirement(new BuildingResearchRequirement(5, "library"));

        final GlobalResearch cast = new GlobalResearch("cast", "civilian", "Cast", 6, new MultiplierModifierResearchEffect("Regeneration", 2.0));

        stamina.addChild(bandaid);
        bandaid.addChild(healingcream);
        healingcream.addChild(bandages);
        bandages.addChild(compress);
        compress.addChild(cast);

        final GlobalResearch resistance = new GlobalResearch("resistance", "civilian", "Resistance", 2, new AdditionModifierResearchEffect("Healing Saturation Limit", -0.5));
        resistance.setRequirement(new BuildingResearchRequirement(2, "cook"));

        final GlobalResearch resilience = new GlobalResearch("resilience", "civilian", "Resilience", 3, new AdditionModifierResearchEffect("Healing Saturation Limit", -1.0));
        resilience.setRequirement(new BuildingResearchRequirement(3, "cook"));

        final GlobalResearch vitality = new GlobalResearch("vitality", "civilian", "Vitality", 4, new AdditionModifierResearchEffect("Healing Saturation Limit", -1.5));
        vitality.setRequirement(new BuildingResearchRequirement(4, "cook"));

        final GlobalResearch fortitude = new GlobalResearch("fortitude", "civilian", "Fortitude", 5, new AdditionModifierResearchEffect("Healing Saturation Limit", -2.0));
        fortitude.setRequirement(new BuildingResearchRequirement(5, "cook"));

        final GlobalResearch indefatigability = new GlobalResearch("indefatigability", "civilian", "Indefatigability", 6, new AdditionModifierResearchEffect("Healing Saturation Limit", -5));

        stamina.addChild(resistance);
        resistance.addChild(resilience);
        resilience.addChild(vitality);
        vitality.addChild(fortitude);
        fortitude.addChild(indefatigability);

        final GlobalResearch circus = new GlobalResearch("circus", "civilian", "Circus", 2, new MultiplierModifierResearchEffect("Happiness", 0.05));
        circus.setRequirement(new BuildingResearchRequirement(2, "cook"));

        final GlobalResearch festival = new GlobalResearch("festival", "civilian", "Festival", 3, new MultiplierModifierResearchEffect("Happiness", 0.1));
        festival.setRequirement(new BuildingResearchRequirement(3, "cook"));

        final GlobalResearch spectacle = new GlobalResearch("spectacle", "civilian", "Spectacle", 4, new MultiplierModifierResearchEffect("Happiness", 0.15));
        spectacle.setRequirement(new BuildingResearchRequirement(4, "cook"));

        final GlobalResearch opera = new GlobalResearch("opera", "civilian", "Opera", 5, new MultiplierModifierResearchEffect("Happiness", 0.2));
        opera.setRequirement(new BuildingResearchRequirement(5, "cook"));

        final GlobalResearch theater = new GlobalResearch("theater", "civilian", "Theater", 6, new MultiplierModifierResearchEffect("Happiness", 0.5));

        firstaid.addChild(circus);
        circus.addChild(festival);
        festival.addChild(spectacle);
        spectacle.addChild(opera);
        opera.addChild(theater);

        final GlobalResearch gourmand = new GlobalResearch("gourmand", "civilian", "Gourmand", 2, new MultiplierModifierResearchEffect("Saturation", 0.1));
        gourmand.setRequirement(new BuildingResearchRequirement(2, "cook"));

        final GlobalResearch gorger = new GlobalResearch("gorger", "civilian", "Gorger", 3, new MultiplierModifierResearchEffect("Saturation", 0.25));
        gorger.setRequirement(new BuildingResearchRequirement(3, "cook"));

        final GlobalResearch stuffer = new GlobalResearch("stuffer", "civilian", "Stuffer", 4, new MultiplierModifierResearchEffect("Saturation", 0.5));
        stuffer.setRequirement(new BuildingResearchRequirement(4, "cook"));

        final GlobalResearch epicure = new GlobalResearch("epicure", "civilian", "Epicure", 5, new MultiplierModifierResearchEffect("Saturation", 1.0));
        epicure.setRequirement(new BuildingResearchRequirement(5, "cook"));

        final GlobalResearch glutton = new GlobalResearch("glutton", "civilian", "Glutton", 6, new MultiplierModifierResearchEffect("Saturation", 2.0));

        firstaid.addChild(gourmand);
        gourmand.addChild(gorger);
        gorger.addChild(stuffer);
        stuffer.addChild(epicure);
        epicure.addChild(glutton);

        researchTree.addResearch(stamina.getBranch(), stamina);

        researchTree.addResearch(resistance.getBranch(), resistance);
        researchTree.addResearch(resilience.getBranch(), resilience);
        researchTree.addResearch(vitality.getBranch(), vitality);
        researchTree.addResearch(fortitude.getBranch(), fortitude);
        researchTree.addResearch(indefatigability.getBranch(), indefatigability);

        researchTree.addResearch(bandaid.getBranch(), bandaid);
        researchTree.addResearch(healingcream.getBranch(), healingcream);
        researchTree.addResearch(bandages.getBranch(), bandages);
        researchTree.addResearch(compress.getBranch(), compress);
        researchTree.addResearch(cast.getBranch(), cast);

        researchTree.addResearch(gourmand.getBranch(), gourmand);
        researchTree.addResearch(gorger.getBranch(), gorger);
        researchTree.addResearch(stuffer.getBranch(), stuffer);
        researchTree.addResearch(epicure.getBranch(), epicure);
        researchTree.addResearch(glutton.getBranch(), glutton);

        researchTree.addResearch(circus.getBranch(), circus);
        researchTree.addResearch(festival.getBranch(), festival);
        researchTree.addResearch(spectacle.getBranch(), spectacle);
        researchTree.addResearch(opera.getBranch(), opera);
        researchTree.addResearch(theater.getBranch(), theater);

        researchTree.addResearch(firstaid.getBranch(), firstaid);
        researchTree.addResearch(firstaid2.getBranch(), firstaid2);
        researchTree.addResearch(livesaver.getBranch(), livesaver);
        researchTree.addResearch(livesaver2.getBranch(), livesaver2);
        researchTree.addResearch(guardianangel.getBranch(), guardianangel);
        researchTree.addResearch(guardianangel2.getBranch(), guardianangel2);

        researchTree.addResearch(rails.getBranch(), rails);
        researchTree.addResearch(nimble.getBranch(), nimble);
        researchTree.addResearch(agile.getBranch(), agile);
        researchTree.addResearch(swift.getBranch(), swift);
        researchTree.addResearch(athlete.getBranch(), athlete);

        researchTree.addResearch(diligent.getBranch(), diligent);
        researchTree.addResearch(studious.getBranch(), studious);
        researchTree.addResearch(scholarly.getBranch(), scholarly);
        researchTree.addResearch(reflective.getBranch(), reflective);
        researchTree.addResearch(academic.getBranch(), academic);

        researchTree.addResearch(keen.getBranch(), keen);
        researchTree.addResearch(outpost.getBranch(), outpost);
        researchTree.addResearch(hamlet.getBranch(), hamlet);
        researchTree.addResearch(village.getBranch(), village);
        researchTree.addResearch(city.getBranch(), city);

        researchTree.addResearch(nurture.getBranch(), nurture);
        researchTree.addResearch(hormones.getBranch(), hormones);
        researchTree.addResearch(puberty.getBranch(), puberty);
        researchTree.addResearch(growth.getBranch(), growth);
        researchTree.addResearch(beanstalk.getBranch(), beanstalk);

        researchTree.addResearch(higherlearning.getBranch(), higherlearning);
        researchTree.addResearch(morebooks.getBranch(), morebooks);
        researchTree.addResearch(bookworm.getBranch(), bookworm);
        researchTree.addResearch(bachelor.getBranch(), bachelor);
        researchTree.addResearch(master.getBranch(), master);
        researchTree.addResearch(phd.getBranch(), phd);
    }

    private static void buildCombatTree(final IGlobalResearchTree researchTree)
    {
        final GlobalResearch tacticTraining = new GlobalResearch("tactictraining", "combat", "Tactic Training", 1, new UnlockBuildingResearchEffect("Barracks", true));
        tacticTraining.setRequirement(new BuildingResearchRequirement(3, "guardtower"));

        final GlobalResearch improvedSwords = new GlobalResearch("improvedswords", "combat", "Improved Swords", 2, new UnlockBuildingResearchEffect("Combat Academy", true));
        improvedSwords.setRequirement(new BuildingResearchRequirement(3, "barracks"));

        final GlobalResearch squireTraining = new GlobalResearch("squiretraining", "combat", "Squire Training", 3, new MultiplierModifierResearchEffect(BLOCK_ATTACKS, 0.05));
        squireTraining.setRequirement(new BuildingResearchRequirement(3, "combatacademy"));

        final GlobalResearch knightTraining = new GlobalResearch("knighttraining", "combat", "Knight Training", 4, new MultiplierModifierResearchEffect(BLOCK_ATTACKS, 0.10));
        knightTraining.setRequirement(new BuildingResearchRequirement(4, "combatacademy"));

        final GlobalResearch captainTraining = new GlobalResearch("captaintraining", "combat", "Captain Training", 5, new MultiplierModifierResearchEffect(BLOCK_ATTACKS, 0.25));
        captainTraining.setRequirement(new BuildingResearchRequirement(5, "combatacademy"));

        final GlobalResearch captainOfTheGuard = new GlobalResearch("captainoftheguard", "combat", "Captain of the Guard", 6, new MultiplierModifierResearchEffect(BLOCK_ATTACKS, 0.5));

        tacticTraining.addChild(improvedSwords);
        improvedSwords.addChild(squireTraining);
        squireTraining.addChild(knightTraining);
        knightTraining.addChild(captainTraining);
        captainTraining.addChild(captainOfTheGuard);

        final GlobalResearch improvedBows = new GlobalResearch("improvedbows", "combat", "Improved Bows", 2, new UnlockBuildingResearchEffect("Archery", true));
        improvedBows.setRequirement(new BuildingResearchRequirement(3, "barracks"));

        final GlobalResearch tickShot = new GlobalResearch("tickshot", "combat", "Tick Shot", 3, new MultiplierModifierResearchEffect(DOUBLE_ARROWS, 0.05));
        tickShot.setRequirement(new BuildingResearchRequirement(3, "archery"));

        final GlobalResearch multiShot = new GlobalResearch("multishot", "combat", "Multi Shot", 4, new MultiplierModifierResearchEffect(DOUBLE_ARROWS, 0.10));
        multiShot.setRequirement(new BuildingResearchRequirement(4, "archery"));

        final GlobalResearch rapidShot = new GlobalResearch("rapidshot", "combat", "Rapid Shot", 5, new MultiplierModifierResearchEffect(DOUBLE_ARROWS, 0.25));
        rapidShot.setRequirement(new BuildingResearchRequirement(5, "archery"));

        final GlobalResearch masterBowman = new GlobalResearch("masterbowman", "combat", "Master Bowman", 6, new MultiplierModifierResearchEffect(DOUBLE_ARROWS, 0.5));

        tacticTraining.addChild(improvedBows);
        improvedBows.addChild(tickShot);
        tickShot.addChild(multiShot);
        multiShot.addChild(rapidShot);
        rapidShot.addChild(masterBowman);

        final GlobalResearch avoidance = new GlobalResearch("avoidance", "combat", "Avoidance", 1, new UnlockAbilityResearchEffect(SHIELD_USAGE, true));
        avoidance.setRequirement(new BuildingResearchRequirement(3, "guardtower"));
        avoidance.setOnlyChild(true);

        final GlobalResearch parry = new GlobalResearch("parry", "combat", "Parry", 2, new MultiplierModifierResearchEffect(MELEE_ARMOR, 0.05));
        parry.setRequirement(new BuildingResearchRequirement(1, "smeltery"));

        final GlobalResearch repost = new GlobalResearch("repost", "combat", "Repost", 3, new MultiplierModifierResearchEffect(MELEE_ARMOR, 0.10));
        repost.setRequirement(new BuildingResearchRequirement(1, "combatacademy"));

        final GlobalResearch duelist = new GlobalResearch("duelist", "combat", "Duelist", 4, new MultiplierModifierResearchEffect(MELEE_ARMOR, 0.25));
        duelist.setRequirement(new BuildingResearchRequirement(3, "smeltery"));

        final GlobalResearch provost = new GlobalResearch("provost", "combat", "Provost", 5, new MultiplierModifierResearchEffect(MELEE_ARMOR, 0.50));
        provost.setRequirement(new BuildingResearchRequirement(5, "combatacademy"));

        final GlobalResearch masterSwordsman = new GlobalResearch("masterswordsman", "combat", "Master Swordsman", 6, new MultiplierModifierResearchEffect(MELEE_ARMOR, 1));

        avoidance.addChild(parry);
        parry.addChild(repost);
        repost.addChild(duelist);
        duelist.addChild(provost);
        provost.addChild(masterSwordsman);

        final GlobalResearch dodge = new GlobalResearch("dodge", "combat", "Dodge", 2, new MultiplierModifierResearchEffect(ARCHER_ARMOR, 0.05));
        dodge.setRequirement(new BuildingResearchRequirement(1, "smeltery"));

        final GlobalResearch improvedDodge = new GlobalResearch("improveddodge", "combat", "Improved Dodge", 3, new MultiplierModifierResearchEffect(ARCHER_ARMOR, 0.10));
        improvedDodge.setRequirement(new BuildingResearchRequirement(1, "archery"));

        final GlobalResearch evasion = new GlobalResearch("evasion", "combat", "Evasion", 4, new MultiplierModifierResearchEffect(ARCHER_ARMOR, 0.25));
        evasion.setRequirement(new BuildingResearchRequirement(3, "smeltery"));

        final GlobalResearch improvedEvasion = new GlobalResearch("improvedevasion", "combat", "Improved Evasion", 5, new MultiplierModifierResearchEffect(ARCHER_ARMOR, 0.50));
        improvedEvasion.setRequirement(new BuildingResearchRequirement(5, "archery"));

        final GlobalResearch agileArcher = new GlobalResearch("agilearcher", "combat", "Agile Archer", 6, new MultiplierModifierResearchEffect(ARCHER_ARMOR, 1));

        avoidance.addChild(dodge);
        dodge.addChild(improvedDodge);
        improvedDodge.addChild(evasion);
        evasion.addChild(improvedEvasion);
        improvedEvasion.addChild(agileArcher);

        final GlobalResearch improvedLeather = new GlobalResearch("improvedleather", "combat", "Improved Leather", 1, new MultiplierModifierResearchEffect(ARMOR_DURABILITY, 0.1));
        improvedLeather.setRequirement(new BuildingResearchRequirement(1, "townhall"));

        final GlobalResearch boiledLeather = new GlobalResearch("boiledleather", "combat", "Boiled Leather", 2, new MultiplierModifierResearchEffect(ARMOR_DURABILITY, 0.2));
        boiledLeather.setRequirement(new BuildingResearchRequirement(2, "townhall"));

        final GlobalResearch ironSkin = new GlobalResearch("ironskin", "combat", "Iron Skin", 3, new MultiplierModifierResearchEffect(ARMOR_DURABILITY, 0.3));
        ironSkin.setRequirement(new BuildingResearchRequirement(3, "townhall"));

        final GlobalResearch ironArmour = new GlobalResearch("ironarmour", "combat", "Iron Armour", 4, new MultiplierModifierResearchEffect(ARMOR_DURABILITY, 0.4));
        ironArmour.setRequirement(new BuildingResearchRequirement(4, "townhall"));

        final GlobalResearch steelArmour = new GlobalResearch("steelarmour", "combat", "Steel Armour", 5, new MultiplierModifierResearchEffect(ARMOR_DURABILITY, 0.5));
        steelArmour.setRequirement(new BuildingResearchRequirement(5, "townhall"));

        final GlobalResearch diamondSkin = new GlobalResearch("diamondskin", "combat", "Diamond Skin", 6, new MultiplierModifierResearchEffect(ARMOR_DURABILITY, 1.0));

        improvedLeather.addChild(boiledLeather);
        boiledLeather.addChild(ironSkin);
        ironSkin.addChild(ironArmour);
        ironArmour.addChild(steelArmour);
        steelArmour.addChild(diamondSkin);

        final GlobalResearch regeneration = new GlobalResearch("regeneration", "combat", "Regeneration", 2, new UnlockAbilityResearchEffect(RETREAT, true));
        regeneration.setRequirement(new BuildingResearchRequirement(2, "guardtower"));
        regeneration.setOnlyChild(true);

        final GlobalResearch feint = new GlobalResearch("feint", "combat", "Feint", 3, new MultiplierModifierResearchEffect(FLEEING_DAMAGE, 0.2));
        feint.setRequirement(new BuildingResearchRequirement(4, "guardtower"));

        final GlobalResearch fear = new GlobalResearch("fear", "combat", "Fear", 4, new MultiplierModifierResearchEffect(FLEEING_DAMAGE, 0.3));
        fear.setRequirement(new BuildingResearchRequirement(8, "guardtower"));

        final GlobalResearch retreat = new GlobalResearch("retreat", "combat", "Retreat", 5, new MultiplierModifierResearchEffect(FLEEING_DAMAGE, 0.4));
        retreat.setRequirement(new BuildingResearchRequirement(12, "guardtower"));

        final GlobalResearch fullRetreat = new GlobalResearch("fullretreat", "combat", "Full Retreat", 6, new MultiplierModifierResearchEffect(FLEEING_DAMAGE, 1.0));

        improvedLeather.addChild(regeneration);
        regeneration.addChild(feint);
        feint.addChild(fear);
        fear.addChild(retreat);
        retreat.addChild(fullRetreat);

        final GlobalResearch avoid = new GlobalResearch("avoid", "combat", "Avoid", 3, new AdditionModifierResearchEffect(FLEEING_SPEED, 1));
        avoid.setRequirement(new BuildingResearchRequirement(4, "guardtower"));

        final GlobalResearch evade = new GlobalResearch("evade", "combat", "Evade", 4, new AdditionModifierResearchEffect(FLEEING_SPEED, 2));
        evade.setRequirement(new BuildingResearchRequirement(8, "guardtower"));

        final GlobalResearch flee = new GlobalResearch("flee", "combat", "Flee", 5, new AdditionModifierResearchEffect(FLEEING_SPEED, 3));
        flee.setRequirement(new BuildingResearchRequirement(12, "guardtower"));

        final GlobalResearch hotFoot = new GlobalResearch("hotfoot", "combat", "Hotfoot", 6, new AdditionModifierResearchEffect(FLEEING_SPEED, 5));

        regeneration.addChild(avoid);
        avoid.addChild(evade);
        evade.addChild(flee);
        flee.addChild(hotFoot);

        final GlobalResearch accuracy = new GlobalResearch("accuracy", "combat", "Accuracy", 1, new MultiplierModifierResearchEffect(SLEEP_LESS, 0.5));
        accuracy.setRequirement(new BuildingResearchRequirement(1, "guardtower"));
        accuracy.setOnlyChild(true);

        final GlobalResearch quickDraw = new GlobalResearch("quickdraw", "combat", "Quick Draw", 2, new AdditionModifierResearchEffect(MELEE_DAMAGE, 0.5));
        quickDraw.setRequirement(new BuildingResearchRequirement(3, "barracks"));

        final GlobalResearch powerAttack = new GlobalResearch("powerattack", "combat", "Power Attack", 3, new AdditionModifierResearchEffect(MELEE_DAMAGE, 0.5));
        powerAttack.setRequirement(new BuildingResearchRequirement(3, "combatacademy"));

        final GlobalResearch cleave = new GlobalResearch("cleave", "combat", "Cleave", 4, new AdditionModifierResearchEffect(MELEE_DAMAGE, 0.5));
        cleave.setRequirement(new BuildingResearchRequirement(10, "guardtower"));

        final GlobalResearch mightyCleave = new GlobalResearch("mightycleave", "combat", "Mightly Cleave", 5, new AdditionModifierResearchEffect(MELEE_DAMAGE, 0.5));
        mightyCleave.setRequirement(new BuildingResearchRequirement(5, "barracks"));

        final GlobalResearch whirlwind = new GlobalResearch("whirlwind", "combat", "Whirlwind", 6, new AdditionModifierResearchEffect(MELEE_DAMAGE, 2.0));

        accuracy.addChild(quickDraw);
        quickDraw.addChild(powerAttack);
        powerAttack.addChild(cleave);
        cleave.addChild(mightyCleave);
        mightyCleave.addChild(whirlwind);

        final GlobalResearch preciseShot = new GlobalResearch("preciseshot", "combat", "Precise Shot", 2, new AdditionModifierResearchEffect(ARCHER_DAMAGE, 0.5));
        preciseShot.setRequirement(new BuildingResearchRequirement(3, "barracks"));

        final GlobalResearch penetratingShot = new GlobalResearch("penetratingshot", "combat", "Penetrating Shot", 3, new AdditionModifierResearchEffect(ARCHER_DAMAGE, 0.5));
        penetratingShot.setRequirement(new BuildingResearchRequirement(3, "archery"));

        final GlobalResearch piercingShot = new GlobalResearch("piercingshot", "combat", "Piercing Shot", 4, new AdditionModifierResearchEffect(ARCHER_DAMAGE, 0.5));
        piercingShot.setRequirement(new BuildingResearchRequirement(10, "guardtower"));

        final GlobalResearch woundingShot = new GlobalResearch("woundingshot", "combat", "Wounding Shot", 5, new AdditionModifierResearchEffect(ARCHER_DAMAGE, 0.5));
        woundingShot.setRequirement(new BuildingResearchRequirement(5, "barracks"));

        final GlobalResearch deadlyAim = new GlobalResearch("deadlyaim", "combat", "Deadly Aim", 6, new AdditionModifierResearchEffect(ARCHER_DAMAGE, 2.0));

        accuracy.addChild(preciseShot);
        preciseShot.addChild(penetratingShot);
        penetratingShot.addChild(piercingShot);
        piercingShot.addChild(woundingShot);
        woundingShot.addChild(deadlyAim);

        researchTree.addResearch(tacticTraining.getBranch(), tacticTraining);
        researchTree.addResearch(improvedSwords.getBranch(), improvedSwords);
        researchTree.addResearch(improvedBows.getBranch(), improvedBows);

        researchTree.addResearch(squireTraining.getBranch(), squireTraining);
        researchTree.addResearch(knightTraining.getBranch(), knightTraining);
        researchTree.addResearch(captainTraining.getBranch(), captainTraining);
        researchTree.addResearch(captainOfTheGuard.getBranch(), captainOfTheGuard);

        researchTree.addResearch(tickShot.getBranch(), tickShot);
        researchTree.addResearch(multiShot.getBranch(), multiShot);
        researchTree.addResearch(rapidShot.getBranch(), rapidShot);
        researchTree.addResearch(masterBowman.getBranch(), masterBowman);

        researchTree.addResearch(avoidance.getBranch(), avoidance);

        researchTree.addResearch(parry.getBranch(), parry);
        researchTree.addResearch(repost.getBranch(), repost);
        researchTree.addResearch(duelist.getBranch(), duelist);
        researchTree.addResearch(provost.getBranch(), provost);
        researchTree.addResearch(masterSwordsman.getBranch(), masterSwordsman);

        researchTree.addResearch(dodge.getBranch(), dodge);
        researchTree.addResearch(improvedDodge.getBranch(), improvedDodge);
        researchTree.addResearch(evasion.getBranch(), evasion);
        researchTree.addResearch(improvedEvasion.getBranch(), improvedEvasion);
        researchTree.addResearch(agileArcher.getBranch(), agileArcher);

        researchTree.addResearch(improvedLeather.getBranch(), improvedLeather);
        researchTree.addResearch(boiledLeather.getBranch(), boiledLeather);
        researchTree.addResearch(ironSkin.getBranch(), ironSkin);
        researchTree.addResearch(ironArmour.getBranch(), ironArmour);
        researchTree.addResearch(steelArmour.getBranch(), steelArmour);
        researchTree.addResearch(diamondSkin.getBranch(), diamondSkin);

        researchTree.addResearch(regeneration.getBranch(), regeneration);

        researchTree.addResearch(feint.getBranch(), feint);
        researchTree.addResearch(fear.getBranch(), fear);
        researchTree.addResearch(retreat.getBranch(), retreat);
        researchTree.addResearch(fullRetreat.getBranch(), fullRetreat);

        researchTree.addResearch(avoid.getBranch(), avoid);
        researchTree.addResearch(evade.getBranch(), evade);
        researchTree.addResearch(flee.getBranch(), flee);
        researchTree.addResearch(hotFoot.getBranch(), hotFoot);

        researchTree.addResearch(accuracy.getBranch(), accuracy);
        researchTree.addResearch(quickDraw.getBranch(), quickDraw);
        researchTree.addResearch(powerAttack.getBranch(), powerAttack);
        researchTree.addResearch(cleave.getBranch(), cleave);
        researchTree.addResearch(mightyCleave.getBranch(), mightyCleave);
        researchTree.addResearch(whirlwind.getBranch(), whirlwind);

        researchTree.addResearch(preciseShot.getBranch(), preciseShot);
        researchTree.addResearch(penetratingShot.getBranch(), penetratingShot);
        researchTree.addResearch(piercingShot.getBranch(), piercingShot);
        researchTree.addResearch(woundingShot.getBranch(), woundingShot);
        researchTree.addResearch(deadlyAim.getBranch(), deadlyAim);
    }

    /**
     * Private constructor to hide implicit public one.
     */
    private ResearchInitializer()
    {
        /*
         * Intentionally left empty.
         */
    }
}
