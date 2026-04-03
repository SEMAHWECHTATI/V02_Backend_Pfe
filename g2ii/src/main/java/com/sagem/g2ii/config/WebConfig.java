package com.sagem.g2ii.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig  implements WebMvcConfigurer {

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOriginPatterns("*") // On utilise bien Patterns
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // À la ligne, hors du commentaire !
//                .allowedHeaders("*")
//                .allowCredentials(true);
//    }
}
