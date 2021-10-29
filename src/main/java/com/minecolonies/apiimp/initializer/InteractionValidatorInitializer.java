package com.minecolonies.apiimp.initializer;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.buildings.workerbuildings.IBuildingDeliveryman;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.interactionhandling.InteractionValidatorRegistry;
import com.minecolonies.api.colony.requestsystem.request.RequestUtils;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.*;
import com.minecolonies.coremod.colony.buildings.workerbuildings.*;
import com.minecolonies.coremod.colony.jobs.*;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIBasic;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

import static com.minecolonies.api.util.ItemStackUtils.*;
import static com.minecolonies.api.util.constant.BuildingConstants.BUILDING_FLOWER_LIST;
import static com.minecolonies.api.util.constant.BuildingConstants.FUEL_LIST;
import static com.minecolonies.api.util.constant.CitizenConstants.LOW_SATURATION;
import static com.minecolonies.api.util.constant.HappinessConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook.FOOD_EXCLUSION_LIST;
import static com.minecolonies.coremod.entity.ai.citizen.smelter.EntityAIWorkSmelter.ORE_LIST;
import static com.minecolonies.coremod.util.WorkerUtil.getLastLadder;
import static com.minecolonies.coremod.util.WorkerUtil.isThereCompostedLand;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

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
        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(FURNACE_USER_NO_FUEL),
          citizen -> citizen.getWorkBuilding() != null && citizen.getWorkBuilding().hasModule(FurnaceUserModule.class)
                       && citizen.getWorkBuilding().getModuleMatching(ItemListModule.class, m -> m.getId().equals(FUEL_LIST)).getList().isEmpty());
        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(BAKER_HAS_NO_FURNACES_MESSAGE),
          citizen -> citizen.getWorkBuilding() != null && citizen.getWorkBuilding().hasModule(FurnaceUserModule.class) && citizen.getWorkBuilding().getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces().isEmpty());
        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(RAW_FOOD),
          citizen -> InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(citizen.getInventory(), ISCOOKABLE) != -1
                       && InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(citizen.getInventory(), stack -> CAN_EAT.test(stack) && (citizen.getWorkBuilding() == null || citizen.getWorkBuilding().canEat(stack))) == -1);
        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(BETTER_FOOD),
          citizen -> citizen.getSaturation() == 0 && !citizen.isChild() && citizen.needsBetterFood());
        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(BETTER_FOOD_CHILDREN),
          citizen -> citizen.getSaturation() == 0 && citizen.isChild() && citizen.needsBetterFood());
        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(NO_RESTAURANT),
          citizen -> citizen.getColony() != null && citizen.getSaturation() <= LOW_SATURATION && citizen.getEntity().isPresent()
                       && citizen.getColony().getBuildingManager().getBestBuilding(citizen.getEntity().get(), BuildingCook.class) == null
                       && InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(citizen.getInventory(), ISFOOD) == -1);
        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(NO_HOSPITAL),
          citizen -> citizen.getColony() != null && citizen.getEntity().isPresent() && citizen.getEntity().get().getCitizenDiseaseHandler().isSick()
                       && citizen.getColony().getBuildingManager().getBestBuilding(citizen.getEntity().get(), BuildingHospital.class) == null);
        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(WAITING_FOR_CURE),
          citizen -> citizen.getColony() != null && citizen.getEntity().isPresent() && !citizen.getEntity().get().getCitizenDiseaseHandler().getDisease().isEmpty());

        InteractionValidatorRegistry.registerPosBasedPredicate(new TranslatableComponent(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_CHESTFULL),
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
                          final IItemHandler inv = building.getCapability(ITEM_HANDLER_CAPABILITY, null).resolve().orElse(null);
                          if (inv != null)
                          {
                              return InventoryUtils.openSlotCount(inv) > 0;
                          }
                      }
                  }
              }
              return false;
          });
        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_NOWAREHOUSE),
          cit -> {
              if (cit.getJob() instanceof JobDeliveryman && cit.getWorkBuilding() != null)
              {
                  for (final IWareHouse wareHouse : cit.getJob().getColony().getBuildingManager().getWareHouses())
                  {
                      if (wareHouse.registerWithWareHouse((IBuildingDeliveryman) cit.getWorkBuilding()))
                      {
                          return false;
                      }
                  }
                  return true;
              }
              return false;
          });

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(NO_FREE_FIELDS),
          citizen -> citizen.getWorkBuilding() instanceof BuildingFarmer && ((BuildingFarmer) citizen.getWorkBuilding()).getFirstModuleOccurance(FarmerFieldModule.class).hasNoFields());

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(INVALID_MINESHAFT),
          citizen -> citizen.getWorkBuilding() instanceof BuildingMiner && (((BuildingMiner) citizen.getWorkBuilding()).getCobbleLocation() == null || ((BuildingMiner) citizen.getWorkBuilding()).getLadderLocation() == null));

        InteractionValidatorRegistry.registerPosBasedPredicate(new TranslatableComponent(NO_SEED_SET),
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
                          final BlockEntity tileEntity = world.getBlockEntity(pos);
                          if (tileEntity instanceof ScarecrowTileEntity)
                          {
                              return ((ScarecrowTileEntity) tileEntity).getSeed() == null;
                          }
                      }
                  }
              }
              return false;
          });

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(COM_MINECOLONIES_COREMOD_ENTITY_WORKER_INVENTORYFULLCHEST),
          citizen -> citizen.getWorkBuilding() != null && InventoryUtils.isProviderFull(citizen.getWorkBuilding()));
        InteractionValidatorRegistry.registerPosBasedPredicate(
          new TranslatableComponent(BUILDING_LEVEL_TOO_LOW), (citizen, pos) ->
          {
              final IBuildingWorker workBuilding = citizen.getWorkBuilding();
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
        InteractionValidatorRegistry.registerTokenBasedPredicate(new TranslatableComponent(NORMAL_REQUEST),
          (citizen, token) -> {

              final IColony colony = citizen.getColony();
              if (colony != null)
              {
                  return RequestUtils.requestChainNeedsPlayer(token, citizen.getColony().getRequestManager());
              }
              return false;
          });

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(FURNACE_USER_NO_ORE),
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

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(PATIENT_FULL_INVENTORY),
          citizen -> citizen.getEntity().isPresent() && citizen.getEntity().get().getCitizenDiseaseHandler().isSick()
                       && InventoryUtils.isItemHandlerFull(citizen.getEntity().get().getInventoryCitizen()));

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(PUPIL_NO_CARPET),
          citizen -> citizen.getEntity().isPresent() && citizen.isChild() && citizen.getWorkBuilding() instanceof BuildingSchool
                       && ((BuildingSchool) citizen.getWorkBuilding()).getRandomPlaceToSit() == null);

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(WATER_TOO_FAR),
          citizen -> citizen.getJob() instanceof JobFisherman && ((JobFisherman) citizen.getJob()).getPonds().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(FURNACE_USER_NO_FUEL),
          citizen -> citizen.getWorkBuilding() != null && citizen.getWorkBuilding().hasModule(FurnaceUserModule.class) && citizen.getWorkBuilding().getModuleMatching(ItemListModule.class, m -> m.getId().equals(FUEL_LIST)).getList().isEmpty());
        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(FURNACE_USER_NO_FOOD),
          citizen -> {
            if (!(citizen.getWorkBuilding() instanceof BuildingCook))
            {
                return false;
            }

            final ImmutableList<ItemStorage> exclusionList = ((BuildingCook) citizen.getWorkBuilding()).getModuleMatching(ItemListModule.class, m -> m.getId().equals(FOOD_EXCLUSION_LIST)).getList();
            for (final ItemStorage storage : IColonyManager.getInstance().getCompatibilityManager().getEdibles())
            {
                if (!exclusionList.contains(storage))
                {
                    return false;
                }
            }

            return true;
          });
        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(SIFTER_NO_MESH),
          citizen -> {
            if (!(citizen.getWorkBuilding() instanceof BuildingSifter))
            {
                return false;
            }
            return InventoryUtils.getItemCountInProvider(citizen.getWorkBuilding(), item -> ModTags.meshes.contains(item.getItem())) <= 0 &&
                   InventoryUtils.getItemCountInItemHandler(citizen.getInventory(), item -> ModTags.meshes.contains(item.getItem())) <= 0;
          });
        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(BAKER_HAS_NO_FURNACES_MESSAGE),
          citizen -> citizen.getWorkBuilding() instanceof BuildingBaker && citizen.getWorkBuilding().getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(NO_HIVES),
          citizen -> citizen.getWorkBuilding() instanceof BuildingBeekeeper && ((BuildingBeekeeper) citizen.getWorkBuilding()).getHives().isEmpty());
        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(NO_BEES),
          citizen -> citizen.getWorkBuilding() instanceof BuildingBeekeeper && citizen.getJob(JobBeekeeper.class).checkForBeeInteraction());

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(NO_WORKERS_TO_DRAIN_SET),
          citizen -> citizen.getWorkBuilding() instanceof BuildingEnchanter && ((BuildingEnchanter) citizen.getWorkBuilding()).getFirstModuleOccurance(EnchanterStationsModule.class).getBuildingsToGatherFrom().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(NO_PLANT_GROUND_FLORIST),
          citizen -> citizen.getWorkBuilding() instanceof BuildingFlorist && ((BuildingFlorist) citizen.getWorkBuilding()).getPlantGround().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(NO_FLOWERS_IN_CONFIG),
          citizen -> citizen.getWorkBuilding() instanceof BuildingFlorist && ItemStackUtils.isEmpty(((BuildingFlorist) citizen.getWorkBuilding()).getFlowerToGrow()));

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(NO_COMPOST),
          citizen ->
          {
              final IBuildingWorker buildingFlorist = citizen.getWorkBuilding();
              if (buildingFlorist instanceof BuildingFlorist && buildingFlorist.getColony().getWorld() != null)
              {
                  return InventoryUtils.getItemCountInItemHandler(citizen.getInventory(), IS_COMPOST) == 0 && !isThereCompostedLand((BuildingFlorist) buildingFlorist,
                    buildingFlorist.getColony().getWorld());
              }
              return false;
          });

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(NEEDS_BETTER_HUT),
          citizen -> {

              final AbstractBuilding buildingMiner = (AbstractBuilding) citizen.getWorkBuilding();
              if (buildingMiner instanceof BuildingMiner && citizen.getColony() != null && citizen.getColony().getWorld() != null)
              {
                  return getLastLadder(((BuildingMiner) buildingMiner).getLadderLocation(), citizen.getColony().getWorld()) < ((BuildingMiner) buildingMiner).getDepthLimit(citizen.getColony().getWorld())
                           && ((BuildingMiner) buildingMiner).getFirstModuleOccurance(MinerLevelManagementModule.class).getNumberOfLevels() == 0;
              }
              return false;
          });

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(WORKER_AI_EXCEPTION),
          citizen -> citizen.getJob() != null && ((AbstractEntityAIBasic<?, ?>) citizen.getJob().getWorkerAI()).getExceptionTimer() > 1);

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(DEMANDS + HOMELESSNESS),
          citizen -> (citizen.getCitizenHappinessHandler()).getModifier(HOMELESSNESS).getDays() > DEMANDS_DAYS_WITHOUT_HOUSE && citizen.getHomeBuilding() == null);

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(NO + HOMELESSNESS),
          citizen -> (citizen.getCitizenHappinessHandler()).getModifier(HOMELESSNESS).getDays() > COMPLAIN_DAYS_WITHOUT_HOUSE
                       && (citizen.getCitizenHappinessHandler()).getModifier(HOMELESSNESS).getDays() <= DEMANDS_DAYS_WITHOUT_HOUSE && citizen.getHomeBuilding() == null);

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(DEMANDS + UNEMPLOYMENT),
          citizen -> (citizen.getCitizenHappinessHandler()).getModifier(UNEMPLOYMENT).getDays() > DEMANDS_DAYS_WITHOUT_JOB && citizen.getJob() == null);
        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(NO + UNEMPLOYMENT),
          citizen -> (citizen.getCitizenHappinessHandler()).getModifier(UNEMPLOYMENT).getDays() > COMPLAIN_DAYS_WITHOUT_JOB
                       && (citizen.getCitizenHappinessHandler()).getModifier(UNEMPLOYMENT).getDays() <= DEMANDS_DAYS_WITHOUT_JOB && citizen.getJob() == null);

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(DEMANDS + IDLEATJOB),
          citizen -> (citizen.getCitizenHappinessHandler()).getModifier(IDLEATJOB).getDays() > IDLE_AT_JOB_DEMANDS_DAYS);
        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(NO + IDLEATJOB),
          citizen -> citizen.getCitizenHappinessHandler().getModifier(IDLEATJOB).getDays() > IDLE_AT_JOB_COMPLAINS_DAYS
                       && citizen.getCitizenHappinessHandler().getModifier(IDLEATJOB).getDays() <= IDLE_AT_JOB_DEMANDS_DAYS);

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(NO + SLEPTTONIGHT),
          citizen -> !(citizen.getJob() instanceof AbstractJobGuard) && citizen.getCitizenHappinessHandler().getModifier(SLEPTTONIGHT).getDays() <= 0);

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(COM_MINECOLONIES_COREMOD_BEEKEEPER_NOFLOWERS),
          citizen -> citizen.getWorkBuilding() instanceof BuildingBeekeeper
                       && ((BuildingBeekeeper) citizen.getWorkBuilding()).getModuleMatching(ItemListModule.class, m -> m.getId().equals(BUILDING_FLOWER_LIST)).getList().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_RAINING),
          citizen -> citizen.getEntity().isPresent() && citizen.getEntity().get().getCommandSenderWorld().isRaining()
                       && !citizen.getColony().getRaiderManager().isRaided()
                       && !citizen.getCitizenMournHandler().isMourning());

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_RAID),
          citizen -> citizen.getEntity().isPresent() && citizen.getColony().getRaiderManager().isRaided());

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_SLEEPING),
          citizen -> citizen.getEntity().isPresent() && citizen.isAsleep());

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_MOURNING),
          citizen -> citizen.getEntity().isPresent() && citizen.getCitizenMournHandler().isMourning()
                     && !citizen.getColony().getRaiderManager().isRaided());

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(CITIZEN_NOT_GUARD_NEAR_WORK),
          citizen -> citizen.getWorkBuilding() != null && !citizen.getWorkBuilding().isGuardBuildingNear());

        InteractionValidatorRegistry.registerStandardPredicate(new TranslatableComponent(CITIZEN_NOT_GUARD_NEAR_HOME),
          citizen -> citizen.getHomeBuilding() != null && !citizen.getHomeBuilding().isGuardBuildingNear());
    }
}
