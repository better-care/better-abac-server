package care.better.abac.rest;

import care.better.abac.dto.PartyRelationDto;
import care.better.abac.exception.PartyRelationInvalidTypesException;
import care.better.abac.jpa.entity.PartyRelation;
import care.better.abac.jpa.entity.PartyType;
import care.better.abac.jpa.repo.PartyRelationRepository;
import care.better.abac.jpa.repo.PartyRepository;
import care.better.abac.jpa.repo.RelationTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * @author Bostjan Lah
 */
@Component
@RestController
@RequestMapping("/rest/v1/admin/partyRelation")
public class PartyRelationResource {
    private final PartyRepository partyRepository;
    private final RelationTypeRepository relationTypeRepository;
    private final PartyRelationRepository partyRelationRepository;

    @Autowired
    public PartyRelationResource(
            PartyRepository partyRepository,
            RelationTypeRepository relationTypeRepository,
            PartyRelationRepository partyRelationRepository) {
        this.partyRepository = partyRepository;
        this.relationTypeRepository = relationTypeRepository;
        this.partyRelationRepository = partyRelationRepository;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public List<PartyRelationDto> findAll() {
        return StreamSupport.stream(partyRelationRepository.findAll().spliterator(), false)
                .map(this::map)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public PartyRelationDto findOne(@PathVariable("id") Long id) {
        PartyRelation partyRelation = partyRelationRepository.findById(id).get();
        return partyRelation == null ? null : map(partyRelation);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<PartyRelationDto> create(@RequestBody PartyRelationDto dto) {
        PartyRelation partyRelation = new PartyRelation();
        map(dto, partyRelation);
        validate(partyRelation);
        PartyRelation saved = partyRelationRepository.save(partyRelation);
        return ResponseEntity.created(linkTo(methodOn(PartyRelationResource.class).findOne(saved.getId())).toUri()).body(map(saved));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<PartyRelationDto> update(@PathVariable("id") Long id, @RequestBody PartyRelationDto dto) {
        PartyRelation partyRelation = partyRelationRepository.findById(id).get();
        map(dto, partyRelation);
        validate(partyRelation);
        return ResponseEntity.ok(map(partyRelation));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        partyRelationRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private void validate(PartyRelation partyRelation) {
        PartyType allowedSource = partyRelation.getRelationType().getAllowedSource();
        if (allowedSource != null && !allowedSource.equals(partyRelation.getSource().getType())) {
            throw new PartyRelationInvalidTypesException(
                    "Invalid source party type: " + partyRelation.getSource().getType().getName() + ", allowed type: " + allowedSource.getName() + '!');
        }
        PartyType allowedTarget = partyRelation.getRelationType().getAllowedTarget();
        if (allowedTarget != null && !allowedTarget.equals(partyRelation.getTarget().getType())) {
            throw new PartyRelationInvalidTypesException(
                    "Invalid target party type: " + partyRelation.getTarget().getType().getName() + ", allowed type: " + allowedTarget.getName() + '!');
        }
    }

    private PartyRelationDto map(PartyRelation partyRelation) {
        PartyRelationDto dto = new PartyRelationDto(partyRelation.getId(),
                                                    partyRelation.getSource().getId(),
                                                    partyRelation.getRelationType().getName(),
                                                    partyRelation.getTarget().getId());
        dto.setValidUntil(partyRelation.getValidUntil());
        return dto;
    }

    private void map(PartyRelationDto dto, PartyRelation partyRelation) {
        partyRelation.setSource(partyRepository.findById(dto.getSource()).get());
        partyRelation.setTarget(partyRepository.findById(dto.getTarget()).get());
        partyRelation.setRelationType(relationTypeRepository.findByName(dto.getRelationType()));
        partyRelation.setValidUntil(dto.getValidUntil());
    }
}
