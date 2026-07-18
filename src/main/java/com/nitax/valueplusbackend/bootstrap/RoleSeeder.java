package com.nitax.valueplusbackend.bootstrap;

import com.nitax.valueplusbackend.domain.AppRoles;
import com.nitax.valueplusbackend.repository.RoleRepository;
import com.nitax.valueplusbackend.utils.enums.Role;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RoleSeeder implements ApplicationListener<ContextRefreshedEvent> {
  private final RoleRepository roleRepository;

  @Value("${spring.datasource.url}")
  private String dbUrl;

  public RoleSeeder(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    log.info("Application started and connected to database at: " + dbUrl);
    this.loadRoles();
  }

  private void loadRoles() {
    Role[] roleNames = new Role[] {Role.ADMIN, Role.SUPER_ADMIN,Role.ADVERTISER, Role.PUBLISHER};
    Map<Role, String> roleDescriptionMap =
        Map.of(
                Role.ADMIN, "Administrator role",
                Role.SUPER_ADMIN, "Super Administrator role",
                Role.ADVERTISER, "Advertiser role",
                Role.PUBLISHER, "Publisher role");

    Arrays.stream(roleNames)
        .forEach(
            (roleName) -> {
              Optional<AppRoles> optionalRole = roleRepository.findByName(roleName);

              optionalRole.ifPresentOrElse(
                  System.out::println,
                  () -> {
                    AppRoles roleToCreate = new AppRoles();

                    roleToCreate.setName(roleName);
                    roleToCreate.setDescription(roleDescriptionMap.get(roleName));

                    roleRepository.save(roleToCreate);
                  });
            });
  }
}
