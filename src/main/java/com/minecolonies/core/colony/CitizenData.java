package com.minecolonies.core.colony;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.CitizenNameFile;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.modules.IAssignsJob;
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
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenDiseaseHandler;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenFoodHandler;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenSkillHandler;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.quests.IQuestDeliveryObjective;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestManager;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.modules.LivingBuildingModule;
import com.minecolonies.core.colony.interactionhandling.QuestDeliveryInteraction;
import com.minecolonies.core.colony.interactionhandling.QuestDialogueInteraction;
import com.minecolonies.core.colony.interactionhandling.ServerCitizenInteraction;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.citizen.citizenhandlers.*;
import com.minecolonies.core.network.messages.client.colony.ColonyViewCitizenViewMessage;
import com.minecolonies.core.util.AttributeModifierUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.*;
import static com.minecolonies.api.research.util.ResearchConstants.*;
import static com.minecolonies.api.util.constant.BuildingConstants.TAG_ACTIVE;
import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.ColonyConstants.UPDATE_SUBSCRIBERS_INTERVAL;
import static com.minecolonies.api.util.constant.Constants.TAG_STRING;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_NAME;
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
    public static final float MAX_HEALTH = 20.0F;

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
     * Number of sound profiles.
     */
    private static final int NUM_SOUND_PROFILES = 4;

    /**
     * The unique citizen id.
     */
    private final int id;

    /**
     * The unique citizen id.
     */
    private UUID uuid;

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
     * The job of the citizen.
     */
    private IJob<?> job;

    /**
     * If the citizen is dirty (Has to be updated on client side).
     */
    private int dirty = Integer.MAX_VALUE;

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
     * The citizen skill handler.
     */
    private final ICitizenFoodHandler citizenFoodHandler;

    /**
     * Disease handler
     */
    private final CitizenDiseaseHandler citizenDiseaseHandler;

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
     * The current location of interest.
     */
    @Nullable private BlockPos statusPosition;

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
     * The list of available quests the citizen can give out.
     */
    private final List<ResourceLocation> availableQuests = new ArrayList<>();

    /**
     * The list of participating quests the citizen can give out.
     */
    private final List<ResourceLocation> participatingQuests = new ArrayList<>();

    /**
     * Tracking quests the citizen was the quest giver in.
     */
    private final List<ResourceLocation> finishedQuests = new ArrayList<>();

    /**
     * Tracking quests the citizen participated in.
     */
    private final List<ResourceLocation> finishedQuestParticipation = new ArrayList<>();

    /**
     * The sound profile index.
     */
    private int voiceProfile;

    /**
     * Recent interaction timer
     */
    private int interactedRecently = 0;

    /**
     * Recent interaction timer
     */
    private Set<UUID> interactedRecentlyPlayers = new HashSet<>();

    /**
     * Texture UUID.
     */
    private UUID textureUUID;

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
        this.citizenFoodHandler = new CitizenFoodHandler(this);
        citizenDiseaseHandler = new CitizenDiseaseHandler(this);
    }

    @Override
    public void onResponseTriggered(@NotNull final Component key, final int responseId, final Player player)
    {
        if (citizenChatOptions.containsKey(key))
        {
            citizenChatOptions.get(key).onServerResponseTriggered(responseId, player, this);
            markDirty(0);
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

        if (citizen != null && citizen.isRemoved())
        {
            entity.clear();
            return Optional.empty();
        }

        return Optional.ofNullable(citizen);
    }

    @Override
    public int getVoiceProfile()
    {
        return voiceProfile;
    }

    @Override
    public void setVoiceProfile(final int profile)
    {
        this.voiceProfile = profile;
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
    public void markDirty(final int time)
    {
        dirty = Math.min(dirty, time);

        if (interactedRecently > 0)
        {
            for (final UUID player : interactedRecentlyPlayers)
            {
                if (getColony().getWorld().getPlayerByUUID(player) instanceof ServerPlayer playerEntity)
                {
                    Network.getNetwork().sendToPlayer(new ColonyViewCitizenViewMessage((Colony) getColony(), this), playerEntity);
                }
            }
        }

        if (isDirty())
        {
            colony.getCitizenManager().markDirty();
        }
    }

    /**
     * Returns a random element in a list.
     *
     * @param rand Random object.
     * @param list Array to select from.
     * @return Random element from array.
     */
    private static String getRandomElement(@NotNull final Random rand, @NotNull final List<String> list)
    {
        return list.get(rand.nextInt(list.size()));
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
        voiceProfile = random.nextInt(NUM_SOUND_PROFILES);
        paused = false;
        name = generateName(random, female, getColony(), getColony().getCitizenNameFile());
        textureId = random.nextInt(255);

        saturation = MAX_SATURATION;

        int levelCap = (int) colony.getOverallHappiness() * 2;
        if (colony.getCitizenManager().getCitizens().size() < IMinecoloniesAPI.getInstance().getConfig().getServer().initialCitizenAmount.get())
        {
            levelCap = Math.max(5, levelCap);
        }
        citizenSkillHandler.init(levelCap);

        markDirty(0);
    }

    /**
     * Initializes the entities values from citizen data.
     */
    @Override
    public void initEntityValues()
    {
        if (!getEntity().isPresent())
        {
            Log.getLogger().warn("Missing entity upon adding data to that entity!" + this.toString(), new Exception());
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

        if (getBedPos().equals(BlockPos.ZERO))
        {
            citizen.getCitizenSleepHandler().onWakeUp();
        }

        citizen.getCitizenExperienceHandler().updateLevel();

        setLastPosition(citizen.blockPosition());

        citizen.getCitizenJobHandler().onJobChanged(citizen.getCitizenJobHandler().getColonyJob());

        applyResearchEffects();

        applyItemModifiers(citizen);

        markDirty(0);
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
            final ItemStack stack;
            if (slot.isArmor())
            {
                stack = citizen.getInventoryCitizen().getArmorInSlot(slot);
            }
            else
            {
                stack = citizen.getItemBySlot(slot);
            }

            if (!ItemStackUtils.isEmpty(stack))
            {
                citizen.getAttributes().addTransientAttributeModifiers(stack.getAttributeModifiers(slot));
            }
        }
    }

    /**
     * Generates a random name from a set of names.
     *
     * @param rand   Random object.
     * @param female the gender
     * @param colony the colony.
     * @return Name of the citizen.
     */
    public static String generateName(@NotNull final Random rand, final boolean female, final IColony colony, final CitizenNameFile nameFile)
    {
        String citizenName;
        final String firstName;
        final String middleInitial;
        final String lastName;

        if (female)
        {
            firstName = getRandomElement(rand, nameFile.femalefirstNames);
        }
        else
        {
            firstName = getRandomElement(rand, nameFile.maleFirstNames);
        }

        middleInitial = String.valueOf(getRandomLetter(rand));
        lastName = getRandomElement(rand, nameFile.surnames);

        if (nameFile.order == CitizenNameFile.NameOrder.EASTERN)
        {
            //For now, don't include middle names, as their rules (and presence) vary heavily by region.
            citizenName = String.format("%s %s", lastName, firstName);
        }
        else
        {
            if (nameFile.parts == 3)
            {
                citizenName = String.format("%s %s. %s", firstName, middleInitial, lastName);
            }
            else if (nameFile.parts == 2)
            {
                citizenName = String.format("%s %s", firstName, lastName);
            }
            else
            {
                citizenName = firstName;
            }
        }

        // Check whether there's already a citizen with this name
        for (final ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            if (citizen != null && citizen.getName().equals(citizenName))
            {
                // Oops - recurse this function and try again
                citizenName = generateName(rand, female, colony, nameFile);
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
    public void generateName(@NotNull final Random rand, final String firstParentName, final String secondParentName, final CitizenNameFile nameFile)
    {
        String nameA = firstParentName;
        String nameB = secondParentName;

        String citizenName;
        final String firstName;
        String middleInitial = "";
        final String lastName;

        if (firstParentName == null || firstParentName.isEmpty())
        {
            nameA = generateName(rand, rand.nextBoolean(), colony, nameFile);
        }

        if (secondParentName == null || secondParentName.isEmpty())
        {
            nameB = generateName(rand, rand.nextBoolean(), colony, nameFile);
        }

        final String[] firstParentNameSplit = nameA.split(" ");
        final String[] secondParentNameSplit = nameB.split(" ");

        if (firstParentNameSplit.length <= 1)
        {
            generateName(rand, "", secondParentName, nameFile);
            return;
        }

        if (secondParentNameSplit.length <= 1)
        {
            generateName(rand, firstParentName, "", nameFile);
            return;
        }

        final boolean eastern = nameFile.order == CitizenNameFile.NameOrder.EASTERN;

        if (random.nextBoolean())
        {
            if (nameFile.parts == 3)
            {
                middleInitial = firstParentNameSplit[eastern ? 0 : firstParentNameSplit.length - 1].substring(0, 1);
                lastName = secondParentNameSplit[eastern ? 0 : secondParentNameSplit.length - 1];
            }
            else
            {
                lastName = eastern ? secondParentNameSplit[0] : nameB.replace(secondParentNameSplit[0], "").trim();
            }
        }
        else
        {
            if (nameFile.parts == 3)
            {
                middleInitial = secondParentNameSplit[eastern ? 0 : secondParentNameSplit.length - 1].substring(0, 1);
                lastName = firstParentNameSplit[eastern ? 0 : firstParentNameSplit.length - 1];
            }
            else
            {
                lastName = eastern ? firstParentNameSplit[0] : nameA.replace(firstParentNameSplit[0], "").trim();
            }
        }

        if (female)
        {
            firstName = getRandomElement(rand, nameFile.femalefirstNames);
        }
        else
        {
            firstName = getRandomElement(rand, nameFile.maleFirstNames);
        }

        if (nameFile.order == CitizenNameFile.NameOrder.EASTERN)
        {
            //For now, don't include middle names, as their rules (and presence) vary heavily by region.
            citizenName = String.format("%s %s", lastName, firstName);
        }
        else
        {
            if (nameFile.parts == 3)
            {
                citizenName = String.format("%s %s. %s", firstName, middleInitial, lastName);
            }
            else if (nameFile.parts == 2)
            {
                citizenName = String.format("%s %s", firstName, lastName);
            }
            else
            {
                citizenName = firstName;
            }
        }

        // Check whether there's already a citizen with this name
        for (final ICitizenData citizen : this.getColony().getCitizenManager().getCitizens())
        {
            if (citizen != null && citizen.getName().equals(citizenName))
            {
                // Oops - recurse this function and try again
                generateName(rand, firstParentName, secondParentName, nameFile);
                return;
            }
        }
        this.name = citizenName;
    }

    @Override
    public boolean isRelatedTo(final ICitizenData data)
    {
        return siblings.contains(data.getId()) || children.contains(data.getId()) || partner == data.getId() || parents.getA().equals(data.getName()) || parents.getB()
          .equals(data.getName());
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
        this.name = generateName(random, isFemale, getColony(), getColony().getCitizenNameFile());
        markDirty(0);
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
        markDirty(40);
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
        return dirty <= 0;
    }

    @Override
    public void clearDirty()
    {
        if (isDirty())
        {
            dirty = Integer.MAX_VALUE;
        }

        if (dirty > 0)
        {
            dirty -= UPDATE_SUBSCRIBERS_INTERVAL;
            if (isDirty())
            {
                colony.getCitizenManager().markDirty();
            }
        }
    }

    @Override
    public void onRemoveBuilding(final IBuilding building)
    {
        if (homeBuilding != null && homeBuilding.getID().equals(building.getID()))
        {
            setHomeBuilding(null);
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
            homeBuilding.getFirstModuleOccurance(LivingBuildingModule.class).removeCitizen(this);
        }

        if (homeBuilding != null)
        {
            setBedPos(BlockPos.ZERO);
        }

        homeBuilding = building;
        markDirty(0);

        if (getEntity().isPresent() && getEntity().get().getCitizenJobHandler().getColonyJob() == null)
        {
            getEntity().get().getCitizenJobHandler().setModelDependingOnJob(null);
        }
    }

    @Override
    @Nullable
    public IBuilding getWorkBuilding()
    {
        if (job == null)
        {
            return null;
        }
        return job.getWorkBuilding();
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
            final IJob oldJob = this.job;
            this.job = null;
            oldJob.onRemoval();
        }
        this.job = job;

        getEntity().ifPresent(entityCitizen -> entityCitizen.getCitizenJobHandler().onJobChanged(job));

        markDirty(0);
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

        buf.writeBoolean(getWorkBuilding() != null);
        if (getWorkBuilding() != null)
        {
            buf.writeBlockPos(getWorkBuilding().getID());
        }

        buf.writeDouble(getSaturation());
        buf.writeDouble(citizenHappinessHandler.getHappiness(getColony(), this));

        buf.writeNbt(citizenSkillHandler.write());

        buf.writeUtf((job != null) ? job.getJobRegistryEntry().getTranslationKey() : "");

        buf.writeInt(colony.getID());

        final CompoundTag compound = new CompoundTag();
        inventory.write(compound);
        buf.writeNbt(compound);
        buf.writeBlockPos(lastPosition);

        if (colony.getWorld() != null)
        {
            final List<IInteractionResponseHandler> subInteractions = citizenChatOptions.values().stream().filter(e -> e.isVisible(colony.getWorld())).toList();

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
        citizenHappinessHandler.write(happinessCompound, false);
        buf.writeNbt(happinessCompound);

        buf.writeInt(status != null ? status.getId() : -1);

        buf.writeBoolean(statusPosition != null);
        if (statusPosition != null)
        {
            buf.writeBlockPos(statusPosition);
        }

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

        buf.writeInt(availableQuests.size());
        for (final ResourceLocation av : availableQuests)
        {
            buf.writeResourceLocation(av);
        }

        buf.writeInt(participatingQuests.size());
        for (final ResourceLocation av : participatingQuests)
        {
            buf.writeResourceLocation(av);
        }

        if (textureUUID == null)
        {
            buf.writeBoolean(false);
        }
        else
        {
            buf.writeBoolean(true);
            buf.writeUUID(textureUUID);
        }
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
        markDirty(0);
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
    public ICitizenFoodHandler getCitizenFoodHandler()
    {
        return citizenFoodHandler;
    }

    @Override
    public ICitizenDiseaseHandler getCitizenDiseaseHandler()
    {
        return citizenDiseaseHandler;
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
        markDirty(0);

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
        nbtTagCompound.putInt(TAG_SOUND_PROFILE, voiceProfile);

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

        citizenHappinessHandler.write(nbtTagCompound, true);
        citizenMournHandler.write(nbtTagCompound);
        citizenFoodHandler.write(nbtTagCompound);
        citizenDiseaseHandler.write(nbtTagCompound);

        inventory.write(nbtTagCompound);
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


        @NotNull final ListTag avQuestNBT = new ListTag();
        for (final ResourceLocation quest : availableQuests)
        {
            avQuestNBT.add(StringTag.valueOf(quest.toString()));
        }
        nbtTagCompound.put(TAG_AV_QUESTS, avQuestNBT);

        @NotNull final ListTag partQuestNBT = new ListTag();
        for (final ResourceLocation quest : participatingQuests)
        {
            partQuestNBT.add(StringTag.valueOf(quest.toString()));
        }
        nbtTagCompound.put(TAG_PART_QUESTS, partQuestNBT);

        @NotNull final ListTag finishedQuestNBT = new ListTag();
        for (final ResourceLocation quest : finishedQuests)
        {
            finishedQuestNBT.add(StringTag.valueOf(quest.toString()));
        }
        nbtTagCompound.put(TAG_FINISHED_AV_QUESTS, finishedQuestNBT);

        @NotNull final ListTag finishedPartQuestNBT = new ListTag();
        for (final ResourceLocation quest : finishedQuestParticipation)
        {
            finishedPartQuestNBT.add(StringTag.valueOf(quest.toString()));
        }
        nbtTagCompound.put(TAG_FINISHED_PART_QUESTS, finishedPartQuestNBT);

        if (textureUUID != null)
        {
            nbtTagCompound.putUUID(TAG_TEXTURE_UUID, textureUUID);
        }
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

        if (nbtTagCompound.contains(TAG_SUFFIX))
        {
            textureSuffix = nbtTagCompound.getString(TAG_SUFFIX);
        }
        else
        {
            textureSuffix = SUFFIXES.get(random.nextInt(SUFFIXES.size()));
        }

        if (nbtTagCompound.contains(TAG_SOUND_PROFILE))
        {
            voiceProfile = nbtTagCompound.getInt(TAG_SOUND_PROFILE);
        }
        else
        {
            voiceProfile = random.nextInt(NUM_SOUND_PROFILES);
        }

        lastPosition = BlockPosUtil.read(nbtTagCompound, TAG_POS);

        if (nbtTagCompound.contains(TAG_RESPAWN_POS))
        {
            nextRespawnPos = BlockPosUtil.read(nbtTagCompound, TAG_RESPAWN_POS);
        }

        citizenSkillHandler.read(nbtTagCompound.getCompound(TAG_NEW_SKILLS));

        saturation = nbtTagCompound.getDouble(TAG_SATURATION);

        if (nbtTagCompound.contains("job"))
        {
            setJob(IJobDataManager.getInstance().createFrom(this, nbtTagCompound.getCompound("job")));
        }

        if (nbtTagCompound.contains(TAG_INVENTORY))
        {
            this.inventory.read(nbtTagCompound);
            this.inventory.setHeldItem(InteractionHand.MAIN_HAND, nbtTagCompound.getInt(TAG_HELD_ITEM_SLOT));
            this.inventory.setHeldItem(InteractionHand.OFF_HAND, nbtTagCompound.getInt(TAG_OFFHAND_HELD_ITEM_SLOT));
        }

        if (name.isEmpty())
        {
            name = generateName(random, isFemale(), getColony(), getColony().getCitizenNameFile());
        }

        if (nbtTagCompound.contains(TAG_ASLEEP))
        {
            bedPos = BlockPosUtil.read(nbtTagCompound, TAG_BEDS);
            isAsleep = nbtTagCompound.getBoolean(TAG_ASLEEP);
        }

        if (nbtTagCompound.contains(TAG_JUST_ATE))
        {
            justAte = nbtTagCompound.getBoolean(TAG_JUST_ATE);
        }

        //  Citizen chat options.
        if (nbtTagCompound.contains(TAG_CHAT_OPTIONS))
        {
            final ListTag handlerTagList = nbtTagCompound.getList(TAG_CHAT_OPTIONS, Tag.TAG_COMPOUND);
            for (int i = 0; i < handlerTagList.size(); ++i)
            {
                try
                {
                    final ServerCitizenInteraction handler =
                      (ServerCitizenInteraction) MinecoloniesAPIProxy.getInstance()
                        .getInteractionResponseHandlerDataManager()
                        .createFrom(this, handlerTagList.getCompound(i).getCompound(TAG_CHAT_OPTION));
                    citizenChatOptions.put(handler.getId(), handler);
                }
                catch (final Exception ex)
                {
                    Log.getLogger().warn("Failed to load Interaction for a quest. Did the quest vanish?", ex);
                }
            }
        }

        this.citizenHappinessHandler.read(nbtTagCompound, true);
        this.citizenMournHandler.read(nbtTagCompound);
        this.citizenFoodHandler.read(nbtTagCompound);
        citizenDiseaseHandler.read(nbtTagCompound);

        if (nbtTagCompound.contains(TAG_LEVEL_MAP) && !nbtTagCompound.contains(TAG_NEW_SKILLS))
        {
            citizenSkillHandler.init((int) citizenHappinessHandler.getHappiness(getColony(), this));
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

        @NotNull final ListTag availQuestNbt = nbtTagCompound.getList(TAG_AV_QUESTS, TAG_STRING);
        for (int i = 0; i < availQuestNbt.size(); i++)
        {
            availableQuests.add(new ResourceLocation(availQuestNbt.getString(i)));
        }

        @NotNull final ListTag partQuestsNbt = nbtTagCompound.getList(TAG_PART_QUESTS, TAG_STRING);
        for (int i = 0; i < partQuestsNbt.size(); i++)
        {
            participatingQuests.add(new ResourceLocation(partQuestsNbt.getString(i)));
        }

        @NotNull final ListTag finQuestNbt = nbtTagCompound.getList(TAG_FINISHED_AV_QUESTS, TAG_STRING);
        for (int i = 0; i < finQuestNbt.size(); i++)
        {
            finishedQuests.add(new ResourceLocation(finQuestNbt.getString(i)));
        }

        @NotNull final ListTag finPartQuestsNbt = nbtTagCompound.getList(TAG_FINISHED_PART_QUESTS, TAG_STRING);
        for (int i = 0; i < finPartQuestsNbt.size(); i++)
        {
            finishedQuestParticipation.add(new ResourceLocation(finPartQuestsNbt.getString(i)));
        }

        if (nbtTagCompound.contains(TAG_TEXTURE_UUID))
        {
            this.textureUUID = nbtTagCompound.getUUID(TAG_TEXTURE_UUID);
        }
    }

    @Override
    public void onBuildingLoad()
    {
        if (job == null)
        {
            return;
        }

        if (job.getBuildingPos() == null)
        {
            setJob(null);
            return;
        }

        if (job.getBuildingPos() != null && job.getWorkBuilding() == null)
        {
            final IBuilding building = colony.getBuildingManager().getBuilding(job.getBuildingPos());

            if (building != null)
            {
                for (final IAssignsJob module : building.getModulesByType(IAssignsJob.class))
                {
                    if (module.getJobEntry().equals(job.getJobRegistryEntry()) && module.assignCitizen(this))
                    {
                        break;
                    }
                }
            }

            if (building == null || job.getWorkBuilding() == null)
            {
                setJob(null);
            }
        }
    }

    @Override
    public void setInteractedRecently(final UUID player)
    {
        interactedRecentlyPlayers.add(player);
        interactedRecently = TICKS_SECOND * 20;
    }

    @Override
    public void update(final int tickRate)
    {
        if (!getEntity().isPresent() || !getEntity().get().isAlive())
        {
            return;
        }

        if (interactedRecently > 0)
        {
            interactedRecently -= TICKS_SECOND * 3;
            if (interactedRecently <= 0)
            {
                interactedRecentlyPlayers.clear();
            }
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
            markDirty(20 * 10);
        }

        for (final IInteractionResponseHandler handler : toRemove)
        {
            citizenChatOptions.remove(handler.getId());
            for (final Component comp : handler.getPossibleResponses())
            {
                if (citizenChatOptions.containsKey(handler.getResponseResult(comp)))
                {
                    citizenChatOptions.get(handler.getResponseResult(comp)).removeParent(handler.getId());
                }
            }
        }

        citizenDiseaseHandler.update(tickRate);
    }

    @Override
    public void triggerInteraction(@NotNull final IInteractionResponseHandler handler)
    {
        if (!this.citizenChatOptions.containsKey(handler.getId()))
        {
            this.citizenChatOptions.put(handler.getId(), handler);
            for (final IInteractionResponseHandler childHandler : handler.genChildInteractions())
            {
                this.citizenChatOptions.put(childHandler.getId(), (ServerCitizenInteraction) childHandler);
            }
            markDirty(20 * 5);
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
            markDirty(20);
        }
        this.status = status;
    }

    @Override
    public @Nullable BlockPos getStatusPosition()
    {
        return this.statusPosition;
    }

    @Override
    public void setStatusPosition(@Nullable BlockPos pos)
    {
        if (!Objects.equals(this.statusPosition, pos))
        {
            this.statusPosition = pos;
            markDirty(20*5);
        }
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
            citizen.getNavigation().getPathingOptions().setCanClimbAdvanced(((EntityCitizen) citizen).canClimbVines());

            final AttributeModifier speedModifier = new AttributeModifier(RESEARCH_BONUS_MULTIPLIER,
              colony.getResearchManager().getResearchEffects().getEffectStrength(WALKING),
              AttributeModifier.Operation.MULTIPLY_TOTAL);
            AttributeModifierUtils.addModifier(citizen, speedModifier, Attributes.MOVEMENT_SPEED);

            final AttributeModifier healthModLevel =
              new AttributeModifier(HEALTH_BOOST.toString(),
                colony.getResearchManager().getResearchEffects().getEffectStrength(HEALTH_BOOST),
                AttributeModifier.Operation.ADDITION);
            AttributeModifierUtils.addHealthModifier(citizen, healthModLevel);

            if (getColony().getResearchManager().getResearchEffects().getEffectStrength(MORE_AIR) > 0)
            {
                ((EntityCitizen) citizen).setMaxAir(600);
            }
        }
    }

    @Override
    public void onGoSleep()
    {
        if (random.nextInt(NO_GUARD_COMPLAIN_CHANCE) != 0)
        {
            return;
        }

        if (job != null && job.getWorkBuilding() != null && !job.getWorkBuilding().isGuardBuildingNear() && !WorldUtil.isPeaceful(colony.getWorld()))
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

        decreaseSaturation(
          job == null || job.getWorkBuilding().getBuildingLevel() == 0 ? 1 : (SATURATION_DECREASE_FACTOR * Math.pow(2, job.getWorkBuilding().getBuildingLevel())) * 2);
    }

    @Override
    public void setNextRespawnPosition(final BlockPos pos)
    {
        nextRespawnPos = pos;
    }

    @Override
    public boolean needsBetterFood()
    {
        if (this.getHomeBuilding() == null)
        {
            return false;
        }
        else
        {
            int slotBadFood = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(inventory,
                stack -> FoodUtils.canEat(stack, getHomeBuilding(), getWorkBuilding()));
            int slotGoodFood = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(inventory,
                stack -> FoodUtils.canEat(stack, getHomeBuilding(), getWorkBuilding()));
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
        this.homeBuilding = null;
        setJob(null);
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

    @Override
    public void setIdleDays(final int days)
    {

    }

    @Override
    public void assignQuest(final IQuestInstance quest)
    {
        this.availableQuests.add(quest.getId());
    }

    @Override
    public void openDialogue(final IQuestInstance quest, final int index)
    {
        final Component comp = Component.literal(quest.getId().toString());
        if (IQuestManager.GLOBAL_SERVER_QUESTS.get(quest.getId()).getObjective(index) instanceof IQuestDeliveryObjective)
        {
            citizenChatOptions.put(comp, new QuestDeliveryInteraction(comp, ChatPriority.CHITCHAT, quest.getId(), index, this));
        }
        else
        {
            citizenChatOptions.put(comp, new QuestDialogueInteraction(comp, ChatPriority.CHITCHAT, quest.getId(), index, this));
        }
        this.markDirty(0);
    }

    @Override
    public void addQuestParticipation(final IQuestInstance quest)
    {
        this.participatingQuests.add(quest.getId());
    }

    @Override
    public void onQuestDeletion(final ResourceLocation questId)
    {
        this.availableQuests.remove(questId);
        this.participatingQuests.remove(questId);
    }

    @Override
    public boolean isParticipantOfQuest(final ResourceLocation questId)
    {
        return this.availableQuests.contains(questId) || this.participatingQuests.contains(questId);
    }

    @Override
    public void onQuestCompletion(final ResourceLocation questId)
    {
        if (this.availableQuests.contains(questId))
        {
            this.availableQuests.remove(questId);
            this.finishedQuests.add(questId);
        }
        else if (this.participatingQuests.contains(questId))
        {
            this.participatingQuests.remove(questId);
            this.finishedQuestParticipation.add(questId);
        }
    }

    @Override
    public boolean hasQuestAssignment()
    {
        return !this.availableQuests.isEmpty() || !this.participatingQuests.isEmpty();
    }

    @Override
    public void onInteractionClosed(final Component key, final ServerPlayer sender)
    {
        final IInteractionResponseHandler chatOption = citizenChatOptions.get(key);
        if (chatOption != null)
        {
            chatOption.onClosed();
        }
    }

    @Override
    public UUID getUUID()
    {
        if (uuid == null)
        {
            uuid = UUID.nameUUIDFromBytes((getId() + ":" + getColony().getID() + ":" + getColony().getDimension().location().toString()).getBytes());
        }
        return uuid;
    }

    @Override
    public void setCustomTexture(final UUID texture)
    {
        this.textureUUID = texture;
    }

    @Override
    public boolean hasCustomTexture()
    {
        return textureUUID != null;
    }

    @Override
    public UUID getCustomTexture()
    {
        return textureUUID;
    }
    @Nullable
    public BlockPos getHomePosition()
    {
        @Nullable final IBuilding homeBuilding = getHomeBuilding();
        if (homeBuilding != null)
        {
            return homeBuilding.getStandingPosition();
        }

        if (colony != null)
        {
            final IBuilding tavern = colony.getBuildingManager().getFirstBuildingMatching(b -> b.getBuildingType() == ModBuildings.tavern.get());
            if (tavern != null)
            {
                return tavern.getStandingPosition();
            }
            else if (colony.getBuildingManager().getTownHall() != null)
            {
                return colony.getBuildingManager().getTownHall().getPosition();
            }
            return colony.getCenter();
        }

        return null;
    }

    @Override
    public double getDiseaseModifier()
    {
        return citizenFoodHandler.getDiseaseModifier(getJob() == null ? 1 : getJob().getDiseaseModifier());
    }
}
