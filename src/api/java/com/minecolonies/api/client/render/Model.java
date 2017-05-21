package com.minecolonies.api.client.render;

/**
 * Enum with possible citizens.
 * //TODO: Rename this to something much more logical please!
 */
public enum Model
{
    SETTLER("Settler", 3),
    CITIZEN("Citizen", 3),
    NOBLE("Noble", 3),
    ARISTOCRAT("Aristocrat", 3),
    BUILDER("Builder", 1),
    DELIVERYMAN("Deliveryman", 1),
    MINER("Miner", 1),
    // Lumberjack: 4 male, 1 female
    LUMBERJACK("Lumberjack", 1),
    FARMER("Farmer", 1),
    FISHERMAN("Fisherman", 1),
    ARCHER_GUARD("Archer", 1),
    KNIGHT_GUARD("Knight", 1);

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

    Model(final String textureBase, final int numTextures)
    {
        this.textureBase = textureBase;
        this.numTextures = numTextures;
    }
}
