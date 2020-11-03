package care.better.abac.jpa;

import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

/**
 * @author Andrej Dolenc
 */
@NoRepositoryBean
public interface QueryDslRepository<T, I> extends Repository<T, I>, QuerydslPredicateExecutor<T> {

    EntityPathBase<T> getEntityPath();

    JPAQueryFactory getQueryFactory();
}
