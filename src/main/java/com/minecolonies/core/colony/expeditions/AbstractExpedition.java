package com.minecolonies.core.colony.expeditions;

import com.minecolonies.api.colony.expeditions.IExpedition;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

import static com.minecolonies.api.util.constant.ExpeditionConstants.EXPEDITION_STAGE_WILDERNESS;

/**
 * Class for an expedition instance.
 */
public abstract class AbstractExpedition implements IExpedition
{
    /**
     * Nbt tag constants.
     */
    private static final String TAG_EQUIPMENT   = "equipment";
    private static final String TAG_LEADER      = "leader";
    private static final String TAG_MEMBERS     = "members";
    private static final String TAG_MEMBER_TYPE = "memberType";
    private static final String TAG_RESULTS     = "results";

    /**
     * The leader of the expedition.
     */
    @NotNull
    protected final IExpeditionMember<?> leader;

    /**
     * The members of the expedition.
     */
    @NotNull
    protected final Map<Integer, IExpeditionMember<?>> members;

    /**
     * The equipment given to the expedition prior to starting.
     */
    @NotNull
    protected final List<ItemStack> equipment;

    /**
     * The results of this expedition.
     */
    protected final Deque<ExpeditionStage> results;

    /**
     * The current active members of the expedition.
     */
    @Nullable
    private List<IExpeditionMember<?>> activeMembersCache;

    /**
     * Deserialization constructor.
     *
     * @param leader    the leader of the expedition.
     * @param members   the members for this expedition.
     * @param equipment the list of equipment for this expedition.
     * @param results   the results for this expedition.
     */
    protected AbstractExpedition(
      final @NotNull IExpeditionMember<?> leader,
      final @NotNull Map<Integer, IExpeditionMember<?>> members,
      final @NotNull List<ItemStack> equipment,
      final @NotNull List<ExpeditionStage> results)
    {
        this.leader = leader;
        this.members = Collections.unmodifiableMap(members);
        this.equipment = Collections.unmodifiableList(equipment);
        this.results = new ArrayDeque<>(results);
    }

    /**
     * Create an expedition instance from compound data.
     *
     * @param compound the compound data.
     */
    public static <T extends AbstractExpedition> T loadFromNBT(final CompoundTag compound, final ExpeditionCreator<T> creator)
    {
        final IExpeditionMember<?> leader = readMember(compound.getCompound(TAG_LEADER));

        final List<IExpeditionMember<?>> members = new ArrayList<>();
        final ListTag membersList = compound.getList(TAG_MEMBERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < membersList.size(); ++i)
        {
            final IExpeditionMember<?> member = readMember(membersList.getCompound(i));
            if (member != null)
            {
                members.add(member);
            }
        }

        final List<ItemStack> equipment = new ArrayList<>();
        final ListTag equipmentList = compound.getList(TAG_EQUIPMENT, Tag.TAG_COMPOUND);
        for (int i = 0; i < equipmentList.size(); ++i)
        {
            equipment.add(ItemStack.of(equipmentList.getCompound(i)));
        }

        final List<ExpeditionStage> results = new ArrayList<>();
        final ListTag resultsList = compound.getList(TAG_RESULTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < resultsList.size(); ++i)
        {
            results.add(ExpeditionStage.loadFromNBT(resultsList.getCompound(i)));
        }

        return creator.create(leader, members, equipment, results);
    }

    /**
     * Read single member data from NBT.
     *
     * @param compound the compound data.
     * @return the list of members.
     */
    @Nullable
    public static IExpeditionMember<?> readMember(final CompoundTag compound)
    {
        final String memberType = compound.getString(TAG_MEMBER_TYPE);
        if (Objects.equals(memberType, "citizen"))
        {
            return new ExpeditionCitizenMember(compound);
        }
        else if (Objects.equals(memberType, "visitor"))
        {
            return new ExpeditionVisitorMember(compound);
        }
        return null;
    }

    /**
     * Write a single member data to NBT.
     *
     * @param compound the compound data.
     * @param member   the member.
     */
    public static void writeMember(final CompoundTag compound, final IExpeditionMember<?> member)
    {
        if (member instanceof ExpeditionCitizenMember)
        {
            compound.putString(TAG_MEMBER_TYPE, "citizen");
        }
        else if (member instanceof ExpeditionVisitorMember)
        {
            compound.putString(TAG_MEMBER_TYPE, "visitor");
        }
        member.write(compound);
    }

