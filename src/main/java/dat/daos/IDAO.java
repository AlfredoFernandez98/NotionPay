package dat.daos;



import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

public interface IDAO<T> {
    Set<T> readAll();
    Optional<T> read(Long id);
    T create(T entity);
    Optional<T> update(Long id, T entity);
    void delete(Long id);
}
