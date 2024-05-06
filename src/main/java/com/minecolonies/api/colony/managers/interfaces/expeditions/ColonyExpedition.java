package com.minecolonies.api.colony.managers.interfaces.expeditions;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.core.colony.expeditions.AbstractExpedition;
import com.minecolonies.core.colony.expeditions.ExpeditionStage;
import com.minecolonies.core.colony.expeditions.ExpeditionVisitorMember;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;

/**
 * Class for a colony expedition instance.
 */
public final class ColonyExpedition extends AbstractExpedition
{
    /**
     * NBT tags.
     */
    private static final String TAG_EXPEDITION_TYPE = "expeditionType";

    /**
     * The id for this expedition.
     */
    private final int id;

    /**
     * The expedition type.
     */
    @NotNull
    private final ResourceLocation expeditionTypeId;

    /**
     * Default constructor.
     *
     * @param id               the id of the expedition.
     * @param expeditionTypeId the expedition type.
     * @param members          the members for this expedition.
     * @param equipment        the list of equipment for this expedition.
     */
    public ColonyExpedition(
      final int id,
      final @NotNull ResourceLocation expeditionTypeId,
      final @NotNull Map<Integer, IExpeditionMember<?>> members,
      final @NotNull List<ItemStack> equipment)
    {
        super(members, equipment, List.of());
        this.id = id;
        this.expeditionTypeId = expeditionTypeId;
    }

    /**
     * Deserialization constructor.
     *
     * @param members          the members for this expedition.
     * @param equipment        the list of equipment for this expedition.
     * @param results          the results for this expedition.
     * @param id               the id of the expedition.
     * @param expeditionTypeId the expedition type.
     */
    private ColonyExpedition(
      final int id,
      final @NotNull ResourceLocation expeditionTypeId,
      final @NotNull List<IExpeditionMember<?>> members,
      final @NotNull List<ItemStack> equipment,
      final @NotNull List<ExpeditionStage> results)
    {
        super(members.stream().collect(Collectors.toMap(IExpeditionMember::getId, v -> v)), equipment, results);
        this.id = id;
        this.expeditionTypeId = expeditionTypeId;
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
        final int id = compound.getInt(TAG_ID);
        final ResourceLocation expeditionTypeId = new ResourceLocation(compound.getString(TAG_EXPEDITION_TYPE));

        return AbstractExpedition.loadFromNBT(compound, (members, equipment, results) -> new ColonyExpedition(id, expeditionTypeId, members, equipment, results));
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
     * Get the expedition type id for this expedition.
     *
     * @return the expedition type id.
     */
    @NotNull
    public ResourceLocation getExpeditionTypeId()
    {
        return expeditionTypeId;
    }

    @Override
    public void write(final CompoundTag compound)
    {
        super.write(compound);
        compound.putInt(TAG_ID, id);
        compound.putString(TAG_EXPEDITION_TYPE, expeditionTypeId.toString());
    }

    /**
     * Get the expeditionary visitor that is leading this expedition.
     *
     * @return the member instance.
     */
    @NotNull
    public ExpeditionVisitorMember getLeader()
    {
        for (final IExpeditionMember<?> member : members.values())
        {
            if (member instanceof ExpeditionVisitorMember visitorMember)
            {
                return visitorMember;
            }
        }
        throw new IllegalStateException("Visitor leader from expedition is missing.");
    }

    /**
     * Comparator class for sorting guards in a predictable order in the window.
     */
    public static class GuardsComparator implements Comparator<ICitizenDataView>
    {
        /**
         * The set of active members.
         */
        private final Set<Integer> activeMembers;

        /**
         * Default constructor.
         *
         * @param activeMembers the set of active members.
         */
        public GuardsComparator(final Set<Integer> activeMembers)
        {
            this.activeMembers = activeMembers;
        }

        @Override
        public int compare(final ICitizenDataView guard1, final ICitizenDataView guard2)
        {
            if (activeMembers.contains(guard1.getId()) && activeMembers.contains(guard2.getId()))
            {
                return guard1.getName().compareTo(guard2.getName());
            }
            else if (activeMembers.contains(guard1.getId()))
            {
                return -1;
            }
            else if (activeMembers.contains(guard2.getId()))
            {
                return 1;
            }

            return guard1.getName().compareTo(guard2.getName());
        }
    }
}
