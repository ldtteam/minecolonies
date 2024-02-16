package com.minecolonies.core.colony.expeditions;

import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_DIMENSION;
import static com.minecolonies.core.colony.expeditions.Expedition.*;

/**
 * Builder instance for {@link Expedition} instances.
 */
public class ExpeditionBuilder
{
    /**
     * The equipment given to the expedition prior to starting.
     */
    @NotNull
    private final Map<ItemStack, Integer> equipment;

    /**
     * The members of the expedition.
     */
    @NotNull
    private final Map<Integer, IExpeditionMember> members;

    /**
     * The dimension to send the expedition to.
     */
    @NotNull
    private ResourceKey<Level> dimensionId = Level.OVERWORLD;

    /**
     * Default constructor.
     */
    public ExpeditionBuilder()
    {
        this.equipment = new HashMap<>();
        this.members = new HashMap<>();
    }

    /**
     * Create an expedition builder instance from compound data.
     *
     * @param compound the compound data.
     * @return the expedition builder instance.
     */
    @NotNull
    public static ExpeditionBuilder loadFromNBT(final CompoundTag compound)
    {
        return Serializer.read(compound);
    }

    /**
     * Set the target dimension for this expedition.
     *
     * @param dimensionId the dimension key.
     */
    public void inDimension(final ResourceKey<Level> dimensionId)
    {
        this.dimensionId = dimensionId;
    }

    /**
     * Adds equipment for this expedition to use.
     *
     * @param stacks the items.
     */
    public void addEquipment(final List<ItemStack> stacks)
    {
        for (final ItemStack stack : stacks)
        {
            final ItemStack key = stack.copyWithCount(1);
            this.equipment.putIfAbsent(key, 0);
            this.equipment.put(key, this.equipment.get(key) + stack.getCount());
        }
    }

    /**
     * Get all members for this expedition.
     *
     * @return the map of members.
     */
    public Map<Integer, IExpeditionMember> getMembers()
    {
        return Collections.unmodifiableMap(members);
    }

    /**
     * Adds a member to the list of members.
     *
     * @param members the new members.
     */
    public void addMembers(final List<IExpeditionMember> members)
    {
        for (final IExpeditionMember member : members)
        {
            this.members.put(member.getId(), member);
        }
    }

    /**
     * Removes a member from the list of members.
     *
     * @param member the member to remove.
     */
    public void removeMember(final IExpeditionMember member)
    {
        this.members.remove(member.getId());
    }

    /**
     * Construct the final expedition instance.
     *
     * @return the expedition instance.
     */
    public Expedition build()
    {
        final List<ItemStack> fullEquipment = equipment.entrySet().stream()
                                                .map(entry -> entry.getKey().copyWithCount(entry.getValue()))
                                                .toList();
        return new Expedition(dimensionId, fullEquipment, members.values().stream().toList());
    }

    /**
     * Write this expedition builder to compound data.
     *
     * @param compound the compound tag.
     */
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
        public static ExpeditionBuilder read(final CompoundTag compound)
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

            final Map<ItemStack, Integer> equipment = new HashMap<>();
            final ListTag equipmentList = compound.getList(TAG_EQUIPMENT, Tag.TAG_COMPOUND);
            for (int i = 0; i < equipmentList.size(); ++i)
            {
                final CompoundTag itemCompound = equipmentList.getCompound(i);
                final ItemStack item = ItemStack.of(itemCompound.getCompound(TAG_EQUIPMENT_ITEM)).copyWithCount(1);
                final int count = itemCompound.getInt(TAG_EQUIPMENT_COUNT);
                equipment.put(item, count);
            }

            final ExpeditionBuilder builder = new ExpeditionBuilder();
            builder.dimensionId = dimensionId;
            builder.equipment.putAll(equipment);
            builder.addMembers(members);
            return builder;
        }

        /**
         * Write an expedition to NBT data.
         *
         * @param expeditionBuilder the expedition builder instance.
         * @param compound          the NBT to write the expedition to.
         */
        public static void write(final ExpeditionBuilder expeditionBuilder, final CompoundTag compound)
        {
            compound.putString(TAG_DIMENSION, expeditionBuilder.dimensionId.location().toString());

            final ListTag memberTag = new ListTag();
            for (final IExpeditionMember member : expeditionBuilder.members.values())
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
            for (final Entry<ItemStack, Integer> equipmentEntry : expeditionBuilder.equipment.entrySet())
            {
                final CompoundTag itemCompound = new CompoundTag();
                itemCompound.put(TAG_EQUIPMENT_ITEM, equipmentEntry.getKey().serializeNBT());
                itemCompound.putInt(TAG_EQUIPMENT_COUNT, equipmentEntry.getValue());
                equipmentTag.add(itemCompound);
            }
            compound.put(TAG_EQUIPMENT, equipmentTag);
        }
    }
}
