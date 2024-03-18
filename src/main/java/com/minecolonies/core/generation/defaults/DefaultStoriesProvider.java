package com.minecolonies.core.generation.defaults;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minecolonies.core.event.ColonyStoryListener;
import net.minecraft.core.Holder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.core.generation.DataGeneratorConstants.COLONY_STORIES_DIR;

public class DefaultStoriesProvider implements DataProvider
{
    private final PackOutput packOutput;

    public DefaultStoriesProvider(@NotNull final PackOutput packOutput)
    {
        this.packOutput = packOutput;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Default Stories Provider";
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(@NotNull final CachedOutput cachedOutput)
    {
        final PackOutput.PathProvider outputProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, COLONY_STORIES_DIR);

        return CompletableFuture.allOf(makeAbandonedStories(outputProvider, cachedOutput), makeSupplyStories(outputProvider, cachedOutput));
    }

    private CompletableFuture<?> makeAbandonedStories(@NotNull final PackOutput.PathProvider outputProvider,
                                                      @NotNull final CachedOutput cachedOutput)
    {
        final JsonArray json = new JsonArray();

        json.add(new StoryBuilder(ColonyStoryListener.ABANDONED_COLONY_NAME)
                .addBiomeTag(Tags.Biomes.IS_WET, Tags.Biomes.IS_PLAINS)
                .addContents(
                        "Clearwater",
                        "Riverdale",
                        "Lakeside",
                        "Brookside",
                        "Willow Creek",
                        "Serenity Springs"
                ).build());

        json.add(new StoryBuilder(ColonyStoryListener.ABANDONED_COLONY_NAME)
                .addBiomeTag(Tags.Biomes.IS_COLD)
                .addContents(
                        "Silvervale",
                        "Frostfield",
                        "Misty Hollow",
                        "Frostwood"
                ).build());

        json.add(new StoryBuilder(ColonyStoryListener.ABANDONED_COLONY_NAME)
                .addBiomeTag(Tags.Biomes.IS_DESERT)
                .addContents(
                        "Goldenridge",
                        "Ashford"
                ).build());

        json.add(new StoryBuilder(ColonyStoryListener.ABANDONED_COLONY_NAME)
                .addContents(
                        "Willowbrook",
                        "Greenwood",
                        "Oakwood",
                        "Stonybrook",
                        "Maplewood",
                        "Fairview",
                        "Pinecrest",
                        "Rosewood",
                        "Meadowbrook",
                        "Suncrest",
                        "Hillcrest",
                        "Ravenwood",
                        "Springvale",
                        "Briarwood",
                        "Evergreen",
                        "Whispering Pines",
                        "Shadowvale",
                        "Highgate",
                        "Millstone",
                        "Emberglow",
                        "Mistwood",
                        "Sycamore",
                        "Moonlight Hollow",
                        "Harveston",
                        "Greenwood",
                        "Stonehaven",
                        "Silverwood",
                        "Redwood",
                        "Ironwood",
                        "Greenwood",
                        "Birchwood",
                        "Pineview",
                        "Foxgrove",
                        "Elmwood",
                        "Briarvale",
                        "Brookhaven",
                        "Whitestone",
                        "Summerfield"
                ).build());

        json.add(new StoryBuilder(ColonyStoryListener.ABANDONED_COLONY_STORY)
                .addContents(
                        "3rd day of Summer, Year 7, in the Era of Prosperity:\nToday, we founded our settlement: %s in this %s. The soil is rich, the air is crisp, and the land promises a bountiful future. With a modest group of settlers, we begin to lay the foundations of our new home, full of hope and ambition.\n\n11th day of Autumn, Year 8, in the Era of Prosperity:\nOur numbers have swelled to over fifty souls as travelers and adventurers are drawn to our burgeoning village. Trade caravans arrive regularly, bringing news from distant lands and exchanging goods. Our prosperity seems assured as our fields yield abundant harvests and our craftsmen ply their trades with skill.\n\n27th day of Winter, Year 10, in the Era of Prosperity:\nDarkness descends upon us as raiders from neighboring tribes launch a surprise attack under the cover of night. Despite our valiant efforts, we suffer heavy losses. In the aftermath, we fortify our defenses, erecting stout walls and training militia to protect our people. Fear hangs heavy in the air, but we resolve to stand firm against future threats.\n\n2nd day of Spring, Year 11, in the Era of Prosperity:\nThe raids persist, relentless in their ferocity. Our numbers dwindle, and morale wanes as hope fades. With heavy hearts, we acknowledge the harsh reality that our once-thriving settlement is no longer sustainable. We make the painful decision to abandon our homes and seek refuge elsewhere.",
                        "15th day of Harvest, Year 4, in the Age of Exploration:\nLaying the foundation for %s amidst towering trees and fertile soil in a verdant %s.\n\n22nd day of Frostfall, Year 5, in the Age of Exploration:\nOur settlement has flourished beyond our wildest expectations. The fields yield abundant crops, and our livestock thrive in the lush pastures. Traders from distant lands bring news of the wider world, fueling our curiosity and igniting our imaginations. Our once-small community has grown into a bustling hub of activity, teeming with life and possibility.\n\n8th day of Rain's End, Year 7, in the Age of Exploration:\nDisaster strikes as a virulent plague sweeps through our settlement, claiming lives with alarming speed. Despite our best efforts to contain the outbreak, the disease spreads unchecked, leaving death and despair in its wake. With heavy hearts, we bury our dead and tend to the sick, but hope dwindles with each passing day.\n\n2nd day of Sun's Rise, Year 8, in the Age of Exploration:\nFaced with the grim reality of our situation, we make the agonizing decision to abandon our once-thriving settlement. The memories of laughter and camaraderie are overshadowed by the specter of loss and grief. With tear-filled eyes, we bid farewell to our homes and set out into the unknown, leaving behind only echoes of the life we once knew.",
                        "12th day of Summer, Year 3, in the Time of Renewal:\nToday marks the beginning of our journey as we establish our little town of %s in this rugged wilderness. Surrounded by a %s, we feel a sense of awe and wonder at the untamed beauty of our surroundings. With determination in our hearts, we set about building our new home, confident in our ability to overcome whatever challenges lie ahead.\n\n6th day of Winter, Year 5, in the Time of Renewal:\nOur settlement has grown into a thriving community, united by a shared vision of prosperity and progress. Through hard work and perseverance, we have transformed this once-wild land into a beacon of civilization. Our efforts have not gone unnoticed, as traders and travelers flock to our gates, eager to partake in the fruits of our labor.\n\n21st day of Spring, Year 7, in the Time of Renewal:\nTragedy strikes as a series of devastating earthquakes rock our settlement, leaving destruction in their wake. Homes are reduced to rubble, and fields lie barren as aftershocks continue to shake the earth beneath our feet. Despite our best efforts to rebuild, the damage is too great, and we are forced to acknowledge the harsh reality that our dreams lie shattered at our feet.\n\n3rd day of Autumn, Year 8, in the Time of Renewal:\nWith heavy hearts and tear-streaked faces, we bid farewell to our beloved settlement, leaving behind the ruins of our dreams.",
                        "10th day of Spring, Year 5, in the Age of Crafting:\nLooking out for a place to found our new settlement: %s, today, we stumbled upon a pristine %s. The land seemed untouched, ripe for settlement. With determination in our hearts, we began constructing our village, crafting shelters from the very earth and wood that surrounded us.\n\n18th day of Summer, Year 6, in the Age of Crafting:\nOur settlement flourished as travelers and merchants from distant lands brought news and goods. However, our prosperity attracted unwanted attention. Creatures of the night, zombies and skeletons, emerged from the darkened woods, threatening our safety.\n\n5th day of Winter, Year 7, in the Age of Crafting:\nDespite our efforts to fortify our village, the attacks persisted. Creepers prowled the perimeter, their hissing warnings a constant reminder of the danger we faced. With heavy hearts, we prepared for the inevitable.\n\n22nd day of Spring, Year 8, in the Age of Crafting:\nThe final assault came with the rising of the blood moon. Waves of monsters descended upon our village, overwhelming our defenses. In the chaos, we were forced to flee, leaving behind our once-thriving home to the mercy of the mobs.",
                        "7th day of Autumn, Year 3, in the Era of Rivers:\nOn the quest to found the settlement of %s, We discovered a hidden %s, untouched by civilization, a blank canvas awaiting our mark. With axes in hand, we began to carve out our future, shaping the landscape to our will.\n\n14th day of Winter, Year 4, in the Era of Blocks\nOur village grew, its buildings reaching for the sky as we mined deep into the earth for resources. But with prosperity came danger. Endermen lurked in the shadows, their glowing eyes watching our every move.\n\n3rd day of Summer, Year 5, in the Era of Blocks\nThe first signs of trouble came with the disappearance of livestock and crops. We blamed it on wolves and spiders, but the truth was far more sinister. A dragon had taken residence in the nearby mountains, its fiery breath threatening to consume our village.\n\n11th day of Autumn, Year 6, in the Era of Blocks\nDespite our best efforts to drive the dragon away, it persisted in its attacks. With our resources depleted and our spirits broken, we had no choice but to abandon our village, leaving behind only memories of what once was.",
                        "2nd day of Winter, Year 8, in the Age of Crafting:\n%s was built upon the promise of this %s, its fertile soil and abundant resources luring us in. With the sun on our backs and pickaxes in hand, we began to carve out a new life for ourselves, one block at a time.\n\n19th day of Spring, Year 9, in the Age of Crafting:\nOur village thrived as traders and travelers passed through, their goods and stories enriching our lives. But prosperity attracted jealousy, and soon, we found ourselves under attack by a rival faction.\n\n7th day of Autumn, Year 10, in the Age of Crafting:\nSkeletons and zombies swarmed our village, their hollow eyes gleaming with malice. We fought back with all our might, but the odds were against us. As our defenses crumbled, we knew that our only hope lay in retreat.\n\n1st day of Winter, Year 11, in the Age of Crafting:\nWith heavy hearts, we abandoned our homes, leaving behind the smoldering ruins of our once-thriving village. The land that had promised so much had become our prison, and we could only hope to find sanctuary elsewhere.\n",
                        "14th day of Summer, Year 4, in the Age of Blocks:\n%s was a beacon of hope in the midst day of the %s, its towering walls and bustling streets a testament to our resilience. With each block we placed, we forged a new future for ourselves, one built upon the sweat and toil of our labor.\n\n3rd day of Winter, Year 5, in the Age of Blocks:\nBut our prosperity drew the attention of the undead, creatures of darkness that hungered for our flesh. Zombies and skeletons lurked in the shadows, their moans and rattles a constant reminder of the danger that surrounded us.\n\n22nd day of Spring, Year 6, in the Age of Blocks:\nDespite our best efforts to defend our village, the attacks grew more frequent and more ferocious. Creepers exploded, leaving craters in their wake, while spiders crawled over our walls, seeking to feast on our blood.\n\n11th day of Autumn, Year 7, in the Age of Blocks:\nIn the end, we could withstand the onslaught no longer. With heavy hearts, we abandoned our homes, leaving behind the ruins of our once-thriving village to be reclaimed by nature.\n",
                        "5th day of Autumn, Year 6, in the Age of Crafting:\n%s was founded in the heart of a %s, its towering trees and winding streams providing us with all we needed to thrive. With axes and shovels, we set to work, carving out a place for ourselves in this untamed wilderness.\n\n19th day of Winter, Year 7, in the Age of Crafting:\nBut our idyllic existence was shattered by the arrival of the Nether's creatures, vile beasts that sought to claim our world as their own. Ghasts spewed fireballs from the sky, while blazes set our homes ablaze.\n\n8th day of Spring, Year 8, in the Age of Crafting:\nWe fought back with all our might, but the forces arrayed against us were too great. As our village burned and our people fell, we knew that our only hope lay in escape.\n\n2nd day of Summer, Year 9, in the Age of Crafting:\nWith heavy hearts, we fled into the wilderness, leaving behind the smoldering ruins of our once-thriving village. The memory of what was lost would haunt us forever.\n",
                        "17th day of Winter, Year 2, in the Time of Mining:\n%s was built upon the promise of hidden riches in a %s, its foundations rooted in the very earth we sought to tame. With pickaxes in hand, we delved deep into the underground, eager to uncover the treasures that lay hidden beneath.\n\n9th day of Spring, Year 3, in the Time of Mining:\nBut our lust for gold and diamonds drew the attention of the cave-dwellers, vile creatures that lurked in the darkness. Zombies and skeletons emerged from the shadows, their hunger for flesh insatiable.\n\n25th day of Summer, Year 4, in the Time of Mining:\nDespite our best efforts to defend our tunnels, the attacks grew more frequent and more ferocious."
                ).build());

        return DataProvider.saveStable(cachedOutput, json, outputProvider.json(new ResourceLocation(MOD_ID, "abandonedcolonies")));
    }

