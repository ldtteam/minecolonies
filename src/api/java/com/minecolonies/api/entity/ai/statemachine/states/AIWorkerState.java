package com.minecolonies.api.entity.ai.statemachine.states;

/**
 * Basic state enclosing states all ai's use.
 * Please extend this class with the states your ai needs.
 * And please document each state on what it does.
 */
public enum AIWorkerState implements IAIState
{

    /*
###GENERAL###
     */
    /**
     * this is the idle state for the ai.
     * From here on it will start working.
     * Use this state in your ai to start your code.
     */
    IDLE(true),
    /**
     * This state is only used on ai initialization.
     * It checks if any important things are null.
     */
    INIT(true),
    /**
     * Inventory has to be dumped.
     */
    INVENTORY_FULL(false),
    /**
     * Check for all required items.
     */
    PREPARING(true),
    /**
     * Start working by walking to the building.
     */
    START_WORKING(true),
    /**
     * The ai needs some items it is waiting for.
     */
    NEEDS_ITEM(true),
    /**
     * Start building a StructureIterator.
     */
    START_BUILDING(false),
    /**
     * Clears the building area.
     */
    CLEAR_STEP(false),
    /**
     * Cleans the building area.
     */
    REMOVE_STEP(false),
    /**
     * Creates the solid structure.
     */
    BUILDING_STEP(false),
    /**
     * Sets decorative blocks.
     */
    DECORATION_STEP(false),
    /**
     * Spawns all entities.
     */
    SPAWN_STEP(false),
    /**
     * Completes the building.
     */
    COMPLETE_BUILD(false),
    /**
     * Pick up left over items after building.
     */
    PICK_UP_RESIDUALS(true),
    /**
     * Decide what AIstate to go to next.
     */
    DECIDE(true),
    /**
     * Do not work, can be used for freetime activities.
     */
    PAUSED(true),
    /*
###FISHERMAN###
     */
    /**
     * The fisherman is looking for water.
     */
    FISHERMAN_SEARCHING_WATER(true),
    /**
     * The fisherman has found water and can start fishing.
     */
    FISHERMAN_WALKING_TO_WATER(true),
    FISHERMAN_CHECK_WATER(true),
    FISHERMAN_START_FISHING(false),

    /*
###Lumberjack###
     */
    /**
     * The lumberjack is looking for trees.
     */
    LUMBERJACK_SEARCHING_TREE(true),
    /**
     * The lumberjack has found trees and can start cutting them.
     */
    LUMBERJACK_CHOP_TREE(false),
    /**
     * The Lumberjack is gathering saplings.
     */
    LUMBERJACK_GATHERING(true),
    /**
     * There are no trees in his search range.
     */
    LUMBERJACK_NO_TREES_FOUND(true),

    /*
###Miner###
     */
    /**
     * Check if there is a mineshaft.
     */
    MINER_CHECK_MINESHAFT(true),
    /**
     * The Miner searches for the ladder.
     */
    MINER_SEARCHING_LADDER(true),
    /**
     * The Miner walks to the ladder.
     */
    MINER_WALKING_TO_LADDER(true),
    /**
     * The Miner mines his shaft.
     */
    MINER_MINING_SHAFT(true),
    /**
     * The Miner builds his shaft.
     */
    MINER_BUILDING_SHAFT(true),
    /**
     * The Miner mines one node.
     */
    MINER_MINING_NODE(true),

    /*
###Builder###
     */

    /**
     * Pick up all materials he might need.
     */
    PICK_UP(false),

    /*
###FARMER###
    */

    /**
     * Hoe the field.
     */
    FARMER_HOE(false),

    /**
     * Plant the seeds.
     */
    FARMER_PLANT(false),

    /**
     * Harvest the crops.
     */
    FARMER_HARVEST(false),

      /*
###Guard###
    */

    /**
     * Physically attack the target.
     */
    GUARD_ATTACK_PHYSICAL(false),

    /**
     * Use a ranged attack against the target.
     */
    GUARD_ATTACK_RANGED(false),

    /**
     * Allow the guard to protect himself.
     */
    GUARD_ATTACK_PROTECT(false),

    /**
     * Patrol through the village.
     */
    GUARD_PATROL(true),

    /**
     * Follow a player.
     */
    GUARD_FOLLOW(true),

    /**
     * Guard a position.
     */
    GUARD_GUARD(true),

    /**
     * Regen at the building.
     */
    GUARD_REGEN(true),

    /**
     * Helping out a citizen in danger
     */
    HELP_CITIZEN(false),

    /*
###Deliveryman###
    */

    /**
     * Get stuff from the warehouse.
     */
    PREPARE_DELIVERY(true),

    /**
     * Delivery required items or tools.
     */
    DELIVERY(true),

    /**
     * Gather not needed items and tools from others.
     */
    GATHERING(true),

    /**
     * Dump inventory over chests in warehouse.
     */
    DUMPING(false),

