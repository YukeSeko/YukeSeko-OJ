package com.wzy.oj.config;

import cn.hutool.core.text.AntPathMatcher;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.model.RestResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wzy.common.common.BaseResponse;
import com.wzy.common.feign.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局请求拦截过滤
 *
 * @author YukeSeko
 */
@Component
@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {


    @Resource
    private UserFeignClient userFeignClient;

    private static final List<String> PATH_WHITE_LIST = Arrays.asList("/api/user/**", "/api/doc/**", "/api/user/v2/**", "/api/question/v2/**","/api/question/list/page/vo");

    //需要登录才能进行访问
    private static final List<String> PATH_LOGIN_LIST = Arrays.asList("/api/question/**", "/api/judge/**");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getPath().toString();
        // 1. 打印请求日志
        //logPrint(request);
        //查询用户是否登录时、用户登录等请求，直接放行
        List<Boolean> collect = PATH_WHITE_LIST.stream().map(item -> {
            AntPathMatcher antPathMatcher = new AntPathMatcher();
            return antPathMatcher.match(item, path);
        }).collect(Collectors.toList());
        if (collect.contains(true)) {
            return chain.filter(exchange);
        }
        // 2、网关统一鉴权：其他接口需要判断用户是否登录
        List<Boolean> collectLogin = PATH_LOGIN_LIST.stream().map(item -> {
            AntPathMatcher antPathMatcher = new AntPathMatcher();
            return antPathMatcher.match(item, path);
        }).collect(Collectors.toList());
        HttpHeaders headers = request.getHeaders();
        String cookie = headers.getFirst("Cookie");
        if (collectLogin.contains(true)) {
            String loginUserVo = HttpRequest.get("http://localhost:88/api/user/get/login")
                    .header("Cookie", cookie)
                    .timeout(20000)
                    .execute().body();
            JSONObject entries = JSONUtil.parseObj(loginUserVo);
            BaseResponse baseResponse = JSONUtil.toBean(entries, BaseResponse.class);
            Object data = baseResponse.getData();
            if (null == data) {
                response.setStatusCode(HttpStatus.FORBIDDEN);
                DataBufferFactory bufferFactory = response.bufferFactory();
                ObjectMapper objectMapper = new ObjectMapper();
                DataBuffer wrap = null;
                try {
                    wrap = bufferFactory.wrap(objectMapper.writeValueAsBytes(new RestResult<>(403, "登录信息过期","Need Login")));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                DataBuffer finalWrap = wrap;
                return response.writeWith(Mono.fromSupplier(() -> finalWrap));
            } else {
                return chain.filter(exchange);
            }
        }
        return chain.filter(exchange);
    }


    @Override
    public int getOrder() {
        return -1;
    }

    /**
     * 打印基本日志信息
     *
     * @param request
     */
    private void logPrint(ServerHttpRequest request) {
        log.info("=====  {} 请求开始 =====", request.getId());
        String path = request.getPath().value();
        String method = request.getMethod().toString();
        log.info("请求路径：" + path);
        log.info("请求方法：" + method);
        log.info("请求参数：" + request.getQueryParams());
        String sourceAddress = request.getLocalAddress().getHostString();
        log.info("请求来源地址：" + sourceAddress);
        //log.info("请求来源地址：" + request.getRemoteAddress());
    }
}
