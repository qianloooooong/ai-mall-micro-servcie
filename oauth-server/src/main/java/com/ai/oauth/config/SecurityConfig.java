package com.ai.oauth.config;

import com.ai.oauth.domain.CustomUserDetails;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({JwtProperties.class})
@RequiredArgsConstructor
public class SecurityConfig {

    private final FrontendConfig frontend;

    private static final String LOGIN_URL = "/login";
    private static final String CALLBACK_URL = "/callback";
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2B);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();

        http
                .securityMatcher(authorizationServer.getEndpointsMatcher())
                .with(authorizationServer, server -> server.oidc(Customizer.withDefaults()))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint(frontend.getUrl() + LOGIN_URL),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // 禁用CSRF保护
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers(LOGIN_URL).permitAll()
                        // /error 放行，否则 404/500 会经 ERROR 转发又被入口点 302 回登录页，把真实错误掩盖掉
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        // loginPage 是绝对 URL 时，loginProcessingUrl 会默认取 loginPage，
                        // 导致过滤器匹配不到 /login，必须显式指定处理地址
                        .loginPage(frontend.getUrl() + LOGIN_URL)
                        .loginProcessingUrl(LOGIN_URL)
                        .failureUrl(frontend.getUrl() + "/login?error"))
                .httpBasic(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(JwtProperties properties) {
        RegisteredClient spa = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("spa-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientSecret(passwordEncoder().encode("spa-secret"))
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(frontend.getUrl() + CALLBACK_URL)
                .scope("openid").scope("profile").scope("read")
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                        .reuseRefreshTokens(false)
                        .accessTokenTimeToLive(properties.getTokenTTL())
                        .refreshTokenTimeToLive(properties.getRefreshTokenTTL())
                        .build())
                .build();
        RegisteredClient service = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("service-client")
                .clientSecret(passwordEncoder().encode("service-secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("read")
                .build();

        return new InMemoryRegisteredClientRepository(spa, service);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(JwtProperties properties) {
        KeyPair keyPair = loadKeyPair(properties);
        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID(properties.getAlias())
                .keyUse(KeyUse.SIGNATURE)
                .build();

        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    private KeyPair loadKeyPair(JwtProperties properties) {
        char[] password = properties.getPassword().toCharArray();
        try (InputStream is = properties.getLocation().getInputStream()) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(is, password);
            KeyStore.PasswordProtection protection = new KeyStore.PasswordProtection(password);
            KeyStore.PrivateKeyEntry entry =
                    (KeyStore.PrivateKeyEntry) keyStore.getEntry(properties.getAlias(), protection);
            return new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());
        } catch (Exception e) {
            throw new IllegalStateException("加载 JKS 密钥库失败: " + properties.getLocation(), e);
        }
    }

/*    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // 1. 从数据库查询用户实体（伪代码）
            // UserEntity entity = userRepository.findByUsername(username);
            // if (entity == null) throw new UsernameNotFoundException(...);

            // 2. 组装 CustomUserDetails
            CustomUserDetails user = new CustomUserDetails();
            user.setUserId("1001");                          // entity.getId()
            user.setUsername("user");                        // entity.getUsername()
            user.setPassword(passwordEncoder().encode("123456"));             // entity.getPassword()
            user.setAuthorities(Set.of(
                    new SimpleGrantedAuthority("ROLE_USER")
            ));
            return user;
        };
    }*/

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

//    @Bean
//    public AuthorizationServerSettings authorizationServerSettings() {
//        return AuthorizationServerSettings.builder().build();
//    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return context -> {
            if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                return;
            }
            if (AuthorizationGrantType.CLIENT_CREDENTIALS.equals(context.getAuthorizationGrantType())) {
                return;
            }

            Authentication principal = context.getPrincipal();
            Object user = principal == null ? null : principal.getPrincipal();

            context.getClaims().claims(claims -> {
                if (user instanceof CustomUserDetails userDetails && userDetails.getUserId() != null) {
                    claims.put("sub", userDetails.getUserId());
                }

                Set<String> roles = principal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
//                        .filter(a -> a.startsWith("ROLE_"))
//                        .map(a -> a.substring("ROLE_".length()))
                        .collect(Collectors.toSet());

                if (!roles.isEmpty()) {
                    claims.put("roles", roles);
                }

//                log.info("JWT customizer: grant={}, principalType={}, claims userid={}, roles={}",
//                        context.getAuthorizationGrantType().getValue(),
//                        user == null ? "null" : user.getClass().getName(),
//                        claims.get("userid"), claims.get("roles"));
            });
        };
    }

    @Bean
    public OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator(JWKSource<SecurityContext> jwkSource) {
        JwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(jwtTokenCustomizer());

        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();

        return new DelegatingOAuth2TokenGenerator(
                jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(JwtProperties properties) {
        return AuthorizationServerSettings.builder().issuer(properties.getIssuer()).build();
    }

//    @Bean
//    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
//        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
//        // 默认已处理 scope -> SCOPE_xxx
//
//        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
//        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
//            var authorities = new java.util.ArrayList<>(scopeConverter.convert(jwt));
//            // 读取自定义 roles Claim，还原为 ROLE_ 前缀
//            java.util.List<String> roles = jwt.getClaimAsStringList("roles");
//            if (roles != null) {
//                roles.forEach(r -> authorities.add(
//                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + r)));
//            }
//            return authorities;
//        });
//        converter.setPrincipalClaimName("userid"); // 让 Authentication.getName() 返回 userid
//        return converter;
//    }
}
