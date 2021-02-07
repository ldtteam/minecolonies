package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.client.gui.WindowHutLumberjack;
import com.minecolonies.coremod.colony.buildings.AbstractFilterableListCrafter;
import com.minecolonies.coremod.colony.buildings.views.AbstractFilterableListsView;
import com.minecolonies.coremod.colony.jobs.JobLumberjack;
import com.minecolonies.coremod.util.AttributeModifierUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.CitizenConstants.SKILL_BONUS_ADD;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * The lumberjacks building.
 */
public class BuildingLumberjack extends AbstractFilterableListCrafter
{
    /**
     * NBT tag if the lj should replant saplings
     */
    private static final String TAG_REPLANT = "shouldReplant";

    /**
     * NBT tag for lj restriction start
     */
    private static final String TAG_RESTRICT_START = "startRestrictionPosition";

    /**
     * NBT tag for lj restriction end
     */
    private static final String TAG_RESTRICT_END = "endRestrictionPosition";

    /**
     * Nbt tag for restriction setting.
     */
    private static final String TAG_RESTRICT = "restrict";

    /**
     * Whether or not the LJ should replant saplings
     */
    private boolean replant = true;

    /**
     * Whether or not the LJ should be restricted
     */
    private boolean restrict = false;

    /**
     * The start position of the restricted area.
     */
    private BlockPos startRestriction = null;

    /**
     * The end position of the restricted area.
     */
    private BlockPos endRestriction = null;

    /**
     * The maximum upgrade of the building.
     */
    private static final int    MAX_BUILDING_LEVEL = 5;
    /**
     * The job description.
     */
    private static final String LUMBERJACK = "lumberjack";

    /**
     * A list of all planted nether trees
     */
    private final Set<BlockPos> netherTrees = new HashSet<>();

    /**
     * Modifier for fungi growing time. Increase to speed up.
     */
    private static final int FUNGI_MODIFIER = 10;

    /**
     * Public constructor of the building, creates an object of the building.
     *
     * @param c the colony.
     * @param l the position.
     */
    public BuildingLumberjack(final IColony c, final BlockPos l)
    {
        super(c, l);

        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.HOE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
    }

    @Override
    public boolean canBeGathered()
    {
        // Normal crafters are only gatherable when they have a task, i.e. while producing stuff.
        // BUT, the lumberjack both gathers and crafts things now, so it should always be gatherable.
        // This unfortunately means that the dman will sometimes "steal" ingredients from the LJ.
        // Fortunately, the dman is smart enough to not instantly gather the ingredients it brought to the LJ.
        // Might be improved in the future. For now, it's a bit annoying, but not too bad imho.
        return true;
    }

    @Override
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> toKeep = new HashMap<>(super.getRequiredItemsAndAmount());

        if (getMainCitizen() != null && getMainCitizen().getInventory() != null)
        {
            final int invSIze = getMainCitizen().getInventory().getSlots();
            int keptStacks = 0;
            for (int i = 0; i < invSIze; i++)
            {
                final ItemStack stack = getMainCitizen().getInventory().getStackInSlot(i);

                if (ItemStackUtils.isEmpty(stack) || !ItemStackUtils.isStackSapling(stack))
                {
                    continue;
                }

                boolean isAlreadyInList = false;
                for (final Map.Entry<Predicate<ItemStack>, Tuple<Integer, Boolean>> entry : toKeep.entrySet())
                {
                    if (entry.getKey().test(stack))
                    {
                        isAlreadyInList = true;
                    }
                }

                if (!isAlreadyInList)
                {
                    toKeep.put(stack::isItemEqual, new Tuple<>(com.minecolonies.api.util.constant.Constants.STACKSIZE, true));
                    keptStacks++;

                    if (keptStacks >= getMaxBuildingLevel() * 2)
                    {
                        return toKeep;
                    }
                }
            }
        }

