package com.minecolonies.coremod.colony;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.inventory.InventoryCitizen;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The CitizenDataView is the client-side representation of a CitizenData. Views
 * contain the CitizenData's data that is relevant to a Client, in a more
 * client-friendly form. Mutable operations on a View result in a message to the
 * server to perform the operation.
 */
public class CitizenDataView
{

    private static final String TAG_HELD_ITEM_SLOT = "HeldItemSlot";

    /**
     * The max amount of lines the latest log allows.
     */
    private static final int MAX_LINES_OF_LATEST_LOG = 4;

    /**
     * Attributes.
     */
    private final int     id;
    private       int     entityId;
    private       String  name;
    private       boolean female;

    /**
     * colony id of the citizen.
     */
    private int colonyId;

    /**
     * Placeholder skills.
     */
    private int    level;
    private double experience;
    private double health;
    private double maxHealth;
    private int    strength;
    private int    endurance;
    private int    charisma;
    private int    intelligence;
    private int    dexterity;
    private double saturation;

    /**
     * Job identifier.
     */
    private String job;

    /**
     * Working and home position.
     */
    @Nullable
    private BlockPos homeBuilding;
    @Nullable
    private BlockPos workBuilding;

    /**
     * The 4 lines of the latest status.
     */
    private final ITextComponent[] latestStatus = new ITextComponent[MAX_LINES_OF_LATEST_LOG];

    private InventoryCitizen inventory;

    /**
     * Set View id.
     *
     * @param id the id to set.
     */
    protected CitizenDataView(final int id)
    {
        this.id = id;
    }

    /**
     * Id getter.
     *
     * @return view Id.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Entity Id getter.
     *
     * @return entity id.
     */
    public int getEntityId()
    {
        return entityId;
    }

    /**
     * Entity name getter.
     *
     * @return entity name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Check entity sex.
     *
     * @return true if entity is female.
     */
    public boolean isFemale()
    {
        return female;
    }

    /**
     * Entity level getter.
     *
     * @return the citizens level.
     */
    public int getLevel()
    {
        return level;
    }

    /**
     * Entity experience getter.
     *
     * @return it's experience.
     */
    public double getExperience()
    {
        return experience;
    }

    /**
     * Entity job getter.
     *
     * @return the job as a string.
     */
    public String getJob()
    {
        return job;
    }

    /**
     * Get the entities home building.
     *
     * @return the home coordinates.
     */
    @Nullable
    public BlockPos getHomeBuilding()
    {
        return homeBuilding;
    }

    /**
     * Get the entities work building.
     *
     * @return the work coordinates.
     */
    @Nullable
    public BlockPos getWorkBuilding()
    {
        return workBuilding;
    }

    /**
     * Get the colony id of the citizen.
     *
     * @return unique id of the colony.
     */
    public int getColonyId()
    {
        return colonyId;
    }

    /**
     * Strength getter.
     *
     * @return citizen Strength value.
     */
    public int getStrength()
    {
        return strength;
    }

    /**
     * Endurance getter.
     *
     * @return citizen Endurance value.
     */
    public int getEndurance()
    {
        return endurance;
    }

    /**
     * Charisma getter.
     *
     * @return citizen Charisma value.
     */
    public int getCharisma()
    {
        return charisma;
    }

    /**
     * Get the saturation of the citizen.
     *
     * @return the saturation a double.
     */
    public double getSaturation()
    {
        return saturation;
    }

    /**
     * Intelligence getter.
     *
     * @return citizen Intelligence value.
     */
    public int getIntelligence()
    {
        return intelligence;
    }

    /**
     * Dexterity getter.
     *
     * @return citizen Dexterity value.
     */
    public int getDexterity()
    {
        return dexterity;
    }

    /**
     * Health getter.
     *
     * @return citizen Dexterity value
     */
    public double getHealth()
    {
        return health;
    }

    /**
     * Max health getter.
     *
     * @return citizen Dexterity value.
     */
    public double getMaxHealth()
    {
        return maxHealth;
    }

    /**
     * Deserialize the attributes and variables from transition.
     *
     * @param buf Byte buffer to deserialize.
     */
    public void deserialize(@NotNull final ByteBuf buf)
    {
        name = ByteBufUtils.readUTF8String(buf);
        female = buf.readBoolean();
        entityId = buf.readInt();

        homeBuilding = buf.readBoolean() ? BlockPosUtil.readFromByteBuf(buf) : null;
        workBuilding = buf.readBoolean() ? BlockPosUtil.readFromByteBuf(buf) : null;

        //  Attributes
        level = buf.readInt();
        experience = buf.readDouble();
        health = buf.readFloat();
        maxHealth = buf.readFloat();

        strength = buf.readInt();
        endurance = buf.readInt();
        charisma = buf.readInt();
        intelligence = buf.readInt();
        dexterity = buf.readInt();
        saturation = buf.readDouble();

        job = ByteBufUtils.readUTF8String(buf);

        final int length = buf.readInt();
        for (int i = 0; i < length; i++)
        {
            final String textComp = ByteBufUtils.readUTF8String(buf);
            final TextComponentTranslation textComponent = new TextComponentTranslation(textComp);
            latestStatus[i] = textComponent;
        }

        colonyId = buf.readInt();

        final NBTTagCompound compound = ByteBufUtils.readTag(buf);
        inventory = new InventoryCitizen(this.name, true);
        final NBTTagList nbttaglist = compound.getTagList("inventory", 10);
        this.inventory.readFromNBT(nbttaglist);
        this.inventory.setHeldItem(compound.getInteger(TAG_HELD_ITEM_SLOT));
    }

    /**
     * Get the array of the latest status.
     *
     * @return the array of ITextComponents.
     */
    public ITextComponent[] getLatestStatus()
    {
        return latestStatus.clone();
    }

    public InventoryCitizen getInventory()
    {
        return inventory;
    }
}
