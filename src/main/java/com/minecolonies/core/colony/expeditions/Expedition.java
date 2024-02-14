package com.minecolonies.core.colony.expeditions;

import com.minecolonies.api.colony.expeditions.ExpeditionStatus;
import com.minecolonies.api.colony.expeditions.IExpedition;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.api.colony.expeditions.IExpeditionStage;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_DIMENSION;

/**
 * Class for an expedition instance.
 */
public final class Expedition implements IExpedition
{
    /**
     * Nbt tag constants.
     */
    static final String TAG_EQUIPMENT       = "equipment";
    static final String TAG_EQUIPMENT_ITEM  = "equipmentItem";
    static final String TAG_EQUIPMENT_COUNT = "equipmentCount";
    static final String TAG_MEMBERS         = "members";
    static final String TAG_MEMBER_TYPE     = "memberType";

    /**
     * The dimension to send the expedition to.
     */
    @NotNull
    private final ResourceKey<Level> dimensionId;

    /**
     * The equipment given to the expedition prior to starting.
     */
    @NotNull
    private final List<ItemStack> equipment;

    /**
     * The members of the expedition.
     */
    @NotNull
    private final List<IExpeditionMember> members;

    /**
     * The results of this expedition.
     */
    private final Deque<IExpeditionStage> results = new ArrayDeque<>(List.of(new ExpeditionStage()));

    /**
     * The stage of the expedition.
     */
    private ExpeditionStatus status = ExpeditionStatus.CREATED;

    /**
     * Internal constructor.
     */
    public Expedition(final @NotNull ResourceKey<Level> dimensionId, final @NotNull List<ItemStack> equipment, final @NotNull List<IExpeditionMember> members)
    {
        this.dimensionId = dimensionId;
        this.equipment = equipment;
        this.members = members;
    }

    /**
     * Create an expedition instance from compound data.
     *
     * @param compound the compound data.
     * @return the expedition instance.
     */
    @NotNull
    public static Expedition loadFromNBT(final CompoundTag compound)
    {
        return Serializer.read(compound);
    }

    @Override
    public @NotNull ResourceKey<Level> getTargetDimension()
    {
        return this.dimensionId;
    }

    @Override
    public ExpeditionStatus getStatus()
    {
        return this.status;
    }

    @Override
    public void setStatus(final ExpeditionStatus stage)
    {
        this.status = stage;
    }

    @Override
    @NotNull
    public List<IExpeditionMember> getMembers()
    {
        return Collections.unmodifiableList(this.members);
    }

    @Override
    public List<ItemStack> getEquipment()
    {
        return equipment;
    }

    @Override
    public @NotNull List<IExpeditionMember> getActiveMembers()
    {
        return this.members.stream().filter(f -> !f.isDead()).toList();
    }

    @Override
    public @NotNull Collection<IExpeditionStage> getResults()
    {
        return Collections.unmodifiableCollection(this.results);
    }

    @Override
    public void advanceStage()
    {
        this.results.add(new ExpeditionStage());
    }

    @Override
    public void rewardFound(final ItemStack itemStack)
    {
        this.results.getLast().addReward(itemStack);
    }

    @Override
    public void mobKilled(final EntityType<?> type)
    {
        this.results.getLast().rewardFound(type);
    }

    @Override
    public void memberLost(final IExpeditionMember member)
    {
        this.results.getLast().memberLost(member);
        member.died();
    }

    /**
     * Write this expedition builder to compound data.
     *
     * @param compound the compound tag.
     */
    @Override
    public void write(final CompoundTag compound)
    {
        Serializer.write(this, compound);
    }

    /**
     * Serializer for the expeditions.
     */
    private static class Serializer
    {
        /**
         * Read a new expedition from NBT.
         *
         * @param compound the NBT data.
         * @return the expedition instance.
         */
        @NotNull
        public static Expedition read(final CompoundTag compound)
        {
            final ResourceKey<Level> dimensionId = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(compound.getString(TAG_DIMENSION)));

            final List<IExpeditionMember> members = new ArrayList<>();
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

            return new Expedition(dimensionId, equipment, members);
        }

        /**
         * Write an expedition to NBT data.
         *
         * @param expedition the expedition instance.
         * @param compound   the NBT to write the expedition to.
         */
        public static void write(final Expedition expedition, final CompoundTag compound)
        {
            compound.putString(TAG_DIMENSION, expedition.dimensionId.location().toString());

            final ListTag memberTag = new ListTag();
            for (final IExpeditionMember member : expedition.members)
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
                memberTag.add(memberCompound);
            }
            compound.put(TAG_MEMBERS, memberTag);

            final ListTag equipmentTag = new ListTag();
            for (final ItemStack itemStack : expedition.equipment)
            {
                equipmentTag.add(itemStack.serializeNBT());
            }
            compound.put(TAG_EQUIPMENT, equipmentTag);
        }
    }
}