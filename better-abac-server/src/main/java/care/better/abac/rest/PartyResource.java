package care.better.abac.rest;

import care.better.abac.FixedTypes;
import care.better.abac.dto.PartyDto;
import care.better.abac.external.PartyInfoService;
import care.better.abac.jpa.entity.Party;
import care.better.abac.jpa.repo.PartyRepository;
import care.better.abac.jpa.repo.PartyTypeRepository;
import care.better.core.Opt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * @author Bostjan Lah
 */
@Component
@RestController
@RequestMapping("/rest/v1/admin/party")
public class PartyResource {
    private final PartyTypeRepository partyTypeRepository;
    private final PartyRepository partyRepository;

    private final Map<String, PartyInfoService> partyInfoServices = new HashMap<>();

    @Autowired
    public PartyResource(
            PartyTypeRepository partyTypeRepository,
            PartyRepository partyRepository,
            @Qualifier("userPartyInfoService") PartyInfoService userPartyInfoService,
            @Qualifier("patientPartyInfoService") PartyInfoService patientPartyInfoService) {
        this.partyTypeRepository = partyTypeRepository;
        this.partyRepository = partyRepository;

        partyInfoServices.put(FixedTypes.USER.name(), userPartyInfoService);
        partyInfoServices.put(FixedTypes.PATIENT.name(), patientPartyInfoService);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PartyDto> findAll() {
        return StreamSupport.stream(partyRepository.findAll().spliterator(), false)
                .map(this::map)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PartyDto> findOne(@PathVariable("id") Long id) {
        return partyRepository.findById(id)
                .map(this::map).map(ResponseEntity::ok)
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @RequestMapping(value = "/externalId/{externalId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PartyDto> findById(@PathVariable("externalId") String externalId) {
        return partyRepository.findByExternalIds(externalId).stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/{type}/{externalId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PartyDto> findOneByTypeAndId(@PathVariable("type") String type, @PathVariable("externalId") String externalId) {
        return Optional.ofNullable(partyRepository.findByTypeAndExternalId(type, externalId))
                .map(this::map).map(ResponseEntity::ok)
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<PartyDto> create(@RequestBody PartyDto dto) {
        Party party = new Party();
        map(dto, party);
        Party saved = partyRepository.save(party);

        return ResponseEntity.created(linkTo(methodOn(PartyResource.class).findOne(saved.getId())).toUri()).body(map(saved));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<PartyDto> update(@PathVariable("id") Long id, @RequestBody PartyDto dto) {
        Party party = new Party();
        map(dto, party);
        return partyRepository.update(id, party)
                .map(entity -> ResponseEntity.ok(map(entity)))
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        try {
            partyRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (EmptyResultDataAccessException ignored) {
            return ResponseEntity.notFound().build();
        }
    }

    private PartyDto map(Party party) {
        PartyDto dto = new PartyDto(party.getId(), party.getType().getName());
        if (!party.getExternalIds().isEmpty()) {
            dto.setExternalIds(new HashSet<>(party.getExternalIds()));
            String partyTypeName = party.getType().getName();
            PartyInfoService partyInfoService = partyInfoServices.get(partyTypeName);
            dto.setFullName(Opt.of(partyInfoService)
                                    .map(s -> partyInfoService.getFullName(dto.getExternalIds()))
                                    .orElse(String.join(",", dto.getExternalIds())));
        }
        return dto;
    }

    private void map(PartyDto dto, Party party) {
        party.setType(partyTypeRepository.findByName(dto.getType()));
        if (dto.getExternalIds() != null) {
            MappingUtils.synchronizeSets(dto.getExternalIds(), party.getExternalIds());
        }
    }
}
