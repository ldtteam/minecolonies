package com.minecolonies.api.client.render.modeltype.registry;

import com.minecolonies.api.client.render.modeltype.modularcitizen.ModularCitizenResourceContainer;

public interface ICitizenResourceRegistry
{
    /**
     * Request the Resource Container for a particular colony style, gender, and setting identifier.
     * This contains the textures and models for a modular colonist in that context.
     * @param isFemale           set to true to get the female colonist container.
     * @param style              the style to request. If no matching style is loaded, returns the "default" style.
     * @param settingIdentifier  the setting identifier to request. Base setting identifiers are usually "body", "eyes", or "illness".
     * @return  a modular citizen resource container closest matching the request, or a default.
     */
    ModularCitizenResourceContainer getResourceContainer(final boolean isFemale, final String style, final String settingIdentifier);

    /**
     * Returns the state of the ResourceRegistry.
     * @return true, if the data is completely loaded.
     */
    boolean isLoaded();
}
