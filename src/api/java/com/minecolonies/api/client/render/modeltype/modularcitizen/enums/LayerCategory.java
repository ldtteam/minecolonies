package com.minecolonies.api.client.render.modeltype.modularcitizen.enums;

/**
 * Holds the various types of texture categories, to provide unified access to describe the connection between textures and models.
 */
public enum LayerCategory
{
    /**
     * BASES holds the first and inner-most layer of the citizen module.  Normally used for sclera (aka "the whites of their eyes"), but is applied to the whole base model.
     * Does not have a color modifier applied.
     */
    BASES(TextureCategory.BASE, ModelCategory.BASE),
    /**
     * EYES hold the second layer, and first color-adjusted layer.  Normally used for pupils.  Only applied to the HEAD CitizenSlot model, using the base model.
     *  Color applied using the CitizenData.getEyeColor() modifier.
     */
    EYES(TextureCategory.EYES, ModelCategory.BASE),
    /**
     * SUFFIXES are per-limb layers, generally containing the colonist's skin and underclothes.  SUFFIXES layers are changed automatically to match if the CitizenSlot's clothing model layer requests a baseModel.
     *  Color applied using the CitizenData.getSuffixColor() modifier.
     */
    SUFFIXES(TextureCategory.SUFFIXES, ModelCategory.BASE),
    /**
     * ILLNESSES are a single texture applied across the whole body on the base model.
     */
    ILLNESS(TextureCategory.ILLNESS, ModelCategory.BASE),
    /**
     * CLOTHES layers are per-limb layers, generally containing work- or home-related clothing.  CLOTHES layer models containing a baseModel will force lower layers to adjust to match that model.
     * Does not have a color modifier applied.
     */
    CLOTHES(TextureCategory.CLOTHES, ModelCategory.CLOTHES),
    /**
     * HAIR layers contain the colonist's hair, with a color adjustment applied.
     */
    HAIR(TextureCategory.HAIR, ModelCategory.CLOTHES),
    /**
     * ACCESSORIES layers hold secondary parts of a model, not part of the citizen clothing.
     */
    ACCESSORIES(TextureCategory.CLOTHES, ModelCategory.ACCESSORY),
    /**
     * JOBS textures contain specialized job-specific data, such as miner torches and stones, or a fisher's catch.
     */
    JOBS(TextureCategory.JOBS, ModelCategory.CLOTHES);

    private final TextureCategory textureCategory;
    private final ModelCategory modelCategory;

    LayerCategory(final TextureCategory textureCategory, final ModelCategory modelCategory)
    {
        this.textureCategory = textureCategory;
        this.modelCategory = modelCategory;
    }

    public TextureCategory texture()
    {
        return this.textureCategory;
    }

    public ModelCategory model()
    {
        return this.modelCategory;
    }
}
