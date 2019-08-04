package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.gui.WindowHutFarmer;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.JobFarmer;
import com.minecolonies.coremod.network.messages.AssignFieldMessage;
import com.minecolonies.coremod.network.messages.AssignmentModeMessage;
import com.minecolonies.coremod.tileentities.TileEntityScarecrow;
import io.netty.buffer.ByteBuf;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.ColonyConstants.NUM_ACHIEVEMENT_FIRST;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_SCARECROW_USER;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_SCARECROW_USER_NOONE;

/**
 * Class which handles the farmer building.
 */
public class BuildingFarmer extends AbstractBuildingWorker
{
    /**
     * Descriptive string of the profession.
     */
    private static final String FARMER = "Farmer";

    /**
     * The maximum building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * NBTTag to store the fields.
     */
    private static final String TAG_FIELDS = "fields";

    /**
     * NBTTag to store the field BlockPos.
     */
    private static final String TAG_FIELDS_BLOCKPOS = "fieldsPos";

    /**
     * NBT tag to store assign manually.
     */
    private static final String TAG_ASSIGN_MANUALLY = "assign";

    /**
     * Flag used to be notified about block updates.
     */
    private static final int                       BLOCK_UPDATE_FLAG = 3;

    /**
     * The last field tag.
     */
    private static final String LAST_FIELD_TAG = "lastField";

    /**
     * Sets the amount of saplings the lumberjack should keep.
     */
    private static final int SEEDS_TO_KEEP = 64;

    /**
     * The list of the fields the farmer manages.
     */
    private final List<BlockPos> farmerFields = new ArrayList<>();

    /**
     * The field the farmer is currently working on.
     */
    @Nullable
    private BlockPos currentField;

    /**
     * The field the farmer worked on the last morning (first).
     */
    @Nullable
    private BlockPos lastField;

    /**
     * Fields should be assigned manually to the farmer.
     */
    private boolean shouldAssignManually = false;

