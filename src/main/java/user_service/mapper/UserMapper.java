package user_service.mapper;

import org.mapstruct.*;
import user_service.dto.user.BaseUserRequestDto;
import user_service.dto.user.IdUserRequestDto;
import user_service.dto.user.UserResponseDto;
import user_service.entity.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "id", ignore = true)
    User toUser(BaseUserRequestDto baseUserRequestDto);
    @Mapping(target = "cards", ignore = true)
    User toUser(IdUserRequestDto idUserRequestDto);
    UserResponseDto toResponseDto(User user);
}