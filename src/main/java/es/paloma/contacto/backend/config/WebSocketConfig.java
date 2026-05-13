package es.paloma.contacto.backend.config;

import es.paloma.contacto.backend.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import java.security.Principal;
import java.util.Collections;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) {
                    return message;
                }
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    autenticarConexion(accessor);
                }
                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    validarSuscripcion(accessor);
                }
                return message;
            }
        });
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:3000", "http://localhost:8080")
                .withSockJS();
    }

    private void autenticarConexion(StompHeaderAccessor accessor) {
        String token = extraerToken(accessor);
        if (token == null || !jwtUtil.validateToken(token)) {
            log.warn("Conexion WebSocket rechazada: token invalido");
            throw new AccessDeniedException("Token WebSocket invalido");
        }
        String email = jwtUtil.extractEmail(token);
        String rol = jwtUtil.extractRol(token);
        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            log.warn("Conexion WebSocket rechazada: token sin userId para {}", email);
            throw new AccessDeniedException("Token WebSocket sin userId");
        }
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                email,
                userId,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rol))
        );
        accessor.setUser(authentication);
    }

    private String extraerToken(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return accessor.getFirstNativeHeader("token");
    }

    private void validarSuscripcion(StompHeaderAccessor accessor) {
        String destino = accessor.getDestination();
        Principal principal = accessor.getUser();
        if (destino == null || principal == null) {
            throw new AccessDeniedException("Suscripcion WebSocket no autorizada");
        }
        if (!destino.startsWith("/topic/messages/") && !destino.startsWith("/topic/errors/")) {
            return;
        }
        Long usuarioIdDestino = extraerIdDestino(destino);
        Long usuarioIdToken = obtenerUserId(principal);
        if (!usuarioIdToken.equals(usuarioIdDestino)) {
            log.warn("Suscripcion WebSocket rechazada: usuario {} intento acceder a {}", usuarioIdToken, destino);
            throw new AccessDeniedException("No puedes suscribirte a canales de otro usuario");
        }
    }

    private Long obtenerUserId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken authentication &&
                authentication.getCredentials() instanceof Long userId) {
            return userId;
        }
        log.warn("Suscripcion WebSocket rechazada: principal sin userId");
        throw new AccessDeniedException("Principal WebSocket sin userId");
    }

    private Long extraerIdDestino(String destino) {
        String id = destino.substring(destino.lastIndexOf('/') + 1);
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("Destino WebSocket invalido");
        }
    }
}