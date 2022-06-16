package com.minecolonies.coremod.colony;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.registry.IJobDataManager;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.citizen.AbstractCivilianEntity;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenSkillHandler;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.modules.LivingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.coremod.colony.interactionhandling.ServerCitizenInteraction;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.citizen.citizenhandlers.CitizenHappinessHandler;
import com.minecolonies.coremod.entity.citizen.citizenhandlers.CitizenMournHandler;
import com.minecolonies.coremod.entity.citizen.citizenhandlers.CitizenSkillHandler;
import com.minecolonies.coremod.util.AttributeModifierUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.*;
import static com.minecolonies.api.research.util.ResearchConstants.HEALTH_BOOST;
import static com.minecolonies.api.research.util.ResearchConstants.WALKING;
import static com.minecolonies.api.util.ItemStackUtils.CAN_EAT;
import static com.minecolonies.api.util.constant.BuildingConstants.TAG_ACTIVE;
import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Extra data for Citizens.
 */
@SuppressWarnings({Suppression.BIG_CLASS, "PMD.ExcessiveClassLength"})
public class CitizenData implements ICitizenData
{
    /**
     * The max health.
     */
    private static final float MAX_HEALTH = 20.0F;

    /**
     * Max levels of an attribute a citizen may initially have.
     */
    private static final int LETTERS_IN_THE_ALPHABET = 26;

    /**
     * Minimum saturation of a citizen.
     */
    private static final int MIN_SATURATION = 0;

    /**
     * Possible texture suffixes.
     */
    public static final List<String> SUFFIXES = Arrays.asList("_b", "_d", "_a", "_w");

    /**
     * The unique citizen id.
     */
    private final int id;

    /**
     * The colony the citizen belongs to.
     */
    private final IColony colony;

    /**
     * Inventory of the citizen.
     */
    protected InventoryCitizen inventory;

    /**
     * The name of the citizen.
     */
    private String name;

    /**
     * Boolean gender, true = female, false = male.
     */
    private boolean female;

    /**
     * Whether the citizen is still a child
     */
    private boolean isChild = false;

    /**
     * Boolean paused, true = paused, false = working.
     */
    private boolean paused;

    /**
     * If restart is scheduled.
     */
    private boolean restartScheduled;

    /**
     * Report end message to:
     */
    private ServerPlayer originPlayerRestart;

    /**
     * The id of the citizens texture.
     */
    private int textureId;

    /**
     * If the citizen is asleep right now.
     */
    private boolean isAsleep;

    /**
     * The citizens current bedBos.
     */
    private BlockPos bedPos = BlockPos.ZERO;

    /**
     * The home building of the citizen.
     */
    @Nullable
    private IBuilding homeBuilding;

    /**
     * The work building of the citizen.
     */
    @Nullable
    private IBuilding workBuilding;

    /**
     * The job of the citizen.
     */
    private IJob<?> job;

    /**
     * If the citizen is dirty (Has to be updated on client side).
     */
    private boolean dirty;

    /**
     * Its entitity.
     */
    @NotNull
    private WeakReference<AbstractEntityCitizen> entity = new WeakReference<>(null);

    /**
     * The citizens saturation at the current moment.
     */
    private double saturation;

    /**
     * Variable indicating if a citizen just ate.
     */
    private boolean justAte;

    /**
     * The last position of the citizen.
     */
    private BlockPos lastPosition = new BlockPos(0, 0, 0);

    /**
     * The citizen happiness handler.
     */
    private final CitizenHappinessHandler citizenHappinessHandler;

    /**
     * The citizen happiness handler.
     */
    private final CitizenMournHandler citizenMournHandler;

    /**
     * The citizen skill handler.
     */
    private final CitizenSkillHandler citizenSkillHandler;

    /**
     * The citizen chat options on the server side.
     */
    protected final Map<Component, IInteractionResponseHandler> citizenChatOptions = new HashMap<>();

    /**
     * If idle at job.
     */
    private boolean idle;

    /**
     * The texture suffix.
     */
    private String textureSuffix;

    /**
     * The status icon to display
     */
    private VisibleCitizenStatus status;

    /**
     * The citizen data random.
     */
    private Random random = new Random();

    /**
     * Chance to complain for having no guard nearby
     */
    private static final int NO_GUARD_COMPLAIN_CHANCE = 10;

    /**
     * Consumed position to determine the next position to respawn at.
     */
    private BlockPos nextRespawnPos = null;

    /**
     * Parents of the citizen.
     */
    private Tuple<String, String> parents = new Tuple<>("", "");

    /**
     * Alive children of the citizen
     */
    private Set<Integer> children = new HashSet<>();

    /**
     * Alive siblings of the citizen.
     */
    private Set<Integer> siblings = new HashSet<>();

    /**
     * Alive partner of the citizen.
     */
    private Integer partner = 0;

    /**
     * If the job is currently active.
     */
    private boolean isWorking = false;

    /**
     * The inactivity timer in seconds.
     */
    private int inactivityTimer = DISABLED;

