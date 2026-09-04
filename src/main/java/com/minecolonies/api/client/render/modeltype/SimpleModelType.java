package com.minecolonies.api.client.render.modeltype;

import net.minecraft.resources.Identifier;

/**
 * A class that implements the ISimpleModelType interface. Used to store references to female and male models for citizens.
 */
public class SimpleModelType implements ISimpleModelType
{
    /**
     * Halloween style string. Null = uninitialized.
     */
    static String cachedHalloweenStyle = null;

    /**
     * String describing the citizen. Used by the renderer. Starts with a capital, and does not contain spaces or other special characters.
     */
    private final Identifier name;

    /**
     * Amount of different textures available for the renderer.
     */
    private final int numTextures;

    /**
     * The male model for the renderer.
     */
    private final CitizenModel<CitizenRenderState> maleModel;

    /**
     * The female model for the renderer.
     */
    private final CitizenModel<CitizenRenderState> femaleModel;

    public SimpleModelType(
      final Identifier name,
      final int numTextures,
      final CitizenModel<CitizenRenderState> maleModel,
      final CitizenModel<CitizenRenderState> femaleModel)
    {
        this.name = name;
        this.numTextures = numTextures;
        this.maleModel = maleModel;
        this.femaleModel = femaleModel;
    }

    @Override
    public Identifier getName()
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
    public CitizenModel<CitizenRenderState> getMaleModel()
    {
        return maleModel;
    }

    @Override
    public CitizenModel<CitizenRenderState> getFemaleModel()
    {
        return femaleModel;
    }
}
