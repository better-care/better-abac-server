package care.better.abac.rest;

import care.better.abac.dto.RelationTypeDto;
import care.better.abac.jpa.entity.RelationType;
import care.better.abac.jpa.repo.PartyTypeRepository;
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
@RequestMapping("/rest/v1/admin/relationType")
@Transactional
public class RelationTypeResource {
    private final RelationTypeRepository relationTypeRepository;
    private final PartyTypeRepository partyTypeRepository;

    @Autowired
    public RelationTypeResource(RelationTypeRepository relationTypeRepository, PartyTypeRepository partyTypeRepository) {
        this.relationTypeRepository = relationTypeRepository;
        this.partyTypeRepository = partyTypeRepository;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RelationTypeDto> findAll() {
        return StreamSupport.stream(relationTypeRepository.findAll().spliterator(), false)
                .map(this::map)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public RelationTypeDto findOne(@PathVariable("id") Long id) {
        RelationType relationType = relationTypeRepository.findById(id).get();
        return relationType == null ? null : map(relationType);
    }

    @RequestMapping(value = "/name/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public RelationTypeDto findOneByName(@PathVariable("name") String name) {
        RelationType relationType = relationTypeRepository.findByName(name);
        return relationType == null ? null : map(relationType);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RelationTypeDto> create(@RequestBody RelationTypeDto dto) {
        RelationType relationType = new RelationType();
        relationType.setName(dto.getName());
        if (dto.getAllowedSourcePartyType() != null) {
            relationType.setAllowedSource(partyTypeRepository.findByName(dto.getAllowedSourcePartyType()));
        }
        if (dto.getAllowedTargetPartyType() != null) {
            relationType.setAllowedTarget(partyTypeRepository.findByName(dto.getAllowedTargetPartyType()));
        }
        RelationType saved = relationTypeRepository.save(relationType);

        return ResponseEntity.created(linkTo(methodOn(RelationTypeResource.class).findOne(saved.getId())).toUri()).body(map(saved));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RelationTypeDto> update(@PathVariable("id") Long id, @RequestBody RelationTypeDto dto) {
        final RelationType relationType = relationTypeRepository.findById(id).get();
        if (dto.getName() != null) {
            relationType.setName(dto.getName());
        }
        if (dto.getAllowedSourcePartyType() != null) {
            relationType.setAllowedSource(partyTypeRepository.findByName(dto.getAllowedSourcePartyType()));
        }
        if (dto.getAllowedTargetPartyType() != null) {
            relationType.setAllowedTarget(partyTypeRepository.findByName(dto.getAllowedTargetPartyType()));
        }
        RelationType updated = relationTypeRepository.save(relationType);

        return ResponseEntity.ok(map(updated));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        relationTypeRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private RelationTypeDto map(RelationType relationType) {
        RelationTypeDto dto = new RelationTypeDto(relationType.getId(), relationType.getName());
        dto.setAllowedSourcePartyType(relationType.getAllowedSource().getName());
        dto.setAllowedTargetPartyType(relationType.getAllowedTarget().getName());
        return dto;
    }
}
