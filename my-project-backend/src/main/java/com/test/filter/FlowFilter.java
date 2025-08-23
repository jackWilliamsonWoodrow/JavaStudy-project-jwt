package com.test.filter;

import com.test.entity.RestBean;
import com.test.utils.Const;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 流量限制过滤器 - 基于IP的请求限流控制
 * 使用Redis实现分布式限流，防止恶意请求或过量访问
 */
@Component
@Order(Const.ORDER_LIMIT)  // 设置过滤器执行顺序
public class FlowFilter extends HttpFilter {

    @Resource
    private StringRedisTemplate stringRedisTemplate;  // Redis操作模板

    /**
     * 过滤器核心方法
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param chain 过滤器链
     */
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // 获取客户端IP地址
        String address = request.getRemoteAddr();

        // 检查IP是否允许通过限流
        if (this.tryCount(address)) {
            // 通过限流检查，继续执行后续过滤器
            chain.doFilter(request, response);
        } else {
            // 未通过限流检查，返回限制响应
            this.writeBlockMessage(response);
        }
    }

    /**
     * 返回限流提示信息
     * @param response HTTP响应对象
     */
    private void writeBlockMessage(HttpServletResponse response) throws IOException {
        // 设置HTTP状态码为403禁止访问
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        // 设置响应内容类型为JSON
        response.setContentType("application/json;charset=utf-8");
        // 写入限流提示信息
        response.getWriter().write(RestBean.forbidden("操作频繁，请稍后再试").asJsonString());
    }

    /**
     * 尝试计数并检查IP是否应该被限制
     * @param ip 客户端IP地址
     * @return true表示允许通过，false表示应该限制
     */
    private boolean tryCount(String ip) {
        // 使用IP字符串作为锁对象，保证对同一IP的检查是同步的
        synchronized (ip.intern()) {
            // 检查IP是否在黑名单中（已被限制）
            if (stringRedisTemplate.hasKey(Const.FLOW_LIMIT_BLOCK + ip)) {
                return false;
            }
            // 执行限流周期检查
            return this.limitPeriodCheck(ip);
        }
    }

    /**
     * 限流周期检查核心逻辑
     * @param ip 客户端IP地址
     * @return true表示允许通过，false表示应该限制
     */
    private boolean limitPeriodCheck(String ip) {
        // 构造Redis计数器key
        String counterKey = Const.FLOW_LIMIT_COUNTER + ip;

        // 检查是否存在计数器（是否在统计周期内）
        if (stringRedisTemplate.hasKey(counterKey)) {
            // 存在计数器，增加计数（原子操作）
            Long increment = Optional.ofNullable(stringRedisTemplate.opsForValue().increment(counterKey))
                    .orElse(0L);

            // 如果计数超过阈值（10次）
            if (increment > 30) {
                // 将IP加入黑名单，设置30秒过期时间
                stringRedisTemplate.opsForValue().set(
                        Const.FLOW_LIMIT_BLOCK + ip,
                        "",
                        30,
                        TimeUnit.SECONDS
                );
                return false;
            }
        } else {
            // 不存在计数器，初始化计数器（3秒窗口期）
            stringRedisTemplate.opsForValue().set(
                    counterKey,
                    "1",
                    3,
                    TimeUnit.SECONDS
            );
        }
        return true;
    }
}
