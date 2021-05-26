package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.huts.WindowHutPlantationModule;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.jobs.JobPlanter;
import com.minecolonies.coremod.network.messages.server.colony.building.plantation.PlantationSetPhaseMessage;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.research.util.ResearchConstants.PLANT_2;
import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Class of the plantation building. Worker will grow sugarcane/bamboo/cactus + craft paper and books.
 */
public class BuildingPlantation extends AbstractBuildingCrafter
{
    /**
     * Description string of the building.
     */
    private static final String PLANTATION = "plantation";

    /**
     * List of sand blocks to grow onto.
     */
    private final List<BlockPos> sand = new ArrayList<>();

    /**
     * The current phase (default sugarcane).
     */
    private Item currentPhase = Items.SUGAR_CANE;

    /**
     * The setting in the hut. If it's in the "pick 2 mode" this is the disabled mode else this is the only used one.
     */
    private Item setting = Items.SUGAR_CANE;

    /**
     * All the possible settings.
     */
    private final List<Item> settings = Arrays.asList(Items.SUGAR_CANE, Items.CACTUS, Items.BAMBOO);

    /**
     * Instantiates a new plantation building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingPlantation(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return PLANTATION;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block == Blocks.SAND)
        {
            final Block down = world.getBlockState(pos.down()).getBlock();
            if (down == Blocks.COBBLESTONE || down == Blocks.STONE_BRICKS)
            {
                sand.add(pos);
            }
        }
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        final ListNBT sandPos = compound.getList(TAG_PLANTGROUND, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < sandPos.size(); ++i)
        {
            sand.add(NBTUtil.readBlockPos(sandPos.getCompound(i).getCompound(TAG_POS)));
        }
        this.currentPhase = ItemStack.read(compound.getCompound(TAG_CURRENT_PHASE)).getItem();
        if (compound.keySet().contains(TAG_SETTING))
        {
            this.setting = ItemStack.read(compound.getCompound(TAG_SETTING)).getItem();
        }
        else
        {
            this.setting = this.currentPhase;
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        @NotNull final ListNBT sandCompoundList = new ListNBT();
        for (@NotNull final BlockPos entry : sand)
        {
            @NotNull final CompoundNBT sandCompound = new CompoundNBT();
            sandCompound.put(TAG_POS, NBTUtil.writeBlockPos(entry));
            sandCompoundList.add(sandCompound);
        }
        compound.put(TAG_PLANTGROUND, sandCompoundList);
        compound.put(TAG_CURRENT_PHASE, new ItemStack(currentPhase).write(new CompoundNBT()));
        compound.put(TAG_SETTING, new ItemStack(setting).write(new CompoundNBT()));
        return compound;
    }

    /**
     * Get a list of positions to check for crops for the current phase.
     *
     * @param world the world.
     * @return the list of positions.
     */
    public List<BlockPos> getPosForPhase(final World world)
    {
        final List<BlockPos> filtered = new ArrayList<>();
        if (tileEntity != null && !tileEntity.getPositionedTags().isEmpty())
        {
            for (final Map.Entry<BlockPos, List<String>> entry : tileEntity.getPositionedTags().entrySet())
            {
                if ((entry.getValue().contains("bamboo") && currentPhase == Items.BAMBOO)
                      || (entry.getValue().contains("sugar") && currentPhase == Items.SUGAR_CANE)
                      || (entry.getValue().contains("cactus") && currentPhase == Items.CACTUS))
                {
                    filtered.add(getPosition().add(entry.getKey()));
                }
            }
        }

        return filtered;
    }

    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return new JobPlanter(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return PLANTATION;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Agility;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Dexterity;
    }

    @Override
    public boolean canRecipeBeAdded(final IToken<?> token)
    {
        if (!super.canRecipeBeAdded(token))
        {
            return false;
        }

        return isRecipeCompatibleWithCraftingModule(token);
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.plantation;
    }

    /**
     * Set the current phase.
     *
     * @param phase the phase to set.
     */
    public void setSetting(final Item phase)
    {
        this.setting = phase;
        this.currentPhase = this.setting;
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeItemStack(new ItemStack(setting));
    }

    /**
     * Get the current phase.
     *
     * @return the current phase item.
     */
    public Item getCurrentPhase()
    {
        return currentPhase;
    }

    /**
     * Iterates over available plants
     * @return the item of the new or unchanged plant phase
     */
    public Item nextPlantPhase()
    {
        if (getColony().getResearchManager().getResearchEffects().getEffectStrength(PLANT_2) > 0)
        {
            int next = settings.indexOf(currentPhase);

            do
            {
                next = (next + 1) % settings.size();
            }
            while (settings.get(next) == setting);

            currentPhase = settings.get(next);
            return currentPhase;
        }

        return setting;
    }

    /**
     * Plantation View.
     */
    public static class View extends AbstractBuildingCrafter.View
    {
        /**
         * All possible phases.
         */
        private final List<Item> phases = Arrays.asList(Items.SUGAR_CANE, Items.CACTUS, Items.BAMBOO);

        /**
         * The current phase.
         */
        private Item setting;

        /**
         * Instantiate the plantation view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        /**
         * Get the list of all phases.
         *
         * @return the list.
         */
        public List<Item> getPhases()
        {
            return phases;
        }

        @Override
        public void deserialize(@NotNull final PacketBuffer buf)
        {
            super.deserialize(buf);
            this.setting = buf.readItemStack().getItem();
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutPlantationModule(this);
        }

        /**
         * Get the current setting.
         *
         * @return the phase.
         */
        public Item getSetting()
        {
            return setting;
        }

        /**
         * Set a new phase.
         *
         * @param phase the phase to set.
         */
        public void setPhase(final Item phase)
        {
            this.setting = phase;
            Network.getNetwork().sendToServer(new PlantationSetPhaseMessage(this, phase));
        }
    }

    public static class CraftingModule extends AbstractCraftingBuildingModule.Crafting
    {
        @Nullable
        @Override
        public IJob<?> getCraftingJob()
        {
            return getMainBuildingJob().orElseGet(() -> new JobPlanter(null));
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;
            final Optional<Boolean> isRecipeAllowed = CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, PLANTATION);
            return isRecipeAllowed.orElse(false);
        }
    }
}
