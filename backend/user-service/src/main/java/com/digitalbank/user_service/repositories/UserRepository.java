    package com.digitalbank.user_service.repositories;

    import aj.org.objectweb.asm.commons.Remapper;
    import com.digitalbank.user_service.models.User;
    import org.springframework.data.jpa.repository.JpaRepository;

    import java.util.Optional;

    public interface UserRepository extends JpaRepository<User, Long> {
        Optional<User> findByEmail(String email);
        Optional<User> findByPhone(String phone);

        Optional<User> findByUsername(String username);
    }
