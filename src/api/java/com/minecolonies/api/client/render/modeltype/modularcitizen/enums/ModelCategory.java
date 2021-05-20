package com.minecolonies.api.client.render.modeltype.modularcitizen.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes the current model categories, as well as their delta offset to avoid z-fighting.
 */
public enum ModelCategory
{
    /**
     * "Base" models represent the most minimal default state, which should always be present for a colonist in some form. They should always share the same or a similar model
     * design, and should be compatible with all models from this style. This also includes the body layers, eyes layer, illness, and style layers. Base layers may be
     * color-adjustable. These should use unique keywords, and not use the BipedModelTypes setters.
     */
    BASE("base", -0.002f),
    /**
     * "Cloth" models represent the general clothing worn by a colonist. These are generally (though not always) form-fitting and cover large portions of the colonist.
     */
    CLOTHES("cloth", 0f),
    /**
     * "Accessory" models represent additional components layered on top of clothing, but below armor. These are generally smaller items with significant volume, but less than
     * total coverage of their component. Accessories also include volumetric components of hair or non-standard body parts such as the noble parasol.
     */
    ACCESSORY("acc", 0.002f);

    private static final Map<String, ModelCategory> categories = new HashMap<>();
    static
    {
        for (ModelCategory category : ModelCategory.values())
        {
            categories.put(category.getName(), category);
        }
    }

    private final String name;
    private final float  sizeModifier;

    ModelCategory(final String component, final float sizeM)
    {
        this.name = component;
        this.sizeModifier = sizeM;
    }

    public String getName()
    {
        return name;
    }

    public float getSizeModifier() {return sizeModifier;}

    public static ModelCategory value(final String component)
    {
        return categories.get(component);
    }
}