    /**
     * Public constructor which instantiates the building.
     *
     * @param c the colony the building is in.
     * @param l the position it has been placed (it's id).
     */
    public BuildingFarmer(final Colony c, final BlockPos l)
    {
        super(c, l);
        final ItemStack stackSeed = new ItemStack(Items.WHEAT_SEEDS);
        final ItemStack stackCarrot = new ItemStack(Items.CARROT);
        final ItemStack stackPotatoe = new ItemStack(Items.POTATO);
        final ItemStack stackReed = new ItemStack(Items.BEETROOT_SEEDS);

        keepX.put(stackSeed::isItemEqual, new Tuple<>(SEEDS_TO_KEEP, true));
        keepX.put(stackCarrot::isItemEqual, new Tuple<>(SEEDS_TO_KEEP, true));
        keepX.put(stackPotatoe::isItemEqual, new Tuple<>(SEEDS_TO_KEEP, true));
        keepX.put(stackReed::isItemEqual, new Tuple<>(SEEDS_TO_KEEP, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.HOE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
    }

    /**
     * Returns list of fields of the farmer.
     *
     * @return a list of field objects.
     */
    @NotNull
    public List<BlockPos> getFarmerFields()
    {
        return Collections.unmodifiableList(farmerFields);
    }

    /**
     * Checks if the farmer has any fields.
     *
     * @return true if he has none.
     */
    public boolean hasNoFields()
    {
        return farmerFields.isEmpty();
    }

    /**
     * Assigns a field list to the field list.
     *
     * @param field the field to add.
     */
    public void addFarmerFields(final BlockPos field)
    {
        final TileEntity scareCrow = getColony().getWorld().getTileEntity(field);
        if (scareCrow instanceof TileEntityScarecrow)
        {
            ((TileEntityScarecrow) scareCrow).calculateSize(getColony().getWorld(), field.down());
            farmerFields.add(field);
            this.markDirty();
        }
    }

    /**
     * Getter of the current field.
     *
     * @return a field object.
     */
    @Nullable
    public BlockPos getCurrentField()
    {
        return currentField;
    }

    /**
     * Sets the field the farmer is currently working on.
     *
     * @param currentField the field to work on.
     */
    public void setCurrentField(@Nullable final BlockPos currentField)
    {
        this.currentField = currentField;
    }

    /**
     * Retrieves a random field to work on for the farmer.
     *
     * @return a field to work on.
     */
    @Nullable
    public BlockPos getFieldToWorkOn(final World world)
    {
        Collections.shuffle(farmerFields);

        if (!farmerFields.isEmpty())
        {
            if (farmerFields.get(0).equals(lastField))
            {
                Collections.shuffle(farmerFields);
            }
            lastField = farmerFields.get(0);
        }
        for (@NotNull final BlockPos field : farmerFields)
        {
            final TileEntity scareCrow = getColony().getWorld().getTileEntity(field);
            if (scareCrow instanceof TileEntityScarecrow && ((TileEntityScarecrow) scareCrow).needsWork())
            {
                currentField = field;
                return field;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public IJob createJob(@NotNull final ICitizenData citizen)
    {
        if (!farmerFields.isEmpty())
        {
            for (@NotNull final BlockPos field : farmerFields)
            {
                final TileEntity scareCrow = getColony().getWorld().getTileEntity(field);
                if (scareCrow instanceof TileEntityScarecrow)
                {
                    ((TileEntityScarecrow) scareCrow).setOwner(citizen.getId());
                }
            }
        }
        return new JobFarmer(citizen);
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        final ListNBT fieldTagList = compound.getTagList(TAG_FIELDS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < fieldTagList.tagCount(); ++i)
        {
            final CompoundNBT fieldCompound = fieldTagList.getCompoundTagAt(i);
            final BlockPos fieldLocation = BlockPosUtil.readFromNBT(fieldCompound, TAG_FIELDS_BLOCKPOS);
            farmerFields.add(fieldLocation);
        }
        shouldAssignManually = compound.getBoolean(TAG_ASSIGN_MANUALLY);

        if (compound.keySet().contains(LAST_FIELD_TAG))
        {
            final BlockPos pos = BlockPosUtil.readFromNBT(compound, LAST_FIELD_TAG);
            lastField = pos;
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        @NotNull final ListNBT fieldTagList = new ListNBT();
        for (@NotNull final BlockPos f : farmerFields)
        {
            @NotNull final CompoundNBT fieldCompound = new CompoundNBT();
            BlockPosUtil.writeToNBT(fieldCompound, TAG_FIELDS_BLOCKPOS, f);
            fieldTagList.add(fieldCompound);
        }
        compound.put(TAG_FIELDS, fieldTagList);
        compound.putBoolean(TAG_ASSIGN_MANUALLY, shouldAssignManually);

        if (lastField != null)
        {
            BlockPosUtil.writeToNBT(compound, LAST_FIELD_TAG, lastField);
        }

        return compound;
    }

    @Override
    public void onDestroyed()
    {
        super.onDestroyed();
        for (@NotNull final BlockPos field : farmerFields)
        {
            final TileEntity scareCrow = getColony().getWorld().getTileEntity(field);
            if (scareCrow instanceof TileEntityScarecrow)
            {
                ((TileEntityScarecrow) scareCrow).setTaken(false);
                ((TileEntityScarecrow) scareCrow).setOwner(0);

                getColony().getWorld()
                        .notifyBlockUpdate(scareCrow.getPos(),
                                getColony().getWorld().getBlockState(scareCrow.getPos()),
                                getColony().getWorld().getBlockState(scareCrow.getPos()),
                                BLOCK_UPDATE_FLAG);
                ((TileEntityScarecrow) scareCrow).setName(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_SCARECROW_USER,
                        LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_SCARECROW_USER_NOONE)));
            }
        }
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return FARMER;
    }

    /**
     * Override this method if you want to keep an amount of items in inventory.
     * When the inventory is full, everything get's dumped into the building chest.
     * But you can use this method to hold some stacks back.
     *
     * @return a list of objects which should be kept.
     */
    @Override
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> toKeep = new HashMap<>(super.getRequiredItemsAndAmount());
        for (final BlockPos field : farmerFields)
        {
            final TileEntity scareCrow = getColony().getWorld().getTileEntity(field);
            if (scareCrow instanceof TileEntityScarecrow && !ItemStackUtils.isEmpty(((TileEntityScarecrow) scareCrow).getSeed()))
            {
                final ItemStack seedStack = ((TileEntityScarecrow) scareCrow).getSeed();
                toKeep.put(seedStack::isItemEqual, new Tuple<>(SEEDS_TO_KEEP, true));
            }
        }
        return toKeep;
    }

    @Override
    public void onWakeUp()
    {
        resetFields();
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return FARMER;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);

        if (newLevel == NUM_ACHIEVEMENT_FIRST)
        {
            getColony().getStatsManager().triggerAchievement(ModAchievements.achievementBuildingFarmer);
        }
        if (newLevel >= getMaxBuildingLevel())
        {
            getColony().getStatsManager().triggerAchievement(ModAchievements.achievementUpgradeFarmerMax);
        }
    }

    /**
     * Method to serialize data to send it to the view.
     *
     * @param buf the used ByteBuffer.
     */
    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeBoolean(shouldAssignManually);

        int size = 0;

        final List<BlockPos> fields = new ArrayList<>(getColony().getBuildingManager().getFields());

        for (@NotNull final BlockPos field : fields)
        {
            final TileEntity scareCrow = getColony().getWorld().getTileEntity(field);
            if (scareCrow instanceof TileEntityScarecrow)
            {
                if (((TileEntityScarecrow) scareCrow).isTaken())
                {
                    if (getAssignedCitizen().isEmpty() || ((TileEntityScarecrow) scareCrow).getOwnerId() == getMainCitizen().getId())
                    {
                        size++;
                    }
                }
                else
                {
                    size++;
                }
            }
        }

        buf.writeInt(size);
        for (@NotNull final BlockPos field : getColony().getBuildingManager().getFields())
        {
            final TileEntity scareCrow = getColony().getWorld().getTileEntity(field);
            if (scareCrow instanceof TileEntityScarecrow)
            {
                if (((TileEntityScarecrow) scareCrow).isTaken())
                {
                    if (getAssignedCitizen().isEmpty() || ((TileEntityScarecrow) scareCrow).getOwnerId() == getMainCitizen().getId())
                    {
                        BlockPosUtil.writeToByteBuf(buf, field);
                    }
                }
                else
                {
                    BlockPosUtil.writeToByteBuf(buf, field);
                }
            }
        }

        buf.writeInt(farmerFields.size());
    }

    /**
     * Resets the fields to need work again.
     */
    public void resetFields()
    {
        for (@NotNull final BlockPos field : farmerFields)
        {
            final TileEntity scareCrow = getColony().getWorld().getTileEntity(field);
            if (scareCrow instanceof TileEntityScarecrow)
            {
                ((TileEntityScarecrow) scareCrow).setNeedsWork(true);
                ((TileEntityScarecrow) scareCrow).calculateSize(getColony().getWorld(), field.down());
            }
        }
    }

    /**
     * Synchronize field list with colony.
     *
     * @param world the world the building is in.
     */
    public void syncWithColony(@NotNull final World world)
    {
        if (!farmerFields.isEmpty())
        {
            @NotNull final ArrayList<BlockPos> tempFields = new ArrayList<>(farmerFields);

            for (@NotNull final BlockPos field : tempFields)
            {
                final TileEntity scarecrow = world.getTileEntity(field);
                if (scarecrow instanceof TileEntityScarecrow)
                {
                    ((TileEntityScarecrow) scarecrow).setName(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_SCARECROW_USER, getMainCitizen().getName()));
                    getColony().getWorld()
                            .notifyBlockUpdate(scarecrow.getPos(),
                                    getColony().getWorld().getBlockState(scarecrow.getPos()),
                                    getColony().getWorld().getBlockState(scarecrow.getPos()),
                                    BLOCK_UPDATE_FLAG);
                }
                else
                {
                    farmerFields.remove(field);
                    if (currentField != null && currentField.equals(field))
                    {
                        currentField = null;
                    }
                }
            }
        }
    }

