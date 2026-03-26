package com.founderlink.authservice.repo;

import static org.assertj.core.api.Assertions.assertThat;

import com.founderlink.authservice.entity.Role;
import com.founderlink.authservice.entity.RoleName;
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
class RoleRepositoryTest {

	@Autowired
	private RoleRepository roleRepository;

	@Test
	void findByName_shouldReturnRole_whenRoleExists() {
		// given
		roleRepository.save(new Role(RoleName.ROLE_ADMIN));

		// when
		Optional<Role> result = roleRepository.findByName(RoleName.ROLE_ADMIN);

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getName()).isEqualTo(RoleName.ROLE_ADMIN);
	}
}
