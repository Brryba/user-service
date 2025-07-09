package user_service.integration.mockmvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import user_service.controller.ExceptionController;
import user_service.controller.UserController;
import user_service.dto.card.CardResponseDto;
import user_service.dto.user.UserRequestDto;
import user_service.dto.user.UserResponseDto;
import user_service.exception.UserNotFoundException;
import user_service.exception.UsersNotFoundException;
import user_service.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@EnableWebMvc
@SpringBootTest(classes = {UserController.class,
        UserMockmvcTests.ObjectMapperConfig.class,
        ExceptionController.class})
public class UserMockmvcTests {
    @Configuration
    static class ObjectMapperConfig {
        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper;
        }
    }


    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRequestDto userRequestDto;

    private String userRequestDtoJson;

    private static UserResponseDto userResponseDto;

    @BeforeAll
    public static void createUserResponseDto() {
        userResponseDto = UserResponseDto.builder()
                .id(1L)
                .name("Name")
                .surname("Surname")
                .email("email@email.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .cards(List.of(CardResponseDto.builder().id(1L).userId(1L).build(),
                        CardResponseDto.builder().id(2L).userId(1L).build()))
                .build();
    }

    @BeforeEach
    public void createUserRequestDto() throws JsonProcessingException {
        userRequestDto = UserRequestDto.builder()
                .name("Name")
                .surname("Surname")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("email@email.com")
                .build();

        userRequestDtoJson = objectMapper.writeValueAsString(userRequestDto);
    }

    private ResultMatcher responseBodyEqualsDto(UserResponseDto user) {
        return result -> {
            jsonPath("$.id", is (1)).match(result);
            jsonPath("$.name", is(user.getName())).match(result);
            jsonPath("$.surname", is(user.getSurname())).match(result);
            jsonPath("$.email", is(user.getEmail())).match(result);
            jsonPath("$.birthDate", is(user.getBirthDate().toString())).match(result);
            jsonPath("$.cards", hasSize(user.getCards().size())).match(result);
            jsonPath("$.cards[0].id", is(1)).match(result);
        };
    }

    @Test
    public void getUserByIdTest_success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userResponseDto);

        mockMvc.perform(get("/api/user/1"))
                .andExpect(status().isOk())
                .andExpect(responseBodyEqualsDto(userResponseDto));
    }

    @Test
    public void getUserByIdTest_failure_noUserIdExists() throws Exception {
        when(userService.getUserById(100L)).thenThrow(new UserNotFoundException(100L));

        mockMvc.perform(get("/api/user/100"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(status().reason("User with id 100 not found"));
    }

    @Test
    public void createNewUserTest_success() throws Exception {
        when(userService.createUser(userRequestDto)).thenReturn(userResponseDto);

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestDtoJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(responseBodyEqualsDto(userResponseDto));
    }

    @Test
    public void createNewUserTest_failure_incorrectEmailFormat() throws Exception {
        userRequestDto.setEmail("@email@email.com");
        userRequestDtoJson = objectMapper.writeValueAsString(userRequestDto);

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestDtoJson))
                .andDo(print())
                .andExpect(jsonPath("$.error", is("Bad request")))
                .andExpect(jsonPath("$.errors[0]", containsString("email")));
    }

    @Test
    public void createNewUserTest_failure_birthDateInFuture() throws Exception {
        userRequestDto.setBirthDate(LocalDate.of(3000, 1, 1));
        userRequestDtoJson = objectMapper.writeValueAsString(userRequestDto);

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestDtoJson))
                .andDo(print())
                .andExpect(jsonPath("$.error", is("Bad request")))
                .andExpect(jsonPath("$.errors[0]", containsString("Birth date must be in the past")));
    }

    @Test
    public void getUserByEmailTest_success() throws Exception {
        when(userService.getUserByEmail(userRequestDto.getEmail())).thenReturn(userResponseDto);

        mockMvc.perform(get("/api/user?email=email@email.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(responseBodyEqualsDto(userResponseDto));
    }

    @Test
    public void getUserByEmailTest_failure_noUserEmailExists() throws Exception {
        when(userService.getUserByEmail(userRequestDto.getEmail()))
                .thenThrow(new UserNotFoundException(userRequestDto.getEmail()));

        mockMvc.perform(get("/api/user?email=email@email.com"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(status().reason("User with email " + userRequestDto.getEmail() + " not found"));
    }

    @Test
    public void getUsersByIds() throws Exception {
        when(userService.getUsersByIds(List.of(1L, 2L)))
                .thenReturn(List.of(userResponseDto));

        mockMvc.perform(get("/api/user?ids=1,2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    public void getUsersByIds_failure_noneUserIdExists() throws Exception {
        when(userService.getUsersByIds(List.of(1L, 2L))).thenThrow(new UsersNotFoundException());

        mockMvc.perform(get("/api/user?ids=1,2"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void getUsers_NoRequestParams() throws Exception {
        mockMvc.perform(get("/api/user?wrong=param"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateUser_success() throws Exception {
        when(userService.updateUser(userRequestDto, 1L)).thenReturn(userResponseDto);

        mockMvc.perform(put("/api/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestDtoJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(responseBodyEqualsDto(userResponseDto));
    }

    @Test
    public void testUpdateUser_failure_noUserIdExists() throws Exception {
        when(userService.updateUser(userRequestDto, 1L)).thenThrow(new UserNotFoundException(1L));

        mockMvc.perform(put("/api/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestDtoJson))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(status().reason("User with id " + 1L + " not found"));
    }

    @Test
    public void testDeleteUser_success() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/user/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}