    /**
     * Getter for the assign manually.
     *
     * @return true if he should.
     */
    public boolean assignManually()
    {
        return shouldAssignManually;
    }

    /**
     * Method called to free a field.
     *
     * @param position id of the field.
     */
    public void freeField(final BlockPos position)
    {
        final TileEntity scarecrow = getColony().getWorld().getTileEntity(position);
        if (scarecrow instanceof TileEntityScarecrow)
        {
            farmerFields.remove(position);
            ((TileEntityScarecrow) scarecrow).setTaken(false);
            ((TileEntityScarecrow) scarecrow).setOwner(0);
            getColony().getWorld()
                        .notifyBlockUpdate(scarecrow.getPos(),
                                getColony().getWorld().getBlockState(scarecrow.getPos()),
                                getColony().getWorld().getBlockState(scarecrow.getPos()),
                                BLOCK_UPDATE_FLAG);
            ((TileEntityScarecrow) scarecrow).setName(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_SCARECROW_USER,
                        LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_SCARECROW_USER_NOONE)));

        }
    }

    /**
     * Method called to assign a field to the farmer.
     *
     * @param position id of the field.
     */
    public void assignField(final BlockPos position)
    {
        final TileEntity scarecrow = getColony().getWorld().getTileEntity(position);
        if (scarecrow instanceof TileEntityScarecrow)
        {
            ((TileEntityScarecrow) scarecrow).setTaken(true);

            if (getMainCitizen() != null)
            {
                ((TileEntityScarecrow) scarecrow).setOwner(getMainCitizen().getId());
            }
            farmerFields.add(position);
        }
    }

    /**
     * Switches the assignManually of the farmer.
     *
     * @param assignManually true if assignment should be manual.
     */
    public void setAssignManually(final boolean assignManually)
    {
        this.shouldAssignManually = assignManually;
    }

    /**
     * Provides a view of the miner building class.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Checks if fields should be assigned manually.
         */
        private boolean shouldAssignFieldManually;

        /**
         * Contains a view object of all the fields in the colony.
         */
        @NotNull
        private List<BlockPos> fields = new ArrayList<>();

        /**
         * The amount of fields the farmer owns.
         */
        private int amountOfFields;

        /**
         * Public constructor of the view, creates an instance of it.
         *
         * @param c the colony.
         * @param l the position.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @Override
        @NotNull
        public Window getWindow()
        {
            return new WindowHutFarmer(this);
        }

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
        {
            fields = new ArrayList<>();
            super.deserialize(buf);
            shouldAssignFieldManually = buf.readBoolean();
            final int size = buf.readInt();
            for (int i = 1; i <= size; i++)
            {
                @NotNull final BlockPos pos = BlockPosUtil.readFromByteBuf(buf);
                fields.add(pos);
            }
            amountOfFields = buf.readInt();
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.ENDURANCE;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.CHARISMA;
        }

        /**
         * Should the farmer be assigned manually to the fields.
         *
         * @return true if yes.
         */
        public boolean assignFieldManually()
        {
            return shouldAssignFieldManually;
        }

        /**
         * Getter of the fields list.
         *
         * @return an unmodifiable List.
         */
        @NotNull
        public List<BlockPos> getFields()
        {
            return Collections.unmodifiableList(fields);
        }

        /**
         * Getter for amount of fields.
         *
         * @return the amount of fields.
         */
        public int getAmountOfFields()
        {
            return amountOfFields;
        }

        /**
         * Sets the assignedFieldManually in the view.
         *
         * @param assignFieldManually variable to set.
         */
        public void setAssignFieldManually(final boolean assignFieldManually)
        {
            MineColonies.getNetwork().sendToServer(new AssignmentModeMessage(this, assignFieldManually));
            this.shouldAssignFieldManually = assignFieldManually;
        }

        /**
         * Change a field at a certain position.
         *
         * @param id          the position of the field.
         * @param addNewField should new field be added.
         * @param scarecrowTileEntity         the tileEntity.
         */
        public void changeFields(final BlockPos id, final boolean addNewField, final TileEntityScarecrow scarecrowTileEntity)
        {
            MineColonies.getNetwork().sendToServer(new AssignFieldMessage(this, addNewField, id));
            scarecrowTileEntity.setTaken(addNewField);

            if (addNewField && !getWorkerId().isEmpty())
            {
                scarecrowTileEntity.setOwner(getWorkerId().get(0), getColony());
                amountOfFields++;
            }
            else
            {
                scarecrowTileEntity.setOwner(0, getColony());
                amountOfFields--;
            }
        }
    }
}
