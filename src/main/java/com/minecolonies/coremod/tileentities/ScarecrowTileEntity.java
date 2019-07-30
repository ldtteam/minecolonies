package com.minecolonies.coremod.tileentities;

import com.ldtteam.structurize.blocks.ModBlocks;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;

/**
 * The scarecrow tile entity to store extra data.
 */
public class ScarecrowTileEntity extends TileEntityChest
{
    /**
     * The max width/length of a field.
     */
    private static final int MAX_RANGE = 5;

    /**
     * Has the field be taken by any worker?
     */
    private boolean taken = false;

    /**
     * Checks if the field doesNeedWork (Hoeig, Seedings, Farming etc).
     */
    private boolean doesNeedWork = true;

    /**
     * Has the field been planted?
     */
    private ScarecrowTileEntity.FieldStage fieldStage = ScarecrowTileEntity.FieldStage.EMPTY;

    /**
     * The length to plus x of the field.
     */
    private int lengthPlusX;

    /**
     * The width to plus z of the seed.
     */
    private int widthPlusZ;

    /**
     * The length to minus xof the field.
     */
    private int lengthMinusX;

    /**
     * The width to minus z of the seed.
     */
    private int widthMinusZ;

    /**
     * Citizen Id of the citizen owning the field.
     */
    private int ownerId;

    /**
     * Name of the citizen claiming the field.
     */
    @NotNull
    private String owner = "";

    /**
     * Random generator.
     */
    private final Random random = new Random();

    /**
     * The type of the scarecrow.
     */
    private ScareCrowType type;

    /**
     * The colony of the field.
     */
    @Nullable
    private Colony colony;

    /**
     * Name of the scarecrow, string set in the GUI.
     */
    private String name;

    /**
     * Inventory of the field.
     */
    private final IItemHandlerModifiable inventory = new ItemStackHandler(1);

    /**
     * Creates an instance of the tileEntity.
     */
    public ScarecrowTileEntity()
    {
        super();
        name = LanguageHandler.format("com.minecolonies.coremod.gui.scarecrow.user", LanguageHandler.format(owner));
    }

    /**
     * Getter of the name of the tileEntity.
     *
     * @return the string.
     */
    public String getDesc()
    {
        return name;
    }

    /**
     * Setter for the name.
     *
     * @param name string to set.
     */
    public void setName(final String name)
    {
        this.name = name;
        setCustomName(name);
        markDirty();
    }

    /**
     * Getter for MAX_RANGE.
     *
     * @return the max range.
     */
    private static int getMaxRange()
    {
        return MAX_RANGE;
    }

    /**
     * Calculates recursively the length of the field until a certain point.
     * <p>
     * This mutates the field!
     *
     * @param position the start position.
     * @param world    the world the field is in.
     */
    public final void calculateSize(@NotNull final World world, @NotNull final BlockPos position)
    {
        //Calculate in all 4 directions
        this.lengthPlusX = searchNextBlock(0, position.east(), EnumFacing.EAST, world);
        this.lengthMinusX = searchNextBlock(0, position.west(), EnumFacing.WEST, world);
        this.widthPlusZ = searchNextBlock(0, position.south(), EnumFacing.SOUTH, world);
        this.widthMinusZ = searchNextBlock(0, position.north(), EnumFacing.NORTH, world);
        markDirty();
    }

    /**
     * Calculates the field size into a specific direction.
     *
     * @param blocksChecked how many blocks have been checked.
     * @param position      the start position.
     * @param direction     the direction to search.
     * @param world         the world object.
     * @return the distance.
     */
    private int searchNextBlock(final int blocksChecked, @NotNull final BlockPos position, final EnumFacing direction, @NotNull final World world)
    {
        if (blocksChecked >= getMaxRange() || isNoPartOfField(world, position))
        {
            return blocksChecked;
        }
        return searchNextBlock(blocksChecked + 1, position.offset(direction), direction, world);
    }

    /**
     * Checks if a certain position is part of the field. Complies with the definition of field block.
     *
     * @param world    the world object.
     * @param position the position.
     * @return true if it is.
     */
    public boolean isNoPartOfField(@NotNull final World world, @NotNull final BlockPos position)
    {
        return world.isAirBlock(position) ||  isValidDelimiter(world.getBlockState(position.up()).getBlock());
    }

    /**
     * Check if a block is a valid delimiter of the field.
     * @param block the block to analyze.
     * @return true if so.
     */
    private static boolean isValidDelimiter(final Block block)
    {
        return block instanceof BlockFence || block instanceof BlockFenceGate || block == ModBlocks.blockCactusFence || block == ModBlocks.blockCactusFenceGate || block instanceof BlockWall;
    }

