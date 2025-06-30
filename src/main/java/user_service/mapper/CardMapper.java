package user_service.mapper;

import org.mapstruct.*;
import user_service.dto.card.*;

import user_service.entity.Card;
import user_service.entity.User;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CardMapper {
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "holder", source = "holder", qualifiedByName = "capitalizeHolderName")
    Card toCard(CardRequestDto cardRequestDto);

    @Mapping(target = "userId", source = "user", qualifiedByName = "setUserId")
    CardResponseDto toResponseDto(Card card);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateCardFromDto(CardRequestDto dto, @MappingTarget Card card);

    @Named("setUserId")
    default long setUserId(User user) {
        return user.getId();
    }

    @Named("capitalizeHolderName")
    default String capitalizeHolderName(String holder) {
        return holder.toUpperCase();
    }
}
