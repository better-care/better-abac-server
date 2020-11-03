package care.better.abac.external;

import care.better.abac.dto.config.ExternalSystemDto;
import care.better.abac.dto.config.ExternalSystemEventType;
import care.better.abac.dto.config.ExternalSystemInputDto;
import care.better.abac.dto.config.ExternalSystemMapper;
import care.better.abac.dto.config.ExternalSystemValidationStatus;
import care.better.abac.jpa.entity.ExternalSystemEntity;
import care.better.abac.jpa.repo.ExternalSystemRepository;
import care.better.abac.rest.client.ExternalSystemRestClient;
import care.better.abac.rest.client.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Matic Ribic
 */
public class ExternalSystemServiceImpl implements ExternalSystemService {
    private final ExternalSystemRepository externalSystemRepository;
    private final ExternalSystemMapper mapper;
    private final ExternalSystemRestClient externalSystemRestClient;

    @Inject
    private ExternalSystemService self;

    public ExternalSystemServiceImpl(
            ExternalSystemRepository externalSystemRepository,
            ExternalSystemMapper mapper,
            ExternalSystemRestClient externalSystemRestClient) {
        this.externalSystemRepository = externalSystemRepository;
        this.mapper = mapper;
        this.externalSystemRestClient = externalSystemRestClient;
    }

    @Override
    @Transactional(readOnly = true)
    public ExternalSystemDto getConfigDto(String systemId) {
        ExternalSystemEntity externalSystemEntity = externalSystemRepository.findByExternalId(systemId);
        return externalSystemEntity != null ? mapper.toDto(externalSystemEntity) : null;
    }

    @Override
    public ExternalSystemEntity createConfigAndNotify(ExternalSystemInputDto inputDto, boolean validate) throws ValidationException {
        ExternalSystemEntity config = self.createConfig(inputDto, validate);
        notifyExternalSystemOnRefresh(inputDto);
        return config;
    }

    @Override
    @Transactional
    public ExternalSystemEntity createConfig(ExternalSystemInputDto inputDto, boolean validate) throws ValidationException {
        ExternalSystemValidationStatus validationStatus = ExternalSystemValidationStatus.UNKNOWN;
        if (validate && StringUtils.isNotBlank(inputDto.getAbacRestBaseUrl())) {
            externalSystemRestClient.validateConfiguration(inputDto);
            validationStatus = ExternalSystemValidationStatus.VALID;
        }

        ExternalSystemEntity entity = mapper.toEntity(inputDto, validationStatus);
        return externalSystemRepository.save(entity);
    }

    @Override
    public ExternalSystemEntity updateConfigAndNotify(String systemId, ExternalSystemInputDto inputDto, boolean validate) throws ValidationException {
        ExternalSystemEntity config = self.updateConfig(systemId, inputDto, validate);
        notifyExternalSystemOnRefresh(inputDto);
        return config;
    }

    @Override
    @Transactional
    public ExternalSystemEntity updateConfig(String systemId, ExternalSystemInputDto inputDto, boolean validate) throws ValidationException {
        ExternalSystemValidationStatus validationStatus = ExternalSystemValidationStatus.UNKNOWN;
        if (validate && StringUtils.isNotBlank(inputDto.getAbacRestBaseUrl())) {
            externalSystemRestClient.validateConfiguration(inputDto);
            validationStatus = ExternalSystemValidationStatus.VALID;
        }

        ExternalSystemEntity entity = externalSystemRepository.findByExternalId(systemId);
        if (entity == null) {
            return null;
        }

        mapper.updateEntity(entity, inputDto, validationStatus);
        return externalSystemRepository.save(entity);
    }

    @Override
    public ExternalSystemDto validate(String systemId, boolean updateStatus) throws ValidationException {
        ExternalSystemDto externalSystemDto = getConfigDto(systemId);
        if (StringUtils.isNotBlank(externalSystemDto.getAbacRestBaseUrl())) {
            try {
                externalSystemRestClient.validateConfiguration(mapper.toInputDto(externalSystemDto));
                if (updateStatus) {
                    self.updateValidationStatus(externalSystemDto.getExternalId(), ExternalSystemValidationStatus.VALID);
                    return getConfigDto(systemId);
                } else {
                    return externalSystemDto;
                }
            } catch (ValidationException e) {
                self.updateValidationStatus(externalSystemDto.getExternalId(), ExternalSystemValidationStatus.INVALID);
                throw e;
            }
        } else {
            if (updateStatus) {
                self.updateValidationStatus(externalSystemDto.getExternalId(), ExternalSystemValidationStatus.UNKNOWN);
            }
            throw ValidationException.unknown();
        }
    }

    @Override
    @Transactional
    public void updateValidationStatus(String systemId, ExternalSystemValidationStatus validationStatus) {
        ExternalSystemEntity entity = externalSystemRepository.findByExternalId(systemId);
        if (entity != null) {
            entity.setValidationStatus(validationStatus);
            externalSystemRepository.save(entity);
        }
    }

    @Override
    public List<ExternalSystemEntity> getConfigListToValidate() {
        return externalSystemRepository.findByValidationStatus(ExternalSystemValidationStatus.UNKNOWN);
    }

    private void notifyExternalSystemOnRefresh(ExternalSystemInputDto inputDto) {
        if (StringUtils.isNotBlank(inputDto.getAbacRestBaseUrl())) {
            externalSystemRestClient.notify(inputDto.getAbacRestBaseUrl(), ExternalSystemEventType.CONFIG_REFRESH);
        }
    }
}
