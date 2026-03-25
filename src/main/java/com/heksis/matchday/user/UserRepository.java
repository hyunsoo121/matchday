package com.heksis.matchday.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByOauthProviderAndOauthId(OAuthProvider provider, String oauthId);
}
