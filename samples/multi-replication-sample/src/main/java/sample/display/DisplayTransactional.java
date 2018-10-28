package sample.display;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Transactional("displayTransactionManager")
public @interface DisplayTransactional {
    /**
     * @see Transactional#propagation()
     */
    @AliasFor(annotation = Transactional.class, attribute = "propagation")
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * @see Transactional#isolation()
     */
    @AliasFor(annotation = Transactional.class, attribute = "isolation")
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * @see Transactional#timeout()
     */
    @AliasFor(annotation = Transactional.class, attribute = "timeout")
    int timeout() default TransactionDefinition.TIMEOUT_DEFAULT;

    /**
     * @see Transactional#readOnly()
     */
    @AliasFor(annotation = Transactional.class, attribute = "readOnly")
    boolean readOnly() default false;

    /**
     * @see Transactional#rollbackFor()
     */
    @AliasFor(annotation = Transactional.class, attribute = "rollbackFor")
    Class<? extends Throwable>[] rollbackFor() default {};

    /**
     * @see Transactional#rollbackForClassName()
     */
    @AliasFor(annotation = Transactional.class, attribute = "rollbackForClassName")
    String[] rollbackForClassName() default {};

    /**
     * @see Transactional#noRollbackFor()
     */
    @AliasFor(annotation = Transactional.class, attribute = "noRollbackFor")
    Class<? extends Throwable>[] noRollbackFor() default {};

    /**
     * @see Transactional#noRollbackForClassName()
     */
    @AliasFor(annotation = Transactional.class, attribute = "noRollbackForClassName")
    String[] noRollbackForClassName() default {};
}
