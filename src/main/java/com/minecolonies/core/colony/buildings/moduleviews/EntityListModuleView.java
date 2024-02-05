package com.minecolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.buildings.modules.IEntityListModuleView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.modules.EntityListModuleWindow;
import com.minecolonies.core.network.messages.server.colony.building.AssignFilterableEntityMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Client side version of the abstract class for all buildings which require a filterable list of allowed entities.
 */
public class EntityListModuleView extends AbstractBuildingModuleView implements IEntityListModuleView
{
    /**
     * The list of entities.
     */
    private final List<ResourceLocation> listOfEntities = new ArrayList<>();

    /**
     * Unique string id of the module.
     */
    private final String id;

    /**
     * if the list is inverted (so list encludes the disabled ones).
     */
    private final boolean inverted;

    /**
     * Lang string for description.
     */
    private final String desc;

    /**
     * Create a nw grouped entity list view for the client side.
     * @param id the id.
     * @param desc desc lang string.
     * @param inverted enabling or disabling.
     */
    public EntityListModuleView(final String id, final String desc, final boolean inverted)
    {
        super();
        this.id = id;
        this.desc = desc;
        this.inverted = inverted;
    }

    @Override
    public void addEntity(final ResourceLocation entity)
    {
        Network.getNetwork().sendToServer(new AssignFilterableEntityMessage(this.buildingView, getProducer().getRuntimeID(), entity, true));
        listOfEntities.add(entity);
    }

    @Override
    public boolean isAllowedEntity(final ResourceLocation entity)
    {
        return listOfEntities.contains(entity);
    }

    @Override
    public int getSize()
    {
        return listOfEntities.size();
    }

    @Override
    public void removeEntity(final ResourceLocation entity)
    {
        Network.getNetwork().sendToServer(new AssignFilterableEntityMessage(this.buildingView, getProducer().getRuntimeID(), entity, false));
        listOfEntities.remove(entity);
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public boolean isInverted()
    {
        return inverted;
    }

    @Override
    public void clearEntities() { listOfEntities.clear(); }

    @Override
    public String getDesc()
    {
        return desc;
    }

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        listOfEntities.clear();
        final int size = buf.readInt();

        for (int j = 0; j < size; j++)
        {
            listOfEntities.add(ForgeRegistries.ENTITY_TYPES.getKey(buf.readRegistryIdUnsafe(ForgeRegistries.ENTITY_TYPES)));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BOWindow getWindow()
    {
        return new EntityListModuleWindow(Constants.MOD_ID + ":gui/layouthuts/layoutfilterableentitylist.xml", buildingView, this);
    }

    @Override
    public String getIcon()
    {
        return "workers";
    }
}
