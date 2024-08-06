package com.minecolonies.core.colony;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.colony.jobs.IJobView;
import com.minecolonies.api.colony.jobs.registry.IJobDataManager;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenHappinessHandler;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenSkillHandler;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.core.colony.interactionhandling.ServerCitizenInteraction;
import com.minecolonies.core.entity.citizen.citizenhandlers.CitizenHappinessHandler;
import com.minecolonies.core.entity.citizen.citizenhandlers.CitizenSkillHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_OFFHAND_HELD_ITEM_SLOT;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_CITIZEN_UNEMPLOYED;

/**
 * The CitizenDataView is the client-side representation of a CitizenData. Views contain the CitizenData's data that is relevant to a Client, in a more client-friendly form.
 * Mutable operations on a View result in a message to the server to perform the operation.
 */
public class CitizenDataView implements ICitizenDataView
{
    private static final String TAG_HELD_ITEM_SLOT = "HeldItemSlot";

    /**
     * The resource location for the blocking overlay.
     */
    private static final ResourceLocation BLOCKING_RESOURCE = new ResourceLocation(Constants.MOD_ID, "textures/icons/blocking.png");

    /**
     * The resource location for the pending overlay.
     */
    private static final ResourceLocation PENDING_RESOURCE = new ResourceLocation(Constants.MOD_ID, "textures/icons/warning.png");

    /**
     * Attributes.
     */
    private final int     id;
    private final IColonyView colonyView;
    protected     int     entityId;
    protected     String  name;
    protected     boolean female;
    protected     boolean paused;
    protected     boolean isChild;

    private IJobView jobView;

    /**
     * colony id of the citizen.
     */
    protected int colonyId;

    /**
     * Placeholder skills.
     */
    private double saturation;

    /**
     * holds the current citizen happiness value
     */
    private double happiness;

    /**
     * The position of the guard.
     */
    private BlockPos position;

    /**
     * Job identifier.
     */
    private String job;

    /**
     * Working and home position.
     */
    @Nullable
    private BlockPos homeBuilding;
    @Nullable
    private BlockPos workBuilding;

    private InventoryCitizen inventory;

    /**
     * The citizen chat options on the server side.
     */
    private final Map<Component, IInteractionResponseHandler> citizenChatOptions = new LinkedHashMap<>();

    /**
     * List of primary interactions (sorted by priority).
     */
    private List<IInteractionResponseHandler> sortedInteractions;

    /**
     * The citizen skill handler on the client side.
     */
    private final CitizenSkillHandler citizenSkillHandler;

    /**
     * The citizen happiness handler.
     */
    private final CitizenHappinessHandler citizenHappinessHandler;

    /**
     * The citizens status icon
     */
    private VisibleCitizenStatus statusIcon;

    /**
     * The current location of interest.
     */
    @Nullable private BlockPos statusPosition;

    /**
     * Parents of the citizen.
     */
    private Tuple<String, String> parents = new Tuple<>("", "");

    /**
     * Alive children of the citizen
     */
    private List<Integer> children = new ArrayList<>();

    /**
     * Alive siblings of the citizen.
     */
    private List<Integer> siblings = new ArrayList<>();

    /**
     * Alive partner of the citizen.
     */
    private Integer partner;

    /**
     * The list of available quests the citizen can give out.
     */
    private final List<ResourceLocation> availableQuests = new ArrayList<>();

    /**
     * The list of participating quests the citizen can give out.
     */
    private final List<ResourceLocation> participatingQuests = new ArrayList<>();

    /**
     * Texture UUID.
     */
    protected UUID textureUUID;

    /**
     * Set View id.
     *
     * @param id the id to set.
     */
    protected CitizenDataView(final int id, final IColonyView colonyView)
    {
        this.id = id;
        this.citizenSkillHandler = new CitizenSkillHandler();
        this.citizenHappinessHandler = new CitizenHappinessHandler();
        this.colonyView = colonyView;
    }

    @Override
    public int getId()
    {
        return id;
    }