     /*
###Baker###
    */

    /**
     * Knead the dough.
     */
    BAKER_KNEADING(false),

    /**
     * Bake the dough.
     */
    BAKER_BAKING(false),

    /**
     * Finish up the product.
     */
    BAKER_FINISHING(false),

    /**
     * Take the product out of the oven.
     */
    BAKER_TAKE_OUT_OF_OVEN(false),

    /*
###Furnace users###
     */

    /**
     * smelter smelts ore until its a bar.
     */
    START_USING_FURNACE(true),

    /**
     * Gathering ore from his building.
     */
    GATHERING_REQUIRED_MATERIALS(true),

    /**
     * Retrieve the ore from the furnace.
     */
    RETRIEVING_END_PRODUCT_FROM_FURNACE(true),

    /*
###Cook###
     */

    /**
     * Serve food to the citizen inside the building.
     */
    COOK_SERVE_FOOD_TO_CITIZEN(true),

    /*
    ###Smelter###
     */
    /**
     * Smelt stuff he finds in his hut to ingots.
     */
    SMELTER_SMELTING_ITEMS(false),

    /*
### Herders ###
     */

    /**
     * Breed two animals together.
     */
    HERDER_BREED(false),

    /**
     * Butcher an animal.
     */
    HERDER_BUTCHER(false),

    /**
     * Pickup items within area.
     */
    HERDER_PICKUP(true),

    /*
### Cowboy ###
     */

    /**
     * Milk cows!
     */
    COWBOY_MILK(false),

    /*
### Shepherd ###
     */

    /**
     * Shear a sheep!
     */
    SHEPHERD_SHEAR(false),

    /*
### Composter ###
     */

    /**
     * Fill up the barrels
     */
    COMPOSTER_FILL(true),

    /**
     * Take the compost from the barrels
     */
    COMPOSTER_HARVEST(true),

    /**
     * Gather materials from the building
     */
    GET_MATERIALS(true),

    /*
### Student ###
     */

    STUDY(true),
    /*
### General Training AI ###

    /**
     * Wander around the building
     */
    TRAINING_WANDER(true),

    /**
     * Go to the shooting position.
     */
    GO_TO_TARGET(true),

    /**
     * Find the position to train from.
     */
    COMBAT_TRAINING(true),

    /*
### Archers in Training ###
     */

    /**
     * Find a good position to shoot from.
     */
    ARCHER_FIND_SHOOTING_STAND_POSITION(false),

    /**
     * Select a random target.
     */
    ARCHER_SELECT_TARGET(false),

    /**
     * Check the shot result.
     */
    ARCHER_CHECK_SHOT(false),

    /**
     * Archer shoot target.
     */
    ARCHER_SHOOT(false),

            /*
### Knights in Training ###
     */

    /**
     *  Guard attack a dummy.
     */
    KNIGHT_ATTACK_DUMMY(true),

    /**
     * Find dummy to attack
     */
    FIND_DUMMY_PARTNER(true),

    /**
     * Find a training partner
     */
    FIND_TRAINING_PARTNER(true),

    /**
     * Attack the training partner.
     */
    KNIGHT_TRAIN_WITH_PARTNER(true),

    /**
     * Attack protect in a certain direction.
     */
    KNIGHT_ATTACK_PROTECT(true),

        /*
### Crafter Workers ###
     */

    /**
     * Get the recipe.
     */
    GET_RECIPE(true),

    /**
     * Query the required items for a recipe.
     */
    QUERY_ITEMS(true),

    /**
     * Execute the crafting action.
     */
    CRAFT(true),

        /*
### Crusher ###
     */

    /**
     * Let the crusher crush blocks.
     */
    CRUSH(true),

            /*
### Sifter ###
     */

    /**
     * Let the sifter sieve blocks.
     */
    SIFT(true),

            /*
### Florist ###
     */

    /**
     * Let the florist harvest a flower.
     */
    FLORIST_HARVEST(true),

    /**
     * Let the florist compost the block.
     */
    FLORIST_COMPOST(true),

            /*
### Enchanter ###
     */

    /**
     * Let the enchanter gather experience.
     */
    ENCHANTER_DRAIN(true),

    /**
     * Enchant ancient tome.
     */
    ENCHANT(false),

    /*
### Avoid-AI ###
     */

    /**
     * Checking it the Avoid AI should start.
     */
    SAFE(true),

    NEED_RUN(true),

    RUNNING(true);

    /**
     * Is it okay to eat.
     */
    private boolean isOkayToEat;

    /**
     * Create a new one.
     * @param okayToEat if okay.
     */
    AIWorkerState(final boolean okayToEat)
    {
        this.isOkayToEat = okayToEat;
    }

    /**
     * Method to check if it is okay.
     * @return true if so.
     */
    public boolean isOkayToEat()
    {
        return isOkayToEat;
    }
}
