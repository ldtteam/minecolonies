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
        final Research betterWeapons = new Research("Tactic Training", "combat", "Tactic Training", 1, new UnlockResearchEffect("Barracks", true));
        betterWeapons.setRequirement(new BuildingResearchRequirement(3, "guardtower"));

        final Research improvedSwords = new Research("Improved Swords", "combat", "Improved Swords", 2, new UnlockResearchEffect("Combat Academy", true));
        improvedSwords.setRequirement(new BuildingResearchRequirement(3, "barracks"));

        final Research squireTraining = new Research("Squire Training", "combat", "Squire Training", 3, new ModifierResearchEffect("Block Attacks", 0.05));
        squireTraining.setRequirement(new BuildingResearchRequirement(3, "combatacademy"));

        final Research knightTraining = new Research("Knight Training", "combat", "Knight Training", 4, new ModifierResearchEffect("Block Attacks", 0.10));
        knightTraining.setRequirement(new BuildingResearchRequirement(4, "combatacademy"));

        final Research captainTraining = new Research("Captain Training", "combat", "Captain Training", 5, new ModifierResearchEffect("Block Attacks", 0.25));
        captainTraining.setRequirement(new BuildingResearchRequirement(5, "combatacademy"));

        final Research captainOfTheGuard = new Research("Captain of the Guard", "combat", "Captain of the Guard", 6, new ModifierResearchEffect("Block Attacks", 0.5));

        betterWeapons.addChild(improvedSwords);
        improvedSwords.addChild(squireTraining);
        squireTraining.addChild(knightTraining);
        knightTraining.addChild(captainTraining);
        captainTraining.addChild(captainOfTheGuard);

        final Research improvedBows = new Research("Improved Bows", "combat", "Improved Bows", 2, new UnlockResearchEffect("Archery", true));
        improvedBows.setRequirement(new BuildingResearchRequirement(3, "barracks"));

        final Research tickShot = new Research("Tick Shot", "combat", "Tick Shot", 3, new ModifierResearchEffect("Double Arrows", 0.05));
        tickShot.setRequirement(new BuildingResearchRequirement(3, "archery"));

        final Research multiShot = new Research("Multi Shot", "combat", "Multi Shot", 4, new ModifierResearchEffect("Double Arrows", 0.10));
        multiShot.setRequirement(new BuildingResearchRequirement(4, "archery"));

        final Research rapidShot = new Research("Rapid Shot", "combat", "Rapid Shot", 5, new ModifierResearchEffect("Double Arrows", 0.25));
        rapidShot.setRequirement(new BuildingResearchRequirement(5, "archery"));

        final Research masterBowman = new Research("Master Bowman", "combat", "Master Bowman", 6, new ModifierResearchEffect("Double Arrows", 0.5));

        betterWeapons.addChild(improvedBows);
        improvedBows.addChild(tickShot);
        tickShot.addChild(multiShot);
        multiShot.addChild(rapidShot);
        rapidShot.addChild(masterBowman);

        final Research avoidance = new Research("Better Armour", "combat", "Better Armour", 1, new UnlockResearchEffect("Shield Usage", true));
        avoidance.setRequirement(new BuildingResearchRequirement(3, "guardtower"));
        avoidance.setOnlyChild(true);

        final Research parry = new Research("Parry", "combat", "Parry", 2, new ModifierResearchEffect("Melee Armour", 0.05));
        parry.setRequirement(new BuildingResearchRequirement(1, "smeltery"));

        final Research repost = new Research("Repost", "combat", "Parry", 3, new ModifierResearchEffect("Melee Armour", 0.10));
        repost.setRequirement(new BuildingResearchRequirement(1, "combatacademy"));

        final Research duelist = new Research("Duelist", "combat", "Parry", 4, new ModifierResearchEffect("Melee Armour", 0.25));
        duelist.setRequirement(new BuildingResearchRequirement(3, "smeltery"));

        final Research provost = new Research("Provost", "combat", "Parry", 5, new ModifierResearchEffect("Melee Armour", 0.50));
        provost.setRequirement(new BuildingResearchRequirement(5, "combatacademy"));

        final Research masterSwordsman = new Research("Master Swordsman", "combat", "Master Swordsman", 6, new ModifierResearchEffect("Melee Armour", 1));

        avoidance.addChild(parry);
        parry.addChild(repost);
        repost.addChild(duelist);
        duelist.addChild(provost);
        provost.addChild(masterSwordsman);

        final Research dodge = new Research("Dodge", "combat", "Dodge", 2, new ModifierResearchEffect("Archer Armour", 0.05));
        dodge.setRequirement(new BuildingResearchRequirement(1, "smeltery"));

        final Research improvedDodge = new Research("Improved Dodge", "Improved Dodge", "Parry", 3, new ModifierResearchEffect("Archer Armour", 0.10));
        improvedDodge.setRequirement(new BuildingResearchRequirement(1, "archery"));

        final Research evasion = new Research("Evasion", "combat", "Evasion", 4, new ModifierResearchEffect("Archer Armour", 0.25));
        evasion.setRequirement(new BuildingResearchRequirement(3, "smeltery"));

        final Research improvedEvasion = new Research("Improved Evasion", "Improved Evasion", "Parry", 5, new ModifierResearchEffect("Archer Armour", 0.50));
        improvedEvasion.setRequirement(new BuildingResearchRequirement(5, "archery"));

        final Research agileArcher = new Research("Agile Archer", "combat", "Agile Archer", 6, new ModifierResearchEffect("Archer Armour", 1));

        avoidance.addChild(dodge);
        dodge.addChild(improvedDodge);
        improvedDodge.addChild(evasion);
        evasion.addChild(improvedEvasion);
        improvedEvasion.addChild(agileArcher);

        researchTree.addResearch(betterWeapons.getBranch(), betterWeapons);
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
