package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenHappinessHandler;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.FieldDataModifier;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteractionResponseHandler;
import com.minecolonies.coremod.colony.jobs.JobFarmer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.minecolonies.api.util.constant.HappinessConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Handler for the citizens happiness.
 * This keeps all the modifiers related to the citizen.
 *
 * @author kevin
 *
 */
public class CitizenHappinessHandler implements ICitizenHappinessHandler
{
    /**
     * The citizen assigned to this manager.
     */
    private final ICitizenData citizen;

    /**
     * holds the base happiness value.
     */
    private double baseHappiness;

    /**
     * holds the modifier for food.
     */
    private double foodModifier;

    /**
     * holds the modifier for health.
     */
    private double healthModifier;

    /**
     * holds the modifier for damage.
     */
    private double damageModifier;

    /**
     * holds the modifier for house.
     */
    private double houseModifier;

    /**
     * holds the numbers of days without a house.
     */
    private int numberOfDaysWithoutHouse;

    /**
     * holds the modifier for job.
     */
    private double jobModifier;

    /**
     * holds the numbers of days without a job.
     */
    private int numberOfDaysWithoutJob;

    /**
     * holds the modifier for farms that the farmer can't farm.
     */
    private double farmerModifier;

    /**
     * holds an indicator of the farmer has no fields to farm
     */
    private boolean hasNoFields;

    /**
     * holds an array of all the fields the farmer has assigned to them.
     */
    private final Map<BlockPos, FieldDataModifier> fieldModifier = new HashMap<BlockPos, FieldDataModifier>();

    /**
     * holds an indicator citizen needing a tool
     */
    private final Map<IToolType, Integer> needsTool = new HashMap<IToolType, Integer>();

    /**
     * holds the modifier for citizen not having a tool.
     */
    private double noToolModifier;

    /**
     * Constructor for the experience handler.
     *
     * @param citizen
     *            the citizen owning the handler.
     */
    public CitizenHappinessHandler(final ICitizenData citizen)
    {
        this.citizen = citizen;
        baseHappiness = BASE_HAPPINESS;
        foodModifier = 0;
        damageModifier = 0;
        houseModifier = 0;
        numberOfDaysWithoutHouse = 0;
        jobModifier = 0;
        numberOfDaysWithoutJob = 0;
        farmerModifier = 0;
        hasNoFields = false;
        needsTool.clear();
    }

    /**
     * This function applies eating adjust to the base hapiness for
     * the citizen.
     *
     * @param eatFood true or false indicate citizen was unable to eat
     */
    @Override
    public void setFoodModifier(final boolean eatFood)
    {
        if (!eatFood)
        {
            if (citizen.getSaturation() < CitizenConstants.LOW_SATURATION)
            {
                foodModifier = FOOD_MODIFIER_MAX;
            }
            else
            {
                foodModifier = FOOD_MODIFIER_MIN;
            }
        }
        else
        {
            foodModifier = 0;
        }
        citizen.markDirty();
    }

    @Override
    public void setHealthModifier(final boolean isHealthy)
    {
        if (!isHealthy)
        {
            healthModifier = HEALTH_MODIFIER_MAX;
        }
        else
        {
            healthModifier = 0;
        }
        citizen.markDirty();
    }

    /**
     * Called once a day to update the citizens daily happiness
     * modifiers.
     *
     * @param hasHouse  boolean if the citizen is assigned to a house
     * @param hasJob boolean to indicate if citizen has a job
     */
    @Override
    public void processDailyHappiness(final boolean hasHouse, final boolean hasJob)
    {
        if (citizen.getColony().getColonyHappinessManager().getLockedHappinessModifier().isPresent())
            return;

        processDailyHappinessForHomeData(hasHouse);
        processDailyHappinessForJobData(hasJob);
        processDailyHappinessForFarmData();
        processDailyHappinessForToolData();

        citizen.markDirty();
    }

