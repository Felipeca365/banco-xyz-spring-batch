package cl.duoc.backendiii.bffmovil.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return noAutorizado(response, "Token JWT ausente");
        }

        String token = authHeader.substring("Bearer ".length());

        try {
            jwtService.validarYObtenerUsuario(token);
            return true;
        } catch (Exception e) {
            return noAutorizado(response, "Token JWT invalido o expirado");
        }
    }

    private boolean noAutorizado(HttpServletResponse response, String mensaje) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + mensaje + "\"}");
        return false;
    }
}