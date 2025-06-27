package user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import user_service.dto.card.*;

import user_service.entity.Card;
import user_service.entity.User;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CardMapper {
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    Card toCard(BaseCardRequestDto baseCardRequestDto);
    @Mapping(target = "user", ignore = true)
    Card toCard(IdCardRequestDto idCardRequestDto);
    @Mapping(target = "userId", source = "user", qualifiedByName = "setUserId")
    CardResponseDto toResponseDto(Card card);

    @Named("setUserId")
    default long setUserId(User user) {
        return user.getId();
    }
}
