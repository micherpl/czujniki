package com.czujniki;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.Charset;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CzujnikiApplicationTests {

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Before
    public void setup() throws Exception {

        CzujnikiApplication.main(new String[] {"https://github.com/relayr/pdm-test/blob/master/sensors.yml"});
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    public void tests() throws Exception {
        gerBadEngins();
        updateEngines();
    }

    public void gerBadEngins() throws Exception{
        this.mockMvc
                .perform(get("/engines").param("pressure_threshold", "72")
                        .param("temp_threshold", "40")  )
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.[:1]").value("123"));
        this.mockMvc
                .perform(get("/engines").param("pressure_threshold", "40")
                        .param("temp_threshold", "40")  )
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.[0]").doesNotExist());
        this.mockMvc
                .perform(get("/engines").param("pressure_threshold", "80")
                        .param("temp_threshold", "90")  )
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.[0]").value("123"))
                .andExpect(jsonPath("$.[1]").value("156"));
    }

    public void updateEngines() throws Exception {
        String json = "{\"operation\": \"decrement\", \"value\": \"5\"}";

        this.mockMvc.perform(post("/sensors/89145")
                .contentType(contentType)
                .content(json))
                    .andDo(print()).andExpect(status().isOk());
        this.mockMvc
                .perform(get("/engine/89145"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$").value("94"));

        json = "{\"operation\": \"set\", \"value\": \"5\"}";

        this.mockMvc.perform(post("/sensors/89145")
                .contentType(contentType)
                .content(json))
                .andDo(print()).andExpect(status().isOk());
        this.mockMvc
                .perform(get("/engine/89145"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$").value("5"));

    }

}
