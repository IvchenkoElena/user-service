package aston.service;

import aston.dto.CreateUserRequestDto;
import aston.dto.UpdateUserRequestDto;
import aston.dto.UserResponseDto;

import java.util.List;

public interface UserService extends AutoCloseable {
    UserResponseDto createUser(CreateUserRequestDto requestDto);
    UserResponseDto findUserById(Long id);
    UserResponseDto findUserByEmail(String email);
    List<UserResponseDto> findAllUsers();
    UserResponseDto updateUser(Long id, UpdateUserRequestDto requestDto);
    void deleteUser(Long id);

}
