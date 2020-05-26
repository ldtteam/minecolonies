package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.WindowHutPlantation;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.jobs.JobPlanter;
import com.minecolonies.coremod.network.messages.server.colony.building.plantation.PlantationSetPhaseMessage;
import com.minecolonies.coremod.research.UnlockBuildingResearchEffect;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Class of the plantation building.
 * Worker will grow sugarcane/bamboo/cactus + craft paper and books.
 */
public class BuildingPlantation extends AbstractBuildingCrafter
{
    /**
     * Description string of the building.
     */
    private static final String PLANTATION     = "plantation";

    /**
     * List of sand blocks to grow onto.
     */
    private List<BlockPos> sand = new ArrayList<>();

    /**
     * The current phase (default sugarcane).
     */
    private Item currentPhase = Items.SUGAR_CANE;

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
        return compound;
    }

    /**
     * Get a list of positions to check for crops for the current phase.
     * @param world the world.
     * @return the list of positions.
     */
    public List<BlockPos> getPosForPhase(final World world)
    {
        final List<BlockPos> filtered = new ArrayList<>();
        for (final BlockPos pos : sand)
        {
            if (currentPhase == Items.SUGAR_CANE)
            {
                if (world.getBlockState(pos.down()).getBlock() == Blocks.COBBLESTONE
                      && (world.getBlockState(pos.north()).getBlock() == Blocks.WATER
                      || world.getBlockState(pos.south()).getBlock() == Blocks.WATER
                      || world.getBlockState(pos.east()).getBlock() == Blocks.WATER
                      || world.getBlockState(pos.west()).getBlock() == Blocks.WATER))
                {
                    filtered.add(pos);
                }
            }
            else if (currentPhase == Items.CACTUS)
            {
                if (world.getBlockState(pos.down()).getBlock() == Blocks.COBBLESTONE
                      && world.getBlockState(pos.north()).getBlock() != Blocks.WATER
                      && world.getBlockState(pos.south()).getBlock() != Blocks.WATER
                      && world.getBlockState(pos.east()).getBlock() != Blocks.WATER
                      && world.getBlockState(pos.west()).getBlock() != Blocks.WATER)
                {
                    filtered.add(pos);
                }
            }
            else
            {
                //Bamboo
                if (world.getBlockState(pos.down()).getBlock() == Blocks.STONE_BRICKS)
                {
                    filtered.add(pos);
                }
            }
        }
        return filtered;
    }

    @NotNull
    @Override
    public IJob createJob(final ICitizenData citizen)
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
    public boolean canRecipeBeAdded(final IToken token)
    {

        Optional<Boolean> isRecipeAllowed;

        if (!super.canRecipeBeAdded(token))
        {
            return false;
        }

        isRecipeAllowed = super.canRecipeBeAddedBasedOnTags(token);
        if (isRecipeAllowed.isPresent())
        {
            return isRecipeAllowed.get();
        }
        else
        {
            // Additional recipe rules

            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);

            // End Additional recipe rules
        }

        return false;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.plantation;
    }

    @Override
    public void requestUpgrade(final PlayerEntity player, final BlockPos builder)
    {
        final UnlockBuildingResearchEffect effect = colony.getResearchManager().getResearchEffects().getEffect("Plantation", UnlockBuildingResearchEffect.class);
        if (effect == null)
        {
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.havetounlock"));
            return;
        }
        super.requestUpgrade(player, builder);
    }

    /**
     * Set the current phase.
     * @param phase the phase to set.
     */
    public void setPhase(final Item phase)
    {
        this.currentPhase = phase;
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
        buf.writeItemStack(new ItemStack(currentPhase));
    }

    /**
     * Get the current phase.
     * @return the current phase item.
     */
    public Item getCurrentPhase()
    {
        return currentPhase;
    }

    /**
     * Plantation View.
     */
    public static class View extends AbstractBuildingCrafter.View
    {
        /**
         * All possible phases.
         */
        private List<Item> phases = new ArrayList<>();

        /**
         * The current phase.
         */
        private Item currentPhase;

        /**
         * Instantiate the plantation view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
            phases.add(Items.SUGAR_CANE);
            phases.add(Items.CACTUS);
            phases.add(Items.BAMBOO);
        }

        /**
         * Get the list of all phases.
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
            this.currentPhase = buf.readItemStack().getItem();
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutPlantation(this);
        }

        /**
         * Get the current phase.
         * @return the phase.
         */
        public Item getCurrentPhase()
        {
            return currentPhase;
        }

        /**
         * Set a new phase.
         * @param phase the phase to set.
         */
        public void setPhase(final Item phase)
        {
            this.currentPhase = phase;
            Network.getNetwork().sendToServer(new PlantationSetPhaseMessage(this, phase));
        }
    }
}
