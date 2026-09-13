package cl.duoc.backendiii.bffmovil.controller;

import cl.duoc.backendiii.bffmovil.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/movil/auth")
public class AuthController {

    @Value("${movil.security.username}")
    private String usuarioEsperado;

    @Value("${movil.security.password}")
    private String claveEsperada;

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> credenciales) {
        String usuario = credenciales.get("usuario");
        String clave = credenciales.get("clave");

        if (usuario == null || clave == null || !usuario.equals(usuarioEsperado) || !clave.equals(claveEsperada)) {
            throw new RuntimeException("Usuario o clave incorrectos");
        }

        String token = jwtService.generarToken(usuario);
        return Map.of("token", token);
    }
}