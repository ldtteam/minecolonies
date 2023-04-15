package com.minecolonies.coremod.event;

import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestManager;
import com.minecolonies.api.quests.IQuestObjectiveTemplate;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.quests.objectives.IBreakBlockObjectiveTemplate;
import com.minecolonies.coremod.quests.objectives.IKillEntityObjectiveTemplate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class handles all permission checks on events and cancels them if needed.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class QuestObjectiveEventHandler
{
    /**
     * Mine block objective tracker.
     */
    private static final Map<Block, Map<UUID, IQuestInstance>> breakBlockObjectives = new HashMap<>();

    /**
     * Entity kill objective tracker.
     */
    private static final Map<EntityType<?>, Map<UUID, IQuestInstance>> entityKillObjectives = new HashMap<>();

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

        final Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
        if (breakBlockObjectives.containsKey(block) && breakBlockObjectives.get(block).containsKey(event.getPlayer().getUUID()))
        {
            final IQuestInstance colonyQuest = breakBlockObjectives.get(block).get(event.getPlayer().getUUID());
            final IQuestObjectiveTemplate objective = IQuestManager.GLOBAL_SERVER_QUESTS.get(colonyQuest.getId()).getObjective(colonyQuest.getIndex());
            if (objective instanceof IBreakBlockObjectiveTemplate)
            {
                ((IBreakBlockObjectiveTemplate) objective).onBlockBreak(colonyQuest.getObjectiveData(), colonyQuest, event.getPlayer());
            }
            else
            {
                breakBlockObjectives.get(block).remove(event.getPlayer().getUUID());
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
            final IQuestInstance colonyQuest = entityKillObjectives.get(event.getEntity().getType()).get(event.getSource().getEntity().getUUID());
            final IQuestObjectiveTemplate objective = IQuestManager.GLOBAL_SERVER_QUESTS.get(colonyQuest.getId()).getObjective(colonyQuest.getIndex());
            if (objective instanceof IKillEntityObjectiveTemplate)
            {
                ((IKillEntityObjectiveTemplate) objective).onEntityKill(colonyQuest.getObjectiveData(), colonyQuest, (Player) event.getSource().getEntity());
            }
            else
            {
                entityKillObjectives.get(event.getEntity().getType()).remove(event.getEntity().getUUID());
            }
        }
    }

    /**
     * Add an objective listener to this event handler.
     *
     * @param blockToMine    the block that we listen for.
     * @param assignedPlayer the player we check for.
     * @param colonyQuest    the colony quest it is related to.
     */
    public static void addQuestObjectiveListener(final Block blockToMine, final UUID assignedPlayer, final IQuestInstance colonyQuest)
    {
        final Map<UUID, IQuestInstance> currentMap = breakBlockObjectives.getOrDefault(blockToMine, new HashMap<>());
        currentMap.put(assignedPlayer, colonyQuest);
        breakBlockObjectives.put(blockToMine, currentMap);
    }

    /**
     * Remove an objective listener to this event handler.
     *
     * @param blockToMine    the block that we listen for.
     * @param assignedPlayer the player we check for.
     * @param colonyQuest    the colony quest it is related to.
     */
    public static void removeQuestObjectiveListener(final Block blockToMine, final UUID assignedPlayer, final IQuestInstance colonyQuest)
    {
        breakBlockObjectives.getOrDefault(blockToMine, new HashMap<>()).remove(assignedPlayer);
    }

    /**
     * Add an objective listener to this event handler.
     *
     * @param entityToKill   the entity type that we listen for.
     * @param assignedPlayer the player we check for.
     * @param colonyQuest    the colony quest it is related to.
     */
    public static void addQuestObjectiveListener(final EntityType<?> entityToKill, final UUID assignedPlayer, final IQuestInstance colonyQuest)
    {
        final Map<UUID, IQuestInstance> currentMap = entityKillObjectives.getOrDefault(entityToKill, new HashMap<>());
        currentMap.put(assignedPlayer, colonyQuest);
        entityKillObjectives.put(entityToKill, currentMap);
    }

    /**
     * Remove an objective listener to this event handler.
     *
     * @param entityToKill   the entity type that we listen for.
     * @param assignedPlayer the player we check for.
     * @param colonyQuest    the colony quest it is related to.
     */
    public static void removeQuestObjectiveListener(final EntityType<?> entityToKill, final UUID assignedPlayer, final IQuestInstance colonyQuest)
    {
        entityKillObjectives.getOrDefault(entityToKill, new HashMap<>()).remove(assignedPlayer);
    }
}
