package com.minecolonies.api.research;

/**
 * The class which loads the global research tree.
 */
public class GlobalResearchTree
{
    /**
     * The map containing all researches by ID.
     */
    public static final ResearchTree researchTree = new ResearchTree();

    static
    {
        fillResearchTree();
    }

    /**
     * Method to fill the research tree with the elements.
     */
    private static void fillResearchTree()
    {
        final GlobalResearch tacticTraining = new GlobalResearch("tactictraining", "combat", "Tactic Training", 1, new UnlockResearchEffect("Barracks", true));
        tacticTraining.setRequirement(new BuildingResearchRequirement(3, "guardtower"));

        final GlobalResearch improvedSwords = new GlobalResearch("improvedswords", "combat", "Improved Swords", 2, new UnlockResearchEffect("Combat Academy", true));
        improvedSwords.setRequirement(new BuildingResearchRequirement(3, "barracks"));

        final GlobalResearch squireTraining = new GlobalResearch("squiretraining", "combat", "Squire Training", 3, new ModifierResearchEffect("Block Attacks", 0.05));
        squireTraining.setRequirement(new BuildingResearchRequirement(3, "combatacademy"));

        final GlobalResearch knightTraining = new GlobalResearch("knighttraining", "combat", "Knight Training", 4, new ModifierResearchEffect("Block Attacks", 0.10));
        knightTraining.setRequirement(new BuildingResearchRequirement(4, "combatacademy"));

        final GlobalResearch captainTraining = new GlobalResearch("captaintraining", "combat", "Captain Training", 5, new ModifierResearchEffect("Block Attacks", 0.25));
        captainTraining.setRequirement(new BuildingResearchRequirement(5, "combatacademy"));

        final GlobalResearch captainOfTheGuard = new GlobalResearch("captainoftheguard", "combat", "Captain of the Guard", 6, new ModifierResearchEffect("Block Attacks", 0.5));

        tacticTraining.addChild(improvedSwords);
        improvedSwords.addChild(squireTraining);
        squireTraining.addChild(knightTraining);
        knightTraining.addChild(captainTraining);
        captainTraining.addChild(captainOfTheGuard);

        final GlobalResearch improvedBows = new GlobalResearch("improvedbows", "combat", "Improved Bows", 2, new UnlockResearchEffect("Archery", true));
        improvedBows.setRequirement(new BuildingResearchRequirement(3, "barracks"));

        final GlobalResearch tickShot = new GlobalResearch("tickshot", "combat", "Tick Shot", 3, new ModifierResearchEffect("Double Arrows", 0.05));
        tickShot.setRequirement(new BuildingResearchRequirement(3, "archery"));

        final GlobalResearch multiShot = new GlobalResearch("multishot", "combat", "Multi Shot", 4, new ModifierResearchEffect("Double Arrows", 0.10));
        multiShot.setRequirement(new BuildingResearchRequirement(4, "archery"));

        final GlobalResearch rapidShot = new GlobalResearch("rapidshot", "combat", "Rapid Shot", 5, new ModifierResearchEffect("Double Arrows", 0.25));
        rapidShot.setRequirement(new BuildingResearchRequirement(5, "archery"));

        final GlobalResearch masterBowman = new GlobalResearch("masterbowman", "combat", "Master Bowman", 6, new ModifierResearchEffect("Double Arrows", 0.5));

        tacticTraining.addChild(improvedBows);
        improvedBows.addChild(tickShot);
        tickShot.addChild(multiShot);
        multiShot.addChild(rapidShot);
        rapidShot.addChild(masterBowman);

        final GlobalResearch avoidance = new GlobalResearch("avoidance", "combat", "avoidance", 1, new UnlockResearchEffect("Shield Usage", true));
        avoidance.setRequirement(new BuildingResearchRequirement(3, "guardtower"));
        avoidance.setOnlyChild(true);

        final GlobalResearch parry = new GlobalResearch("parry", "combat", "Parry", 2, new ModifierResearchEffect("Melee Armour", 0.05));
        parry.setRequirement(new BuildingResearchRequirement(1, "smeltery"));

        final GlobalResearch repost = new GlobalResearch("repost", "combat", "Repost", 3, new ModifierResearchEffect("Melee Armour", 0.10));
        repost.setRequirement(new BuildingResearchRequirement(1, "combatacademy"));

        final GlobalResearch duelist = new GlobalResearch("duelist", "combat", "Duelist", 4, new ModifierResearchEffect("Melee Armour", 0.25));
        duelist.setRequirement(new BuildingResearchRequirement(3, "smeltery"));

        final GlobalResearch provost = new GlobalResearch("provost", "combat", "Provost", 5, new ModifierResearchEffect("Melee Armour", 0.50));
        provost.setRequirement(new BuildingResearchRequirement(5, "combatacademy"));

        final GlobalResearch masterSwordsman = new GlobalResearch("masterswordsman", "combat", "Master Swordsman", 6, new ModifierResearchEffect("Melee Armour", 1));

        avoidance.addChild(parry);
        parry.addChild(repost);
        repost.addChild(duelist);
        duelist.addChild(provost);
        provost.addChild(masterSwordsman);

        final GlobalResearch dodge = new GlobalResearch("dodge", "combat", "Dodge", 2, new ModifierResearchEffect("Archer Armour", 0.05));
        dodge.setRequirement(new BuildingResearchRequirement(1, "smeltery"));

        final GlobalResearch improvedDodge = new GlobalResearch("improveddodge", "combat", "Improved Dodge", 3, new ModifierResearchEffect("Archer Armour", 0.10));
        improvedDodge.setRequirement(new BuildingResearchRequirement(1, "archery"));

        final GlobalResearch evasion = new GlobalResearch("evasion", "combat", "Evasion", 4, new ModifierResearchEffect("Archer Armour", 0.25));
        evasion.setRequirement(new BuildingResearchRequirement(3, "smeltery"));

        final GlobalResearch improvedEvasion = new GlobalResearch("improvedevasion", "combat", "Improved Evasion", 5, new ModifierResearchEffect("Archer Armour", 0.50));
        improvedEvasion.setRequirement(new BuildingResearchRequirement(5, "archery"));

        final GlobalResearch agileArcher = new GlobalResearch("agilearcher", "combat", "Agile Archer", 6, new ModifierResearchEffect("Archer Armour", 1));

        avoidance.addChild(dodge);
        dodge.addChild(improvedDodge);
        improvedDodge.addChild(evasion);
        evasion.addChild(improvedEvasion);
        improvedEvasion.addChild(agileArcher);

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
    }
}
