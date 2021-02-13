package com.minecolonies.coremod.research;

import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A temporary lookup table from statically defined research IDs to the new JSON ResourceLocation ids.
 * //TODO: remove after 1.17, or if sufficient time has passed.
 */
public class ResearchCompatMap
{
    public static final Map<String, ResourceLocation> updateMap = Stream.of(new String[][] {
      {"healingcream", "minecolonies:civilian/healingcream"},
      {"ironskin", "minecolonies:combat/ironskin"},
      {"duelist", "minecolonies:combat/duelist"},
      {"multishot", "minecolonies:combat/multishot"},
      {"amazingveins", "minecolonies:technology/amazingveins"},
      {"masterswordsman", "minecolonies:combat/masterswordsman"},
      {"sieving", "minecolonies:technology/sieving"},
      {"dodge", "minecolonies:combat/dodge"},
      {"cleave", "minecolonies:combat/cleave"},
      {"opera", "minecolonies:civilian/opera"},
      {"circus", "minecolonies:civilian/circus"},
      {"puberty", "minecolonies:civilian/puberty"},
      {"theflintstones", "minecolonies:technology/theflintstones"},
      {"scholarly", "minecolonies:civilian/scholarly"},
      {"guardianangel2", "minecolonies:civilian/guardianangel2"},
      {"outpost", "minecolonies:civilian/outpost"},
      {"hamlet", "minecolonies:civilian/hamlet"},
      {"stringwork", "minecolonies:technology/stringwork"},
      {"knighttraining", "minecolonies:combat/knighttraining"},
      {"improvedevasion", "minecolonies:combat/improvedevasion"},
      {"rapidshot", "minecolonies:combat/rapidshot"},
      {"steelarmour", "minecolonies:combat/steelarmor"},
      {"steelarmor", "minecolonies:combat/steelarmor"},
      {"capacity", "minecolonies:technology/capacity"},
      {"swift", "minecolonies:civilian/swift"},
      {"skills", "minecolonies:technology/skills"},
      {"avoidance", "minecolonies:combat/avoidance"},
      {"improvedbows", "minecolonies:combat/improvedbows"},
      {"accuracy", "minecolonies:combat/accuracy"},
      {"nightowl2", "minecolonies:civilian/nightowl2"},
      {"isthisredstone", "minecolonies:technology/isthisredstone"},
      {"deadlyaim", "minecolonies:combat/deadlyaim"},
      {"bandaid", "minecolonies:civilian/bandaid"},
      {"quickdraw", "minecolonies:combat/quickdraw"},
      {"compost", "minecolonies:technology/compost"},
      {"spectacle", "minecolonies:civilian/spectacle"},
      {"ability", "minecolonies:technology/ability"},
      {"fertilizer", "minecolonies:technology/fertilizer"},
      {"hardened", "minecolonies:technology/hardened"},
      {"cheatsheet", "minecolonies:technology/cheatsheet"},
      {"magiccompost", "minecolonies:technology/magiccompost"},
      {"parry", "minecolonies:combat/parry"},
      {"strong", "minecolonies:technology/strong"},
      {"city", "minecolonies:civilian/city"},
      {"whatyaneed", "minecolonies:technology/whatyaneed"},
      {"studious", "minecolonies:civilian/studious"},
      {"fullretreat", "minecolonies:combat/fullretreat"},
      {"hot", "minecolonies:technology/hot"},
      {"growth", "minecolonies:civilian/growth"},
      {"athlete", "minecolonies:civilian/athlete"},
      {"rockingroll", "minecolonies:technology/rockingroll"},
      {"hittingiron", "minecolonies:technology/hittingiron"},
      {"diamondcoated", "minecolonies:technology/diamondcoated"},
      {"heavymachinery", "minecolonies:technology/heavymachinery"},
      {"heavilyloaded", "minecolonies:technology/heavilyloaded"},
      {"evasion", "minecolonies:combat/evasion"},
      {"avoid", "minecolonies:combat/avoid"},
      {"ironarmour", "minecolonies:combat/ironarmor"},
      {"ironarmor", "minecolonies:combat/ironarmor"},
      {"nurture", "minecolonies:civilian/nurture"},
      {"regeneration", "minecolonies:combat/regeneration"},
      {"seemsautomatic", "minecolonies:technology/seemsautomatic"},
      {"agilearcher", "minecolonies:combat/agilearcher"},
      {"richveins", "minecolonies:technology/richveins"},
      {"arrowuse", "minecolonies:combat/arrowuse"},
      {"rails", "minecolonies:civilian/rails"},
      {"reinforced", "minecolonies:technology/reinforced"},
      {"stonecake", "minecolonies:technology/stonecake"},
      {"epicure", "minecolonies:civilian/epicure"},
      {"diamondskin", "minecolonies:combat/diamondskin"},
      {"firstaid2", "minecolonies:civilian/firstaid2"},
      {"gourmand", "minecolonies:civilian/gourmand"},
      {"lightning", "minecolonies:technology/lightning"},
      {"memoryaid", "minecolonies:technology/memoryaid"},
      {"nimble", "minecolonies:civilian/nimble"},
      {"retreat", "minecolonies:combat/retreat"},
      {"fullstock", "minecolonies:technology/fullstock"},
      {"biodegradable", "minecolonies:technology/biodegradable"},
      {"piercingshot", "minecolonies:combat/piercingshot"},
      {"repost", "minecolonies:combat/riposte"},
      {"riposte", "minecolonies:combat/riposte"},
      {"vitality", "minecolonies:civilian/vitality"},
      {"nightowl", "minecolonies:civilian/nightowl"},
      {"festival", "minecolonies:civilian/festival"},
      {"knowtheend", "minecolonies:technology/knowtheend"},
      {"arrowpierce", "minecolonies:combat/arrowpierce"},
      {"resistance", "minecolonies:civilian/resistance"},
      {"fortitude", "minecolonies:civilian/fortitude"},
      {"evade", "minecolonies:combat/evade"},
      {"stamina", "minecolonies:civilian/stamina"},
      {"deeppockets", "minecolonies:technology/deeppockets"},
      {"guardianangel", "minecolonies:civilian/guardianangel"},
      {"hotfoot", "minecolonies:combat/hotfoot"},
      {"stuffer", "minecolonies:civilian/stuffer"},
      {"madness", "minecolonies:technology/madness"},
      {"pavetheroad", "minecolonies:technology/pavetheroad"},
      {"diligent", "minecolonies:civilian/diligent"},
      {"livesaver2", "minecolonies:civilian/lifesaver2"},
      {"lifesaver2", "minecolonies:civilian/lifesaver2"},
      {"beanstalk", "minecolonies:civilian/beanstalk"},
      {"improvedleather", "minecolonies:combat/improvedleather"},
      {"resilience", "minecolonies:civilian/resilience"},
      {"taunt", "minecolonies:combat/taunt"},
      {"veinminer", "minecolonies:technology/veinminer"},
      {"firstaid", "minecolonies:civilian/firstaid"},
      {"compress", "minecolonies:civilian/compress"},
      {"flee", "minecolonies:combat/flee"},
      {"captainoftheguard", "minecolonies:combat/captainoftheguard"},
      {"boiledleather", "minecolonies:combat/boiledleather"},
      {"preciseshot", "minecolonies:combat/preciseshot"},
      {"flowerpower", "minecolonies:technology/flowerpower"},
      {"morebooks", "minecolonies:civilian/morebooks"},
      {"livesaver", "minecolonies:civilian/lifesaver"},
      {"lifesaver", "minecolonies:civilian/lifesaver"},
      {"provost", "minecolonies:combat/provost"},
      {"master", "minecolonies:civilian/master"},
      {"cast", "minecolonies:civilian/cast"},
      {"enhanced_gates2", "minecolonies:technology/enhanced_gates2"},
      {"feint", "minecolonies:combat/feint"},
      {"village", "minecolonies:civilian/village"},
      {"redstonepowered", "minecolonies:technology/redstonepowered"},
      {"tools", "minecolonies:technology/tools"},
      {"whatisthisspeed", "minecolonies:technology/whatisthisspeed"},
      {"hotboots", "minecolonies:technology/hotboots"},
      {"academic", "minecolonies:civilian/academic"},
      {"indefatigability", "minecolonies:civilian/indefatigability"},
      {"whirldwind", "minecolonies:combat/savagestrike"},
      {"whirlwind", "minecolonies:combat/savagestrike"},
      {"doubletrouble", "minecolonies:technology/doubletrouble"},
      {"motherlode", "minecolonies:technology/motherlode"},
      {"improvedswords", "minecolonies:combat/improvedswords"},
      {"theater", "minecolonies:civilian/theater"},
      {"dung", "minecolonies:technology/dung"},
      {"captaintraining", "minecolonies:combat/captaintraining"},
      {"space", "minecolonies:technology/space"},
      {"enhanced_gates1", "minecolonies:technology/enhanced_gates1"},
      {"woodwork", "minecolonies:technology/woodwork"},
      {"tickshot", "minecolonies:combat/trickshot"},
      {"fear", "minecolonies:combat/fear"},
      {"higherlearning", "minecolonies:civilian/higherlearning"},
      {"powerattack", "minecolonies:combat/powerattack"},
      {"steelbracing", "minecolonies:technology/steelbracing"},
      {"keen", "minecolonies:civilian/keen"},
      {"gorger", "minecolonies:civilian/gorger"},
      {"reflective", "minecolonies:civilian/reflective"},
      {"thoselungs", "minecolonies:technology/thoselungs"},
      {"letitgrow", "minecolonies:technology/letitgrow"},
      {"squiretraining", "minecolonies:combat/squiretraining"},
      {"agile", "minecolonies:civilian/agile"},
      {"bookworm", "minecolonies:civilian/bookworm"},
      {"recipebook", "minecolonies:technology/recipebook"},
      {"loaded", "minecolonies:technology/loaded"},
      {"mightycleave", "minecolonies:combat/mightycleave"},
      {"bonemeal", "minecolonies:technology/bonemeal"},
      {"masterbowman", "minecolonies:combat/masterbowman"},
      {"woundingshot", "minecolonies:combat/woundingshot"},
      {"morescrolls", "minecolonies:technology/morescrolls"},
      {"goodveins", "minecolonies:technology/goodveins"},
      {"rtm", "minecolonies:technology/rtm"},
      {"knockbackaoe", "minecolonies:combat/whirlwind"},
      {"bachelor", "minecolonies:civilian/bachelor"},
      {"glutton", "minecolonies:civilian/glutton"},
      {"gildedhammer", "minecolonies:technology/gildedhammer"},
      {"rainman", "minecolonies:technology/rainman"},
      {"rainbowheaven", "minecolonies:technology/rainbowheaven"},
      {"phd", "minecolonies:civilian/phd"},
      {"hormones", "minecolonies:civilian/hormones"},
      {"improveddodge", "minecolonies:combat/improveddodge"},
      {"penetratingshot", "minecolonies:combat/penetratingshot"},
      {"bandages", "minecolonies:civilian/bandages"},
      {"tactictraining", "minecolonies:combat/tactictraining"}
    }).collect(Collectors.toMap(data -> data[0], data -> new ResourceLocation(data[1])));
}
