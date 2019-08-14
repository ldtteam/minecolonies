package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.jobs.JobBlacksmith;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Creates a new building for the blacksmith.
 */
public class BuildingBlacksmith extends AbstractBuildingCrafter
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String BLACKSMITH = "blacksmith";

    /**
     * Instantiates the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingBlacksmith(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return BLACKSMITH;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @NotNull
    @Override
    public IJob createJob(final ICitizenData citizen)
    {
        return new JobBlacksmith(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return BLACKSMITH;
    }

    @Override
    public boolean canRecipeBeAdded(final IToken token)
    {
        if(!super.canRecipeBeAdded(token))
        {
            return false;
        }

        final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
        if(storage == null)
        {
            return false;
        }

        final int size = storage.getCleanedInput().size();
        int ingots = 0;
        for(final ItemStorage itemStorage : storage.getCleanedInput())
        {
            final ItemStack stack = itemStorage.getItemStack();
            if (!ItemStackUtils.isEmpty(stack))
            {
                if (stack.getItem().isIn(Tags.Items.INGOTS))
                {
                    ingots++;
                    break;
                }
            }
        }

        final ItemStack output = storage.getPrimaryOutput();
        return output.getItem() instanceof ToolItem || output.getItem() instanceof SwordItem || output.getItem() instanceof ArmorItem || output.getItem() instanceof HoeItem || Compatibility.isTinkersWeapon(output) || ingots == size;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.blacksmith;
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends AbstractBuildingCrafter.View
    {
        /**
         * Instantiates the view of the building.
         *
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        public Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<>(this, BLACKSMITH);
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.DEXTERITY;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.STRENGTH;
        }
    }
}
