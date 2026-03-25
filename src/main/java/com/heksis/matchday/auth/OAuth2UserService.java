package com.heksis.matchday.auth;

import com.heksis.matchday.user.OAuthProvider;
import com.heksis.matchday.user.User;
import com.heksis.matchday.user.UserRepository;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuth2UserService
    implements org.springframework.security.oauth2.client.userinfo.OAuth2UserService<
        OAuth2UserRequest, OAuth2User> {

  private final UserRepository userRepository;
  private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = delegate.loadUser(request);

    String oauthId = oAuth2User.getAttribute("sub");
    String email = oAuth2User.getAttribute("email");
    String name = oAuth2User.getAttribute("name");

    User user =
        userRepository
            .findByOauthProviderAndOauthId(OAuthProvider.GOOGLE, oauthId)
            .orElseGet(
                () -> userRepository.save(User.create(OAuthProvider.GOOGLE, oauthId, name, email)));

    return new DefaultOAuth2User(
        Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
        Map.of("userId", user.getId(), "sub", oauthId, "email", email != null ? email : ""),
        "sub");
  }
}
