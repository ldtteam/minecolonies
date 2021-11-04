package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.util.ResourceLocation;

/**
 * Enum with possible citizens.
 */
public class BipedModelType implements ISimpleModelType
{
    /**
     * String describing the citizen. Used by the renderer. Starts with a capital, and does not contain spaces or other special characters.
     */
    private final ResourceLocation name;

    /**
     * Amount of different textures available for the renderer.
     */
    private final int numTextures;

    /**
     * The male model for the renderer.
     */
    private final CitizenModel<AbstractEntityCitizen> maleModel;

    /**
     * The female model for the renderer.
     */
    private final CitizenModel<AbstractEntityCitizen> femaleModel;

    public BipedModelType(
      final ResourceLocation name,
      final int numTextures,
      final CitizenModel<AbstractEntityCitizen> maleModel,
      final CitizenModel<AbstractEntityCitizen> femaleModel)
    {
        this.name = name;
        this.numTextures = numTextures;
        this.maleModel = maleModel;
        this.femaleModel = femaleModel;
    }

    @Override
    public ResourceLocation getName()
    {
        return this.name;
    }

    @Override
    public ResourceLocation getTextureBase()
    {
        return name;
    }

    @Override
    public int getNumTextures()
    {
        return numTextures;
    }

    @Override
    public CitizenModel<AbstractEntityCitizen> getMaleModel()
    {
        return maleModel;
    }

    @Override
    public CitizenModel<AbstractEntityCitizen> getFemaleMap()
    {
        return femaleModel;
    }
}
