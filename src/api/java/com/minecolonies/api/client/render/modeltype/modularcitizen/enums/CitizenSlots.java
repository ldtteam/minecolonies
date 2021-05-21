package com.minecolonies.api.client.render.modeltype.modularcitizen.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * This enum describes the available model positions on a citizen that can hold models and textures.
 */
public enum CitizenSlots
{
    MODEL_HEAD("head"),
    MODEL_HEAD_WEAR("headwear"),
    MODEL_BODY("body"),
    MODEL_RIGHT_ARM("rightarm"),
    MODEL_LEFT_ARM("leftarm"),
    MODEL_RIGHT_LEG("rightleg"),
    MODEL_LEFT_LEG("leftleg");

    private final static Map<String, CitizenSlots> slots = new HashMap<>();
    static
    {
        for(CitizenSlots slot : CitizenSlots.values())
        {
            slots.put(slot.getName(), slot);
        }
    }

    private final String name;

    CitizenSlots(final String component)
    {
        this.name = component;
    }

    public String getName()
    {
        return name;
    }

    public static CitizenSlots value(final String component)
    {
        return slots.get(component);
    }
}
