package com.example.eventmanagement.commonPackage;

import com.example.eventmanagement.entity.BaseEntity;
import com.example.eventmanagement.excepition.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public abstract class GenericServiceImplementation<T extends BaseEntity, R extends JpaRepository<T,Long>> implements GenericService<T> {

    protected final R repository;
    private final String entityName;


    protected GenericServiceImplementation(R repository, String entityName) {
        this.repository = repository;
        this.entityName = entityName;
    }

    protected abstract void copyFields(T source, T target);

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll(){
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page <T> findAll(Pageable pageable){
        return repository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public T findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(entityName, id));
    }
    @Override
    public T create(T entity){
        entity.setId(null);
        return repository.save(entity);
    }

    @Override
    public T update(Long id, T entity) {
        T existing = findById(id);
        copyFields(entity, existing);
        return repository.save(existing);
    }

    @Override
    public void delete(Long id) {
        repository.delete(findById(id));
    }
}
