package care.better.abac.rest;

import care.better.abac.dto.PartyTypeDto;
import care.better.abac.jpa.entity.PartyType;
import care.better.abac.jpa.repo.PartyTypeRepository;
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
@RequestMapping("/rest/v1/admin/partyType")
@Transactional
public class PartyTypeResource {
    private final PartyTypeRepository partyTypeRepository;

    @Autowired
    public PartyTypeResource(PartyTypeRepository partyTypeRepository) {
        this.partyTypeRepository = partyTypeRepository;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PartyTypeDto> findAll() {
        return StreamSupport.stream(partyTypeRepository.findAll().spliterator(), false)
                .map(pt -> new PartyTypeDto(pt.getId(), pt.getName()))
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PartyTypeDto> findOne(@PathVariable("id") Long id) {
        return partyTypeRepository.findById(id)
                .map(entity -> new PartyTypeDto(entity.getId(), entity.getName())).map(ResponseEntity::ok)
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @RequestMapping(value = "/name/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PartyTypeDto> findOneByName(@PathVariable("name") String name) {
        return Optional.ofNullable(partyTypeRepository.findByName(name))
                .map(entity -> new PartyTypeDto(entity.getId(), entity.getName())).map(ResponseEntity::ok)
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PartyTypeDto> create(@RequestBody PartyTypeDto dto) {
        PartyType partyType = new PartyType();
        partyType.setName(dto.getName());
        PartyType saved = partyTypeRepository.save(partyType);

        return ResponseEntity.created(linkTo(methodOn(PartyTypeResource.class).findOne(saved.getId())).toUri()).body(map(saved));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PartyTypeDto> update(@PathVariable("id") Long id, @RequestBody PartyTypeDto dto) {
        return partyTypeRepository.findById(id).map(entity -> {
                    entity.setName(dto.getName());
                    return ResponseEntity.ok(map(entity));
                })
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        try {
            partyTypeRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (EmptyResultDataAccessException ignored) {
            return ResponseEntity.notFound().build();
        }
    }


    private PartyTypeDto map(PartyType partyType) {
        return new PartyTypeDto(partyType.getId(), partyType.getName());
    }
}
