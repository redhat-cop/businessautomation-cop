package org.redhat.services.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

@Slf4j
@Component
@Order(1)
public class RequestResponseLoggingFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Log Basic Request info
        log.info("Logging Request method={}, uri={}",
                req.getMethod(),
                req.getRequestURI());

//        if ("POST".equalsIgnoreCase(req.getMethod())) {
//            log.info("Request Body: {}", IOUtils.toString(req.getReader()));
//        }

        // Log Headers
        if (req.getHeaderNames() != null) {
            Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String header = headerNames.nextElement();
                log.info("Header:  KEY={}, VALUE={} ", header, req.getHeader(header));
            }
        }
    }
}
