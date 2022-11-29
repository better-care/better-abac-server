package care.better.abac.dto.content;

import java.util.Objects;

/**
 * @author Matic Ribic
 */
public class DtoSyncResult<T extends PlainDto> {
    private final T submittedValue;
    private final DtoSyncResultState state;
    private final T previousValue;

    public DtoSyncResult(T submittedValue, DtoSyncResultState state, T previousValue) {
        this.submittedValue = submittedValue;
        this.state = state;
        this.previousValue = previousValue;
    }

    public static <U extends PlainDto> DtoSyncResult<U> created(U submittedDto) {
        return new DtoSyncResult<>(submittedDto, DtoSyncResultState.CREATED, null);
    }

    public static <U extends PlainDto> DtoSyncResult<U> updated(U submittedDto, U updatedDto) {
        return new DtoSyncResult<>(submittedDto, DtoSyncResultState.UPDATED, updatedDto);
    }

    public static <U extends PlainDto> DtoSyncResult<U> unmodified(U submittedDto) {
        return new DtoSyncResult<>(submittedDto, DtoSyncResultState.UNMODIFIED, null);
    }

    public static <U extends PlainDto> DtoSyncResult<U> deleted(U deletedDto) {
        return new DtoSyncResult<>(null, DtoSyncResultState.DELETED, deletedDto);
    }

    public T getSubmittedValue() {
        return submittedValue;
    }

    public DtoSyncResultState getState() {
        return state;
    }

    public T getPreviousValue() {
        return previousValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DtoSyncResult)) {
            return false;
        }
        DtoSyncResult<?> that = (DtoSyncResult<?>)o;
        return Objects.equals(submittedValue, that.submittedValue) && state == that.state && Objects.equals(previousValue, that.previousValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(submittedValue, state, previousValue);
    }

    @Override
    public String toString() {
        return String.format("DtoSyncResult{state='%s', submittedValue='%s', previousValue='%s'}", state, submittedValue, previousValue);
    }
}
