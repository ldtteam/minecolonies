package com.minecolonies.coremod.entity.ai.util;

import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.test.AbstractMockStaticsTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.minecolonies.coremod.entity.ai.util.ChatSpamFilter.BASE_TIMEOUT;
import static com.minecolonies.coremod.entity.ai.util.ChatSpamFilter.MAX_TIMEOUT;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ChatSpamFilterTest extends AbstractMockStaticsTest
{
    private static final String MESSAGE_1 = "Whatever";
    private static final String MESSAGE_2 = "Whatever2";
    @Mock
    private EntityCitizen  citizen;
    private ChatSpamFilter filter;

    @Before
    public void setUp()
    {
        filter = new ChatSpamFilter(citizen);
        when(citizen.getOffsetTicks()).thenReturn(0);
    }

    @Test
    public void testFilterFirst()
    {
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        verify(citizen).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterFiltering()
    {
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        verify(citizen).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterRelease()
    {
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        filter.requestTextStringWithoutSpam(MESSAGE_2);
        verify(citizen, times(2)).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterReleaseAndFilter()
    {
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        filter.requestTextStringWithoutSpam(MESSAGE_2);
        filter.requestTextStringWithoutSpam(MESSAGE_2);
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        verify(citizen, times(3)).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterTimeout()
    {
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT);
        filter.requestTextStringWithoutSpam(MESSAGE_1);

        verify(citizen, times(2)).sendLocalizedChat(any(), any());
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

        verify(citizen, times(4)).sendLocalizedChat(any(), any());
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

        verify(citizen, times(5)).sendLocalizedChat(any(), any());

        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT * 15 + MAX_TIMEOUT);
        filter.requestTextStringWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT * 15 + MAX_TIMEOUT * 2);
        filter.requestTextStringWithoutSpam(MESSAGE_1);

        verify(citizen, times(7)).sendLocalizedChat(any(), any());
    }
}
