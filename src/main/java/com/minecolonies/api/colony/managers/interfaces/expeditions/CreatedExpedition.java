package com.minecolonies.api.colony.managers.interfaces.expeditions;

import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionBuilder;
import net.minecraft.resources.ResourceLocation;

/**
 * Container class for created expedition instances.
 *
 * @param id               the id of the expedition.
 * @param expeditionTypeId the expedition type for this expedition.
 * @param accepted         whether the expedition has already been accepted and is waiting for resources.
 */
public record CreatedExpedition(int id, ResourceLocation expeditionTypeId, boolean accepted)
{
    /**
     * Accepts this expedition instance.
     *
     * @return a copy of this instance with {@link CreatedExpedition#accepted} set to true.
     */
    public CreatedExpedition accept()
    {
        return new CreatedExpedition(id, expeditionTypeId, true);
    }

    /**
     * Turn this created expedition into a full-fledged expedition instance.
     *
     * @param builder the builder containing additional information.
     * @return the complete expedition instance.
     */
    public ColonyExpedition createExpedition(final ColonyExpeditionBuilder builder)
    {
        return builder.build(id, expeditionTypeId);
    }
}
