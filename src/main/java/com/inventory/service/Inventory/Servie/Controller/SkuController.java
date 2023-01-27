package com.inventory.service.Inventory.Servie.Controller;

import com.inventory.service.Inventory.Servie.Entity.SKU;
import com.inventory.service.Inventory.Servie.dao.SkuDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
public class SkuController {

    @Autowired
    private SkuDao skuDao;
    @GetMapping("sku")
    public List<SKU>  getAllSku(){
        return skuDao.getAll();
    }

    @PostMapping("sku")
    public ResponseEntity<Object> addSku(@RequestBody SKU sku) throws SQLException {
        skuDao.mapContainer("POST" ,0, 0, sku);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    @PutMapping("sku/{id}")
    public ResponseEntity<Object> addQuantity(@PathVariable int id, @RequestParam("quantity") int quantity) throws SQLException{
        skuDao.mapContainer("PUT", quantity, id, null);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
}
