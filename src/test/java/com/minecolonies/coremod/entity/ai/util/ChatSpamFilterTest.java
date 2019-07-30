package com.minecolonies.coremod.entity.ai.util;

import com.minecolonies.coremod.colony.ICitizenData;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.citizenhandlers.ICitizenChatHandler;
import com.minecolonies.coremod.entity.citizenhandlers.ICitizenColonyHandler;
import com.minecolonies.coremod.test.AbstractMockStaticsTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Optional;

import static com.minecolonies.coremod.entity.ai.util.ChatSpamFilter.BASE_TIMEOUT;
import static com.minecolonies.coremod.entity.ai.util.ChatSpamFilter.MAX_TIMEOUT;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ChatSpamFilterTest extends AbstractMockStaticsTest
{
    private static final String                MESSAGE_1 = "Whatever";
    private static final String                MESSAGE_2 = "Whatever2";
    @Mock
    private              EntityCitizen         citizen;
    @Mock
    private              ICitizenData          data;
    @Mock
    private              ICitizenColonyHandler ICitizenColonyHandler;
    @Mock
    private              ICitizenChatHandler   ICitizenChatHandler;

    private ChatSpamFilter filter;

    @Before
    public void setUp()
    {
        when(citizen.getCitizenData()).thenReturn(data);
        when(data.getCitizenEntity()).thenReturn(Optional.of(citizen));
        when(citizen.getOffsetTicks()).thenReturn(0);
        when(citizen.getCitizenChatHandler()).thenReturn(ICitizenChatHandler);
        when(citizen.getCitizenColonyHandler()).thenReturn(ICitizenColonyHandler);
        filter = new ChatSpamFilter(citizen.getCitizenData());
    }

    @Test
    public void testFilterFirst()
    {
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        verify(citizen.getCitizenChatHandler()).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterFiltering()
    {
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        verify(citizen.getCitizenChatHandler()).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterRelease()
    {
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        filter.requestTextStringWithoutSpam(MESSAGE_2);
        verify(citizen.getCitizenChatHandler(), times(2)).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterReleaseAndFilter()
    {
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        filter.requestTextStringWithoutSpam(MESSAGE_2);
        filter.requestTextStringWithoutSpam(MESSAGE_2);
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        verify(citizen.getCitizenChatHandler(), times(3)).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterTimeout()
    {
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT);
        filter.requestTextStringWithoutSpam(MESSAGE_1);

        verify(citizen.getCitizenChatHandler(), times(2)).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterTimeoutLonger()
    {
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT);
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT * 3);
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT * 7);
        filter.requestTextStringWithoutSpam(MESSAGE_1);

        verify(citizen.getCitizenChatHandler(), times(4)).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterTimeoutMax()
    {
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT);
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT * 3);
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT * 7);
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT * 15);
        filter.requestTextStringWithoutSpam(MESSAGE_1);

        verify(citizen.getCitizenChatHandler(), times(5)).sendLocalizedChat(any(), any());

        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT * 15 + MAX_TIMEOUT);
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT * 15 + MAX_TIMEOUT * 2);
        filter.requestTextStringWithoutSpam(MESSAGE_1);

        verify(citizen.getCitizenChatHandler(), times(7)).sendLocalizedChat(any(), any());
    }
}
