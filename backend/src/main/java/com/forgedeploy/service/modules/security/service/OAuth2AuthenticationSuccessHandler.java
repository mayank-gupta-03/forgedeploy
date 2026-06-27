package com.forgedeploy.service.modules.security.service;

import com.forgedeploy.service.entities.UserInfo;
import com.forgedeploy.service.modules.security.jwt.JwtService;
import com.forgedeploy.service.modules.users.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            String login = oAuth2User.getAttribute("login");
            email = login + "@github.placeholder.com";
        }

        Object githubIdObj = oAuth2User.getAttribute("id");
        String githubId = githubIdObj != null ? githubIdObj.toString() : null;
        String githubUsername = oAuth2User.getAttribute("login");

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName());
        String accessToken = client.getAccessToken().getTokenValue();

        String finalEmail = email;
        UserInfo user = userRepository.findByEmail(email)
                .map(existingUser -> {
                    existingUser.setGithubId(githubId);
                    existingUser.setGithubUsername(githubUsername);
                    existingUser.setGithubAccessToken(accessToken);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    UserInfo newUser = UserInfo.builder()
                            .email(finalEmail)
                            .githubId(githubId)
                            .githubUsername(githubUsername)
                            .githubAccessToken(accessToken)
                            .build();
                    return userRepository.save(newUser);
                });

        String token = jwtService.generateToken(user.getEmail(), user.getId());
        String targetUrl = UriComponentsBuilder
                .fromUriString("http://localhost:3000/oauth2/redirect")
                .queryParam("token", token)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
