package com.minecolonies.core.colony.expeditions.colony;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.expeditions.ExpeditionStatus;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.core.colony.expeditions.Expedition;
import com.minecolonies.core.colony.expeditions.ExpeditionVisitorMember;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_DIMENSION;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;

/**
 * Class for a colony expedition instance.
 */
public final class ColonyExpedition extends Expedition
{
    /**
     * The id for this expedition.
     */
    private final int id;

    /**
     * The dimension to send the expedition to.
     */
    @NotNull
    private final ResourceKey<Level> dimensionId;

    /**
     * Deserialization constructor.
     *
     * @param dimensionId the target dimension for this expedition.
     * @param equipment   the list of equipment for this expedition.
     * @param members     the members for this expedition.
     */
    public ColonyExpedition(
      final int id,
      final @NotNull ResourceKey<Level> dimensionId,
      final @NotNull Collection<ItemStack> equipment,
      final @NotNull Collection<IExpeditionMember<?>> members)
    {
        super(equipment, members);
        this.id = id;
        this.dimensionId = dimensionId;
    }

    /**
     * Create a colony expedition instance from compound data.
     *
     * @param compound the compound data.
     * @return the expedition instance.
     */
    @NotNull
    public static ColonyExpedition loadFromNBT(final CompoundTag compound)
    {
        final Expedition base = Expedition.loadFromNBT(compound);

        final int id = compound.getInt(TAG_ID);
        final ResourceKey<Level> dimensionId = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(compound.getString(TAG_DIMENSION)));
        return new ColonyExpedition(id, dimensionId, base.getEquipment(), base.getMembers());
    }

    /**
     * Unique id for this expedition instance.
     *
     * @return the id.
     */
    public int getId()
    {
        return id;
    }

    /**
     * The dimension this expedition is going to.
     *
     * @return the dimension key.
     */
    @NotNull
    public ResourceKey<Level> getTargetDimension()
    {
        return this.dimensionId;
    }

    /**
     * Add a new member to this expedition, only possible when the status is still {@link ExpeditionStatus#CREATED}.
     *
     * @param member the new member to add.
     */
    public void addMember(final IExpeditionMember<?> member)
    {
        if (!status.equals(ExpeditionStatus.CREATED))
        {
            return;
        }

        members.put(member.getId(), member);
    }

    /**
     * Remove a member from this expedition, only possible when the status is still {@link ExpeditionStatus#CREATED}.
     *
     * @param member the new member to add.
     */
    public void removeMember(final IExpeditionMember<?> member)
    {
        if (!status.equals(ExpeditionStatus.CREATED))
        {
            return;
        }

        members.remove(member.getId());
    }

    /**
     * Set the equipment which is going to be used for this expedition, only possible when the status is still {@link ExpeditionStatus#CREATED}.
     *
     * @param equipment the new list of equipment.
     */
    public void setEquipment(final List<ItemStack> equipment)
    {
        if (!status.equals(ExpeditionStatus.CREATED))
        {
            return;
        }

        this.equipment.clear();
        this.equipment.addAll(equipment);
    }

    @Override
    public void write(final CompoundTag compound)
    {
        super.write(compound);
        compound.putInt(TAG_ID, id);
        compound.putString(TAG_DIMENSION, dimensionId.location().toString());
    }

    /**
     * Get the expeditionary visitor that is leading this expedition.
     *
     * @return the member instance.
     */
    @Nullable
    public ExpeditionVisitorMember getLeader()
    {
        for (final IExpeditionMember<?> member : members.values())
        {
            if (member instanceof ExpeditionVisitorMember visitorMember)
            {
                return visitorMember;
            }
        }
        return null;
    }

    /**
     * Comparator class for sorting guards in a predictable order in the window.
     */
    public static class GuardsComparator implements Comparator<ICitizenDataView>
    {
        /**
         * The expedition instance.
         */
        private final ColonyExpedition colonyExpedition;

        /**
         * Default constructor.
         *
         * @param colonyExpedition the expedition instance.
         */
        public GuardsComparator(final ColonyExpedition colonyExpedition)
        {
            this.colonyExpedition = colonyExpedition;
        }

        @Override
        public int compare(final ICitizenDataView guard1, final ICitizenDataView guard2)
        {
            if (colonyExpedition.members.containsKey(guard1.getId()) && colonyExpedition.members.containsKey(guard2.getId()))
            {
                return guard1.getName().compareTo(guard2.getName());
            }
            else if (colonyExpedition.members.containsKey(guard1.getId()))
            {
                return -1;
            }
            else if (colonyExpedition.members.containsKey(guard2.getId()))
            {
                return 1;
            }

            return guard1.getName().compareTo(guard2.getName());
        }
    }
}
