package Models;

public @interface ColumnAliases {
	String[] aliases() default "";
}