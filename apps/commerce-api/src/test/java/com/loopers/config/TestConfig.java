package com.loopers.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> mockTemplate = Mockito.mock(RedisTemplate.class);
        ValueOperations<String, String> mockValueOps = Mockito.mock(ValueOperations.class);
        
        // Set up the mock to return the value operations
        Mockito.when(mockTemplate.opsForValue()).thenReturn(mockValueOps);
        
        // Set up basic behavior for common Redis operations used in the application
        Mockito.when(mockValueOps.get(Mockito.anyString())).thenReturn(null);
        Mockito.when(mockValueOps.increment(Mockito.anyString())).thenReturn(1L);
        Mockito.when(mockValueOps.decrement(Mockito.anyString())).thenReturn(1L);
        Mockito.doNothing().when(mockValueOps).set(Mockito.anyString(), Mockito.anyString(), Mockito.any());
        Mockito.when(mockTemplate.expire(Mockito.anyString(), Mockito.any())).thenReturn(true);
        
        return mockTemplate;
    }
}
