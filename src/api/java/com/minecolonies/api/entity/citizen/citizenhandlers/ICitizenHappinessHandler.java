package com.minecolonies.api.entity.citizen.citizenhandlers;

import com.minecolonies.api.util.constant.IToolType;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public interface ICitizenHappinessHandler
{
    /**
     * This function applies eating adjust to the base hapiness for
     * the citizen.
     *
     * @param eatFood true or false indicate citizen was unable to eat
     */
    void setFoodModifier(boolean eatFood);

    /**
     * Called once a day to update the citizens daily happiness
     * modifiers.
     *
     * @param hasHouse  boolean if the citizen is assigned to a house
     * @param hasJob boolean to indicate if citizen has a job
     */
    void processDailyHappiness(boolean hasHouse, boolean hasJob);

    /**
     * this function applies adjust to happiness.
     * This would mean the citizen is full.
     */
    void setSaturated();

    /**
     * set the Damage modifier on the citizens happiness
     * depending on how hurt they are.
     */
    void setDamageModifier();

    /**
     * Called to set if the farmer can farm a specific field.
     *
     * @param pos
     *            position of the scarecrow block
     * @param canFarm
     *            boolean to indicate if the field can be farmed
     */
    void setNoFieldForFarmerModifier(BlockPos pos, boolean canFarm);

    /**
     * Indicates the farmer has not fields to farm.
     */
    void setNoFieldsToFarm();

    /**
     * Call this function to add a tool type that is needed by the citizen.
     * If citizen gets its tool, then passing in false will remove it from the needed tools.
     *
     * @param toolType Tooltype to indicate
     * @param needs indicate if the tool type is needed
     */
    void setNeedsATool(@NotNull IToolType toolType, boolean needs);

    /**
     * @param hasHouse
     *            indicate the citizen has an assigned house
     */
    void setHomeModifier(boolean hasHouse);

    /**
     * Called to set if the citizen has a job or not.
     *
     * @param hasJob
     *            boolean to indicate if the citizen has a job
     */
    void setJobModifier(boolean hasJob);

    /**
     * @return current citizens overall happiness
     */
    double getHappiness();

    /**
     * @return current food modifier for happiness
     */
    double getFoodModifier();

    /**
     * @return current damage modifier for happiness
     */
    double getDamageModifier();

    /**
     * @return current house modifier for happiness
     */
    double getHouseModifier();

    /**
     * Store the level to nbt.
     *
     * @param compound  compound to use.
     */
    void writeToNBT(NBTTagCompound compound);

    /**
     * Reads in Happiness data from the NBT file.
     *
     * @param compound pointer to NBT fields
     */
    void readFromNBT(NBTTagCompound compound);

    /**
     * Write to the incoming variable all the related data to modifiers.
     *
     * @param buf  buffer to witch values of the modifiers will be written to.
     */
    void serializeViewNetworkData(@NotNull ByteBuf buf);
}
