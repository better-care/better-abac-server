package care.better.abac.rest;

import care.better.abac.jpa.repo.PartyRelationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author Bostjan Lah
 */
@Component
@RestController
@RequestMapping("/rest/v1/relation")
public class UserRelationResource {
    private final PartyRelationRepository partyRelationRepository;

    @Autowired
    public UserRelationResource(PartyRelationRepository partyRelationRepository) {
        this.partyRelationRepository = partyRelationRepository;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public List<String> find(@RequestParam("name") String relationName) {
        return partyRelationRepository.findTargetIds(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString(), relationName,
                                                     OffsetDateTime.now());
    }
}
