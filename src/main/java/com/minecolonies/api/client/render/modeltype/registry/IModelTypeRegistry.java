package com.minecolonies.api.client.render.modeltype.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.client.render.modeltype.IModelType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * The registry interface for model types.
 */
public interface IModelTypeRegistry
{
    /**
     * Gets the current instance of the model type registry.
     *
     * @return The model type registry instance.
     */
    static IModelTypeRegistry getInstance()
    {
        return IMinecoloniesAPI.getInstance().getModelTypeRegistry();
    }

    /**
     * Registry the given model type into the registry.
     *
     * @param modelType The model type to register.
     */
    void register(IModelType modelType);

    /**
     * Get the model type from the registry or null if it doesn't exist.
     *
     * @param name The name of the model in ResourceLocation format.
     * @return The model type or null if it doesn't exist.
     */
    @Nullable
    IModelType getModelType(ResourceLocation name);
}
