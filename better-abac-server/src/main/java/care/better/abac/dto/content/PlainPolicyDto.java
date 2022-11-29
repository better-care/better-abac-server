package care.better.abac.dto.content;

import java.util.Objects;

/**
 * @author Matic Ribic
 */
public class PlainPolicyDto extends NamedPlainDto {
    private String policy;

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlainPolicyDto)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        PlainPolicyDto that = (PlainPolicyDto)o;
        return Objects.equals(policy, that.policy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), policy);
    }

    @Override
    public String toString() {
        return String.format("PlainPolicyDto{name='%s', policy='%s'}", getName(), policy);
    }
}
