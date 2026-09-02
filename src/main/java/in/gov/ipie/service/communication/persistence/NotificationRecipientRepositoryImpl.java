package in.gov.ipie.service.communication.persistence;

import java.util.Optional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import in.gov.ipie.service.communication.repository.NotificationRecipientRepository;

@Repository
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class NotificationRecipientRepositoryImpl implements NotificationRecipientRepository {

    private final NotificationRecipientJpaRepository jpaRepository;

    @Override
    public Optional<String> findEmailByPurpose(String purpose) {
        return jpaRepository.findFirstByPurpose(purpose).map(NotificationRecipientJpaEntity::getEmail);
    }
}
