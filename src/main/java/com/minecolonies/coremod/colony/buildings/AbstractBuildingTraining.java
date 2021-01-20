package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.IBuildingCanBeHiredFrom;

import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.BuildingHiringModeMessage;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractBuildingTraining extends AbstractBuildingWorker {
    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public AbstractBuildingTraining(@NotNull IColony c, BlockPos l) {
        super(c, l);
    }

    public static class View extends AbstractBuildingView implements IBuildingCanBeHiredFrom {
        /**
         * List of the worker ids.
         */
        private final Set<Integer> workerIDs = new HashSet<>();

        /**
         * List of recipes.
         */
        private final List<IRecipeStorage> recipes = new ArrayList<>();

        /**
         * Variable defining if the building owner can craft complex 3x3 recipes.
         */
        private boolean canCraftComplexRecipes;

        /**
         * The hiring mode of the building.
         */
        private HiringMode hiringMode;

        /**
         * The name of the job.
         */
        private String jobName;

        /**
         * The max amount of inhabitants
         */
        private int maxInhabitants = 1;

        /**
         * The primary skill.
         */
        private Skill primary = Skill.Intelligence;

        /**
         * The secondary skill.
         */
        private Skill secondary = Skill.Intelligence;

        /**
         * The maximum number of recipes this building can have currently
         */
        private int maxRecipes;

        /**
         * If the building allows altering of recipes
         */
        private boolean isRecipeAlterationAllowed;

        /**
         * The job display name
         */
        private String jobDisplayName;

        /**
         * Creates a building view.
         *
         * @param c ColonyView the building is in.
         * @param l The location of the building.
         */
        protected View(IColonyView c, @NotNull BlockPos l) {
            super(c, l);
        }

        @Override
        public List<Integer> getWorkerId() {
            return new ArrayList<>(workerIDs);
        }

        @Override
        public void addWorkerId(int workerId) {
            workerIDs.add(workerId);
        }

        @Override
        public List<IRecipeStorage> getRecipes() {
            return new ArrayList<>(recipes);
        }

        @Override
        public void removeRecipe(int i) {
            if (i < recipes.size() && i >= 0)
            {
                recipes.remove(i);
            }
        }

        @Override
        public void switchIndex(int i, int j) {
            if (i < recipes.size() && j < recipes.size() && i >= 0 && j >= 0)
            {
                final IRecipeStorage storage = recipes.get(i);
                recipes.set(i, recipes.get(j));
                recipes.set(j, storage);
            }
        }

        @NotNull
        @Override
        public Skill getPrimarySkill() {
            return primary;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill() {
            return secondary;
        }

        @Override
        public void removeWorkerId(int id) {
            workerIDs.remove(id);
        }

        @Override
        public boolean hasEnoughWorkers() {
            return !workerIDs.isEmpty();
        }

        @Override
        public boolean canCraftComplexRecipes() {
            return this.canCraftComplexRecipes;
        }

        @Override
        public boolean canRecipeBeAdded() {
            return getMaxRecipes() > getRecipes().size();
        }
        public int getMaxRecipes()
        {
            return maxRecipes;
        }
        @Override
        public boolean isRecipeAlterationAllowed() {
            return isRecipeAlterationAllowed;
        }

        @Override
        public HiringMode getHiringMode() {
            return hiringMode;
        }

        @Override
        public void setHiringMode(HiringMode hiringMode) {
            this.hiringMode = hiringMode;
            Network.getNetwork().sendToServer(new BuildingHiringModeMessage(this, hiringMode));
        }

        @Override
        public String getJobName() {
            return this.jobName;
        }

        @Override
        public String getJobDisplayName() {
            return jobDisplayName;
        }
    }
}
