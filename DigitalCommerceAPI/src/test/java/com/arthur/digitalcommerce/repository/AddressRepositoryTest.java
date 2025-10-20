package com.arthur.digitalcommerce.repository;

import com.arthur.digitalcommerce.model.Address;
import com.arthur.digitalcommerce.model.User;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AddressRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AddressRepository addressRepository;

    private Address createAndPersistValidAddress(String email) {
        User user = new User(
                "testuser",
                email,
                "Str0ngP@ss123",
                "12345678901"
        );
        User savedUser = entityManager.persistAndFlush(user);

        Address address = new Address();
        address.setStreet("Valid Test Street");
        address.setBuildingName("Junit Building");
        address.setCity("Test City");
        address.setState("TS");
        address.setCountry("Testland");
        address.setCep("12345-678");

        address.setUser(savedUser);
        savedUser.getAddresses().add(address);

        return entityManager.persistAndFlush(address);
    }

    @Test
    void testFindByUserEmail_WhenUserHasAddress_ShouldReturnList() {
        String email = "valid.test@email.com";
        createAndPersistValidAddress(email);

        List<Address> foundAddresses = addressRepository.findByUserEmail(email);

        assertThat(foundAddresses)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);

        assertThat(foundAddresses.get(0).getStreet()).isEqualTo("Valid Test Street");
        assertThat(foundAddresses.get(0).getUser().getEmail()).isEqualTo(email);
    }

    @Test
    void testFindByUserEmail_WhenUserDoesNotExist_ShouldReturnEmptyList() {
        createAndPersistValidAddress("other.user@email.com");

        List<Address> foundAddresses = addressRepository.findByUserEmail("nonexistent.email@email.com");

        assertThat(foundAddresses).isNotNull().isEmpty();
    }

    @Test
    void testFindByIdAndUserEmail_WhenFound_ShouldReturnOptionalPresent() {
        String email = "address.owner@email.com";
        Address savedAddress = createAndPersistValidAddress(email);

        Optional<Address> found = addressRepository.findByIdAndUserEmail(
                savedAddress.getAddressId(),
                email
        );

        assertThat(found).isPresent();
        assertThat(found.get().getAddressId()).isEqualTo(savedAddress.getAddressId());
    }

    @Test
    void testFindByIdAndUserEmail_WhenEmailIsDifferent_ShouldReturnEmptyOptional() {
        String correctEmail = "correct.owner@email.com";
        Address savedAddress = createAndPersistValidAddress(correctEmail);

        String wrongEmail = "wrong.email@email.com";

        Optional<Address> found = addressRepository.findByIdAndUserEmail(
                savedAddress.getAddressId(),
                wrongEmail
        );

        assertThat(found).isNotPresent();
    }

    @Test
    void testSaveUser_WhenEmailIsDuplicate_ShouldThrowException() {
        createAndPersistValidAddress("duplicate@email.com");

        // --- CORREÇÃO APLICADA ---
        // Usamos o "nome completo" da classe de exceção do Hibernate
        org.hibernate.exception.ConstraintViolationException exception = assertThrows(
                org.hibernate.exception.ConstraintViolationException.class, // <-- Nome completo aqui
                () -> {
                    User user2 = new User(
                            "otheruser",
                            "duplicate@email.com",
                            "P@ssword456",
                            "98765432100"
                    );
                    entityManager.persistAndFlush(user2);
                }
        );

        // O método .getCause() agora é resolvido corretamente
        assertThat(exception.getCause().getMessage()).contains("violates unique constraint \"users_email_key\"");
    }

    @Test
    void testSaveAddress_WhenStreetIsNull_ShouldThrowException() {
        User user = new User("testuser", "email@test.com", "Pass123", "11122233344");
        entityManager.persistAndFlush(user);

        Address address = new Address();
        address.setStreet(null);
        address.setBuildingName("Junit Building");
        address.setCity("Test City");
        address.setState("TS");
        address.setCountry("Testland");
        address.setCep("12345-678");
        address.setUser(user);

        // Este teste já estava correto, pois usa o ConstraintViolationException
        // que importamos (o do 'jakarta.validation')
        assertThrows(
                ConstraintViolationException.class,
                () -> {
                    entityManager.persistAndFlush(address);
                }
        );
    }

    @Test
    void testOrphanRemoval_WhenRemovingAddressFromUser_ShouldDeleteAddress() {
        Address savedAddress = createAndPersistValidAddress("orphan.test@email.com");
        Long userId = savedAddress.getUser().getUserId();
        Long addressId = savedAddress.getAddressId();

        assertThat(addressRepository.findById(addressId)).isPresent();

        User user = entityManager.find(User.class, userId);
        assertThat(user.getAddresses()).hasSize(1);

        user.getAddresses().remove(0);

        entityManager.persistAndFlush(user);
        entityManager.clear();

        assertThat(addressRepository.findById(addressId)).isNotPresent();
    }
}