package user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import user_service.dto.user.UserRequestDto;
import user_service.dto.user.UserResponseDto;
import user_service.entity.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {CardMapper.class})
public interface UserMapper {
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "id", ignore = true)
    User toUser(UserRequestDto userRequestDto);
    @Mapping(target = "cards", source = "cards")
    UserResponseDto toResponseDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    void updateUserFromDto(UserRequestDto userRequestDto, @MappingTarget User target);
}