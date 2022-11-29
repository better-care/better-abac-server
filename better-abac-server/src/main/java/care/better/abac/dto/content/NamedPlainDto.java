package care.better.abac.dto.content;

import care.better.abac.jpa.entity.Named;

import java.util.Objects;

/**
 * @author Matic Ribic
 */
public abstract class NamedPlainDto implements PlainDto, Named {
    private String name;

    protected NamedPlainDto() {
    }

    protected NamedPlainDto(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NamedPlainDto that = (NamedPlainDto)o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
