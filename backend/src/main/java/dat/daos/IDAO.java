package dat.daos;



import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

public interface IDAO<T> {
    T create(T entity);
    Optional<T> getById(Long id);
    Set<T> getAll();
    void update(T entity);
    void delete(Long id);
    Optional<T> findByName(String name);
}
