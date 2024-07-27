package com.minecolonies.apiimp.initializer;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.interactionhandling.InteractionValidatorRegistry;
import com.minecolonies.api.colony.requestsystem.request.RequestUtils;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.happiness.ITimeBasedHappinessModifier;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.ItemListModule;
import com.minecolonies.core.colony.buildings.modules.QuarryModule;
import com.minecolonies.core.colony.buildings.workerbuildings.*;
import com.minecolonies.core.colony.jobs.*;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.minecolonies.core.util.WorkerUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import java.util.List;

import static com.minecolonies.api.util.ItemStackUtils.*;
import static com.minecolonies.api.util.constant.BuildingConstants.BUILDING_FLOWER_LIST;
import static com.minecolonies.api.util.constant.CitizenConstants.LOW_SATURATION;
import static com.minecolonies.api.util.constant.HappinessConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants.REQUEST_RESOLVER_NORMAL;
import static com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants.REQUEST_SYSTEM_BUILDING_LEVEL_TOO_LOW;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.ITEMLIST_FOODEXCLUSION;
import static com.minecolonies.core.entity.ai.workers.crafting.EntityAIWorkSmelter.ORE_LIST;
import static com.minecolonies.core.util.WorkerUtil.getLastLadder;
import static com.minecolonies.core.util.WorkerUtil.isThereCompostedLand;

/**
 * Class containing initializer for all the validator predicates.
 */
