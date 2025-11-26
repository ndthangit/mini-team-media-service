package com.example.mediaservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class JoinGroupRequest {
    private Long userId;
    private String groupCode;
}

