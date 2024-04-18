package com.minecolonies.core.colony.expeditions;

import com.minecolonies.api.colony.expeditions.ExpeditionStatus;
import com.minecolonies.api.colony.expeditions.IExpedition;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

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
    private static final String TAG_MEMBERS     = "members";
    private static final String TAG_MEMBER_TYPE = "memberType";
    private static final String TAG_RESULTS     = "results";
    private static final String TAG_STATUS      = "status";

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
     * The stage of the expedition.
     */
    protected ExpeditionStatus status;

    /**
     * The current active members of the expedition.
     */
    @Nullable
    private List<IExpeditionMember<?>> activeMembersCache;

    /**
     * Deserialization constructor.
     *
     * @param members   the members for this expedition.
     * @param equipment the list of equipment for this expedition.
     * @param results   the results for this expedition.
     * @param status    the status of the expedition.
     */
    protected AbstractExpedition(
      final @NotNull List<IExpeditionMember<?>> members,
      final @NotNull List<ItemStack> equipment,
      final @NotNull List<ExpeditionStage> results,
      final @NotNull ExpeditionStatus status)
    {
        this.members = members.stream().collect(Collectors.toMap(IExpeditionMember::getId, v -> v));
        this.equipment = equipment;
        this.results = new ArrayDeque<>(results);
        this.status = status;
    }

    /**
     * Create an expedition instance from compound data.
     *
     * @param compound the compound data.
     */
    public static <T extends AbstractExpedition> T loadFromNBT(final CompoundTag compound, final ExpeditionCreator<T> creator)
    {
        final List<IExpeditionMember<?>> members = new ArrayList<>();
        final ListTag membersList = compound.getList(TAG_MEMBERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < membersList.size(); ++i)
        {
            final CompoundTag memberCompound = membersList.getCompound(i);
            final String memberType = memberCompound.getString(TAG_MEMBER_TYPE);
            if (Objects.equals(memberType, "citizen"))
            {
                members.add(new ExpeditionCitizenMember(memberCompound));
            }
            else if (Objects.equals(memberType, "visitor"))
            {
                members.add(new ExpeditionVisitorMember(memberCompound));
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

        final ExpeditionStatus status = compound.contains(TAG_STATUS) ? ExpeditionStatus.valueOf(compound.getString(TAG_STATUS)) : ExpeditionStatus.CREATED;

        return creator.create(members, equipment, results, status);
    }

    @Override
    public final ExpeditionStatus getStatus()
    {
        return status;
    }

    @Override
    public final void setStatus(final ExpeditionStatus status)
    {
        this.status = status;
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
            activeMembersCache = this.members.values().stream().filter(f -> !f.isDead()).toList();
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
        final ExpeditionStage current = getCurrentStage();
        // If the last inserted stage has not yielded any results yet, simply remove the stage and append the next one.
        if (current.getRewards().isEmpty() && current.getKills().isEmpty() && current.getMembersLost().isEmpty())
        {
            this.results.pop();
        }
        this.results.add(new ExpeditionStage(header));
    }

    @Override
    public void rewardFound(final ItemStack itemStack)
    {
        getCurrentStage().addReward(itemStack);
    }

    @Override
    public void mobKilled(final EntityType<?> type)
    {
        getCurrentStage().addKill(type);
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
        final ListTag membersCompound = new ListTag();
        for (final IExpeditionMember<?> member : members.values())
        {
            final CompoundTag memberCompound = new CompoundTag();
            if (member instanceof ExpeditionCitizenMember)
            {
                memberCompound.putString(TAG_MEMBER_TYPE, "citizen");
            }
            else if (member instanceof ExpeditionVisitorMember)
            {
                memberCompound.putString(TAG_MEMBER_TYPE, "visitor");
            }
            member.write(memberCompound);
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

        compound.putString(TAG_STATUS, status.name());
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
         * @param status    the status of the expedition.
         * @return the new expedition instance.
         */
        T create(final List<IExpeditionMember<?>> members, final List<ItemStack> equipment, final List<ExpeditionStage> results, final ExpeditionStatus status);
    }
}