    @Override
    @NotNull
    public IExpeditionMember<?> getLeader()
    {
        return leader;
    }

    @Override
    @NotNull
    public List<IExpeditionMember<?>> getMembers()
    {
        return this.members.values().stream().toList();
    }

    @Override
    @NotNull
    public List<ItemStack> getEquipment()
    {
        return equipment;
    }

    @Override
    @NotNull
    public List<IExpeditionMember<?>> getActiveMembers()
    {
        if (activeMembersCache == null)
        {
            final Stream<IExpeditionMember<?>> aliveMembers = this.members.values().stream().filter(f -> !f.isDead());
            if (this.leader.isDead())
            {
                activeMembersCache = aliveMembers.toList();
            }
            else
            {
                activeMembersCache = Stream.concat(Stream.of(this.leader), aliveMembers).toList();
            }
        }

        return activeMembersCache;
    }

    @Override
    @NotNull
    public List<ExpeditionStage> getResults()
    {
        return this.results.stream().toList();
    }

    @Override
    public void advanceStage(final Component header)
    {
        cleanStages();

        // In case 2 identical structures hit back-to-back, we want to assume its one large structure.
        final ExpeditionStage stage = getCurrentStage();
        if (!stage.getHeader().equals(header))
        {
            this.results.add(new ExpeditionStage(header));
        }
    }

    @Override
    public void rewardFound(final ItemStack itemStack)
    {
        getCurrentStage().addReward(itemStack);
    }

    @Override
    public void mobKilled(final ResourceLocation encounterId)
    {
        getCurrentStage().addKill(encounterId);
    }

    @Override
    public void memberLost(final IExpeditionMember<?> member)
    {
        getCurrentStage().memberLost(member.getId());
        activeMembersCache = null;
    }

    /**
     * Write this expedition builder to compound data.
     *
     * @param compound the compound tag.
     */
    @Override
    public void write(final CompoundTag compound)
    {
        final CompoundTag leaderCompound = new CompoundTag();
        writeMember(leaderCompound, leader);
        compound.put(TAG_LEADER, leaderCompound);

        final ListTag membersCompound = new ListTag();
        for (final IExpeditionMember<?> member : members.values())
        {
            final CompoundTag memberCompound = new CompoundTag();
            writeMember(memberCompound, member);
            membersCompound.add(memberCompound);
        }
        compound.put(TAG_MEMBERS, membersCompound);

        final ListTag equipmentCompound = new ListTag();
        for (final ItemStack itemStack : equipment)
        {
            equipmentCompound.add(itemStack.serializeNBT());
        }
        compound.put(TAG_EQUIPMENT, equipmentCompound);

        final ListTag resultsCompound = new ListTag();
        for (final ExpeditionStage result : results)
        {
            final CompoundTag resultCompound = new CompoundTag();
            result.write(resultCompound);
            resultsCompound.add(resultCompound);
        }
        compound.put(TAG_RESULTS, resultsCompound);
    }

    /**
     * Clean up empty stages from the last stage back to the front.
     */
    public void cleanStages()
    {
        if (results.isEmpty())
        {
            return;
        }

        final ExpeditionStage current = results.getLast();
        if (current.getRewards().isEmpty() && current.getKills().isEmpty() && current.getMembersLost().isEmpty())
        {
            results.removeLast();
            cleanStages();
        }
    }

    /**
     * The currently active stage, or create a default one if none exists just yet.
     *
     * @return the current expedition stage.
     */
    @NotNull
    private ExpeditionStage getCurrentStage()
    {
        if (results.isEmpty())
        {
            results.push(new ExpeditionStage(Component.translatable(EXPEDITION_STAGE_WILDERNESS)));
        }
        return results.getLast();
    }

    /**
     * Lambda method for creating the expedition instance.
     *
     * @param <T> the type of the expedition.
     */
    @FunctionalInterface
    public interface ExpeditionCreator<T extends AbstractExpedition>
    {
        /**
         * Callback for creating the new expedition.
         *
         * @param members   the members for this expedition.
         * @param equipment the list of equipment for this expedition.
         * @param results   the results for this expedition.
         * @return the new expedition instance.
         */
        T create(final IExpeditionMember<?> leader, final List<IExpeditionMember<?>> members, final List<ItemStack> equipment, final List<ExpeditionStage> results);
    }
}
