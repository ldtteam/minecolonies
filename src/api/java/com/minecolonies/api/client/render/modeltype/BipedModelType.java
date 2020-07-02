package com.minecolonies.api.client.render.modeltype;

/**
 * Enum with possible citizens.
 */
public enum BipedModelType implements ISimpleModelType
{
    SETTLER("settler", 3),
    CITIZEN("citizen", 3),
    NOBLE("noble", 3),
    ARISTOCRAT("aristocrat", 3),
    BUILDER("builder", 1),
    DELIVERYMAN("deliveryman", 1),
    MINER("miner", 1),
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
    BLACKSMITH("blacksmith", 1),
    CHILD("child", 4),
    HEALER("healer", 1),
    TEACHER("teacher", 1),
    GLASSBLOWER("glassblower", 3),
    DYER("dyer", 3),
    MECHANIST("mechanist", 1),
    FLETCHER("fletcher", 1),
    CONCRETE_MIXER("concretemixer", 1),
    RABBIT_HERDER("rabbitherder", 1),
    PLANTER("planter", 1),
    BEEKEEPER("beekeeper", 1);

    /**
     * String describing the citizen. Used by the renderer. Starts with a capital, and does not contain spaces or other special characters.
     */
    private final String textureBase;

    /**
     * Amount of different textures available for the renderer.
     */
    private final int numTextures;

    BipedModelType(final String textureBase, final int numTextures)
    {
        this.textureBase = textureBase;
        this.numTextures = numTextures;
    }

    @Override
    public String getName()
    {
        return this.name();
    }

    @Override
    public String getTextureBase()
    {
        return textureBase;
    }

    @Override
    public int getNumTextures()
    {
        return numTextures;
    }
}
