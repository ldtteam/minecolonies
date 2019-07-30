package com.minecolonies.coremod.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.ICitizenData;
import org.assertj.core.api.Fail;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.managers.interfaces.ICitizenManager;
import com.minecolonies.coremod.commands.AbstractCommandParser.ModuleContext;
import com.minecolonies.coremod.commands.AbstractCommandParser.PermissionsChecker;
import com.minecolonies.coremod.commands.CommandEntryPointNew.MineColonyDataProvider;
import com.minecolonies.coremod.commands.citizencommands.CitizenInfoCommand;
import com.minecolonies.coremod.commands.colonycommands.ChangeColonyOwnerCommand;
import com.minecolonies.coremod.commands.colonycommands.ClaimChunksCommand;
import com.minecolonies.coremod.commands.colonycommands.DeleteColonyCommand;
import com.minecolonies.coremod.commands.colonycommands.ListColoniesCommand;
import com.minecolonies.coremod.commands.generalcommands.CheckForAutoDeletesCommand;
import com.minecolonies.coremod.commands.generalcommands.ScanCommand;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import scala.actors.threadpool.Arrays;

@SuppressWarnings({"PMD.MethodNamingConventions", "PMD.JUnitTestsShouldIncludeAssert", "PMD.AvoidDuplicateLiterals", "PMD.ExcessiveImports"})
public class CommandEntryPointTest
{
    @NotNull private MinecraftServer server;
    @NotNull private ICommandSender sender;
    @NotNull private CommandEntryPointNew instance;
    @Nullable private BlockPos pos;
    @NotNull private PermissionsChecker permissionsChecker;
    @NotNull private ModuleContext moduleContext;

    @Before
    public void setUp()
    {
        final Colony colony1 = mock(Colony.class);
        when(colony1.getID()).thenReturn(1);
        final Colony colony2 = mock(Colony.class);
        when(colony2.getID()).thenReturn(2);
        @SuppressWarnings("unchecked")
        final List<IColony> colonyList = Arrays.asList(new Colony[] {colony1, colony2});

        final ICitizenManager citizenManager1 = mock(ICitizenManager.class);
        when(colony1.getCitizenManager()).thenReturn(citizenManager1);
        final ICitizenManager citizenManager2 = mock(ICitizenManager.class);
        when(colony2.getCitizenManager()).thenReturn(citizenManager2);

        final List<ICitizenData> citizenDataList1 = new ArrayList<>();
        when(citizenManager1.getCitizens()).thenReturn(citizenDataList1);

        final ICitizenData citizenJohnSSmith = mock(CitizenData.class);
        when(citizenJohnSSmith.getId()).thenReturn(101);
        when(citizenJohnSSmith.getName()).thenReturn("John S. Smith");

        citizenDataList1.add(citizenJohnSSmith);
        when(citizenManager1.getCitizen(101)).thenReturn(citizenJohnSSmith);

        final ICitizenData citizenJohnSJones = mock(CitizenData.class);
        when(citizenJohnSJones.getId()).thenReturn(104);
        when(citizenJohnSJones.getName()).thenReturn("John S. Jones");

        citizenDataList1.add(citizenJohnSJones);
        when(citizenManager1.getCitizen(104)).thenReturn(citizenJohnSJones);

        final ICitizenData citizenJohnAJones = mock(CitizenData.class);
        when(citizenJohnAJones.getId()).thenReturn(102);
        when(citizenJohnAJones.getName()).thenReturn("John A. Jones");

        citizenDataList1.add(citizenJohnAJones);
        when(citizenManager1.getCitizen(102)).thenReturn(citizenJohnAJones);

        final ICitizenData citizenJennaQBar = mock(CitizenData.class);
        when(citizenJennaQBar.getId()).thenReturn(103);
        when(citizenJennaQBar.getName()).thenReturn("Jenna Q. Bar");

        citizenDataList1.add(citizenJennaQBar);
        when(citizenManager1.getCitizen(103)).thenReturn(citizenJennaQBar);

        final List<ICitizenData> citizenDataList2 = new ArrayList<>();
        when(citizenManager2.getCitizens()).thenReturn(citizenDataList2);


        final ICitizenData citizenSallyJaneJohnsonTheThird = mock(CitizenData.class);
        when(citizenSallyJaneJohnsonTheThird.getId()).thenReturn(201);
        when(citizenSallyJaneJohnsonTheThird.getName()).thenReturn("Sally Jane Johnson the Third");

        citizenDataList2.add(citizenSallyJaneJohnsonTheThird);
        when(citizenManager2.getCitizen(201)).thenReturn(citizenSallyJaneJohnsonTheThird);

        final ICitizenData citizenSallyOfLoxley = mock(CitizenData.class);
        when(citizenSallyOfLoxley.getId()).thenReturn(202);
        when(citizenSallyOfLoxley.getName()).thenReturn("Sally of Loxley");

        citizenDataList2.add(citizenSallyOfLoxley);
        when(citizenManager2.getCitizen(202)).thenReturn(citizenSallyOfLoxley);

        final ICitizenData citizenSally = mock(CitizenData.class);
        when(citizenSally.getId()).thenReturn(203);
        when(citizenSally.getName()).thenReturn("Sally");

        citizenDataList2.add(citizenSally);
        when(citizenManager2.getCitizen(203)).thenReturn(citizenSally);

        final ICitizenData citizenSallyJaneJohnsonTheThirdBananaFoFanna = mock(CitizenData.class);
        when(citizenSallyJaneJohnsonTheThirdBananaFoFanna.getId()).thenReturn(204);
        when(citizenSallyJaneJohnsonTheThirdBananaFoFanna.getName()).thenReturn("Sally Jane Johnson the Third Banana Fo Fanna");

        citizenDataList2.add(citizenSallyJaneJohnsonTheThirdBananaFoFanna);
        when(citizenManager2.getCitizen(204)).thenReturn(citizenSallyJaneJohnsonTheThirdBananaFoFanna);

        final ICitizenData citizenRAYCOMS = mock(CitizenData.class);
        when(citizenRAYCOMS.getId()).thenReturn(205);
        when(citizenRAYCOMS.getName()).thenReturn("R A Y C O M S");

        citizenDataList2.add(citizenRAYCOMS);
        when(citizenManager2.getCitizen(205)).thenReturn(citizenRAYCOMS);

        final ServerPlayerEntity playerBob = mock(ServerPlayerEntity.class);
        when(playerBob.getName()).thenReturn("Bob");
        final ServerPlayerEntity playerSally = mock(ServerPlayerEntity.class);
        when(playerSally.getName()).thenReturn("Sally");

        final PlayerList serverPlayerList = mock(PlayerList.class);
        when(serverPlayerList.getPlayerByUsername("Bob")).thenReturn(playerBob);
        when(serverPlayerList.getPlayerByUsername("Sally")).thenReturn(playerSally);

        final List<ServerPlayerEntity> allServerPlayerEntityList = new ArrayList<>();
        allServerPlayerEntityList.add(playerBob);
        allServerPlayerEntityList.add(playerSally);

        when(serverPlayerList.getPlayers()).thenReturn(allServerPlayerEntityList);

        server = mock(MinecraftServer.class);
        when(server.getOnlinePlayerNames()).thenReturn(new String[] {"Bob", "Sally"});
        when(server.getPlayerList()).thenReturn(serverPlayerList);

        sender = mock(MinecraftServer.class);
        
        pos = new BlockPos(1,2,3);

        permissionsChecker = mock(PermissionsChecker.class);
        when(permissionsChecker.hasPermission(any(), any())).thenReturn(true);

        final MineColonyDataProvider mineColonyDataProvider = mock(MineColonyDataProvider.class);
        when(mineColonyDataProvider.getColonies()).thenReturn(colonyList);
        when(mineColonyDataProvider.getColony(1, 0)).thenReturn(colony1);
        when(mineColonyDataProvider.getColony(2, 0)).thenReturn(colony2);
        
        moduleContext = mock(ModuleContext.class);
        when(moduleContext.get(MineColonyDataProvider.class)).thenReturn(mineColonyDataProvider);

        instance = new CommandEntryPointNew();
    }

