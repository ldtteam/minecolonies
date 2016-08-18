package com.minecolonies.entity.ai.util;

import com.minecolonies.entity.EntityCitizen;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.minecolonies.entity.ai.util.ChatSpamFilter.BASE_TIMEOUT;
import static com.minecolonies.entity.ai.util.ChatSpamFilter.MAX_TIMEOUT;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ChatSpamFilterTest
{
    @Mock
    private EntityCitizen citizen;

    private ChatSpamFilter filter;

    private static final String MESSAGE_1 = "Whatever";
    private static final String MESSAGE_2 = "Whatever2";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        filter = new ChatSpamFilter(citizen);
        when(citizen.getOffsetTicks()).thenReturn(0);
    }

    @Test
    public void testFilterFirst()
    {
        filter.requestWithoutSpam(MESSAGE_1);
        verify(citizen).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterFiltering()
    {
        filter.requestWithoutSpam(MESSAGE_1);
        filter.requestWithoutSpam(MESSAGE_1);
        verify(citizen).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterRelease()
    {
        filter.requestWithoutSpam(MESSAGE_1);
        filter.requestWithoutSpam(MESSAGE_2);
        verify(citizen, times(2)).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterReleaseAndFilter()
    {
        filter.requestWithoutSpam(MESSAGE_1);
        filter.requestWithoutSpam(MESSAGE_2);
        filter.requestWithoutSpam(MESSAGE_2);
        filter.requestWithoutSpam(MESSAGE_1);
        verify(citizen, times(3)).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterTimeout()
    {
        filter.requestWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT);
        filter.requestWithoutSpam(MESSAGE_1);

        verify(citizen, times(2)).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterTimeoutLonger()
    {
        filter.requestWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT);
        filter.requestWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT * 3);
        filter.requestWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT * 7);
        filter.requestWithoutSpam(MESSAGE_1);

        verify(citizen, times(4)).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterTimeoutMax()
    {
        filter.requestWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT);
        filter.requestWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT * 3);
        filter.requestWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT * 7);
        filter.requestWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT * 15);
        filter.requestWithoutSpam(MESSAGE_1);

        verify(citizen, times(5)).sendLocalizedChat(any(), any());

        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT * 15 + MAX_TIMEOUT);
        filter.requestWithoutSpam(MESSAGE_1);
        when(citizen.getOffsetTicks()).thenReturn(BASE_TIMEOUT * 15 + MAX_TIMEOUT * 2);
        filter.requestWithoutSpam(MESSAGE_1);

        verify(citizen, times(7)).sendLocalizedChat(any(), any());
    }

}
