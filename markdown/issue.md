### 问题集
1. 在创建的DTO中，如果有人传入id，则插入数据会使用该id导致主键混乱
   + 使用自定义注解做“禁止传入字段”
   ```java
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = NotAllowedValidator.class)
    public @interface NotAllowed {
        String message() default "该字段禁止传入";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
    // 需要编写校验器规则
    public class NotAllowedValidator implements ConstraintValidator<NotAllowed, Object> {
        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {
            return value == null;  // 值必须为空，传了就验证失败
        }
    }
    // 这样调用
    @NotAllowed(groups = CreateGroup.class)  // 创建场景禁止传id
    private Long id;
    ```
    + 我还是选择多DTO了

2. token长度变长导致存入不了数据库，无法登录，目前选择增大数据库中的token字段类型为varchar(1024)，后面修改存入redis。