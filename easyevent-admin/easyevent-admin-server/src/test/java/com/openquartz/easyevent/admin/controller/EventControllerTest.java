package com.openquartz.easyevent.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openquartz.easyevent.admin.model.BusEventDetail;
import com.openquartz.easyevent.admin.model.BusEventHistoryEntity;
import com.openquartz.easyevent.admin.service.EventAdminService;
import java.util.Collections;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventAdminService eventAdminService;

    @Test
    void getEventDetails_WhenEventExists_ShouldReturnEvent() throws Exception {
        // Arrange
        BusEventDetail event = new BusEventDetail();
        event.setId(1L);
        event.setTitle("Test Event");
        event.setAppId("test-app");
        event.setMaxRetries(3);
        event.setCreatedTime(new Date());
        
        BusEventHistoryEntity history = new BusEventHistoryEntity();
        history.setId(100L);
        history.setEntityId(1L);
        history.setStatus("AVAILABLE");
        history.setContext("Initial creation");
        history.setCreateTime(new Date());
        event.setStatusHistory(Collections.singletonList(history));

        when(eventAdminService.getEventDetail(1L)).thenReturn(event);

        // Act & Assert
        mockMvc.perform(get("/api/events/1/details").header("Authorization", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Event"))
                .andExpect(jsonPath("$.appId").value("test-app"))
                .andExpect(jsonPath("$.maxRetries").value(3))
                .andExpect(jsonPath("$.statusHistory[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.statusHistory[0].context").value("Initial creation"));
    }

    @Test
    void getEventDetails_WhenEventDoesNotExist_ShouldReturn404() throws Exception {
        // Arrange
        when(eventAdminService.getEventDetail(anyLong())).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/events/999/details").header("Authorization", "admin"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEventDetails_WhenNoAuth_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/events/1/details"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getEventDetails_WhenInvalidAuth_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/events/1/details").header("Authorization", "wrong_token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateEvent_ShouldCallService() throws Exception {
        BusEventDetail detail = new BusEventDetail();
        detail.setProcessingState("PROCESS_COMPLETE");

        mockMvc.perform(put("/api/events/1")
                .header("Authorization", "admin")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(detail)))
                .andExpect(status().isOk());

        verify(eventAdminService).updateEvent(any(BusEventDetail.class));
    }

    @Test
    void deleteEvent_ShouldCallService() throws Exception {
        mockMvc.perform(delete("/api/events/1")
                .header("Authorization", "admin"))
                .andExpect(status().isOk());

        verify(eventAdminService).deleteEvent(1L);
    }
}
