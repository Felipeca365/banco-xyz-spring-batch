package cl.duoc.backendiii.bffweb.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Base64;

@Component
public class BasicAuthInterceptor implements HandlerInterceptor {

    @Value("${web.security.username}")
    private String usuarioEsperado;

    @Value("${web.security.password}")
    private String claveEsperada;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return noAutorizado(response);
        }

        String base64Credenciales = authHeader.substring("Basic ".length());
        String credenciales = new String(Base64.getDecoder().decode(base64Credenciales));
        String[] partes = credenciales.split(":", 2);

        if (partes.length != 2 || !partes[0].equals(usuarioEsperado) || !partes[1].equals(claveEsperada)) {
            return noAutorizado(response);
        }

        return true;
    }

    private boolean noAutorizado(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "Basic realm=\"BFF Web\"");
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Credenciales invalidas o ausentes\"}");
        return false;
    }
}