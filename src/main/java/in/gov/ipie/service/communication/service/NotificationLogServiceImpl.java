package in.gov.ipie.service.communication.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.gov.ipie.common.core.paging.CursorPageRequest;
import in.gov.ipie.common.core.paging.CursorPageResult;
import in.gov.ipie.service.communication.domain.NotificationLog;
import in.gov.ipie.service.communication.repository.NotificationLogRepository;

/** {@link NotificationLogService} implementation. */
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class NotificationLogServiceImpl implements NotificationLogService {

    private final NotificationLogRepository logRepository;

    @Override
    @Transactional(readOnly = true)
    public CursorPageResult<NotificationLog> listNotifications(CursorPageRequest pageRequest) {
        return logRepository.searchAfter(pageRequest);
    }
}
