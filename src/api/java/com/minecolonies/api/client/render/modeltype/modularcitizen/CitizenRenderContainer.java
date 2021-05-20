package com.minecolonies.api.client.render.modeltype.modularcitizen;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.modularcitizen.enums.CitizenSlots;
import com.minecolonies.api.client.render.modeltype.modularcitizen.enums.LayerCategory;
import com.minecolonies.api.client.render.modeltype.modularcitizen.enums.TextureCategory;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;

import java.util.EnumMap;
import java.util.Map;

/**
 * This is a simple class to hold settings regarding a ModularCitizen's current texture and model settings.
 * These are potentially called very often, so this class should be cautious about unnecessary look ups.
 */
public class CitizenRenderContainer
{
    // It's possible for the client to look for models before any are available to be requested.
    // This gives a single static fallback.
    private static final ModularCitizenModel DEFAULT_MODEL = new ModularCitizenModel();

    /**
     * The RGB values for the colonist eye color.
     */
    public float eyesRed;
    public float eyesBlue;
    public float eyesGreen;

    /**
     * The RGB values for the colonist 'suffix' color.
     */
    public float suffixRed;
    public float suffixBlue;
    public float suffixGreen;

    /**
     * The RGB values for the colonist hair colors.
     */
    public float hairRed;
    public float hairBlue;
    public float hairGreen;

    /**
     * The non-slot-based texture resource locations for the colonist.
     */
    public ResourceLocation textureBase = new ResourceLocation(Constants.MOD_ID, "textures/entity/modularcitizen/base.png");
    public ResourceLocation textureEyes = new ResourceLocation(Constants.MOD_ID, "textures/entity/modularcitizen/eyes.png");
    public ResourceLocation textureHair;
    public ResourceLocation textureIllness;

    /**
     * The slot-based textures for the colonist.
     */
    public Map<CitizenSlots, ResourceLocation> texturesSuffix = new EnumMap<>(CitizenSlots.class);
    public Map<CitizenSlots, ResourceLocation> texturesCloth  = new EnumMap<>(CitizenSlots.class);
    public Map<CitizenSlots, ResourceLocation> texturesAccessories = new EnumMap<>(CitizenSlots.class);

    /**
     * The non-slot-based texture resource locations for the colonist.
     */
    public ModularCitizenModel modelBase = DEFAULT_MODEL;

    /**
     * The slot-based textures for the colonist.
     */
    public Map<CitizenSlots, ModularCitizenModel> modelsSuffix      = new EnumMap<>(CitizenSlots.class);
    public Map<CitizenSlots, ModularCitizenModel> modelsCloth       = new EnumMap<>(CitizenSlots.class);
    public Map<CitizenSlots, ModularCitizenModel> modelsAccessories = new EnumMap<>(CitizenSlots.class);

