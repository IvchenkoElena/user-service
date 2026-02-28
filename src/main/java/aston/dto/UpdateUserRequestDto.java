package aston.dto;

import lombok.Data;

@Data
public class UpdateUserRequestDto {
    private String name;
    private String email;
    private Integer age;
}
