package com.minecolonies.api.client.render.modeltype.modularcitizen;

import com.google.gson.JsonObject;
import com.minecolonies.api.client.render.modeltype.modularcitizen.enums.CitizenSlots;
import com.minecolonies.api.client.render.modeltype.modularcitizen.enums.ModelCategory;
import com.minecolonies.api.client.render.modeltype.modularcitizen.enums.TextureCategory;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This is a simple holder class for ResourceLocation tokens for a given modular citizen component.
 * It's here to provide saner/safer access to the body components without passing or parsing a ton of strings in other files,
 * and to simplify debugging instead of having unnecessarily deep HashMap stacks.
 * This class is only populated or safe on the client side.
 */
public class ModularCitizenResourceContainer
{
    /**
     * Current default supported styles.
     */
    public static String[] STYLES = new String[]{ "default", "medieval" };

    /**
     * Current supported genders.
     */
    public static final String[] GENDERS = new String[]{ "male", "female" };

    /**
     * A Map of models by category, then by slot, to a list of resource locations matching that model.
     */
    protected Map<ModelCategory, Map<CitizenSlots, List<ResourceLocation>>> models = new EnumMap<>(ModelCategory.class);

    /**
     * A list of main textures by ResourceLocation.
     */
    protected Map<TextureCategory, List<ResourceLocation>> textures = new EnumMap<>(TextureCategory.class);

    /**
     * Both parsing and storing limb models are relatively expensive, so we'll adopt a naive lazy-loading approach;
     * players are unlikely to load every model for a style until very late-game, and may never load some styles.
     * This can likely be further improved by de-duplication efforts, as (many) different models have the same content, if required.
     * Deeper solution may involve move the basic rendering unit from BipedModel to the individual ModelRenderers, if total memory consumption is too high.
     */
    private final Map<ResourceLocation, ModularCitizenModel> modelRenderers = new HashMap<>();

    /**
     * Parsing and storing full models are even worse.  For now, only support one fullModel per model category.
     * Full models are mostly only relevant internally for Base models, where they allow better-performance transformation for the eyes and suffix layers.
     * They may also be useful externally or for display purposes.
     */
    private ModularCitizenModel fullModel;

    /**
     * Add a model to the Resource Container, for a specific slot.
     * @param category  the type of model to register.
     * @param modelPart the slot to register into.
     * @param res       the resource location of the model being added.
     */
    public void putModel(final ModelCategory category, final CitizenSlots modelPart, final ResourceLocation res)
    {
        if(!models.containsKey(category))
        {
            models.put(category, new EnumMap<>(CitizenSlots.class));
        }
        if(!models.get(category).containsKey(modelPart))
        {
            models.get(category).put(modelPart, new ArrayList<>());
        }
        models.get(category).get(modelPart).add(res);
    }

    /**
     * Add a texture to the Resource Container.
     * @param category      The type of texture file.
     * @param res           The resource location of the texture.
     */
    public void putTexture(final TextureCategory category, final ResourceLocation res)
    {
        if(!textures.containsKey(category))
        {
            textures.put(category, new ArrayList<>());
        }
        textures.get(category).add(res);
    }

    /**
     * Get a main texture from this container.
     * @param id  A variant identifier.
     * @return    the resource location of the texture.
     * May return null, if the container has no valid textures.
     */
    @Nullable
    public ResourceLocation getTexture(final TextureCategory category, final int id)
    {
        if(textures.get(category) == null || textures.get(category).size() == 0)
        {
            return null;
        }
        return textures.get(category).get(id % textures.get(category).size());
    }

    /**
     * Get a single part model from this container.
     * @param modelPart the Slot to retrieve.
     * @param id        the variant number.
     * @return a model matching the slot, or null if no matching models have been registered.
     */
    @Nullable
    public ModularCitizenModel getModel(final ModelCategory category, final CitizenSlots modelPart, final int id)
    {
        if(models.get(category) == null)
        {
            return null;
        }
        final List<ResourceLocation> mod = models.get(category).get(modelPart);
        if(mod == null || mod.size() == 0)
        {
            return null;
        }
        final ResourceLocation res = mod.get(id % mod.size());
        if(modelRenderers.containsKey(res))
        {
            return modelRenderers.get(res);
        }
        // TODO: this is a much bulkier way of handling the models in memory than necessary, effectively creating a full BipedModel for each ModelRenderer.
        //  It's only stored in memory, since the other ModelRenderers are hidden, but consider using EntityModels as the store of data.
        final ModularCitizenModel newModel = new ModularCitizenModel(res, category);
        modelRenderers.put(res, newModel);
        return newModel;
    }

    /**
     * Get a full model assembled from this container, for a given category.
     *
     */
    @Nullable
    public ModularCitizenModel getFullModel(final ModelCategory category)
    {
        if(fullModel != null)
        {
            return fullModel;
        }
        if(!models.containsKey(category) || models.get(category).size() == 0)
        {
            return null;
        }
        fullModel = new ModularCitizenModel();
        for(final CitizenSlots slot : CitizenSlots.values())
        {
            if(models.get(category).containsKey(slot) && models.get(category).get(slot).size() != 0)
            {
                final JsonObject modelJson = ModularCitizenModel.getJsonFromResourceLocation(models.get(category).get(slot).get(0));
                if(modelJson != null)
                {
                    fullModel.setModelSection(slot, ModularCitizenModel.parseFromJsonObject(modelJson, fullModel, category));
                }
            }
        }
        return fullModel;
    }
}