    @Override
    public int getEntityId()
    {
        return entityId;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean isFemale()
    {
        return female;
    }

    @Override
    public boolean isPaused()
    {
        return paused;
    }

    @Override
    public IColony getColony()
    {
        return colonyView;
    }

    @Override
    public boolean isChild()
    {
        return isChild;
    }

    /**
     * DEPRECATED
     */
    @Override
    public void setPaused(final boolean p)
    {
        this.paused = p;
    }

    @Override
    public String getJob()
    {
        return job;
    }

    @Override
    public MutableComponent getJobComponent()
    {
        return job.isEmpty() ? Component.translatableEscape(COM_MINECOLONIES_COREMOD_GUI_TOWNHALL_CITIZEN_UNEMPLOYED) : Component.translatableEscape(job);
    }

    @Override
    @Nullable
    public BlockPos getHomeBuilding()
    {
        return homeBuilding;
    }

    @Override
    @Nullable
    public BlockPos getWorkBuilding()
    {
        return workBuilding;
    }

    @Override
    public void setHomeBuilding(final BlockPos homeBuilding)
    {
        this.homeBuilding = homeBuilding;
    }

    @Override
    public void setWorkBuilding(@Nullable final BlockPos bp)
    {
        this.workBuilding = bp;
    }

    @Override
    public int getColonyId()
    {
        return colonyId;
    }

    @Override
    public double getHappiness()
    {
        return happiness;
    }

    @Override
    public double getSaturation()
    {
        return saturation;
    }

    @Override
    public double getHealth()
    {
        final Entity entity = colonyView.getWorld().getEntity(entityId);

        if (entity instanceof LivingEntity)
        {
            return ((LivingEntity) entity).getHealth();
        }

        return CitizenData.MAX_HEALTH;
    }

    @Override
    public double getMaxHealth()
    {
        final Entity entity = colonyView.getWorld().getEntity(entityId);

        if (entity instanceof LivingEntity)
        {
            return ((LivingEntity) entity).getMaxHealth();
        }

        return CitizenData.MAX_HEALTH;
    }

    @Override
    public BlockPos getPosition()
    {
        return position;
    }

    @Override
    public void deserialize(@NotNull final RegistryFriendlyByteBuf buf)
    {
        name = buf.readUtf(32767);
        female = buf.readBoolean();
        entityId = buf.readInt();
        paused = buf.readBoolean();
        isChild = buf.readBoolean();

        homeBuilding = buf.readBoolean() ? buf.readBlockPos() : null;
        workBuilding = buf.readBoolean() ? buf.readBlockPos() : null;

        saturation = buf.readDouble();
        happiness = buf.readDouble();

        citizenSkillHandler.read(buf.readNbt());

        job = buf.readUtf(32767);

        colonyId = buf.readInt();

        final CompoundTag compound = buf.readNbt();
        inventory = new InventoryCitizen(this.name, true);
        this.inventory.read(compound);
        this.inventory.setHeldItem(InteractionHand.MAIN_HAND, compound.getInt(TAG_HELD_ITEM_SLOT));
        this.inventory.setHeldItem(InteractionHand.OFF_HAND, compound.getInt(TAG_OFFHAND_HELD_ITEM_SLOT));

        position = buf.readBlockPos();

        citizenChatOptions.clear();
        final int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            final CompoundTag compoundNBT = buf.readNbt();
            final ServerCitizenInteraction handler =
              (ServerCitizenInteraction) MinecoloniesAPIProxy.getInstance().getInteractionResponseHandlerDataManager().createFrom(this, compoundNBT);
            citizenChatOptions.put(handler.getInquiry(), handler);
        }

        sortedInteractions = new ArrayList<>(citizenChatOptions.values());
        sortedInteractions.sort(Comparator.comparingInt(e -> -e.getPriority().getPriority()));

        citizenHappinessHandler.read(buf.readNbt());

        int statusindex = buf.readInt();
        statusIcon = statusindex >= 0 ? VisibleCitizenStatus.getForId(statusindex) : null;
        statusPosition = buf.readBoolean() ? buf.readBlockPos() : null;

        if (buf.readBoolean())
        {
            final IColonyView colonyView = IColonyManager.getInstance().getColonyView(colonyId, Minecraft.getInstance().level.dimension());
            jobView = IJobDataManager.getInstance().createViewFrom(colonyView, this, buf);
        }
        else
        {
            jobView = null;
        }

        children.clear();
        siblings.clear();

        partner = buf.readInt();
        final int siblingsSize = buf.readInt();
        for (int i = 0; i < siblingsSize; i++)
        {
            siblings.add(buf.readInt());
        }

        final int childrenSize = buf.readInt();
        for (int i = 0; i < childrenSize; i++)
        {
            children.add(buf.readInt());
        }

        final String parentA = buf.readUtf();
        final String parentB = buf.readUtf();
        parents = new Tuple<>(parentA, parentB);

        availableQuests.clear();
        participatingQuests.clear();

        final int avSize = buf.readInt();
        for (int i = 0; i < avSize; i++)
        {
            availableQuests.add(buf.readResourceLocation());
        }

        final int partSize = buf.readInt();
        for (int i = 0; i < partSize; i++)
        {
            participatingQuests.add(buf.readResourceLocation());
        }

        if (buf.readBoolean())
        {
            textureUUID = buf.readUUID();
        }
    }

