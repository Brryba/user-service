package user_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
public class CardController {
    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    private final CardService cardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponseDto create(@RequestBody @Valid CardRequestDto card,
                                  @AuthenticationPrincipal Long userId) {
        return cardService.createCard(card, userId);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CardResponseDto findById(@PathVariable long id, @AuthenticationPrincipal Long userId) {
        return cardService.getCardById(id, userId);
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<CardResponseDto> findCardsByIds(@RequestParam(required = false) List<Long> ids,
                                                @AuthenticationPrincipal Long userId) {
        return cardService.getCardsByIds(ids, userId);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CardResponseDto update(@PathVariable long id,
                                  @RequestBody @Valid CardRequestDto cardRequestDto,
                                  @AuthenticationPrincipal Long userId) {
        return cardService.updateCard(cardRequestDto, id, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable long id,
                           @AuthenticationPrincipal Long userId) {
        cardService.deleteCard(id, userId);
    }
}
