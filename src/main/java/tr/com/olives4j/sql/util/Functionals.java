package tr.com.olives4j.sql.util;

public interface Functionals {
	public interface Consumer<T> {
	    void accept(T t);
	}

	public interface Supplier<T> {
	    T get();
	}

	public interface Function<T, R> {
	    R apply(T t);
	}

	public interface Predicate<T> {
	    boolean test(T t);
	}
}
