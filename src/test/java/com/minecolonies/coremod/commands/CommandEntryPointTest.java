package com.minecolonies.coremod.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.assertj.core.api.Fail;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.managers.ICitizenManager;
import com.minecolonies.coremod.commands.citizencommands.CitizenInfoCommand;
import com.minecolonies.coremod.commands.colonycommands.ChangeColonyOwnerCommand;
import com.minecolonies.coremod.commands.generalcommands.ScanCommand;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import scala.actors.threadpool.Arrays;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class CommandEntryPointTest
{
    @NotNull private MinecraftServer server;
    @NotNull private ICommandSender sender;
    @NotNull private CommandEntryPointNew instance;
    @Nullable private BlockPos pos;

    @Before
    public void setup()
    {
        final Colony colony1 = mock(Colony.class);
        when(colony1.getID()).thenReturn(1);
        final Colony colony2 = mock(Colony.class);
        when(colony2.getID()).thenReturn(2);
        @SuppressWarnings("unchecked")
        final List<Colony> colonyList = Arrays.asList(new Colony[] {colony1, colony2});

        final ICitizenManager citizenManager1 = mock(ICitizenManager.class);
        when(colony1.getCitizenManager()).thenReturn(citizenManager1);
        final ICitizenManager citizenManager2 = mock(ICitizenManager.class);
        when(colony2.getCitizenManager()).thenReturn(citizenManager2);

        final List<CitizenData> citizenDataList1 = new ArrayList<>();
        when(citizenManager1.getCitizens()).thenReturn(citizenDataList1);

        final CitizenData citizenJohnSSmith = mock(CitizenData.class);
        when(citizenJohnSSmith.getId()).thenReturn(1);
        when(citizenJohnSSmith.getName()).thenReturn("John S. Smith");

        citizenDataList1.add(citizenJohnSSmith);
        when(citizenManager1.getCitizen(1)).thenReturn(citizenJohnSSmith);

        final CitizenData citizenJohnSJones = mock(CitizenData.class);
        when(citizenJohnSJones.getId()).thenReturn(4);
        when(citizenJohnSJones.getName()).thenReturn("John S. Jones");

        citizenDataList1.add(citizenJohnSJones);
        when(citizenManager1.getCitizen(4)).thenReturn(citizenJohnSJones);

        final CitizenData citizenJohnAJones = mock(CitizenData.class);
        when(citizenJohnAJones.getId()).thenReturn(2);
        when(citizenJohnAJones.getName()).thenReturn("John A. Jones");

        citizenDataList1.add(citizenJohnAJones);
        when(citizenManager1.getCitizen(2)).thenReturn(citizenJohnAJones);

        final CitizenData citizenJennaQBar = mock(CitizenData.class);
        when(citizenJennaQBar.getId()).thenReturn(3);
        when(citizenJennaQBar.getName()).thenReturn("Jenna Q. Bar");

        citizenDataList1.add(citizenJennaQBar);
        when(citizenManager1.getCitizen(3)).thenReturn(citizenJennaQBar);

        PowerMockito.mockStatic(ColonyManager.class);
        BDDMockito.given(ColonyManager.getColonies()).willReturn(colonyList);
        BDDMockito.given(ColonyManager.getColony(1)).willReturn(colony1);
        BDDMockito.given(ColonyManager.getColony(2)).willReturn(colony2);

        final EntityPlayerMP playerBob = mock(EntityPlayerMP.class);
        when(playerBob.getName()).thenReturn("Bob");
        final EntityPlayerMP playerSally = mock(EntityPlayerMP.class);
        when(playerSally.getName()).thenReturn("Sally");

        final PlayerList serverPlayerList = mock(PlayerList.class);
        when(serverPlayerList.getPlayerByUsername("Bob")).thenReturn(playerBob);
        when(serverPlayerList.getPlayerByUsername("Sally")).thenReturn(playerSally);

        server = mock(MinecraftServer.class);
        when(server.getOnlinePlayerNames()).thenReturn(new String[] {"Bob", "Sally"});
        when(server.getPlayerList()).thenReturn(serverPlayerList);

        sender = mock(MinecraftServer.class);
        instance = new CommandEntryPointNew();
        pos = new BlockPos(1,2,3);
    }

    @After
    public void tearDown()
    {
        instance = null;
        server = null;
        sender = null;
        pos = null;
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_no_args__DO_getTabCompletions__EXPECT_colony_colonies_citizen()
    {

        // GIVEN:
        final String[] args = new String[] {
                ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("colonies", "colony", "citizens", "kill", "check", "whoami", "whereami", "home", "raid-tonight", "raid-now", "rtp", "backup",
                "scan");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_scan__DO_getTabCompletions__EXPECT_x1_x2_y1_y2_z1_z2()
    {

        // GIVEN:
        final String[] args = new String[] {
                "scan", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("x1:", "x2:", "y1:", "y2:", "z1:", "z2:");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_Scan__DO_getTabCompletions__EXPECT_x1_x2_y1_y2_z1_z2()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Scan", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("x1:", "x2:", "y1:", "y2:", "z1:", "z2:");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_SCAN__DO_getTabCompletions__EXPECT_x1_x2_y1_y2_z1_z2()
    {

        // GIVEN:
        final String[] args = new String[] {
                "SCAN", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("x1:", "x2:", "y1:", "y2:", "z1:", "z2:");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_SCAN_x1NoColon__DO_getTabCompletions__EXPECT_x1()
    {

        // GIVEN:
        final String[] args = new String[] {
                "SCAN", "x1"
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("x1:");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_SCAN_X1NoColon__DO_getTabCompletions__EXPECT_x1()
    {

        // GIVEN:
        final String[] args = new String[] {
                "SCAN", "X1"
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("x1:");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_SCAN_X1_space__DO_getTabCompletions__EXPECT_1()
    {

        // GIVEN:
        final String[] args = new String[] {
                "SCAN", "X1:", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("1");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_SCAN_y2_space__DO_getTabCompletions__EXPECT_2()
    {

        // GIVEN:
        final String[] args = new String[] {
                "SCAN", "y2:", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("2");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_SCAN_z2_space__DO_getTabCompletions__EXPECT_3()
    {

        // GIVEN:
        final String[] args = new String[] {
                "SCAN", "z2:", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("3");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_SCAN_X1_1_x2_2_y1_3_y2_4_z1_5_z2_6__DO_execute__EXPECT_scan_command_executed() throws CommandException
    {

        // GIVEN:
        final String[] args = new String[] {
                "SCAN",
                "X1:", "1", "x2:", "2",
                "y1:", "3", "y2:", "4",
                "z1:", "5", "z2:", "6",
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    final Map<String, Object> argumentValueByActionArgumentNameMap,
                    final List<ActionArgument> actionArgumentList,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).isEqualTo(ScanCommand.class);
                assertThat(actionArgumentList).extracting("name").containsExactlyInAnyOrder("x1", "x2", "y1", "y2", "z1", "z2");
                assertThat(actionArgumentList).extracting("type").containsOnly(ActionArgumentType.CoordinateX, ActionArgumentType.CoordinateY, ActionArgumentType.CoordinateZ);
                assertThat(argumentValueByActionArgumentNameMap.get("x1")).isEqualTo(1);
                assertThat(argumentValueByActionArgumentNameMap.get("x2")).isEqualTo(2);
                assertThat(argumentValueByActionArgumentNameMap.get("y1")).isEqualTo(3);
                assertThat(argumentValueByActionArgumentNameMap.get("y2")).isEqualTo(4);
                assertThat(argumentValueByActionArgumentNameMap.get("z1")).isEqualTo(5);
                assertThat(argumentValueByActionArgumentNameMap.get("z2")).isEqualTo(6);
            }
        };

        instance.execute(server, sender, args);
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens__DO_getTabCompletions__EXPECT_citizens()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens"
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("citizens");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens_space__DO_getTabCompletions__EXPECT_info()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("kill", "info", "list", "respawn");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens_in__DO_getTabCompletions__EXPECT_info()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "in"
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("info");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens_info__DO_getTabCompletions__EXPECT_info()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info"
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("info");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens_info_space__DO_getTabCompletions__EXPECT_colony()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("colony:");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens_info_colonyNoColon_space__DO_getTabCompletions__EXPECT_1_2()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("colony:");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens_info_colony_space__DO_getTabCompletions__EXPECT_1_2()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("1", "2");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens_info_colony_1_space__DO_getTabCompletions__EXPECT_citizen()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("citizen:");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens_info_colony_1_citizen_J__DO_getTabCompletions__EXPECT_John_Jenna()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "J"
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("John", "Jenna");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens_info_colony_1_citizen_Jo__DO_getTabCompletions__EXPECT_John()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "Jo"
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("John");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens_info_colony_1_citizen_John_space__DO_getTabCompletions__EXPECT_A_S()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "John", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("A.", "S.");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens_info_colony_1_citizen_John_A_space__DO_getTabCompletions__EXPECT_A_S()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "John", "A.", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("Jones");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens_info_colony_1_citizen_John_S_space__DO_getTabCompletions__EXPECT_A_S()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "John", "S.", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("Smith", "Jones");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens_info_colony_1_citizen_John_S_Smith_space__DO_getTabCompletions__EXPECT_citizen()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "John", "S.", "Smith", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).isEmpty();
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens_info_colony_1_citizen_1_space__DO_getTabCompletions__EXPECT_citizen()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "1", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).isEmpty();
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens_info_colony_1_citizen_John_S_Smith__DO_execute__EXPECT_colony_info_command_executed() throws CommandException
    {

        // GIVEN:
        final String[] args = new String[] {
                "Citizens", "info", "colony:", "1", "citizen:", "John", "S.", "Smith"
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    final Map<String, Object> argumentValueByActionArgumentNameMap,
                    final List<ActionArgument> actionArgumentList,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).isEqualTo(CitizenInfoCommand.class);
                assertThat(actionArgumentList).extracting("name").containsExactly("colony", "citizen");
                assertThat(actionArgumentList).extracting("type").containsOnly(ActionArgumentType.Colony, ActionArgumentType.Citizen);
                final Colony colony = (Colony) argumentValueByActionArgumentNameMap.get("colony");
                final CitizenData citizenData = (CitizenData) argumentValueByActionArgumentNameMap.get("citizen");
                assertThat(colony.getID()).isEqualTo(1);
                assertThat(citizenData.getName()).isEqualTo("John S. Smith");
            }
        };

        instance.execute(server, sender, args);
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_colony_space__DO_getTabCompletions__EXPECT_ownerchange()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Colony", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("addofficer", "barbarians", "delete", "deletable", "info", "ownerchange", "raid", "raid-tonight", "refresh", "teleport");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_colony_ownerchange_space__DO_getTabCompletions__EXPECT_colony_player()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Colony", "ownerchange", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("colony:", "player:");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_colony_ownerchange_colony_space__DO_getTabCompletions__EXPECT_1_2()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Colony", "ownerchange", "colony:", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("1", "2");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_colony_ownerchange_player_space__DO_getTabCompletions__EXPECT_Bob_Sally()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Colony", "ownerchange", "player:", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("Bob", "Sally");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_colony_ownerchange_player_B__DO_getTabCompletions__EXPECT_Bob()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Colony", "ownerchange", "player:", "B"
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("Bob");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_colony_ownerchange_player_Bob_space__DO_getTabCompletions__EXPECT_colony()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Colony", "ownerchange", "player:", "Bob", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("colony:");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_colony_ownerchange_player_Bob_colony_space__DO_getTabCompletions__EXPECT_1_2()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Colony", "ownerchange", "player:", "Bob", "colony:", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).containsExactlyInAnyOrder("1", "2");
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_colony_ownerchange_player_Bob_colony_1_space__DO_getTabCompletions__EXPECT_nothing()
    {

        // GIVEN:
        final String[] args = new String[] {
                "Colony", "ownerchange", "player:", "Bob", "colony:", "1", ""
        };

        // DO:

        final List<String> results = instance.getTabCompletions(server, sender, args, pos);

        // EXPECT:
        assertThat(results).isEmpty();
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_colony_ownerchange_colony_1_player_Bob_space__DO_execute__EXPECT_ChangeColonyOwnerCommand_executed() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "Colony", "ownerchange", "colony:", "1", "player:", "Bob", ""
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    final Map<String, Object> argumentValueByActionArgumentNameMap,
                    final List<ActionArgument> actionArgumentList,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).isEqualTo(ChangeColonyOwnerCommand.class);
                assertThat(actionArgumentList).extracting("name").containsExactlyInAnyOrder("player", "colony");
                assertThat(actionArgumentList).extracting("type").containsOnly(ActionArgumentType.Player, ActionArgumentType.Colony);
                final EntityPlayerMP player = (EntityPlayerMP) argumentValueByActionArgumentNameMap.get("player");
                final Colony colony = (Colony) argumentValueByActionArgumentNameMap.get("colony");
                assertThat(player.getName()).isEqualTo("Bob");
                assertThat(colony.getID()).isEqualTo(1);
            }
        };

        instance.execute(server, sender, args);
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_colony_ownerchange_player_Bob_colony_1_space__DO_execute__EXPECT_ChangeColonyOwnerCommand_executed() throws CommandException
    {
        // GIVEN:
        final String[] args = new String[] {
                "Colony", "ownerchange", "player:", "Bob", "colony:", "1", ""
        };

        // DO:

        instance = new CommandEntryPointNew()
        {
            protected void createInstanceAndExecute(final MinecraftServer myServer, final ICommandSender mySender,
                    final Map<String, Object> argumentValueByActionArgumentNameMap,
                    final List<ActionArgument> actionArgumentList,
                    final Class<? extends IActionCommand> clazz)
                    throws InstantiationException, IllegalAccessException, CommandException
            {
                // EXPECT:

                assertThat(clazz).isEqualTo(ChangeColonyOwnerCommand.class);
                assertThat(actionArgumentList).extracting("name").containsExactlyInAnyOrder("player", "colony");
                assertThat(actionArgumentList).extracting("type").containsOnly(ActionArgumentType.Player, ActionArgumentType.Colony);
                final EntityPlayerMP player = (EntityPlayerMP) argumentValueByActionArgumentNameMap.get("player");
                final Colony colony = (Colony) argumentValueByActionArgumentNameMap.get("colony");
                assertThat(player.getName()).isEqualTo("Bob");
                assertThat(colony.getID()).isEqualTo(1);
            }
        };

        instance.execute(server, sender, args);
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_noargs__DO_execute__EXPECT_throwUsage()
    {

        // GIVEN:
        final String[] args = new String[] {
                ""
        };

        // DO:

        try
        {
            instance.execute(server, sender, args);
            Fail.failBecauseExceptionWasNotThrown(CommandException.class);
        }
        catch (final CommandException e)
        {
            assertThat(e).hasMessage("/mineColonies <colonies|kill|colony|citizens|rtp|backup|home|raid-tonight|raid-now|check|whoami|whereami|scan>");
        }
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens__DO_execute__EXPECT_throwUsage()
    {

        // GIVEN:
        final String[] args = new String[] {
                "citizens"
        };

        // DO:

        try
        {
            instance.execute(server, sender, args);
            Fail.failBecauseExceptionWasNotThrown(CommandException.class);
        }
        catch (final CommandException e)
        {
            assertThat(e).hasMessage("/mineColonies citizens <list|kill|respawn|info>");
        }
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens_info__DO_execute__EXPECT_throwUsage()
    {

        // GIVEN:
        final String[] args = new String[] {
                "citizens", "info"
        };

        // DO:

        try
        {
            instance.execute(server, sender, args);
            Fail.failBecauseExceptionWasNotThrown(CommandException.class);
        }
        catch (final CommandException e)
        {
            assertThat(e).hasMessage("/mineColonies citizens info <colony: colony-id>: missing required parameter colony");
        }
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens_info_colonyNoColon__DO_execute__EXPECT_throwUsage()
    {

        // GIVEN:
        final String[] args = new String[] {
                "citizens", "info", "colony"
        };

        // DO:

        try
        {
            instance.execute(server, sender, args);
            Fail.failBecauseExceptionWasNotThrown(CommandException.class);
        }
        catch (final CommandException e)
        {
            assertThat(e).hasMessage("/mineColonies citizens info <colony: colony-id>: missing required parameter colony");
        }
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens_info_colony__DO_execute__EXPECT_throwUsage()
    {

        // GIVEN:
        final String[] args = new String[] {
                "citizens", "info", "colony:"
        };

        // DO:

        try
        {
            instance.execute(server, sender, args);
            Fail.failBecauseExceptionWasNotThrown(CommandException.class);
        }
        catch (final CommandException e)
        {
            assertThat(e).hasMessage("/mineColonies citizens info <colony: colony-id>: no value specified for required argument colony");
        }
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_citizens_info_colony_BAD__DO_execute__EXPECT_throwUsage()
    {

        // GIVEN:
        final String[] args = new String[] {
                "citizens", "info", "colony:", "BAD"
        };

        // DO:

        try
        {
            instance.execute(server, sender, args);
            Fail.failBecauseExceptionWasNotThrown(CommandException.class);
        }
        catch (final CommandException e)
        {
            assertThat(e).hasMessage("/mineColonies citizens info <colony: colony-id>: invalid value 'BAD' for required argument colony");
        }
    }

    @Test
    @PrepareForTest(ColonyManager.class)
    public void GIVEN_args_colony_info__DO_execute__EXPECT_throwUsage()
    {

        // GIVEN:
        final String[] args = new String[] {
                "colony", "info"
        };

        // DO:

        try
        {
            instance.execute(server, sender, args);
            Fail.failBecauseExceptionWasNotThrown(CommandException.class);
        }
        catch (final CommandException e)
        {
            assertThat(e).hasMessage("/mineColonies citizens info <colony: colony-id>: missing required parameter colony");
        }
    }
}
