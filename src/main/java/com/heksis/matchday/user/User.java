package com.heksis.matchday.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "\"user\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "oauth_provider", nullable = false, columnDefinition = "oauth_provider")
  private OAuthProvider oauthProvider;

  @Column(name = "oauth_id", nullable = false, length = 100)
  private String oauthId;

  @Column(nullable = false, length = 50)
  private String nickname;

  @Column(length = 100)
  private String email;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public static User create(OAuthProvider provider, String oauthId, String nickname, String email) {
    User user = new User();
    user.oauthProvider = provider;
    user.oauthId = oauthId;
    user.nickname = nickname;
    user.email = email;
    user.createdAt = LocalDateTime.now();
    return user;
  }
}
