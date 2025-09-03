package user_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import user_service.dto.card.CardRequestDto;
import user_service.dto.card.CardResponseDto;
import user_service.service.CardService;

import java.util.List;

@RestController
@RequestMapping("api/card")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    private final String USER_ID_HEADER = "X-User-Id";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponseDto create(@RequestBody @Valid CardRequestDto card,
                                  @RequestHeader(USER_ID_HEADER) Long userId) {
        return cardService.createCard(card, userId);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CardResponseDto findById(@PathVariable long id, @RequestHeader(USER_ID_HEADER) Long userId) {
        return cardService.getCardById(id, userId);
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<CardResponseDto> findCardsByIds(@RequestParam(required = false) List<Long> ids,
                                                @RequestHeader(USER_ID_HEADER) Long userId) {
        return cardService.getCardsByIds(ids, userId);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CardResponseDto update(@PathVariable long id,
                                  @RequestBody @Valid CardRequestDto cardRequestDto,
                                  @RequestHeader(USER_ID_HEADER) Long userId) {
        return cardService.updateCard(cardRequestDto, id, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable long id,
                           @RequestHeader(USER_ID_HEADER) Long userId) {
        cardService.deleteCard(id, userId);
    }
}