    @Override
    public IJobView getJobView()
    {
        return this.jobView;
    }

    @Override
    public InventoryCitizen getInventory()
    {
        return inventory;
    }

    @Override
    public List<IInteractionResponseHandler> getOrderedInteractions()
    {
        return sortedInteractions;
    }

    @Override
    @Nullable
    public IInteractionResponseHandler getSpecificInteraction(@NotNull final Component component)
    {
        return citizenChatOptions.getOrDefault(component, null);
    }

    @Override
    public boolean hasBlockingInteractions()
    {
        if (sortedInteractions.isEmpty())
        {
            return false;
        }

        for (final IInteractionResponseHandler interaction : sortedInteractions)
        {
            if (interaction.getPriority().getPriority() >= ChatPriority.IMPORTANT.getPriority())
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasVisibleInteractions()
    {
        if (sortedInteractions.isEmpty())
        {
            return false;
        }

        for (final IInteractionResponseHandler interaction : sortedInteractions)
        {
            if (interaction.getPriority().getPriority() >= ChatPriority.CHITCHAT.getPriority())
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasPendingInteractions()
    {
        if (sortedInteractions.isEmpty())
        {
            return false;
        }

        for (final IInteractionResponseHandler interaction : sortedInteractions)
        {
            if (interaction.isPrimary())
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public ICitizenSkillHandler getCitizenSkillHandler()
    {
        return citizenSkillHandler;
    }

    @Override
    public ICitizenHappinessHandler getHappinessHandler()
    {
        return citizenHappinessHandler;
    }

    @Override
    public ResourceLocation getInteractionIcon()
    {
        if (sortedInteractions == null || sortedInteractions.isEmpty())
        {
            return null;
        }

        ResourceLocation icon = sortedInteractions.get(0).getInteractionIcon();
        if (icon == null)
        {
            if (hasBlockingInteractions())
            {
                icon = BLOCKING_RESOURCE;
            }
            else if (hasVisibleInteractions())
            {
                icon = PENDING_RESOURCE;
            }
        }

        return icon;
    }

    @Override
    public VisibleCitizenStatus getVisibleStatus()
    {
        return statusIcon;
    }

    @Override
    public @Nullable BlockPos getStatusPosition()
    {
        return statusPosition;
    }

    @Nullable
    @Override
    public Integer getPartner()
    {
        return partner;
    }

    @Override
    public List<Integer> getChildren()
    {
        return new ArrayList<>(children);
    }

    @Override
    public List<Integer> getSiblings()
    {
        return new ArrayList<>(siblings);
    }

    @Override
    public Tuple<String, String> getParents()
    {
        return parents;
    }

    @Override
    public ResourceLocation getCustomTexture()
    {
        return null;
    }

    @Override
    public UUID getCustomTextureUUID()
    {
        return textureUUID;
    }

    @Override
    public void setJobView(final IJobView jobView)
    {
        this.jobView = jobView;
    }

    @Override
    public int hashCode()
    {
        return id;
    }

    @SuppressWarnings(Suppression.TOO_MANY_RETURNS)
    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final ICitizenDataView data = (ICitizenDataView) o;

        return id == data.getId();
    }
}