    /**
     * Returns the {@link BlockPos} of the current object, also used as ID.
     *
     * @return {@link BlockPos} of the current object.
     */
    public BlockPos getID()
    {
        // Location doubles as ID
        return this.getPos();
    }

    /**
     * Has the field been taken?
     *
     * @return true if the field is not free to use, false after releasing it.
     */
    public boolean isTaken()
    {
        return this.taken;
    }

    /**
     * Sets the field taken.
     *
     * @param taken is field free or not
     */
    public void setTaken(final boolean taken)
    {
        this.taken = taken;
        markDirty();
    }

    public void nextState()
    {
        if (getFieldStage().ordinal() + 1 >= FieldStage.values().length)
        {
            doesNeedWork = false;
            setFieldStage(FieldStage.values()[0]);
            return;
        }
        setFieldStage(FieldStage.values()[getFieldStage().ordinal() + 1]);
        markDirty();
    }

    /**
     * Checks if the field has been planted.
     *
     * @return true if there are crops planted.
     */
    public FieldStage getFieldStage()
    {
        return this.fieldStage;
    }

    /**
     * Sets if there are any crops planted.
     *
     * @param fieldStage true after planting, false after harvesting.
     */
    public void setFieldStage(final FieldStage fieldStage)
    {
        this.fieldStage = fieldStage;
        markDirty();
    }

    /**
     * Checks if the field needs work (planting, hoeing).
     *
     * @return true if so.
     */
    public boolean needsWork()
    {
        return this.doesNeedWork;
    }

    /**
     * Sets that the field needs work.
     *
     * @param needsWork true if work needed, false after completing the job.
     */
    public void setNeedsWork(final boolean needsWork)
    {
        this.doesNeedWork = needsWork;
        markDirty();
    }

    /**
     * Getter of the seed of the field.
     *
     * @return the ItemSeed
     */
    @Nullable
    public ItemStack getSeed()
    {
        if (inventory.getStackInSlot(0) != ItemStackUtils.EMPTY && inventory.getStackInSlot(0).getItem() instanceof IPlantable)
        {
            return inventory.getStackInSlot(0);
        }
        return null;
    }

    /**
     * Getter of the length in plus x direction.
     *
     * @return field length.
     */
    public int getLengthPlusX()
    {
        return lengthPlusX;
    }

    /**
     * Getter of the with in plus z direction.
     *
     * @return field width.
     */
    public int getWidthPlusZ()
    {
        return widthPlusZ;
    }

    /**
     * Getter of the length in minus x direction.
     *
     * @return field length.
     */
    public int getLengthMinusX()
    {
        return lengthMinusX;
    }

    /**
     * Getter of the with in minus z direction.
     *
     * @return field width.
     */
    public int getWidthMinusZ()
    {
        return widthMinusZ;
    }

    /**
     * Getter of the owner of the field.
     *
     * @return the string description of the citizen.
     */
    @NotNull
    public String getOwner()
    {
        return owner;
    }

    /**
     * Getter for the ownerId of the field.
     * @return the int id.
     */
    public int getOwnerId()
    {
        return ownerId;
    }

    /**
     * Sets the owner of the field.
     *
     * @param ownerId the id of the citizen.
     */
    public void setOwner(@NotNull final int ownerId)
    {
        this.ownerId = ownerId;
        if(colony != null)
        {
            if(colony.getCitizenManager().getCitizen(ownerId) == null)
            {
                owner = "";
            }
            else
            {
                owner = colony.getCitizenManager().getCitizen(ownerId).getName();
            }
        }
        setName(LanguageHandler.format("com.minecolonies.coremod.gui.scarecrow.user", LanguageHandler.format(owner)));
        markDirty();
    }

    /**
     * Sets the owner of the field.
     *
     * @param ownerId the name of the citizen.
     * @param tempColony the colony view.
     */
    public void setOwner(final int ownerId, final ColonyView tempColony)
    {
        this.ownerId = ownerId;
        if(tempColony != null)
        {
            if(tempColony.getCitizen(ownerId) == null)
            {
                owner = "";
            }
            else
            {
                owner = tempColony.getCitizen(ownerId).getName();
            }
        }
        markDirty();
    }

    /**
     * Describes the stage the field is in.
     * Like if it has been hoed, planted or is empty.
     */
    public enum FieldStage
    {
        EMPTY,
        HOED,
        PLANTED
    }

