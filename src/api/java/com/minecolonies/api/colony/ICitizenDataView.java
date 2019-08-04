package com.minecolonies.api.colony;

import com.minecolonies.api.inventory.InventoryCitizen;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICitizenDataView
{
    String TAG_OFFHAND_HELD_ITEM_SLOT = "OffhandHeldItemSlot";

    /**
     * Id getter.
     *
     * @return view Id.
     */
    int getId();

    /**
     * Entity Id getter.
     *
     * @return entity id.
     */
    int getEntityId();

    /**
     * Entity name getter.
     *
     * @return entity name.
     */
    String getName();

    /**
     * Check entity sex.
     *
     * @return true if entity is female.
     */
    boolean isFemale();

    /**
     * Check if the entity is paused.
     *
     * @return true if entity is paused.
     */
    boolean isPaused();

    /**
     * Check if the entity is a child
     *
     * @return true if child
     */
    boolean isChild();

    /**
     * DEPRECATED
     */
    void setPaused(boolean p);

    /**
     * Entity level getter.
     *
     * @return the citizens level.
     */
    int getLevel();

    /**
     * Entity experience getter.
     *
     * @return it's experience.
     */
    double getExperience();

    /**
     * Entity job getter.
     *
     * @return the job as a string.
     */
    String getJob();

    /**
     * Get the entities home building.
     *
     * @return the home coordinates.
     */
    @Nullable
    BlockPos getHomeBuilding();

    /**
     * Get the entities work building.
     *
     * @return the work coordinates.
     */
    @Nullable
    BlockPos getWorkBuilding();

    /**
     * DEPRECATED
     */
    @Nullable
    void setWorkBuilding(BlockPos bp);

    /**
     * Get the colony id of the citizen.
     *
     * @return unique id of the colony.
     */
    int getColonyId();

    /**
     * Strength getter.
     *
     * @return citizen Strength value.
     */
    int getStrength();

    /**
     * Endurance getter.
     *
     * @return citizen Endurance value.
     */
    int getEndurance();

    /**
     * Charisma getter.
     *
     * @return citizen Charisma value.
     */
    int getCharisma();

    /**
     * Gets the current Happiness value for the citizen
     *
     * @return citizens current Happiness value
     */
    double getHappiness();

    /**
     * Get the saturation of the citizen.
     *
     * @return the saturation a double.
     */
    double getSaturation();

    /**
     * Intelligence getter.
     *
     * @return citizen Intelligence value.
     */
    int getIntelligence();

    /**
     * Dexterity getter.
     *
     * @return citizen Dexterity value.
     */
    int getDexterity();

    /**
     * Health getter.
     *
     * @return citizen Dexterity value
     */
    double getHealth();

    /**
     * Max health getter.
     *
     * @return citizen Dexterity value.
     */
    double getMaxHealth();

    /**
     * Get the last registered position of the citizen.
     *
     * @return the BlockPos.
     */
    BlockPos getPosition();

    /**
     * Deserialize the attributes and variables from transition.
     *
     * @param buf
     *            Byte buffer to deserialize.
     */
    void deserialize(@NotNull PacketBuffer buf);

    /**
     * Get the array of the latest status.
     *
     * @return the array of ITextComponents.
     */
    ITextComponent[] getLatestStatus();

    /**
     * Get the inventory of the citizen.
     * @return the inventory of the citizen.
     */
    InventoryCitizen getInventory();

    /**
     * @return returns the current modifier related to food.
     */
    double getFoodModifier();

    /**
     * @return returns the current modifier related to damage.
     */
    double getDamageModifier();

    /**
     * @return returns the current modifier related to house.
     */
    double getHouseModifier();

    /**
     * @return returns the current modifier related to job.
     */
    double getJobModifier();

    /**
     * @return returns the current modifier related to fields.
     */
    double getFieldsModifier();

    /**
     * @return returns the current modifier related to tools.
     */
    double getToolsModifiers();
}
