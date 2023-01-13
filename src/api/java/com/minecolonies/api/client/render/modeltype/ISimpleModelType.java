package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.DATA_STYLE;
import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.DATA_TEXTURE_SUFFIX;

public interface ISimpleModelType extends IModelType
{
    /**
     * Base folder for textures.
     */
    String BASE_FOLDER = "textures/entity/citizen/";

    /**
     * Default folder.
     */
    String DEFAULT_FOLDER = "default";

    /**
     * The base name of the texture. Is by default appended by a random textureId as well as the render info.
     *
     * @return The base file name.
     */
    String getTextureBase();

    /**
     * The available amount of textures in this model type.
     *
     * @return The amount of textures available.
     */
    int getNumTextures();

    /**
     * Method used to get the path to the texture every time it is updated on the entity. By default this uses the textureBase + sex marker + randomly assigned texture index +
     * metadata as a format.
     *
     * @param entityCitizen The citizen in question to get the path.
     * @return The path to the citizen.
     */
    default ResourceLocation getTexture(@NotNull final AbstractEntityCitizen entityCitizen)
    {
        final int moddedTextureId = (entityCitizen.getTextureId() % getNumTextures()) + 1;
        final String textureIdentifier =
          getName().getPath() + (entityCitizen.isFemale() ? "female" : "male") + moddedTextureId + entityCitizen.getEntityData().get(DATA_TEXTURE_SUFFIX);
        final ResourceLocation modified = new ResourceLocation(Constants.MOD_ID, BASE_FOLDER + entityCitizen.getEntityData().get(DATA_STYLE) + "/" + textureIdentifier + ".png");
        if (Minecraft.getInstance().getResourceManager().getResource(modified).isPresent())
        {
            return modified;
        }

        return new ResourceLocation(Constants.MOD_ID, BASE_FOLDER + DEFAULT_FOLDER + "/" + textureIdentifier + ".png");
    }

    default ResourceLocation getTextureIcon(@NotNull final AbstractEntityCitizen entityCitizen)
    {
        final int moddedTextureId = (entityCitizen.getTextureId() % getNumTextures()) + 1;
        final String textureIdentifier =
          getTextureBase() + (entityCitizen.isFemale() ? "female" : "male") + moddedTextureId + entityCitizen.getEntityData()
            .get(DATA_TEXTURE_SUFFIX);
        return new ResourceLocation(Constants.MOD_ID, "textures/entity_icon/citizen/" + entityCitizen.getEntityData().get(DATA_STYLE) + "/" + textureIdentifier + ".png");
    }
}
