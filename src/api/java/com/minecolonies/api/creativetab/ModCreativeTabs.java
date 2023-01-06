package com.minecolonies.api.creativetab;

import com.minecolonies.api.blocks.*;
import com.minecolonies.api.blocks.decorative.AbstractBlockGate;
import com.minecolonies.api.blocks.decorative.AbstractBlockMinecoloniesConstructionTape;
import com.minecolonies.api.blocks.decorative.AbstractColonyFlagBanner;
import com.minecolonies.api.blocks.huts.AbstractBlockMinecoloniesDefault;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.minecolonies.api.blocks.decorative.AbstractBlockGate.IRON_GATE;
import static com.minecolonies.api.blocks.decorative.AbstractBlockGate.WOODEN_GATE;

/**
 * Class used to handle the creativeTab of minecolonies.
 */
@Mod.EventBusSubscriber
public final class ModCreativeTabs
{
    private static final ResourceLocation CREATIVE_TAB = new ResourceLocation(Constants.MOD_ID, "minecolonies");

    @SubscribeEvent
    public static void CreativeTabEvent(final CreativeModeTabEvent.Register event)
    {
        event.registerCreativeModeTab(CREATIVE_TAB, (cf) -> cf.icon(() -> new ItemStack(ModBlocks.blockHutTownHall)).withSearchBar().title(Component.literal("Minecolonies")).displayItems((flagSet, output, ifSth) -> {

            for (final AbstractBlockHut<?> hut : ModBlocks.getHuts())
            {
                output.accept(hut);
            }

            output.accept(ModBlocks.blockScarecrow);
            output.accept(ModBlocks.blockRack);
            output.accept(ModBlocks.blockGrave);
            output.accept(ModBlocks.blockNamedGrave);
            output.accept(ModBlocks.blockWayPoint);
            output.accept(ModBlocks.blockBarrel);
            output.accept(ModBlocks.blockDecorationPlaceholder);
            output.accept(ModBlocks.blockCompostedDirt);
            output.accept(ModBlocks.blockConstructionTape);

            output.accept(ModItems.scepterLumberjack);
            output.accept(ModItems.permTool);
            output.accept(ModItems.scepterGuard);
            output.accept(ModItems.scepterBeekeeper);

            output.accept(ModItems.bannerRallyGuards);

            output.accept(ModItems.supplyChest);
            output.accept(ModItems.supplyCamp);

            output.accept(ModItems.clipboard);
            output.accept(ModItems.resourceScroll);
            output.accept(ModItems.compost);
            output.accept(ModItems.mistletoe);
            output.accept(ModItems.magicpotion);
            output.accept(ModItems.buildGoggles);

            output.accept(ModItems.breadDough);
            output.accept(ModItems.cookieDough);
            output.accept(ModItems.cakeBatter);
            output.accept(ModItems.rawPumpkinPie);

            output.accept(ModItems.milkyBread);
            output.accept(ModItems.sugaryBread);
            output.accept(ModItems.goldenBread);
            output.accept(ModItems.chorusBread);

            output.accept(ModItems.scrollColonyTP);
            output.accept(ModItems.scrollColonyAreaTP);
            output.accept(ModItems.scrollBuff);
            output.accept(ModItems.scrollGuardHelp);
            output.accept(ModItems.scrollHighLight);

            output.accept(ModItems.santaHat);

            output.accept(ModItems.irongate);
            output.accept(ModItems.woodgate);

            output.accept(ModItems.flagBanner);

            output.accept(ModItems.ancientTome);
            output.accept(ModItems.chiefSword);
            output.accept(ModItems.scimitar);
            output.accept(ModItems.pharaoscepter);
            output.accept(ModItems.firearrow);
            output.accept(ModItems.spear);
            output.accept(ModItems.pirateHelmet_1);
            output.accept(ModItems.pirateChest_1);
            output.accept(ModItems.pirateLegs_1);
            output.accept(ModItems.pirateBoots_1);

            output.accept(ModItems.pirateHelmet_2);
            output.accept(ModItems.pirateChest_2);
            output.accept(ModItems.pirateLegs_2);
            output.accept(ModItems.pirateBoots_2);

            output.accept(ModItems.plateArmorHelmet);
            output.accept(ModItems.plateArmorChest);
            output.accept(ModItems.plateArmorLegs);
            output.accept(ModItems.plateArmorBoots);

            output.accept(ModItems.sifterMeshString);
            output.accept(ModItems.sifterMeshFlint);
            output.accept(ModItems.sifterMeshIron);
            output.accept(ModItems.sifterMeshDiamond);
        }));
    }

    /**
     * Private constructor to hide the implicit one.
     */
    private ModCreativeTabs()
    {
        /*
         * Intentionally left empty.
         */
    }
}
