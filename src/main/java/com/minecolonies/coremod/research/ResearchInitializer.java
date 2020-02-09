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
     */
    public static void fillResearchTree(final IGlobalResearchTree researchTree)
    {
        final GlobalResearch tacticTraining = new GlobalResearch("tactictraining", "combat", "Tactic Training", 1, new UnlockBuildingResearchEffect("Barracks", true));
        tacticTraining.setRequirement(new BuildingResearchRequirement(3, "guardtower"));

        final GlobalResearch improvedSwords = new GlobalResearch("improvedswords", "combat", "Improved Swords", 2, new UnlockBuildingResearchEffect("Combat Academy", true));
        improvedSwords.setRequirement(new BuildingResearchRequirement(3, "barracks"));

        final GlobalResearch squireTraining = new GlobalResearch("squiretraining", "combat", "Squire Training", 3, new MultiplierModifierResearchEffect("Block Attacks", 0.05));
        squireTraining.setRequirement(new BuildingResearchRequirement(3, "combatacademy"));

        final GlobalResearch knightTraining = new GlobalResearch("knighttraining", "combat", "Knight Training", 4, new MultiplierModifierResearchEffect("Block Attacks", 0.10));
        knightTraining.setRequirement(new BuildingResearchRequirement(4, "combatacademy"));

        final GlobalResearch captainTraining = new GlobalResearch("captaintraining", "combat", "Captain Training", 5, new MultiplierModifierResearchEffect("Block Attacks", 0.25));
        captainTraining.setRequirement(new BuildingResearchRequirement(5, "combatacademy"));

        final GlobalResearch captainOfTheGuard = new GlobalResearch("captainoftheguard", "combat", "Captain of the Guard", 6, new MultiplierModifierResearchEffect("Block Attacks", 0.5));

        tacticTraining.addChild(improvedSwords);
        improvedSwords.addChild(squireTraining);
        squireTraining.addChild(knightTraining);
        knightTraining.addChild(captainTraining);
        captainTraining.addChild(captainOfTheGuard);

        final GlobalResearch improvedBows = new GlobalResearch("improvedbows", "combat", "Improved Bows", 2, new UnlockBuildingResearchEffect("Archery", true));
        improvedBows.setRequirement(new BuildingResearchRequirement(3, "barracks"));

        final GlobalResearch tickShot = new GlobalResearch("tickshot", "combat", "Tick Shot", 3, new MultiplierModifierResearchEffect("Double Arrows", 0.05));
        tickShot.setRequirement(new BuildingResearchRequirement(3, "archery"));

        final GlobalResearch multiShot = new GlobalResearch("multishot", "combat", "Multi Shot", 4, new MultiplierModifierResearchEffect("Double Arrows", 0.10));
        multiShot.setRequirement(new BuildingResearchRequirement(4, "archery"));

        final GlobalResearch rapidShot = new GlobalResearch("rapidshot", "combat", "Rapid Shot", 5, new MultiplierModifierResearchEffect("Double Arrows", 0.25));
        rapidShot.setRequirement(new BuildingResearchRequirement(5, "archery"));

        final GlobalResearch masterBowman = new GlobalResearch("masterbowman", "combat", "Master Bowman", 6, new MultiplierModifierResearchEffect("Double Arrows", 0.5));

        tacticTraining.addChild(improvedBows);
        improvedBows.addChild(tickShot);
        tickShot.addChild(multiShot);
        multiShot.addChild(rapidShot);
        rapidShot.addChild(masterBowman);

        final GlobalResearch avoidance = new GlobalResearch("avoidance", "combat", "Avoidance", 1, new UnlockAbilityResearchEffect("Shield Usage", true));
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
        feint.setRequirement(new BuildingResearchRequirement(3, "guardtower"));

        final GlobalResearch fear = new GlobalResearch("fear", "combat", "Fear", 4, new MultiplierModifierResearchEffect(FLEEING_DAMAGE, 0.3));
        fear.setRequirement(new BuildingResearchRequirement(4, "guardtower"));

        final GlobalResearch retreat = new GlobalResearch("retreat", "combat", "Retreat", 5, new MultiplierModifierResearchEffect(FLEEING_DAMAGE, 0.4));
        retreat.setRequirement(new BuildingResearchRequirement(5, "guardtower"));

        final GlobalResearch fullRetreat = new GlobalResearch("fullretreat", "combat", "Full Retreat", 6, new MultiplierModifierResearchEffect(FLEEING_DAMAGE, 1.0));

        improvedLeather.addChild(regeneration);
        regeneration.addChild(feint);
        feint.addChild(fear);
        fear.addChild(retreat);
        retreat.addChild(fullRetreat);

        final GlobalResearch avoid = new GlobalResearch("avoid", "combat", "Avoid", 3, new AdditionModifierResearchEffect(FLEEING_SPEED, 1));
        avoid.setRequirement(new BuildingResearchRequirement(3, "guardtower"));

        final GlobalResearch evade = new GlobalResearch("evade", "combat", "Evade", 4, new AdditionModifierResearchEffect(FLEEING_SPEED, 2));
        evade.setRequirement(new BuildingResearchRequirement(4, "guardtower"));

        final GlobalResearch flee = new GlobalResearch("flee", "combat", "Flee", 5, new AdditionModifierResearchEffect(FLEEING_SPEED, 3));
        flee.setRequirement(new BuildingResearchRequirement(5, "guardtower"));

        final GlobalResearch hotFoot = new GlobalResearch("hotfoot", "combat", "Hotfoot", 6, new AdditionModifierResearchEffect(FLEEING_SPEED, 5));

        regeneration.addChild(avoid);
        avoid.addChild(evade);
        evade.addChild(flee);
        flee.addChild(hotFoot);

        final GlobalResearch accuracy = new GlobalResearch("accuracy", "combat", "Accuracy", 1, new AdditionModifierResearchEffect("None", 0));
        accuracy.setOnlyChild(true);

        final GlobalResearch quickDraw = new GlobalResearch("quickdraw", "combat", "Quick Draw", 2, new AdditionModifierResearchEffect(MELEE_DAMAGE, 0.5));
        quickDraw.setRequirement(new BuildingResearchRequirement(3, "barracks"));

        final GlobalResearch powerAttack = new GlobalResearch("powerattack", "combat", "Power Attack", 3, new AdditionModifierResearchEffect(MELEE_DAMAGE, 0.5));
        powerAttack.setRequirement(new BuildingResearchRequirement(3, "combatacademy"));

        final GlobalResearch cleave = new GlobalResearch("cleave", "combat", "Cleave", 4, new AdditionModifierResearchEffect(MELEE_DAMAGE, 0.5));
        cleave.setRequirement(new BuildingResearchRequirement(5, "guardtower"));

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
        piercingShot.setRequirement(new BuildingResearchRequirement(5, "guardtower"));

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
