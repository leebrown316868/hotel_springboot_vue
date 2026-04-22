package com.hotel.controller;

import com.alipay.api.internal.util.AlipaySignature;
import com.hotel.config.AlipayConfig;
import com.hotel.service.impl.BookingServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final BookingServiceImpl bookingService;
    private final AlipayConfig alipayConfig;

    /**
     * 支付宝异步回调通知
     */
    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            StringBuilder valueStr = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                valueStr.append(i == values.length - 1 ? values[i] : values[i] + ",");
            }
            params.put(name, valueStr.toString());
        }

        log.info("收到支付宝异步回调: {}", params);

        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    "UTF-8",
                    "RSA2"
            );

            if (!signVerified) {
                log.warn("支付宝回调签名验证失败");
                return "failure";
            }

            boolean success = bookingService.handleAlipayNotify(params);
            return success ? "success" : "failure";
        } catch (Exception e) {
            log.error("处理支付宝回调异常", e);
            return "failure";
        }
    }
}
