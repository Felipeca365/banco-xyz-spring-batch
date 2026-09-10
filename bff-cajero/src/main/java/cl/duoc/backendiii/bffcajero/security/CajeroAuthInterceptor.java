package cl.duoc.backendiii.bffcajero.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CajeroAuthInterceptor implements HandlerInterceptor {

    @Value("${cajero.security.token}")
    private String tokenEsperado;

    private static final String HEADER_NAME = "X-Cajero-Token";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String tokenRecibido = request.getHeader(HEADER_NAME);

        if (tokenRecibido == null || !tokenRecibido.equals(tokenEsperado)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token de cajero invalido o ausente\"}");
            return false;
        }

        return true;
    }
}