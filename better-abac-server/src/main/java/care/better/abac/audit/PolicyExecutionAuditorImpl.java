package care.better.abac.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import care.better.abac.exception.PolicyExecutionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author Bostjan Lah
 */
@Component
public class PolicyExecutionAuditorImpl implements PolicyExecutionAuditor {
    private static final Logger log = LogManager.getLogger(PolicyExecutionAuditorImpl.class.getName());

    private final ObjectMapper objectMapper;

    @Autowired
    public PolicyExecutionAuditorImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void auditCall(String callId, List<ExecutionAuditEntry> auditLog) {
        try {
            log.info(objectMapper.writeValueAsString(new AuditLogWrapper(callId, auditLog, ZonedDateTime.now())));
        } catch (JsonProcessingException e) {
            throw new PolicyExecutionException("Unable to write execution log!", e);
        }
    }

    private static final class AuditLogWrapper {
        private String callId;
        private List<ExecutionAuditEntry> auditLog;
        private ZonedDateTime timestamp;

        private AuditLogWrapper(String callId, List<ExecutionAuditEntry> auditLog, ZonedDateTime timestamp) {
            this.callId = callId;
            this.auditLog = auditLog;
            this.timestamp = timestamp;
        }

        public String getCallId() {
            return callId;
        }

        public void setCallId(String callId) {
            this.callId = callId;
        }

        public List<ExecutionAuditEntry> getAuditLog() {
            return auditLog;
        }

        public void setAuditLog(List<ExecutionAuditEntry> auditLog) {
            this.auditLog = auditLog;
        }

        public ZonedDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(ZonedDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}
