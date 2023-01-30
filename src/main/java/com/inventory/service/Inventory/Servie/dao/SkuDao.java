package com.inventory.service.Inventory.Servie.dao;

import com.inventory.service.Inventory.Servie.Entity.SKU;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
public interface SkuDao{

    List<SKU> getAll();


    void mapContainer(String post, int added_quantity, int added_id, Object object) throws SQLException;

    SKU getSkuById(int id);
}
