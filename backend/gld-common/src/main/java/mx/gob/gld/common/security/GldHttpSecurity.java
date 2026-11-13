package mx.gob.gld.common.security;

import mx.gob.gld.common.error.ApiErrorWriter;
import mx.gob.gld.common.error.ApiException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

public final class GldHttpSecurity {

    private GldHttpSecurity() {
    }

    public static SecurityFilterChain build(
            HttpSecurity http,
            GldJwtService jwt,
            String internalToken,
            RequestMatcher publicos,
            RequestMatcher internos,
            RequestMatcher preToken
    ) throws Exception {
        GldTenantFilter tenantFilter = new GldTenantFilter();
        GldInternalTokenFilter internalFilter = new GldInternalTokenFilter(internalToken, internos);
        GldJwtFilter jwtFilter = new GldJwtFilter(jwt, publicos, internos, preToken);

        http.csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/v3/api-docs", "/v3/api-docs/**",
                                "/swagger-ui.html", "/swagger-ui/**", "/error").permitAll()
                        .requestMatchers(publicos).permitAll()
                        .requestMatchers(internos).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> ApiErrorWriter.write(res, ApiException.of(
                                401, "No autenticado", "Token ausente o vencido.", null, null, "TOKEN_INVALIDO")))
                        .accessDeniedHandler((req, res, e) -> ApiErrorWriter.write(res, ApiException.of(
                                403, "No autorizado", "El rol no puede realizar esta operación.", null, null, "NO_AUTORIZADO"))))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(internalFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tenantFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
