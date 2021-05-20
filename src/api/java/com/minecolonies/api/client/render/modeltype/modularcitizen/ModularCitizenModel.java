package com.minecolonies.api.client.render.modeltype.modularcitizen;

import com.google.gson.JsonObject;
import com.minecolonies.api.client.render.modeltype.modularcitizen.enums.ModelCategory;
import com.minecolonies.api.client.render.modeltype.modularcitizen.enums.CitizenSlots;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;

import static com.minecolonies.api.client.render.modeltype.modularcitizen.ModelRenderer.*;
import static com.minecolonies.coremod.generation.DataGeneratorConstants.GSON;

public class ModularCitizenModel extends BipedModel<AbstractEntityCitizen>
{
    private static final String MODEL_TYPE = "modelType";
    private static final String MODEL_BASE_MODEL = "baseModel";
    private static final String MODEL_TEXTURE_SIZE_X = "texX";
    private static final String MODEL_TEXTURE_SIZE_Y = "texY";
    private static final String MODEL_HIDE_BASE = "hideBase";
    private static final int MODEL_TEXTURE_DEFAULT_SIZE_X = 64;
    private static final int MODEL_TEXTURE_DEFAULT_SIZE_Y = 64;
    private boolean hideBase = false;

    /**
     * The model's base layer. If non-empty, will require the Slot to use a matching Base, if one is available.
     */
    public String baseModel = "";

    /**
     * Creates a modular citizen model.
     */
    public ModularCitizenModel()
    {
        super(0.0F, 0.0F, MODEL_TEXTURE_DEFAULT_SIZE_X, MODEL_TEXTURE_DEFAULT_SIZE_Y);
    }

    public ModularCitizenModel(final float size, final int texX, final int texY)
    {
        super(size, 0.0F, texX, texY);
    }

    /**
     * Creates a modular citizen model.
     */
    public ModularCitizenModel(final float size)
    {
        super(size);
    }

    /**
     * If true, this model assumes that any underlying textures should not be displayed.
     */
    public boolean getHideBase()
    {
        return hideBase;
    }

    /**
     * Sets a specific model slot component for a model.
     * @param slot       The slot to assign.
     * @param newModel   The model renderer to assign to the slot, or null to keep the current assignment, but set the slot visible.
     */
    public void setModelSection(@NotNull final CitizenSlots slot, @Nullable final net.minecraft.client.renderer.model.ModelRenderer newModel)
    {
        switch (slot)
        {
            case MODEL_HEAD:
                if(newModel != null)
                {
                    this.bipedHead = newModel;
                }
                this.bipedHead.showModel = true;
                break;
            case MODEL_HEAD_WEAR:
                if (newModel != null)
                {
                    this.bipedHeadwear = newModel;
                }
                this.bipedHeadwear.showModel = true;
                break;
            case MODEL_BODY:
                if (newModel != null)
                {
                    this.bipedBody = newModel;
                }
                this.bipedBody.showModel = true;
                break;
            case MODEL_LEFT_ARM:
                if (newModel != null)
                {
                    this.bipedLeftArm = newModel;
                }
                this.bipedLeftArm.showModel = true;
                break;
            case MODEL_RIGHT_ARM:
                if (newModel != null)
                {
                    this.bipedRightArm = newModel;
                }
                this.bipedRightArm.showModel = true;
                break;
            case MODEL_LEFT_LEG:
                if (newModel != null)
                {
                    this.bipedLeftLeg = newModel;
                }
                this.bipedLeftLeg.showModel = true;
                break;
            case MODEL_RIGHT_LEG:
                if (newModel != null)
                {
                    this.bipedRightLeg = newModel;
                }
                this.bipedRightLeg.showModel = true;
                break;
            default:
        }
    }

    /**
     * Gets a specific model slot component for a model, if present.
     * @param modelSection The slot for the model.
     * @return  The model renderer.
     */
    public net.minecraft.client.renderer.model.ModelRenderer getModelSection(@NotNull CitizenSlots modelSection)
    {
        switch(modelSection)
        {
            case MODEL_HEAD:
                return this.bipedHead;
            case MODEL_HEAD_WEAR:
                return this.bipedHeadwear;
            case MODEL_BODY:
                return this.bipedBody;
            case MODEL_LEFT_ARM:
                return this.bipedLeftArm;
            case MODEL_RIGHT_ARM:
                return this.bipedRightArm;
            case MODEL_LEFT_LEG:
                return this.bipedLeftLeg;
            case MODEL_RIGHT_LEG:
                return this.bipedRightLeg;
            default:
                return null;
        }
    }

