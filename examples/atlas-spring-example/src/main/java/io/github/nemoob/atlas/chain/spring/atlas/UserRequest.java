package io.github.nemoob.atlas.chain.spring.atlas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 示例请求类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    private Long userId;
    private String username;
}