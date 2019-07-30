package com.minecolonies.coremod.client.render;

/**
 * Enum with possible citizens.
 */
public enum BipedModelType
{
    SETTLER("Settler", 3),
    CITIZEN("Citizen", 3),
    NOBLE("Noble", 3),
    ARISTOCRAT("Aristocrat", 3),
    BUILDER("Builder", 1),
    DELIVERYMAN("Deliveryman", 1),
    MINER("Miner", 1),
    // Lumberjack: 4 male, 1 female
    LUMBERJACK("lumberjack", 1),
    FARMER("farmer", 1),
    FISHERMAN("fisherman", 1),
    ARCHER_GUARD("archer", 1),
    KNIGHT_GUARD("knight", 1),
    BAKER("baker", 1),
    SHEEP_FARMER("sheepfarmer", 1),
    COW_FARMER("cowfarmer", 1),
    PIG_FARMER("pigfarmer", 1),
    CHICKEN_FARMER("chickenfarmer", 1),
    COMPOSTER("composter", 1),
    SMELTER("smelter", 1),
    COOK("cook", 1),
    STUDENT("student", 6),
    CRAFTER("crafter", 1),
    BLACKSMITH("Blacksmith", 1);

    /**
     * String describing the citizen.
     * Used by the renderer.
     * Starts with a capital, and does not contain spaces or other special characters.
     */
    public final String textureBase;

    /**
     * Amount of different textures available for the renderer.
     */
    public final int numTextures;

    BipedModelType(final String textureBase, final int numTextures)
    {
        this.textureBase = textureBase;
        this.numTextures = numTextures;
    }
}
