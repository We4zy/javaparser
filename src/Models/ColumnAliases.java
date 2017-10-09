package Models;

/**
 * @author rory richter
 *
 */
public @interface ColumnAliases {
	String[] aliases() default "";
}