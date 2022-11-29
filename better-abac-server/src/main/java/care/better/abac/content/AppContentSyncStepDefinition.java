package care.better.abac.content;

import care.better.abac.dto.content.PlainDto;
import care.better.abac.jpa.entity.EntityWithId;

/**
 * @author Matic Ribic
 */
public class AppContentSyncStepDefinition<T extends PlainDto, U extends EntityWithId> {
    private final AppContentSyncStep<T, U> step;
    private final AppContentSyncStepActionType actionType;

    public AppContentSyncStepDefinition(AppContentSyncStep<T, U> step, AppContentSyncStepActionType actionType) {
        this.step = step;
        this.actionType = actionType;
    }

    public AppContentSyncStep<T, U> getStep() {
        return step;
    }

    public AppContentSyncStepActionType getActionType() {
        return actionType;
    }
}
