package com.example.quartz.model;

import java.time.LocalDateTime;
import java.time.ZoneId;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ScheduleEmailRequest {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String subject;
    @NotBlank
    private String body;
    @NotNull
    private LocalDateTime localDateTime;
    @NotNull
    private ZoneId zoneId;

}
