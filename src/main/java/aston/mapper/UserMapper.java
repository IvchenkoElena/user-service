package aston.mapper;

import aston.dto.CreateUserRequestDto;
import aston.dto.UpdateUserRequestDto;
import aston.dto.UserResponseDto;
import aston.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toDTO(User user);
    User toEntity(CreateUserRequestDto request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromRequest(UpdateUserRequestDto request, @MappingTarget User user);
}