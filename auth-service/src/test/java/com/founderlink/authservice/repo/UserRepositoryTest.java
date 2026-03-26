package com.founderlink.authservice.repo;

import static org.assertj.core.api.Assertions.assertThat;

import com.founderlink.authservice.entity.Role;
import com.founderlink.authservice.entity.RoleName;
import com.founderlink.authservice.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest(properties = {
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@ActiveProfiles("test")
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Test
	void findByEmailWithRoles_shouldReturnUserWithRoles_whenEmailExists() {
		// given
		Role founderRole = roleRepository.save(new Role(RoleName.ROLE_FOUNDER));
		User user = new User();
		user.setName("Alice");
		user.setEmail("alice@founderlink.com");
		user.setPassword("encoded");
		user.getRoles().add(founderRole);
		userRepository.save(user);

		// when
		Optional<User> result = userRepository.findByEmailWithRoles("alice@founderlink.com");

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getEmail()).isEqualTo("alice@founderlink.com");
		assertThat(result.get().getRoles()).extracting(Role::getName).containsExactly(RoleName.ROLE_FOUNDER);
	}

	@Test
	void existsByEmail_shouldReturnTrue_whenEmailExists() {
		// given
		User user = new User();
		user.setName("Bob");
		user.setEmail("bob@founderlink.com");
		user.setPassword("encoded");
		userRepository.save(user);

		// when
		boolean exists = userRepository.existsByEmail("bob@founderlink.com");

		// then
		assertThat(exists).isTrue();
	}
}
