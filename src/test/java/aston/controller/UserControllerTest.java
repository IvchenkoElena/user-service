package aston.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import aston.dto.CreateUserRequestDto;
import aston.dto.UpdateUserRequestDto;
import aston.dto.UserResponseDto;
import aston.exception.UserNotFoundException;
import aston.mapper.UserMapper;
import aston.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

@WebMvcTest(UserController.class)
@ExtendWith(SpringExtension.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final Long USER_ID = 1L;
    private static final String USER_NAME = "John Doe";
    private static final String USER_EMAIL = "john@example.com";
    private static final Integer USER_AGE = 30;

    @Test
    void createUser_SuccessfullyCreatesUser() throws Exception {
        CreateUserRequestDto requestDto = new CreateUserRequestDto();
        requestDto.setName(USER_NAME);
        requestDto.setEmail(USER_EMAIL);
        requestDto.setAge(USER_AGE);

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(USER_ID);
        responseDto.setName(USER_NAME);
        responseDto.setEmail(USER_EMAIL);
        responseDto.setAge(USER_AGE);
        when(userService.createUser(requestDto)).thenReturn(responseDto);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value(USER_NAME))
                .andExpect(jsonPath("$.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.age").value(USER_AGE));
    }

    @Test
    void createUser_ValidatesInput() throws Exception {
        CreateUserRequestDto invalidDto = new CreateUserRequestDto();
        invalidDto.setName("");
        invalidDto.setEmail("invalid-email");
        invalidDto.setAge(-5);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_ReturnsUser_WhenFound() throws Exception {
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(USER_ID);
        responseDto.setName(USER_NAME);
        responseDto.setEmail(USER_EMAIL);
        responseDto.setAge(USER_AGE);
        when(userService.findUserById(USER_ID)).thenReturn(responseDto);

        mockMvc.perform(get("/api/users/{id}", USER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value(USER_NAME))
                .andExpect(jsonPath("$.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.age").value(USER_AGE));
    }

    @Test
    void getUserById_ReturnsNotFound_WhenUserDoesNotExist() throws Exception {
        when(userService.findUserById(USER_ID))
                .thenThrow(new UserNotFoundException("Пользователь с ID " + USER_ID + " не найден"));

        mockMvc.perform(get("/api/users/{id}", USER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Пользователь с ID " + USER_ID + " не найден"));

    }

    @Test
    void getUserByEmail_ReturnsUser_WhenFound() throws Exception {
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(USER_ID);
        responseDto.setName(USER_NAME);
        responseDto.setEmail(USER_EMAIL);
        responseDto.setAge(USER_AGE);
        when(userService.findUserByEmail(USER_EMAIL)).thenReturn(responseDto);

        mockMvc.perform(get("/api/users/email/{email}", USER_EMAIL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.email").value(USER_EMAIL));
    }

    @Test
    void getUserByEmail_ReturnsNotFound_WhenUserDoesNotExist() throws Exception {
        when(userService.findUserByEmail(USER_EMAIL))
                .thenThrow(new UserNotFoundException("Пользователь с email " + USER_EMAIL + " не найден"));

        mockMvc.perform(get("/api/users/email/{email}", USER_EMAIL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    @Test
    void getAllUsers_ReturnsAllUsers() throws Exception {
        UserResponseDto responseDto1 = new UserResponseDto();
        responseDto1.setId(1L);
        responseDto1.setName("Alice");
        responseDto1.setEmail("alice@example.com");
        responseDto1.setAge(25);
        UserResponseDto responseDto2 = new UserResponseDto();
        responseDto2.setId(2L);
        responseDto2.setName("Bob");
        responseDto2.setEmail("bob@example.com");
        responseDto2.setAge(35);
        List<UserResponseDto> usersList = Arrays.asList(responseDto1, responseDto2);
        when(userService.findAllUsers()).thenReturn(usersList);

        mockMvc.perform(get("/api/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));
    }

    @Test
    void updateUser_SuccessfullyUpdatesUser() throws Exception {
        UpdateUserRequestDto updateDto = new UpdateUserRequestDto();
        updateDto.setName("Updated Name");
        updateDto.setEmail("updated@example.com");
        updateDto.setAge(35);

        UserResponseDto updatedUserDto = new UserResponseDto();
        updatedUserDto.setId(USER_ID);
        updatedUserDto.setName("Updated Name");
        updatedUserDto.setEmail("updated@example.com");
        updatedUserDto.setAge(35);
        when(userService.updateUser(USER_ID, updateDto)).thenReturn(updatedUserDto);

        mockMvc.perform(put("/api/users/{id}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.age").value(35));
    }

    @Test
    void updateUserPartially_UpdatesOnlyProvidedFields() throws Exception {
        UpdateUserRequestDto updateDto = new UpdateUserRequestDto();
        updateDto.setName("Updated Name");
        updateDto.setAge(25);

        UserResponseDto updatedUserDto = new UserResponseDto();
        updatedUserDto.setId(USER_ID);
        updatedUserDto.setName("Updated Name");
        updatedUserDto.setEmail(USER_EMAIL);
        updatedUserDto.setAge(25);

        when(userService.updateUser(USER_ID, updateDto)).thenReturn(updatedUserDto);

        mockMvc.perform(put("/api/users/{id}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.age").value(25))
                .andExpect(jsonPath("$.email").value(USER_EMAIL));
    }

    @Test
    void updateUserPartially_ReturnsNotFound_WhenUserDoesNotExist() throws Exception {
        UpdateUserRequestDto updateDto = new UpdateUserRequestDto();
        updateDto.setName("New Name");

        when(userService.updateUser(USER_ID, updateDto))
                .thenThrow(new UserNotFoundException("Пользователь с ID " + USER_ID + " не найден"));

        mockMvc.perform(put("/api/users/{id}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserPartially_ValidatesInput() throws Exception {
        UpdateUserRequestDto invalidUpdateDto = new UpdateUserRequestDto();
        invalidUpdateDto.setEmail("invalid-email");

        mockMvc.perform(put("/api/users/{id}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_SuccessfullyDeletesUser() throws Exception {
        doNothing().when(userService).deleteUser(USER_ID);

        mockMvc.perform(delete("/api/users/{id}", USER_ID))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(USER_ID);
    }

    @Test
    void deleteUser_ReturnsNotFound_WhenUserDoesNotExist() throws Exception {
        doThrow(new UserNotFoundException("Пользователь с ID " + USER_ID + " не найден"))
                .when(userService).deleteUser(USER_ID);

        mockMvc.perform(delete("/api/users/{id}", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Пользователь с ID " + USER_ID + " не найден"));
    }
}
