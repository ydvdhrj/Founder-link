package com.founderlink.investmentservice.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.founderlink.investmentservice.entity.Investment;
import com.founderlink.investmentservice.entity.InvestmentStatus;
import com.founderlink.investmentservice.security.JwtAuthenticationFilter;
import com.founderlink.investmentservice.service.InvestmentService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InvestmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class InvestmentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private InvestmentService investmentService;

	@MockitoBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Test
	void byStartup_shouldReturn200_whenInvestmentsExist() throws Exception {
		Investment investment = Investment.builder()
				.id(UUID.randomUUID())
				.startupId("startup-1")
				.investorId("investor-1")
				.amount(12000.0)
				.status(InvestmentStatus.PENDING)
				.build();
		given(investmentService.getInvestmentsByStartup("startup-1")).willReturn(List.of(investment));

		mockMvc.perform(get("/investments/startup/{startupId}", "startup-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].startupId").value("startup-1"))
				.andExpect(jsonPath("$[0].investorId").value("investor-1"));

		then(investmentService).should().getInvestmentsByStartup("startup-1");
	}

	@Test
	void create_shouldReturn400_whenValidationFails() throws Exception {
		String invalidBody = """
				{
				  "startupId": "",
				  "amount": 0
				}
				""";

		mockMvc.perform(post("/investments")
						.header("X-User-Id", "investor-1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidBody))
				.andExpect(status().isBadRequest());

		then(investmentService).shouldHaveNoInteractions();
	}

	@Test
	void create_shouldReturn201_whenRequestIsValid() throws Exception {
		UUID id = UUID.randomUUID();
		Investment saved = Investment.builder()
				.id(id)
				.startupId("startup-9")
				.investorId("investor-42")
				.amount(50000.0)
				.status(InvestmentStatus.PENDING)
				.build();
		given(investmentService.createInvestment("startup-9", "investor-42", 50000.0)).willReturn(saved);

		String body = """
				{
				  "startupId": "startup-9",
				  "amount": 50000
				}
				""";

		mockMvc.perform(post("/investments")
						.header("X-User-Id", "investor-42")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(id.toString()))
				.andExpect(jsonPath("$.startupId").value("startup-9"))
				.andExpect(jsonPath("$.investorId").value("investor-42"))
				.andExpect(jsonPath("$.status").value("PENDING"));

		then(investmentService).should().createInvestment(eq("startup-9"), eq("investor-42"), eq(50000.0));
	}
}
