package de.freerider.repository;

import de.freerider.datamodel.Customer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

@Component
public class CustomerRepository implements CrudRepository<Customer, Long> {
    // mapping the customers to their IDs
    private HashMap<Long, Customer> customers = new HashMap<Long, Customer>();

    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param entity must not be {@literal null}.
     * @return the saved entity; will never be {@literal null}.
     * @throws IllegalArgumentException in case the given {@literal entity} is {@literal null}.
     */
    public <S extends Customer> S save( S entity ) {
        if(entity != null) {
            customers.put(entity.getId(), entity);
            return entity;
        }
        else throw new IllegalArgumentException("Entity must not be null!");
    }


    /**
     * Saves all given entities.
     *
     * @param entities must not be {@literal null} nor must it contain {@literal null}.
     * @return the saved entities; will never be {@literal null}. The returned {@literal Iterable} will have the same size
     *         as the {@literal Iterable} passed as an argument.
     * @throws IllegalArgumentException in case the given {@link Iterable entities} or one of its entities is
     *           {@literal null}.
     */
    public <S extends Customer> Iterable<S> saveAll( Iterable<S> entities ) {
        // null check
        for(S entity: entities) {
            if(entity == null) throw new IllegalArgumentException("Entities must not be null!");
        }

        for(S entity: entities) {
            save(entity);
        }

        return entities;
    }


    /**
     * Returns whether an entity with the given id exists.
     *
     * @param id must not be {@literal null}.
     * @return {@literal true} if an entity with the given id exists, {@literal false} otherwise.
     * @throws IllegalArgumentException if {@literal id} is {@literal null}.
     */
    public boolean existsById( Long id ) {
        if (id == null) throw new IllegalArgumentException("ID must not be null!");
        return customers.containsKey(id) ? true : false;
    }


    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id or {@literal Optional#empty()} if none found.
     * @throws IllegalArgumentException if {@literal id} is {@literal null}.
     */
    public Optional<Customer> findById( Long id ) {
        if (id == null) throw new IllegalArgumentException("ID must not be null!");
        return customers.containsKey(id) ? Optional.of(customers.get(id)) : Optional.empty();
    }


    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    public Iterable<Customer> findAll() {
        return customers.isEmpty() ? null : customers.values();
    }


    /**
     * Returns all instances of the type {@code T} with the given IDs.
     * <p>
     * If some or all ids are not found, no entities are returned for these IDs.
     * <p>
     * Note that the order of elements in the result is not guaranteed.
     *
     * @param ids must not be {@literal null} nor contain any {@literal null} values.
     * @return guaranteed to be not {@literal null}. The size can be equal or less than the number of given
     *         {@literal ids}.
     * @throws IllegalArgumentException in case the given {@link Iterable ids} or one of its items is {@literal null}.
     */
    public Iterable<Customer> findAllById( Iterable<Long> ids ) {
        // null check
        if(ids == null) throw new IllegalArgumentException("ids must not be null!");
        for(Long id: ids) {
            if(id == null) throw new IllegalArgumentException("id must not be null!");
        }
        ArrayList<Customer> foundCustomers = new ArrayList<Customer>();
        for(long id: ids) {
            if(existsById(id)) foundCustomers.add(customers.get(id));
        }
        return foundCustomers;
    }


    /**
     * Returns the number of entities available.
     *
     * @return the number of entities.
     */
    public long count() {
        return customers.size();
    }


    /**
     * Deletes the entity with the given id.
     *
     * @param id must not be {@literal null}.
     * @throws IllegalArgumentException in case the given {@literal id} is {@literal null}
     */
    public void deleteById( Long id ) {
        if(id == null) throw new IllegalArgumentException("id must not be null!");
        if(existsById(id)) customers.remove(id);
    }


    /**
     * Deletes a given entity.
     *
     * @param entity must not be {@literal null}.
     * @throws IllegalArgumentException in case the given entity is {@literal null}.
     */
    public void delete( Customer entity ) {
        if(entity == null) throw new IllegalArgumentException("entity must not be null!");
        if(customers.containsValue(entity)) customers.remove(entity.getId());
    }


    /**
     * Deletes all instances of the type {@code T} with the given IDs.
     *
     * @param ids must not be {@literal null}. Must not contain {@literal null} elements.
     * @throws IllegalArgumentException in case the given {@literal ids} or one of its elements is {@literal null}.
     * @since 2.5
     */
    public void deleteAllById( Iterable<? extends Long> ids ) {
        if(ids == null) throw new IllegalArgumentException("ids must not be null!");
        for(Long id: ids) {
            if(id == null) throw new IllegalArgumentException("id must not be null!");
        }
        for(long id: ids) {
            deleteById(id);
        }
    }


    /**
     * Deletes the given entities.
     *
     * @param entities must not be {@literal null}. Must not contain {@literal null} elements.
     * @throws IllegalArgumentException in case the given {@literal entities} or one of its entities is {@literal null}.
     */
    public void deleteAll( Iterable<? extends Customer> entities ) {
        if(entities == null) throw new IllegalArgumentException("entities must not be null!");
        for(Customer entity: entities) {
            if(entity == null) throw new IllegalArgumentException("entity must not be null!");
        }
        for(Customer entity: entities) {
            delete(entity);
        }
    }


    /**
     * Deletes all entities managed by the repository.
     */
    public void deleteAll() {
        customers.clear();
    }
}
