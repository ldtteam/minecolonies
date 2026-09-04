package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.MineColonies;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.Month;

import static com.minecolonies.api.client.render.modeltype.SimpleModelType.cachedHalloweenStyle;
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
    default Identifier getTexture(@NotNull final CitizenRenderState renderState)
    {
        if (cachedHalloweenStyle == null)
        {
            if (MineColonies.getConfig().getClient().holidayFeatures.get() &&
                ((LocalDateTime.now().getDayOfMonth() >= 29 && LocalDateTime.now().getMonth() == Month.OCTOBER)
                    || (LocalDateTime.now().getDayOfMonth() <= 2 && LocalDateTime.now().getMonth() == Month.NOVEMBER)))
            {
                cachedHalloweenStyle = "nether";
            }
            else
            {
                cachedHalloweenStyle = "";
            }
        }

        String style = renderState.getCitizen() != null ? renderState.getCitizen().getEntityData().get(DATA_STYLE) : "default";
        if (!cachedHalloweenStyle.isEmpty())
        {
            style = cachedHalloweenStyle;
        }

        final int moddedTextureId = (renderState.getCitizen().getTextureId() % getNumTextures()) + 1;
        final String textureIdentifier =
          getName().getPath() + (renderState.getCitizen().isFemale() ? "female" : "male") + moddedTextureId
            + renderState.getCitizen().getEntityData().get(DATA_TEXTURE_SUFFIX);
        final Identifier modified = Identifier.fromNamespaceAndPath(Constants.MOD_ID, BASE_FOLDER + style + "/" + textureIdentifier + ".png");
        if (Minecraft.getInstance().getResourceManager().getResource(modified).isPresent())
        {
            return modified;
        }

        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, BASE_FOLDER + DEFAULT_FOLDER + "/" + textureIdentifier + ".png");
    }

    default Identifier getTextureIcon(@NotNull final CitizenRenderState renderState)
    {
        String style = renderState.getCitizen() != null ? renderState.getCitizen().getEntityData().get(DATA_STYLE) : "default";
        if (cachedHalloweenStyle != null && !cachedHalloweenStyle.isEmpty())
        {
            style = cachedHalloweenStyle;
        }

        final int moddedTextureId = (renderState.getCitizen().getTextureId() % getNumTextures()) + 1;
        final String textureIdentifier =
          getTextureBase() + (renderState.getCitizen().isFemale() ? "female" : "male") + moddedTextureId
            + renderState.getCitizen().getEntityData().get(DATA_TEXTURE_SUFFIX);
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/entity_icon/citizen/" + style + "/" + textureIdentifier + ".png");
    }

    default Identifier getTextureIcon(@NotNull final com.minecolonies.api.entity.citizen.AbstractEntityCitizen citizen)
    {
        final CitizenRenderState renderState = new CitizenRenderState();
        renderState.setCitizen(citizen);
        return getTextureIcon(renderState);
    }
}