    private CompletableFuture<?> makeSupplyStories(@NotNull final PackOutput.PathProvider outputProvider,
                                                   @NotNull final CachedOutput cachedOutput)
    {
        final JsonArray json = new JsonArray();

        json.add(new StoryBuilder(ColonyStoryListener.SUPPLY_CAMP_STORY)
                .addContents(
                        "Engraved on Stone:\n\nDriven by whispers of a hidden paradise, we ventured deep into the uncharted desert. Burdened with supplies and fueled by hope, we traversed scorching sands until a towering mesa pierced the horizon. Yet, our dreams turned to dust as hardship consumed us. Dwindling water and relentless heat became our constant companions, and unseen creatures stalked the dunes. Now, a solitary soul, I leave this warning: the path to glory is parched with peril. May fate grant you the resilience we lacked, and may you flourish where we withered.",
                        "Lost Journal Entry:\n\nA yearning for a new life propelled us to trek across the treacherous mountain range. Laden with provisions and unwavering determination, we braved the icy winds and treacherous slopes until a hidden valley emerged. But our hope soon froze in the unforgiving grip of winter. Hunger gnawed at our bellies, and the unforgiving terrain became our adversary. We faced constant avalanches thundering down the peaks. Now, a lone survivor, I leave this message: the road to triumph is fraught with frozen dangers. May the winds of fortune carry you where they forsook us, and may you thrive where we faltered.",
                        "A Wanderer's Song:\n\nFueled by a hunger for adventure, we set out to explore the vast and untamed jungle. Armed with tools and fueled by youthful spirit, we pushed through the dense foliage until a hidden temple peeked through the canopy. Yet, our dreams were devoured by the unforgiving wilderness. Dwindling supplies and relentless insects became our adversaries, and unseen predators lurked in the shadows. Now, a solitary wanderer, I sing this cautionary tale: the path to discovery is choked with danger. May fortune guide you where it misled us, and may you find solace where we met our demise.",
                        "Carvings in the Canyon:\n\nA yearning for a new beginning propelled us to explore the depths of the uncharted canyon. Armed with ropes and fueled by ambition, we rappelled down treacherous cliffs until a hidden oasis shimmered in the distance. But our dreams were dashed upon the rocks of hardship. Dwindling resources and scorching heat became our constant companions, and flash floods threatened to engulf us. Now, a lone survivor, I etch this warning: the descent to glory is fraught with peril. May fate grant you the blessings it denied us, and may you thrive where we perished.",
                        "A Nomad's Chronicle:\n\nDriven by a nomadic spirit, we wandered across the boundless plains. Laden with provisions and a thirst for the unknown, we navigated the grassy expanse until a nomadic tribe welcomed us with open arms. We integrated into their way of life, but hardship soon followed. Scarcity of resources and relentless storms challenged our newfound home. Savage beasts roamed the plains, and internal conflicts threatened to tear the tribe apart. Now, a solitary witness, I leave this record: the path to belonging is fraught with challenges. May the winds of fortune guide you where they forsook us, and may you find peace where we found strife.",
                        "A Survivor's Whisper:\n\nFueled by desperation, we fled the encroaching darkness that consumed our homeland. Laden with meager supplies and fueled by fear, we traversed the desolate wasteland until a glimmer of hope appeared on the horizon. But our dreams crumbled beneath the harsh realities of the wasteland. Dwindling resources and relentless sandstorms became our constant companions, and mutated creatures stalked our every step. Now, a solitary survivor, I whisper this warning: the path to escape is choked with peril. May fate grant you the strength we lacked, and may you outrun the darkness where we faltered.",
                        "A Shepherd's Legacy:\n\nDriven by a desire for a better life, we migrated across the rolling hills with our flocks. Laden with provisions and a spirit of resilience, we braved scorching summers and harsh winters until fertile pastures greeted us. We established a new home, but hardship followed in the wake of prosperity. Raiders threatened our livelihood, and a mysterious illness swept through the flocks. Now, a solitary shepherd, I leave this legacy: the path to prosperity is fraught with trials. May fate grant you the courage we lacked, and may you find peace where we found despair.",
                        "A Cartographer's Scroll:\n\nFueled by a thirst for knowledge, we embarked on a journey to map the uncharted forest. Equipped with tools and unwavering curiosity, we ventured into the dense woods until a hidden clearing revealed a network of ancient ruins. But our dreams were tangled in the unforgiving wilderness. Dwindling supplies and treacherous paths became our obstacles, and unseen creatures guarded the secrets of the ruins. Now, a solitary cartographer, I leave this scroll: the path to discovery is veiled in danger. May fortune guide you where it misled us, and may you unveil the mysteries where we faltered.",
                        "A Wanderer's Mark:\n\nFueled by a restless spirit, we roamed the vast tundra in search of solace. Laden with provisions and a yearning for a simpler life, we traversed the frozen plains until a glimmering aurora borealis painted the night sky. Yet, our dreams were lost in the unforgiving embrace of the cold. Dwindling supplies and relentless blizzards became our constant companions, and solitary predators stalked the icy expanse. Now, a solitary wanderer, I leave this mark: the path to serenity is shrouded in peril. May fate grant you the resilience we lacked, and may you find peace where we found only the silence of the frozen plains.",
                        "Whispers on the Wind:\n\nA yearning for a new purpose propelled us to explore the depths of the uncharted swamp. Armed with tools and fueled by youthful ambition, we slogged through the murky waters until a hidden grove shimmered with an unnatural light. Yet, our dreams were swallowed by the suffocating embrace of the swamp. Dwindling resources and unseen creatures became our adversaries, and a miasma of despair hung heavy in the air. Now, a solitary echo whispers this warning: the path to discovery is choked with peril. May fortune guide you where it misled us, and may you find solace where we met our demise."
                ).build());

        json.add(new StoryBuilder(ColonyStoryListener.SUPPLY_SHIP_STORY)
                .addContents(
                        "Hear this, traveler:\n\nDriven by dreams of a new life, we set sail for a distant shore. Laden with supplies and optimism, we braved the storm-tossed seas until land rose on the horizon. But our dreams turned to dust as hardship gripped us. Dwindling resources and unforgiving terrain became our constant companions, and lurking dangers whispered threats. Now, a lone survivor, I leave this warning: the path to glory is paved with peril. May fate grant you the fortune we lacked, and may you prosper where we fell.",
                        "A message etched in time:\n\nOur grand adventure began with a yearning for a new home. We set sail across the vast ocean, hearts brimming with hope, to establish a colony on a foreign shore. We weathered the fury of the sea until land met our eyes. Yet, our dreams were soon dashed upon the rocks of reality. Hunger gnawed at our bellies, and the unforgiving landscape became our enemy. We faced constant threats in the shadows. Now, the sole survivor, I offer this grim record: the path to greatness is fraught with danger. May fortune smile upon you where it abandoned us, and may you find success where we met our demise.",
                        "Mark these words well:\n\nFueled by ambition, we embarked on a journey to carve out a new life in the uncharted wilderness. Laden with provisions and a spirit of determination, we braved the relentless ocean until a new world greeted us. We ventured inland, but our initial optimism was quickly eroded by dwindling supplies and the harsh realities of the terrain. We were constantly beset by unseen dangers. Now, a solitary soul, I leave this cautionary message: the road to triumph is fraught with challenges. May the winds of fortune blow in your favor where they forsook us, and may you flourish where we withered.",
                        "A survivor's testament:\n\nA yearning for a fresh start propelled us to set sail for a distant land, hearts ablaze with dreams of a new colony. We weathered the ocean's fury, our spirits high with hope, until a new horizon unfolded before us. We disembarked, venturing deeper into the unknown, but our dreams crumbled under the weight of dwindling resources and a hostile environment. We were stalked by unseen predators. Now, the lone witness to our demise, I offer this grave inscription: the path to greatness is riddled with perils. May fortune guide you where it misled us, and may you bloom where we perished.",
                        "To whomever inherits this legacy:\n\nBuoyed by the dream of a new beginning, we embarked on a voyage to a distant land, determined to establish a new society. Laden with provisions and optimism, we conquered the tumultuous seas until the promise of a new world rose from the horizon. We ventured inland, but our hope soon surrendered to the harsh realities of dwindling resources and unforgiving terrain. We were relentlessly hunted. Now, a solitary survivor, I leave this message: the path to glory is fraught with hardship. May fate grant you the blessings it denied us, and may you rise where we stumbled.",
                        "Heed this warning:\n\nWith hearts filled with ambition, we set sail for a faraway land, seeking to establish a new colony. Loaded with supplies and resolute spirits, we braved the ocean's wrath until a new land greeted us. We disembarked, venturing into the unknown, but our dreams were soon crushed by the harsh realities of dwindling resources and a treacherous landscape. We were forced to confront spectral attackers and fiery foes. Now, the sole survivor, I offer this stark warning: the journey to greatness is fraught with danger. May fortune smile upon you where it eluded us, and may you triumph where we faltered.",
                        "Lost journal entry:\n\nDriven by a yearning for a new beginning, we ventured across the vast ocean, hearts ablaze with dreams of establishing a new colony. We weathered the storm-tossed seas, fueled by hope, until a new land rose from the horizon. We ventured inland, but our dreams were quickly dashed upon the rocks of hardship. Our supplies dwindled, and the unforgiving terrain became our adversary. We faced constant threats from the skies. Now, a solitary figure, I leave this message of caution: the path to triumph is fraught with trials. May the winds of fortune carry you where they forsook us, and may you thrive where we faltered.",
                        "Castaway's note:\n\nWith hearts brimming with ambition, we set sail for a distant land, seeking to establish a new colony. Laden with supplies and unwavering determination, we braved the churning seas until a new horizon emerged. We disembarked, venturing deeper into the unknown, but our dreams were soon swallowed by the relentless realities of dwindling resources and a hostile environment. We faced relentless foes that haunted our every step. Now, the sole survivor, I offer this stark warning: the journey to greatness is fraught with danger. May fortune smile upon you where it eluded us, and may you triumph where we faltered.",
                        "Echoes from the Past:\n\nFueled by a hunger for a new life, we set sail for a distant shore, hearts ablaze with dreams of a new society. Laden with provisions and unwavering resolve, we conquered the tumultuous seas until the promise of a new world rose on the horizon. We ventured inland, but our hope soon surrendered to the harsh realities of dwindling resources and a treacherous landscape. We were forced to confront unseen threats that stalked the night. Now, a solitary wanderer, I leave this inscription: the path to glory is paved with peril. May fate grant you the blessings it denied us, and may you rise where we faltered.",
                        "Whispers on the Wind:\n\nA yearning for a fresh start propelled us to set sail for a distant land, hearts ablaze with dreams of a new colony. We weathered the ocean's fury, our spirits high with hope, until a new horizon unfolded before us. We disembarked, venturing deeper into the unknown, but our dreams crumbled under the weight of dwindling resources and a hostile environment. We were stalked by unseen predators. Now, the lone echo of a forgotten past, I leave this inscription: the path to greatness is riddled with perils. May fortune guide you where it misled us, and may you bloom where we perished."
                ).build());

        return DataProvider.saveStable(cachedOutput, json, outputProvider.json(new ResourceLocation(MOD_ID, "supplies")));
    }