public class InteractionValidatorInitializer
{
    /**
     * Init method called on startup.
     */
    public static void init()
    {
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(FURNACE_USER_NO_FUEL),
          citizen -> citizen.getWorkBuilding() != null && citizen.getWorkBuilding().hasModule(BuildingModules.FURNACE) && citizen.getWorkBuilding().hasModule(BuildingModules.ITEMLIST_FUEL)
                       && citizen.getWorkBuilding().getModule(BuildingModules.ITEMLIST_FUEL).getList().isEmpty());
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(BAKER_HAS_NO_FURNACES_MESSAGE),
          citizen -> citizen.getWorkBuilding() != null && citizen.getWorkBuilding().hasModule(BuildingModules.FURNACE) && citizen.getWorkBuilding().getModule(BuildingModules.FURNACE).getFurnaces().isEmpty());
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(RAW_FOOD),
          citizen -> InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(citizen.getInventory(), ISCOOKABLE) != -1
                       && InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(citizen.getInventory(), stack -> CAN_EAT.test(stack) && (citizen.getWorkBuilding() == null || citizen.getWorkBuilding().canEat(stack))) == -1);
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(BETTER_FOOD),
          citizen -> citizen.getSaturation() == 0 && !citizen.isChild() && citizen.needsBetterFood());
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(BETTER_FOOD_CHILDREN),
          citizen -> citizen.getSaturation() == 0 && citizen.isChild() && citizen.needsBetterFood());
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(NO_RESTAURANT),
          citizen -> citizen.getColony() != null && citizen.getSaturation() <= LOW_SATURATION && citizen.getEntity().isPresent()
                       && citizen.getColony().getBuildingManager().getBestBuilding(citizen.getEntity().get(), BuildingCook.class) == null
                       && InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(citizen.getInventory(), ISFOOD) == -1);
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(NO_HOSPITAL),
          citizen -> citizen.getColony() != null && citizen.getEntity().isPresent() && citizen.getEntity().get().getCitizenDiseaseHandler().isSick()
                       && citizen.getColony().getBuildingManager().getBestBuilding(citizen.getEntity().get(), BuildingHospital.class) == null);
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(WAITING_FOR_CURE),
          citizen -> citizen.getColony() != null && citizen.getEntity().isPresent() && !citizen.getEntity().get().getCitizenDiseaseHandler().getDisease().isEmpty());

        InteractionValidatorRegistry.registerPosBasedPredicate(Component.translatableEscape(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_CHESTFULL),
          (citizen, pos) ->
          {
              if (citizen.getJob() instanceof JobDeliveryman)
              {
                  final IColony colony = citizen.getColony();
                  if (colony != null)
                  {
                      final IBuilding building = colony.getBuildingManager().getBuilding(pos);
                      if (building != null)
                      {
                          final IItemHandler inv = building.getItemHandlerCap();
                          if (inv != null)
                          {
                              return InventoryUtils.openSlotCount(inv) <= 0;
                          }
                      }
                  }
              }
              return false;
          });
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_NOWAREHOUSE),
          cit -> {
              if (cit.getJob() instanceof JobDeliveryman && cit.getWorkBuilding() != null)
              {
                  return ((JobDeliveryman) cit.getJob()).findWareHouse() == null;
              }
              return false;
          });

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(NO_FREE_FIELDS),
          citizen -> citizen.getWorkBuilding() instanceof BuildingFarmer && citizen.getWorkBuilding().getModule(BuildingModules.FARMER_FIELDS).hasNoFields());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(INVALID_MINESHAFT),
          citizen -> citizen.getWorkBuilding() instanceof BuildingMiner && citizen.getJob() instanceof JobMiner && (((BuildingMiner) citizen.getWorkBuilding()).getCobbleLocation() == null || ((BuildingMiner) citizen.getWorkBuilding()).getLadderLocation() == null));

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(COM_MINECOLONIES_COREMOD_ENTITY_WORKER_INVENTORYFULLCHEST),
          citizen -> citizen.getWorkBuilding() != null && InventoryUtils.isBuildingFull(citizen.getWorkBuilding()));
        InteractionValidatorRegistry.registerPosBasedPredicate(
          Component.translatableEscape(REQUEST_SYSTEM_BUILDING_LEVEL_TOO_LOW), (citizen, pos) ->
          {
              final IBuilding workBuilding = citizen.getWorkBuilding();
              if (workBuilding != null)
              {
                  final IColony colony = citizen.getColony();
                  if (colony != null)
                  {
                      final Level world = colony.getWorld();
                      if (world != null)
                      {
                          return workBuilding.getMaxToolLevel() < WorkerUtil.getCorrectHarvestLevelForBlock(world.getBlockState(pos));
                      }
                  }
              }
              return false;
          });
        InteractionValidatorRegistry.registerTokenBasedPredicate(Component.translatableEscape(REQUEST_RESOLVER_NORMAL),
          (citizen, token) -> {

              final IColony colony = citizen.getColony();
              if (colony != null)
              {
                  return RequestUtils.requestChainNeedsPlayer(token, citizen.getColony().getRequestManager());
              }
              return false;
          });

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(FURNACE_USER_NO_ORE),
          citizen -> {
            if (citizen.getWorkBuilding() instanceof BuildingSmeltery)
            {
                final List<ItemStorage> oreList = ((BuildingSmeltery) citizen.getWorkBuilding()).getModuleMatching(ItemListModule.class, m -> m.getId().equals(ORE_LIST)).getList();
                for (final ItemStorage storage : IColonyManager.getInstance().getCompatibilityManager().getSmeltableOres())
                {
                    if (!oreList.contains(storage))
                    {
                        return true;
                    }
                }
            }
            return false;

          });

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(PATIENT_FULL_INVENTORY),
          citizen -> citizen.getEntity().isPresent() && citizen.getEntity().get().getCitizenDiseaseHandler().isSick()
                       && InventoryUtils.isItemHandlerFull(citizen.getEntity().get().getInventoryCitizen()));

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(PUPIL_NO_CARPET),
          citizen -> citizen.getEntity().isPresent() && citizen.isChild() && citizen.getWorkBuilding() instanceof BuildingSchool
                       && ((BuildingSchool) citizen.getWorkBuilding()).getRandomPlaceToSit() == null);

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(WATER_TOO_FAR),
          citizen -> citizen.getJob() instanceof JobFisherman && ((JobFisherman) citizen.getJob()).getPonds().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(FURNACE_USER_NO_FUEL),
          citizen -> citizen.getWorkBuilding() != null && citizen.getWorkBuilding().hasModule(BuildingModules.FURNACE) && citizen.getWorkBuilding().hasModule(BuildingModules.ITEMLIST_FUEL) && citizen.getWorkBuilding().getModule(BuildingModules.ITEMLIST_FUEL).getList().isEmpty());
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(FURNACE_USER_NO_FOOD),
          citizen -> {
            if (!(citizen.getWorkBuilding() instanceof BuildingCook))
            {
                return false;
            }

            final ImmutableList<ItemStorage> exclusionList = ((BuildingCook) citizen.getWorkBuilding()).getModule(ITEMLIST_FOODEXCLUSION).getList();
            for (final ItemStorage storage : IColonyManager.getInstance().getCompatibilityManager().getEdibles(citizen.getWorkBuilding().getBuildingLevel() - 1))
            {
                if (!exclusionList.contains(storage))
                {
                    return false;
                }
            }

            return true;
          });
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(SIFTER_NO_MESH),
          citizen -> {
            if (!(citizen.getWorkBuilding() instanceof BuildingSifter))
            {
                return false;
            }
            return InventoryUtils.getItemCountInProvider(citizen.getWorkBuilding(), item -> item.is(ModTags.meshes)) <= 0 &&
                   InventoryUtils.getItemCountInItemHandler(citizen.getInventory(), item -> item.is(ModTags.meshes)) <= 0;
          });
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(BAKER_HAS_NO_FURNACES_MESSAGE),
          citizen -> citizen.getWorkBuilding() instanceof BuildingBaker && citizen.getWorkBuilding().getModule(BuildingModules.FURNACE).getFurnaces().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(NO_HIVES),
          citizen -> citizen.getWorkBuilding() instanceof BuildingBeekeeper && ((BuildingBeekeeper) citizen.getWorkBuilding()).getHives().isEmpty());
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(NO_BEES),
          citizen -> citizen.getWorkBuilding() instanceof BuildingBeekeeper && citizen.getJob(JobBeekeeper.class).checkForBeeInteraction());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(NO_WORKERS_TO_DRAIN_SET),
          citizen -> citizen.getWorkBuilding() instanceof BuildingEnchanter && ((BuildingEnchanter) citizen.getWorkBuilding()).getModule(BuildingModules.ENCHANTER_STATIONS).getBuildingsToGatherFrom().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(NO_PLANT_GROUND_FLORIST),
          citizen -> citizen.getWorkBuilding() instanceof BuildingFlorist && ((BuildingFlorist) citizen.getWorkBuilding()).getPlantGround().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(NO_FLOWERS_IN_CONFIG),
          citizen -> citizen.getWorkBuilding() instanceof BuildingFlorist && ItemStackUtils.isEmpty(((BuildingFlorist) citizen.getWorkBuilding()).getFlowerToGrow()));

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(NO_COMPOST),
          citizen ->
          {
              final IBuilding buildingFlorist = citizen.getWorkBuilding();
              if (buildingFlorist instanceof BuildingFlorist && buildingFlorist.getColony().getWorld() != null)
              {
                  return InventoryUtils.getItemCountInItemHandler(citizen.getInventory(), IS_COMPOST) == 0 && !isThereCompostedLand((BuildingFlorist) buildingFlorist,
                    buildingFlorist.getColony().getWorld());
              }
              return false;
          });

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(NEEDS_BETTER_HUT),
          citizen -> {

              final AbstractBuilding buildingMiner = (AbstractBuilding) citizen.getWorkBuilding();
              if (buildingMiner instanceof BuildingMiner && citizen.getColony() != null && citizen.getColony().getWorld() != null && citizen.getJob() instanceof JobMiner)
              {
                  return getLastLadder(((BuildingMiner) buildingMiner).getLadderLocation(), citizen.getColony().getWorld()) < ((BuildingMiner) buildingMiner).getDepthLimit(citizen.getColony().getWorld())
                           && ((BuildingMiner) buildingMiner).getModule(BuildingModules.MINER_LEVELS).getNumberOfLevels() == 0;
              }
              return false;
          });

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(WORKER_AI_EXCEPTION),
          citizen -> citizen.getJob() != null && ((AbstractEntityAIBasic<?, ?>) citizen.getJob().getWorkerAI()).getExceptionTimer() > 1);

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(DEMANDS + HOMELESSNESS),
          citizen -> ((ITimeBasedHappinessModifier)(citizen.getCitizenHappinessHandler()).getModifier(HOMELESSNESS)).getDays() > DEMANDS_DAYS_WITHOUT_HOUSE && citizen.getHomeBuilding() == null);

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(NO + HOMELESSNESS),
          citizen -> ((ITimeBasedHappinessModifier)(citizen.getCitizenHappinessHandler()).getModifier(HOMELESSNESS)).getDays() > COMPLAIN_DAYS_WITHOUT_HOUSE
                       && ((ITimeBasedHappinessModifier)(citizen.getCitizenHappinessHandler()).getModifier(HOMELESSNESS)).getDays() <= DEMANDS_DAYS_WITHOUT_HOUSE && citizen.getHomeBuilding() == null);

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(DEMANDS + UNEMPLOYMENT),
          citizen -> ((ITimeBasedHappinessModifier)(citizen.getCitizenHappinessHandler()).getModifier(UNEMPLOYMENT)).getDays() > DEMANDS_DAYS_WITHOUT_JOB && citizen.getJob() == null);
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(NO + UNEMPLOYMENT),
          citizen -> ((ITimeBasedHappinessModifier)(citizen.getCitizenHappinessHandler()).getModifier(UNEMPLOYMENT)).getDays() > COMPLAIN_DAYS_WITHOUT_JOB
                       && ((ITimeBasedHappinessModifier)(citizen.getCitizenHappinessHandler()).getModifier(UNEMPLOYMENT)).getDays() <= DEMANDS_DAYS_WITHOUT_JOB && citizen.getJob() == null);

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(DEMANDS + IDLEATJOB),
          citizen -> citizen.getJob() != null && ((ITimeBasedHappinessModifier)(citizen.getCitizenHappinessHandler()).getModifier(IDLEATJOB)).getDays() > citizen.getJob().getIdleSeverity(true));
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(NO + IDLEATJOB),
          citizen -> citizen.getJob() != null && ((ITimeBasedHappinessModifier)citizen.getCitizenHappinessHandler().getModifier(IDLEATJOB)).getDays() > citizen.getJob().getIdleSeverity(false)
                       && ((ITimeBasedHappinessModifier)citizen.getCitizenHappinessHandler().getModifier(IDLEATJOB)).getDays() <= citizen.getJob().getIdleSeverity(true));

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(NO + SLEPTTONIGHT),
          citizen -> !(citizen.getJob() instanceof AbstractJobGuard) && ((ITimeBasedHappinessModifier)citizen.getCitizenHappinessHandler().getModifier(SLEPTTONIGHT)).getDays() <= 0);

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO + HADDECENTFOOD),
          citizen -> ((ITimeBasedHappinessModifier)citizen.getCitizenHappinessHandler().getModifier(HADDECENTFOOD)).getDays() <= 0);


        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(COM_MINECOLONIES_COREMOD_BEEKEEPER_NOFLOWERS),
          citizen -> citizen.getWorkBuilding() instanceof BuildingBeekeeper
                       && ((BuildingBeekeeper) citizen.getWorkBuilding()).getModuleMatching(ItemListModule.class, m -> m.getId().equals(BUILDING_FLOWER_LIST)).getList().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_RAINING),
          citizen -> citizen.getEntity().isPresent() && citizen.getEntity().get().getCommandSenderWorld().isRaining()
                       && !citizen.getColony().getRaiderManager().isRaided()
                       && !citizen.getCitizenMournHandler().isMourning());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_RAID),
          citizen -> citizen.getEntity().isPresent() && citizen.getColony().getRaiderManager().isRaided());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_SLEEPING),
          citizen -> citizen.getEntity().isPresent() && citizen.isAsleep());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_MOURNING),
          citizen -> citizen.getEntity().isPresent() && citizen.getCitizenMournHandler().isMourning()
                     && !citizen.getColony().getRaiderManager().isRaided());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(CITIZEN_NOT_GUARD_NEAR_WORK),
          citizen -> citizen.getWorkBuilding() != null && !citizen.getWorkBuilding().isGuardBuildingNear());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(CITIZEN_NOT_GUARD_NEAR_HOME),
          citizen -> citizen.getHomeBuilding() != null && !citizen.getHomeBuilding().isGuardBuildingNear());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(QUARRY_MINER_NO_QUARRY),
          citizen -> citizen.getJob() instanceof JobQuarrier &&  ((JobQuarrier) citizen.getJob()).findQuarry() == null);

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatableEscape(QUARRY_MINER_FINISHED_QUARRY),
          citizen -> citizen.getJob() instanceof JobQuarrier &&  ((JobQuarrier) citizen.getJob()).findQuarry() != null && ((JobQuarrier) citizen.getJob()).findQuarry().getFirstModuleOccurance(QuarryModule.class).isFinished());
    }
}
