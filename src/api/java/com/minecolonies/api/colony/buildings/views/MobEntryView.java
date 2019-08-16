package com.minecolonies.api.colony.buildings.views;

import com.minecolonies.api.IMinecoloniesAPI;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

/**
 * Used by the guards for information about Mobs.
 */
public class MobEntryView
{
    /**
     * The priority for attacking this mob.
     */
    private int priority;

    /**
     * The ResourceLocation of the mob.
     */
    private ResourceLocation location;

    /**
     * Whether to attack this mob.
     */
    private boolean attack;

    /**
     * Public constructor of the WorkOrderView.
     */
    public MobEntryView(final ResourceLocation location, final Boolean attack, final Integer priority)
    {
        this.location = location;
        this.attack = attack;
        this.priority = priority;
    }

    /**
     * Writes the Location, Attack, and Priority to a {@link PacketBuffer}.
     *
     * @param buf Buf to write to.
     * @param entry Entry to write.
     */
    public static void writeToByteBuf(@NotNull final PacketBuffer buf, @NotNull final MobEntryView entry)
    {
        buf.writeString(entry.getLocation().toString());
        buf.writeBoolean(entry.hasAttack());
        buf.writeInt(entry.getPriority());
    }

    /**
     * Reads the Location, Attack, and Priority from a {@link PacketBuffer} to create a MobEntryView
     *
     * @param buf Buf to read from.
     * @return MobEntryView that was created.
     */
    @NotNull
    public static MobEntryView readFromByteBuf(@NotNull final PacketBuffer buf)
    {
        final ResourceLocation location = new ResourceLocation(buf.readString());
        final Boolean attack = buf.readBoolean();
        final Integer priority = buf.readInt();

        return new MobEntryView(location, attack, priority);
    }

    /**
     * Writes the Location, Attack, and Priority to NBT
     *
     * @param compound Compound to write to.
     * @param name     Name of the tag.
     * @param entry      the View to write
     */
    public static void write(@NotNull final CompoundNBT compound, final String name, @NotNull final MobEntryView entry)
    {
        @NotNull final CompoundNBT coordsCompound = new CompoundNBT();
        coordsCompound.putString("location", entry.getLocation().toString());
        coordsCompound.putBoolean("attack", entry.hasAttack());
        coordsCompound.putInt("priority", entry.getPriority());
        compound.put(name, coordsCompound);
    }

    /**
     * Reads the Location, Attack, and Priority from nbt to create a MobEntryView
     *
     * @param compound Compound to read data from.
     * @param name     Tag name to read data from.
     * @return The new MobEntryView
     */
    @NotNull
    public static MobEntryView read(@NotNull final CompoundNBT compound, final String name)
    {
        final CompoundNBT entryCompound = compound.getCompound(name);
        final ResourceLocation location = new ResourceLocation(entryCompound.getString("location"));
        final Boolean attack = entryCompound.getBoolean("attack");
        final Integer priority = entryCompound.getInt("priority");
        return new MobEntryView(location, attack, priority);
    }

    /**
     * Priority getter.
     *
     * @return the priority.
     */
    public int getPriority()
    {
        return priority;
    }

    /**
     * Setter for the priority.
     *
     * @param priority the new priority attribute.
     */
    public void setPriority(final int priority)
    {
        this.priority = priority;
    }

    /**
     * location getter.
     *
     * @return the priority.
     */
    public ResourceLocation getLocation()
    {
        return location;
    }

    /**
     * Setter for the location.
     *
     * @param location the new location attribute.
     */
    public void setLocation(final ResourceLocation location)
    {
        this.location = location;
    }

    /**
     * attack getter.
     *
     * @return the attack.
     */
    public boolean hasAttack()
    {
        return attack;
    }

    /**
     * Setter for the attack.
     *
     * @param attack the new attack attribute.
     */
    public void setAttack(final boolean attack)
    {
        this.attack = attack;
    }

    /**
     * Getter for the Mob Entry's name
     *
     * @return the Translated name.
     */
    public String getName()
    {
        if (IMinecoloniesAPI.getInstance().getConfig().getCommon().enableInDevelopmentFeatures.get())
        {
            return String.format("%s:%d", ForgeRegistries.ENTITIES.getValue(this.location).getTranslationKey(), this.priority);
        }
        return ForgeRegistries.ENTITIES.getValue(this.location).getName().getFormattedText();
    }

    public EntityType<?> getEntityEntry()
    {
        return ForgeRegistries.ENTITIES.getValue(this.location);
    }
}
