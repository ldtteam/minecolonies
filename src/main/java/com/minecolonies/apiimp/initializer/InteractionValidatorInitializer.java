package com.minecolonies.apiimp.initializer;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.interactionhandling.InteractionValidatorRegistry;
import com.minecolonies.api.colony.requestsystem.request.RequestUtils;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.buildings.modules.*;
import com.minecolonies.coremod.colony.buildings.workerbuildings.*;
import com.minecolonies.coremod.colony.jobs.*;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIBasic;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

import static com.minecolonies.api.util.ItemStackUtils.*;
import static com.minecolonies.api.util.constant.BuildingConstants.BUILDING_FLOWER_LIST;
import static com.minecolonies.api.util.constant.BuildingConstants.FUEL_LIST;
import static com.minecolonies.api.util.constant.CitizenConstants.LOW_SATURATION;
import static com.minecolonies.api.util.constant.HappinessConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants.REQUEST_RESOLVER_NORMAL;
import static com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants.REQUEST_SYSTEM_BUILDING_LEVEL_TOO_LOW;
import static com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook.FOOD_EXCLUSION_LIST;
import static com.minecolonies.coremod.entity.ai.citizen.smelter.EntityAIWorkSmelter.ORE_LIST;
import static com.minecolonies.coremod.util.WorkerUtil.getLastLadder;
import static com.minecolonies.coremod.util.WorkerUtil.isThereCompostedLand;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

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
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(FURNACE_USER_NO_FUEL),
          citizen -> citizen.getWorkBuilding() != null && citizen.getWorkBuilding().hasModule(FurnaceUserModule.class)
                       && citizen.getWorkBuilding().getModuleMatching(ItemListModule.class, m -> m.getId().equals(FUEL_LIST)).getList().isEmpty());
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(BAKER_HAS_NO_FURNACES_MESSAGE),
          citizen -> citizen.getWorkBuilding() != null && citizen.getWorkBuilding().hasModule(FurnaceUserModule.class) && citizen.getWorkBuilding().getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces().isEmpty());
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(RAW_FOOD),
          citizen -> InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(citizen.getInventory(), ISCOOKABLE) != -1
                       && InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(citizen.getInventory(), stack -> CAN_EAT.test(stack) && (citizen.getWorkBuilding() == null || citizen.getWorkBuilding().canEat(stack))) == -1);
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(BETTER_FOOD),
          citizen -> citizen.getSaturation() == 0 && !citizen.isChild() && citizen.needsBetterFood());
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(BETTER_FOOD_CHILDREN),
          citizen -> citizen.getSaturation() == 0 && citizen.isChild() && citizen.needsBetterFood());
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO_RESTAURANT),
          citizen -> citizen.getColony() != null && citizen.getSaturation() <= LOW_SATURATION && citizen.getEntity().isPresent()
                       && citizen.getColony().getBuildingManager().getBestBuilding(citizen.getEntity().get(), BuildingCook.class) == null
                       && InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(citizen.getInventory(), ISFOOD) == -1);
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO_HOSPITAL),
          citizen -> citizen.getColony() != null && citizen.getEntity().isPresent() && citizen.getEntity().get().getCitizenDiseaseHandler().isSick()
                       && citizen.getColony().getBuildingManager().getBestBuilding(citizen.getEntity().get(), BuildingHospital.class) == null);
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(WAITING_FOR_CURE),
          citizen -> citizen.getColony() != null && citizen.getEntity().isPresent() && !citizen.getEntity().get().getCitizenDiseaseHandler().getDisease().isEmpty());

        InteractionValidatorRegistry.registerPosBasedPredicate(Component.translatable(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_CHESTFULL),
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
                          final IItemHandler inv = building.getCapability(ForgeCapabilities.ITEM_HANDLER, null).resolve().orElse(null);
                          if (inv != null)
                          {
                              return InventoryUtils.openSlotCount(inv) <= 0;
                          }
                      }
                  }
              }
              return false;
          });
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_NOWAREHOUSE),
          cit -> {
              if (cit.getJob() instanceof final JobDeliveryman deliveryman && cit.getWorkBuilding() != null)
              {
                  return deliveryman.findWareHouse() == null;
              }
              return false;
          });

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO_FREE_FIELDS),
          citizen -> citizen.getWorkBuilding() instanceof final BuildingFarmer farmer && farmer.getFirstModuleOccurance(FarmerFieldModule.class).hasNoFields());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(INVALID_MINESHAFT),
          citizen -> citizen.getWorkBuilding() instanceof final BuildingMiner miner && citizen.getJob() instanceof JobMiner && (miner.getCobbleLocation() == null || miner.getLadderLocation() == null));

        InteractionValidatorRegistry.registerPosBasedPredicate(Component.translatable(NO_SEED_SET),
          (citizen, pos) ->
          {
              if (citizen.getJob() instanceof JobFarmer)
              {
                  final IColony colony = citizen.getColony();
                  if (colony != null)
                  {
                      final Level world = colony.getWorld();
                      if (world != null)
                      {
                          if (world.getBlockEntity(pos) instanceof final ScarecrowTileEntity scarecrow)
                          {
                              return scarecrow.getSeed() == null;
                          }
                      }
                  }
              }
              return false;
          });

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(COM_MINECOLONIES_COREMOD_ENTITY_WORKER_INVENTORYFULLCHEST),
          citizen -> citizen.getWorkBuilding() != null && InventoryUtils.isProviderFull(citizen.getWorkBuilding()));
        InteractionValidatorRegistry.registerPosBasedPredicate(
          Component.translatable(REQUEST_SYSTEM_BUILDING_LEVEL_TOO_LOW), (citizen, pos) ->
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
        InteractionValidatorRegistry.registerTokenBasedPredicate(Component.translatable(REQUEST_RESOLVER_NORMAL),
          (citizen, token) -> {

              final IColony colony = citizen.getColony();
              if (colony != null)
              {
                  return RequestUtils.requestChainNeedsPlayer(token, citizen.getColony().getRequestManager());
              }
              return false;
          });

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(FURNACE_USER_NO_ORE),
          citizen -> {
            if (citizen.getWorkBuilding() instanceof final BuildingSmeltery smeltery)
            {
                final List<ItemStorage> oreList = smeltery.getModuleMatching(ItemListModule.class, m -> m.getId().equals(ORE_LIST)).getList();
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

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(PATIENT_FULL_INVENTORY),
          citizen -> citizen.getEntity().isPresent() && citizen.getEntity().get().getCitizenDiseaseHandler().isSick()
                       && InventoryUtils.isItemHandlerFull(citizen.getEntity().get().getInventoryCitizen()));

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(PUPIL_NO_CARPET),
          citizen -> citizen.getEntity().isPresent() && citizen.isChild() && citizen.getWorkBuilding() instanceof final BuildingSchool school
                       && school.getRandomPlaceToSit() == null);

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(WATER_TOO_FAR),
          citizen -> citizen.getJob() instanceof final JobFisherman fisherman && fisherman.getPonds().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(FURNACE_USER_NO_FUEL),
          citizen -> citizen.getWorkBuilding() != null && citizen.getWorkBuilding().hasModule(FurnaceUserModule.class) && citizen.getWorkBuilding().getModuleMatching(ItemListModule.class, m -> m.getId().equals(FUEL_LIST)).getList().isEmpty());
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(FURNACE_USER_NO_FOOD),
          citizen -> {
            if (!(citizen.getWorkBuilding() instanceof final BuildingCook cook))
            {
                return false;
            }

            final ImmutableList<ItemStorage> exclusionList = cook.getModuleMatching(ItemListModule.class, m -> m.getId().equals(FOOD_EXCLUSION_LIST)).getList();
            for (final ItemStorage storage : IColonyManager.getInstance().getCompatibilityManager().getEdibles(citizen.getWorkBuilding().getBuildingLevel() - 1))
            {
                if (!exclusionList.contains(storage))
                {
                    return false;
                }
            }

            return true;
          });
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(SIFTER_NO_MESH),
          citizen -> {
            if (!(citizen.getWorkBuilding() instanceof BuildingSifter))
            {
                return false;
            }
            return InventoryUtils.getItemCountInProvider(citizen.getWorkBuilding(), item -> item.is(ModTags.meshes)) <= 0 &&
                   InventoryUtils.getItemCountInItemHandler(citizen.getInventory(), item -> item.is(ModTags.meshes)) <= 0;
          });
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(BAKER_HAS_NO_FURNACES_MESSAGE),
          citizen -> citizen.getWorkBuilding() instanceof BuildingBaker && citizen.getWorkBuilding().getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO_HIVES),
          citizen -> citizen.getWorkBuilding() instanceof final BuildingBeekeeper beekeeper && beekeeper.getHives().isEmpty());
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO_BEES),
          citizen -> citizen.getWorkBuilding() instanceof BuildingBeekeeper && citizen.getJob(JobBeekeeper.class).checkForBeeInteraction());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO_WORKERS_TO_DRAIN_SET),
          citizen -> citizen.getWorkBuilding() instanceof final BuildingEnchanter enchanter && enchanter.getFirstModuleOccurance(EnchanterStationsModule.class).getBuildingsToGatherFrom().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO_PLANT_GROUND_FLORIST),
          citizen -> citizen.getWorkBuilding() instanceof final BuildingFlorist florist && florist.getPlantGround().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO_FLOWERS_IN_CONFIG),
          citizen -> citizen.getWorkBuilding() instanceof final BuildingFlorist florist && ItemStackUtils.isEmpty(florist.getFlowerToGrow()));

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO_COMPOST),
          citizen ->
          {
              if (citizen.getWorkBuilding() instanceof final BuildingFlorist florist && florist.getColony().getWorld() != null)
              {
                  return InventoryUtils.getItemCountInItemHandler(citizen.getInventory(), IS_COMPOST) == 0 && !isThereCompostedLand(florist,
                    florist.getColony().getWorld());
              }
              return false;
          });

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NEEDS_BETTER_HUT),
          citizen -> {

              if (citizen.getWorkBuilding() instanceof final BuildingMiner miner && citizen.getColony() != null && citizen.getColony().getWorld() != null && citizen.getJob() instanceof JobMiner)
              {
                  return getLastLadder(miner.getLadderLocation(), citizen.getColony().getWorld()) < miner.getDepthLimit(citizen.getColony().getWorld())
                           && miner.getFirstModuleOccurance(MinerLevelManagementModule.class).getNumberOfLevels() == 0;
              }
              return false;
          });

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(WORKER_AI_EXCEPTION),
          citizen -> citizen.getJob() != null && ((AbstractEntityAIBasic<?, ?>) citizen.getJob().getWorkerAI()).getExceptionTimer() > 1);

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(DEMANDS + HOMELESSNESS),
          citizen -> (citizen.getCitizenHappinessHandler()).getModifier(HOMELESSNESS).getDays() > DEMANDS_DAYS_WITHOUT_HOUSE && citizen.getHomeBuilding() == null);

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO + HOMELESSNESS),
          citizen -> (citizen.getCitizenHappinessHandler()).getModifier(HOMELESSNESS).getDays() > COMPLAIN_DAYS_WITHOUT_HOUSE
                       && (citizen.getCitizenHappinessHandler()).getModifier(HOMELESSNESS).getDays() <= DEMANDS_DAYS_WITHOUT_HOUSE && citizen.getHomeBuilding() == null);

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(DEMANDS + UNEMPLOYMENT),
          citizen -> (citizen.getCitizenHappinessHandler()).getModifier(UNEMPLOYMENT).getDays() > DEMANDS_DAYS_WITHOUT_JOB && citizen.getJob() == null);
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO + UNEMPLOYMENT),
          citizen -> (citizen.getCitizenHappinessHandler()).getModifier(UNEMPLOYMENT).getDays() > COMPLAIN_DAYS_WITHOUT_JOB
                       && (citizen.getCitizenHappinessHandler()).getModifier(UNEMPLOYMENT).getDays() <= DEMANDS_DAYS_WITHOUT_JOB && citizen.getJob() == null);

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(DEMANDS + IDLEATJOB),
          citizen -> citizen.getJob() != null && (citizen.getCitizenHappinessHandler()).getModifier(IDLEATJOB).getDays() > citizen.getJob().getIdleSeverity(true));
        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO + IDLEATJOB),
          citizen -> citizen.getJob() != null && citizen.getCitizenHappinessHandler().getModifier(IDLEATJOB).getDays() > citizen.getJob().getIdleSeverity(false)
                       && citizen.getCitizenHappinessHandler().getModifier(IDLEATJOB).getDays() <= citizen.getJob().getIdleSeverity(true));

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(NO + SLEPTTONIGHT),
          citizen -> !(citizen.getJob() instanceof AbstractJobGuard) && citizen.getCitizenHappinessHandler().getModifier(SLEPTTONIGHT).getDays() <= 0);

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(COM_MINECOLONIES_COREMOD_BEEKEEPER_NOFLOWERS),
          citizen -> citizen.getWorkBuilding() instanceof final BuildingBeekeeper beekeeper
                       && beekeeper.getModuleMatching(ItemListModule.class, m -> m.getId().equals(BUILDING_FLOWER_LIST)).getList().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_RAINING),
          citizen -> citizen.getEntity().isPresent() && citizen.getEntity().get().getCommandSenderWorld().isRaining()
                       && !citizen.getColony().getRaiderManager().isRaided()
                       && !citizen.getCitizenMournHandler().isMourning());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_RAID),
          citizen -> citizen.getEntity().isPresent() && citizen.getColony().getRaiderManager().isRaided());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_SLEEPING),
          citizen -> citizen.getEntity().isPresent() && citizen.isAsleep());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_MOURNING),
          citizen -> citizen.getEntity().isPresent() && citizen.getCitizenMournHandler().isMourning()
                     && !citizen.getColony().getRaiderManager().isRaided());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(CITIZEN_NOT_GUARD_NEAR_WORK),
          citizen -> citizen.getWorkBuilding() != null && !citizen.getWorkBuilding().isGuardBuildingNear());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(CITIZEN_NOT_GUARD_NEAR_HOME),
          citizen -> citizen.getHomeBuilding() != null && !citizen.getHomeBuilding().isGuardBuildingNear());

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(QUARRY_MINER_NO_QUARRY),
          citizen -> citizen.getJob() instanceof final JobQuarrier quarrier && quarrier.findQuarry() == null);

        InteractionValidatorRegistry.registerStandardPredicate(Component.translatable(QUARRY_MINER_FINISHED_QUARRY),
          citizen -> citizen.getJob() instanceof final JobQuarrier quarrier && quarrier.findQuarry() != null && quarrier.findQuarry().getFirstModuleOccurance(QuarryModule.class).isFinished());
    }
}
