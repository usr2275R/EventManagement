package com.example.eventmanagement.commonPackage;

import com.example.eventmanagement.entity.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GenericService<T extends BaseEntity> {

    List<T> findAll();

    Page<T> findAll(Pageable pageable);

    T findById(Long id);

    T create(T entity);

    T update(Long id, T entity);

    void delete(Long id);
}
