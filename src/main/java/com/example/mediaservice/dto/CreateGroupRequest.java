package com.example.mediaservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateGroupRequest {
    private String name;
    private boolean hidden;
    private Long userId;
}


