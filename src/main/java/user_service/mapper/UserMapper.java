package user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import user_service.dto.user.BaseUserRequestDto;
import user_service.dto.user.IdUserRequestDto;
import user_service.dto.user.UserResponseDto;
import user_service.entity.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toUserFromBaseDto(BaseUserRequestDto baseUserRequestDto);
    User toUserFromIdDto(IdUserRequestDto idUserRequestDto);
    UserResponseDto toResponseDto(User user);
}