        return toKeep;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.lumberjack;
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return LUMBERJACK;
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
    }

    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return new JobLumberjack(citizen);
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);

        if (compound.keySet().contains(TAG_REPLANT))
        {
            replant = compound.getBoolean(TAG_REPLANT);
        }
        else
        {
            replant = true;
        }

        if (compound.keySet().contains(TAG_RESTRICT))
        {
            restrict = compound.getBoolean(TAG_RESTRICT);
        }

        if (compound.keySet().contains(TAG_RESTRICT_START))
        {
            startRestriction = NBTUtil.readBlockPos(compound.getCompound(TAG_RESTRICT_START));
        }
        else
        {
            startRestriction = null;
        }

        if (compound.keySet().contains(TAG_RESTRICT_END))
        {
            endRestriction = NBTUtil.readBlockPos(compound.getCompound(TAG_RESTRICT_END));
        }
        else
        {
            endRestriction = null;
        }

        final ListNBT netherTreeBinTagList = compound.getList(TAG_NETHER_TREE_LIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < netherTreeBinTagList.size(); i++)
        {
            netherTrees.add(BlockPosUtil.readFromListNBT(netherTreeBinTagList, i));
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();

        compound.putBoolean(TAG_REPLANT, replant);

        if (startRestriction != null)
        {
            compound.put(TAG_RESTRICT_START, NBTUtil.writeBlockPos(startRestriction));
        }

        if (endRestriction != null)
        {
            compound.put(TAG_RESTRICT_END, NBTUtil.writeBlockPos(endRestriction));
        }

        @NotNull final ListNBT netherTreeBinCompoundList = new ListNBT();
        for (@NotNull final BlockPos pos : netherTrees)
        {
            BlockPosUtil.writeToListNBT(netherTreeBinCompoundList, pos);
        }
        compound.put(TAG_NETHER_TREE_LIST, netherTreeBinCompoundList);

        compound.putBoolean(TAG_RESTRICT, restrict);
        return compound;
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return LUMBERJACK;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Strength;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Focus;
    }

    @Override
    public boolean canCraftComplexRecipes()
    {
        return false;
    }

    @Override
    public boolean isRecipeAlterationAllowed()
    {
        return false;
    }

    @Override
    public boolean canRecipeBeAdded(final IToken<?> token)
    {
        return false;
    }

    @Override
    public void openCraftingContainer(final ServerPlayerEntity player)
    {
        return;
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeBoolean(replant);
        buf.writeBoolean(restrict);
    }

    /**
     * Whether or not the LJ should replant saplings.
     *
     * @return true if so.
     */
    public boolean shouldReplant()
    {
        return replant;
    }

    /**
     * Set whether or not LJ should replant saplings
     *
     * @param shouldReplant whether or not the LJ should replant
     */
    public void setShouldReplant(final boolean shouldReplant)
    {
        this.replant = shouldReplant;
        markDirty();
    }

    /**
     * Whether or not the LJ should be restricted.
     *
     * @return true if it should restrict.
     */
    public boolean shouldRestrict()
    {
        if (restrict)
        {
            if (startRestriction == null || endRestriction == null)
            {
                restrict = false;
                markDirty();
            }
        }
        return restrict;
    }

    /**
     * Set whether or not LJ should replant saplings
     *
     * @param shouldRestrict whether or not the LJ should be restricted
     */
    public void setShouldRestrict(final boolean shouldRestrict)
    {
        this.restrict = shouldRestrict;
        markDirty();
    }

    public void setRestrictedArea(final BlockPos startPosition, final BlockPos endPosition)
    {
        this.startRestriction = startPosition;
        this.endRestriction = endPosition;
    }

    public BlockPos getStartRestriction()
    {
        return this.startRestriction;
    }

    public BlockPos getEndRestriction()
    {
        return this.endRestriction;
    }

    @Override
    public void removeCitizen(final ICitizenData citizen)
    {
        if (citizen != null)
        {
            final Optional<AbstractEntityCitizen> optCitizen = citizen.getEntity();
            optCitizen.ifPresent(entityCitizen -> AttributeModifierUtils.removeModifier(entityCitizen, SKILL_BONUS_ADD, Attributes.MOVEMENT_SPEED));
        }
        super.removeCitizen(citizen);
    }

    /**
     * Returns early if no worker is assigned
     * Iterates over the nether tree position list
     * If position is a fungus, grows it depending on worker's level
     * If the block has changed, removes the position from the list and returns early
     * If the position is not a fungus, removes the position from the list
     *
     */
    private void bonemealFungi()
    {
        if (getMainCitizen() == null)
        {
            return;
        }
        final int modifier = Math.max(0, Math.min(FUNGI_MODIFIER, 100));
        for (final BlockPos pos : netherTrees)
        {
            final World world = colony.getWorld();
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockState blockState = world.getBlockState(pos);
                final Block block = blockState.getBlock();
                if (block == Blocks.CRIMSON_FUNGUS || block == Blocks.WARPED_FUNGUS)
                {
                    int threshold = modifier + (int) Math.ceil(getMainCitizen().getCitizenSkillHandler().getLevel(getPrimarySkill())*(1-((float) modifier/100)));
                    final int rand = world.getRandom().nextInt(100);
                    if (rand < threshold)
                    {
                        final IGrowable growable = (IGrowable) block;
                        if (growable.canGrow(world, pos, blockState, world.isRemote))
                        {
                            if (!world.isRemote) {
                                if(growable.canUseBonemeal(world, world.rand, pos, blockState))
                                {
                                    growable.grow((ServerWorld) world, world.rand, pos, blockState);
                                    return;
                                }
                            }
                        }
                    }

                }
                else
                {
                    removeNetherTree(pos);
                }
            }
        }
    }

    /**
     * Returns a list of the registered nether trees to grow.
     * @return a copy of the list
     */
    public Set<BlockPos> getNetherTrees()
    {
        return new HashSet<>(netherTrees);
    }

    /**
     * Removes a position from the nether trees
     * @param pos the position
     */
    public void removeNetherTree(BlockPos pos)
    {
        netherTrees.remove(pos);
    }

    /**
     * Adds a position to the nether trees
     * @param pos the position
     */
    public void addNetherTree(BlockPos pos)
    {
        netherTrees.add(pos);
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        super.onColonyTick(colony);
        bonemealFungi();
    }

    /**
     * Provides a view of the lumberjack building class.
     */
    public static class View extends AbstractFilterableListsView
    {
        /**
         * Whether or not the LJ should replant saplings
         */
        public boolean shouldReplant = true;

        /**
         * Whether or not the LJ should be restricted
         */
        public boolean shouldRestrict = false;

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
        public void deserialize(@NotNull final PacketBuffer buf)
        {
            super.deserialize(buf);
            shouldReplant = buf.readBoolean();
            shouldRestrict = buf.readBoolean();
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutLumberjack(this);
        }
    }
}
