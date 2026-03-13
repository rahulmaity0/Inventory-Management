package com.rahul.inventorybilling.repository;

import com.rahul.inventorybilling.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}
