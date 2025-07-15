package user_service.integration.mockmvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import user_service.controller.CardController;
import user_service.controller.ExceptionController;
import user_service.dto.card.CardRequestDto;
import user_service.dto.card.CardResponseDto;
import user_service.exception.CardNotFoundException;
import user_service.exception.CardNumberNotUniqueException;
import user_service.exception.UserNotFoundException;
import user_service.service.CardService;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@EnableWebMvc
@SpringBootTest(classes = {CardController.class,
        ExceptionController.class,
ObjectMapper.class})
public class CardMockmvcTests {
    @MockitoBean
    private CardService cardService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CardRequestDto cardRequestDto;

    private String cardRequestDtoJson;

    private static CardResponseDto cardResponseDto;

    @BeforeAll
    public static void createUserResponseDto() {
        cardResponseDto = CardResponseDto.builder()
                .id(1L)
                .userId(1L)
                .holder("CARD HOLDER")
                .expirationDate("03/26")
                .number("001122334455667")
                .build();
    }

    @BeforeEach
    public void createUserRequestDto() throws JsonProcessingException {
        cardRequestDto = CardRequestDto.builder()
                .userId(1L)
                .holder("CARD HOLDER")
                .expirationDate("03/26")
                .number("0011223344556677")
                .build();

        cardRequestDtoJson = objectMapper.writeValueAsString(cardRequestDto);
    }

    private ResultMatcher responseBodyEqualsDto(CardResponseDto card) {
        return result -> {
            jsonPath("$.id", equalTo(card.getId().intValue())).match(result);
            jsonPath("$.userId", equalTo(card.getUserId().intValue())).match(result);
            jsonPath("$.holder", equalTo(card.getHolder())).match(result);
            jsonPath("$.expirationDate", equalTo(card.getExpirationDate())).match(result);
            jsonPath("$.number", equalTo(card.getNumber())).match(result);
        };
    }

    @Test
    public void createCardTest_success_201() throws Exception {
        when(cardService.createCard(cardRequestDto)).thenReturn(cardResponseDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/card")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cardRequestDtoJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(responseBodyEqualsDto(cardResponseDto));
    }

    @Test
    public void createCardTest_failure_cardValidation_400() throws Exception {
        cardRequestDto.setNumber("1234567890");
        cardRequestDtoJson = objectMapper.writeValueAsString(cardRequestDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/card")
        .contentType(MediaType.APPLICATION_JSON)
                .content(cardRequestDtoJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]",
                        containsString("Card number should only contains 16 digits")));
    }


    @Test
    public void createCard_failure_userNotExists_404() throws Exception {
        when(cardService.createCard(cardRequestDto))
                .thenThrow(new UserNotFoundException(cardRequestDto.getUserId()));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/card")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cardRequestDtoJson))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void getCardByIdTest_success() throws Exception {
        when(cardService.getCardById(1L)).thenReturn(cardResponseDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/card/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(responseBodyEqualsDto(cardResponseDto));
    }


    @Test
    public void getCardByIdTest_failure_cardNotFound_404() throws Exception {
        when(cardService.getCardById(1L)).thenThrow(new CardNotFoundException(1L));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/card/1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Card with id 1 not found"));
    }

    @Test
    public void updateCardTest_success() throws Exception {
        when(cardService.updateCard(cardRequestDto, 1)).thenReturn(cardResponseDto);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/card/1")
                .contentType(MediaType.APPLICATION_JSON)
        .content(cardRequestDtoJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(responseBodyEqualsDto(cardResponseDto));
    }

    @Test
    public void updateCardTest_failure_duplicateNumber_409() throws Exception {
        when(cardService.updateCard(cardRequestDto, 1L))
                .thenThrow(new CardNumberNotUniqueException());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/card/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cardRequestDtoJson))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    public void deleteCardTest_success_204() throws Exception {
        doNothing().when(cardService).deleteCard(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/card/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteCardTest_failure_cardNotFound_404() throws Exception {
        doThrow(new CardNotFoundException(1L)).when(cardService).deleteCard(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/card/1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Card with id 1 not found"));
    }
}