    /**
     * Create a CitizenData given an ID. Used as a super-constructor or during loading.
     *
     * @param id     ID of the Citizen.
     * @param colony Colony the Citizen belongs to.
     */
    public CitizenData(final int id, final IColony colony)
    {
        this.id = id;
        this.colony = colony;
        inventory = new InventoryCitizen("Minecolonies Inventory", true, this);
        this.citizenHappinessHandler = new CitizenHappinessHandler(this);
        this.citizenMournHandler = new CitizenMournHandler(this);
        this.citizenSkillHandler = new CitizenSkillHandler();
    }

    @Override
    public void onResponseTriggered(@NotNull final Component key, @NotNull final Component response, final Player player)
    {
        if (citizenChatOptions.containsKey(key))
        {
            citizenChatOptions.get(key).onServerResponseTriggered(response, player, this);
            markDirty();
        }
    }

    /**
     * Return the entity instance of the citizen data. Respawn the citizen if needed.
     *
     * @return {@link AbstractEntityCitizen} of the citizen data.
     */
    @Override
    @NotNull
    public Optional<AbstractEntityCitizen> getEntity()
    {
        final AbstractEntityCitizen citizen = entity.get();
        return Optional.ofNullable(citizen);
    }

    @Override
    public void setEntity(@Nullable final AbstractCivilianEntity citizen)
    {
        if (entity.get() != null)
        {
            entity.clear();
        }

        if (citizen != null)
        {
            entity = new WeakReference<>((AbstractEntityCitizen) citizen);
            citizen.setCivilianData(this);
        }
    }

    @Override
    public void markDirty()
    {
        dirty = true;
        colony.getCitizenManager().markDirty();
    }

    /**
     * Returns a random element in a list.
     *
     * @param rand  Random object.
     * @param array Array to select from.
     * @return Random element from array.
     */
    private static String getRandomElement(@NotNull final Random rand, @NotNull final String[] array)
    {
        return array[rand.nextInt(array.length)];
    }

    /**
     * Returns a random capital letter from the alphabet.
     *
     * @param rand Random object.
     * @return Random capital letter.
     */
    private static char getRandomLetter(@NotNull final Random rand)
    {
        return (char) (rand.nextInt(LETTERS_IN_THE_ALPHABET) + 'A');
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

        final CitizenData data = (CitizenData) o;

        if (id != data.id)
        {
            return false;
        }

        return colony != null ? (data.colony != null && colony.getID() == data.colony.getID()) : (data.colony == null);
    }

    @Override
    public IColony getColony()
    {
        return colony;
    }

    @Override
    public int getId()
    {
        return id;
    }

    @Override
    public void initForNewCivilian()
    {
        //Assign the gender before name
        female = random.nextBoolean();
        textureSuffix = SUFFIXES.get(random.nextInt(SUFFIXES.size()));
        paused = false;
        name = generateName(random, female, getColony());
        textureId = random.nextInt(255);

        saturation = MAX_SATURATION;
        final int levelCap = (int) colony.getOverallHappiness();

        citizenSkillHandler.init(levelCap);

        markDirty();
    }

    /**
     * Initializes the entities values from citizen data.
     */
    @Override
    public void initEntityValues()
    {
        if (!getEntity().isPresent())
        {
            return;
        }

        final AbstractEntityCitizen citizen = getEntity().get();

        citizen.setCitizenId(getId());
        citizen.getCitizenColonyHandler().setColonyId(getColony().getID());

        citizen.setIsChild(isChild());
        citizen.setCustomName(Component.literal(getName()));

        citizen.getAttribute(Attributes.MAX_HEALTH).setBaseValue(BASE_MAX_HEALTH);

        citizen.setFemale(isFemale());
        citizen.setTextureId(getTextureId());

        citizen.getEntityData().set(DATA_COLONY_ID, colony.getID());
        citizen.getEntityData().set(DATA_CITIZEN_ID, citizen.getCivilianID());
        citizen.getEntityData().set(DATA_IS_FEMALE, citizen.isFemale() ? 1 : 0);
        citizen.getEntityData().set(DATA_TEXTURE, citizen.getTextureId());
        citizen.getEntityData().set(DATA_TEXTURE_SUFFIX, getTextureSuffix());
        citizen.getEntityData().set(DATA_IS_ASLEEP, isAsleep());
        citizen.getEntityData().set(DATA_IS_CHILD, isChild());
        citizen.getEntityData().set(DATA_BED_POS, getBedPos());
        citizen.getEntityData().set(DATA_JOB, getJob() == null ? "" : getJob().getJobRegistryEntry().getKey().toString());
        citizen.getEntityData().set(DATA_STYLE, colony.getTextureStyleId());

        citizen.getCitizenExperienceHandler().updateLevel();

        setLastPosition(citizen.blockPosition());

        citizen.getCitizenJobHandler().onJobChanged(citizen.getCitizenJobHandler().getColonyJob());

        applyResearchEffects();

        applyItemModifiers(citizen);

        markDirty();
    }

    /**
     * Upon loading/registering an entity we also apply item modifiers to it
     *
     * @param citizen
     */
    private void applyItemModifiers(AbstractEntityCitizen citizen)
    {
        for (final EquipmentSlot slot : EquipmentSlot.values())
        {
            final ItemStack stack = citizen.getItemBySlot(slot);
            if (!ItemStackUtils.isEmpty(stack))
            {
                citizen.getAttributes().addTransientAttributeModifiers(stack.getAttributeModifiers(slot));
            }
        }
    }

