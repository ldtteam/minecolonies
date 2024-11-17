package com.minecolonies.core.colony.jobs;

import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.entity.ai.workers.production.herders.EntityAIWorkSwineHerder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.StatisticsConstants.ITEM_USED;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.STATS_MODULE;

/**
 * The SwineHerder job
 */
public class JobSwineHerder extends AbstractJob<EntityAIWorkSwineHerder, JobSwineHerder>
{
    /**
     * Instantiates the placeholder job.
     *
     * @param entity the entity.
     */
    public JobSwineHerder(final ICitizenData entity)
    {
        super(entity);
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @Nullable
    @Override
    public EntityAIWorkSwineHerder generateAI()
    {
        return new EntityAIWorkSwineHerder(this);
    }

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.PIG_FARMER_ID;
    }

    @Override
    public double getDiseaseModifier()
    {
        return 2;
    }

    @Override
    public boolean onStackPickUp(@NotNull final ItemStack pickedUpStack)
    {
        if (getCitizen().getWorkBuilding() != null && getCitizen().getEntity().isPresent() && getCitizen().getWorkBuilding()
          .isInBuilding(getCitizen().getEntity().get().blockPosition()))
        {
            getCitizen().getWorkBuilding().getModule(STATS_MODULE).incrementBy(ITEM_USED + ";" + pickedUpStack.getItem().getDescriptionId(), pickedUpStack.getCount());
        }

        return super.onStackPickUp(pickedUpStack);
    }
}