    /**
     * Update the CitizenRenderStatus based on the work or home status.
     * @param style           The colony style to be applied to the colonist.
     * @param isFemale        If true, will update to a female model.
     * @param setting         The setting, usually derived from BipedModelType.
     * @param variantNumber   The variant id of the colonist, a random number.
     */
    public void updateWorker(final String style, final boolean isFemale, final BipedModelType setting, final int variantNumber)
    {
        final ModularCitizenResourceContainer worker = IMinecoloniesAPI.getInstance().getCitizenResourceRegistry().getResourceContainer(isFemale, style, setting.getTextureBase());
        for(final CitizenSlots slot : CitizenSlots.values())
        {
            if(worker != null)
            {
                modelsAccessories.put(slot, worker.getModel(LayerCategory.ACCESSORIES.model(), slot, variantNumber));
                texturesAccessories.put(slot, worker.getTexture(LayerCategory.ACCESSORIES.texture(), variantNumber));
                modelsCloth.put(slot, worker.getModel(LayerCategory.CLOTHES.model(), slot, variantNumber));
                texturesCloth.put(slot, worker.getTexture(LayerCategory.CLOTHES.texture(), variantNumber));
            }
            else
            {
                modelsAccessories.remove(slot);
                texturesAccessories.remove(slot);
                modelsCloth.remove(slot);
                texturesCloth.remove(slot);
            }

            if (modelsCloth.get(slot) != null && !modelsCloth.get(slot).getHideBase())
            {
                final ModularCitizenResourceContainer suffixNew = IMinecoloniesAPI.getInstance().getCitizenResourceRegistry().getResourceContainer(isFemale, style, modelsCloth.get(slot).baseModel);
                if(suffixNew != null)
                {
                    // this mode may intentionally set models to null, if a setter is populated but has no matching models. This is intentional, generally as a lower-cost way to hide limbs for a worker state.
                    modelsSuffix.put(slot, suffixNew.getModel(LayerCategory.SUFFIXES.model(), slot, variantNumber));
                    texturesSuffix.put(slot, suffixNew.getTexture(LayerCategory.SUFFIXES.texture(), variantNumber));
                    if(slot == CitizenSlots.MODEL_HEAD)
                    {
                        modelBase = suffixNew.getFullModel(LayerCategory.SUFFIXES.model());
                    }
                }
            }
            else if(modelsCloth.get(slot) == null)
            {
                final ModularCitizenResourceContainer suffixReset = IMinecoloniesAPI.getInstance().getCitizenResourceRegistry().getResourceContainer(isFemale, style, isFemale ? "steve" : "alex");
                if(suffixReset != null)
                {
                    modelsSuffix.put(slot, suffixReset.getModel(LayerCategory.SUFFIXES.model(), slot, variantNumber));
                }
            }
            else
            {
                modelsSuffix.remove(slot);
            }
        }

        // For now, don't support separate "hair", and instead derive from accessories status.
        if(worker != null && worker.getModel(LayerCategory.HAIR.model(), CitizenSlots.MODEL_HEAD, variantNumber) != null)
        {
            // Intentionally allow textureHair to be set null, as a quick way to disable the hair texture.
            //modelHair = accessories.getModel(CitizenSlots.MODEL_HEAD, variantNumber);
            textureHair = worker.getTexture(LayerCategory.HAIR.texture(), variantNumber);
        }
    }

    /**
     * Masks used to convert RGB to integer and back using bit-shifting.
     */
    private final static int RED_MASK = 0xFF0000, GREEN_MASK = 0xFF00, BLUE_MASK = 0xFF;

    /**
     * Updates citizen data
     * @param hairColor   The citizen hair color, as a packed RGB int
     * @param eyeColor    The citizen eye color, as a packed RGB int
     * @param suffixColor The citizen suffix color, as a packed RGB int
     */
    public void updateCitizen(final int hairColor, final int eyeColor, final int suffixColor)
    {
        this.hairRed = ((hairColor & RED_MASK) >> 16) / 255F;
        this.hairGreen = ((hairColor & GREEN_MASK) >> 8) / 255F;
        this.hairBlue = (hairColor & BLUE_MASK) / 255F;

        this.eyesRed = ((eyeColor & RED_MASK) >> 16) / 255F;
        this.eyesGreen = ((eyeColor & GREEN_MASK) >> 8) / 255F;
        this.eyesBlue = (eyeColor & BLUE_MASK) / 255F;

        this.suffixRed = ((suffixColor & RED_MASK) >> 16) / 255F;
        this.suffixGreen = ((suffixColor & GREEN_MASK) >> 8) / 255F;
        this.suffixBlue = (suffixColor & BLUE_MASK) / 255F;
    }

    /**
     * Updates the citizen's illness status, or sets to no texture if no compatible illness is found.
     * @param style           The colonist style.
     * @param isFemale        If true, the returned texture will be for female colonists.
     * @param illnessVariant  The numeric illness variant. May exceed the number of known illnesses.
     */
    public void updateIllness(final String style, final boolean isFemale, final int illnessVariant)
    {
        textureIllness = IMinecoloniesAPI.getInstance().getCitizenResourceRegistry().getResourceContainer(isFemale, style, "illness").getTexture(TextureCategory.BASE, illnessVariant);
    }
}