    private static class StoryBuilder
    {
        final JsonObject json = new JsonObject();

        public StoryBuilder(@NotNull final ResourceLocation type)
        {
            json.addProperty("type", type.toString());
        }

        public JsonObject build()
        {
            return this.json;
        }

        @SafeVarargs
        public final StoryBuilder addBiome(@NotNull final Holder<Biome>... biomes)
        {
            final String[] ids = Arrays.stream(biomes).map(b -> b.unwrapKey().get().location().toString()).toArray(String[]::new);
            return addStringOrArray("biomes", ids);
        }

        @SafeVarargs
        public final StoryBuilder addBiomeTag(@NotNull final TagKey<Biome>... tags)
        {
            final String[] ids = Arrays.stream(tags).map(t -> "#" + t.location()).toArray(String[]::new);
            return addStringOrArray("biomes", ids);
        }

        public StoryBuilder addContents(@NotNull final String... contents)
        {
            return addStringOrArray("content", contents);
        }

        private StoryBuilder addStringOrArray(@NotNull final String property, @NotNull final String... values)
        {
            if (values.length == 0) return this;
            if (values.length == 1 && !json.has(property))
            {
                json.addProperty(property, values[0]);
                return this;
            }

            final JsonArray array;
            if (json.has(property))
            {
                array = json.getAsJsonArray(property);
            }
            else
            {
                array = new JsonArray();
                json.add(property, array);
            }

            for (final String value : values)
            {
                array.add(value);
            }
            return this;
        }
    }
}
