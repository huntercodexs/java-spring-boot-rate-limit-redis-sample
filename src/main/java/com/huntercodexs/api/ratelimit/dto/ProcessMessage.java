package com.huntercodexs.api.ratelimit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessMessage {
    private String id;
    private String userId; // Identifier for rate limiting
    private String content;
}