    /**
     * Generates a random name from a set of names.
     *
     * @param rand Random object.
     * @param female the gender
     * @param colony the colony.
     * @return Name of the citizen.
     */
    public static String generateName(@NotNull final Random rand, final boolean female, final IColony colony)
    {
        String citizenName;
        final String firstName;
        final String middleInitial;
        final String lastName;

        if (female)
        {
            firstName = getRandomElement(rand, MineColonies.getConfig().getServer().femaleFirstNames.get().toArray(new String[0]));
        }
        else
        {
            firstName = getRandomElement(rand, MineColonies.getConfig().getServer().maleFirstNames.get().toArray(new String[0]));
        }

        middleInitial = String.valueOf(getRandomLetter(rand));
        lastName = getRandomElement(rand, MineColonies.getConfig().getServer().lastNames.get().toArray(new String[0]));

        if(MineColonies.getConfig().getServer().useEasternNameOrder.get())
        {
            //For now, don't include middle names, as their rules (and presence) vary heavily by region.
            citizenName = String.format("%s %s", lastName, firstName);
        }
        else
        {
            if (MineColonies.getConfig().getServer().useMiddleInitial.get())
            {
                citizenName = String.format("%s %s. %s", firstName, middleInitial, lastName);
            }
            else
            {
                citizenName = String.format("%s %s", firstName, lastName);
            }
        }

        // Check whether there's already a citizen with this name
        for (final ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            if (citizen != null && citizen.getName().equals(citizenName))
            {
                // Oops - recurse this function and try again
                citizenName = generateName(rand, female, colony);
                break;
            }
        }

        return citizenName;
    }

    /**
     * Generates a random name considering both parent names.
     *
     * @param rand Random object.
     */
    public void generateName(@NotNull final Random rand, final String firstParentName, final String secondParentName)
    {
        String nameA = firstParentName;
        String nameB = secondParentName;

        String citizenName;
        final String firstName;
        final String middleInitial;
        final String lastName;

        if (firstParentName == null || firstParentName.isEmpty())
        {
            nameA = generateName(rand, rand.nextBoolean(), colony);
        }

        if (secondParentName == null || secondParentName.isEmpty())
        {
            nameB = generateName(rand, rand.nextBoolean(), colony);
        }

        final String[] firstParentNameSplit = nameA.split(" ");
        final String[] secondParentNameSplit = nameB.split(" ");

        int lastNameIndex = 1;
        if (MineColonies.getConfig().getServer().useEasternNameOrder.get())
        {
            lastNameIndex = 0;
        }
        else if (MineColonies.getConfig().getServer().useMiddleInitial.get())
        {
            lastNameIndex = 2;
        }

        if (random.nextBoolean())
        {
            middleInitial = firstParentNameSplit[lastNameIndex].substring(0, 1);
            lastName = secondParentNameSplit[lastNameIndex];
        }
        else
        {
            middleInitial = secondParentNameSplit[lastNameIndex].substring(0, 1);
            lastName = firstParentNameSplit[lastNameIndex];
        }

        if (female)
        {
            firstName = getRandomElement(rand, MineColonies.getConfig().getServer().femaleFirstNames.get().toArray(new String[0]));
        }
        else
        {
            firstName = getRandomElement(rand, MineColonies.getConfig().getServer().maleFirstNames.get().toArray(new String[0]));
        }

        if(MineColonies.getConfig().getServer().useEasternNameOrder.get())
        {
            //For now, don't include middle names, as their rules (and presence) vary heavily by region.
            citizenName = String.format("%s %s", lastName, firstName);
        }
        else
        {
            if (MineColonies.getConfig().getServer().useMiddleInitial.get())
            {
                citizenName = String.format("%s %s. %s", firstName, middleInitial, lastName);
            }
            else
            {
                citizenName = String.format("%s %s", firstName, lastName);
            }
        }

        // Check whether there's already a citizen with this name
        for (final ICitizenData citizen : this.getColony().getCitizenManager().getCitizens())
        {
            if (citizen != null && citizen.getName().equals(citizenName))
            {
                // Oops - recurse this function and try again
                generateName(rand, firstParentName, secondParentName);
                return;
            }
        }
        this.name = citizenName;
    }

    @Override
    public boolean isRelatedTo(final ICitizenData data)
    {
        return siblings.contains(data.getId()) || children.contains(data.getId()) || partner == data.getId() || parents.getA().equals(data.getName()) || parents.getB().equals(data.getName());
    }

