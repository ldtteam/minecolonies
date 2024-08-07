package com.minecolonies.core.event;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestManager;
import com.minecolonies.api.quests.IQuestObjectiveTemplate;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.quests.objectives.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class handles all permission checks on events and cancels them if needed.
 */
@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class QuestObjectiveEventHandler
{
    /**
     * Building building objective tracker.
     */
    private static final Map<BuildingEntry, Map<IColony, List<IQuestInstance>>> buildBuildingObjectives = new HashMap<>();

    /**
     * Research objective tracker.
     */
    private static final Map<ResourceLocation, Map<IColony, List<IQuestInstance>>> researchObjectives = new HashMap<>();

    /**
     * Mine block objective tracker.
     */
    private static final Map<Block, Map<UUID, List<IQuestInstance>>> breakBlockObjectives = new HashMap<>();

    /**
     * Entity kill objective tracker.
     */
    private static final Map<EntityType<?>, Map<UUID, List<IQuestInstance>>> entityKillObjectives = new HashMap<>();

    /**
     * Place block objective tracker.
     */
    private static final Map<Block, Map<UUID, List<IQuestInstance>>> placeBlockObjectives = new HashMap<>();

    /**
     * BlockEvent.BreakEvent handler.
     *
     * @param event BlockEvent.BreakEvent
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void on(final BlockEvent.BreakEvent event)
    {
        final LevelAccessor world = event.getLevel();
        if (world.isClientSide())
        {
            return;
        }

        final Block block = event.getState().getBlock();
        if (breakBlockObjectives.containsKey(block) && breakBlockObjectives.get(block).containsKey(event.getPlayer().getUUID()))
        {
            final List<IQuestInstance> objectives = breakBlockObjectives.get(block).get(event.getPlayer().getUUID());
            for (IQuestInstance colonyQuest : new ArrayList<>(objectives))
            {
                final IQuestObjectiveTemplate objective = IQuestManager.GLOBAL_SERVER_QUESTS.get(colonyQuest.getId()).getObjective(colonyQuest.getObjectiveIndex());
                if (objective instanceof IBreakBlockObjectiveTemplate)
                {
                    ((IBreakBlockObjectiveTemplate) objective).onBlockBreak(colonyQuest.getCurrentObjectiveInstance(), colonyQuest, event.getPlayer());
                }
                else
                {
                    objectives.remove(colonyQuest);
                    break;
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void on(final LivingDeathEvent event)
    {
        if (event.getSource().getEntity() instanceof Player
              && entityKillObjectives.containsKey(event.getEntity().getType())
              && entityKillObjectives.get(event.getEntity().getType()).containsKey(event.getSource().getEntity().getUUID()))
        {
            final List<IQuestInstance> objectives = entityKillObjectives.get(event.getEntity().getType()).get(event.getSource().getEntity().getUUID());
            for (IQuestInstance colonyQuest : new ArrayList<>(objectives))
            {
                final IQuestObjectiveTemplate objective = IQuestManager.GLOBAL_SERVER_QUESTS.get(colonyQuest.getId()).getObjective(colonyQuest.getObjectiveIndex());
                if (objective instanceof IKillEntityObjectiveTemplate)
                {
                    ((IKillEntityObjectiveTemplate) objective).onEntityKill(colonyQuest.getCurrentObjectiveInstance(), colonyQuest, (Player) event.getSource().getEntity());
                }
                else
                {
                    objectives.remove(colonyQuest);
                    break;
                }
            }
        }
    }

    /**
     * BlockEvent.Place handler.
     *
     * @param event BlockEvent.BreakEvent
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void on(final BlockEvent.EntityPlaceEvent event)
    {
        final LevelAccessor world = event.getLevel();
        if (world.isClientSide() || !(event.getEntity() instanceof Player))
        {
            return;
        }

        final Block block =  event.getPlacedBlock().getBlock();
        if (placeBlockObjectives.containsKey(block) && placeBlockObjectives.get(block).containsKey(event.getEntity().getUUID()))
        {
            final List<IQuestInstance> objectives = placeBlockObjectives.get(block).get(event.getEntity().getUUID());
            for (IQuestInstance colonyQuest : new ArrayList<>(objectives))
            {
                final IQuestObjectiveTemplate objective = IQuestManager.GLOBAL_SERVER_QUESTS.get(colonyQuest.getId()).getObjective(colonyQuest.getObjectiveIndex());
                if (objective instanceof IPlaceBlockObjectiveTemplate)
                {
                    ((IPlaceBlockObjectiveTemplate) objective).onBlockPlace(colonyQuest.getCurrentObjectiveInstance(), colonyQuest, (Player) event.getEntity());
                }
                else
                {
                    objectives.remove(colonyQuest);
                    break;
                }
            }
        }
    }

    /**
     * Add an objective listener for block mining to this event handler.
     *
     * @param blockToMine    the block that we listen for.
     * @param assignedPlayer the player we check for.
     * @param colonyQuest    the colony quest it is related to.
     */
    public static void addQuestMineObjectiveListener(final Block blockToMine, final UUID assignedPlayer, final IQuestInstance colonyQuest)
    {
        final Map<UUID, List<IQuestInstance>> currentMap = breakBlockObjectives.getOrDefault(blockToMine, new HashMap<>());
        final List<IQuestInstance> objectives = currentMap.getOrDefault(assignedPlayer, new ArrayList<>());
        objectives.add(colonyQuest);
        currentMap.put(assignedPlayer, objectives);
        breakBlockObjectives.put(blockToMine, currentMap);
    }

    /**
     * Remove an objective listener to this event handler.
     *
     * @param blockToMine    the block that we listen for.
     * @param assignedPlayer the player we check for.
     * @param colonyQuest    the colony quest it is related to.
     */
    public static void removeQuestMineObjectiveListener(final Block blockToMine, final UUID assignedPlayer, final IQuestInstance colonyQuest)
    {
        breakBlockObjectives.getOrDefault(blockToMine, new HashMap<>()).getOrDefault(assignedPlayer, new ArrayList<>()).remove(colonyQuest);
    }

    /**
     * Add an objective listener for block placement to this event handler.
     *
     * @param blockToPlace    the block that we listen for.
     * @param assignedPlayer the player we check for.
     * @param colonyQuest    the colony quest it is related to.
     */
    public static void addQuestPlaceObjectiveListener(final Block blockToPlace, final UUID assignedPlayer, final IQuestInstance colonyQuest)
    {
        final Map<UUID, List<IQuestInstance>> currentMap = placeBlockObjectives.getOrDefault(blockToPlace, new HashMap<>());
        final List<IQuestInstance> objectives = currentMap.getOrDefault(assignedPlayer, new ArrayList<>());
        objectives.add(colonyQuest);
        currentMap.put(assignedPlayer, objectives);
        placeBlockObjectives.put(blockToPlace, currentMap);
    }

    /**
     * Remove an objective listener for block placement to this event handler.
     *
     * @param blockToPlace    the block that we listen for.
     * @param assignedPlayer the player we check for.
     * @param colonyQuest    the colony quest it is related to.
     */
    public static void removeQuestPlaceBlockObjectiveListener(final Block blockToPlace, final UUID assignedPlayer, final IQuestInstance colonyQuest)
    {
        placeBlockObjectives.getOrDefault(blockToPlace, new HashMap<>()).getOrDefault(assignedPlayer, new ArrayList<>()).remove(colonyQuest);
    }

    /**
     * Add an objective listener to this event handler.
     *
     * @param entityToKill   the entity type that we listen for.
     * @param assignedPlayer the player we check for.
     * @param colonyQuest    the colony quest it is related to.
     */
    public static void addKillQuestObjectiveListener(final EntityType<?> entityToKill, final UUID assignedPlayer, final IQuestInstance colonyQuest)
    {
        final Map<UUID, List<IQuestInstance>> currentMap = entityKillObjectives.getOrDefault(entityToKill, new HashMap<>());
        final List<IQuestInstance> objectives = currentMap.getOrDefault(assignedPlayer, new ArrayList<>());
        objectives.add(colonyQuest);
        currentMap.put(assignedPlayer, objectives);
        entityKillObjectives.put(entityToKill, currentMap);
    }

    /**
     * Remove an objective listener to this event handler.
     *
     * @param entityToKill   the entity type that we listen for.
     * @param assignedPlayer the player we check for.
     * @param colonyQuest    the colony quest it is related to.
     */
    public static void removeKillQuestObjectiveListener(final EntityType<?> entityToKill, final UUID assignedPlayer, final IQuestInstance colonyQuest)
    {
        entityKillObjectives.getOrDefault(entityToKill, new HashMap<>()).getOrDefault(assignedPlayer, new ArrayList<>()).remove(colonyQuest);
    }

    /**
     * Research complete event.
     * @param colony the colony the research happened in.
     * @param id the research id.
     */
    public static void onResearchComplete(final IColony colony, final ResourceLocation id)
    {
        if (researchObjectives.containsKey(id))
        {
            for (final IQuestInstance instance : new ArrayList<>(researchObjectives.get(id).getOrDefault(colony, new ArrayList<>())))
            {
                final IQuestObjectiveTemplate objective = IQuestManager.GLOBAL_SERVER_QUESTS.get(instance.getId()).getObjective(instance.getObjectiveIndex());
                if (objective instanceof IResearchObjectiveTemplate researchTemplate)
                {
                    researchTemplate.onResearchCompletion(instance);
                }
            }
        }
    }

    /**
     * Building upgrade handling.
     * @param building the building being upgraded.
     * @param level its level.
     */
    public static void onBuildingUpgradeComplete(final IBuilding building, final int level)
    {
        if (buildBuildingObjectives.containsKey(building.getBuildingType()))
        {
            for (final IQuestInstance instance : new ArrayList<>(buildBuildingObjectives.get(building.getBuildingType()).getOrDefault(building.getColony(), new ArrayList<>())))
            {
                final IQuestObjectiveTemplate objective = IQuestManager.GLOBAL_SERVER_QUESTS.get(instance.getId()).getObjective(instance.getObjectiveIndex());
                if (objective instanceof IBuildingUpgradeObjectiveTemplate buildingTemplate)
                {
                    buildingTemplate.onBuildingUpgrade(instance.getCurrentObjectiveInstance(), instance, level);
                }
            }
        }
    }

    /**
     * Track building leveling.
     * @param buildingEntry the building to track.
     * @param colonyQuest the quest tracking it.
     */
    public static void trackBuildingLevelUp(final @NotNull BuildingEntry buildingEntry, final @NotNull IQuestInstance colonyQuest)
    {
        final Map<IColony, List<IQuestInstance>> currentMap = buildBuildingObjectives.getOrDefault(buildingEntry, new HashMap<>());
        final List<IQuestInstance> objectives = currentMap.getOrDefault(colonyQuest.getColony(), new ArrayList<>());
        objectives.add(colonyQuest);
        currentMap.put(colonyQuest.getColony(), objectives);
        buildBuildingObjectives.put(buildingEntry, currentMap);
    }

    /**
     * Stop tracking building leveling.
     * @param buildingEntry the building to stop tracking.
     * @param colonyQuest the quest tracking it.
     */
    public static void stopTrackingBuildingLevelUp(final @NotNull BuildingEntry buildingEntry, final @NotNull IQuestInstance colonyQuest)
    {
        buildBuildingObjectives.getOrDefault(buildingEntry, new HashMap<>()).getOrDefault(colonyQuest.getColony(), new ArrayList<>()).remove(colonyQuest);
    }

    /**
     * Track research.
     * @param researchId the research to track.
     * @param colonyQuest the quest tracking it.
     */
    public static void trackResearch(final ResourceLocation researchId, final IQuestInstance colonyQuest)
    {
        final Map<IColony, List<IQuestInstance>> currentMap = researchObjectives.getOrDefault(researchId, new HashMap<>());
        final List<IQuestInstance> objectives = currentMap.getOrDefault(colonyQuest.getColony(), new ArrayList<>());
        objectives.add(colonyQuest);
        currentMap.put(colonyQuest.getColony(), objectives);
        researchObjectives.put(researchId, currentMap);
    }

    /**
     * Stop tracking research.
     * @param researchId the research to stop tracking.
     * @param colonyQuest the quest tracking it.
     */
    public static void stopTrackingResearch(final @NotNull ResourceLocation researchId, final @NotNull IQuestInstance colonyQuest)
    {
        researchObjectives.getOrDefault(researchId, new HashMap<>()).getOrDefault(colonyQuest.getColony(), new ArrayList<>()).remove(colonyQuest);
    }
}
