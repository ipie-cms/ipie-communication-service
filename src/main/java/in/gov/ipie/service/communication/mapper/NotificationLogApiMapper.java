package in.gov.ipie.service.communication.mapper;

import org.mapstruct.Mapper;

import in.gov.ipie.service.communication.domain.NotificationLog;
import in.gov.ipie.service.communication.dto.response.NotificationLogResponse;

/** MapStruct mapping between the API's response DTO and the domain model (master standards doc, 5.2). */
@Mapper(componentModel = "spring")
public interface NotificationLogApiMapper {

    NotificationLogResponse toResponse(NotificationLog notificationLog);
}