    @After
    public void tearDown()
    {
        instance = null;
        server = null;
        sender = null;
        pos = null;
    }

    public void GIVEN_no_args__DO_getTabCompletions__EXPECT_colony_colonies_citizen()
    {

        // GIVEN:
        final String[] args = new String[] {
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("colonies", "colony", "citizens", "kill", "check", "whoami", "whereami", "home", "raid-tonight", "raid-now", "rs", "rtp",
                "backup", "scan", "lootgen");
    }

    public void GIVEN_empty_args__DO_getTabCompletions__EXPECT_colony_colonies_citizen()
    {

        // GIVEN:
        final String[] args = new String[] {
                ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("colonies", "colony", "citizens", "kill", "check", "whoami", "whereami", "home", "raid-tonight", "raid-now", "rs", "rtp",
                "backup", "scan", "lootgen");
    }

    
    public void GIVEN_args_scan__DO_getTabCompletions__EXPECT_x1_x2_y1_y2_z1_z2()
    {

        // GIVEN:
        final String[] args = new String[] {
                "scan", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("player:", "x1:", "x2:", "y1:", "y2:", "z1:", "z2:", "name:");
    }

    
    public void GIVEN_args_Scan__DO_getTabCompletions__EXPECT_x1_x2_y1_y2_z1_z2()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Scan", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("player:", "x1:", "x2:", "y1:", "y2:", "z1:", "z2:", "name:");
    }

    
    public void GIVEN_args_SCAN__DO_getTabCompletions__EXPECT_x1_x2_y1_y2_z1_z2()
    {

        // GIVEN:
        final String[] args = new String[] {
                "SCAN", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("player:", "x1:", "x2:", "y1:", "y2:", "z1:", "z2:", "name:");
    }

    
    public void GIVEN_args_SCAN_x1NoColon__DO_getTabCompletions__EXPECT_x1()
    {

        // GIVEN:
        final String[] args = new String[] {
                "SCAN", "x1"
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("x1:");
    }

    
    public void GIVEN_args_SCAN_X1NoColon__DO_getTabCompletions__EXPECT_x1()
    {

        // GIVEN:
        final String[] args = new String[] {
                "SCAN", "X1"
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("x1:");
    }

    
    public void GIVEN_args_SCAN_X1_space__DO_getTabCompletions__EXPECT_1()
    {

        // GIVEN:
        final String[] args = new String[] {
                "SCAN", "X1:", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("1");
    }

    
    public void GIVEN_args_SCAN_y2_space__DO_getTabCompletions__EXPECT_2()
    {

        // GIVEN:
        final String[] args = new String[] {
                "SCAN", "y2:", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("2");
    }

    
    public void GIVEN_args_SCAN_z2_space__DO_getTabCompletions__EXPECT_3()
    {

        // GIVEN:
        final String[] args = new String[] {
                "SCAN", "z2:", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("3");
    }

    
    public void GIVEN_args_SCAN_X1_1_x2_2_y1_3_y2_4_z1_5_z2_6__DO_execute__EXPECT_scan_command_executed() throws CommandException
    {

        // GIVEN:
        final String[] args = new String[] {
                "SCAN",
                "player:", "Bob",
                "x1:", "1", "x2:", "2",
                "y1:", "3", "y2:", "4",
                "z1:", "5", "z2:", "6",
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:
                assertThat(clazz).isEqualTo(ScanCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).extracting("name").containsExactlyInAnyOrder("player", "x1", "x2", "y1", "y2", "z1", "z2", "name");

                assertThat(actionArgumentList).extracting("type").containsOnly(ActionArgumentType.ONLINE_PLAYER, ActionArgumentType.COORDINATE_X, ActionArgumentType.COORDINATE_Y,
                        ActionArgumentType.COORDINATE_Z, ActionArgumentType.STRING);
                assertThat(actionMenuState.getIntForArgument("x1")).isEqualTo(1);
                assertThat(actionMenuState.getIntForArgument("x2")).isEqualTo(2);
                assertThat(actionMenuState.getIntForArgument("y1")).isEqualTo(3);
                assertThat(actionMenuState.getIntForArgument("y2")).isEqualTo(4);
                assertThat(actionMenuState.getIntForArgument("z1")).isEqualTo(5);
                assertThat(actionMenuState.getIntForArgument("z2")).isEqualTo(6);

            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

    
    public void GIVEN_args_CLAIM__DO_execute__EXPECT_claim_command_executed() throws CommandException
    {

        // GIVEN:
        final String[] args = new String[] {
                "Colony",
                "Claim",
                "colony:", "1",
                "dimension:", "1",
                "range:", "1",
                "add:", "true"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:
                assertThat(clazz).isEqualTo(ClaimChunksCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).extracting("name").containsExactlyInAnyOrder("colony", "dimension", "range", "add");

                assertThat(actionArgumentList).extracting("type").containsOnly(ActionArgumentType.INTEGER, ActionArgumentType.INTEGER, ActionArgumentType.INTEGER, ActionArgumentType.BOOLEAN);
                assertThat(actionMenuState.getIntForArgument("range")).isEqualTo(1);
                assertThat(actionMenuState.getBooleanForArgument("add")).isEqualTo(true);
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

    
    public void GIVEN_args_citizens__DO_getTabCompletions__EXPECT_citizens()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens"
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("citizens");
    }

    
    public void GIVEN_args_citizens_space__DO_getTabCompletions__EXPECT_info()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("kill", "spawn", "info", "list", "respawn");
    }

    
    public void GIVEN_args_citizens_in__DO_getTabCompletions__EXPECT_info()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "in"
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("info");
    }

    
    public void GIVEN_args_citizens_info__DO_getTabCompletions__EXPECT_info()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info"
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("info");
    }

    
    public void GIVEN_args_citizens_info_space__DO_getTabCompletions__EXPECT_colony()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("colony:");
    }

    
    public void GIVEN_args_citizens_info_colonyNoColon_space__DO_getTabCompletions__EXPECT_1_2()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("colony:");
    }

    
    public void GIVEN_args_citizens_info_colony_space__DO_getTabCompletions__EXPECT_1_2()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("1", "2");
    }

    
    public void GIVEN_args_citizens_info_colony_1_space__DO_getTabCompletions__EXPECT_citizen()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("citizen:");
    }

    
    public void GIVEN_args_citizens_info_colony_1_citizen_space__DO_getTabCompletions__EXPECT_101_102_103_104_John_Jenna()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("101", "102", "103", "104", "John", "Jenna");
    }

    
    public void GIVEN_args_citizens_info_colony_1_citizen_J__DO_getTabCompletions__EXPECT_John_Jenna()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "J"
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("John", "Jenna");
    }

    
    public void GIVEN_args_citizens_info_colony_1_citizen_Jo__DO_getTabCompletions__EXPECT_John()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "Jo"
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("John");
    }

    
    public void GIVEN_args_citizens_info_colony_1_citizen_John_space__DO_getTabCompletions__EXPECT_A_S()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "John", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("A.", "S.");
    }

    
    public void GIVEN_args_citizens_info_colony_1_citizen_John_A_space__DO_getTabCompletions__EXPECT_A_S()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "John", "A.", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("Jones");
    }

    
    public void GIVEN_args_citizens_info_colony_1_citizen_John_S_space__DO_getTabCompletions__EXPECT_A_S()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "John", "S.", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("Smith", "Jones");
    }

    
    public void GIVEN_args_citizens_info_colony_1_citizen_John_S_Smith_space__DO_getTabCompletions__EXPECT_citizen()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "John", "S.", "Smith", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).isEmpty();
    }

    
    public void GIVEN_args_citizens_info_colony_1_citizen_1__DO_getTabCompletions__EXPECT_101_102_103_104()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "1"
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("101", "102", "103", "104");
    }

    
    public void GIVEN_args_citizens_info_colony_2_citizen_space__DO_getTabCompletions__EXPECT_201_202_203_204_205_Sally_R()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "2", "citizen:", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("201", "202", "203", "204", "205","Sally", "R");
    }

    
    public void GIVEN_args_citizens_info_colony_2_citizen_Sally_space__DO_getTabCompletions__EXPECT_Jane()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "2", "citizen:", "Sally", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("Jane", "of");
    }

    
    public void GIVEN_args_citizens_info_colony_2_citizen_SallyJaneJohnsonTheThird_space__DO_getTabCompletions__EXPECT_Banana()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "2", "citizen:", "Sally", "Jane", "Johnson", "the", "Third", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("Banana");
    }

    
    public void GIVEN_args_citizens_info_colony_2_citizen_SallyJaneJohnsonTheThird_space_B__DO_getTabCompletions__EXPECT_Banana()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "2", "citizen:", "Sally", "Jane", "Johnson", "the", "Third", "B"
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("Banana");
    }


    
    public void GIVEN_args_citizens_info_colony_1_citizen_Sally__DO_execute__EXPECT_colony_info_command_parses_Sally() throws CommandException
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "2", "citizen:", "Sally"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).isEqualTo(CitizenInfoCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).extracting("name").containsExactly("colony");
                assertThat(actionArgumentList).extracting("type").containsOnly(ActionArgumentType.COLONY);
                assertThat(actionArgumentList.get(0).getActionArgumentList()).extracting("name").containsExactly("citizen");
                assertThat(actionArgumentList.get(0).getActionArgumentList()).extracting("type").containsOnly(ActionArgumentType.CITIZEN);
                final IColony colony = actionMenuState.getColonyForArgument("colony");
                final ICitizenData citizenData = actionMenuState.getCitizenForArgument("citizen");
                assertThat(colony.getID()).isEqualTo(2);
                assertThat(citizenData.getName()).isEqualTo("Sally");
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }


    
    public void GIVEN_args_citizens_info_colony_1_citizen_Sally_Jane_Johnson_the_Third__DO_execute__EXPECT_colony_info_command_parses_SallyJaneJohnsonTheThird()
            throws CommandException
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "2", "citizen:", "Sally", "Jane", "Johnson", "the", "Third"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).isEqualTo(CitizenInfoCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).extracting("name").containsExactly("colony");
                assertThat(actionArgumentList).extracting("type").containsOnly(ActionArgumentType.COLONY);
                assertThat(actionArgumentList.get(0).getActionArgumentList()).extracting("name").containsExactly("citizen");
                assertThat(actionArgumentList.get(0).getActionArgumentList()).extracting("type").containsOnly(ActionArgumentType.CITIZEN);
                final IColony colony = actionMenuState.getColonyForArgument("colony");
                final ICitizenData citizenData = actionMenuState.getCitizenForArgument("citizen");
                assertThat(citizenData).as("citizen.getName()").isNotNull();
                assertThat(colony.getID()).isEqualTo(2);
                assertThat(citizenData.getName()).isEqualTo("Sally Jane Johnson the Third");
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

    
    public void GIVEN_args_citizens_info_colony_1_citizen_Sally_Jane_Johnson_the_Third_Banana_Fo_Fanna__DO_execute__EXPECT_colony_info_command_parses_SallyJaneJohnsonTheThirdBananaFoFanna() throws CommandException
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "2", "citizen:", "Sally", "Jane", "Johnson", "the", "Third", "Banana", "Fo", "Fanna"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).isEqualTo(CitizenInfoCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).extracting("name").containsExactly("colony");
                assertThat(actionArgumentList).extracting("type").containsOnly(ActionArgumentType.COLONY);
                assertThat(actionArgumentList.get(0).getActionArgumentList()).extracting("name").containsExactly("citizen");
                assertThat(actionArgumentList.get(0).getActionArgumentList()).extracting("type").containsOnly(ActionArgumentType.CITIZEN);
                final IColony colony = actionMenuState.getColonyForArgument("colony");
                final ICitizenData citizenData = actionMenuState.getCitizenForArgument("citizen");
                assertThat(citizenData).as("citizen.getName()").isNotNull();
                assertThat(colony.getID()).isEqualTo(2);
                assertThat(citizenData.getName()).isEqualTo("Sally Jane Johnson the Third Banana Fo Fanna");
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

    
    public void GIVEN_args_citizens_info_colony_1_citizen_1_space__DO_getTabCompletions__EXPECT_nothing()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "1", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        // TODO: bad argument as 1 isn't a citizen number or name
        assertThat(results).isEmpty();
    }

    
    public void GIVEN_args_citizens_info_colony_1_citizen_1_S__DO_getTabCompletions__EXPECT_nothing()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "1", "S"
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        // TODO: bad argument as 1 isn't a citizen number or name
        assertThat(results).isEmpty();
    }

    
    public void GIVEN_args_citizens_info_colony_1_citizen_1_space__DO_execute__EXPECT_bad_args() throws CommandException
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "1", ""
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                throw new IllegalAccessException("Should not reach here");
            }
        };

        try
        {
            instance.execute(server, sender, args, permissionsChecker, moduleContext);
            Fail.failBecauseExceptionWasNotThrown(CommandException.class);
        }
        catch (final CommandException e)
        {
            assertThat(e).hasMessage("/mineColonies citizens info <colony: colony-id>: invalid value '1' for required argument citizen");
        }
    }

    
    public void GIVEN_args_citizens_info_colony_1_citizen_1_S__DO_execute__EXPECT_bad_args() throws CommandException
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "1", "S"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                throw new IllegalAccessException("Should not reach here");
            }
        };

        try
        {
            instance.execute(server, sender, args, permissionsChecker, moduleContext);
            Fail.failBecauseExceptionWasNotThrown(CommandException.class);
        }
        catch (final CommandException e)
        {
            assertThat(e).hasMessage("/mineColonies citizens info <colony: colony-id>: invalid value '1' for required argument citizen");
        }
    }

    
    public void GIVEN_args_citizens_info_colony_1_citizen_John_S_Smith__DO_execute__EXPECT_colony_info_command_executed() throws CommandException
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "John", "S.", "Smith"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).isEqualTo(CitizenInfoCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).extracting("name").containsExactly("colony");
                assertThat(actionArgumentList).extracting("type").containsOnly(ActionArgumentType.COLONY);
                assertThat(actionArgumentList.get(0).getActionArgumentList()).extracting("name").containsExactly("citizen");
                assertThat(actionArgumentList.get(0).getActionArgumentList()).extracting("type").containsOnly(ActionArgumentType.CITIZEN);
                final IColony colony = actionMenuState.getColonyForArgument("colony");
                final ICitizenData citizenData = actionMenuState.getCitizenForArgument("citizen");
                assertThat(colony.getID()).isEqualTo(1);
                assertThat(citizenData.getName()).isEqualTo("John S. Smith");
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

    
    public void GIVEN_args_colony_space__DO_getTabCompletions__EXPECT_ownerchange()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Colony", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("addofficer", "loadbackup", "barbarians", "shl", "delete", "deletable", "info", "ownerchange", "raid", "raid-tonight", "refresh", "teleport",
                "claim");
    }

    
    public void GIVEN_args_colony_ownerchange_space__DO_getTabCompletions__EXPECT_colony_player()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Colony", "ownerchange", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("colony:", "player:");
    }

    
    public void GIVEN_args_colony_ownerchange_colony_space__DO_getTabCompletions__EXPECT_1_2()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Colony", "ownerchange", "colony:", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("1", "2");
    }

    
    public void GIVEN_args_colony_ownerchange_player_space__DO_getTabCompletions__EXPECT_Bob_Sally()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Colony", "ownerchange", "player:", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("Bob", "Sally");
    }

    
    public void GIVEN_args_colony_ownerchange_player_B__DO_getTabCompletions__EXPECT_Bob()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Colony", "ownerchange", "player:", "B"
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("Bob");
    }

    
    public void GIVEN_args_colony_ownerchange_player_Bob_space__DO_getTabCompletions__EXPECT_colony()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Colony", "ownerchange", "player:", "Bob", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("colony:");
    }

    
    public void GIVEN_args_colony_ownerchange_player_Bob_colony_space__DO_getTabCompletions__EXPECT_1_2()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Colony", "ownerchange", "player:", "Bob", "colony:", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("1", "2");
    }

    
    public void GIVEN_args_colony_ownerchange_player_Bob_colony_1_space__DO_getTabCompletions__EXPECT_nothing()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Colony", "ownerchange", "player:", "Bob", "colony:", "1", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).isEmpty();
    }

    
    public void GIVEN_args_colony_ownerchange_colony_1_player_Bob_space__DO_execute__EXPECT_ChangeColonyOwnerCommand_executed() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "Colony", "ownerchange", "colony:", "1", "player:", "Bob", ""
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).isEqualTo(ChangeColonyOwnerCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).extracting("name").containsExactlyInAnyOrder("player", "colony");
                assertThat(actionArgumentList).extracting("type").containsOnly(ActionArgumentType.PLAYER, ActionArgumentType.COLONY);
                final ServerPlayerEntity player = actionMenuState.getPlayerForArgument("player");
                final IColony colony = actionMenuState.getColonyForArgument("colony");
                assertThat(player.getName()).isEqualTo("Bob");
                assertThat(colony.getID()).isEqualTo(1);
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

    
    public void GIVEN_args_colony_ownerchange_player_Bob_colony_1_space__DO_execute__EXPECT_ChangeColonyOwnerCommand_executed() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "Colony", "ownerchange", "player:", "Bob", "colony:", "1", ""
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).isEqualTo(ChangeColonyOwnerCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).extracting("name").containsExactlyInAnyOrder("player", "colony");
                assertThat(actionArgumentList).extracting("type").containsOnly(ActionArgumentType.PLAYER, ActionArgumentType.COLONY);
                final ServerPlayerEntity player = actionMenuState.getPlayerForArgument("player");
                final IColony colony = actionMenuState.getColonyForArgument("colony");
                assertThat(player.getName()).isEqualTo("Bob");
                assertThat(colony.getID()).isEqualTo(1);
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

    public void GIVEN_noargs__DO_execute__EXPECT_throwUsage()
    {

        // GIVEN:
        final String[] args = new String[] {
                ""
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                throw new IllegalAccessException("Should not reach here");
            }
        };

        try
        {
            instance.execute(server, sender, args, permissionsChecker, moduleContext);
            Fail.failBecauseExceptionWasNotThrown(CommandException.class);
        }
        catch (final CommandException e)
        {
            assertThat(e).hasMessage("/mineColonies <colonies|kill|colony|citizens|rs|rtp|backup|home|raid-tonight|raid-now|check|whoami|whereami|scan|lootGen>");
        }
    }

    
    public void GIVEN_args_citizens__DO_execute__EXPECT_throwUsage()
    {

        // GIVEN:
        final String[] args = new String[] {
                "citizens"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                throw new IllegalAccessException("Should not reach here");
            }
        };

        try
        {
            instance.execute(server, sender, args, permissionsChecker, moduleContext);
            Fail.failBecauseExceptionWasNotThrown(CommandException.class);
        }
        catch (final CommandException e)
        {
            assertThat(e).hasMessage("/mineColonies citizens <spawn|list|kill|respawn|info>");
        }
    }

    
    public void GIVEN_args_citizens_info__DO_execute__EXPECT_throwUsage()
    {

        // GIVEN:
        final String[] args = new String[] {
                "citizens", "info"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                throw new IllegalAccessException("Should not reach here");
            }
        };

        try
        {
            instance.execute(server, sender, args, permissionsChecker, moduleContext);
            Fail.failBecauseExceptionWasNotThrown(CommandException.class);
        }
        catch (final CommandException e)
        {
            assertThat(e).hasMessage("/mineColonies citizens info <colony: colony-id>: missing required parameter colony");
        }
    }

    
    public void GIVEN_args_citizens_info_colonyNoColon__DO_execute__EXPECT_throwUsage()
    {

        // GIVEN:
        final String[] args = new String[] {
                "citizens", "info", "colony"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                throw new IllegalAccessException("Should not reach here");
            }
        };

        try
        {
            instance.execute(server, sender, args, permissionsChecker, moduleContext);
            Fail.failBecauseExceptionWasNotThrown(CommandException.class);
        }
        catch (final CommandException e)
        {
            assertThat(e).hasMessage("/mineColonies citizens info <colony: colony-id>: missing required parameter colony");
        }
    }

    
    public void GIVEN_args_citizens_info_colony__DO_execute__EXPECT_throwUsage()
    {

        // GIVEN:
        final String[] args = new String[] {
                "citizens", "info", "colony:"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                throw new IllegalAccessException("Should not reach here");
            }
        };

        try
        {
            instance.execute(server, sender, args, permissionsChecker, moduleContext);
            Fail.failBecauseExceptionWasNotThrown(CommandException.class);
        }
        catch (final CommandException e)
        {
            assertThat(e).hasMessage("/mineColonies citizens info <colony: colony-id>: no value specified for required argument colony");
        }
    }

    
    public void GIVEN_args_citizens_info_colony_BAD__DO_execute__EXPECT_throwUsage()
    {

        // GIVEN:
        final String[] args = new String[] {
                "citizens", "info", "colony:", "BAD"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                throw new IllegalAccessException("Should not reach here");
            }
        };

        try
        {
            instance.execute(server, sender, args, permissionsChecker, moduleContext);
            Fail.failBecauseExceptionWasNotThrown(CommandException.class);
        }
        catch (final CommandException e)
        {
            assertThat(e).hasMessage("/mineColonies citizens info <colony: colony-id>: invalid value 'BAD' for required argument colony");
        }
    }

