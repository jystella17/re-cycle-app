package com.erecycler.server.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.erecycler.server.common.ErrorCase;
import com.erecycler.server.domain.RecycleGuide;
import com.erecycler.server.service.RecycleGuideService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecycleGuideController.class)
@AutoConfigureMockMvc
class RecycleGuideControllerTest {
	List<String> mockMaterials = new ArrayList<>();
	List<String> mockItemsOfMock1 = new ArrayList<>();
	RecycleGuide mockRecycleGuide1 = RecycleGuide.builder()
		.material("materialName1")
		.item("itemName1")
		.guideline("guidelineBody1")
		.build();
	RecycleGuide mockRecycleGuide2 = RecycleGuide.builder()
		.material("materialName1")
		.item("itemName2")
		.guideline("guidelineBody2")
		.build();
	RecycleGuide mockRecycleGuide3 = RecycleGuide.builder()
		.material("materialName2")
		.item("itemName3")
		.guideline("guidelineBody3")
		.build();
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockBean
	private RecycleGuideService recycleGuideService;

	private String toJSONArray(List<String> list) throws JSONException {
		return new JSONArray(list.toString()).toString();
	}

	@BeforeEach
	void setUp() {
		Set<String> materialSet = new HashSet<>();
		materialSet.add(mockRecycleGuide1.getMaterial());
		materialSet.add(mockRecycleGuide2.getMaterial());
		materialSet.add(mockRecycleGuide3.getMaterial());
		mockMaterials = new ArrayList<>(materialSet);

		List<String> mockItemsOfMock1 = Arrays
			.asList(mockRecycleGuide1.getItem(), mockRecycleGuide2.getItem());
	}

	@Test
	@DisplayName("POST /guide controller")
	void addGuide() throws Exception {
		String requestBody = objectMapper.writeValueAsString(mockRecycleGuide1);

		// given
		given(this.recycleGuideService.addGuide(any(RecycleGuide.class))).willReturn("OK");
		// when
		mockMvc.perform(post("/guide")
			.content(requestBody)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON))
			// then
			.andExpect(status().isCreated())
			.andExpect(content().string(""))
			.andDo(print());

