package care.better.abac.rest;

import care.better.abac.dto.PartyRelationDto;
import care.better.abac.jpa.entity.PartyRelation;
import care.better.abac.jpa.repo.PartyRelationRepository;
import care.better.abac.jpa.repo.PartyRelationValidator;
import care.better.abac.jpa.repo.PartyRepository;
import care.better.abac.jpa.repo.RelationTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<PartyRelationDto> findAll() {
        return StreamSupport.stream(partyRelationRepository.findAll().spliterator(), false)
                .map(this::map)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PartyRelationDto> findOne(@PathVariable("id") Long id) {
        return partyRelationRepository.findById(id)
                .map(this::map).map(ResponseEntity::ok)
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<PartyRelationDto> create(@RequestBody PartyRelationDto dto) {
        PartyRelation partyRelation = new PartyRelation();
        map(dto, partyRelation);
        PartyRelationValidator.validate(partyRelation);
        PartyRelation saved = partyRelationRepository.save(partyRelation);
        return ResponseEntity.created(linkTo(methodOn(PartyRelationResource.class).findOne(saved.getId())).toUri()).body(map(saved));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<PartyRelationDto> update(@PathVariable("id") Long id, @RequestBody PartyRelationDto dto) {
        PartyRelation partyRelation = new PartyRelation();
        map(dto, partyRelation);
        return partyRelationRepository.update(id, partyRelation).map(entity -> ResponseEntity.ok(map(entity))).orElseGet(ResponseEntity.notFound()::build);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        try {
            partyRelationRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (EmptyResultDataAccessException ignored) {
            return ResponseEntity.notFound().build();
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