    /**
     * Parses a modular citizen model from its json representation.
     * @param res        The resource location of a JSON representing a model.
     * @param category   The category of model this resource belongs to.
     */
    public ModularCitizenModel(final ResourceLocation res, final ModelCategory category)
    {
        super(0.0F);
        this.setVisible(false);

        final JsonObject modelObj = getJsonFromResourceLocation(res);

        if(modelObj == null || !modelObj.has(MODEL_TYPE))
        {
            return;
        }
        if(modelObj.has(MODEL_BASE_MODEL))
        {
            this.baseModel = modelObj.get(MODEL_BASE_MODEL).getAsString();
        }
        this.hideBase = modelObj.has(MODEL_HIDE_BASE) && modelObj.get(MODEL_HIDE_BASE).getAsBoolean();

        final ModelRenderer newModel = parseFromJsonObject(modelObj,this, category);
        if(newModel == null)
        {
            return;
        }
        // These values are never used unless further models are programmatically added. But best to be consistent.
        this.textureHeight = (int)newModel.texXSize;
        this.textureHeight = (int)newModel.texYSize;
        // This can, potentially, set a model's content to null.  This is allowed, as a simple way to disable certain limbs or other components.
        setModelSection(CitizenSlots.value(modelObj.get(MODEL_TYPE).getAsString()), newModel);
    }

    @Nullable
    public static JsonObject getJsonFromResourceLocation(final ResourceLocation res)
    {
        final Minecraft mc = Minecraft.getInstance();
        if (mc == null)
        {
            throw new UnsupportedOperationException("Attempted to open a model renderer when the ResourceManager was not available.");
        }
        final JsonObject modelObj;
        try
        {
            modelObj = GSON.fromJson(new InputStreamReader(mc.getResourceManager().getResource(res).getInputStream()), JsonObject.class);
        }
        catch (final IOException e)
        {
            Log.getLogger().error("Unable to read " + res + " due to " + e);
            return null;
        }
        return modelObj;
    }

    @Nullable
    public static ModelRenderer parseFromJsonObject(@NotNull final JsonObject modelObj, final Model parentModel, final ModelCategory category)
    {
        parentModel.textureWidth = modelObj.has(MODEL_TEXTURE_SIZE_X) ? modelObj.get(MODEL_TEXTURE_SIZE_X).getAsInt() : MODEL_TEXTURE_DEFAULT_SIZE_X;
        parentModel.textureHeight = modelObj.has(MODEL_TEXTURE_SIZE_Y) ? modelObj.get(MODEL_TEXTURE_SIZE_Y).getAsInt() : MODEL_TEXTURE_DEFAULT_SIZE_Y;
        if (modelObj.has(MODEL_CHILD_MODELS))
        {
            return new ModelRenderer(parentModel, modelObj.getAsJsonObject(MODEL_CHILD_MODELS), parentModel.textureWidth, parentModel.textureHeight, category.getSizeModifier());
        }
        return null;
    }

    /**
     * Serializes the model into a JSON format, section by section.  This should only be called during DataGen, and will not save data outside of it.
     * @param modelSection   The slot of the section to serialize.
     * @param category       The category of data to serialize.
     * @param gender         The gender of the model.
     * @return A Json Object.
     */
    public JsonObject serializeToJson(@NotNull final CitizenSlots modelSection, final ModelCategory category, final String gender)
    {
        final JsonObject modelJson = new JsonObject();
        modelJson.addProperty(MODEL_TYPE, modelSection.getName());
        modelJson.addProperty(MODEL_BASE_MODEL, this.baseModel);
        // Avoid writing contents for models that solely contain default components.
        // MineColonies (and general BlockBench Java Models) don't do it outside of default citizen models, but others could plausibly depend on the vanilla model.
        final net.minecraft.client.renderer.model.ModelRenderer model = getModelSection(modelSection);
        if(model instanceof ModelRenderer)
        {
            final ModelRenderer modelReadable = (ModelRenderer)model;
            // Avoid writing the most common texture sizes to file.
            if(Math.abs(modelReadable.texXSize - MODEL_TEXTURE_DEFAULT_SIZE_X) > 0.1)
            {
                modelJson.addProperty(MODEL_TEXTURE_SIZE_X, modelReadable.texXSize);
            }
            if(Math.abs(modelReadable.texYSize - MODEL_TEXTURE_DEFAULT_SIZE_Y) > 0.1)
            {
                modelJson.addProperty(MODEL_TEXTURE_SIZE_Y, modelReadable.texYSize);
            }
            if(!model.showModel)
            {
                modelJson.addProperty(MODEL_HIDE_BASE, true);
            }
            final JsonObject childJson = modelReadable.serializeToJSON(category, modelSection == CitizenSlots.MODEL_BODY && gender.equals("female"));
            if(childJson != null)
            {
                modelJson.add(MODEL_CHILD_MODELS, childJson);
            }
            else
            {
                // don't need to create or save empty accessories.
                return null;
            }
        }
        else
        {
            // don't need to create or save accessories that only contain the default model.
            return null;
        }
        return modelJson;
    }
}
