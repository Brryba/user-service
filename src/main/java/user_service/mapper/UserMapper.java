package user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import user_service.dto.user.UserRequestDto;
import user_service.dto.user.UserResponseDto;
import user_service.entity.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "id", ignore = true)
    User toUser(UserRequestDto userRequestDto);
    UserResponseDto toResponseDto(User user);
}