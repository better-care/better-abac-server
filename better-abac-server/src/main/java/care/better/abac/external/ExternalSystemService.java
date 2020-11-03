package care.better.abac.external;

import care.better.abac.dto.config.ExternalSystemDto;
import care.better.abac.dto.config.ExternalSystemInputDto;
import care.better.abac.dto.config.ExternalSystemValidationStatus;
import care.better.abac.jpa.entity.ExternalSystemEntity;
import care.better.abac.rest.client.ValidationException;

import java.util.List;

/**
 * @author Matic Ribic
 */
public interface ExternalSystemService {

    ExternalSystemDto getConfigDto(String systemId);

    ExternalSystemEntity createConfigAndNotify(ExternalSystemInputDto inputDto, boolean validate) throws ValidationException;

    ExternalSystemEntity createConfig(ExternalSystemInputDto inputDto, boolean validate) throws ValidationException;

    ExternalSystemEntity updateConfigAndNotify(String systemId, ExternalSystemInputDto inputDto, boolean validate) throws ValidationException;

    ExternalSystemEntity updateConfig(String systemId, ExternalSystemInputDto inputDto, boolean validate) throws ValidationException;

    ExternalSystemDto validate(String systemId, boolean updateStatus) throws ValidationException;

    void updateValidationStatus(String systemId, ExternalSystemValidationStatus validationStatus);

    List<ExternalSystemEntity> getConfigListToValidate();
}
