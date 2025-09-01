package io.github.nemoob.atlas.chain.atlas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 示例响应类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String message;
    private boolean success = true;
}