//    
//    public void GIVEN_args_colony_info_sender_is_not_player__DO_execute__EXPECT_sendMsg() throws CommandException
//    {
//
//        // GIVEN:
//        final String[] args = new String[] {
//                "colony", "info"
//        };
//
//        // DO:
//
//        // WARNING -- this test runs an actual command!
//        
//        // Sender is not a player
//        instance.execute(server, sender, args, permissionsChecker, moduleContext);
//        verify(sender, times(1)).sendMessage(any());
//    }
//
//    
//    public void GIVEN_args_colony_info_sender_is_a_player__DO_execute__EXPECT_sendMsg() throws CommandException
//    {
//
//        // GIVEN:
//        final String[] args = new String[] {
//                "colony", "info"
//        };
//
//        // Sender is a player
//        // TODO: make this playerSender the owner of a colony
//        final ServerPlayerEntity playerSender = mock(ServerPlayerEntity.class);
//
//        // DO:
//
//
//        // WARNING -- this test runs an actual command!
//
//        instance.execute(server, playerSender, args, permissionsChecker, moduleContext);
//        verify(playerSender, times(1)).sendMessage(any());
//    }

    
    public void GIVEN_args_colony_delete_colony_1_canDestroy_space__DO_getTabCompletions__EXPECT_true_false() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "Colony", "delete", "colony:", "1", "canDestroy:", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("true", "false");
    }

    
    public void GIVEN_args_colonies_list_page_space__DO_getTabCompletions__EXPECT_nothing() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "colonies", "list", "page:", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos, moduleContext);

        // EXPECT:
        assertThat(results).isEmpty();
    }

    
    public void GIVEN_args_colony_delete_colony_1_canDestroy_true_confirmDelete_true__DO_execute__EXPECT_DeleteColonyCommand_executed() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "Colony", "delete", "colony:", "1", "canDestroy:", "true", "confirmDelete:", "true"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).as("command class").isEqualTo(DeleteColonyCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).as("actionArgumentList name").extracting("name").containsExactlyInAnyOrder("colony", "canDestroy", "confirmDelete");
                assertThat(actionArgumentList).as("actionArgumentList type").extracting("type").containsOnly(ActionArgumentType.BOOLEAN, ActionArgumentType.COLONY);
                final IColony colony = actionMenuState.getColonyForArgument("colony");
                assertThat(colony).as("colony").isNotNull();
                assertThat(colony.getID()).as("colony.getID()").isEqualTo(1);
                final Boolean canDestroyBoolean = (Boolean) actionMenuState.getBooleanForArgument("canDestroy");
                assertThat(canDestroyBoolean).as("canDestroyBoolean").isTrue();
                final boolean canDestroy = actionMenuState.getBooleanValueForArgument("canDestroy", false);
                assertThat(canDestroy).as("canDestroy").isTrue();
                final Boolean confirmDeleteBoolean = (Boolean) actionMenuState.getBooleanForArgument("confirmDelete");
                assertThat(confirmDeleteBoolean).as("confirmDeleteBoolean").isTrue();
                final boolean confirmDelete = actionMenuState.getBooleanValueForArgument("confirmDelete", false);
                assertThat(confirmDelete).as("confirmDelete").isTrue();
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

    
    public void GIVEN_args_colony_delete_colony_1__DO_execute__EXPECT_DeleteColonyCommand_executed() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "Colony", "delete", "colony:", "1"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).as("command class").isEqualTo(DeleteColonyCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).as("actionArgumentList name").extracting("name").containsExactlyInAnyOrder("colony", "canDestroy", "confirmDelete");
                assertThat(actionArgumentList).as("actionArgumentList type").extracting("type").containsOnly(ActionArgumentType.BOOLEAN, ActionArgumentType.COLONY);
                final IColony colony = actionMenuState.getColonyForArgument("colony");
                assertThat(colony.getID()).as("colony.getID()").isEqualTo(1);
                final Boolean canDestroyBoolean = (Boolean) actionMenuState.getBooleanForArgument("canDestroy");
                assertThat(canDestroyBoolean).as("canDestroyBoolean").isNull();
                final boolean canDestroy = actionMenuState.getBooleanValueForArgument("canDestroy", false);
                assertThat(canDestroy).as("canDestroy").isFalse();
                final Boolean confirmDeleteBoolean = (Boolean) actionMenuState.getBooleanForArgument("confirmDelete");
                assertThat(confirmDeleteBoolean).as("confirmDeleteBoolean").isNull();
                final boolean confirmDelete = actionMenuState.getBooleanValueForArgument("confirmDelete", false);
                assertThat(confirmDelete).as("confirmDelete").isFalse();
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

    
    public void GIVEN_args_colonies_list_page_2_abandonedSinceTimeInHours_3__DO_execute__EXPECT_ListColoniesCommand_executed() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "colonies", "list", "page:", "2", "abandonedSinceTimeInHours:", "3"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).as("command class").isEqualTo(ListColoniesCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).as("actionArgumentList name").extracting("name").containsExactlyInAnyOrder("page", "abandonedSinceTimeInHours");
                assertThat(actionArgumentList).as("actionArgumentList type").extracting("type").containsOnly(ActionArgumentType.INTEGER);
                final Integer pageInteger = (Integer) actionMenuState.getIntForArgument("page");
                assertThat(pageInteger).as("pageInteger").isEqualTo(2);
                final int page = actionMenuState.getIntValueForArgument("page", 1);
                assertThat(page).as("page").isEqualTo(2);
                final Integer abandonedSinceTimeInHoursInteger = (Integer) actionMenuState.getIntForArgument("abandonedSinceTimeInHours");
                assertThat(abandonedSinceTimeInHoursInteger).as("abandonedSinceTimeInHoursInteger").isEqualTo(3);
                final int abandonedSinceTimeInHours = actionMenuState.getIntValueForArgument("abandonedSinceTimeInHours", 1);
                assertThat(abandonedSinceTimeInHours).as("abandonedSinceTimeInHours").isEqualTo(3);
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

    
    public void GIVEN_args_colonies_list_page_2__DO_execute__EXPECT_ListColoniesCommand_executed() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "colonies", "list", "page:", "2"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).as("command class").isEqualTo(ListColoniesCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).as("actionArgumentList name").extracting("name").containsExactlyInAnyOrder("page", "abandonedSinceTimeInHours");
                assertThat(actionArgumentList).as("actionArgumentList type").extracting("type").containsOnly(ActionArgumentType.INTEGER);
                final Integer pageInteger = (Integer) actionMenuState.getIntForArgument("page");
                assertThat(pageInteger).as("pageInteger").isEqualTo(2);
                final int page = actionMenuState.getIntValueForArgument("page", 1);
                assertThat(page).as("page").isEqualTo(2);
                final Integer abandonedSinceTimeInHoursInteger = (Integer) actionMenuState.getIntForArgument("abandonedSinceTimeInHours");
                assertThat(abandonedSinceTimeInHoursInteger).as("abandonedSinceTimeInHoursInteger").isNull();
                final int abandonedSinceTimeInHours = actionMenuState.getIntValueForArgument("abandonedSinceTimeInHours", 1);
                assertThat(abandonedSinceTimeInHours).as("abandonedSinceTimeInHours").isEqualTo(1);
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

    
    public void GIVEN_args_check_confirmDelete_true__DO_execute__EXPECT_CheckForAutoDeletesCommand_executed() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "check", "confirmDelete:", "true"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).as("command class").isEqualTo(CheckForAutoDeletesCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).as("actionArgumentList name").extracting("name").containsExactlyInAnyOrder("confirmDelete");
                assertThat(actionArgumentList).as("actionArgumentList type").extracting("type").containsOnly(ActionArgumentType.BOOLEAN);
                final Boolean confirmDeleteBoolean = (Boolean) actionMenuState.getBooleanForArgument("confirmDelete");
                assertThat(confirmDeleteBoolean).as("confirmDeleteBoolean").isTrue();
                final boolean confirmDelete = actionMenuState.getBooleanValueForArgument("confirmDelete", false);
                assertThat(confirmDelete).as("confirmDelete").isTrue();
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

    
    public void GIVEN_args_check_confirmDelete_t__DO_execute__EXPECT_CheckForAutoDeletesCommand_executed() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "check", "confirmDelete:", "t"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).as("command class").isEqualTo(CheckForAutoDeletesCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).as("actionArgumentList name").extracting("name").containsExactlyInAnyOrder("confirmDelete");
                assertThat(actionArgumentList).as("actionArgumentList type").extracting("type").containsOnly(ActionArgumentType.BOOLEAN);
                final Boolean confirmDeleteBoolean = (Boolean) actionMenuState.getBooleanForArgument("confirmDelete");
                assertThat(confirmDeleteBoolean).as("confirmDeleteBoolean").isTrue();
                final boolean confirmDelete = actionMenuState.getBooleanValueForArgument("confirmDelete", false);
                assertThat(confirmDelete).as("confirmDelete").isTrue();
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

    
    public void GIVEN_args_check_confirmDelete_yes__DO_execute__EXPECT_CheckForAutoDeletesCommand_executed() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "check", "confirmDelete:", "yes"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).as("command class").isEqualTo(CheckForAutoDeletesCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).as("actionArgumentList name").extracting("name").containsExactlyInAnyOrder("confirmDelete");
                assertThat(actionArgumentList).as("actionArgumentList type").extracting("type").containsOnly(ActionArgumentType.BOOLEAN);
                final Boolean confirmDeleteBoolean = (Boolean) actionMenuState.getBooleanForArgument("confirmDelete");
                assertThat(confirmDeleteBoolean).as("confirmDeleteBoolean").isTrue();
                final boolean confirmDelete = actionMenuState.getBooleanValueForArgument("confirmDelete", false);
                assertThat(confirmDelete).as("confirmDelete").isTrue();
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

    
    public void GIVEN_args_check_confirmDelete_y__DO_execute__EXPECT_CheckForAutoDeletesCommand_executed() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "check", "confirmDelete:", "y"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).as("command class").isEqualTo(CheckForAutoDeletesCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).as("actionArgumentList name").extracting("name").containsExactlyInAnyOrder("confirmDelete");
                assertThat(actionArgumentList).as("actionArgumentList type").extracting("type").containsOnly(ActionArgumentType.BOOLEAN);
                final Boolean confirmDeleteBoolean = (Boolean) actionMenuState.getBooleanForArgument("confirmDelete");
                assertThat(confirmDeleteBoolean).as("confirmDeleteBoolean").isTrue();
                final boolean confirmDelete = actionMenuState.getBooleanValueForArgument("confirmDelete", false);
                assertThat(confirmDelete).as("confirmDelete").isTrue();
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

    
    public void GIVEN_args_check_confirmDelete_1__DO_execute__EXPECT_CheckForAutoDeletesCommand_executed() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "check", "confirmDelete:", "1"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).as("command class").isEqualTo(CheckForAutoDeletesCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).as("actionArgumentList name").extracting("name").containsExactlyInAnyOrder("confirmDelete");
                assertThat(actionArgumentList).as("actionArgumentList type").extracting("type").containsOnly(ActionArgumentType.BOOLEAN);
                final Boolean confirmDeleteBoolean = (Boolean) actionMenuState.getBooleanForArgument("confirmDelete");
                assertThat(confirmDeleteBoolean).as("confirmDeleteBoolean").isTrue();
                final boolean confirmDelete = actionMenuState.getBooleanValueForArgument("confirmDelete", false);
                assertThat(confirmDelete).as("confirmDelete").isTrue();
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

    
    public void GIVEN_args_check_confirmDelete_false__DO_execute__EXPECT_CheckForAutoDeletesCommand_executed() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "check", "confirmDelete:", "false"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).as("command class").isEqualTo(CheckForAutoDeletesCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).as("actionArgumentList name").extracting("name").containsExactlyInAnyOrder("confirmDelete");
                assertThat(actionArgumentList).as("actionArgumentList type").extracting("type").containsOnly(ActionArgumentType.BOOLEAN);
                final Boolean confirmDeleteBoolean = (Boolean) actionMenuState.getBooleanForArgument("confirmDelete");
                assertThat(confirmDeleteBoolean).as("confirmDeleteBoolean").isFalse();
                final boolean confirmDelete = actionMenuState.getBooleanValueForArgument("confirmDelete", true);
                assertThat(confirmDelete).as("confirmDelete").isFalse();
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

    
    public void GIVEN_args_check_confirmDelete_f__DO_execute__EXPECT_CheckForAutoDeletesCommand_executed() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "check", "confirmDelete:", "f"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).as("command class").isEqualTo(CheckForAutoDeletesCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).as("actionArgumentList name").extracting("name").containsExactlyInAnyOrder("confirmDelete");
                assertThat(actionArgumentList).as("actionArgumentList type").extracting("type").containsOnly(ActionArgumentType.BOOLEAN);
                final Boolean confirmDeleteBoolean = (Boolean) actionMenuState.getBooleanForArgument("confirmDelete");
                assertThat(confirmDeleteBoolean).as("confirmDeleteBoolean").isFalse();
                final boolean confirmDelete = actionMenuState.getBooleanValueForArgument("confirmDelete", true);
                assertThat(confirmDelete).as("confirmDelete").isFalse();
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

    
    public void GIVEN_args_check_confirmDelete_no__DO_execute__EXPECT_CheckForAutoDeletesCommand_executed() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "check", "confirmDelete:", "no"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).as("command class").isEqualTo(CheckForAutoDeletesCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).as("actionArgumentList name").extracting("name").containsExactlyInAnyOrder("confirmDelete");
                assertThat(actionArgumentList).as("actionArgumentList type").extracting("type").containsOnly(ActionArgumentType.BOOLEAN);
                final Boolean confirmDeleteBoolean = (Boolean) actionMenuState.getBooleanForArgument("confirmDelete");
                assertThat(confirmDeleteBoolean).as("confirmDeleteBoolean").isFalse();
                final boolean confirmDelete = actionMenuState.getBooleanValueForArgument("confirmDelete", true);
                assertThat(confirmDelete).as("confirmDelete").isFalse();
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

    
    public void GIVEN_args_check_confirmDelete_n__DO_execute__EXPECT_CheckForAutoDeletesCommand_executed() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "check", "confirmDelete:", "n"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).as("command class").isEqualTo(CheckForAutoDeletesCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).as("actionArgumentList name").extracting("name").containsExactlyInAnyOrder("confirmDelete");
                assertThat(actionArgumentList).as("actionArgumentList type").extracting("type").containsOnly(ActionArgumentType.BOOLEAN);
                final Boolean confirmDeleteBoolean = (Boolean) actionMenuState.getBooleanForArgument("confirmDelete");
                assertThat(confirmDeleteBoolean).as("confirmDeleteBoolean").isFalse();
                final boolean confirmDelete = actionMenuState.getBooleanValueForArgument("confirmDelete", true);
                assertThat(confirmDelete).as("confirmDelete").isFalse();
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

        public void GIVEN_args_check_confirmDelete_0__DO_execute__EXPECT_CheckForAutoDeletesCommand_executed() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "check", "confirmDelete:", "0"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).as("command class").isEqualTo(CheckForAutoDeletesCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).as("actionArgumentList name").extracting("name").containsExactlyInAnyOrder("confirmDelete");
                assertThat(actionArgumentList).as("actionArgumentList type").extracting("type").containsOnly(ActionArgumentType.BOOLEAN);
                final Boolean confirmDeleteBoolean = (Boolean) actionMenuState.getBooleanForArgument("confirmDelete");
                assertThat(confirmDeleteBoolean).as("confirmDeleteBoolean").isFalse();
                final boolean confirmDelete = actionMenuState.getBooleanValueForArgument("confirmDelete", true);
                assertThat(confirmDelete).as("confirmDelete").isFalse();
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }

        public void GIVEN_args_delete_colony_1__DO_execute_twice__EXPECT_DeleteColonyCommand_state_2nd_time_to_be_different() throws CommandException
    {
        // GIVEN:
        final String[] argsFirstTime = new String[] {
                "colony", "delete", "colony:", "1", "confirmDelete:", "true", "canDestroy:", "true"
        };

        final String[] argsSecondTime = new String[] {
                "colony", "delete", "colony:", "1"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).as("command class").isEqualTo(DeleteColonyCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).as("actionArgumentList name").extracting("name").containsExactlyInAnyOrder("colony", "canDestroy", "confirmDelete");
                assertThat(actionArgumentList).as("actionArgumentList type").extracting("type").containsOnly(ActionArgumentType.COLONY, ActionArgumentType.BOOLEAN);
                final IColony colony = actionMenuState.getColonyForArgument("colony");
                assertThat(colony).as("colony").isNotNull();
                final Boolean canDestroy = actionMenuState.getBooleanForArgument("canDestroy");
                final Boolean confirmDelete = actionMenuState.getBooleanForArgument("confirmDelete");
                assertThat(colony.getID()).isEqualTo(1);
                assertThat(canDestroy).isTrue();
                assertThat(confirmDelete).isTrue();
            }
        };

        instance.execute(server, sender, argsFirstTime, permissionsChecker, moduleContext);

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).as("command class").isEqualTo(DeleteColonyCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).as("actionArgumentList name").extracting("name").containsExactlyInAnyOrder("colony", "canDestroy", "confirmDelete");
                assertThat(actionArgumentList).as("actionArgumentList type").extracting("type").containsOnly(ActionArgumentType.COLONY, ActionArgumentType.BOOLEAN);
                final IColony colony = actionMenuState.getColonyForArgument("colony");
                final Boolean canDestroy = actionMenuState.getBooleanForArgument("canDestroy");
                final Boolean confirmDelete = actionMenuState.getBooleanForArgument("confirmDelete");
                assertThat(colony.getID()).isEqualTo(1);
                assertThat(canDestroy).isNull();
                assertThat(confirmDelete).isNull();
            }
        };

        instance.execute(server, sender, argsSecondTime, permissionsChecker, moduleContext);
    }

    
    public void GIVEN_args_delete_colony_1__DO_execute__EXPECT_DeleteColonyCommand_executed() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "colony", "delete", "colony:", "1"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            @Override
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    @NotNull final ActionMenuState actionMenuState,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).as("command class").isEqualTo(DeleteColonyCommand.class);
                final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
                assertThat(actionArgumentList).as("actionArgumentList name").extracting("name").containsExactlyInAnyOrder("colony", "canDestroy", "confirmDelete");
                assertThat(actionArgumentList).as("actionArgumentList type").extracting("type").containsOnly(ActionArgumentType.COLONY, ActionArgumentType.BOOLEAN);
                final IColony colony = actionMenuState.getColonyForArgument("colony");
                final Boolean canDestroy = actionMenuState.getBooleanForArgument("canDestroy");
                final Boolean confirmDelete = actionMenuState.getBooleanForArgument("confirmDelete");
                assertThat(colony.getID()).isEqualTo(1);
                assertThat(canDestroy).isNull();
                assertThat(confirmDelete).isNull();
            }
        };

        instance.execute(server, sender, args, permissionsChecker, moduleContext);
    }
}
