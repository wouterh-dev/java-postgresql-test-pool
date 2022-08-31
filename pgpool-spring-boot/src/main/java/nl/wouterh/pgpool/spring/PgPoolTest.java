package nl.wouterh.pgpool.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;

/**
 * Enables Spring Boot PgPool for a test
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@TestExecutionListeners(listeners = {PgPoolTestExecutionListener.class},
    mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
@Import(PgPoolAutoConfiguration.class)
public @interface PgPoolTest {

}
