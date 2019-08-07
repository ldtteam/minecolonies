package com.minecolonies.coremod.colony;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The CitizenDataView is the client-side representation of a CitizenData. Views
 * contain the CitizenData's data that is relevant to a Client, in a more
 * client-friendly form. Mutable operations on a View result in a message to the
 * server to perform the operation.
 */
public class CitizenDataView implements ICitizenDataView
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
    private       boolean paused;
    private       boolean isChild;

    /**
     * colony id of the citizen.
     */
    private int colonyId;

    /**
     * Placeholder skills.
     */
    private int level;
    private double experience;
    private double health;
    private double maxHealth;
    private int strength;
    private int endurance;
    private int charisma;
    private int intelligence;
    private int dexterity;
    private double saturation;

    /**
     * holds the current citizen happiness value
     */
    private double happiness;

    /**
     * holds the current citizen happiness modifiers for
     * each type of modifier.
     */
    private double foodModifier;
    private double damageModifier;
    private double houseModifier;
    private double jobModifier;
    private double fieldsModifier;
    private double toolsModifiers;

    /**
     * The position of the guard.
     */
    private BlockPos position;

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
     * @param id
     *            the id to set.
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
    @Override
    public int getId()
    {
        return id;
    }

    /**
     * Entity Id getter.
     *
     * @return entity id.
     */
    @Override
    public int getEntityId()
    {
        return entityId;
    }

    /**
     * Entity name getter.
     *
     * @return entity name.
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * Check entity sex.
     *
     * @return true if entity is female.
     */
    @Override
    public boolean isFemale()
    {
        return female;
    }

    /**
     * Check if the entity is paused.
     *
     * @return true if entity is paused.
     */
    @Override
    public boolean isPaused()
    {
        return paused;
    }

    /**
     * Check if the entity is a child
     *
     * @return true if child
     */
    @Override
    public boolean isChild()
    {
        return isChild;
    }

    /**
     * DEPRECATED
     */
    @Override
    public void setPaused(final boolean p)
    {
        this.paused = p;
    }

    /**
     * Entity level getter.
     *
     * @return the citizens level.
     */
    @Override
    public int getLevel()
    {
        return level;
    }

    /**
     * Entity experience getter.
     *
     * @return it's experience.
     */
    @Override
    public double getExperience()
    {
        return experience;
    }

    /**
     * Entity job getter.
     *
     * @return the job as a string.
     */
    @Override
    public String getJob()
    {
        return job;
    }

    /**
     * Get the entities home building.
     *
     * @return the home coordinates.
     */
    @Override
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
    @Override
    @Nullable
    public BlockPos getWorkBuilding()
    {
        return workBuilding;
    }

    /**
     * DEPRECATED
     */
    @Override
    @Nullable
    public void setWorkBuilding(final BlockPos bp)
    {
        this.workBuilding = bp;
    }

    /**
     * Get the colony id of the citizen.
     *
     * @return unique id of the colony.
     */
    @Override
    public int getColonyId()
    {
        return colonyId;
    }

    /**
     * Strength getter.
     *
     * @return citizen Strength value.
     */
    @Override
    public int getStrength()
    {
        return strength;
    }

    /**
     * Endurance getter.
     *
     * @return citizen Endurance value.
     */
    @Override
    public int getEndurance()
    {
        return endurance;
    }

    /**
     * Charisma getter.
     *
     * @return citizen Charisma value.
     */
    @Override
    public int getCharisma()
    {
        return charisma;
    }

    /**
     * Gets the current Happiness value for the citizen
     * 
     * @return citizens current Happiness value
     */
    @Override
    public double getHappiness()
    {
        return happiness;
    }

    /**
     * Get the saturation of the citizen.
     *
     * @return the saturation a double.
     */
    @Override
    public double getSaturation()
    {
        return saturation;
    }

    /**
     * Intelligence getter.
     *
     * @return citizen Intelligence value.
     */
    @Override
    public int getIntelligence()
    {
        return intelligence;
    }

    /**
     * Dexterity getter.
     *
     * @return citizen Dexterity value.
     */
    @Override
    public int getDexterity()
    {
        return dexterity;
    }

    /**
     * Health getter.
     *
     * @return citizen Dexterity value
     */
    @Override
    public double getHealth()
    {
        return health;
    }

    /**
     * Max health getter.
     *
     * @return citizen Dexterity value.
     */
    @Override
    public double getMaxHealth()
    {
        return maxHealth;
    }

    /**
     * Get the last registered position of the citizen.
     * 
     * @return the BlockPos.
     */
    @Override
    public BlockPos getPosition()
    {
        return position;
    }

    /**
     * Deserialize the attributes and variables from transition.
     *
     * @param buf
     *            Byte buffer to deserialize.
     */
    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
    {
        name = buf.readString();
        female = buf.readBoolean();
        entityId = buf.readInt();
        paused = buf.readBoolean();
        isChild = buf.readBoolean();

        homeBuilding = buf.readBoolean() ? BlockPosUtil.readFromByteBuf(buf) : null;
        workBuilding = buf.readBoolean() ? BlockPosUtil.readFromByteBuf(buf) : null;

        // Attributes
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
        happiness = buf.readDouble();

        foodModifier = buf.readDouble();
        damageModifier = buf.readDouble();
        houseModifier = buf.readDouble();
        jobModifier = buf.readDouble();
        fieldsModifier = buf.readDouble();
        toolsModifiers = buf.readDouble();

        job = buf.readString();

        final int length = buf.readInt();
        for (int i = 0; i < length; i++)
        {
            final String textComp = buf.readString();
            final TranslationTextComponent textComponent = new TranslationTextComponent(textComp);
            latestStatus[i] = textComponent;
        }

        colonyId = buf.readInt();

        final CompoundNBT compound = buf.readCompoundTag();
        inventory = new InventoryCitizen(this.name, true);
        final ListNBT ListNBT = compound.getList("inventory", 10);
        this.inventory.read(ListNBT);
        this.inventory.setHeldItem(Hand.MAIN_HAND, compound.getInt(TAG_HELD_ITEM_SLOT));
        this.inventory.setHeldItem(Hand.OFF_HAND, compound.getInt(TAG_OFFHAND_HELD_ITEM_SLOT));

        position = buf.readBlockPos();
    }

    /**
     * Get the array of the latest status.
     *
     * @return the array of ITextComponents.
     */
    @Override
    public ITextComponent[] getLatestStatus()
    {
        return latestStatus.clone();
    }

    @Override
    public InventoryCitizen getInventory()
    {
        return inventory;
    }

    /**
     * @return returns the current modifier related to food.
     */
    @Override
    public double getFoodModifier()
    {
        return foodModifier;
    }

    /**
     * @return returns the current modifier related to damage.
     */
    @Override
    public double getDamageModifier()
    {
        return damageModifier;
    }

    /**
     * @return returns the current modifier related to house.
     */
    @Override
    public double getHouseModifier()
    {
        return houseModifier;
    }

    /**
     * @return returns the current modifier related to job.
     */
    @Override
    public double getJobModifier()
    {
        return jobModifier;
    }

    /**
     * @return returns the current modifier related to fields.
     */
    @Override
    public double getFieldsModifier()
    {
        return fieldsModifier;
    }

    /**
     * @return returns the current modifier related to tools.
     */
    @Override
    public double getToolsModifiers()
    {
        return toolsModifiers;
    }
}