		// given
		given(this.recycleGuideService.addGuide(any(RecycleGuide.class)))
			.willReturn(ErrorCase.INVALID_FIELD_ERROR);
		// when
		mockMvc.perform(post("/guide")
			.content(requestBody)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON))
			// then
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@Test
	@DisplayName("GET /materials controller")
	void getMaterials() throws Exception {
		// given
		given(recycleGuideService.getMaterials()).willReturn(mockMaterials);
		// when
		mockMvc.perform(get("/materials"))
			// then
			.andExpect(status().isOk())
			.andExpect(content().string(toJSONArray(mockMaterials)))
			.andDo(print());

		// given
		given(recycleGuideService.getMaterials()).willReturn(
			Collections.singletonList(ErrorCase.DATABASE_CONNECTION_ERROR));
		// when
		mockMvc.perform(get("/materials"))
			// then
			.andExpect(status().isInternalServerError())
			.andDo(print());
	}

	@Test
	@DisplayName("GET /:material/items controller")
	void getItems() throws Exception {
		// given
		given(this.recycleGuideService.getItems(mockRecycleGuide1.getMaterial()))
			.willReturn(mockItemsOfMock1);
		// when
		mockMvc.perform(get("/{material}/items", mockRecycleGuide1.getMaterial()))
			// then
			.andExpect(status().isOk())
			.andExpect(content().string(toJSONArray(mockItemsOfMock1)))
			.andDo(print());

		// given
		given(this.recycleGuideService.getItems(mockRecycleGuide1.getMaterial()))
			.willReturn(Collections.singletonList(ErrorCase.DATABASE_CONNECTION_ERROR));
		// when
		mockMvc.perform(get("/{material}/items", mockRecycleGuide1.getMaterial()))
			// then
			.andExpect(status().isInternalServerError())
			.andDo(print());

		// given
		given(this.recycleGuideService.getItems("invalidName"))
			.willReturn(Collections.singletonList(ErrorCase.NO_SUCH_MATERIAL_ERROR));
		// when
		mockMvc.perform(get("/{material}/items", "invalidName"))
			// then
			.andExpect(status().isNotFound())
			.andDo(print());
	}

	@Test
	@DisplayName("GET /:material/:item/guide controller")
	void getGuideline() throws Exception {
		// given
		given(this.recycleGuideService
			.getGuideline(mockRecycleGuide1.getMaterial(), mockRecycleGuide1.getItem()))
			.willReturn(mockRecycleGuide1.getGuideline());
		// when
		mockMvc.perform(get("/{material}/{item}/guide",
			mockRecycleGuide1.getMaterial(), mockRecycleGuide1.getItem()))
			// then
			.andExpect(status().isOk())
			.andExpect(content().string(mockRecycleGuide1.getGuideline()))
			.andDo(print());

		// given
		given(this.recycleGuideService
			.getGuideline(mockRecycleGuide1.getMaterial(), mockRecycleGuide1.getItem()))
			.willReturn(ErrorCase.DATABASE_CONNECTION_ERROR);
		// when
		mockMvc.perform(get("/{material}/{item}/guide",
			mockRecycleGuide1.getMaterial(), mockRecycleGuide1.getItem()))
			// then
			.andExpect(status().isInternalServerError())
			.andDo(print());

		// given
		given(this.recycleGuideService
			.getGuideline("invalidName", "invalidName"))
			.willReturn(ErrorCase.NO_SUCH_ITEM_ERROR);
		// when
		mockMvc.perform(get("/{material}/{item}/guide", "invalidName", "invalidName"))
			// then
			.andExpect(status().isNotFound())
			.andDo(print());
	}

	@Test
	@DisplayName("DELETE /:material/:item/guide controller")
	void deleteGuideline() throws Exception {
		// given
		given(this.recycleGuideService
			.deleteGuide(mockRecycleGuide1.getMaterial(), mockRecycleGuide1.getItem()))
			.willReturn("OK");
		// when
		mockMvc.perform(delete("/{material}/{item}/guide",
			mockRecycleGuide1.getMaterial(), mockRecycleGuide1.getItem()))
			// then
			.andExpect(status().isNoContent())
			.andExpect(content().string(""))
			.andDo(print());

		// given
		given(this.recycleGuideService
			.deleteGuide(mockRecycleGuide1.getMaterial(), mockRecycleGuide1.getItem()))
			.willReturn(ErrorCase.DATABASE_CONNECTION_ERROR);
		// when
		mockMvc.perform(delete("/{material}/{item}/guide",
			mockRecycleGuide1.getMaterial(), mockRecycleGuide1.getItem()))
			// then
			.andExpect(status().isInternalServerError())
			.andDo(print());

		// given
		given(this.recycleGuideService
			.deleteGuide("invalidName", "invalidName"))
			.willReturn(ErrorCase.NO_SUCH_ITEM_ERROR);
		// when
		mockMvc.perform(delete("/{material}/{item}/guide", "invalidName", "invalidName"))
			// then
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@Test
	@DisplayName("PATCH /:material/:item/guide controller")
	void updateGuideline() throws Exception {
		// given
		given(this.recycleGuideService
			.updateGuideline(mockRecycleGuide1.getMaterial(), mockRecycleGuide1.getItem(),
				"newGuidelineBody"))
			.willReturn("OK");
		// when
		mockMvc.perform(patch("/{material}/{item}/guide",
			mockRecycleGuide1.getMaterial(), mockRecycleGuide1.getItem())
			.content("newGuidelineBody"))
			// then
			.andExpect(status().isOk())
			.andExpect(content().string(""))
			.andDo(print());

		// given
		given(this.recycleGuideService
			.updateGuideline(mockRecycleGuide1.getMaterial(), mockRecycleGuide1.getItem(),
				"newGuidelineBody"))
			.willReturn(ErrorCase.DATABASE_CONNECTION_ERROR);
		// when
		mockMvc.perform(patch("/{material}/{item}/guide",
			mockRecycleGuide1.getMaterial(), mockRecycleGuide1.getItem())
			.content("newGuidelineBody"))
			// then
			.andExpect(status().isInternalServerError())
			.andDo(print());

		// given
		given(this.recycleGuideService
			.updateGuideline("invalidName", "invalidName", "newGuidelineBody"))
			.willReturn(ErrorCase.NO_SUCH_ITEM_ERROR);
		// when
		mockMvc.perform(patch("/{material}/{item}/guide", "invalidName", "invalidName")
			.content("newGuidelineBody"))
			// then
			.andExpect(status().isBadRequest())
			.andDo(print());
	}
}