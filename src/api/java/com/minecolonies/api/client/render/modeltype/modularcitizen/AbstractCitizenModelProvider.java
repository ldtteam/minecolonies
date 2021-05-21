package com.minecolonies.api.client.render.modeltype.modularcitizen;

import com.google.gson.JsonObject;
import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.client.render.modeltype.modularcitizen.enums.ModelCategory;
import com.minecolonies.api.client.render.modeltype.modularcitizen.enums.CitizenSlots;
import com.minecolonies.api.util.Log;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.client.render.modeltype.modularcitizen.ModularCitizenResourceContainer.GENDERS;
import static com.minecolonies.api.client.render.modeltype.modularcitizen.ModularCitizenResourceContainer.STYLES;
import static com.minecolonies.coremod.generation.DataGeneratorConstants.GSON;

/**
 * This class allows the serialization of Java ModularCitizenModels into a JSON format, later used for Modular Citizens.
 * Only Models using the {@link com.minecolonies.api.client.render.modeltype.modularcitizen.ModelRenderer} model variant
 * will be successfully serialized.
 */
public abstract class AbstractCitizenModelProvider implements IDataProvider
{
    /**
     * The data generator, used for saving output files.
     */
    protected final DataGenerator generator;

    /**
     * The output file path for JSON files.
     */
    private final Path modelOutputPath;

    private DirectoryCache cache;

    /**
     * The abstract variant of a CitizenModelProvider.
     * @param generator  the data generator.
     */
    public AbstractCitizenModelProvider(final DataGenerator generator)
    {
        this.generator = generator;
        this.modelOutputPath = generator.getOutputFolder().resolve("assets\\minecolonies\\models\\entity\\modularcitizen");
        ModelRenderer.IS_RUN_DATA = true;
    }

    /**
     * @return The name of the model provider.
     */
    @NotNull
    @Override
    public String getName()
    {
        return "Default Citizen Model Provider";
    }

    /**
     * Generate and save the models for this instance.
     * @param cache Directory Cache
     */
    @Override
    public void act(@NotNull final DirectoryCache cache)
    {
        this.cache = cache;
        for (final String style : STYLES)
        {
            for (final String gender : GENDERS)
            {
                for (final Map.Entry<IModelType, List<CitizenModel>> m : (gender.equals(GENDERS[1]) ? getFemaleModels(style).entrySet() : getMaleModels(style).entrySet()))
                {
                    for(final CitizenModel cm : m.getValue())
                    {
                        if (m.getKey() instanceof BipedModelType)
                        {
                            generateModels(cm, ((BipedModelType) m.getKey()).getTextureBase(), style, gender, ModelCategory.CLOTHES);
                        }
                    }
                }
            }
            for (String gender : GENDERS)
            {
                for (Map.Entry<String, CitizenModel> m : getBaseModels().entrySet())
                {
                    generateModels(m.getValue(), m.getKey(), style, gender, ModelCategory.BASE);
                }
            }
        }
    }

    /**
     * Gets the list of Base Models, used for the BASE, EYES, and SUFFIX layers.
     * Generates models for both male and female colonists.
     * @return Base models, mapped by their identifier.
     */
    public abstract Map<String, CitizenModel> getBaseModels();

    /**
     * Gets the list of Female Models, used for the CLOTHES and ACCESSORIES layers.
     * @param style the style of the model being provided.
     * @return Female citizen models, mapped by colony style.
     */
    public abstract Map<IModelType, List<CitizenModel>> getFemaleModels(final String style);

    /**
     * Gets the list of male models, used for the CLOTHES And ACCESSORIES layers.
     * @param style the style of the models being provided.
     * @return Male citizen models, mapped by colony style.
     */
    public abstract Map<IModelType, List<CitizenModel>> getMaleModels(final String style);

    /**
     * Generates JSONs for the provided model.
     * @param model     Input model.
     * @param type      Input type.
     * @param style     Input style.
     * @param gender    Input gender.  "male" and "female" are currently supported.
     * @param category  The {@link com.minecolonies.api.client.render.modeltype.modularcitizen.enums.ModelCategory}
     */
    private void generateModels(final ModularCitizenModel model, final String type, final String style, final String gender, final ModelCategory category)
    {
        for(final CitizenSlots slot : CitizenSlots.values())
        {
            try
            {
                sliceModels(model, style, gender, slot, category, type);
            }
            catch (final IOException e)
            {
                Log.getLogger().error("Error writing model : " + e);
            }
        }
    }

    /**
     * Slices the input model into serialized components, and saves those components to JSON.
     * @param m           The input model.
     * @param style       The input style.
     * @param gender      The input gender.
     * @param slot        The input slot.
     * @param category    The input category {@link com.minecolonies.api.client.render.modeltype.modularcitizen.enums.ModelCategory}
     * @param typeName    The input typeName, used as a Setting in Minecolonies.
     * @throws IOException if unable to save output file.
     */
    private void sliceModels(@NotNull final ModularCitizenModel m, final String style, final String gender, final CitizenSlots slot, final ModelCategory category, final String typeName)
      throws IOException
    {
        final JsonObject modelJson = m.serializeToJson(slot, category, gender);
        if (modelJson == null)
        {
            // don't need to create empty files.
            return;
        }
        // TODO: consider adding textures as a list here, rather than auto-discovery during resource pack load.
        // Doing so involves more procedural overhead, but handles cases where models are only compatible with a subset of textures, and reduces the startup cost during gameplay.
        // However, de conflicting where two resource packs add new textures to the same model may be unsolvable.
        Path p =
          modelOutputPath.resolve(style).resolve(gender).resolve(slot.getName()).resolve(category.getName()).resolve(typeName + ".json");
        IDataProvider.save(GSON, cache, modelJson, p);
        if (category == ModelCategory.CLOTHES)
        {
            sliceModels(m, style, gender, slot, ModelCategory.ACCESSORY, typeName);
        }
    }
}
