package com.minecolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.buildings.modules.IEntityListModuleView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.modules.building.EntityListModuleWindow;
import com.minecolonies.core.network.messages.server.colony.building.AssignFilterableEntityMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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
    private final List<Identifier> listOfEntities = new ArrayList<>();

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
    private final Component desc;

    /**
     * Create a nw grouped entity list view for the client side.
     * @param id the id.
     * @param desc desc lang string.
     * @param inverted enabling or disabling.
     */
    public EntityListModuleView(final String id, final Component desc, final boolean inverted)
    {
        super();
        this.id = id;
        this.desc = desc;
        this.inverted = inverted;
    }

    @Override
    public void addEntity(final Identifier entity)
    {
        new AssignFilterableEntityMessage(this.buildingView, getProducer().getRuntimeID(), entity, true).sendToServer();
        listOfEntities.add(entity);
    }

    @Override
    public boolean isAllowedEntity(final Identifier entity)
    {
        return listOfEntities.contains(entity);
    }

    @Override
    public int getSize()
    {
        return listOfEntities.size();
    }

    @Override
    public void removeEntity(final Identifier entity)
    {
        new AssignFilterableEntityMessage(this.buildingView, getProducer().getRuntimeID(), entity, false).sendToServer();
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
    public Component getDesc()
    {
        return desc;
    }

    @Override
    public void deserialize(@NotNull final RegistryFriendlyByteBuf buf)
    {
        listOfEntities.clear();
        final int size = buf.readInt();

        for (int j = 0; j < size; j++)
        {
            listOfEntities.add(buf.readIdentifier());
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BOWindow getWindow()
    {
        return new EntityListModuleWindow(this);
    }

    @Override
    public Identifier getIconIdentifier()
    {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/modules/workers.png");
    }
}
