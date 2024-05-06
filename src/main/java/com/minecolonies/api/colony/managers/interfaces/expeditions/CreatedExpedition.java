package com.minecolonies.api.colony.managers.interfaces.expeditions;

import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

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
     * @param members   the list of members.
     * @param equipment the list of equipment.
     * @return the complete expedition instance.
     */
    public ColonyExpedition createExpedition(final List<IExpeditionMember<?>> members, final List<ItemStack> equipment)
    {
        return new ColonyExpedition(id, expeditionTypeId, members.stream().collect(Collectors.toMap(IExpeditionMember::getId, v -> v)), equipment);
    }
}
