package fi.jyu.issuetracker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import fi.jyu.issuetracker.dao.models.Importance;
import fi.jyu.issuetracker.dao.models.Issue;
import fi.jyu.issuetracker.dao.models.Status;
import fi.jyu.issuetracker.dao.repositories.IssueRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test class for issue processing functions.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(JacksonConfiguration.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestInstance(Lifecycle.PER_CLASS)
class IssuetrackerApplicationTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private IssueRepository issueRepository;

	// Initialize DB with two issues

	@BeforeAll
	public void initialize(){	
		Issue issue = new Issue();
		issue.setSummary("Test issue");
		issue.setDescription("Issue about testing issues");
		issue.setImportance(Importance.MEDIUM);
		issue.setStatus(Status.OPEN);
		issueRepository.save(issue);

		Issue issue2 = new Issue();
		issue2.setSummary("Deleted issue");
		issue2.setDescription("This is deleted");
		issue2.setImportance(Importance.MEDIUM);
		issue2.setStatus(Status.CLOSED);
		issueRepository.save(issue2);
	}


	// Make sure that DB is initialized correctly

	@Test 
	public void correctEntitiesInDb(){
		Long count = issueRepository.count();
		assertEquals(count, 2L, "Count is 2 after initialization");
	}

	// Tests below are executed by mocking a user which is by default authenticated

	@Test
	@WithMockUser
	public void correctIssueIsReturned() throws Exception {
		this.mvc
			.perform(get("/api/issues/1"))
				.andExpect(status().isOk());
		MvcResult result = this.mvc.perform(get("/api/issues/1"))
								.andDo(print())
								.andReturn();
		String content = result.getResponse().getContentAsString();
		Issue issue = objectMapper.readValue(content, Issue.class);
		assertEquals(1L, issue.getId(), "ID is 1");
		assertEquals("Test issue", issue.getSummary());
	}

	@Test
	@WithMockUser
	public void issueIsUpdatedCorrectly() throws JsonProcessingException, Exception {
		Issue issue = new Issue();
		issue.setId(1L);
		issue.setSummary("Updated issue");
		issue.setDescription("Updated details");
		issue.setImportance(Importance.MEDIUM);
		issue.setStatus(Status.CLOSED);

		this.mvc.perform(post("/api/issues/1")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(issue)))
			.andExpect(status().isOk());

		MvcResult result = this.mvc.perform(get("/api/issues/1"))
			.andDo(print())
			.andReturn();
		
		String content = result.getResponse().getContentAsString();
		Issue updatedIssue = objectMapper.readValue(content, Issue.class);

		assertEquals(1L, updatedIssue.getId(), "ID is 1");
		assertEquals("Updated issue", updatedIssue.getSummary());
	}

 	@Test
	@WithMockUser
	public void issueIsDeletedCorrectly() throws Exception {
		this.mvc.perform(delete("/api/issues/2"))
			.andExpect(status().isOk());
	
		Long count = issueRepository.count();
		assertEquals(1L, count, "Count is 1");
		assertEquals(false, issueRepository.existsById(2L));
	} 

}
