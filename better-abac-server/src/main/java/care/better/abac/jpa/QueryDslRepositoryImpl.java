package care.better.abac.jpa;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.QuerydslJpaRepository;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;

import javax.persistence.EntityManager;
import java.io.Serializable;

/**
 * @author Andrej Dolenc
 */
public class QueryDslRepositoryImpl<T, I extends Serializable> extends QuerydslJpaRepository<T, I> implements QueryDslRepository<T, I> {

    @Getter
    private final EntityPathBase<T> entityPath;

    @Getter
    private final JPAQueryFactory queryFactory;

    public QueryDslRepositoryImpl(
            @NonNull JpaEntityInformation<T, I> entityInformation,
            @NonNull EntityManager entityManager) {
        this(entityInformation, entityManager, SimpleEntityPathResolver.INSTANCE);
    }

    public QueryDslRepositoryImpl(
            @NonNull JpaEntityInformation<T, I> entityInformation,
            @NonNull EntityManager entityManager,
            @NonNull EntityPathResolver resolver) {
        super(entityInformation, entityManager, resolver);
        queryFactory = new JPAQueryFactory(entityManager);
        entityPath = createPath(resolver, entityInformation);
    }

    private EntityPathBase<T> createPath(EntityPathResolver resolver, JpaEntityInformation<T, I> entityInformation) {
        EntityPath<T> path = resolver.createPath(entityInformation.getJavaType());
        return new EntityPathBase<>(path.getType(), path.getMetadata());
    }
}
