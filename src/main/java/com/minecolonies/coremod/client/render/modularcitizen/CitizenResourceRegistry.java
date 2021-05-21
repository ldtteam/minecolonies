package com.minecolonies.coremod.client.render.modularcitizen;

import com.minecolonies.api.client.render.modeltype.modularcitizen.ModularCitizenResourceContainer;
import com.minecolonies.api.client.render.modeltype.registry.ICitizenResourceRegistry;

import java.util.HashMap;
import java.util.Map;

public class CitizenResourceRegistry implements ICitizenResourceRegistry
{
    private boolean isLoaded = false;

    private static final String DEFAULT_STYLE = "default";

    // These registries contain ResourceLocations in the format Style -> Setting Identifier -> item, with ModularCitizenResourceContainer internally
    // handling the slots for the component.  Separated by gender and type to minimize how deep the Maps get, and to avoid potential references from other contexts.
    private Map<String, Map<String, ModularCitizenResourceContainer>> maleRegistry   = new HashMap<>();
    private Map<String, Map<String, ModularCitizenResourceContainer>> femaleRegistry  = new HashMap<>();

    /**
     * Sets whether the data in this registry is in a valid and complete state.
     * @param isLoaded if true, the CitizenResourceRegistry is available.
     */
    public void setLoaded(final boolean isLoaded)
    {
        this.isLoaded = isLoaded;
    }

    /**
     * Returns if the CitizenResourceRegistry has been fully loaded and populated.
     * @return
     */
    public boolean isLoaded() {return this.isLoaded; }

    /**
     * Updates the Citizen Resource Registry.
     * This approach _should_ be atomic-enough that the loaded functionality is overkill, but the JLS leaves a lot to implementation.
     * @param resourceMap A map of available resources, linking isFemale -> style -> SettingIentifier -> ResourceContainer
     */
    public void putResourceRegistry(Map<Boolean, Map<String, Map<String, ModularCitizenResourceContainer>>> resourceMap)
    {
        femaleRegistry = resourceMap.get(true);
        maleRegistry = resourceMap.get(false);
    }

    @Override
    public ModularCitizenResourceContainer getResourceContainer(final boolean isFemale, final String style, final String settingIdentifier)
    {
        if(isFemale)
        {
            return femaleRegistry.getOrDefault(style, femaleRegistry.get(DEFAULT_STYLE)).get(settingIdentifier);
        }
        return maleRegistry.getOrDefault(style, maleRegistry.get(DEFAULT_STYLE)).get(settingIdentifier);
    }
}
