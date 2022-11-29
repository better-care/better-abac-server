package care.better.abac.content;

/**
 * @author Matic Ribic
 */
public interface AppContentService {

    AppContent getContent(AppContentRequestContext requestContext);

    AppContentSyncResult submitContent(AppContent contentDto, AppContentRequestContext requestContext);
}
