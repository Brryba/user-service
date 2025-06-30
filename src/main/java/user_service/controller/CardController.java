package user_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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
    public CardResponseDto create(@RequestBody @Valid CardRequestDto card) {
        return cardService.createCard(card);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CardResponseDto findById(@PathVariable long id) {
        return cardService.getCardById(id);
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<CardResponseDto> findCardsByIds(@RequestParam(required = false) List<Long> ids) {
        return cardService.getCardsByIds(ids);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CardResponseDto update(@PathVariable long id,
                                  @RequestBody @Valid CardRequestDto cardRequestDto) {
        return cardService.updateCard(cardRequestDto, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable long id) {
        cardService.deleteCard(id);
    }
}
