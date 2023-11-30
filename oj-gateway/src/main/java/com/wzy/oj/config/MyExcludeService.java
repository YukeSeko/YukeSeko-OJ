package com.wzy.oj.config;

import com.github.xiaoymin.knife4j.spring.gateway.Knife4jGatewayProperties;
import com.github.xiaoymin.knife4j.spring.gateway.discover.spi.GatewayServiceExcludeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * 排除判题服务的knife4j文档
 */
@Component
public class MyExcludeService implements GatewayServiceExcludeService {
    @Override
    public Set<String> exclude(Environment environment, Knife4jGatewayProperties properties, List<String> services) {
        if (!CollectionUtils.isEmpty(services)){
                        // 排除注册中心包含order字眼的服务
            return services.stream().filter(s -> s.contains("judge")).collect(Collectors.toSet());
        }
        return new TreeSet<>();
    }
}