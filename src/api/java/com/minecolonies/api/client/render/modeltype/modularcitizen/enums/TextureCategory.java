package com.minecolonies.api.client.render.modeltype.modularcitizen.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes the Texture Categories for modular colonists, as well as if they are color-adjusted.
 */
public enum TextureCategory
{
    /**
     * BASE textures hold the first layer, generally the sclera (or "whites of their eyes"), but may be useful for SUFFIX model components that can not have color added.
     */
    BASE("base"),
    /**
     * EYES textures hold the pupils of the citizen, but may also be useful for other model components that need a pastel-ish color range.
     */
    EYES("eyes", true),
    /**
     * SUFFIXES textures hold the citizen body and underclothes. Pale areas of the model will be effected most by color adjustment.
     */
    SUFFIXES("suffixes", true),
    /**
     * ILLNESS textures hold variant textures for sick colonists.
     */
    ILLNESS("illness"),
    /**
     * CLOTHES textures hold the non-color-adjusted components set by the colonist job or home.
     */
    CLOTHES("clothes"),
    /**
     * HAIR textures hold the colonist hair.
     */
    HAIR("hair", true),
    /**
     * JOB textures hold variant textures applied based on job statuses. These textures generally have reserved names or special properties based on worker traits.
     */
    JOBS("jobs");

    private static final Map<String, TextureCategory> categories = new HashMap<>();
    static
    {
        for (TextureCategory category : TextureCategory.values())
        {
            categories.put(category.getName(), category);
        }
    }

    private final String category;
    private final boolean isColorAdjusted;

    TextureCategory(final String component)
    {
        this.category = component;
        this.isColorAdjusted = false;
    }

    TextureCategory(final String component, final boolean isColorAdjusted)
    {
        this.category = component;
        this.isColorAdjusted = isColorAdjusted;
    }


    public String getName()
    {
        return category;
    }

    public boolean isColorAdjusted()
    {
        return isColorAdjusted;
    }

    public static TextureCategory value(final String component)
    {
        return categories.get(component);
    }
}