    /**
     * Get the inventory of the scarecrow.
     * @return the IItemHandler.
     */
    public IItemHandlerModifiable getInventory()
    {
        return inventory;
    }

    ///////////---- Following methods are used to update the tileEntity between client and server ----///////////

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        final NBTTagCompound compound = new NBTTagCompound();
        this.writeToNBT(compound);
        if(colony != null)
        {
            compound.setInteger(TAG_COLONY_ID, colony.getID());
        }
        return new SPacketUpdateTileEntity(this.pos, 0, compound);
    }

    @NotNull
    @Override
    public NBTTagCompound getUpdateTag()
    {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity packet)
    {
        final NBTTagCompound compound = packet.getNbtCompound();
        this.readFromNBT(compound);
        if(compound.hasKey(TAG_COLONY_ID))
        {
            setOwner(ownerId, ColonyManager.getColonyView(compound.getInteger(TAG_COLONY_ID), world.provider.getDimension()));
        }
    }

    /////////////--------------------------- End Synchronization-area ---------------------------- /////////////

    @Override
    public void onLoad()
    {
        super.onLoad();
        final World world = getWorld();

        colony = ColonyManager.getColonyByPosFromWorld(world, pos);
        if (colony != null && !colony.getBuildingManager().getFields().contains(pos))
        {
            @Nullable final Entity entity = EntityUtils.getEntityFromUUID(world, colony.getPermissions().getOwner());

            if (entity instanceof EntityPlayer)
            {
                colony.getBuildingManager().addNewField(this, pos, world);
            }
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound)
    {
        final NBTTagList inventoryTagList = compound.getTagList(TAG_INVENTORY, TAG_COMPOUND);
        for (int i = 0; i < inventoryTagList.tagCount(); ++i)
        {
            final NBTTagCompound inventoryCompound = inventoryTagList.getCompoundTagAt(i);
            final ItemStack stack = new ItemStack(inventoryCompound);
            if (ItemStackUtils.getSize(stack) <= 0)
            {
                inventory.setStackInSlot(i, ItemStackUtils.EMPTY);
            }
            else
            {
                inventory.setStackInSlot(i, stack);
            }
        }

        taken = compound.getBoolean(TAG_TAKEN);
        fieldStage = FieldStage.values()[compound.getInteger(TAG_STAGE)];
        lengthPlusX = compound.getInteger(TAG_LENGTH_PLUS);
        widthPlusZ = compound.getInteger(TAG_WIDTH_PLUS);
        lengthMinusX = compound.getInteger(TAG_LENGTH_MINUS);
        widthMinusZ = compound.getInteger(TAG_WIDTH_MINUS);
        ownerId = compound.getInteger(TAG_OWNER);
        name = compound.getString(TAG_NAME);
        setOwner(ownerId);

        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound)
    {
        @NotNull final NBTTagList inventoryTagList = new NBTTagList();
        for (int slot = 0; slot < inventory.getSlots(); slot++)
        {
            @NotNull final NBTTagCompound inventoryCompound = new NBTTagCompound();
            final ItemStack stack = inventory.getStackInSlot(slot);
            if (stack == ItemStackUtils.EMPTY)
            {
                new ItemStack(Blocks.AIR, 0).writeToNBT(inventoryCompound);
            }
            else
            {
                stack.writeToNBT(inventoryCompound);
            }
            inventoryTagList.appendTag(inventoryCompound);
        }
        compound.setTag(TAG_INVENTORY, inventoryTagList);

        compound.setBoolean(TAG_TAKEN, taken);
        compound.setInteger(TAG_STAGE, fieldStage.ordinal());
        compound.setInteger(TAG_LENGTH_PLUS, lengthPlusX);
        compound.setInteger(TAG_WIDTH_PLUS, widthPlusZ);
        compound.setInteger(TAG_LENGTH_MINUS, lengthMinusX);
        compound.setInteger(TAG_WIDTH_MINUS, widthMinusZ);
        compound.setInteger(TAG_OWNER, ownerId);
        compound.setString(TAG_NAME, name);

        return super.writeToNBT(compound);
    }


    //----------------------- Type Specific parameters -----------------------//

    /**
     * Returns the type of the scarecrow (Important for the rendering).
     *
     * @return the enum type.
     */
    public ScareCrowType getType()
    {
        if (this.type == null)
        {
            this.type = ScareCrowType.values()[this.random.nextInt(2)];
        }
        return this.type;
    }

    /**
     * Enum describing the different textures the scarecrow has.
     */
    public enum ScareCrowType
    {
        PUMPKINHEAD,
        NORMAL
    }
}
