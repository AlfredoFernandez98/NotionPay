package dat.daos.impl;

import dat.daos.IDAO;
import dat.entities.Receipt;

import java.util.Optional;
import java.util.Set;

public class ReceiptDAO implements IDAO<Receipt> {
    @Override
    public Receipt create(Receipt entity) {
        return null;
    }

    @Override
    public Optional<Receipt> getById(Long id) {
        return Optional.empty();
    }

    @Override
    public Set<Receipt> getAll() {
        return Set.of();
    }

    @Override
    public void update(Receipt entity) {

    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public Optional<Receipt> findByName(String name) {
        return Optional.empty();
    }
}