    private void processDailyHappinessForHomeData(final boolean hasHouse)
    {
        if (!hasHouse && !citizen.isChild())
        {
            numberOfDaysWithoutHouse++;
            if (numberOfDaysWithoutHouse > DEMANDS_DAYS_WITHOUT_HOUSE)
            {
                citizen.triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(DEMANDS_HOUSE), ChatPriority.CHITCHAT));
            }
            else if (numberOfDaysWithoutHouse > COMPLAIN_DAYS_WITHOUT_HOUSE)
            {
                citizen.triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(NO_HOUSE), ChatPriority.CHITCHAT));
            }
        }
        else
        {
            numberOfDaysWithoutHouse = 0;
        }
        setHomeModifier(hasHouse);
    }

    private void processDailyHappinessForJobData(final boolean hasJob)
    {
        if (!hasJob && !citizen.isChild())
        {
            numberOfDaysWithoutJob++;
            if (numberOfDaysWithoutJob > DEMANDS_DAYS_WITHOUT_JOB)
            {
                citizen.triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(DEMANDS_JOB), ChatPriority.CHITCHAT));
            }
            else if (numberOfDaysWithoutJob > COMPLAIN_DAYS_WITHOUT_JOB)
            {
                citizen.triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(NO_JOB), ChatPriority.CHITCHAT));
            }
        }
        else
        {
            numberOfDaysWithoutJob = 0;
        }
        setJobModifier(hasJob);
    }

    private void processDailyHappinessForFarmData()
    {
        farmerModifier = 0;
        hasNoFields = true;
        for (final FieldDataModifier field : fieldModifier.values())
        {
            if (field.isCanFarm())
            {
                farmerModifier += FIELD_MODIFIER_POSITIVE;
                hasNoFields = false;
            }
            else
            {
                field.increaseInactiveDays();
                if (field.getInactiveDays() < NO_FIELDS_COMPLAINS_DAYS)
                {
                    farmerModifier += FIELD_MODIFIER_MIN;
                }
                else
                {
                    farmerModifier += (((double) field.getInactiveDays() / FIELD_MAX_DAYS_MODIFIER) * FIELD_MODIFIER_MAX) + FIELD_MODIFIER_MIN;
                }
            }
        }

        if (hasNoFields && citizen.getJob() instanceof JobFarmer)
        {
            farmerModifier = NO_FIELD_MODIFIER;
        }
    }

    private void processDailyHappinessForToolData()
    {
        noToolModifier = 0;
        final int maxToolOpenDays = getMaxOpenToolDays();
        if (maxToolOpenDays > NO_TOOLS_DEMANDS_DAYS)
        {
            citizen.triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(DEMANDS_TOOL), ChatPriority.CHITCHAT));
        }
        else if (maxToolOpenDays > NO_TOOLS_COMPLAINS_DAYS)
        {
            citizen.triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(NO_TOOL), ChatPriority.CHITCHAT));
        }
        noToolModifier += (((double) maxToolOpenDays / NO_TOOLS_MAX_DAYS_MODIFIER) * NO_TOOLS_MODIFIER);
    }

    @Override
    public int getMaxOpenToolDays()
    {
        int maxOpenDays = 0;
        for (final Map.Entry<IToolType, Integer> entry : needsTool.entrySet())
        {
            final int numDays = entry.getValue() + 1;
            if (numDays > maxOpenDays)
            {
                maxOpenDays = numDays;
            }
            final IToolType toolType = entry.getKey();
            needsTool.put(toolType, numDays);
        }
        return maxOpenDays;
    }

    /**
     * this function applies adjust to happiness.
     * This would mean the citizen is full.
     */
    @Override
    public void setSaturated()
    {
        foodModifier = FOOD_MODIFIER_POSITIVE;
    }

    /**
     * set the Damage modifier on the citizens happiness
     * depending on how hurt they are.
     */
    @Override
    public void updateDamageModifier()
    {
        final Optional<AbstractEntityCitizen> entityCitizen = citizen.getCitizenEntity();
        if (entityCitizen.isPresent())
        {
            final double health = entityCitizen.get().getHealth() / entityCitizen.get().getMaxHealth();
            final double prevDamageModifier = damageModifier;
            if (health < DAMAGE_LOWEST_POINT)
            {
                damageModifier = DAMAGE_MODIFIER_MAX;
            }
            else if (health < DAMAGE_MEDIUM_POINT)
            {
                damageModifier = DAMAGE_MODIFIER_MID;
            }
            else if (health < DAMAGE_HIGHEST_POINT)
            {
                damageModifier = DAMAGE_MODIFIER_MIN;
            }
            else
            {
                damageModifier = 0;
            }

            if (prevDamageModifier != damageModifier)
            {
                citizen.markDirty();
            }
        }
    }

    /**
     * Called to set if the farmer can farm a specific field.
     *
     * @param pos
     *            position of the scarecrow block
     * @param canFarm
     *            boolean to indicate if the field can be farmed
     */
    @Override
    public void setNoFieldForFarmerModifier(final BlockPos pos, final boolean canFarm)
    {
        FieldDataModifier field = fieldModifier.get(pos);
        if (field == null)
        {
            field = new FieldDataModifier();
            fieldModifier.put(pos, field);
        }

        field.isCanFarm(canFarm);
        citizen.markDirty();
    }

    /**
     * Indicates the farmer has not fields to farm.
     */
    @Override
    public void setNoFieldsToFarm()
    {
        hasNoFields = true;
    }

    /**
     * Call this function to add a tool type that is needed by the citizen.
     * If citizen gets its tool, then passing in false will remove it from the needed tools.
     *
     * @param toolType Tooltype to indicate
     * @param needs indicate if the tool type is needed
     */
    @Override
    public void setNeedsATool(@NotNull final IToolType toolType, final boolean needs)
    {
        if (needs)
        {
            if (!needsTool.containsKey(toolType))
            {
                needsTool.put(toolType, 0);
            }
        }
        else
        {
            needsTool.remove(toolType);
        }
        citizen.markDirty();
    }

    /**
     * @param hasHouse
     * indicate the citizen has an assigned house
     */
    @Override
    public void setHomeModifier(final boolean hasHouse)
    {
        if (hasHouse)
        {
            houseModifier = HOUSE_MODIFIER_POSITIVE;
        }
        else
        {
            houseModifier = (MAX_HOUSE_PENALTY * ((double) numberOfDaysWithoutHouse / MAX_DAYS_WITHOUT_HOUSE)) * -1;
        }
        citizen.markDirty();
    }

    /**
     * Called to set if the citizen has a job or not.
     *
     * @param hasJob
     *            boolean to indicate if the citizen has a job
     */
    @Override
    public void setJobModifier(final boolean hasJob)
    {
        if (hasJob)
        {
            jobModifier = JOB_MODIFIER_POSITIVE;
        }
        else
        {
            jobModifier = (MAX_JOB_PENALTY * ((double) numberOfDaysWithoutHouse / MAX_DAYS_WITHOUT_JOB)) * -1;
        }
        citizen.markDirty();
    }

    /**
     * @return current citizens overall happiness
     */
    @Override
    public double getHappiness()
    {
        if (citizen.getColony().getColonyHappinessManager().getLockedHappinessModifier().isPresent())
        {
            return citizen.getColony().getColonyHappinessManager().getLockedHappinessModifier().get();
        }

        double value = baseHappiness + foodModifier + damageModifier + houseModifier + jobModifier + farmerModifier + noToolModifier + healthModifier;
        if (value > MAX_HAPPINESS)
        {
            value = MAX_HAPPINESS;
        }
        else if (value < MIN_HAPPINESS)
        {
            value = MIN_HAPPINESS;
        }

        return value;
    }

    /**
     * @return current food modifier for happiness
     */
    @Override
    public double getFoodModifier()
    {
        return foodModifier;
    }

    /**
     * @return current damage modifier for happiness
     */
    @Override
    public double getDamageModifier()
    {
        return damageModifier;
    }

    /**
     * @return current house modifier for happiness
     */
    @Override
    public double getHouseModifier()
    {
        return houseModifier;
    }

    /**
     * Store the level to nbt.
     *
     * @param compound  compound to use.
     */
    @Override
    public void write(final CompoundNBT compound)
    {
        @NotNull final CompoundNBT taskCompound = new CompoundNBT();
        taskCompound.putDouble(NbtTagConstants.TAG_BASE, baseHappiness);
        taskCompound.putDouble(NbtTagConstants.TAG_FOOD, foodModifier);
        taskCompound.putDouble(NbtTagConstants.TAG_DAMAGE, damageModifier);
        taskCompound.putDouble(NbtTagConstants.TAG_HEALTH, healthModifier);
        taskCompound.putDouble(NbtTagConstants.TAG_HOUSE, houseModifier);
        taskCompound.putInt(NbtTagConstants.TAG_NUMBER_OF_DAYS_HOUSE, numberOfDaysWithoutHouse);

        taskCompound.putDouble(NbtTagConstants.TAG_JOB, jobModifier);
        taskCompound.putInt(NbtTagConstants.TAG_NUMBER_OF_DAYS_JOB, numberOfDaysWithoutJob);
        taskCompound.putBoolean(NbtTagConstants.TAG_HAS_NO_FIELDS, hasNoFields);

        @NotNull final ListNBT fieldsTagList = new ListNBT();
        for (final Map.Entry<BlockPos, FieldDataModifier> entry : fieldModifier.entrySet())
        {
            final BlockPos pos = entry.getKey();
            final FieldDataModifier field = entry.getValue();
            @NotNull final CompoundNBT fieldCompound = new CompoundNBT();
            fieldCompound.putInt(NbtTagConstants.TAG_FIELD_DAYS_INACTIVE, field.getInactiveDays());
            fieldCompound.putBoolean(NbtTagConstants.TAG_FIELD_CAN_FARM, field.isCanFarm());

            @NotNull final ListNBT containerTagList = new ListNBT();
            containerTagList.add(NBTUtil.writeBlockPos(pos));
            fieldCompound.put(NbtTagConstants.TAG_ID, containerTagList);

            fieldsTagList.add(fieldCompound);
        }
        taskCompound.put(NbtTagConstants.TAG_FIELDS, fieldsTagList);

        @NotNull final ListNBT noToolsTagList = new ListNBT();
        for (final Map.Entry<IToolType, Integer> entry : needsTool.entrySet())
        {
            final IToolType toolType = entry.getKey();
            final int numDays = entry.getValue();
            @NotNull final CompoundNBT noToolsCompound = new CompoundNBT();
            noToolsCompound.putInt(NbtTagConstants.TAG_NO_TOOLS_NUMBER_DAYS, numDays);
            noToolsCompound.putString(NbtTagConstants.TAG_NO_TOOLS_TOOL_TYPE, toolType.getName());

            noToolsTagList.add(noToolsCompound);
        }
        taskCompound.put(NbtTagConstants.TAG_FIELDS, fieldsTagList);

        compound.put(NbtTagConstants.TAG_HAPPINESS_NAME, taskCompound);
    }

    /**
     * Reads in Happiness data from the NBT file.
     *
     * @param compound pointer to NBT fields
     */
    @Override
    public void read(final CompoundNBT compound)
    {
        final CompoundNBT tagCompound = compound.getCompound(NbtTagConstants.TAG_HAPPINESS_NAME);
        baseHappiness = tagCompound.getDouble(NbtTagConstants.TAG_BASE);
        foodModifier = tagCompound.getDouble(NbtTagConstants.TAG_FOOD);
        healthModifier = tagCompound.getDouble(NbtTagConstants.TAG_HEALTH);
        damageModifier = tagCompound.getDouble(NbtTagConstants.TAG_DAMAGE);
        houseModifier = tagCompound.getDouble(NbtTagConstants.TAG_HOUSE);
        numberOfDaysWithoutHouse = tagCompound.getInt(NbtTagConstants.TAG_NUMBER_OF_DAYS_HOUSE);

        jobModifier = tagCompound.getDouble(NbtTagConstants.TAG_JOB);
        numberOfDaysWithoutJob = tagCompound.getInt(NbtTagConstants.TAG_NUMBER_OF_DAYS_JOB);
        hasNoFields = tagCompound.getBoolean(NbtTagConstants.TAG_HAS_NO_FIELDS);

        final ListNBT fieldTagList = tagCompound.getList(NbtTagConstants.TAG_FIELDS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < fieldTagList.size(); ++i)
        {
            final FieldDataModifier field = new FieldDataModifier();
            final CompoundNBT containerCompound = fieldTagList.getCompound(i);
            field.setInactiveDays(containerCompound.getInt(NbtTagConstants.TAG_FIELD_DAYS_INACTIVE));
            field.isCanFarm(containerCompound.getBoolean(NbtTagConstants.TAG_FIELD_CAN_FARM));

            final ListNBT blockPosTagList = containerCompound.getList(NbtTagConstants.TAG_ID, Constants.NBT.TAG_COMPOUND);
            final CompoundNBT blockPoCompound = blockPosTagList.getCompound(0);
            final BlockPos pos = NBTUtil.readBlockPos(blockPoCompound);
            fieldModifier.put(pos, field);
        }

        final ListNBT noToolsTagList = tagCompound.getList(NbtTagConstants.TAG_NO_TOOLS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < noToolsTagList.size(); ++i)
        {
            final CompoundNBT containerCompound = noToolsTagList.getCompound(i);
            final int numDays = containerCompound.getInt(NbtTagConstants.TAG_NO_TOOLS_NUMBER_DAYS);
            final IToolType toolType = ToolType.getToolType(containerCompound.getString(NbtTagConstants.TAG_NO_TOOLS_TOOL_TYPE));
            needsTool.put(toolType, numDays);
        }

        if (baseHappiness == 0)
        {
            baseHappiness = BASE_HAPPINESS;
        }
    }

    /**
     * Write to the incoming variable all the related data to modifiers.
     * 
     * @param buf  buffer to witch values of the modifiers will be written to.
     */
    @Override
    public void serializeViewNetworkData(@NotNull final PacketBuffer buf)
    {
        buf.writeDouble(getFoodModifier());
        buf.writeDouble(getDamageModifier());
        buf.writeDouble(getHouseModifier());
        buf.writeDouble(jobModifier);
        buf.writeDouble(farmerModifier);
        buf.writeDouble(noToolModifier);
        buf.writeDouble(healthModifier);
    }

    @Override
    public int getNumberOfDaysWithoutHouse()
    {
        return numberOfDaysWithoutHouse;
    }

    @Override
    public int getNumberOfDaysWithoutJob()
    {
        return numberOfDaysWithoutJob;
    }
}
