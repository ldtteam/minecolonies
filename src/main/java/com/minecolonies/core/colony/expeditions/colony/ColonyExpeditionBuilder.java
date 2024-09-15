package com.minecolonies.core.colony.expeditions.colony;

import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.api.colony.managers.interfaces.expeditions.ColonyExpedition;
import com.minecolonies.api.util.InventoryUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder class for expedition instances.
 */
public class ColonyExpeditionBuilder
{
    /**
     * The leader for the expedition.
     */
    private final IExpeditionMember<?> leader;

    /**
     * The map of all members assigned.
     */
    private final Map<Integer, IExpeditionMember<?>> members = new HashMap<>();

    /**
     * The list of all equipment provided on start.
     */
    private final List<ItemStack> equipment = new ArrayList<>();

    /**
     * Default constructor.
     *
     * @param leader the leader for the expedition.
     */
    public ColonyExpeditionBuilder(final IExpeditionMember<?> leader)
    {
        this.leader = leader;
        this.members.put(leader.getId(), leader);
    }

    /**
     * Get the leader for the expedition.
     *
     * @return the leader.
     */
    public IExpeditionMember<?> getLeader()
    {
        return leader;
    }

    /**
     * Add a member to the builder.
     *
     * @param member the member instance.
     */
    public void addMember(final IExpeditionMember<?> member)
    {
        this.members.putIfAbsent(member.getId(), member);
    }

    /**
     * Add equipment to the builder.
     *
     * @param itemStack the item stack.
     */
    public void addEquipment(final ItemStack itemStack)
    {
        this.equipment.add(itemStack);
    }

    /**
     * Build the final expedition instance.
     *
     * @param id               the input id of the expedition.
     * @param expeditionTypeId the expedition type.
     * @return the full expedition instance.
     */
    public ColonyExpedition build(int id, ResourceLocation expeditionTypeId)
    {
        return new ColonyExpedition(id, expeditionTypeId, members, InventoryUtils.processItemStackListAndMerge(equipment));
    }
}
