package com.minecolonies.client.render;

import com.minecolonies.client.model.*;
import com.minecolonies.entity.EntityCitizen;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

/**
 * Renderer for the citizens.
 */
public class RenderBipedCitizen extends RenderBiped<EntityCitizen>
{
    private static final ModelBiped             defaultModelMale   = new ModelBiped();
    private static final ModelBiped             defaultModelFemale = new ModelEntityCitizenFemaleCitizen();
    private static final Map<Model, ModelBiped> idToMaleModelMap   = new EnumMap<>(Model.class);
    private static final Map<Model, ModelBiped> idToFemaleModelMap = new EnumMap<>(Model.class);
    static
    {
        idToMaleModelMap.put(Model.DELIVERYMAN, new ModelEntityDeliverymanMale());
        idToMaleModelMap.put(Model.LUMBERJACK, new ModelEntityLumberjackMale());
        idToMaleModelMap.put(Model.FARMER, new ModelEntityFarmerMale());
        idToMaleModelMap.put(Model.FISHERMAN, new ModelEntityFishermanMale());

        idToFemaleModelMap.put(Model.NOBLE, new ModelEntityCitizenFemaleNoble());
        idToFemaleModelMap.put(Model.ARISTOCRAT, new ModelEntityCitizenFemaleAristocrat());
        idToFemaleModelMap.put(Model.BUILDER, new ModelEntityBuilderFemale());
        idToFemaleModelMap.put(Model.DELIVERYMAN, new ModelEntityDeliverymanFemale());
        idToFemaleModelMap.put(Model.MINER, new ModelEntityMinerFemale());
        idToFemaleModelMap.put(Model.LUMBERJACK, new ModelEntityLumberjackFemale());
        idToFemaleModelMap.put(Model.FARMER, new ModelEntityFarmerFemale());
        idToFemaleModelMap.put(Model.FISHERMAN, new ModelEntityFishermanFemale());
    }
    /**
     * Renders model, see {@link RenderBiped}.
     *
     * @param renderManagerIn the RenderManager for this Renderer.
     */
    public RenderBipedCitizen(RenderManager renderManagerIn)
    {
        super(renderManagerIn, defaultModelMale, 0.5F);
        this.addLayer(new LayerBipedArmor(this));
    }

    @Override
    public void doRender(@NotNull EntityCitizen citizen, double d, double d1, double d2, float f, float f1)
    {
        modelBipedMain = citizen.isFemale() ?
                           idToFemaleModelMap.get(citizen.getModelID()) :
                                                                          idToMaleModelMap.get(citizen.getModelID());

        if (modelBipedMain == null)
        {
            modelBipedMain = citizen.isFemale() ? defaultModelFemale : defaultModelMale;
        }

        mainModel = modelBipedMain;

        super.doRender(citizen, d, d1, d2, f, f1);
    }

    @Override
    protected ResourceLocation getEntityTexture(@NotNull EntityCitizen entity)
    {
        return entity.getTexture();
    }

    /**
     * Enum with possible citizens.
     */
    public enum Model
    {
        SETTLER("Settler", 3),
        CITIZEN("Citizen", 3),
        NOBLE("Noble", 3),
        ARISTOCRAT("Aristocrat", 3),
        BUILDER("Builder", 1),
        DELIVERYMAN("Deliveryman", 1),
        MINER("Miner", 1),
        // Lumberjack: 4 male, 1 female
        LUMBERJACK("Lumberjack", 1),
        FARMER("Farmer", 1),
        FISHERMAN("Fisherman", 1),
        GUARD("Knight", 1);

        /**
         * String describing the citizen.
         * Used by the renderer.
         * Starts with a capital, and does not contain spaces or other special characters
         */
        public final String textureBase;
        /**
         * Amount of different textures available for the renderer
         */
        public final int    numTextures;

        Model(String textureBase, int numTextures)
        {
            this.textureBase = textureBase;
            this.numTextures = numTextures;
        }
    }
}