    @Override
    public boolean doesLiveWith(final ICitizenData data)
    {
        return data.getHomeBuilding() != null && getHomeBuilding() != null && data.getHomeBuilding().getPosition().equals(getHomeBuilding().getPosition());
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
    public void setGenderAndGenerateName(final boolean isFemale)
    {
        this.female = isFemale;
        this.name = generateName(random, isFemale, getColony());
        markDirty();
    }

    @Override
    public void setGender(final boolean isFemale)
    {
        this.female = isFemale;
    }

    @Override
    public void setPaused(final boolean p)
    {
        this.paused = p;
        markDirty();
    }

    @Override
    public boolean isPaused()
    {
        return paused;
    }

    @Override
    public int getTextureId()
    {
        return textureId;
    }

    @Override
    public boolean isDirty()
    {
        return dirty;
    }

    @Override
    public void clearDirty()
    {
        dirty = false;
    }

    @Override
    public void onRemoveBuilding(final IBuilding building)
    {
        if (homeBuilding != null && homeBuilding.getID().equals(building.getID()))
        {
            setHomeBuilding(null);
        }

        if (workBuilding != null && workBuilding.getID().equals(building.getID()))
        {
            setWorkBuilding(null);
        }
    }

    @Override
    @Nullable
    public IBuilding getHomeBuilding()
    {
        return homeBuilding;
    }

    @Override
    public void setHomeBuilding(@Nullable final IBuilding building)
    {
        if (homeBuilding != null && building != null && !homeBuilding.equals(building))
        {
            homeBuilding.getFirstOptionalModuleOccurance(LivingBuildingModule.class).ifPresent(b -> b.removeCitizen(this));
        }

        homeBuilding = building;
        markDirty();

        if (getEntity().isPresent() && getEntity().get().getCitizenJobHandler().getColonyJob() == null)
        {
            getEntity().get().getCitizenJobHandler().setModelDependingOnJob(null);
        }

        setBedPos(BlockPos.ZERO);
    }

    @Override
    @Nullable
    public IBuilding getWorkBuilding()
    {
        if (job == null && workBuilding != null)
        {
            setWorkBuilding(null);
        }
        return workBuilding;
    }

    @Override
    public void setWorkBuilding(@Nullable final IBuilding building)
    {
        if (workBuilding != null && building != null && workBuilding != building)
        {
            Log.getLogger().warn("CitizenData.setWorkBuilding() - already assigned a work building when setting a new work building");
        }
        else if (workBuilding != building)
        {
            workBuilding = building;

            if (workBuilding != null)
            {
                //  We have a place to work, do we have the assigned Job?
                if (job == null)
                {
                    // If this is null, something is very wrong!
                    final WorkerBuildingModule module = building.getModuleMatching(WorkerBuildingModule.class, m -> m.getAssignedCitizen().contains(this));
                    //  No job, create one!
                    setJob(module.createJob(this));
                    colony.getWorkManager().clearWorkForCitizen(this);
                }
            }
            else if (job != null)
            {
                getEntity().ifPresent(entityCitizen -> {
                    entityCitizen.getTasks()
                      .availableGoals.stream()
                      .filter(task -> task.getGoal() instanceof AbstractAISkeleton)
                      .findFirst()
                      .ifPresent(e -> entityCitizen.getTasks().removeGoal(e));
                });

                //  No place of employment, get rid of our job
                setJob(null);
                colony.getWorkManager().clearWorkForCitizen(this);
            }

            markDirty();
        }
    }

    @Override
    public void updateEntityIfNecessary()
    {
        if (getEntity().isPresent())
        {
            final Entity entity = getEntity().get();
            if (entity.isAlive() && WorldUtil.isEntityBlockLoaded(entity.level, entity.blockPosition()))
            {
                return;
            }
        }

        if (nextRespawnPos != null)
        {
            colony.getCitizenManager().spawnOrCreateCivilian(this, colony.getWorld(), nextRespawnPos, true);
            nextRespawnPos = null;
        }
        else
        {
            colony.getCitizenManager().spawnOrCreateCivilian(this, colony.getWorld(), lastPosition, true);
        }
    }

    @Override
    public IJob<?> getJob()
    {
        return job;
    }

    @Override
    public void setJob(final IJob<?> job)
    {
        if (this.job != null && job == null)
        {
            this.job.onRemoval();
        }
        this.job = job;

        getEntity().ifPresent(entityCitizen -> entityCitizen.getCitizenJobHandler().onJobChanged(job));

        markDirty();
    }

    @Override
    @Nullable
    public <J extends IJob<?>> J getJob(@NotNull final Class<J> type)
    {
        if (type.isInstance(job))
        {
            return type.cast(job);
        }

        return null;
    }

    @Override
    public void serializeViewNetworkData(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeUtf(name);
        buf.writeBoolean(female);

        buf.writeInt(getEntity().map(AbstractEntityCitizen::getId).orElse(-1));

        buf.writeBoolean(paused);

        buf.writeBoolean(isChild);

        buf.writeBoolean(homeBuilding != null);
        if (homeBuilding != null)
        {
            buf.writeBlockPos(homeBuilding.getID());
        }

        buf.writeBoolean(workBuilding != null);
        if (workBuilding != null)
        {
            buf.writeBlockPos(workBuilding.getID());
        }

        // If the entity is not present we assumes standard values.
        buf.writeFloat(getEntity().map(AbstractEntityCitizen::getHealth).orElse(MAX_HEALTH));
        buf.writeFloat(getEntity().map(AbstractEntityCitizen::getMaxHealth).orElse(MAX_HEALTH));

        buf.writeDouble(getSaturation());
        buf.writeDouble(citizenHappinessHandler.getHappiness(getColony()));

        buf.writeNbt(citizenSkillHandler.write());

        buf.writeUtf((job != null) ? job.getJobRegistryEntry().getTranslationKey() : "");

        buf.writeInt(colony.getID());

        final CompoundTag compound = new CompoundTag();
        compound.put("inventory", inventory.write(new ListTag()));
        buf.writeNbt(compound);
        buf.writeBlockPos(lastPosition);

        if (colony.getWorld() != null)
        {
            final List<IInteractionResponseHandler> subInteractions = citizenChatOptions.values().stream().filter(e -> e.isVisible(colony.getWorld())).collect(Collectors.toList());

            buf.writeInt(subInteractions.size());
            for (final IInteractionResponseHandler interactionHandler : subInteractions)
            {
                buf.writeNbt(interactionHandler.serializeNBT());
            }
        }
        else
        {
            buf.writeInt(0);
        }

        final CompoundTag happinessCompound = new CompoundTag();
        citizenHappinessHandler.write(happinessCompound);
        buf.writeNbt(happinessCompound);

        buf.writeInt(status != null ? status.getId() : -1);

        buf.writeBoolean(job != null);
        if (job != null)
        {
            job.serializeToView(buf);
        }

        if (colony.getCitizenManager().getCivilian(partner) == null)
        {
            partner = 0;
        }

        siblings.removeIf(s -> colony.getCitizenManager().getCivilian(s) == null);
        children.removeIf(c -> colony.getCitizenManager().getCivilian(c) == null);

        buf.writeInt(partner);
        buf.writeInt(siblings.size());
        for (int sibling : siblings)
        {
            buf.writeInt(sibling);
        }
        buf.writeInt(children.size());
        for (int child : children)
        {
            buf.writeInt(child);
        }
        buf.writeUtf(parents.getA());
        buf.writeUtf(parents.getB());
    }

    @Override
    public void increaseSaturation(final double extraSaturation)
    {
        this.saturation = Math.min(MAX_SATURATION, this.saturation + Math.abs(extraSaturation));
    }

    @Override
    public void decreaseSaturation(final double extraSaturation)
    {
        if (colony != null && colony.isActive())
        {
            this.saturation = Math.max(MIN_SATURATION, this.saturation - Math.abs(extraSaturation * MineColonies.getConfig().getServer().foodModifier.get()));
            this.justAte = false;
        }
    }

    @Override
    public void setName(final String name)
    {
        this.name = name;
    }

    @Override
    public void setLastPosition(final BlockPos lastPosition)
    {
        this.lastPosition = lastPosition;
    }

    @Override
    public BlockPos getLastPosition()
    {
        return lastPosition;
    }

    @Override
    public double getSaturation()
    {
        return this.saturation;
    }

    @Override
    public void setSaturation(final double saturation)
    {
        this.saturation = saturation;
    }

    @Override
    public InventoryCitizen getInventory()
    {
        return inventory;
    }

    @Override
    public boolean isAsleep()
    {
        return isAsleep;
    }

    @Override
    public BlockPos getBedPos()
    {
        return bedPos;
    }

    @Override
    public void setAsleep(final boolean asleep)
    {
        isAsleep = asleep;
    }

    @Override
    public void setBedPos(final BlockPos bedPos)
    {
        this.bedPos = bedPos;
    }

    @Override
    public CitizenHappinessHandler getCitizenHappinessHandler()
    {
        return citizenHappinessHandler;
    }

    @Override
    public CitizenMournHandler getCitizenMournHandler()
    {
        return citizenMournHandler;
    }

    @Override
    public ICitizenSkillHandler getCitizenSkillHandler()
    {
        return citizenSkillHandler;
    }

    @Override
    public void scheduleRestart(final ServerPlayer player)
    {
        originPlayerRestart = player;
        restartScheduled = true;
    }

    @Override
    public boolean shouldRestart()
    {
        return restartScheduled;
    }

    @Override
    public void restartDone()
    {
        restartScheduled = false;
        MessageUtils.format(MESSAGE_CITIZEN_RESTARTED, getName()).sendTo(originPlayerRestart);
    }

    @Override
    public void setIsChild(final boolean isChild)
    {
        this.isChild = isChild;
        markDirty();

        if (colony != null)
        {
            colony.updateHasChilds();
        }
    }

    @Override
    public boolean isChild()
    {
        return isChild;
    }

    @Override
    public boolean justAte()
    {
        return this.justAte;
    }

    @Override
    public void setJustAte(final boolean justAte)
    {
        this.justAte = justAte;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag nbtTagCompound = new CompoundTag();

        nbtTagCompound.putInt(TAG_ID, id);
        nbtTagCompound.putString(TAG_NAME, name);
        nbtTagCompound.putString(TAG_SUFFIX, textureSuffix);

        nbtTagCompound.putBoolean(TAG_FEMALE, female);
        nbtTagCompound.putBoolean(TAG_PAUSED, paused);
        nbtTagCompound.putBoolean(TAG_CHILD, isChild);
        nbtTagCompound.putInt(TAG_TEXTURE, textureId);

        nbtTagCompound.put(TAG_NEW_SKILLS, citizenSkillHandler.write());

        BlockPosUtil.write(nbtTagCompound, TAG_POS, getEntity().isPresent() ? getEntity().get().blockPosition() : lastPosition);
        if (nextRespawnPos != null)
        {
            BlockPosUtil.write(nbtTagCompound, TAG_RESPAWN_POS, nextRespawnPos);
        }
        nbtTagCompound.putDouble(TAG_SATURATION, saturation);

        if (job != null)
        {
            @NotNull final Tag jobCompound = job.serializeNBT();
            nbtTagCompound.put("job", jobCompound);
        }

        citizenHappinessHandler.write(nbtTagCompound);
        citizenMournHandler.write(nbtTagCompound);

        nbtTagCompound.put(TAG_INVENTORY, inventory.write(new ListTag()));
        nbtTagCompound.putInt(TAG_HELD_ITEM_SLOT, inventory.getHeldItemSlot(InteractionHand.MAIN_HAND));
        nbtTagCompound.putInt(TAG_OFFHAND_HELD_ITEM_SLOT, inventory.getHeldItemSlot(InteractionHand.OFF_HAND));

        BlockPosUtil.write(nbtTagCompound, TAG_BEDS, bedPos);
        nbtTagCompound.putBoolean(TAG_ASLEEP, isAsleep);
        nbtTagCompound.putBoolean(TAG_JUST_ATE, justAte);

        @NotNull final ListTag chatTagList = new ListTag();
        for (@NotNull final IInteractionResponseHandler entry : citizenChatOptions.values())
        {
            @NotNull final CompoundTag chatOptionCompound = new CompoundTag();
            chatOptionCompound.put(TAG_CHAT_OPTION, entry.serializeNBT());
            chatTagList.add(chatOptionCompound);
        }

        nbtTagCompound.put(TAG_CHAT_OPTIONS, chatTagList);
        nbtTagCompound.putBoolean(TAG_IDLE, idle);

        nbtTagCompound.putString(TAG_PARENT_A, parents.getA());
        nbtTagCompound.putString(TAG_PARENT_B, parents.getB());

        @NotNull final ListTag siblingsNBT = new ListTag();
        for (final int sibling : siblings)
        {
            siblingsNBT.add(IntTag.valueOf(sibling));
        }
        nbtTagCompound.put(TAG_SIBLINGS, siblingsNBT);

        @NotNull final ListTag childrenNBT = new ListTag();
        for (final int child : children)
        {
            childrenNBT.add(IntTag.valueOf(child));
        }
        nbtTagCompound.put(TAG_CHILDREN, childrenNBT);
        nbtTagCompound.putInt(TAG_PARTNER, partner);
        nbtTagCompound.putBoolean(TAG_ACTIVE, this.isWorking);

        return nbtTagCompound;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbtTagCompound)
    {
        name = nbtTagCompound.getString(TAG_NAME);
        female = nbtTagCompound.getBoolean(TAG_FEMALE);
        paused = nbtTagCompound.getBoolean(TAG_PAUSED);
        isChild = nbtTagCompound.getBoolean(TAG_CHILD);
        textureId = nbtTagCompound.getInt(TAG_TEXTURE);

        if (nbtTagCompound.getAllKeys().contains(TAG_SUFFIX))
        {
            textureSuffix = nbtTagCompound.getString(TAG_SUFFIX);
        }
        else
        {
            textureSuffix = SUFFIXES.get(random.nextInt(SUFFIXES.size()));
        }

        lastPosition = BlockPosUtil.read(nbtTagCompound, TAG_POS);

        if (nbtTagCompound.contains(TAG_RESPAWN_POS))
        {
            nextRespawnPos = BlockPosUtil.read(nbtTagCompound, TAG_RESPAWN_POS);
        }

        citizenSkillHandler.read(nbtTagCompound.getCompound(TAG_NEW_SKILLS));

        saturation = nbtTagCompound.getDouble(TAG_SATURATION);

        if (nbtTagCompound.getAllKeys().contains("job"))
        {
            setJob(IJobDataManager.getInstance().createFrom(this, nbtTagCompound.getCompound("job")));
        }

        if (nbtTagCompound.getAllKeys().contains(TAG_INVENTORY))
        {
            final ListTag nbttaglist = nbtTagCompound.getList(TAG_INVENTORY, 10);
            this.inventory.read(nbttaglist);
            this.inventory.setHeldItem(InteractionHand.MAIN_HAND, nbtTagCompound.getInt(TAG_HELD_ITEM_SLOT));
            this.inventory.setHeldItem(InteractionHand.OFF_HAND, nbtTagCompound.getInt(TAG_OFFHAND_HELD_ITEM_SLOT));
        }

        if (name.isEmpty())
        {
            name = generateName(random, isFemale(), getColony());
        }

        if (nbtTagCompound.getAllKeys().contains(TAG_ASLEEP))
        {
            bedPos = BlockPosUtil.read(nbtTagCompound, TAG_BEDS);
            isAsleep = nbtTagCompound.getBoolean(TAG_ASLEEP);
        }

        if (nbtTagCompound.getAllKeys().contains(TAG_JUST_ATE))
        {
            justAte = nbtTagCompound.getBoolean(TAG_JUST_ATE);
        }

        //  Citizen chat options.
        if (nbtTagCompound.getAllKeys().contains(TAG_CHAT_OPTIONS))
        {
            final ListTag handlerTagList = nbtTagCompound.getList(TAG_CHAT_OPTIONS, Tag.TAG_COMPOUND);
            for (int i = 0; i < handlerTagList.size(); ++i)
            {
                final ServerCitizenInteraction handler =
                  (ServerCitizenInteraction) MinecoloniesAPIProxy.getInstance()
                                               .getInteractionResponseHandlerDataManager()
                                               .createFrom(this, handlerTagList.getCompound(i).getCompound(TAG_CHAT_OPTION));
                citizenChatOptions.put(handler.getInquiry(), handler);
            }
        }

        this.citizenHappinessHandler.read(nbtTagCompound);
        this.citizenMournHandler.read(nbtTagCompound);

        if (nbtTagCompound.getAllKeys().contains(TAG_LEVEL_MAP) && !nbtTagCompound.getAllKeys().contains(TAG_NEW_SKILLS))
        {
            citizenSkillHandler.init((int) citizenHappinessHandler.getHappiness(getColony()));
            final Map<String, Integer> levels = new HashMap<>();
            final ListTag levelTagList = nbtTagCompound.getList(TAG_LEVEL_MAP, Tag.TAG_COMPOUND);
            for (int i = 0; i < levelTagList.size(); ++i)
            {
                final CompoundTag levelExperienceAtJob = levelTagList.getCompound(i);
                final String jobName = levelExperienceAtJob.getString(TAG_NAME);
                final int level = Math.min(levelExperienceAtJob.getInt(TAG_LEVEL), MAX_CITIZEN_LEVEL);
                levels.put(jobName, level);
            }

            for (final Map.Entry<String, Integer> entry : levels.entrySet())
            {
                final Skill primary = Skill.values()[random.nextInt(Skill.values().length)];
                final Skill secondary = Skill.values()[random.nextInt(Skill.values().length)];

                citizenSkillHandler.incrementLevel(primary, entry.getValue() / 2);
                citizenSkillHandler.incrementLevel(secondary, entry.getValue() / 4);
            }
        }

        this.idle = nbtTagCompound.getBoolean(TAG_IDLE);

        final String parentA = nbtTagCompound.getString(TAG_PARENT_A);
        final String parentB = nbtTagCompound.getString(TAG_PARENT_B);

        this.parents = new Tuple<>(parentA, parentB);
        @NotNull final ListTag siblingsNBT = nbtTagCompound.getList(TAG_SIBLINGS, Tag.TAG_INT);
        for (int i = 0; i < siblingsNBT.size(); i++)
        {
            siblings.add(siblingsNBT.getInt(i));
        }

        @NotNull final ListTag childrenNBT = nbtTagCompound.getList(TAG_CHILDREN, Tag.TAG_INT);
        for (int i = 0; i < childrenNBT.size(); i++)
        {
            children.add(childrenNBT.getInt(i));
        }

        partner = nbtTagCompound.getInt(TAG_PARTNER);
        this.isWorking = nbtTagCompound.getBoolean(TAG_ACTIVE);
    }

    @Override
    public void tick()
    {
        if (!getEntity().isPresent() || !getEntity().get().isAlive())
        {
            return;
        }

        if (!isWorking && job != null && inactivityTimer != DISABLED && ++inactivityTimer >= job.getInactivityLimit())
        {
            job.triggerActivityChangeAction(this.isWorking);
            inactivityTimer = DISABLED;
        }

        final List<IInteractionResponseHandler> toRemove = new ArrayList<>();
        for (final IInteractionResponseHandler handler : citizenChatOptions.values())
        {
            try
            {
                if (!handler.isValid(this))
                {
                    toRemove.add(handler);
                }
            }
            catch (final Exception e)
            {
                Log.getLogger().warn("Error during validation of handler: " + handler.getInquiry(), e);
                // If anything goes wrong in checking validity, remove handler.
                toRemove.add(handler);
            }
        }

        if (!toRemove.isEmpty())
        {
            markDirty();
        }

        for (final IInteractionResponseHandler handler : toRemove)
        {
            citizenChatOptions.remove(handler.getInquiry());
            for (final Component comp : handler.getPossibleResponses())
            {
                if (citizenChatOptions.containsKey(handler.getResponseResult(comp)))
                {
                    citizenChatOptions.get(handler.getResponseResult(comp)).removeParent(handler.getInquiry());
                }
            }
        }
    }

    @Override
    public void triggerInteraction(@NotNull final IInteractionResponseHandler handler)
    {
        if (!this.citizenChatOptions.containsKey(handler.getInquiry()))
        {
            this.citizenChatOptions.put(handler.getInquiry(), handler);
            for (final IInteractionResponseHandler childHandler : handler.genChildInteractions())
            {
                this.citizenChatOptions.put(childHandler.getInquiry(), (ServerCitizenInteraction) childHandler);
            }
            markDirty();
        }
    }

    @Override
    public boolean isIdleAtJob()
    {
        return this.idle;
    }

    @Override
    public void setIdleAtJob(final boolean idle)
    {
        this.idle = idle;
    }

    @Override
    public String getTextureSuffix()
    {
        return this.textureSuffix;
    }

    @Override
    public void setSuffix(final String suffix)
    {
        this.textureSuffix = suffix;
    }

    // --------------------------- Request Handling --------------------------- //

    @Override
    public <R extends IRequestable> IToken<?> createRequest(@NotNull final R requested)
    {
        return getWorkBuilding().createRequest(this, requested, false);
    }

    @Override
    public <R extends IRequestable> IToken<?> createRequestAsync(@NotNull final R requested)
    {
        return getWorkBuilding().createRequest(this, requested, true);
    }

    @Override
    public void onRequestCancelled(@NotNull final IToken<?> token)
    {
        if (isRequestAsync(token))
        {
            job.getAsyncRequests().remove(token);
        }
    }

    @Override
    public boolean isRequestAsync(@NotNull final IToken<?> token)
    {
        if (job != null)
        {
            return job.getAsyncRequests().contains(token);
        }
        return false;
    }

    @Override
    public VisibleCitizenStatus getStatus()
    {
        return status;
    }

    @Override
    public void setVisibleStatus(final VisibleCitizenStatus status)
    {
        if (this.status != status)
        {
            markDirty();
        }
        this.status = status;
    }

    /**
     * Loads this citizen data from nbt
     *
     * @param colony colony to load for
     * @param nbt    nbt compound to read from
     * @return new CitizenData
     */
    public static CitizenData loadFromNBT(final IColony colony, final CompoundTag nbt)
    {
        final CitizenData data = new CitizenData(nbt.getInt(TAG_ID), colony);
        data.deserializeNBT(nbt);
        return data;
    }

    @Override
    public Random getRandom()
    {
        return random;
    }

    @Override
    public void applyResearchEffects()
    {
        if (getEntity().isPresent())
        {
            final AbstractEntityCitizen citizen = getEntity().get();

            // Applies entity related research effects.
            citizen.getNavigation().getPathingOptions().setCanUseRails(((EntityCitizen) citizen).canPathOnRails());
            citizen.getNavigation().getPathingOptions().setCanClimbVines(((EntityCitizen) citizen).canClimbVines());

            final AttributeModifier speedModifier = new AttributeModifier(RESEARCH_BONUS_MULTIPLIER,
              colony.getResearchManager().getResearchEffects().getEffectStrength(WALKING),
              AttributeModifier.Operation.MULTIPLY_TOTAL);
            AttributeModifierUtils.addModifier(citizen, speedModifier, Attributes.MOVEMENT_SPEED);

            final AttributeModifier healthModLevel =
              new AttributeModifier(HEALTH_BOOST.toString(), colony.getResearchManager().getResearchEffects().getEffectStrength(HEALTH_BOOST), AttributeModifier.Operation.ADDITION);
            AttributeModifierUtils.addHealthModifier(citizen, healthModLevel);
        }
    }

    @Override
    public void onGoSleep()
    {
        if (random.nextInt(NO_GUARD_COMPLAIN_CHANCE) != 0)
        {
            return;
        }

        if (workBuilding != null && !workBuilding.isGuardBuildingNear() && !WorldUtil.isPeaceful(colony.getWorld()))
        {
            triggerInteraction(new StandardInteraction(Component.translatable(CITIZEN_NOT_GUARD_NEAR_WORK),
              Component.translatable(CITIZEN_NOT_GUARD_NEAR_WORK),
              ChatPriority.CHITCHAT));
        }

        if (homeBuilding != null && !homeBuilding.isGuardBuildingNear() && !WorldUtil.isPeaceful(colony.getWorld()))
        {
            triggerInteraction(new StandardInteraction(Component.translatable(CITIZEN_NOT_GUARD_NEAR_HOME),
              Component.translatable(CITIZEN_NOT_GUARD_NEAR_HOME),
              ChatPriority.CHITCHAT));
        }
    }

    @Override
    public void setNextRespawnPosition(final BlockPos pos)
    {
        nextRespawnPos = pos;
    }

    @Override
    public boolean needsBetterFood()
    {
        if (this.getWorkBuilding() == null)
        {
            return false;
        }
        else
        {
            int slotBadFood = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(inventory,
              stack -> CAN_EAT.test(stack) && !this.getWorkBuilding().canEat(stack));
            int slotGoodFood = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(inventory,
              stack -> CAN_EAT.test(stack) && this.getWorkBuilding().canEat(stack));
            return slotBadFood != -1 && slotGoodFood == -1;
        }
    }

    @Override
    public boolean isWorking()
    {
        return isWorking;
    }

    @Override
    public void onResurrect()
    {
        this.workBuilding = null;
        this.homeBuilding = null;
        this.job = null;
    }

    @Override
    public void setWorking(final boolean isWorking)
    {
        if (isWorking && !this.isWorking)
        {
            if (job != null)
            {
                this.isWorking = isWorking;
                job.triggerActivityChangeAction(isWorking);
            }
            inactivityTimer = DISABLED;
        }
        else if (!isWorking && this.isWorking)
        {
            inactivityTimer = 0;
            this.isWorking = isWorking;
        }
    }

    @Nullable
    @Override
    public ICitizenData getPartner()
    {
        return colony.getCitizenManager().getCivilian(partner);
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
    public void addSiblings(final Integer... siblings)
    {
        Collections.addAll(this.siblings, siblings);
    }

    @Override
    public void addChildren(final Integer... children)
    {
        Collections.addAll(this.children, children);
    }

    @Override
    public void setPartner(final int id)
    {
        this.partner = id;
    }

    @Override
    public void onDeath(final Integer id)
    {
        this.children.remove(id);
        this.siblings.remove(id);
        if (this.partner.equals(id))
        {
            this.partner = 0;
        }
    }

    @Override
    public void setParents(final String firstParent, final String secondParent)
    {
        this.parents = new Tuple<>(firstParent, secondParent);
    }
}
