package care.better.abac.dto.content;

/**
 * @author Matic Ribic
 */
public class PlainPartyTypeDto extends NamedPlainDto {

    public PlainPartyTypeDto() {
    }

    public PlainPartyTypeDto(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return String.format("PlainPartyTypeDto{name='%s'}", getName());
    }
}
