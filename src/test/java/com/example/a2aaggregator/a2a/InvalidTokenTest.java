package com.example.a2aaggregator.a2a;

import com.example.a2aaggregator.registry.CapabilityRegistry;
import com.example.a2aaggregator.security.BearerTokenFilter;
import com.example.a2aaggregator.task.TaskRoutingService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InvalidTokenTest {
    @Test
    void rejectsInvalidBearerTokenWithA2a401Body() throws Exception {
        var objectMapper = new ObjectMapper();
        var controller = new A2aController(mock(TaskRoutingService.class), mock(CapabilityRegistry.class));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilters(new BearerTokenFilter(objectMapper)).build();

        mvc.perform(post("/message:send")
                        .header("Authorization", "Bearer wrong-token")
                        .header("A2A-Version", "1.0")
                        .contentType(A2aController.A2A_MEDIA_TYPE)
                        .content("{\"message\":{}}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer realm=\"a2a-aggregator\""))
                .andExpect(jsonPath("$.error.code").value(401))
                .andExpect(jsonPath("$.error.status").value("UNAUTHENTICATED"));
    }
}
