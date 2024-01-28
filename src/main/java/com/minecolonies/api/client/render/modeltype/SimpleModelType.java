package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.resources.ResourceLocation;

/**
 * A class that implements the ISimpleModelType interface. Used to store references to female and male models for citizens.
 */
public class SimpleModelType implements ISimpleModelType
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

    public SimpleModelType(
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
    public String getTextureBase()
    {
        return name.getPath();
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
    public CitizenModel<AbstractEntityCitizen> getFemaleModel()
    {
        return femaleModel;
    }
}
