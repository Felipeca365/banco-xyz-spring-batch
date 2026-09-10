package cl.duoc.backendiii.bffcajero.config;

import cl.duoc.backendiii.bffcajero.security.CajeroAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private CajeroAuthInterceptor cajeroAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cajeroAuthInterceptor)
                .addPathPatterns("/api/cajero/**");
    }
}